package zio.prelude

import zio.prelude.coherent.EqualInverse
import zio.prelude.newtypes.{ Sum }
import zio.test.TestResult
import zio.test.laws.{ Lawful, Laws }

trait Inverse[A] extends Identity[A] {

  /**
   * Returns a right inverse for the given `A` value, such that when
   * the value is combined with the inverse (on the right hand side),
   * the identity element is returned.
   */
  def inverse(l: => A, r: => A): A
}

object Inverse extends Lawful[EqualInverse] {

  val rightInverseLaw: Laws[EqualInverse] =
    new Laws.Law1[EqualInverse]("rightInverseLaw") {
      def apply[A](a: A)(implicit I: EqualInverse[A]): TestResult =
        I.inverse(I.combine(I.identity, a), a) <-> I.identity
    }

  val laws: Laws[EqualInverse] =
    rightInverseLaw + Identity.laws

  def apply[A](implicit Inverse: Inverse[A]): Inverse[A] = Inverse

  def make[A](identity0: A, op: (A, A) => A, inv: (A, A) => A): Inverse[A] =
    new Inverse[A] {
      def identity: A                  = identity0
      def combine(l: => A, r: => A): A = op(l, r)
      def inverse(l: => A, r: => A): A = inv(l, r)
    }

  implicit val BooleanSumInverse: Inverse[Sum[Boolean]] =
    Inverse.make(
      Sum(false),
      (l: Sum[Boolean], r: Sum[Boolean]) => Sum((l || r)),
      (l: Sum[Boolean], r: Sum[Boolean]) => Sum((l && !r))
    )

  implicit val ByteSumInverse: Inverse[Sum[Byte]] =
    Inverse.make(
      Sum(0),
      (l: Sum[Byte], r: Sum[Byte]) => Sum((l + r).toByte),
      (l: Sum[Byte], r: Sum[Byte]) => Sum((l - r).toByte)
    )

  implicit val CharSumInverse: Inverse[Sum[Char]] =
    Inverse.make(Sum('\u0000'), (l, r) => Sum((l + r).toChar), (l, r) => Sum((l - r).toChar))

  /**
   * Derives an `Inverse[F[A]]` given a `Derive[F, Inverse]` and an
   * `Inverse[A]`.
   */
  implicit def DeriveInverse[F[_], A](implicit derive: Derive[F, Inverse], inverse: Inverse[A]): Inverse[F[A]] =
    derive.derive(inverse)

  implicit val DoubleSumInverse: Inverse[Sum[Double]] =
    Inverse.make(Sum(0), (l: Sum[Double], r: Sum[Double]) => Sum(l + r), (l: Sum[Double], r: Sum[Double]) => Sum(l - r))

  implicit val FloatSumInverse: Inverse[Sum[Float]] =
    Inverse.make(Sum(0), (l, r) => Sum(l + r), (l, r) => Sum(l - r))

  implicit val IntSumInverse: Inverse[Sum[Int]] =
    Inverse.make(Sum(0), (l, r) => Sum(l + r), (l, r) => Sum(l - r))

  implicit val LongSumInverse: Inverse[Sum[Long]] =
    Inverse.make(Sum(0L), (l, r) => Sum(l + r), (l, r) => Sum(l - r))

  implicit def SetInverse[A]: Inverse[Set[A]] =
    Inverse.make(Set.empty, _ | _, _ &~ _)

  implicit val ShortSumInverse: Inverse[Sum[Short]] =
    Inverse.make(
      Sum(0),
      (l: Sum[Short], r: Sum[Short]) => Sum((l + r).toShort),
      (l: Sum[Short], r: Sum[Short]) => Sum((l - r).toShort)
    )
}
