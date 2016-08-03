package org.coroutines.extra



import org.coroutines._
import org.scalatest._
import scala.collection._



class EnumeratorsTest extends FunSuite with Matchers {
  val rube = coroutine { () =>
    yieldval(1)
    yieldval(2)
    yieldval(3)
  }

  test("coroutine instance should be private inside enumerator") {
    val e = Enumerator(rube)
    """
    e.instance
    """ shouldNot compile
  }

  // Asserts that `apply` takes a `snapshot` of the instance.
  test("enumerator creation from coroutine instance") {
    val instance = call(rube())

    val enumerator1 = Enumerator(instance)
    assert(enumerator1.hasNext())
    assert(enumerator1.next == 1)
    assert(enumerator1.next == 2)
    assert(enumerator1.next == 3)
    assert(!enumerator1.hasNext)

    val enumerator2 = Enumerator(instance)
    assert(enumerator2.hasNext())
    assert(enumerator2.next == 1)
    assert(enumerator2.next == 2)
    assert(enumerator2.next == 3)
    assert(!enumerator2.hasNext)
  }

  /** Asserts that more than one `Enumerator` can be created from the same
   *  `Coroutine._0`.
   */
  test("enumerator creation from coroutine_0") {
    val enumerator1 = Enumerator(rube)
    assert(enumerator1.hasNext())
    assert(enumerator1.next == 1)
    assert(enumerator1.next == 2)
    assert(enumerator1.next == 3)
    assert(!enumerator1.hasNext)

    val enumerator2 = Enumerator(rube)
    assert(enumerator2.hasNext())
    assert(enumerator2.next == 1)
    assert(enumerator2.next == 2)
    assert(enumerator2.next == 3)
    assert(!enumerator2.hasNext)
  }

  test("enumerator should ignore return value of coroutine") {
    val rubeWithReturn = coroutine { () =>
      yieldval(1)
      yieldval(2)
      yieldval(3)
      "foo"
    }
    val enumerator = Enumerator(rubeWithReturn)
    assert(enumerator.hasNext())
    assert(enumerator.next == 1)
    assert(enumerator.next == 2)
    assert(enumerator.next == 3)
    assert(!enumerator.hasNext)
  }

  test("exceptions thrown during a yieldval should be thrown during calls to next") {
    val failure = coroutine { () =>
      yieldval(1)
      yieldval(sys.error("Oh no"))
    }
    val e = Enumerator(failure)
    assert(e.hasNext)
    assert(e.next == 1)
    var caughtMessage = null.asInstanceOf[String]
    try {
      e.next
    } catch {
      case re: RuntimeException => caughtMessage = re.getMessage()
    }
    assert(caughtMessage == "Oh no")
    assert(!e.hasNext)
  }

  test("construction should fail if the first yielded value is an exception") {
    val failure = coroutine { () =>
      yieldval(sys.error("Oh no"))
      yieldval(2)
    }
    var caughtMessage = null.asInstanceOf[String]
    try {
      val e = Enumerator(failure)
    } catch {
      case re: RuntimeException => caughtMessage = re.getMessage()
    }
    assert(caughtMessage == "Oh no")
  }

  test("failing yieldval should stop the enumerator") {
    val failure = coroutine { () =>
      yieldval(1)
      yieldval(sys.error("Oh no"))
      yieldval(2)
    }
    val e = Enumerator(failure)
    assert(e.next == 1)
    try { e.next } catch { case _: Throwable => {} }
    assert(!e.hasNext)
    try {
      e.next
    } catch {
      case cse: CoroutineStoppedException => assert(true)
      case _: Throwable => assert(false)
    }
  }
}
