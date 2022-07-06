package akka.http.interop

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import zio._

trait HttpServer  {
  def start: ZIO[Scope, Throwable, Http.ServerBinding]
}
object HttpServer {

  val live: ZLayer[ActorSystem with Config with Route, Nothing, HttpServer] =
    ZLayer {
      for {
        sys    <- ZIO.service[ActorSystem]
        cfg    <- ZIO.service[Config]
        routes <- ZIO.service[Route]
      } yield new HttpServer {
        implicit val system: ActorSystem = sys

        val start: ZIO[Scope, Throwable, Http.ServerBinding] =
          ZIO.acquireRelease(ZIO.fromFuture(_ => Http().newServerAt(cfg.host, cfg.port).bind(routes)))(b =>
            ZIO.fromFuture(_ => b.unbind()).orDie
          )
      }
    }

  def start: ZIO[Scope with HttpServer, Throwable, Http.ServerBinding] =
    ZIO.serviceWithZIO[HttpServer](_.start)

  final case class Config(host: String, port: Int)
}
