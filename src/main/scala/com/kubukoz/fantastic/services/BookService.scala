package com.kubukoz.fantastic.services

import cats.Monad
import cats.data.ValidatedNel
import cats.instances.future._
import cats.instances.list._
import cats.syntax.flatMap._
import cats.syntax.apply._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.syntax.validated._
import com.kubukoz.fantastic.dao.BookDao
import com.kubukoz.fantastic.data.{Book, BookId}
import com.kubukoz.fantastic.dto.BookCreateError.{InvalidISBN, InvalidName}
import com.kubukoz.fantastic.dto.RentError.{BookAlreadyRented, BookNotFound}
import com.kubukoz.fantastic.dto._

import scala.language.higherKinds

class BookService[F[_]](dao: BookDao[F])(implicit M: Monad[F]) {
  type RentResult = Either[RentError, Unit]

  def findBooks(): F[List[Book]] = dao.findAll()

  val addBook: BookToCreate => F[ValidatedNel[BookCreateError, BookId]] = {

    val validatedBook: BookToCreate => ValidatedNel[BookCreateError, Book] = { toCreate =>
      val validateISBN = toCreate.isbn.valid.ensure(InvalidISBN)(_.length == 10)
      val validateName = toCreate.name.valid.ensure(InvalidName)(s => (1 to 10).contains(s.length))

      (
        BookId("-").validNel,
        validateISBN.toValidatedNel,
        validateName.toValidatedNel
      ).mapN(Book)
    }

    bookToCreate => validatedBook(bookToCreate).traverse(dao.saveBook)
  }

  def rentBook(request: RentBookRequest): F[RentResult] = {

    def rentIfNotRented(isRented: Boolean): F[RentResult] = {
      if (isRented) M.pure(Left(BookAlreadyRented))
      else dao.rentBook(request.bookId).map(Right(_))
    }

    val findF: F[Option[Book]] = dao.findById(request.bookId)
    val isRentedF: F[Boolean]  = dao.isRented(request.bookId)

    for {
      bookEither <- findF.map(_.toRight(BookNotFound))
      isRented   <- isRentedF

      result <- bookEither.fold(
        e => M.pure(Left(e)),
        _ => rentIfNotRented(isRented)
      )
    } yield result
  }

  final def rentBooks(request: List[RentBookRequest]): F[List[RentResult]] =
    request.traverse(rentBook)
}
