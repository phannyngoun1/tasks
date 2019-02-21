
import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

lazy val commonSettings = Seq(
  scalaVersion := Common.versions.scala,
  organization := Common.Setting.organization,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions ++= Seq(
    "-encoding", "UTF-8", // yes, this is 2 args
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-numeric-widen"
  )
)

// a special crossProject for configuring a JS/JVM/shared structure
lazy val shared =
  (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("shared"))
    .enablePlugins(ScalaJSPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.julienrf" %%% "play-json-derived-codecs" % "4.0.0"
        // logging lib that also works with ScalaJS
        , "biz.enef" %%% "slogging" % "0.6.0",
        "com.lihaoyi" %%% "autowire" % "0.2.6",
        "io.suzaku" %%% "boopickle" % "1.3.0"
      )
    )
    .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonJS = (project in file("common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    //persistLauncher := false,
    //persistLauncher in Test := false,
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % "1.1.1",
      "com.github.japgolly.scalajs-react" %%% "extra" % "1.1.1",
      "org.scala-js" %%% "scalajs-dom" % "0.9.3"
    )
  )


lazy val client = (project in file("client")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := true,
  scalaJSUseMainModuleInitializer in Test := false,

  scalaJSOptimizerOptions in fastOptJS ~= { _.withDisableOptimizer(true) },
  scalaJSOptimizerOptions in fullOptJS ~= { _.withDisableOptimizer(false) },
  skip in packageJSDependencies := false,
  libraryDependencies ++= Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % "1.1.1",
    "com.github.japgolly.scalajs-react" %%% "extra" % "1.1.1",
    "com.github.japgolly.scalacss" %%% "ext-react" % "0.5.5",
    "org.scala-js" %%% "scalajs-dom" % "0.9.3",
    "com.lihaoyi" %%% "scalatags" % "0.6.7",
    "io.suzaku" %%% "diode-core" % "1.1.3",
    "io.suzaku" %%% "diode-react" % "1.1.3",
    "fr.hmil" %%% "roshttp" % "2.0.2",
    // java.time supprot for ScalaJS
    "org.scala-js" %%% "scalajs-java-time" % "0.2.2",
    "com.typesafe.play" % "play-json-joda_2.12" % "2.6.0"

  ),
  npmDependencies in Compile ++= Seq(
    "react" -> "15.6.1",
    "react-dom" -> "15.6.1")

).enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(sharedJs)



lazy val common =  (project in file("modules/common"))
  .settings(Common.projectSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % Common.versions.akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % Common.versions.akkaVersion
    )
  )


lazy val workflow =  (project in file("modules/workflow"))
  .settings(Common.projectSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % Common.versions.akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % Common.versions.akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % Common.versions.akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % "2.5.19",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.4.0",
      "org.iq80.leveldb" % "leveldb" % "0.7",
      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

      "mysql" % "mysql-connector-java" % "5.1.42",
    )
  )
  .aggregate(common)
  .dependsOn(common)


lazy val server = (project in file("server"))
  .settings(Common.projectSettings)
  .settings(
    name := """my_tasks""",
    version := "1.0-SNAPSHOT",
    organization := "com.dream",
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,

    routesGenerator := InjectedRoutesGenerator,
    resolvers ++= Seq(
      "Atlassian Releases" at "https://maven.atlassian.com/public",
      "Maven 2" at "http://central.maven.org/maven2",
      "Clojars" at "http://clojars.org/repo"
    ),
    libraryDependencies ++= Seq(
      guice,
      "com.vmunier" %% "scalajs-scripts" % "1.1.2",
      "org.webjars" %% "webjars-play" % "2.7.0-1",
      "org.webjars" % "font-awesome" % "4.2.0",
      "org.webjars" % "jquery" % "3.2.1",
      ehcache,
      specs2 % Test,
    ),

    LessKeys.compress in Assets := true,
    includeFilter in(Assets, LessKeys.less) := "*.less",

    // to have routing also in ScalaJS
    // Create a map of versioned assets, replacing the empty versioned.js
    DigestKeys.indexPath := Some("javascripts/versioned.js"),
    // Assign the asset index to a global versioned var
    DigestKeys.indexWriter ~= { writer => index => s"var versioned = ${writer(index)};" }

  ).enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
    .dependsOn(sharedJvm, workflow )

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen { s: State => "project server" :: s }
