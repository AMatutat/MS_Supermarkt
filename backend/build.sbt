
name := "smartmarkt"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.2"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayAkkaHttp2Support, AkkaGrpcPlugin)

import play.grpc.gen.scaladsl.PlayScalaServerCodeGenerator
akkaGrpcExtraGenerators += PlayScalaServerCodeGenerator

// ALPN agent
enablePlugins(JavaAgent)
javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.10" % "runtime;test"

libraryDependencies += "com.typesafe.akka" %% "akka-discovery" % "2.6.5"
libraryDependencies += "com.lightbend.play" %% "play-grpc-runtime" % "0.8.2"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies +="postgresql" % "postgresql" % "9.1-901.jdbc4"
libraryDependencies += jdbc

libraryDependencies += "com.rabbitmq" % "amqp-client" % "5.9.0"

PlayKeys.devSettings := Seq("play.server.http.port" -> "8080")