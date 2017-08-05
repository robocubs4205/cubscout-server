name := """CubScoutPlay"""
organization := "com.robocubs4205"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"
scalacOptions += "-feature"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
libraryDependencies += "com.typesafe.play" %% "play-slick" %  "3.0.0-M5"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0-M5"
libraryDependencies += "com.h2database" % "h2" % "1.4.194"
libraryDependencies += "io.lemonlabs" %% "scala-uri" % "0.4.16"
libraryDependencies += evolutions

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.robocubs4205.cubscout.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.robocubs4205.binders._"
