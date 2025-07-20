import org.typelevel.sbt.tpolecat.*

ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "3.7.0"

// This disables fatal-warnings for local development. To enable it in CI set the `SBT_TPOLECAT_CI` environment variable in your pipeline.
// See https://github.com/typelevel/sbt-tpolecat/?tab=readme-ov-file#modes
ThisBuild / tpolecatDefaultOptionsMode := VerboseMode

ThisBuild / Compile / run / fork := true

val catsVersion   = "3.6.2"
val circeVersion  = "0.14.14"
val fs2Version    = "3.12.0"
val http4sVersion = "0.23.30"
val scraperVersion = "3.2.0"

val logbackVersion = "1.5.18"

lazy val `scala-3-playground` = (project in file(".")).settings(
  name := "cats-effect-3-quick-start-bloop",
  libraryDependencies ++= Seq(
    "org.typelevel"    %% "cats-effect"          % catsVersion,
    "org.typelevel"    %% "cats-effect-kernel"   % catsVersion,
    "org.typelevel"    %% "cats-effect-std"      % catsVersion,
    "io.circe"         %% "circe-generic"        % circeVersion,
    "io.circe"         %% "circe-generic-extras" % "0.14.5-RC1",
    "io.circe"         %% "circe-parser"         % circeVersion,
    "co.fs2"           %% "fs2-core"             % fs2Version,
    "co.fs2"           %% "fs2-io"               % fs2Version,
    "org.http4s"       %% "http4s-circe"         % http4sVersion,
    "org.http4s"       %% "http4s-dsl"           % http4sVersion,
    "org.http4s"       %% "http4s-ember-client"  % http4sVersion,
    "org.http4s"       %% "http4s-ember-server"  % http4sVersion,
    "net.ruippeixotog" %% "scala-scraper"        % scraperVersion,
    // test dependencies
    "org.scalatest" %% "scalatest"                     % "3.2.19" % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.6.0"  % Test
  ).map(_.withSources().withJavadoc()) ++ Seq(
    "ch.qos.logback" % "logback-classic" % logbackVersion
  )
)
