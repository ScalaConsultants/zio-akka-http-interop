package akka.http.interop

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import zio._

object HttpServer {

  trait Service {
    def start: Managed[Throwable, Http.ServerBinding]
  }

  val live: ZLayer[Has[ActorSystem] with Has[Config] with Has[Route], Nothing, HttpServer] =
    ZLayer.fromServices[ActorSystem, Config, Route, HttpServer.Service] { (sys, cfg, routes) =>
      new Service {
        implicit val system: ActorSystem = sys

        val start: Managed[Throwable, Http.ServerBinding] =
          ZManaged.make(ZIO.fromFuture(_ => Http().newServerAt(cfg.host, cfg.port).bind(routes)))(b =>
            ZIO.fromFuture(_ => b.unbind()).orDie
          )
      }
    }

  def start: ZManaged[HttpServer, Throwable, Http.ServerBinding] =
    ZManaged.accessManaged[HttpServer](_.get.start)

  final case class Config(host: String, port: Int)
}
