package com.kubukoz.fantastic.dto

import io.circe.{Decoder, Encoder}

case class BookToCreate(isbn: String, name: String)

sealed trait BookCreateError extends AppError

object BookCreateError {

  case object InvalidName extends BookCreateError

  case object InvalidISBN extends BookCreateError
}

case class BookId(value: String) extends AnyVal

object BookId {
  implicit val encoder: Encoder[BookId] = Encoder[String].contramap(_.value)
  implicit val decoder: Decoder[BookId] = Decoder[String].map(BookId(_))
}
