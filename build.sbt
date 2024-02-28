val zioVersion      = "2.0.0"
val akkaVersion     = "2.6.19"
val akkaHttpVersion = "10.2.9"

inThisBuild(
  List(
    organization := "io.scalac",
    homepage     := Some(url("https://github.com/ScalaConsultants/zio-akka-http-interop")),
    licenses     := List("MIT" -> url("https://opensource.org/licenses/MIT")),
    developers   := List(
      Developer(
        id = "jczuchnowski",
        name = "Jakub Czuchnowski",
        email = "jakub.czuchnowski@gmail.com",
        url = url("https://github.com/jczuchnowski")
      )
    )
  )
)

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

val root = (project in file("."))
  .settings(
    name               := "zio-akka-http-interop",
    scalaVersion       := "2.13.13",
    crossScalaVersions := Seq("2.12.16", "2.13.13"),
    scalacOptions ++= {
      if (priorTo2_13(scalaVersion.value)) compilerOptions
      else
        compilerOptions.flatMap {
          case "-Ywarn-unused-import" => Seq("-Ywarn-unused:imports")
          case "-Xfuture"             => Nil
          case other                  => Seq(other)
        }
    },
    testFrameworks     := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
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

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _                              => false
  }
