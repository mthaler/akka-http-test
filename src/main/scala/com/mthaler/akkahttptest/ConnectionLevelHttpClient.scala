package com.mthaler.akkahttptest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import scala.concurrent.Future

object ConnectionLevelHttpClient extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http().outgoingConnection("127.0.0.1", 8888)
  val responseFuture =
    Source.single(HttpRequest(uri = "/test.txt"))
      .via(connectionFlow)
      .runWith(Sink.head).map { response =>
      if (response.status == StatusCodes.OK) {
        println("Result: " + response.entity)
      } else {
        println("Error: " + response.status)
      }
    }
}
