package com.mthaler.akkahttptest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, Authorization}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.Future

object ConnectionLevelHttpClient extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val auth = Authorization(BasicHttpCredentials("test", "test"))

  val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnection("127.0.0.1", 8888)
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
}
