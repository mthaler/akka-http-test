package com.mthaler.akkahttptest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, Authorization}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.Future

object RequestLevelHttpClient extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val auth = Authorization(BasicHttpCredentials("test", "test"))

  val responseFuture: Future[String] =
    Http().singleRequest(HttpRequest(uri = "http://127.0.0.1:8888/test.txt", headers = List(auth))).flatMap { response =>
      if (response.status == StatusCodes.OK) {
        Unmarshal(response).to[String]
      } else {
        Future.successful("Error: " + response.status)
      }
    }

  responseFuture.onSuccess {
    case result => println(result)
  }
}
