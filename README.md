http4s-auth
===========

[ ![Codeship Status for gvolpe/http4s-auth](https://app.codeship.com/projects/213c5170-05d7-0135-edfd-52b395dcacd9/status?branch=master)](https://app.codeship.com/projects/213643)
[![Coverage Status](https://coveralls.io/repos/github/gvolpe/http4s-auth/badge.svg?branch=master)](https://coveralls.io/github/gvolpe/http4s-auth?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/94bf4c355bca4354a8c2a1cda3dc40b9)](https://www.codacy.com/app/volpegabriel/http4s-auth?utm_source=github.com&utm_medium=referral&utm_content=gvolpe/http4s-auth&utm_campaign=badger)

Authentication library for [Http4s](http://http4s.org/)

## Introduction

Although Http4s [now supports](http://http4s.org/v0.15/auth/) basic token-based authentication, the idea behind this library is to support multiple authentication methods. And the same feature here is implemented quite differently. Besides, it works with previous version of Http4s v0.14.x.

## Usage

Just add the following dependencies to your project (examples are optional):

```scala
libraryDependencies ++= Seq(
  "com.github.gvolpe" %% "http4s-auth" % "0.1",
  "com.github.gvolpe" %% "http4s-auth-examples" % "0.1"
)
```

For now, it depends on Http4s v0.14.6 and it's only available for Scala 2.11.x. No releases scheduled for previous versions.

However, it's possible to exclude the Http4s dependencies from your project and just include the version you need. A demonstration project using http4s v0.15 and http4s-auth v0.1 can be found [here](https://github.com/gvolpe/http4s-auth-015-demo). Take into account that it's only possible to use the Secured feature without the provided endpoints signup, login and logout that are implemented on top of the version 0.14.6.

Http4s's guys are currently working very hard on the release of two major versions 0.16 & 0.17 that support Cats and FS2. It will be soon supported by this library.

## Authentication Methods

#### Basic Authentication

By adding the AuthHttpEndpoint.service to the services you will have available the endpoints signup, login and logout:

```scala
object Demo extends ServerApp {

  import BindingsModule._

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(AuthHttpEndpoint.service)
      .mountService(DemoHttpEndpoint.service)
      .start
  }

}
```

In order to secure an endpoint you just need to use **Secured** and have in scope implementations for TokenRepository and UserRepository:

```scala
import com.gvolpe.http4s.auth.service.Secured

object DemoHttpEndpoint {

 import com.gvolpe.http4s.auth.demo.BindingsModule._

  val service = HttpService {
    case GET -> Root / "public" =>
      Ok("Public resource")
    case req @ GET -> Root / "protected" => Secured(req) {
      Ok("Protected resource")
    }
  }

}
```

By default, InMemory representations of the repositories are available. The BindingsModule object demonstrates a recommended way of use:

```scala
import com.gvolpe.http4s.auth.repository.{InMemoryTokenRepository, InMemoryUserRepository}

object BindingsModule {

  implicit val authTokenRepository = new InMemoryTokenRepository()
  implicit val authUserRepository  = new InMemoryUserRepository()

}
```

#### Signup

- Method: POST
- JSON body: 

```json
{
  "username": "gvolpe",
  "password: "123456"
}
```
- Responses:

	- **CONFLICT 409** in case of failure (user already exists!).
	- **CREATED 201** in case of success with body including token:
	```
	{ 
	  "value": "35c07890-209d-11e7-a3b7-d13fe8119206"
	}
	```

#### Login

- Method: POST
- JSON body:

```json
{
  "username": "gvolpe",
  "password: "123456"
}
```
- Responses:

	- **200 OK** in case of success with same body as signup.
	- **401 UNAUTHORIZED** in case the password doesn't match.
	- **NOTFOUND 404** if the user does not exist.

#### Logout

- Method: POST
- Header: X-Auth-Token with the token value
- Responses:

	- **204 NoContent** in case of success.
	- **404 NotFound** in case the user does not exist.

#### Protected Endpoint

Any protected endpoint should include the **X-Auth-Token** in the request headers in order to authenticate it. Example:

```json
curl -X GET -H "X-Auth-Token: 35c07890-209d-11e7-a3b7-d13fe8119206" http://localhost:8080/protected
```

--------------------------------------------

### COMING SOON... WORK IN PROGRESS ...

#### OAuth1
#### OAuth2
#### OpenID 
#### CAS

## LICENSE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with
the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.
