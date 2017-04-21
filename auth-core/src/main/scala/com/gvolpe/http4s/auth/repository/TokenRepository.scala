package com.gvolpe.http4s.auth.repository

import com.gvolpe.http4s.auth.model.{HttpToken, HttpUser}

import scala.collection.mutable
import scalaz.\/
import scalaz.syntax.either._
import scalaz.concurrent.Task
import com.gvolpe.http4s.auth.extension.TaskExtension._
import com.gvolpe.http4s.auth.repository.exceptions.{TokenNotFound, UserNotFound}

trait TokenRepository {
  def find(token: HttpToken): Task[HttpUser]
  def save(user: HttpUser): Task[Throwable \/ Unit]
  def remove(user: HttpUser): Task[HttpUser]
}

class InMemoryTokenRepository() extends TokenRepository {
  private val tokens = mutable.HashMap.empty[HttpToken, HttpUser]

  override def find(token: HttpToken): Task[HttpUser] = tokens.get(token).toTask(TokenNotFound)
  override def save(user: HttpUser): Task[Throwable \/ Unit] = Task.delay { tokens.update(user.httpToken, user).right }
  override def remove(user: HttpUser): Task[HttpUser] = tokens.remove(user.httpToken).toTask(UserNotFound)

}