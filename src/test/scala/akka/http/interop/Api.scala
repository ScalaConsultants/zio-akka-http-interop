package akka.http.interop

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
