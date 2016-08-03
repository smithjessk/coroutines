package org.coroutines.extra



import org.coroutines._
import scala.collection._
import scala.util.{ Success, Failure }



/** Ignores and does nothing with the return value of the coroutine. This makes
 *  specialization simpler and also makes it more straightforward for the user to
 *  create an `Enumerator` from a `Coroutine`.
 *
 *  Takes a `Coroutine.Instance` over a `Coroutine` so that both the constructor is
 *  more general and so that an enumerator can be built from an in-progress coroutine.
 */
class Enumerator[@specialized Y](instance: Coroutine.Instance[Y, _]) {
  private var _hasNext = instance.pull

  /** Return whether or not the enumerator has a next value.
   *
   *  Internally, this variable is set via calls to `instance.pull`.
   *
   *  @return true iff `next` can be called again without error
   */
  def hasNext(): Boolean = _hasNext

  /** Returns the next value in the enumerator.
   *
   *  Also advances the enumerator to the next return point.
   *
   *  @return The result of `instance.value` after the previous call to `instance.pull`
   */
  def next(): Y = {
    val result: Y = instance.tryValue match {
      case Success(value) => value
      case Failure(error) =>
        _hasNext = false
        throw error
    }
    _hasNext = instance.pull
    result
  }
}

object Enumerator {
  def apply[Y](c: Coroutine.Instance[Y, _]) = new Enumerator(c.snapshot)

  def apply[Y](c: Coroutine._0[Y, _]) = new Enumerator(call(c()))
}