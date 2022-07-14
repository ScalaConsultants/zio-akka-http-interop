package akka.http.interop

import akka.http.scaladsl.marshalling.{ Marshaller, Marshalling, PredefinedToResponseMarshallers }
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.RouteResult.Complete
import akka.http.scaladsl.server.{ RequestContext, Route, RouteResult }
import zio._

import scala.concurrent.{ Future, Promise }
import scala.language.implicitConversions

/**
 * Provides support for ZIO values in akka-http routes
 */
trait ZIOSupport extends ZIOSupportInstances1

trait ZIOSupportInstances1 extends ZIOSupportInstances2 {
  implicit def zioSupportUIOMarshaller[A](implicit
    ma: Marshaller[A, HttpResponse]
  ): Marshaller[UIO[A], HttpResponse] =
    Marshaller { implicit ec => a =>
      val r = a.flatMap(a => ZIO.fromFuture(implicit ec => ma(a)))

      val p = Promise[List[Marshalling[HttpResponse]]]()

      Unsafe.unsafe { implicit u =>
        Runtime.default.unsafe.fork(r.foldCause(e => p.failure(e.squash), s => p.success(s)))
      }

      p.future
    }
}

trait ZIOSupportInstances2 {
  implicit def zioSupportErrorMarshaller[E: ErrorResponse]: Marshaller[E, HttpResponse] =
    Marshaller { implicit ec => a =>
      PredefinedToResponseMarshallers.fromResponse(implicitly[ErrorResponse[E]].toHttpResponse(a))
    }

  implicit def zioSupportIOMarshaller[A, E](implicit
    ma: Marshaller[A, HttpResponse],
    me: Marshaller[E, HttpResponse]
  ): Marshaller[IO[E, A], HttpResponse] =
    Marshaller { implicit ec => a =>
      val r = a.foldZIO(
        e => ZIO.fromFuture(implicit ec => me(e)),
        a => ZIO.fromFuture(implicit ec => ma(a))
      )

      val p = Promise[List[Marshalling[HttpResponse]]]()

      Unsafe.unsafe { implicit u =>
        Runtime.default.unsafe.fork(r.foldCause(e => p.failure(e.squash), s => p.success(s)))
      }

      p.future
    }

  implicit def zioSupportIORoute[E: ErrorResponse](z: IO[E, Route]): Route = ctx => {
    val p = Promise[RouteResult]()

    val f = z.fold(
      e => (_: RequestContext) => Future.successful(Complete(implicitly[ErrorResponse[E]].toHttpResponse(e))),
      a => a
    )

    Unsafe.unsafe { implicit u =>
      Runtime.default.unsafe.fork(f.foldCause(e => p.failure(e.squash), s => p.completeWith(s.apply(ctx))))
    }

    p.future
  }
}
