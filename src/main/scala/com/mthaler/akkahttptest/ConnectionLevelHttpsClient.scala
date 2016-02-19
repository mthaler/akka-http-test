package com.mthaler.akkahttptest

import java.security.cert.X509Certificate
import javax.net.ssl._

import akka.actor.ActorSystem
import akka.http.scaladsl.{HttpsContext, Http}
import akka.http.scaladsl.model.{StatusCodes, HttpResponse, HttpRequest}
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, Authorization}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source, Flow}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

object ConnectionLevelHttpsClient extends App {

  val config = ConfigFactory.parseURL(getClass.getResource("httpsclient.conf"))

  implicit val system = ActorSystem("ConnectionLevelHttpsClient", config)
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val auth = Authorization(BasicHttpCredentials("test", "test"))

  private val trustfulSslContext: SSLContext = {

    object NoCheckX509TrustManager extends X509TrustManager {
      override def checkClientTrusted(chain: Array[X509Certificate], authType: String) = ()
      override def checkServerTrusted(chain: Array[X509Certificate], authType: String) = ()
      override def getAcceptedIssuers = Array[X509Certificate]()
    }

    val context = SSLContext.getInstance("TLS")
    context.init(Array[KeyManager](), Array(NoCheckX509TrustManager), null)
    context
  }

  val trustfulClientContext: HttpsContext =
    HttpsContext(trustfulSslContext)

  val allHostsValid = new HostnameVerifier() {
    override def verify(s: String, sslSession: SSLSession): Boolean = true
  }
  HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)


  val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnectionTls("127.0.0.1", 6443, httpsContext = Some(trustfulClientContext))
  val responseFuture: Future[String] =
    Source.single(HttpRequest(uri = "/test.txt", headers = List(auth)))
      .via(connectionFlow)
      .runWith(Sink.head).flatMap { response =>
      if (response.status == StatusCodes.OK) {
        Unmarshal(response).to[String]
      } else {
        Future.successful("Error: " + response.status)
      }
    }
  responseFuture.onSuccess {
    case result => println(result)
  }
  responseFuture.onFailure {
    case ex => ex.printStackTrace()
  }
}
