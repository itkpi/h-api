package com.github

package object hapi {
  type ANil = Api.ANil
  val Anil = Api.ANil

  type ?::[F <: ApiFunc, A <: Api] = Api.?::[F, A]
  val ?:: = Api.?::

  type +?+[A <: Api, B <: Api] = Api.+?+[A, B]
  val +?+ = Api.+?+

  type IsPartOf[F <: ApiFunc, A <: Api] = Api.IsPartOf[F, A]
  val IsPartOf = Api.IsPartOf

  type ReturnsOnly[A <: Api, Out] = Api.ReturnsOnly[A, Out]
  val ReturnsOnly = Api.ReturnsOnly

  type TakesOnly[A <: Api, In] = Api.TakesOnly[A, In]
  val TakesOnly = Api.TakesOnly
}
