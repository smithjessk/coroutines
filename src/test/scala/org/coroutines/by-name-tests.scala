package org.coroutines



import org.scalatest._



class ByNameTest extends FunSuite with Matchers {
  test("coroutine applications should not be allowed as by-name parameters 1") {
    import scala.concurrent.Future
    import scala.concurrent.ExecutionContext.Implicits.global

    val c1 = coroutine { (i: Int) =>
      i
    }
    """
    val c2 = coroutine { () =>
      val f = Future(c1(4))
      10
    }
    """  shouldNot typeCheck
  }

  test("coroutine applications should not be allowed as by-name parameters 2") {
    def foo(a: Int, b: => Coroutine._0[Nothing, Int], c: Int): Int = {
      val instance = call(b())
      instance.resume 
      a + instance.result + c
    }

    val c1 = coroutine { (i: Int) =>
      i
    }
    """
    val c2 = coroutine { () =>
      foo(1, c1(2), 3)
    }
    """ shouldNot typeCheck
  }

  test("repeated parameters") {
    def foo(a: => Int, otherInts: Int*): Int = {
      otherInts.foldLeft(a)((sum: Int, current: Int) => sum + current)
    }

    val c = coroutine { (starter: Int) =>
      foo(starter, 1, 2, 3)
    }
    val instance = call(c(4))
    assert(!instance.resume)
    assert(instance.result == 10)
  }

  // Inspired by https://git.io/vrAlJ
  test("by-name arguments aren't lifted when they surround a non by-name one") {
    def foo(firstIgnored: => Any, b: Int, secondIgnored: => Any) = b
    val c = coroutine { () =>
      foo(???, 1, { throw new Exception })
    }
    val instance = call(c())
    assert(!instance.resume)
    assert(instance.result == 1)
  }

  test("mixed argument lists can end in non by-name parameters") {
    def foo(firstIgnored: => Any, b: Int, secondIgnored: => Any, d: Int) = { b + d }
    val c = coroutine { () =>
      foo(???, 1, { throw new Exception }, 2)
    }
    val instance = call(c())
    assert(!instance.resume)
    assert(instance.result == 3)
  }
}
