package antonkulaga.projects.views.charts

import org.denigma.controls.charts.Point

object Cell {

  def apply(pos: Point, s: Double /*near: List[Cell]*/): Cell = new Cell{
    val position = pos
    val side = s
    //val neighbors = near
  }

}

trait Cell{
  val position: Point
  val side: Double
  //def color:(Int,Int,Int)
}
