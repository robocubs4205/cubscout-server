import java.nio.file.Files

import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline
import org.scalajs.sbtplugin.cross.CrossType
import sbt.Keys.{organization, scalacOptions}

import scala.collection.JavaConverters._

version := "1.0-SNAPSHOT"

inThisBuild(Seq(
  scalaVersion := "2.12.0",
  name := """CubScout""",
  organization := "com.robocubs4205",
  version := "1.0-SNAPSHOT",
  scalacOptions += "-feature",
  resolvers += "Atlassian" at "https://maven.atlassian.com/content/repositories/atlassian-public/"
))

lazy val apiServer = (project in file("api-server")).settings(
  libraryDependencies ++= Seq(
    guice,
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test,
    "com.typesafe.play" %% "play-slick" % "3.0.0",
    "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
    evolutions
  ),
  libraryDependencies += "com.h2database" % "h2" % "1.4.194",
  libraryDependencies ++= Seq(
    "com.nulab-inc" %% "scala-oauth2-core" % "1.3.0",
    "com.nulab-inc" %% "play2-oauth2-provider" % "1.3.0"
  ),
  //libraryDependencies += "com.mohiva" %% "play-silhouette" % "5.0.0",

  libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % "3.1"

  //Adds additional packages into Twirl
  //TwirlKeys.templateImports += "com.robocubs4205.cubscout.controllers._",

  //Adds additional packages into conf/routes
  //play.sbt.routes.RoutesKeys.routesImport += "com.robocubs4205.binders._",
).dependsOn(commonJVM).enablePlugins(PlayScala)

lazy val client = (project in file("client")).settings(
  scalaJSUseMainModuleInitializer := true,
  webpackConfigFile := Some(baseDirectory.value / "webpack.config.js"),
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  libraryDependencies += "com.github.japgolly.scalajs-react" %%% "core" % "1.1.0",
  libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "1.1.0",
  libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-scalaz72" % "1.1.0",
  libraryDependencies += "com.olvind" %%% "scalajs-react-components" % "0.7.0",
  libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.4" % "test",
  npmDependencies in Compile ++= Seq(
    "react" -> "15.6.1",
    "react-dom" -> "15.6.1",
    "material-ui" -> "0.19.0"
  ),
  scalaSource in Compile := baseDirectory.value / "app",
  scalaSource in Test := baseDirectory.value / "test",
  javaSource in Compile := baseDirectory.value / "app",
  javaSource in Test := baseDirectory.value / "test",
  resourceDirectory in Compile := baseDirectory.value / "resources",
  resourceDirectory in Test := baseDirectory.value / "testResources",
  resourceDirectory in Assets := baseDirectory.value / "resources",
  resourceDirectory in TestAssets := baseDirectory.value / "testResources"
).enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, ScalaJSWeb).dependsOn(commonJS)

lazy val clientTestServer = (project in file("client-test-server")).settings(
  libraryDependencies += guice,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline)
).dependsOn(client).enablePlugins(PlayScala, WebScalaJSBundlerPlugin)

lazy val commonJVM = common.jvm

lazy val commonJS = common.js

lazy val common = (crossProject.crossType(CrossType.Pure) in file("common")).settings(
  libraryDependencies += "io.lemonlabs" %% "scala-uri" % "0.4.16",
  libraryDependencies += "com.typesafe.play" %% "play" % "2.6.2",
  libraryDependencies += "commons-codec" % "commons-codec" % "1.10"
).jvmSettings(
  libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
).jsSettings(
  libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.2"
)

onLoad in Global := (Command.process("project apiServer", _: State)) compose (onLoad in Global).value
