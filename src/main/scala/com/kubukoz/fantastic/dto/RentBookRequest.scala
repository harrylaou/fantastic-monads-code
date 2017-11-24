package com.kubukoz.fantastic.dto

case class RentBookRequest(bookId: BookId)

sealed trait RentError extends AppError

object RentError {

  case object BookNotFound extends RentError

  case object BookAlreadyRented extends RentError
}
