
lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion = "2.5.24"


lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "1.0.3",
      "com.typesafe.akka" %% "akka-discovery" % "2.5.23",
      "com.lightbend.akka.management" %% "akka-management" % "1.0.3",
      "javax.inject" % "javax.inject" % "1",
      "com.google.inject" % "guice" % "4.0"
    )
  )
  .settings(
    name := "sharding",
    organization := "com.example",
    scalaVersion := "2.12.8"
  )