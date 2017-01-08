name := "youtubeAgent"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

//play.Project.playScalaSettings
//lazy val root = (project in file(".")).enablePlugins(PlayScala)
lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

libraryDependencies += "org.json4s" % "json4s-native_2.11" % "3.3.0"

libraryDependencies += "org.json4s" % "json4s-jackson_2.11" % "3.3.0"

libraryDependencies ++= Seq(
  cache,
  ws,
  specs2 % Test,
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "react" % "0.14.0",
  "org.webjars" % "marked" % "0.3.2",
  "org.webjars" % "jquery" % "2.1.4"
)

libraryDependencies ++= Seq(
	"com.google.api-client" % "google-api-client" % "1.22.0",
	"com.google.api-client" % "google-api-client-appengine" % "1.7.0-beta",
	"com.google.oauth-client" % "google-oauth-client-jetty" % "1.11.0-beta",
	"com.google.oauth-client" % "google-oauth-client-java6" % "1.11.0-beta",
	"com.google.apis" % "google-api-services-youtube" % "v3-rev179-1.22.0"
)

libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.49"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

disablePlugins(PlayLayoutPlugin)


fork in run := true