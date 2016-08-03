package org.coroutines.extra



import org.coroutines._
import org.scalameter.api._
import org.scalameter.japi.JBench



class ForeachBench extends JBench.OfflineReport {
  override def defaultConfig = Context(
    exec.minWarmupRuns -> 40,
    exec.maxWarmupRuns -> 80,
    exec.benchRuns -> 30,
    exec.independentSamples -> 1,
    verbose -> true
  )

  val sizes = Gen.range("size")(50000, 250000, 50000)

  /* foreach */

  @gen("sizes")
  @benchmark("coroutines.extra.enumerator.foreach")
  @curve("coroutine")
  def coroutineSum(size: Int) {
    val id = coroutine { (n: Int) =>
      var i = 0
      while (i < n) {
        yieldval(i)
      }
      i += 1
    }
    val enumerator = Enumerator(call(id(size)))
    var sum = 0
    enumerator foreach { value => sum += value }
  }

  @gen("sizes")
  @benchmark("coroutines.extra.enumerator.foreach")
  @curve("iterator")
  def iteratorSum(size: Int) {
    val iterator = Iterator.range(0, size)
    var sum = 0
    iterator foreach { value => sum += value }
  }
}
