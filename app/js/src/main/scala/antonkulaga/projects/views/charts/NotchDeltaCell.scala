package antonkulaga.projects.views.charts

import org.denigma.binding.binders.Events
import org.denigma.binding.extensions._
import org.denigma.controls.charts.Point
import org.scalajs.dom.Element
import rx.Rx
import rx.core.Var
import rx.ops._

import scala.scalajs.js

case class NotchDeltaCell(position: Point,
                          side: Double,
                          concentrations: Concentrations,
                          params: NotchDeltaParams,
                          neighbours: js.Array[Var[NotchDeltaCell]] = js.Array(),
                          previous: List[Concentrations] = Nil
                         ) extends Cell
{
  self =>


  def prettyDigit(d: Double): String = f"$d%1.2f"

  type Derivative = (Concentrations, Concentrations) => Double

  def neighboursSum: Concentrations = neighbours.foldLeft(Concentrations.empty)(
    (acc, nb) =>  acc + nb.now.concentrations
  )

  def neighboursAvg: Concentrations = neighboursSum / neighbours.size


  @inline def computeDelta(f: Derivative, neighbours: Concentrations, step: Double): Double =
  {
/*    val a = step * f(this.concentrations, neighbours)
    val b = step * f(this.concentrations + (0.5 * a), neighbours)
    val c = step * f(this.concentrations + (0.5 * b), neighbours)
    val d: Double = step * f(this.concentrations + c, neighbours)
    (a + 2.0 * b + 2.0 * c + d) / 6*/
    f(this.concentrations, neighbours) * step
  }

  protected def d_Notch(my: Concentrations, other: Concentrations): Double = {
    println(
      s"""
         |notchProd(${params.notchProd}) - my.notch(${my.notch}) * params.notchDecay(${params.notchDecay}) -
         |- other.delta(${other.delta})  * my.notch(${my.notch})  / kTrans(${params.kTrans})  -
         |- my.delta(${my.delta}) * my.notch${my.notch}  / params.kCis(${params.kCis})
       """.stripMargin)
    params.notchProd -  my.notch * params.notchDecay -
      other.delta  * my.notch  / params.kTrans  -
      my.delta * my.notch  / params.kCis
  }

  protected def d_Delta(my: Concentrations, other: Concentrations): Double = {
    println(s"""
       |deltaProd(${params.deltaProd}) - my.delta(${my.delta}) * params.deltaDecay(${params.deltaDecay}) -
       |- other.notch(${other.notch})  * my.delta(${my.delta})  / kTrans(${params.kTrans})  -
       |- my.delta(${my.delta}) * my.notch${my.notch}  / params.kCis(${params.kCis})
       """.stripMargin)
      params.deltaProd - my.delta * params.deltaDecay -
      my.delta * other.notch / params.kTrans  -
      my.delta * my.notch / params.kCis
    
  }

  protected def d_notchDeltaTrans(my: Concentrations, other: Concentrations): Double = {
    my.notch * other.delta / params.kTrans  - params.notchDeltaTransDecay
  }

  def updated(step: Double): NotchDeltaCell = {
    val other = neighboursAvg
    //println("OTHER NEAR "+neighbours.map(n=>n.concentrations).mkString(" ||  "))
    val newCon = this.concentrations + (
      this.computeDelta(d_Notch, other, step),
      this.computeDelta(d_Delta, other, step),
      this.computeDelta(d_notchDeltaTrans, other, step)
      )
    //println("new concentraions = "+newCon)
    copy(concentrations = newCon, previous = self.concentrations::previous)
  }

}

class NotchDeltaCellView(elem: Element,
                         cell: Var[NotchDeltaCell],
                         selection: Var[List[Var[NotchDeltaCell]]]
                        ) extends CellView(elem, cell)
{

  val values = cell.map(c =>
    s"${c.prettyDigit(c.concentrations.notch)} | " +
      s"${c.prettyDigit(c.concentrations.delta)} | " +
      s"${c.prettyDigit(c.concentrations.notchDeltaTrans)}")

  val selected = selection.map(_.contains(cell))

  val selectClick = Var(Events.createMouseEvent())

  selectClick.onChange("selectionClick"){
    case ev =>
      val s = selection.now
      selection() = if (s.contains(cell)) s.filterNot(_==cell) else {
       if(ev.ctrlKey || ev.shiftKey)  cell::s else cell::Nil
      }
  }

  val stroke: Rx[String] = selected.map{
    case true => "yellow"
    case false => "green"
  }

  override lazy val dots = Rx{ // draws shape
  val c = cell()
    draw(side.now, vert.now)
  }
}