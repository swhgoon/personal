package antonkulaga.projects.views.charts

import org.denigma.binding.binders.{Events, GeneralBinder}
import org.denigma.binding.extensions._
import org.denigma.binding.views.BindableView
import org.denigma.controls.charts.Point
import org.scalajs.dom
import org.scalajs.dom.Element
import rx.Rx
import rx.core.Var
import rx.ops._
import scala.collection.immutable._
import scala.scalajs.js

class NotchDeltaView(val elem: Element,
                     val rows: Var[Int],
                     val cols: Var[Int],
                     val side: Var[Int],
                     val stepSize: Var[Double],
                     concentrations: Var[Concentrations] = Var(Concentrations.empty),
                     parameters: Var[NotchDeltaParams] = Var(NotchDeltaParams.default)
                    ) extends BindableView{

  self =>

  type Item = Var[NotchDeltaCell]

  val items: Var[js.Array[js.Array[Item]]] = Var(new js.Array[js.Array[Item]](0))

  val dimensions = Rx{ Dimensions(rows(), cols(), side())}

  val selection: Var[List[Item]] = Var(List.empty[Item])

  val lastSelected = selection.map(_.headOption)

  override lazy val injector = defaultInjector
    .register("cells") { case (el, params) =>
      new CellsFieldView(el, items, selection, self.dimensions,  self.concentrations, self.parameters).withBinder(new GeneralBinder(_))
    }
    .register("selection") { case (el, params) =>
      new SelectionView(el, self.concentrations.now, self.parameters.now, items, selection).withBinder(new GeneralBinder(_))
    }
    .register("last") { case (el, params) =>
      new LastSelected(el, lastSelected).withBinder(new GeneralBinder(_))
    }

  var fieldViews = scala.collection.immutable.Map.empty[String, CellsFieldView]

  val started: Var[Boolean] = Var(false)

  val start = Var(Events.createMouseEvent())
  start.handler{
    println("start click")
    started.set(!started.now)
  }


  override def addView(view: ChildView) = {
    super.addView(view)
    view match {
      case view: CellsFieldView =>
        fieldViews = fieldViews + (view.name -> view)
        view
      case _ => view
    }
  }

  protected def onEnterFrame(value: Double) = {
    println(s"onEnterFrame $value")
    for( (key, value) <- fieldViews) value.updateCells(stepSize.now)
  }


  var handle: Int = -1 // NOTE: unsafe!

  started.onChange("onStarted"){
    case false =>
      if(handle != -1) dom.cancelAnimationFrame(handle)
      handle = -1
    case true =>
      val enterFrameHandler: js.Function1[Double, _] = onEnterFrame _
      handle = dom.requestAnimationFrame(enterFrameHandler)
  }
}

class LastSelected(val elem: Element, lastSelection: Rx[Option[Var[NotchDeltaCell]]]) extends BindableView {

  val selected = lastSelection.map(s => s.map(_.now))

  val title = selected.map{cell => cell.map(c => s"Last selected cell: ${c.position}").getOrElse("Nothing selected")}

  val notch = selected.map{
    case Some(cell)=> cell.concentrations.notch
    case None => "N/A"
  }

  val delta = selected.map{
    case Some(cell)=> cell.concentrations.delta
    case None => "N/A"
  }

  val glow = selected.map{
    case Some(cell)=> cell.concentrations.glow
    case None => "N/A"
  }
}


class CellsFieldView(val elem: Element,
                     val items: Var[js.Array[js.Array[Var[NotchDeltaCell]]]],
                     val selection: Var[List[Var[NotchDeltaCell]]],
                     val dimensions: Rx[Dimensions],
                     val concentrations: Var[Concentrations],
                     val parameters: Var[NotchDeltaParams]
                    ) extends CellsChart
{

  self =>

  override type Item = Var[NotchDeltaCell]

  override type ItemView = NotchDeltaCellView

  type VectorDerivative = (Double, Array[Double]) => Double

  override def newItemView(item: Item): ItemView = this.constructItemView(item){
    case (e, mp) => new NotchDeltaCellView(e, item, selection).withBinder(new GeneralBinder(_))
  }

  override def makeItem(r: Int, c: Int): Item = {
    val s = dimensions.now.side
    val vert = this.vertSide(s)
    val xStart = s * ( if (isOdd(r)) 0.5 else 2.0 )
    val cell = NotchDeltaCell(Point(xStart + s * 3 * c,  vert * (r +1) ), s, Concentrations.empty, NotchDeltaParams.default)
    Var(cell)
  }


  def updateCells(stepSize: Double): Unit = {
    val cells = items.now
    for{
      r <- cells.indices
      c <- cells(r).indices
      cell = cells(r)(c)
      ns = this.neighbours(cells, r, c)
    }
    {
      val c = cell.now
      cell()  = c.updated(stepSize, ns.toIterable.map(_.now))
      //= cell.now
    }
  }

}

class SelectionView(val elem: Element,
                    initialConcentrations: Concentrations,
                    initialParameters: NotchDeltaParams,
                    items: Var[js.Array[js.Array[Var[NotchDeltaCell]]]],
                    selection: Var[List[Var[NotchDeltaCell]]]) extends BindableView {
  self =>

  val title = selection.map(sel => s"selected items: ${sel.size}")

  val kCis = Var(initialParameters.kCis)
  val kTrans = Var(initialParameters.kTrans)
  val notchProd = Var(initialParameters.notchProd)
  val notchDecay = Var(initialParameters.notchDecay)
  val deltaProd = Var(initialParameters.notchProd)
  val deltaDecay = Var(initialParameters.notchDecay)
  val glowDecay = Var(initialParameters.glowDecay)

  protected def newParameters() = NotchDeltaParams(notchProd.now, notchDecay.now, deltaProd.now, deltaDecay.now, glowDecay.now, kCis.now, kTrans.now)
  protected def newConcentration() = Concentrations(notch.now, delta.now, glow.now)

  val notch = Var(initialConcentrations.notch)
  val delta = Var(initialConcentrations.delta)
  val glow = Var(initialConcentrations.glow)




  //val deltaDecay = Var(parameters.now.notchDecay)

  val applyAll = Var(Events.createMouseEvent())
  applyAll.onChange("onApplyAll"){
    case ev =>
      for{
        row <- items.now
        cell <- row
      } cell() = cell.now.copy(params = self.newParameters(), concentrations = self.newConcentration())
  }

  val applySelection = Var(Events.createMouseEvent())
  applySelection.onChange("onApplySelection"){
    case ev => selection.now.foreach(c => c() = c.now.copy(params = self.newParameters(), concentrations = self.newConcentration()))
  }

  val resetSelection = Var(Events.createMouseEvent())
  resetSelection.onChange("resetSelection"){
    case ev => selection() = List.empty[Var[NotchDeltaCell]]
  }


}


/*case class ColorMap(min: Double, max: Double)
{
  def color(value: Double) = {

  }
}*/

/*
/*
   Return a RGB colour value given a scalar v in the range [vmin,vmax]
   In this case each colour component ranges from 0 (no contribution) to
   1 (fully saturated), modifications for other ranges is trivial.
   The colour is clipped at the end of the scales if v is outside
   the range [vmin,vmax]
*/

typedef struct {
    double r,g,b;
} COLOUR;

COLOUR GetColour(double v,double vmin,double vmax)
{
   COLOUR c = {1.0,1.0,1.0}; // white
   double dv;

   if (v < vmin)
      v = vmin;
   if (v > vmax)
      v = vmax;
   dv = vmax - vmin;

   if (v < (vmin + 0.25 * dv)) {
      c.r = 0;
      c.g = 4 * (v - vmin) / dv;
   } else if (v < (vmin + 0.5 * dv)) {
      c.r = 0;
      c.b = 1 + 4 * (vmin + 0.25 * dv - v) / dv;
   } else if (v < (vmin + 0.75 * dv)) {
      c.r = 4 * (v - vmin - 0.5 * dv) / dv;
      c.b = 0;
   } else {
      c.g = 1 + 4 * (vmin + 0.75 * dv - v) / dv;
      c.b = 0;
   }

   return(c);
}
 */