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
      new CellsFieldView(el, items, selection, self.dimensions, self.concentrations, self.parameters).withBinder(new GeneralBinder(_))
    }
    .register("selection") { case (el, params) =>
      new SelectionView(el, self.concentrations.now, self.parameters.now, items, selection).withBinder(new GeneralBinder(_))
    }
    .register("last") { case (el, params) =>
      new LastSelected(el, lastSelected).withBinder(new GeneralBinder(_))
    }

  var fieldViews = scala.collection.immutable.Map.empty[String, CellsFieldView]

  val started: Var[Boolean] = Var(false)

  val startCaption = started.map{
    case false => "Start simulation"
    case true => "Stop simulation"
  }

  val start = Var(Events.createMouseEvent())
  start.handler{
    //println("start click")
    started.set(!started.now)
  }

  val step = Var(Events.createMouseEvent())
  step.handler{
    makeStep()
  }

  protected def makeStep() = {
    for( (key, value) <- fieldViews) value.updateCells(stepSize.now)
  }


  override def addView(view: ChildView): ParentView = {
    super.addView(view)
    view match {
      case view: CellsFieldView =>
        fieldViews = fieldViews + (view.name -> view)
        view
      case _ => view
    }
  }

  var count = 0

  lazy val enterFrameHandler: js.Function1[Double, _] = onEnterFrame _

  protected def onEnterFrame(value: Double): Int = {
    count +=1
    //println(s"onEnterFrame $value with count = ${count+1}")
    this.makeStep()
    if(started.now) dom.requestAnimationFrame(enterFrameHandler) else -1
  }



  started.onChange("onStarted"){
    case false =>
      println("switch off")
    case true =>
      dom.requestAnimationFrame(enterFrameHandler)
  }
}
