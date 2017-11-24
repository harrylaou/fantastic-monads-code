package com.kubukoz.fantastic.dao

import cats.Id
import com.kubukoz.fantastic.data.{Book, BookId}

import scala.concurrent.Future
import scala.language.higherKinds
import scala.util.Random

trait BookDao[F[_]] {

  def findAll(): F[List[Book]]

  def isRented(bookId: BookId): F[Boolean]

  def findById(bookId: BookId): F[Option[Book]]

  def rentBook(bookId: BookId): F[Unit]

  def saveBook(book: Book): F[BookId]
}

object MockBookDao extends BookDao[Id] {
  type IsRented = Boolean

  private var memory: Map[BookId, (Book, IsRented)] = Map(
    BookId("1") -> (Book(BookId("1"), "1234567890", "FP in Scala") -> false),
    BookId("2") -> (Book(BookId("2"), "1234567891", "FP in Java")  -> true)
  )

  override def findAll(): List[Book] = memory.values.map(_._1).toList

  override def isRented(bookId: BookId): Boolean =
    memory.get(bookId).fold(false)(_._2)

  override def findById(bookId: BookId): Option[Book] =
    memory.get(bookId).map(_._1)

  override def rentBook(bookId: BookId): Unit = {
    memory.get(bookId).foreach {
      case (book, _) =>
        memory = memory.updated(bookId, book -> true)
    }

    Unit
  }

  override def saveBook(book: Book): BookId = {
    val newBookId = BookId(Random.nextString(10)) //don't do this at home

    memory += (newBookId -> (book.copy(id = newBookId), false))

    newBookId
  }

}

//e.g. Slick
trait Database {}

class RealBookDao(db: Database) extends BookDao[Future] {
  override def findAll(): Future[List[Book]] = ???

  override def isRented(bookId: BookId): Future[Boolean] = ???

  override def findById(bookId: BookId): Future[Option[Book]] = ???

  override def rentBook(bookId: BookId): Future[Unit] = ???

  override def saveBook(book: Book): Future[BookId] = ???
}
