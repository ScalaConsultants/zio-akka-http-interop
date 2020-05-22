package akka.http

import zio.Has

package object interop {
  type HttpServer = Has[HttpServer.Service]
}
