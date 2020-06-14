addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.2")
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.11.0")
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "1.0.0-M1")
resolvers += Resolver.bintrayRepo("playframework", "maven")
libraryDependencies += "com.lightbend.play" %% "play-grpc-generators" % "0.8.2"
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "0.8.4")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.5")
