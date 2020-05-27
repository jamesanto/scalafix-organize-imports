
inThisBuild(
  List(
    organization := "com.github.liancheng",
    homepage := Some(url("https://github.com/liancheng/scalafix-organize-imports")),
    licenses := List("MIT" -> url("https://opensource.org/licenses/MIT")),
    developers := List(
      Developer(
        "liancheng",
        "Cheng Lian",
        "lian.cs.zju@gmail.com",
        url("https://github.com/liancheng")
      )
    ),
    scalaVersion := "2.13.2",
    scalacOptions ++= List(
      "-Yrangepos",
      "-P:semanticdb:synthetics:on"
    ),
    conflictManager := ConflictManager.strict,
    dependencyOverrides ++= List(
      "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "com.lihaoyi" %% "sourcecode" % "0.2.1"
    ),
    addCompilerPlugin("org.scalameta" % "semanticdb-scalac_2.13.2" % "4.3.10"),
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.3.0",
    // Super shell output often messes up Scalafix test output.
    useSuperShell := false
  )
)

skip in publish := true

lazy val rules = project
  .settings(
    moduleName := "organize-imports",
    dependencyOverrides += "com.lihaoyi" %% "sourcecode" % "0.2.1",
    libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % "0.9.15.2-SNAPSHOT",
    scalacOptions ++= List("-Ywarn-unused"),
    version := "0.3.1.2-SNAPSHOT",
    credentials += Credentials("Sonatype Nexus Repository Manager", "127.0.0.1", "admin", "admin"),
    publishTo := Some("Sonatype Nexus Repository Manager" at "http://127.0.0.1:8081/repository/maven-snapshots")
  )

lazy val shared = project.settings(skip in publish := true)

lazy val input = project
  .dependsOn(shared)
  .settings(skip in publish := true)

lazy val output = project
  .dependsOn(shared)
  .settings(skip in publish := true)

lazy val inputUnusedImports = project
  .dependsOn(shared)
  .settings(
    skip in publish := true,
    scalacOptions ++= List("-Ywarn-unused")
  )

lazy val tests = project
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
  .settings(
    skip in publish := true,
    scalacOptions ++= List("-Ywarn-unused"),
    libraryDependencies +=
      "ch.epfl.scala" % "scalafix-testkit" % "0.9.15.2-SNAPSHOT" % Test cross CrossVersion.full,
    (compile in Compile) := (compile in Compile)
      .dependsOn(
        compile in (input, Compile),
        compile in (inputUnusedImports, Compile)
      )
      .value,
    scalafixTestkitOutputSourceDirectories := sourceDirectories.in(output, Compile).value,
    scalafixTestkitInputSourceDirectories := (
      sourceDirectories.in(input, Compile).value ++
        sourceDirectories.in(inputUnusedImports, Compile).value
    ),
    scalafixTestkitInputClasspath := (
      fullClasspath.in(input, Compile).value ++
        fullClasspath.in(inputUnusedImports, Compile).value
    ).distinct
  )
