val scalaV = "2.12.8"
val v = "0.1"
val testV = "3.0.4"
val sttpV = "1.5.0"

val baseSetings = Seq(
  version := v,
  scalaVersion := scalaV,
  libraryDependencies ++= Seq(
    "org.scalactic" %% "scalactic" % testV % "test",
    "org.scalatest" %% "scalatest" % testV % "test"
  )
)

lazy val hapi_core = project
  .in(file("./core"))
  .settings(baseSetings: _*)
  .settings(name := "hapi")

lazy val hapi_sttp = project
  .in(file("./hapi_sttp"))
  .dependsOn(hapi_core)
  .settings(baseSetings: _*)
  .settings(
    name := "hapi-sttp",
    libraryDependencies += "com.softwaremill.sttp" %% "core" % sttpV
  )

lazy val root = project
  .in(file("."))
  .settings(baseSetings)
  .aggregate(hapi_core, hapi_sttp)
