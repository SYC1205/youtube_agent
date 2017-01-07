name := "youtubeAgent"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

//play.Project.playScalaSettings
lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

disablePlugins(PlayLayoutPlugin)