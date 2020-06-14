name := "SmartMarkt"
version := "0.1"

enablePlugins(ScalaJSPlugin)
scalaVersion := "2.12.8"                  
scalaJSUseMainModuleInitializer := true   
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.7"
libraryDependencies += "org.querki" %%% "jquery-facade" % "1.2"
libraryDependencies += "org.scalaj" % "scalaj-http_2.11" % "2.3.0"
//libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.9.5"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0-M5"

skip in packageJSDependencies := false
jsDependencies += "org.webjars" % "jquery" % "2.2.1" / "jquery.js" minified "jquery.min.js"
