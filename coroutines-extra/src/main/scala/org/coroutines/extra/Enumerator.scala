package org.coroutines.extra



import org.coroutines._
import scala.collection._



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
    val result = instance.value
    _hasNext = instance.pull
    result
  }

  // Consumes the coroutine
  def foreach[Z](f: Function1[Y, Z]) {
    while (hasNext()) {
      f(next())
    }
  }

  // Consumes the coroutine
  def map[Z](f: Function1[Y, Z]): Enumerator[Z] = {
    val toYield = mutable.Seq.empty[Z]
    foreach { value => toYield :+ f(value) }
    Enumerator(coroutine { () =>
      var i = 0
      while (i < toYield.size) {
        yieldval(toYield(i))
      }
    })
  }

  // Consumes the coroutine
  def toSeq(): immutable.List[Y] = {
    val seq = mutable.Seq.empty[Y]
    while (hasNext()) {
      seq :+ next()
    }
    seq.toList
  }

  // Consumes the coroutine
  def mapToSeq(f: Function1[Y, Y]): immutable.List[Y] = {
    val toYield = mutable.Seq.empty[Y]
    foreach { value => toYield :+ f(value) }
    toYield.toList
  }
}

object Enumerator {
  def apply[Y](c: Coroutine.Instance[Y, _]) = new Enumerator(c)

  def apply[Y](c: Coroutine._0[Y, _]) = new Enumerator(call(c()))
}