// *****************************************************************************
// Projects
// *****************************************************************************
import microsites.ExtraMdFileConfig

lazy val IT = config("it") extend Test

val filterDependencies: ModuleID => ModuleID = { dependency =>
  library.exclusions.foldRight(dependency) { (rule, module) =>
    module.excludeAll(rule)
  }
}

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val gstorage = (project in file("gstorage"))
  .enablePlugins(BuildInfoPlugin)
  .configs(IntegrationTest)
  .settings(inConfig(IT)(Defaults.testSettings))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "google4s-gstorage",
    libraryDependencies ++= Seq(
      compilerPlugin(library.kindProjector),
      // compile time dependencies
      library.gCloudStorage % Compile
    )
  )
  .dependsOn(core % "compile->compile;test->test;it->it")

lazy val gkms = (project in file("gkms"))
  .enablePlugins(BuildInfoPlugin)
  .configs(IntegrationTest)
  .settings(inConfig(IT)(Defaults.testSettings))
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    name := "google4s-gkms",
    libraryDependencies ++= Seq(
      // compiler plugins
      compilerPlugin(library.kindProjector),
      // compile time dependencies
      library.gCloudKMS   % Compile,
      library.terminology % Compile
    )
  )
  .dependsOn(core % "compile->compile;test->test;it->it")

lazy val gpubsub = (project in file("gpubsub"))
  .enablePlugins(BuildInfoPlugin)
  .configs(IntegrationTest)
  .settings(commonSettings: _*)
  .settings(inConfig(IT)(Defaults.testSettings))
  .settings(publishSettings: _*)
  .settings(
    name := "google4s-gpubsub",
    libraryDependencies ++= Seq(
      // compile time dependencies
      library.gPubSub    % Compile,
      library.akkaStream % Compile
    )
  )
  .dependsOn(core % "compile->compile;test->test;it->it")

lazy val core = (project in file("core"))
  .configs(IntegrationTest)
  .settings(commonSettings: _*)
  .settings(inConfig(IT)(Defaults.testSettings))
  .settings(
    name := "google4s-core",
    skip in publish := true,
    libraryDependencies ++= Seq(
      compilerPlugin(library.kindProjector),
      library.akkaStreamTest % "it,test",
      library.akkaTest       % "it,test",
      library.scalaTest      % "it,test",
      library.scalaCheck     % "it,test",
      library.gCloudNio      % "it,test",
      // compile time dependencies
      library.gApiCommon % Compile,
      library.gApi       % Compile,
      library.catsCore   % Compile
    )
  )

lazy val google4s = (project in file("."))
  .settings(publishSettings: _*)
  .settings(
    skip in publish := true,
    name := "google4s"
  )
  .aggregate(core, gstorage, gkms, gpubsub)

  lazy val microsite = (project in file ("site"))
  .enablePlugins(MicrositesPlugin)
  .dependsOn(gpubsub, gkms, gstorage)
  .settings(
    skip in publish := true,
    micrositeFooterText := Some(
      """
        |<p>&copy; 2019 <a href="https://github.com/toknapp/google4s">Ivan Morozov, Tokn GmbH</a></p>
        |""".stripMargin
    ),
    micrositeName := "Google4s",
    micrositeDescription := "A lean, functional library for Google Cloud Services in Scala",
    micrositeAuthor := "Ivan Morozov",
    micrositeOrganizationHomepage := "https://toknapp.github.io/google4s/",
    micrositeGitHostingUrl := "https://github.com/toknapp/google4s",
    micrositeGithubOwner := "upvest",
    micrositeGithubRepo := "google4s",
    micrositeTwitterCreator := "@allquantor",

    scalacOptions in Tut ~= (_ filterNot Set(
    "-Xfatal-warnings",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:imports",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates",
    "-Ywarn-numeric-widen",
    "-Ywarn-dead-code",
    "-Xlint:-missing-interpolator,_").contains),
  )

// *****************************************************************************
// Dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka          = "2.5.19"
      val cats          = "1.4.0"
      val circe         = "0.10.1"
      val gCloudStorage = "1.53.0"
      val gCloudKMS     = "0.74.0-beta"
      val gCloudNio     = "0.72.0-alpha"
      val gPubSub       = "1.53.0"
      val gApiCommon    = "1.7.0"
      val gApi          = "1.35.1"
      val terminology   = "0.7.0"
      val scalaTest     = "3.0.5"
      val scalaCheck    = "1.13.5" // https://github.com/typelevel/cats/blob/910df7807a890f0f2ba26a750ab51e1072bf37ee/build.sbt#L140
      val kindProjector = "0.9.9"
    }

    val akkaStream     = "com.typesafe.akka" %% "akka-stream"         % Version.akka
    val akkaStreamTest = "com.typesafe.akka" %% "akka-stream-testkit" % Version.akka
    val akkaTest       = "com.typesafe.akka" %% "akka-testkit"        % Version.akka
    val catsCore       = "org.typelevel"     %% "cats-core"           % Version.cats
    val gPubSub        = "com.google.cloud"  % "google-cloud-pubsub"  % Version.gPubSub
    val gCloudStorage  = "com.google.cloud"  % "google-cloud-storage" % Version.gCloudStorage
    val gCloudKMS      = "com.google.cloud"  % "google-cloud-kms"     % Version.gCloudKMS
    val gCloudNio      = "com.google.cloud"  % "google-cloud-nio"     % Version.gCloudNio
    val gApi           = "com.google.api"    % "gax"                  % Version.gApi
    val gApiCommon     = "com.google.api"    % "api-common"           % Version.gApiCommon
    val terminology    = "co.upvest"         %% "terminology"         % Version.terminology
    val kindProjector  = "org.spire-math"    %% "kind-projector"      % Version.kindProjector
    val scalaTest      = "org.scalatest"     %% "scalatest"           % Version.scalaTest
    val scalaCheck     = "org.scalacheck"    %% "scalacheck"          % Version.scalaCheck

    // All exclusions that should be applied to every module.
    val exclusions = Seq(
      ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12"),
      ExclusionRule(organization = "log4j", name = "log4j"),
      ExclusionRule(organization = "ch.qos.logback", name = "logback-classic")
    )
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val commonSettings = Seq(
  scalaVersion := "2.12.8",
  organization := "co.upvest",
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding",
    "UTF-8",
    "-Xfatal-warnings",
    "-Ywarn-unused-import",
    "-Yno-adapted-args",
    "-Ywarn-inaccessible",
    "-Ywarn-dead-code",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-unused-import",
    "-Ypartial-unification",
    "-Xmacro-settings:materialize-derivations"
  ),
  scalacOptions in (Compile, console) ~= { _ filterNot (_ == "-Ywarn-unused-import") },
  scalacOptions in Test ~= { _ filterNot (_ == "-Ywarn-dead-code") },
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  cancelable in Global := true,
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot),
  fork in Global := true
)

// *****************************************************************************
// Release settings
// *****************************************************************************

organization in ThisBuild := "co.upvest"

lazy val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}

lazy val credentialSettings = Seq(
  credentials ++= (for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
)

import microsites.ExtraMdFileConfig
import sbtrelease.ReleaseStateTransformations._
import sbtrelease.Version

def checkoutBranch(branch: String): ReleaseStep = { st: State =>
  val Some(vcs) = Project.extract(st).get(releaseVcs)
  val 0         = vcs.cmd("checkout", branch).!
  st
}

publishArtifact in google4s := false

lazy val releaseSettings = Seq(
  releaseTagName := tagName.value,
  useGpg := true,
  pgpReadOnly := true,
  usePgpKeyHex("5C90DFE428FC2B33"),
  pgpPassphrase := Some(Array.empty),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseVcsSign := true,
  releaseVersionBump := Version.Bump.Minor,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),
  releaseCommitMessage := "Bumping version",
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishConfiguration := publishConfiguration.value.withOverwrite(isSnapshot.value),
  PgpKeys.publishSignedConfiguration := PgpKeys.publishSignedConfiguration.value.withOverwrite(isSnapshot.value),
  publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(isSnapshot.value),
  PgpKeys.publishLocalSignedConfiguration := PgpKeys.publishLocalSignedConfiguration.value
    .withOverwrite(isSnapshot.value),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    setReleaseVersion,
    tagRelease,
    publishArtifacts,
    releaseStepCommand("sonatypeReleaseAll"),
    checkoutBranch("develop"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/toknapp/google4s")),
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo := Some(ScmInfo(url("https://github.com/toknapp/google4s"), "scm:git@github.com:toknapp/google4s.git")),
  pomExtra := (
    <developers>
      <developer>
        <id>allquantor</id>
        <name>Ivan Morozov</name>
        <url>https://github.com/allquantor/</url>
      </developer>
      <developer>
        <id>rootmos</id>
        <name>Gustav Behm</name>
        <url>https://github.com/rootmos/</url>
      </developer>
    </developers>
  )
) ++ credentialSettings ++ releaseSettings
