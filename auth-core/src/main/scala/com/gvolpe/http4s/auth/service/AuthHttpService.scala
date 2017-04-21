package com.gvolpe.http4s.auth.service

import com.gvolpe.http4s.auth.model._
import com.gvolpe.http4s.auth.repository.exceptions.TokenNotFound
import com.gvolpe.http4s.auth.repository.{TokenRepository, UserRepository}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.slf4j.LoggerFactory

import scalaz.{-\/, \/-}
import scalaz.EitherT.eitherT
import scalaz.concurrent.Task

object AuthHttpService {

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = jsonEncoderOf[A]

  private val XAuthToken = "x-auth-token"

  private val log = LoggerFactory.getLogger(getClass)

  val unauthorized: Task[Response] = Response(Status.Unauthorized).withBody("Unauthorized.")

  def findTokenFromHeaders(headers: List[Header]): Option[HttpUser] =
    headers.find(_.name.toString() == XAuthToken) flatMap { tokenHeader =>
      HttpUser.validateToken(HttpToken(tokenHeader.value))
    }

  def findHttpUser(headers: List[Header])(implicit tokenRepo: TokenRepository): Task[HttpUser] =
    findTokenFromHeaders(headers).fold[Task[HttpUser]](Task.fail(TokenNotFound)){ token =>
      tokenRepo.find(token.httpToken)
    }

  def signUp(form: SignUpForm)(implicit tokenRepo: TokenRepository, userRepo: UserRepository): Task[Response] = {
    userRepo.find(form.username) flatMap(user => Conflict(s"User with username ${user.username} already exists!")) or
      createUser(form.username, form.password).flatMap(Created(_))
  }

  private def createUser(username: String, password: String)(implicit tokenRepo: TokenRepository, userRepo: UserRepository): Task[HttpToken] = {
    lazy val token = HttpUser.createToken(username)
    for {
      _ <- userRepo.save(User(username, User.encrypt(password)))
      _ <- tokenRepo.save(HttpUser(username, token))
    } yield token
  }


  def logout(req: Request)(implicit tokenRepo: TokenRepository): Task[Response] = {
    findHttpUser(req.headers.toList).flatMap(tokenRepo.remove).flatMap(_ => NoContent()) or NotFound().handleWith {
      case error =>
        log.info(s"Logout: ${error.getMessage}")
        InternalServerError()
    }
  }

  def login(form: LoginForm)(implicit tokenRepo: TokenRepository, userRepo: UserRepository): Task[Response] = {
//    userRepo.find(form.username) flatMap {
//      case Some(user) if User.isPasswordValid(user.password, form.password) =>
//        val token = HttpUser.createToken(form.username)
//        val user  = HttpUser(form.username, token)
//        tokenRepo.save(user).flatMap {
//          case \/-(())    =>
////            val expires = Some(Instant.now().plus(1, ChronoUnit.DAYS))
//            Ok(token) //.addCookie(Cookie(XAuthToken, token.token, expires))
//          case -\/(error) =>
//            log.info(s"Logout: ${error.getMessage}")
//            InternalServerError(error.getMessage)
//        }
//      case Some(user) =>
//        unauthorized
//      case None =>
//        NotFound(s"Username ${form.username} not found!")
//    }

    NotFound(s"Username ${form.username} not found!")
  }

}

object Secured {
  def apply(req: Request)(response: Task[Response])(implicit repo: TokenRepository): Task[Response] =
    AuthHttpService.findHttpUser(req.headers.toList).flatMap(_ => response) or AuthHttpService.unauthorized
}