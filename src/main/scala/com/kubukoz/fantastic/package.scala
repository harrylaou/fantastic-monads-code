package com.kubukoz

import cats.Id

package object fantastic {
  type Result[T] = Id[T]
}
