package antonkulaga.projects.views.charts

import org.denigma.binding.binders.GeneralBinder
import org.denigma.controls.charts.Point
import org.scalajs.dom.Element
import rx.Rx
import rx.core.Var

import scala.collection.immutable._
import scala.scalajs.js
import org.denigma.binding.extensions._


class CellsFieldView(val elem: Element,
                     val items: Var[js.Array[js.Array[Var[NotchDeltaCell]]]],
                     val selection: Var[List[Var[NotchDeltaCell]]],
                     val dimensions: Rx[Dimensions],
                     val concentrations: Var[Concentrations],
                     val parameters: Var[NotchDeltaParams]
                    ) extends CellsChart
{

  self =>

  val autoResize = true

  val padding = 10.0

  override type Item = Var[NotchDeltaCell]

  override type ItemView = NotchDeltaCellView

  type VectorDerivative = (Double, Array[Double]) => Double


  override def newItemView(item: Item): ItemView = this.constructItemView(item){
    case (e, mp) => new NotchDeltaCellView(e, item, selection).withBinder(new GeneralBinder(_))
  }

  override def makeItem(r: Int, c: Int): Item = {
    val s = dimensions.now.side
    val xStart = s * (if (isOdd(r)) 0.5 else 2.0)
    val vert = dimensions.now.vertical
    val cx = xStart + s * 3 * c
    val cy = vert * (r +1)
    val cell = NotchDeltaCell(Point(cx,  cy ), s, concentrations.now, parameters.now)
    Var(cell)
  }

  @inline private def topBottom(row: js.Array[Item], c: Int, shift: Int): js.Array[Var[NotchDeltaCell]] = {
    val (from, to) = (c + shift, c + shift +2)
    row.slice(from, to)
  }
  @inline private def leftRight(row: js.Array[Item], c: Int): js.Array[Item] = row.length match {
    case len if len < c +1 =>
      throw  new Exception(s"row sliceength(${row.length}) cannot be less than column($c) number")
    case len if len == c + 1 =>
      if(len == 1) new js.Array[Item]() else js.Array.apply[Item](row(c - 1))
    case len if c == 0 => js.Array.apply[Item](row(c + 1))
    case _ =>
      js.Array.apply[Item](row(c - 1), row(c + 1))
  }

  def neighbours(arr: js.Array[js.Array[Item]], r: Int, c: Int): js.Array[Item] ={
    //require(arr.length > r)
    val shift = if (isOdd(r)) -1 else 0
    if (arr.length == r + 1)
      if(r == 0)
        leftRight(arr(r), c)
      else
        topBottom(arr(r - 1), c, shift) ++ leftRight(arr(r), c)
    else if (r == 0)
      leftRight(arr(r), c) ++ topBottom(arr(r + 1), c, shift)
    else
    {
      topBottom(arr(r - 1), c, shift) ++ leftRight(arr(r), c) ++ topBottom(arr(r + 1), c, shift)
    }
  }

  protected def updateNeighbours() = {
    val cells: js.Array[js.Array[Var[NotchDeltaCell]]] = items.now

    for{
      r <- cells.indices
      c <- cells(r).indices
      cell = cells(r)(c)
      nb = this.neighbours(cells, r, c)
    } cell() = cell.now.copy(neighbours = nb)
  }

  override protected def onResize(oldValue: Dimensions, newValue: Dimensions): Unit = {
    if (this.autoResize) this.height.set((newValue.rows + 2) * newValue.vertical + 2 * padding)
    super.onResize(oldValue, newValue)
    updateNeighbours()
  }

    def updateCells(stepSize: Double): Unit = {
      val cells = items.now
      for{
        r <- cells.indices
        c <- cells(r).indices
        cell = cells(r)(c)
      }
      {
        val c = cell.now
        val upd = c.updated(stepSize)
        println(s"updated = ${upd.concentrations}")

        cell()  = upd
        //= cell.now
    }
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