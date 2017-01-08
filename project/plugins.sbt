// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/"

// Typesafe snapshots
resolvers += "Typesafe Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

// The Typesafe repository 
//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"


// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.10")
	//addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")

// Eclipse
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.github.ddispaltro" % "sbt-reactjs" % "0.5.2")


