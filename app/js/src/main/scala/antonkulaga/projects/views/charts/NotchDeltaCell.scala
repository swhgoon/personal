package antonkulaga.projects.views.charts

import org.denigma.binding.binders.Events
import org.denigma.binding.extensions._
import org.denigma.controls.charts.Point
import org.scalajs.dom.Element
import rx.Rx
import rx.core.Var
import rx.ops._

case class NotchDeltaCell(position: Point,
                          side: Double,
                          concentrations: Concentrations,
                          params: NotchDeltaParams,
                          previous: List[Concentrations] = Nil
                         ) extends Cell
{
  self =>

  type Derivative = (Concentrations, Concentrations) => Double

  @inline def computeDelta(f: Derivative, neighbours: Concentrations, step: Double): Double =
  {
    val a = step * f(this.concentrations, neighbours)
    val b = step * f(this.concentrations + 0.5 * a, neighbours)
    val c = step * f(this.concentrations + 0.5 * b, neighbours)
    val d: Double = step * f(this.concentrations + c, neighbours)
    (a + 2.0 * b + 2.0 * c + d) / 6
  }

  protected def d_Notch(my: Concentrations, other: Concentrations): Double = {
    println(
      s"""
         |notchProd(${params.notchProd}) - my.notch(${my.notch}) * params.notchDecay(${params.notchDecay}) -
         |- other.delta(${other.delta})  * my.notch(${my.notch})  / kTrans(${params.kTrans}) / 6 -
         |- my.delta(${my.delta}) * my.notch${my.notch}  / params.kCis${params.kCis}
       """.stripMargin)
    params.notchProd -  my.notch * params.notchDecay -
      other.delta  * my.notch  / params.kTrans / 6 -
      my.delta * my.notch  / params.kCis
  }

  protected def d_Delta(my: Concentrations, other: Concentrations): Double = {
    params.deltaProd - my.delta * params.deltaDecay -
      my.delta * other.notch / params.kTrans / 6 -
      my.delta * my.notch / params.kCis
  }

  protected def d_glowing(my: Concentrations, other: Concentrations): Double = {
    my.notch * other.delta / params.kTrans / 6 - params.glowDecay
  }

  def updated(step: Double, neighbours: Iterable[NotchDeltaCell]): NotchDeltaCell = {
    val other: Concentrations = neighbours.foldLeft(Concentrations.empty)(
      (acc, con) =>  acc + con.concentrations
    )
    val newCon = this.concentrations + (
      this.computeDelta(d_Notch, other, step),
      this.computeDelta(d_Delta, other, step),
      this.computeDelta(d_glowing, other, step)
      )
    println("new concentraions = "+newCon)
    copy(concentrations = newCon, previous = self.concentrations::previous)
  }

}

class NotchDeltaCellView(elem: Element,
                         cell: Var[NotchDeltaCell],
                         selection: Var[List[Var[NotchDeltaCell]]]
                        ) extends CellView(elem, cell)
{

  val delta = cell.map(c => c.concentrations.delta)
  val notch = cell.map(c => c.concentrations.notch)
  val glow = cell.map(c => c.concentrations.glow)
  val selected = selection.map(_.contains(cell))

  val selectClick = Var(Events.createMouseEvent())
  selectClick.onChange("selectionClick"){
    case ev =>
      val s = selection.now
      selection() = if (s.contains(cell)) s.filterNot(_==cell) else cell::s
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