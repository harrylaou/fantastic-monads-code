package com.kubukoz.fantastic.routes

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.Functor
import cats.data.ValidatedNel
import cats.syntax.functor._
import com.kubukoz.fantastic.Result
import com.kubukoz.fantastic.dto.{AppError, BookToCreate, RentBookRequest}
import com.kubukoz.fantastic.services.BookService
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.Encoder
import io.circe.generic.auto._
import scala.concurrent.ExecutionContext.Implicits.global
import cats.instances.future._
import scala.language.higherKinds

trait MainRoutes {
  protected def bookService: BookService[Result]

  val mainRoutes: Route = post {
    pathPrefix("rent") {
      entity(as[RentBookRequest]) { request =>
        complete {
          bookService.rentBook(request)
        }
      }
    } ~ pathPrefix("rentMany") {
      entity(as[List[RentBookRequest]]) { requests =>
        complete {
          bookService.rentBooks(requests)
        }
      }
    } ~ pathPrefix("add") {
      entity(as[BookToCreate]) { request =>
        complete {
          handleValidation {
            bookService.addBook(request)
          }
        }
      }
    }
  } ~ get {
    pathPrefix("all") {
      complete {
        bookService.findBooks()
      }
    }
  }

  //todo define custom marshallers SOMEHOW?
  def handleValidation[F[_]: Functor, E <: AppError, T: Encoder](
    result: F[ValidatedNel[E, T]]): F[ToResponseMarshallable] = {
    result.map {
      _.fold(
        e => (StatusCodes.BadRequest, e.toList.map(_.toString)),
        value => value
      )
    }
  }

}
