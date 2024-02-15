ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

lazy val fpsCode = (project in file("."))
  .settings(
    name := "fps-code",
    idePackagePrefix := Some("ge.zgharbi.study.fps"),
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / excludeLintKeys += idePackagePrefix
