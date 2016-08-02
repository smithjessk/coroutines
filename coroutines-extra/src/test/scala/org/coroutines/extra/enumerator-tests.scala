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

  test("enumerator creation from coroutine instance") {
    val instance = call(rube())
    val enumerator = Enumerator(instance)
    assert(enumerator.hasNext())
    assert(enumerator.next == 1)
    assert(enumerator.next == 2)
    assert(enumerator.next == 3)
    assert(!enumerator.hasNext)
  }

  test("enumerator creation from coroutine_0") {
    val enumerator = Enumerator(rube)
    assert(enumerator.hasNext())
    assert(enumerator.next == 1)
    assert(enumerator.next == 2)
    assert(enumerator.next == 3)
    assert(!enumerator.hasNext)
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

  test("foreach should call its argument function at each value") {
    var result = ""
    Enumerator(rube).foreach { value =>
      result = result + value.toString
    }
    assert(result == "123")
  }

  test("map should call its argument function at each value") {
    var enumerator = Enumerator(rube).map { _ + 1 }
    assert(enumerator.hasNext())
    assert(enumerator.next == 2)
    assert(enumerator.next == 3)
    assert(enumerator.next == 4)
    assert(!enumerator.hasNext)
  }

  test("toSeq should produce a correct result") {
    assert(Enumerator(rube).toSeq == Seq(1, 2, 3))
  }

  test("mapToSeq should produce a correct result") {
    val result = Enumerator(rube).mapToSeq { _ + 1 }
    assert(result == Seq(2, 3, 4))
  }
}
