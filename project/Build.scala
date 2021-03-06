import sbt._
import Keys._
import xerial.sbt.Sonatype._
import SonatypeKeys._
import sbtrelease.ReleasePlugin._
import ReleaseKeys._
import com.typesafe.sbt.pgp.PgpKeys

object ShellPrompt {
  def currentBranch = Process(List("git", "rev-parse", "--abbrev-ref", "HEAD"))
    .lines_!.headOption.getOrElse("-")

  val buildShellPrompt = (state: State) => {
    val extracted = Project.extract(state)
    val currentVersion = (version in ThisBuild get extracted.structure.data).getOrElse("-")
    val currentProject = extracted.currentProject.id
    s"[$currentProject:$currentVersion][$currentBranch]\n> "
  }
}

object Build extends Build {
  import Deps._

  lazy val basicSettings = Seq(
    organization := "com.github.ancane",
    name         := "haldr",
    description  := "HAL builder for spray-json",
    scalaVersion := V.scala,
    crossScalaVersions := V.crossScala,
    scalacOptions := Seq(
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Xlog-reflective-calls"
    ),
    shellPrompt := ShellPrompt.buildShellPrompt
  )

  lazy val publishSettings = Seq(
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra := {
      <url>https://github.com/ancane/haldr</url>
      <licenses>
        <license>
          <name>MIT</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com:ancane/haldr</connection>
        <developerConnection>scm:git:git@github.com:ancane/haldr</developerConnection>
        <url>https://github.com/ancane/haldr</url>
      </scm>
      <developers>
        <developer>
          <id>ancane</id>
          <email>igor.shimko@gmail.com</email>
          <name>Igor Shymko</name>
          <url>https://github.com/ancane</url>
        </developer>
      </developers>
    }
  )

  lazy val haldr = Project("haldr", file("."))
    .settings(basicSettings: _*)
    .settings(sonatypeSettings: _*)
    .settings(releaseSettings ++ Seq(publishArtifactsAction := PgpKeys.publishSigned.value))
    .settings(publishSettings: _*)
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
    .settings(
    libraryDependencies ++= Seq(
      uriTempl8,
      sprayJson,
      sprayHttp,
      sprayRouting % "test",
      sprayTestkit % "test",
      specs2 % "test",
      akka % "provided"
    )
  )
}

object Deps {
  object V {
    val scala = "2.11.6"
    val spray = "1.3.3"
    val crossScala = Seq("2.10.5", "2.11.6")
  }

  val uriTempl8 = "no.arktekk"  %% "uri-template"   % "1.0.2"
  val sprayJson = "io.spray"    %% "spray-json"     % "1.3.1"

  val sprayHttp = "io.spray"    %% "spray-http"     % V.spray
  val sprayRouting = "io.spray" %% "spray-routing"  % V.spray
  val sprayTestkit = "io.spray" %% "spray-testkit"  % V.spray
  val akka      = "com.typesafe.akka" %% "akka-actor" % "2.3.9"
  val specs2    = "org.specs2"  %% "specs2"         % "2.3.13"
}
