package com.github.hapi.sttp

import scala.language.higherKinds
import com.github.hapi._
import com.softwaremill.sttp._

sealed abstract class BaseUriConnector[R[_], S, A <: Api](
  implicit backend: SttpBackend[R, S],
  ev: A ReturnsOnly Uri
)

abstract class UriApiConnector[R[_], S, A <: Api](
  implicit backend: SttpBackend[R, S],
  ev: A ReturnsOnly Uri
) {
  protected def buildRequest[T](uri: Uri): Request[T, S]

  def apiRequest[F <: ApiFunc, T, S0](
    f: F
  )(in: f.In)(implicit isPartOf: F IsPartOf A): R[Response[T]] =
    buildRequest(ev.lifted[F](f)(in)).send()
}

abstract class UriApiConnectorWithSession[R[_], S, A <: Api, Credentials, Session](
  implicit backend: SttpBackend[R, S],
  ev: A ReturnsOnly Uri
) {
  protected def buildRequest[T](uri: Uri)(
    implicit session: Session
  ): Request[T, S]

  def openSession(credentials: Credentials): R[Session]

  def closeSession(session: Session): R[Unit]

  def apiRequest[F <: ApiFunc, T, S0](f: F)(
    in: f.In
  )(implicit session: Session, isPartOf: F IsPartOf A): R[Response[T]] =
    buildRequest(ev.lifted[F](f)(in)).send()
}
