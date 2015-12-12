package antonkulaga.projects.views


import org.denigma.binding.views.MapCollectionView
import org.scalajs.dom.raw.Element
import rx.Rx
import rx.core.Var

import scala.collection.immutable._


class MenuView(elem: Element) extends MapCollectionView(elem) {
  self =>

  override val items: Rx[Seq[Map[String, Any]]] = Var(
    Seq(
      Map("uri" -> "pages/notch-delta", "label" -> "Notch-delta simulation"),
      Map("uri" -> "pages/diff", "label" -> "Differential expressions project")
    )
  )
}