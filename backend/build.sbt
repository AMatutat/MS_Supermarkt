
name := "smartmarkt"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.2"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayAkkaHttp2Support, AkkaGrpcPlugin)

import play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator
akkaGrpcExtraGenerators += PlayScalaServerCodeGenerator
libraryDependencies += "com.typesafe.akka" %% "akka-discovery" % "2.6.5"
libraryDependencies += "com.lightbend.play" %% "play-grpc-runtime" % "0.8.2"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
