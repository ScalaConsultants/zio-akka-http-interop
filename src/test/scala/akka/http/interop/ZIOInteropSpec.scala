package akka.http.interop

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import zio.ZIO
import zio.test.Assertion._
import zio.test._

object ZIOInteropSpec extends ZIORouteTest {

  object SimpleRoutes extends ZIOSupport {
    val routes = pathPrefix("fail") {
      get {
        val res: ZIO[Any, Throwable, String] = ZIO.fail(new Throwable("error"))
        complete(res)
      }
    } ~ pathPrefix("success") {
      get {
        val res: ZIO[Any, Throwable, String] = ZIO.succeed("OK")
        complete(res)
      }
    }
  }

  val domainRoutes = Route.seal(Api.routes)
  val simpleRoutes = Route.seal(SimpleRoutes.routes)

  private val specs =
    suite("ZIO Interop routes")(
      test("succeed on /ok") {
        ZIO.attempt(Get("/ok") ~> domainRoutes ~> check {
          val s = status
          assert(s)(equalTo(StatusCodes.OK))
        })
      },
      test("fail with 500 on /internal_server_error") {
        ZIO.attempt(Get("/internal_server_error") ~> domainRoutes ~> check {
          val s = status
          assert(s)(equalTo(StatusCodes.InternalServerError))
        })
      },
      test("fail with 400 on /bad_request") {
        ZIO.attempt(Get("/bad_request") ~> domainRoutes ~> check {
          val s = status
          assert(s)(equalTo(StatusCodes.BadRequest))
        })
      },
      testM("fail with 400 on /bad_request_narrow") {
        ZIO.effect(Get("/bad_request_narrow") ~> domainRoutes ~> check {
          val s = status
          assert(s)(equalTo(StatusCodes.BadRequest))
        })
      },
      test("succeed fail with 500 on /fail (no domain errors)") {
        ZIO.attempt(Get("/fail") ~> simpleRoutes ~> check {
          val s = status
          assert(s)(equalTo(StatusCodes.InternalServerError))
        })
      },
      test("succeed on /success (no domain errors)") {
        ZIO.attempt(Get("/success") ~> simpleRoutes ~> check {
          val s = status
          assert(s)(equalTo(StatusCodes.OK))
        })
      }
    )

  def spec = specs
}
