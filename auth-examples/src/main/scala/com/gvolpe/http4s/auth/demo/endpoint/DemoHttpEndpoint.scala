package com.gvolpe.http4s.auth.demo.endpoint

import com.gvolpe.http4s.auth.service.Secured
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object DemoHttpEndpoint {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = jsonOf[A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = jsonEncoderOf[A]

  import com.gvolpe.http4s.auth.demo.BindingsModule._

  val service = HttpService {
    case GET -> Root / "public" =>
      Ok("Public resource")
    case req @ GET -> Root / "protected" => Secured(req) {
      Ok("Protected resource")
    }
  }

}
