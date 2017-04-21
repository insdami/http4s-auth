package com.gvolpe.http4s.auth.extension

import scalaz.concurrent.Task


object TaskExtension {

  implicit class OptionOps[A](opt: Option[A]) {
    def toTask(ifEmpty: => Throwable): Task[A] =
      opt.fold[Task[A]](Task.fail(ifEmpty))(Task.delay(_))
  }

}
