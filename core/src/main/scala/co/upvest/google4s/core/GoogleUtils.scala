package co.upvest.google4s.core

import com.google.api.core.{ ApiFuture, ApiFutureCallback, ApiFutures }
import com.google.common.util.concurrent.MoreExecutors

import scala.concurrent.{ Future, Promise }

object GoogleUtils {

  trait GoogleConverters {

    implicit class RichApiFuture[T](lf: ApiFuture[T]) {

      def asScala: Future[T] = {

        val p = Promise[T]()

        ApiFutures.addCallback(
          lf,
          new ApiFutureCallback[T] {
            def onFailure(t: Throwable): Unit = p failure t
            def onSuccess(result: T): Unit    = p success result
          },
          MoreExecutors.directExecutor()
        )

        p.future
      }
    }
  }
}
