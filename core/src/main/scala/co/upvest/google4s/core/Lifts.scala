package co.upvest.google4s.core

import cats.Id

import scala.concurrent.{ blocking, ExecutionContext, Future }
import scala.util.Try

object Lifts {

  trait FutureInstances {

    def liftWithEC(implicit ec: ExecutionContext) =
      new Llift[Future] {
        override def apply[A](fa: () => A): Future[A] =
          Future { fa() }
      }

    def blockingLiftWithEC(implicit ec: ExecutionContext) =
      new Llift[Future] {
        override def apply[A](fa: () => A): Future[A] =
          Future { blocking { fa() } }
      }

    def liftWithoutEC = new Llift[Future] {
      override def apply[A](fa: () => A): Future[A] =
        Future.fromTry(Try { fa() })
    }
  }

  trait TryInstances {

    def liftTry = new Llift[Try] {
      override def apply[A](fa: () => A): Try[A] =
        Try { fa() }
    }
  }

  trait IdInstance {

    implicit def liftId = new Llift[Id] {
      override def apply[A](fa: () => A): cats.Id[A] = fa()
    }
  }
}
