package zio.prelude

object ValidatedTemp {

  sealed trait Validated[+L,+R]

  final case class Valid[+A](a: A) extends Validated[Nothing, A]
  final case class Invalid[+E](e: E) extends Validated[E, Nothing]

  // Applicative is IdentityBoth and Contravariant

 implicit def ValidatedCovariant[L]: Covariant[({ type lambda[+r] = Validated[L, r] })#lambda] =
    new Covariant[({ type lambda[+r] = Validated[L, r] })#lambda] {
      def map[A, B](f: A => B): Validated[L,A] => Validated[L,B] = {
        case Valid(r) =>
          Valid(f(r))
        case Invalid(err) =>
          Invalid(err)
      }
    }

  implicit def ValidatedIdentityBoth[L: Associative]: IdentityBoth[({ type lambda[+r] = Validated[L, r] })#lambda] =
    new IdentityBoth[({ type lambda[+r] = Validated[L, r] })#lambda] {
      val any: Validated[L, Any] = Valid(())

      def both[A, B](fa: => Validated[L,A], fb: => Validated[L,B])
        : Validated[L, (A, B)] = {
        (fa, fb) match {
          case (Valid(a), Valid(b)) =>
                Valid((a,b))
          case (Valid(_), invalid @ Invalid(_)) =>
                invalid
          case (invalid @ Invalid(_), Valid(_)) =>
                invalid
          case (Invalid(l1), Invalid(l2)) =>
                Invalid(l1 <> l2)

        }
      }

    }

    // IdentityBoth[({ type lambda[+r] = Validated[NonEmptyList[String], r] })#lambda].both(Invalid(NonEmptyList("ass2")), Invalid(NonEmptyList("ass")))
    // AssociativeBoth.mapN(Valid(22) : Validated[NonEmptyList[String], Int], Valid(23))(_ + _)

    import scala.concurrent.Future
    //import zio.prelude.classic._

    case class User(id: String, accountId: String)
    case class AccountStatus(id: String, balance: Long)

    def getUser(email: String): Future[User] = ???

    def getAccountStatus(id: String): Future[AccountStatus] = ???

    def flatMap[F[+_]: Covariant : IdentityFlatten, A, B](fa: F[A])(fab: A => F[B]): F[B] = {
      fa.map(a => fab(a)).flatten
    }

    def pure[F[+_] : Covariant : IdentityFlatten, A](a: A)(implicit i : IdentityFlatten[F]): F[A] = {
      i.any.map(_ => a)
    }

    def map[F[+_] : Covariant : IdentityFlatten, A, B](fa: F[A])(fab: A => B): F[B] = {
      flatMap(fa)(a => pure[F,B](fab(a)))
    }

    // Variance of Equal

    abstract class Animal {
      def name: String
    }

    implicit val animalEqual = new Equal[Animal] {
      def checkEqual(d1: Animal, d2: Animal): Boolean = {
        println("animal equal")
        if(d1.name == d2.name) true
        else false
      }
    }

    abstract class Pet extends Animal {
      def owner: String
    }

    case class Cat(name: String, owner: String) extends Pet

    case class Dog(name: String, owner: String) extends Pet

    implicit val dogEqual = new Equal[Dog] {
      def checkEqual(d1: Dog, d2: Dog): Boolean = {
        println("dog equal")
        if(d1.name == d2.name && d1.owner == d2.owner) true
        else false
      }
    }

    case class Lion(name: String, territory: String) extends Animal

    // Can contain a subtype of Pet but not an Animal
    class PetContainer[P <: Pet](p: P) {
      def pet: P = p
    }

    val nero = Dog("Nero", "Justin")
    val nero2 = Dog("Nero", "Justin2")
    val lenny = Lion("lenny", "Nairobi")

    Traversable[List].contains(List[Animal](lenny, nero, nero2))(nero)

  }