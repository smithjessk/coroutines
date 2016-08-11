package org.coroutines.extra



import org.coroutines._
import org.scalameter.api._
import org.scalameter.japi.JBench
import scala.async.Async.async
import scala.async.Async.await
import scala.collection._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global



class AsyncAwaitBench extends JBench.OfflineReport {
  override def defaultConfig = Context(
    exec.minWarmupRuns -> 100,
    exec.maxWarmupRuns -> 100,
    exec.benchRuns -> 36,
    exec.independentSamples -> 4,
    verbose -> true
  )

  val sizes = Gen.range("size")(5000, 25000, 5000)

  val delayedSizes = Gen.range("size")(5, 25, 5)

  private def request(i: Int): Future[Unit] = Future { () }

  private def delayedRequest(i: Int): Future[Unit] = Future { Thread.sleep(1) }

  @gen("sizes")
  @benchmark("coroutines.extra.async.request-reply")
  @curve("async")
  def asyncAwait(sz: Int) = {
    val done = async {
      var i = 0
      while (i < sz) {
        val reply = await(request(i))
        i += 1
      }
    }
    Await.result(done, 10.seconds)
  }

  @gen("delayedSizes")
  @benchmark("coroutines.extra.async.delayed-request-reply")
  @curve("async")
  def delayedAsyncAwait(sz: Int) = {
    val done = async {
      var i = 0
      while (i < sz) {
        val reply = await(delayedRequest(i))
        i += 1
      }
    }
    Await.result(done, 10.seconds)
  }

  @gen("sizes")
  @benchmark("coroutines.extra.async.request-reply")
  @curve("coroutine")
  def coroutineAsyncAwait(sz: Int) = {
    val done = AsyncAwait.async {
      var i = 0
      while (i < sz) {
        val reply = AsyncAwait.await(request(i))
        i += 1
      }
    }
    Await.result(done, 10.seconds)
  }

  @gen("delayedSizes")
  @benchmark("coroutines.extra.async.delayed-request-reply")
  @curve("coroutine")
  def delayedCoroutineAsyncAwait(sz: Int) = {
    val done = AsyncAwait.async {
      var i = 0
      while (i < sz) {
        val reply = AsyncAwait.await(delayedRequest(i))
        i += 1
      }
    }
    Await.result(done, 10.seconds)
  }
}
