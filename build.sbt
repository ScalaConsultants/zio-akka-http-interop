import ReleaseTransformations._
import ReleasePlugin.autoImport._

val zioVersion      = "1.0.10"
val akkaVersion     = "2.6.15"
val akkaHttpVersion = "10.2.8"

val compilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture",
  "-Xfatal-warnings",
  "-Ywarn-unused-import"
)

val publishSettings = Seq(
  releaseUseGlobalVersion := true,
  releaseVersionFile := file(".") / "version.sbt",
  releaseCommitMessage := s"Set version to ${version.value}",
  releaseIgnoreUntrackedFiles := true,
  releaseCrossBuild := true,
  homepage := Some(url("https://github.com/ScalaConsultants/zio-akka-http-interop")),
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/ScalaConsultants/zio-akka-http-interop"),
      "scm:git:git@github.com:ScalaConsultants/zio-akka-http-interop.git"
    )
  ),
  developers := List(
    Developer(
      id = "vpavkin",
      name = "Vladimir Pavkin",
      email = "vpavkin@gmail.com",
      url = url("https://pavkin.ru")
    )
  ),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

val root = (project in file("."))
  .settings(
    organization := "io.scalac",
    name := "zio-akka-http-interop",
    scalaVersion := "2.13.6",
    crossScalaVersions := Seq("2.12.14", "2.13.6"),
    scalacOptions ++= {
      if (priorTo2_13(scalaVersion.value)) compilerOptions
      else
        compilerOptions.flatMap {
          case "-Ywarn-unused-import" => Seq("-Ywarn-unused:imports")
          case "-Xfuture"             => Nil
          case other                  => Seq(other)
        }
    },
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor"          % akkaVersion     % Provided,
      "com.typesafe.akka" %% "akka-stream"         % akkaVersion     % Provided,
      "com.typesafe.akka" %% "akka-http"           % akkaHttpVersion % Provided,
      "dev.zio"           %% "zio"                 % zioVersion      % Provided,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion % Test,
      "dev.zio"           %% "zio-test-sbt"        % zioVersion      % Test
    )
  )
  .settings(publishSettings: _*)

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }
