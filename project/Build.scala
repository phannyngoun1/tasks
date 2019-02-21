import sbt.Keys._
import sbt.{Resolver, _}

object Common {


  object Setting {
    /** The name of your application */
    val name = "My task"

    /** The version of your application */
    val version = "1.1.5"

    val organization = "com.dream"
  }


  object versions {
    val scala = "2.12.6"
    val akkaVersion = "2.5.19"
    val pureConfig  = "0.9.0"

    val scalaDom = "0.9.3"
    val scalajsReact = "1.1.0"
    val scalaCSS = "0.5.3"
    val log4js = "1.4.10"
    val autowire = "0.2.6"
    val booPickle = "1.2.6"
    val diode = "1.1.2"
    val uTest = "0.4.7"

    val react = "15.6.1"
    val jQuery = "1.11.1"
    val bootstrap = "4.1.1"
    val chartjs = "2.1.3"
    val scalajsScripts = "1.0.0"
  }


  def projectSettings = Seq(
    scalaVersion := versions.scala,
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions ++= Seq(
      "-encoding", "UTF-8", // yes, this is 2 args
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-numeric-widen"
    ),
    resolvers ++= Seq(
      "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots")),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "javax.inject" % "javax.inject" % "1",
      "joda-time" % "joda-time" % "2.9.9",
      "org.joda" % "joda-convert" % "1.9.2",
      "com.google.inject" % "guice" % "4.1.0",
      "org.julienrf" %% "play-json-derived-codecs" % "4.0.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
      "org.typelevel" %% "cats-core" % "1.4.0",
      "com.beachape" %% "enumeratum" % "1.5.13",
      "com.beachape" %% "enumeratum-play-json" % "1.5.14",
      "com.github.mpilquist" %% "simulacrum" % "0.14.0",

      "com.github.pureconfig" %% "pureconfig" % versions.pureConfig

    ),
    scalacOptions in Test ++= Seq("-Yrangepos")
  )
}
