ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.0"

lazy val fpsCode = (project in file("."))
  .settings(
    name := "fps-code",
    scalacOptions ++= Seq(
      "-Xkind-projector:underscores"
    ),
    idePackagePrefix := Some("ge.zgharbi.study.fps"),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.1" % Test,
    ),
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / excludeLintKeys += idePackagePrefix
