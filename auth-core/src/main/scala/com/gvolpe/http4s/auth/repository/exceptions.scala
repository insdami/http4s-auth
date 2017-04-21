package com.gvolpe.http4s.auth.repository


object exceptions {

  sealed trait RepositoryException extends Throwable
  case object UserNotFound         extends RepositoryException
  case object TokenNotFound        extends RepositoryException


}
