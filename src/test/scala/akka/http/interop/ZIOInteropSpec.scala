package akka.http.interop

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import zio._
import zio.test.Assertion._
import zio.test._

object ZIOInteropSpec extends ZIORouteTest {

  val routes = Route.seal(Api.routes)

  private val specs: Spec[Any, TestFailure[Throwable], TestSuccess] =
    suite("Api")(
      testM("succeed on /a") {
        ZIO.effect(Get("/a") ~> routes ~> check {
          val s = status
          assert(s)(equalTo(StatusCodes.OK))
        })
      },
      testM("fail with 500 on /b") {
        ZIO.effect(Get("/b") ~> routes ~> check {
          val s = status
          assert(s)(equalTo(StatusCodes.InternalServerError))
        })
      },
      testM("fail with 400 on /c") {
        ZIO.effect(Get("/c") ~> routes ~> check {
          val s = status
          assert(s)(equalTo(StatusCodes.BadRequest))
        })
      }
    )

  def spec = specs
}
