package antonkulaga.projects.views

import antonkulaga.projects.views.charts._
import org.denigma.binding.views._
import org.denigma.controls.code.CodeBinder
import org.scalajs.dom.raw.Element
import rx.core._



class DynamicsView(val elem: Element) extends BindableView
{
  self =>

   override lazy val injector = defaultInjector
     .register("notch-delta") { case (el, params) =>
       new NotchDeltaView(el, side = Var(50), rows = Var(1), cols = Var(2), stepSize = Var(0.001)).withBinder(new CodeBinder(_))
     }
}