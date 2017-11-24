package com.kubukoz

import cats.Id

import scala.concurrent.Future

package object fantastic {
  type Result[T] = Future[T]
}
