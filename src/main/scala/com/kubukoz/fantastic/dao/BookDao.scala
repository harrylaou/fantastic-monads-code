package com.kubukoz.fantastic.dao

import com.kubukoz.fantastic.data.Book
import com.kubukoz.fantastic.dto.BookId

import scala.concurrent.Future
import scala.util.Random

trait BookDao {

  def findAll(): Future[List[Book]]

  def isRented(bookId: BookId): Future[Boolean]

  def findById(bookId: BookId): Future[Option[Book]]

  def rentBook(bookId: BookId): Future[Unit]

  def saveBook(book: Book): Future[BookId]
}

object MockBookDao extends BookDao {
  type IsRented = Boolean

  var memory: Map[BookId, (Book, IsRented)] = Map(
    BookId("1") -> (Book(BookId("1"), "1234567890", "FP in Scala") -> false),
    BookId("2") -> (Book(BookId("2"), "1234567891", "FP in Java")  -> true)
  )

  override def findAll(): Future[List[Book]] = Future.successful(memory.values.map(_._1).toList)

  override def isRented(bookId: BookId): Future[Boolean] =
    Future.successful(memory.get(bookId).fold(false)(_._2))

  override def findById(bookId: BookId): Future[Option[Book]] =
    Future.successful(memory.get(bookId).map(_._1))

  override def rentBook(bookId: BookId): Future[Unit] = {
    memory.get(bookId).foreach {
      case (book, _) =>
        memory = memory.updated(bookId, book -> true)
    }

    Future.successful(Unit)
  }

  override def saveBook(book: Book): Future[BookId] = {
    val newBookId = BookId(Random.nextString(10)) //don't do this at home

    memory += (newBookId -> (book.copy(id = newBookId), false))

    Future.successful(newBookId)
  }
}
