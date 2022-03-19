package akka.http.interop

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import zio._

final case class Config(host: String, port: Int)

trait HttpServer {
  def start(sys: ActorSystem, cfg: Config, routes: Route): ZIO[Any, Throwable, Http.ServerBinding]
}


final case class HttpServerImpl() extends HttpServer {
  def start(sys: ActorSystem, cfg: Config, routes: Route): ZIO[Any, Throwable, Http.ServerBinding] = ???
//    ZManaged.make(ZIO.fromFuture(_ => Http().newServerAt(cfg.host, cfg.port).bind(routes)))(b =>
//                                                                                              ZIO.fromFuture(_ => b.unbind()).orDie)

}

object HttpServer {
  val live: ZLayer[Any, Nothing, HttpServer] = (HttpServerImpl.apply _).toLayer
}
