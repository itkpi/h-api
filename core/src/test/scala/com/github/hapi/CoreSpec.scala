package com.github.hapi

import org.scalatest.FlatSpec

trait Foo1 extends ApiFunc {
  final type In = Int
  final type Out = String
  def apply(in: Int): String = s"foo#$in"
}

trait Foo2 extends ApiFunc {
  final type In = Int
  final type Out = String
  def apply(in: Int): String = (BigInt(in) pow in).toString
}

trait Foo3 extends ApiFunc {
  final type In = Int
  final type Out = String
  def apply(in: Int): String = in.toString
}

object Foo {
  val Foo1: Foo1 = new Foo1 {}
  val Foo2: Foo2 = new Foo2 {}
  val Foo3: Foo3 = new Foo3 {}

  type Api = Foo1 ?:: Foo2 ?:: Foo3 ?:: ANil
}

trait Bar1 extends ApiFunc {
  final type In = String
  final type Out = Int
  def apply(in: String): Int = in.length
}

trait Bar2 extends ApiFunc {
  final type In = String
  final type Out = Int
  def apply(in: String): Int = in.count(_ == 'b')
}

trait Bar3 extends ApiFunc {
  final type In = String
  final type Out = String
  def apply(in: String): String = "bar_" + in
}

object Bar {
  val Bar1: Bar1 = new Bar1 {}
  val Bar2: Bar2 = new Bar2 {}
  val Bar3: Bar3 = new Bar3 {}

  type Api = Bar1 ?:: Bar2 ?:: Bar3 ?:: ANil
}

trait Baz1 extends ApiFunc {
  final type In = String
  final type Out = String
  def apply(in: String): String = in + "baz"
}

trait Baz2 extends ApiFunc {
  final type In = String
  final type Out = String
  def apply(in: String): String = in.map(_ + "baz").mkString
}

trait Baz3 extends ApiFunc {
  final type In = String
  final type Out = String
  def apply(in: String): String = "baz_" + in + "_baz"
}

object Baz {
  val Baz1: Baz1 = new Baz1 {}
  val Baz2: Baz2 = new Baz2 {}
  val Baz3: Baz3 = new Baz3 {}

  type Api = Baz1 ?:: Baz2 ?:: Baz3 ?:: ANil
}

object FooBar {
  type Api = Foo.Api +?+ Bar.Api
}

object FooBaz {
  type Api = Foo.Api +?+ Baz.Api
}

object BarBaz {
  type Api = Bar.Api +?+ Baz.Api
}

object FooBarBaz {
  type Api = Foo.Api +?+ Bar.Api +?+ Baz.Api
}

class CoreSpec extends FlatSpec {
  "IsPartOf implicit search" should "work correctly" in {
    assertCompiles("IsPartOf[Foo1, Foo.Api]")
    assertCompiles("IsPartOf[Foo2, Foo.Api]")
    assertCompiles("IsPartOf[Foo3, Foo.Api]")

    assertCompiles("IsPartOf[Foo1, FooBar.Api]")
    assertCompiles("IsPartOf[Foo2, FooBar.Api]")
    assertCompiles("IsPartOf[Foo3, FooBar.Api]")

    assertCompiles("IsPartOf[Bar1, FooBar.Api]")
    assertCompiles("IsPartOf[Bar2, FooBar.Api]")
    assertCompiles("IsPartOf[Bar3, FooBar.Api]")

    assertCompiles("IsPartOf[Foo1, FooBarBaz.Api]")
    assertCompiles("IsPartOf[Foo2, FooBarBaz.Api]")
    assertCompiles("IsPartOf[Foo3, FooBarBaz.Api]")
    assertCompiles("IsPartOf[Bar1, FooBarBaz.Api]")
    assertCompiles("IsPartOf[Bar2, FooBarBaz.Api]")
    assertCompiles("IsPartOf[Bar3, FooBarBaz.Api]")
    assertCompiles("IsPartOf[Baz1, FooBarBaz.Api]")
    assertCompiles("IsPartOf[Baz2, FooBarBaz.Api]")
    assertCompiles("IsPartOf[Baz3, FooBarBaz.Api]")

    assertDoesNotCompile("IsPartOf[Bar1, Foo.Api]")
  }

  "ReturnsOnly implicit search" should "work correctly" in {
    assertCompiles("ReturnsOnly[Foo.Api, String]")
    assertCompiles("ReturnsOnly[FooBaz.Api, String]")

    assertDoesNotCompile("ReturnsOnly[Foo.Api, Int]")
    assertDoesNotCompile("ReturnsOnly[Bar.Api, Int]")
  }

  "TakesOnly implicit search" should "work correctly" in {
    assertCompiles("TakesOnly[Bar.Api, String]")
    assertCompiles("TakesOnly[Foo.Api, Int]")
    assertCompiles("TakesOnly[BarBaz.Api, String]")

    assertDoesNotCompile("TakesOnly[Bar.Api, Int]")
    assertDoesNotCompile("TakesOnly[Foo.Api, String]")
  }
}
