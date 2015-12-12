package antonkulaga.projects

import antonkulaga.projects.views.{MenuView, DynamicsView, SidebarView}
import org.denigma.binding.binders.{GeneralBinder, NavigationBinder}
import org.denigma.binding.extensions.sq
import org.denigma.binding.views.BindableView
import org.denigma.controls.login.{AjaxSession, LoginView}
import org.scalajs.dom
import org.scalajs.dom.raw.Element

import scala.scalajs.js.annotation.JSExport

@JSExport("FrontEnd")
object FrontEnd extends BindableView with scalajs.js.JSApp
{

  override def name: String = "main"

  lazy val elem: Element = dom.document.body
  val session = new AjaxSession()

    /**
     * Register views
     */
    override lazy val injector = defaultInjector
      // p.register("menu")( (el, args) => new MenuView(el).withBinder(new GeneralBinder(_)).withBinder(new NavigationBinder(_)))
      .register("sidebar")((el, args) => new SidebarView(el).withBinder(new GeneralBinder(_)))
      .register("login")((el, args) => new LoginView(el, session).withBinder(new GeneralBinder(_)))
      .register("dynamics")((el, args) => new DynamicsView(el).withBinder(new GeneralBinder(_)))
      .register("menu"){
        case (el, args) => new MenuView(el)
          .withBinder(new GeneralBinder(_))
          .withBinder(new NavigationBinder(_))
      }

  this.withBinders(me => List(new GeneralBinder(me), new NavigationBinder(me)))

  @JSExport
  def main(): Unit = {
    this.bindView()
  }

  @JSExport
  def load(content: String, into: String): Unit = {
    dom.document.getElementById(into).innerHTML = content
  }

  @JSExport
  def moveInto(from: String, into: String): Unit = {
    for {
      ins <- sq.byId(from)
      intoElement <- sq.byId(into)
    } {
      this.loadElementInto(intoElement, ins.innerHTML)
      ins.parentNode.removeChild(ins)
    }
  }

}
