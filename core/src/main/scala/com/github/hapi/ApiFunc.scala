package com.github.hapi
import scala.annotation.implicitNotFound

/**
  * Generic class
  * representing a part of API
  **/
trait ApiFunc {

  /** Abstract type representing function parameter type */
  type In

  /** Abstract type representing request to the api  */
  type Out

  /**
    * Creates a request based on function [[In]]
    *
    * @param in - input
    * @return - request to the api
    **/
  def apply(in: In): Out
}

object ApiFunc {
  type AuxOut[Out0] = ApiFunc { type Out = Out0 }
  type AuxIn[In0] = ApiFunc { type In = In0 }
  type Aux[In0, Out0] = ApiFunc { type In = In0; type Out = Out0 }
}

sealed trait Api extends Product with Serializable

object Api {

  /**
    * An empty [[Api]]
    * (like shapeless.HNil
    **/
  sealed trait ANil extends Api

  /** A single instance of [[ANil]] */
  case object ANil extends ANil

  /**
    * Cons type (like shapeless.::)
    *
    * @tparam PH - head of API (is a [[ApiFunc]])
    * @tparam PT - tail of API (actually [[ANil]] or another [[?::]])
    * @param headFunc - head function
    * @param rest     - rest of API functions
    **/
  final case class ?::[PH <: ApiFunc, PT <: Api] private () extends Api

  final case class +?+[A <: Api, B <: Api] private () extends Api

  /**
    * Prove that api function `F` is a part of  api `A`.
    *
    * @tparam F - some api function
    * @tparam A - api
    **/
  @implicitNotFound("Function ${F} is not a part of ${A}")
  sealed trait IsPartOf[F <: ApiFunc, A <: Api] extends Serializable
  object IsPartOf {
    def apply[F <: ApiFunc, A <: Api](
      implicit params: IsPartOf[F, A]
    ): IsPartOf[F, A] = params

    /**
      * It is obvious that function [[F]] is part of api `H ?:: T`
      * where H == [[F]]
      **/
    implicit def headCase[F <: ApiFunc, T <: Api]: IsPartOf[F, F ?:: T] =
      new IsPartOf[F, F ?:: T] {}

    /**
      * If [[H]] != [[F]] and there are some tail [[T]]
      * prove search continues in the tail
      **/
    implicit def tailCase[H <: ApiFunc, T <: Api, F <: ApiFunc](
      implicit att: IsPartOf[F, T]
    ): IsPartOf[F, H ?:: T] = new IsPartOf[F, H ?:: T] {}

    implicit def leftCase[F <: ApiFunc, A <: Api, B <: Api](
      implicit ev: IsPartOf[F, A]
    ): IsPartOf[F, A +?+ B] = new IsPartOf[F, A +?+ B] {}

    implicit def rightCase[F <: ApiFunc, A <: Api, B <: Api](
      implicit ev: IsPartOf[F, B]
    ): IsPartOf[F, A +?+ B] = new IsPartOf[F, A +?+ B] {}
  }
  @implicitNotFound("""
      Cannot prove that all functions in ${A} returns only ${Out}.
      The use-site expects all functions returns only ${Out}
      Please inspect the whole API signature.
      Probably some functions don't return ${Out}
    """)
  sealed trait ReturnsOnly[A <: Api, Out] extends Serializable {
    final def lifted[F <: ApiFunc](f: F)(params: f.In)(
      implicit ev: F IsPartOf A
    ): Out = f(params).asInstanceOf[Out]
  }
  object ReturnsOnly {
    def apply[A <: Api, Out](
      implicit params: ReturnsOnly[A, Out]
    ): ReturnsOnly[A, Out] = params

    /**
      * For API [[F ?:: ANil]]
      * if [[F]] returns [[Out]]
      * then the whole api only returns [[Out]]
      **/
    implicit def headCase[Out, F <: ApiFunc.AuxOut[_ <: Out]]
      : ReturnsOnly[F ?:: ANil, Out] =
      new ReturnsOnly[F ?:: ANil, Out] {}

    /**
      * For API [[F ?:: T]]
      * If [[F]] returns [[Out]]
      * and [[T]] only returns [[Out]]
      * then the whole API only returns [[Out]]
      **/
    implicit def tailCase[Out, F <: ApiFunc.AuxOut[_ <: Out], T <: Api](
      implicit tailReturnsOnly: ReturnsOnly[T, Out]
    ): ReturnsOnly[F ?:: T, Out] =
      new ReturnsOnly[F ?:: T, Out] {}

    /**
      * For API [[A +?+ B]]
      * If [[A]]  only returns [[Out]]
      * and [[B]] only returns [[Out]]
      * then the whole API only returns [[Out]]
      **/
    implicit def concatCase[Out, A <: Api, B <: Api](
      implicit leftReturnsOnly: ReturnsOnly[A, Out],
      rightReturnsOnly: ReturnsOnly[B, Out]
    ): ReturnsOnly[A +?+ B, Out] = new ReturnsOnly[A +?+ B, Out] {}
  }

  @implicitNotFound("""
      Cannot prove that all functions in ${A} take only ${In}.
      The use-site expects all functions takes only ${In}
      Please inspect the whole API signature.
      Probably some functions don't take ${In}
    """)
  sealed trait TakesOnly[A <: Api, In] extends Serializable {
    final def lifted[F <: ApiFunc](f: F)(params: f.In)(
      implicit ev: F IsPartOf A
    ): In = f(params).asInstanceOf[In]
  }
  object TakesOnly {
    def apply[A <: Api, In](
      implicit params: TakesOnly[A, In]
    ): TakesOnly[A, In] = params

    /**
      * For API [[F ?:: ANil]]
      * if [[F]] takes [[In]]
      * then the whole api only returns [[In]]
      **/
    implicit def headCase[In, F <: ApiFunc.AuxIn[_ <: In]]
      : TakesOnly[F ?:: ANil, In] =
      new TakesOnly[F ?:: ANil, In] {}

    /**
      * For API [[F ?:: T]]
      * If [[F]] takes [[In]]
      * and [[T]] only takes [[In]]
      * then the whole API only takes [[In]]
      **/
    implicit def tailCase[In, F <: ApiFunc.AuxIn[_ <: In], T <: Api](
      implicit tailTakesOnly: TakesOnly[T, In]
    ): TakesOnly[F ?:: T, In] =
      new TakesOnly[F ?:: T, In] {}

    /**
      * For API [[A +?+ B]]
      * If [[A]]  only takes [[In]]
      * and [[B]] only takes [[In]]
      * then the whole API only takes [[In]]
      **/
    implicit def concatCase[In, A <: Api, B <: Api](
      implicit leftTakesOnly: TakesOnly[A, In],
      rightTakesOnly: TakesOnly[B, In]
    ): TakesOnly[A +?+ B, In] = new TakesOnly[A +?+ B, In] {}
  }
}
