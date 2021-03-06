package cats
package laws
package discipline

import cats.data._
import cats.implicits._
import eq.tuple2Eq

import org.scalacheck.Arbitrary

trait EqK[F[_]] {
  def synthesize[A: Eq]: Eq[F[A]]
}

object EqK {
  def apply[F[_]](implicit eqk: EqK[F]): EqK[F] = eqk

  implicit val nonEmptyList: EqK[NonEmptyList] =
    new EqK[NonEmptyList] { def synthesize[A: Eq]: Eq[NonEmptyList[A]] = implicitly }

  implicit val option: EqK[Option] =
    new EqK[Option] { def synthesize[A: Eq]: Eq[Option[A]] = implicitly }

  implicit def eitherA[A: Eq]: EqK[Either[A, ?]] =
    new EqK[Either[A, ?]] { def synthesize[B: Eq]: Eq[Either[A, B]] = implicitly }

  implicit def eitherB[B: Eq]: EqK[Either[?, B]] =
    new EqK[Either[?, B]] { def synthesize[A: Eq]: Eq[Either[A, B]] = implicitly }

  implicit val function0: EqK[Function0] =
    new EqK[Function0] { def synthesize[A: Eq]: Eq[() => A] = implicitly }

  implicit val list: EqK[List] =
    new EqK[List] { def synthesize[A: Eq]: Eq[List[A]] = implicitly }

  implicit val eval: EqK[Eval] =
    new EqK[Eval] { def synthesize[A: Eq]: Eq[Eval[A]] = implicitly }

  implicit def mapA[B: Eq]: EqK[Map[?, B]] =
    new EqK[Map[?, B]] { def synthesize[A: Eq]: Eq[Map[A, B]] = implicitly }

  implicit def mapB[A]: EqK[Map[A, ?]] =
    new EqK[Map[A, ?]] { def synthesize[B: Eq]: Eq[Map[A, B]] = implicitly[Eq[Map[A, B]]] }

  implicit def constA[A: Eq]: EqK[Const[A, ?]] =
    new EqK[Const[A, ?]] { def synthesize[B: Eq]: Eq[Const[A, B]] = implicitly }

  implicit def xorA[A: Eq]: EqK[A Xor ?] =
    new EqK[A Xor ?] { def synthesize[B: Eq]: Eq[A Xor B] = implicitly }

  implicit def xorB[B: Eq]: EqK[? Xor B] =
    new EqK[? Xor B] { def synthesize[A: Eq]: Eq[A Xor B] = implicitly }

  implicit def xorTA[F[_]: EqK, A: Eq]: EqK[XorT[F, A, ?]] =
    new EqK[XorT[F, A, ?]] {
      def synthesize[B: Eq]: Eq[XorT[F, A, B]] =
        XorT.xorTEq(EqK[F].synthesize[A Xor B])
    }

  implicit def xorTB[F[_]: EqK, B: Eq]: EqK[XorT[F, ?, B]] =
    new EqK[XorT[F, ?, B]] {
      def synthesize[A: Eq]: Eq[XorT[F, A, B]] =
        XorT.xorTEq(EqK[F].synthesize[A Xor B])
    }

  implicit def validA[A: Eq]: EqK[Validated[A, ?]] =
    new EqK[Validated[A, ?]] { def synthesize[B: Eq]: Eq[Validated[A, B]] = implicitly }

  implicit def validB[B: Eq]: EqK[Validated[?, B]] =
    new EqK[Validated[?, B]] { def synthesize[A: Eq]: Eq[Validated[A, B]] = implicitly }

  implicit def iorA[A: Eq]: EqK[A Ior ?] =
    new EqK[A Ior ?] { def synthesize[B: Eq]: Eq[A Ior B] = implicitly }

  implicit def iorB[B: Eq]: EqK[? Ior B] =
    new EqK[? Ior B] { def synthesize[A: Eq]: Eq[A Ior B] = implicitly }

  implicit val set: EqK[Set] =
    new EqK[Set] { def synthesize[A: Eq]: Eq[Set[A]] = implicitly }

  implicit val stream: EqK[Stream] =
    new EqK[Stream] { def synthesize[A: Eq]: Eq[Stream[A]] = implicitly }

  implicit val vector: EqK[Vector] =
    new EqK[Vector] { def synthesize[A: Eq]: Eq[Vector[A]] = implicitly }

  implicit val streaming: EqK[Streaming] =
    new EqK[Streaming] { def synthesize[A: Eq]: Eq[Streaming[A]] = implicitly }

  implicit def streamT[F[_]: EqK: Monad]: EqK[StreamingT[F, ?]] =
    new EqK[StreamingT[F, ?]] {
      def synthesize[A: Eq]: Eq[StreamingT[F, A]] = {
        implicit val eqfla: Eq[F[List[A]]] = EqK[F].synthesize[List[A]]
        implicitly
      }
    }

  implicit def function1L[A: Arbitrary]: EqK[A => ?] =
    new EqK[A => ?] {
      def synthesize[B: Eq]: Eq[A => B] =
        cats.laws.discipline.eq.function1Eq
    }

  implicit def kleisli[F[_]: EqK, A](implicit evKA: EqK[A => ?]): EqK[Kleisli[F, A, ?]] =
    new EqK[Kleisli[F, A, ?]] {
      def synthesize[B: Eq]: Eq[Kleisli[F, A, B]] = {
        implicit val eqFB: Eq[F[B]] = EqK[F].synthesize[B]
        implicit val eqAFB: Eq[A => F[B]] = evKA.synthesize[F[B]]
        eqAFB.on[Kleisli[F, A, B]](_.run)
      }
    }

  implicit def optionT[F[_]: EqK]: EqK[OptionT[F, ?]] =
    new EqK[OptionT[F, ?]] {
      def synthesize[A: Eq]: Eq[OptionT[F, A]] = {
        implicit val eqFOA: Eq[F[Option[A]]] = EqK[F].synthesize[Option[A]]
        implicitly
      }
    }

  implicit def writerTEqK[F[_]: EqK, L: Eq]: EqK[WriterT[F, L, ?]] = new EqK[WriterT[F, L, ?]] {
    def synthesize[A: Eq]: Eq[WriterT[F, L, A]] = EqK[F].synthesize[(L, A)].on(_.run)
  }
}
