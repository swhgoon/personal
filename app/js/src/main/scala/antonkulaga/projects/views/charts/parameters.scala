package antonkulaga.projects.views.charts

object Concentrations {
  lazy val empty: Concentrations = Concentrations(0.0, 0.0, 0.0)
  lazy val default: Concentrations = Concentrations(0.0, 0.0, 0.0)
}
case class Concentrations(notch: Double, delta: Double, glow: Double)
{
  def *(k: Double): Concentrations = copy(notch * k, delta * k, glow * k)
  def +(b: Double): Concentrations = copy(notch + b, delta + b, glow + b)
  def +(c: Concentrations): Concentrations = copy(notch + c.notch, delta + c.delta, glow + c.glow)
  def +(c: (Double, Double, Double)): Concentrations = copy(notch + c._1, delta + c._2, glow + c._3)

}

object NotchDeltaParams {
  lazy val default: NotchDeltaParams = NotchDeltaParams(
    notchProd = 200.0, notchDecay = 0.1,
    deltaProd = 1000.0, deltaDecay = 0.1,
    glowDecay = 0.01,
    kCis = 0.2,
    kTrans = 2
  )
}
case class NotchDeltaParams(notchProd: Double,
                            notchDecay: Double,
                            deltaProd: Double,
                            deltaDecay: Double,
                            glowDecay: Double,
                            kCis: Double,
                            kTrans: Double)

