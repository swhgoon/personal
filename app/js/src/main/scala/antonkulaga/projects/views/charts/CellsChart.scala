package antonkulaga.projects.views.charts

import org.denigma.binding.binders.GeneralBinder
import org.denigma.binding.views.BindableView
import org.denigma.controls.charts.Point
import org.scalajs.dom.Element
import rx.Rx
import rx.core.Var

import scala.scalajs.js


class SimpleCellsChart(val elem: Element, val dimensions: Rx[Dimensions]) extends CellsChart{

  override type Item = Rx[Cell]
  override type ItemView = CellView

  val items: Var[js.Array[js.Array[Rx[Cell]]]] = Var(new js.Array[js.Array[Item]](0))

  override def newItemView(item: Item): ItemView = this.constructItemView(item){
    case (el, mp) => new CellView(el, item).withBinder(new GeneralBinder(_))
  }

  override def makeItem(r: Int, c: Int): Item = {
    val dim = dimensions.now
    val (s, vert) = (dim.side, dim.vertical)
    val xStart = s * ( if (isOdd(r)) 0.5 else 2.0 )
    Var(Cell(Point(xStart + s * 3 * c,  vert * (r +1) ), s))
  }

}

trait CellsChart extends ArrayChart
{

  override type Item <: Rx[Cell]

  override type ItemView <: CellView

  val width: Var[Double] = Var(800.0)

  val height: Var[Double] =Var(800.0)

  def isEven(v: Int): Boolean = v % 2 == 0

  def isOdd(v: Int): Boolean = v % 2 != 0

  override protected def makeRow(old: js.Array[js.Array[Item]], r: Int, cOld: Int, c: Int): js.Array[Item] = if (r >= old.length)
  {
    createRow(r, c)(makeItem)
  }
  else
  {
    val oldRow = old(r)
    c - cOld match
    {
      case 0 => oldRow

      case less if less < 0 =>
        for(j <- c until cOld) onRemove(oldRow(j))
        oldRow.slice(0, c)

      case more if more > 0 =>
        val arr =  new js.Array[Item](c)
        for(j <- 0 until cOld) arr(j) = oldRow(j)
        for(j <- cOld until c) {
          val item = makeItem(r, j)
          arr(j) = item
          onInsert(item)
        }
        arr
    }
  }

}

/**
  * View class for the cell
  * @param elem Element ( HTML or SVG or smth. else)
  * @param cell cell reactive variable
  */
class CellView(val elem: Element, val cell: Rx[Cell]) extends BindableView {

  self =>

  import rx.ops._

  val x = cell.map(c => c.position.x)
  val y = cell.map(c => c.position.y)
  val vert = cell.map(c => Math.sqrt(Math.pow(c.side, 2)-Math.pow(c.side / 2, 2)))
  val side = cell.map(c => c.side)
  val half = side.map(s => s / 2)

  def draw(x: Double, y: Double) = {
    val h = half.now
    val v = vert.now
    val s = this.side.now
    // val hyp = side * Math.sin(Math.PI / 3)
    List(
      Point(x - s, y),
      Point(x - h, y + v),
      Point(x + h, y + v),
      Point(x + s, y),
      Point(x + h, y - v),
      Point(x - h, y - v)
    )
  }

  lazy val dots = Rx{ // draws shape
    val c = cell()
    draw(c.position.x + side.now, c.position.y + vert.now)
  }

  val points = dots.map(_.foldLeft(""){ case (acc, Point(x, y)) =>
    acc+s"$x,$y "
  }.trim)

}