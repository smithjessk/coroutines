package org.coroutines



import org.scalatest._



class ByNameTest extends FunSuite with Matchers {
  test("coroutines should not be allowed as by-name parameters 1") {
    import scala.concurrent.Future
    import scala.concurrent.ExecutionContext.Implicits.global

    val c1 = coroutine { () => 
      5 
    }
    """
    val c2 = coroutine { () =>
      val f = Future(c1)
      10
    }
    """  shouldNot typeCheck 
  }

  test("coroutines should not be allowed as by-name parameters 2") {
    def foo(a: Int, b: => Coroutine._0[Nothing, Int], c: Int): Int = {
      val instance = call(b())
      instance.resume 
      a + instance.result + c
    }

    val c1 = coroutine { () =>
      2
    }
    """
    val c2 = coroutine { () =>
      foo(1, c1, 3)
    }
    """ shouldNot typeCheck
  }
}
