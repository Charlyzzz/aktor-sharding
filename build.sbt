import com.lightbend.sbt.SbtAspectj.aspectjUseInstrumentedClasses
import sbtassembly.AssemblyPlugin._

lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion = "2.5.24"

lazy val aspectjLintConfig = {
  aspectjLintProperties in Aspectj += "invalidAbsoluteTypeName = ignore"
  aspectjLintProperties in Aspectj += "adviceDidNotMatch = ignore"
}

lazy val tracing = (project in file("tracing"))
    .enablePlugins(SbtAspectj)
    .disablePlugins(RevolverPlugin)
    .settings(buildSettings)
    .settings(aspectjLintConfig)
    .settings(
      aspectjInputs in Aspectj += (aspectjCompiledClasses in Aspectj).value,
      // add akka-actor as an aspectj input (find it in the update report)
      aspectjInputs in Aspectj ++= update.value.matching(moduleFilter(organization = "com.typesafe.akka", name = "akka-actor*")),
      // replace regular products with compiled aspects
      products in Compile := (products in Aspectj).value,
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion,
        "javax.inject" % "javax.inject" % "1",
        "com.google.inject" % "guice" % "4.0"
      )
    )


lazy val root = (project in file("."))
    .enablePlugins(SbtAspectj, AssemblyPlugin)
    .settings(buildSettings)
    .settings(assemblySettings: _*)
    .settings(
      assemblyMergeStrategy in assembly := {
        case PathList("META-INF", _*) => MergeStrategy.discard
        case "reference.conf" => MergeStrategy.concat
        case _ => MergeStrategy.first
      }
    )
    .settings(
      name := "sharding",
      organization := "com.example",
      scalaVersion := "2.12.8"
    )
    .settings(aspectjLintConfig)
    .settings(
      aspectjBinaries in Aspectj ++= (products in Compile in tracing).value,
      // replace the original akka-actor jar with the instrumented classes in runtime
      fullClasspath in Runtime := aspectjUseInstrumentedClasses(Runtime).value,
      // weave this project's classes
      aspectjInputs in Aspectj += (aspectjCompiledClasses in Aspectj).value,
      products in Compile := (products in Aspectj).value,
      products in Runtime := (products in Compile).value,
    )
    .settings(aggregate in assembly := false)
    .settings(Revolver.enableDebugging(port = 5050, suspend = false))
    .aggregate(tracing)
    .dependsOn(tracing)

