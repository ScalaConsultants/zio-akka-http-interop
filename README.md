# zio-akka-http

Small library, that provides interop between [akka-http] and [ZIO]: you'll be able to use ZIO values in your akka-http routes instead of `Future`s

### How to use

Include zio-akka-http in your build:

```
libraryDependencies += "io.scalac" %% "zio-akka-http" % "<tbd>"
```

Then just mix `akka.http.interop.ZIOSupport` into the class, that defines your routes:

```scala
import akka.http.interop.ZIOSupport
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import zio._

object Api extends ZIOSupport {

  val routes: Route =
    pathPrefix("a") {
      get {
        val res: Task[String] = ZIO.succeed("OK")
        complete(res)
      }
    }
}
```

This will work with `Throwable` on the error channel.

#### Custom domain errors

If you want to use ZIO values with custom error type, you'll need some additional setup: interop code will have to know how to translate your domain error into `HttpResponse`.
You can provide that knowledge by defining an `akka.http.interop.ErrorResponse` typeclass instance:

```scala
import akka.http.interop._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import zio.{ IO, ZIO }

object Api extends ZIOSupport {

  sealed trait DomainError
  case object FatalError extends DomainError
  case object BadData    extends DomainError

  implicit val domainErrorResponse: ErrorResponse[DomainError] = {
    case FatalError => HttpResponse(StatusCodes.InternalServerError)
    case BadData    => HttpResponse(StatusCodes.BadRequest)
  }

  val routes: Route =
    pathPrefix("ok") {
      get {
        val res: IO[DomainError, String] = ZIO.succeed("OK")
        complete(res)
      }
    } ~ pathPrefix("internal_server_error") {
      get {
        val res: IO[DomainError, String] = ZIO.fail(FatalError)
        complete(res)
      }
    } ~
      pathPrefix("bad_request") {
        get {
          val res: IO[DomainError, String] = ZIO.fail(BadData)
          complete(res)
        }
      }
}
```
