// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Pixel Editor project build.sbt file

val whichJvmSettings = sys.props.getOrElse("jvm", default = "7")
val jvmSettings = whichJvmSettings match {
  case "6" => JvmSettings("1.6", "1.6", "1.6")
  case _ => JvmSettings("1.7", "1.7", "1.7")
}

// JVM settings can be verified using the following command:
// javap -verbose -cp pixeleditor.jar bdzimmer.pixeleditor.view.Main
// major version will be 50 for Java 1.6 and 51 for Java 1.7.

lazy val root = (project in file("."))
  .settings(
    name := "Pixel Editor",
    version := "2015.12.24",
    organization := "bdzimmer",
    scalaVersion := "2.10.6",
    mainClass in (Compile, run) := Some("bdzimmer.pixeleditor.view.Main"),

    javacOptions ++= Seq("-source", jvmSettings.javacSource, "-target", jvmSettings.javacTarget),
    scalacOptions ++= Seq(s"-target:jvm-${jvmSettings.scalacTarget}"),

    libraryDependencies ++= Seq(
      "commons-io" % "commons-io" % "2.4",
      "org.scalatest" %% "scalatest" % "2.2.4" % "it,test"
    ))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .dependsOn(utilscala)

lazy val utilscala = RootProject(file("../util-scala"))

// import into Eclipse as a Scala project
EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala

// use Java 1.7 in Eclipse
EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE17)

// use the version of Scala from sbt in Eclipse
EclipseKeys.withBundledScalaContainers := false
