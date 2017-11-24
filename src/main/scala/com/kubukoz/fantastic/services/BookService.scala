package com.kubukoz.fantastic.services

import cats.data.ValidatedNel
import cats.instances.future._
import cats.syntax.apply._
import cats.syntax.validated._
import com.kubukoz.fantastic.dao.BookDao
import com.kubukoz.fantastic.data.{Book, BookId}
import com.kubukoz.fantastic.dto.BookCreateError.{InvalidISBN, InvalidName}
import com.kubukoz.fantastic.dto.RentError.{BookAlreadyRented, BookNotFound}
import com.kubukoz.fantastic.dto._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BookService(dao: BookDao) {
  type RentResult = Either[RentError, Unit]

  def findBooks(): Future[List[Book]] = dao.findAll()

  val addBook: BookToCreate => Future[ValidatedNel[BookCreateError, BookId]] = {

    val validatedBook: BookToCreate => ValidatedNel[BookCreateError, Book] = { toCreate =>
      val validateISBN = toCreate.isbn.valid.ensure(InvalidISBN)(_.length == 10)
      val validateName = toCreate.name.valid.ensure(InvalidName)(s => (1 to 10).contains(s.length))

      (
        BookId("-").validNel[BookCreateError],
        validateISBN.toValidatedNel,
        validateName.toValidatedNel
      ).mapN(Book)
    }

    validatedBook(_).traverse(dao.saveBook)
  }

  def rentBook(request: RentBookRequest): Future[RentResult] = {

    def rentIfNotRented(isRented: Boolean): Future[RentResult] = {
      if (isRented) Future.successful(Left(BookAlreadyRented))
      else dao.rentBook(request.bookId).map(Right(_))
    }

    val findF: Future[Option[Book]] = dao.findById(request.bookId)
    val isRentedF: Future[Boolean]  = dao.isRented(request.bookId)

    for {
      bookEither <- findF.map(_.toRight(BookNotFound))
      isRented   <- isRentedF

      result <- bookEither.fold(
        e => Future.successful(Left(e)),
        _ => rentIfNotRented(isRented)
      )
    } yield result
  }

  final def rentBooks(request: List[RentBookRequest]): Future[List[RentResult]] =
    Future.sequence(request.map(rentBook))
}
