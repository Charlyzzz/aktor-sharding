import sbtassembly.AssemblyPlugin._

lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion = "2.5.24"

lazy val root = (project in file("."))
    .settings(buildSettings)
    .settings(
      inThisBuild(List(
        organization := "com.example",
        scalaVersion := "2.12.8"
      )),
      name := "aktors_sharding",
    )
    .aggregate(tracer, sharding)

lazy val tracer = (project in file("tracer"))
    .enablePlugins(SbtAspectj)
    .settings(buildSettings)
    .settings(
      aspectjLintProperties in Aspectj += "invalidAbsoluteTypeName = ignore",
      products in Compile := (products in Aspectj).value,
      aspectjInputs in Aspectj ++= update.value.matching(moduleFilter(organization = "com.typesafe.akka", name = "akka-actor*")),
      fullClasspath in Runtime := SbtAspectj.aspectjUseInstrumentedClasses(Runtime).value,
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion
      )
    )

lazy val sharding = (project in file("sharding"))
    .enablePlugins(SbtAspectj, AssemblyPlugin)
    .settings(buildSettings)
    .settings(assemblySettings: _*)
    .settings(
      // add the compiled aspects from tracer
      aspectjBinaries in Aspectj ++= (products in Compile in tracer).value,
      // weave this project's classes
      aspectjInputs in Aspectj += (aspectjCompiledClasses in Aspectj).value,
      products in Compile := (products in Aspectj).value,
      products in Runtime := (products in Compile).value,
      assemblyMergeStrategy in assembly := {
        case PathList("META-INF", _*) => MergeStrategy.discard
        case "reference.conf" => MergeStrategy.concat
        case _ => MergeStrategy.first
      }
    ).dependsOn(tracer)

