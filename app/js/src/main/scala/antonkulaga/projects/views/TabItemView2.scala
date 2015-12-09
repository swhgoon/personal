package antonkulaga.projects.views

import org.denigma.binding.binders.Events
import org.denigma.binding.extensions._
import org.denigma.binding.views._
import org.denigma.controls.tabs._
import rx.core._
import rx.ops._

trait TabItemView2 extends BindableView {

  val item: Rx[TabItem]
  val selection: Var[Option[Rx[TabItem]]]

  val content: rx.Rx[String] = item.map(_.content)
  val label: rx.Rx[String] = item.map(_.label)

  lazy val active: Rx[Boolean] = Rx{
    val sel = selection()
    sel.isDefined && sel.get.now == item()
  }

  val onClick = Var(Events.createMouseEvent())
  onClick.handler{
    selection() = Some(this.item)
  }
}






