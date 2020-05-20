package akka.http.interop

import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }

/**
 * Describes how to map a custom domain error into an HTTP server response
 */
trait ErrorResponse[E] {
  def toHttpResponse(e: E): HttpResponse
}

object ErrorResponse {
  implicit val throwableAsInternalServerError: ErrorResponse[Throwable] =
    _ => HttpResponse(StatusCodes.InternalServerError)
}
