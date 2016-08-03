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

  val sizes = Gen.range("size")(50000, 500000, 25000)

  /* identity */
  @gen("sizes")
  @benchmark("coroutines.extra.enumerator.foreach.identity")
  @curve("coroutine")
  def coroutineIdentity(size: Int) {
    val id = coroutine { (n: Int) =>
      var i = 0
      while (i < n) {
        yieldval(i)
        i += 1
      }
    }
    val enumerator = Enumerator(call(id(size)))
    enumerator foreach { value => value }
  }

  @gen("sizes")
  @benchmark("coroutines.extra.enumerator.foreach.identity")
  @curve("iterator")
  def iteratorIdentity(size: Int) {
    val iterator = Iterator.range(0, size)
    iterator foreach { value => value }
  }

  /* sum */

  @gen("sizes")
  @benchmark("coroutines.extra.enumerator.foreach.sum")
  @curve("coroutine")
  def coroutineSum(size: Int) {
    val id = coroutine { (n: Int) =>
      var i = 0
      while (i < n) {
        yieldval(i)
        i += 1
      }
    }
    val enumerator = Enumerator(call(id(size)))
    var sum = 0
    enumerator foreach { value => sum += value }
  }

  @gen("sizes")
  @benchmark("coroutines.extra.enumerator.foreach.sum")
  @curve("iterator")
  def iteratorSum(size: Int) {
    val iterator = Iterator.range(0, size)
    var sum = 0
    iterator foreach { value => sum += value }
  }
}
