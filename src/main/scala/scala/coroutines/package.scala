package scala



import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context



package object coroutines {

  def yieldval[T](x: T): Unit = {
    sys.error("Yield allowed only inside coroutines.")
  }

  def yieldto[T](f: Coroutine[T]): Unit = {
    sys.error("Yield allowed only inside coroutines.")
  }

  def call[T](f: Coroutine[T]): Coroutine[T] = ???

  def coroutine[T](f: Any): Coroutine.Definition[T] = macro Coroutine.transform

}
