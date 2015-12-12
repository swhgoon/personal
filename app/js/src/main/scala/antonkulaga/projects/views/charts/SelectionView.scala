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
  val deltaProd = Var(initialParameters.deltaProd)
  val deltaDecay = Var(initialParameters.deltaDecay)
  val notchDeltaTransDecay = Var(initialParameters.notchDeltaTransDecay)

  protected def newParameters() = NotchDeltaParams(notchProd.now, notchDecay.now, deltaProd.now, deltaDecay.now, notchDeltaTransDecay.now, kCis.now, kTrans.now)
  protected def newConcentration() = Concentrations(notch.now, delta.now, notchDeltaTrans.now)

  val notch = Var(initialConcentrations.notch)
  val delta = Var(initialConcentrations.delta)
  val notchDeltaTrans = Var(initialConcentrations.notchDeltaTrans)
  val hasSelection: Rx[Boolean] = selection.map(s => s.nonEmpty)

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
