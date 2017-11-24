package com.kubukoz.fantastic

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.kubukoz.fantastic.dao.{MockBookDao, RealBookDao}
import com.kubukoz.fantastic.routes.MainRoutes
import com.kubukoz.fantastic.services.BookService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn
import scala.language.higherKinds
import cats.instances.future._

object Main extends MainRoutes {
  implicit private val system: ActorSystem             = ActorSystem("fantastic")
  implicit private val materializer: ActorMaterializer = ActorMaterializer()

  private val routes: Route = mainRoutes

  def main(args: Array[String]): Unit = {
    runServer()
  }

  private def runServer(): Unit = {
    val port   = 8080
    val server = Http().bindAndHandle(routes, "localhost", port)

    StdIn.readLine()

    server.flatMap(_.unbind()).onComplete { _ =>
      system.terminate()
    }
  }

  override protected def bookService: BookService[Result] = new BookService[Result](new RealBookDao(null))
}
