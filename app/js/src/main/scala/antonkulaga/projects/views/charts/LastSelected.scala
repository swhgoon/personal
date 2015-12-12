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
/**
  * Created by antonkulaga on 12/10/15.
  */
class LastSelected(val elem: Element, lastSelection: Rx[Option[Var[NotchDeltaCell]]]) extends BindableView {

  val selected: Rx[Option[NotchDeltaCell]] = lastSelection.map(s => s.map(_.now))

  val title = selected.map{cell => cell.map(c => s"Last selected cell, coordinates: ${c.position}").getOrElse("Nothing selected")}

  val notch = selected.map( sel => sel.map(cell => cell.prettyDigit(cell.concentrations.notch)).getOrElse("N/A"))

  val delta = selected.map( sel => sel.map(cell => cell.prettyDigit(cell.concentrations.delta)).getOrElse("N/A"))

  val notchDeltaTrans = selected.map( sel => sel.map(cell => cell.prettyDigit(cell.concentrations.notchDeltaTrans)).getOrElse("N/A"))

  val neighboursNotch: Rx[String] = selected.map{
    sel =>
      sel.map { case s =>
        //println("notchNEB = "+s.neighbours.length)
        s.neighbours.foldLeft("") { case (acc, el) => acc + s"${el.now.concentrations.notch} " }
      }.getOrElse("N/A")
  }

  val neighboursDelta: Rx[String] = selected.map{
    sel => sel.map(s=>
      s.neighbours.foldLeft(""){ case (acc, el) =>  acc + s"${el.now.concentrations.delta} "}
    ).getOrElse("N/A")
  }


  val neighboursNotchDeltaTrans: Rx[String] = selected.map{
    sel => sel.map(s=>
      s.neighbours.foldLeft(""){ case (acc, el) =>  acc + s"${el.now.concentrations.notchDeltaTrans} "}
    ).getOrElse("N/A")
  }
}
