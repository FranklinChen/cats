package cats
package free

import cats.std.function.function0Instance

// To workaround SI-7139 `object Trampoline` needs to be defined inside the package object
// together with the type alias.
abstract class TrampolineFunctions {
  def done[A](a: A): Trampoline[A] =
    Free.Pure[Function0,A](a)

  def suspend[A](a: => Trampoline[A]): Trampoline[A] =
    Free.suspend(a)

  def delay[A](a: => A): Trampoline[A] =
    suspend(done(a))
}

