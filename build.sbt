// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Pixel Editor project build.sbt file

val jvmSettings = JvmSettings("1.8", "1.8", "1.8")

// JVM settings can be verified using the following command:
// javap -verbose -cp pixeleditor.jar bdzimmer.pixeleditor.view.Main
// Major version will be 52 for Java 1.8.

lazy val root = (project in file("."))
  .settings(
    name := "Pixel Editor",
    version := "2015.12.24",
    organization := "bdzimmer",
    scalaVersion := "2.10.6",
    mainClass in (Compile, run) := Some("bdzimmer.pixeleditor.view.Main"),

    javacOptions ++= Seq("-source", jvmSettings.javacSource, "-target", jvmSettings.javacTarget),
    scalacOptions ++= Seq(s"-target:jvm-1.7"),

    libraryDependencies ++= Seq(
      "commons-io"     % "commons-io" % "2.4",
      "org.scalatest" %% "scalatest"  % "2.2.4" % "it,test"
    ))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .dependsOn(utilscala)

lazy val utilscala = RootProject(file("../util-scala"))

// import into Eclipse as a Scala project
EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala

// use Java 1.8 in Eclipse
EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18)

// use the version of Scala from sbt in Eclipse
EclipseKeys.withBundledScalaContainers := false
