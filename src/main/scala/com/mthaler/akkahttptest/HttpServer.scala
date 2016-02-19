package com.mthaler.akkahttptest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

object HttpServer extends App {

  implicit val system = ActorSystem("system")
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  // credentials used to authenticate users
  val credentials = Map("test" -> ("test", Set("user")))

  // authenticator used to authenticate users, uses credentials
  def authenticator[T](cred: Credentials) : Option[Set[String]] =
    cred match {
      case cred@Credentials.Provided(name) =>
        credentials.get(name).flatMap {
          case (pass, roles) =>
            if(cred.verify(pass))
              Some(roles)
            else
              None
        }
      case Credentials.Missing =>
        None
    }

  val route = authenticateBasic("HTTP test server", authenticator) { roles ⇒
    get {
      path ("test.txt") {
        complete ("Hello, World")
      }
    }
  }

  val binding = Http(system).bindAndHandle(
    interface = "0.0.0.0",
    port = 8888,
    handler = route
  )

  binding onFailure {
    case ex: Exception => println("Failed to bind to port 8888, reason {}", ex)
  }
}
