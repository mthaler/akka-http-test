package com.mthaler.akkahttptest

import java.security.{SecureRandom, KeyStore}
import javax.net.ssl.{KeyManagerFactory, SSLContext}

import akka.actor.ActorSystem
import akka.http.scaladsl.{HttpsContext, Http}
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

object HttpsServer extends App {

  implicit val system = ActorSystem("system")
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val serverContext: HttpsContext = {
    val password = "123456789".toCharArray
    val context = SSLContext.getInstance("TLS")
    val ks = KeyStore.getInstance("PKCS12")
    val is = getClass.getResource("mykeystore.pkcs12").openStream()
    ks.load(is, password)
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)
    context.init(keyManagerFactory.getKeyManagers, null, new SecureRandom)
    // start up the web server
    HttpsContext(context)
  }

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
    port = 6443,
    handler = route,
    httpsContext = Some(serverContext)
  )

  binding onFailure {
    case ex: Exception => println("Failed to bind to port 8888, reason {}", ex)
  }
}
