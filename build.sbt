import BuildHelper._

val AmmoniteVersion = "2.1.4-8-5d0c097"

inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://zio.github.io/zio-prelude/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "jdegoes",
        "John De Goes",
        "john@degoes.net",
        url("http://degoes.net")
      )
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
    scmInfo := Some(
      ScmInfo(url("https://github.com/zio/zio-prelude/"), "scm:git:git@github.com:zio/zio-prelude.git")
    )
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

val zioVersion = "1.0.0"
resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies ++= Seq(
  "dev.zio" %% "zio"          % zioVersion,
  "dev.zio" %% "zio-test"     % zioVersion,
  "dev.zio" %% "zio-test-sbt" % zioVersion,
  "com.lihaoyi" %% "ammonite" % AmmoniteVersion % "test" cross CrossVersion.full
)

// Ammonite repl launch command (Run the test:run task in sbt)
sourceGenerators in Test += Def.task {
  val file = (sourceManaged in Test).value / "amm.scala"
  IO.write(file, """object amm extends App { ammonite.Main().run() }""")
  Seq(file)
}.taskValue


testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val root =
  (project in file("."))
    .settings(
      stdSettings("zio-prelude")
    )
    .settings(buildInfoSettings("zio.prelude"))
    .settings(scalacOptions in (Compile, console) ~= { _.filterNot(Set("-Xfatal-warnings")) })
    .enablePlugins(BuildInfoPlugin)

lazy val benchmarks = project
  .in(file("zio-prelude-benchmarks"))
  .settings(
    skip.in(publish) := true,
    moduleName := "zio-prelude-benchmarks",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio"       % zioVersion,
      "org.typelevel" %% "cats-core" % "2.2.0-M2"
    )
  )
  .dependsOn(root)
  .enablePlugins(JmhPlugin)

lazy val docs = project
  .in(file("zio-prelude-docs"))
  .settings(
    skip.in(publish) := true,
    moduleName := "zio-prelude-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion
    ),
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(root),
    target in (ScalaUnidoc, unidoc) := (baseDirectory in LocalRootProject).value / "website" / "static" / "api",
    cleanFiles += (target in (ScalaUnidoc, unidoc)).value,
    docusaurusCreateSite := docusaurusCreateSite.dependsOn(unidoc in Compile).value,
    docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(unidoc in Compile).value
  )
  .dependsOn(root)
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)
