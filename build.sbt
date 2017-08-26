import org.scalajs.sbtplugin.cross.CrossType
import sbt.Keys.{organization, scalacOptions}

version := "1.0-SNAPSHOT"

inThisBuild(Seq(
  scalaVersion := "2.12.2",
  name := """CubScout""",
  organization := "com.robocubs4205",
  version := "1.0-SNAPSHOT",
  scalacOptions += "-feature",
  resolvers += "Atlassian" at "https://maven.atlassian.com/content/repositories/atlassian-public/"
))

lazy val server = (project in file("server")).settings(
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


  // Adds additional packages into Twirl
  //TwirlKeys.templateImports += "com.robocubs4205.cubscout.controllers._",

  // Adds additional packages into conf/routes
  // play.sbt.routes.RoutesKeys.routesImport += "com.robocubs4205.binders._",
).dependsOn(commonJVM).enablePlugins(PlayScala)

lazy val client = (project in file("client")).settings(
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1"
  ),
  scalaSource in Compile := baseDirectory.value / "app",
  scalaSource in Test := baseDirectory.value / "test",
  javaSource in Compile := baseDirectory.value / "app",
  javaSource in Test := baseDirectory.value / "test"
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(commonJS)

lazy val commonJVM = common.jvm

lazy val commonJS = common.js

lazy val common = (crossProject.crossType(CrossType.Pure) in file("common")).settings(
  libraryDependencies += "io.lemonlabs" %% "scala-uri" % "0.4.16",
  libraryDependencies += "com.typesafe.play" %% "play" % "2.6.2",
  libraryDependencies += "commons-codec" % "commons-codec" % "1.10"
).jsConfigure(_ enablePlugins ScalaJSWeb).jvmSettings(
  libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
)

onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
