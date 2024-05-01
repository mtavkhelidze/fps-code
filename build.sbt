ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.2"

lazy val fpsCode = (project in file("."))
  .settings(
    name := "fps-code",
    scalacOptions ++= Seq(
      "-Ykind-projector:underscores"
    ),
    idePackagePrefix := Some("ge.zgharbi.study.fps"),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
    ),
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / excludeLintKeys += idePackagePrefix
