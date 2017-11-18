name := "gatling-recorder-standalone"

version := "3.0.0-SNAPSHOT"

scalaVersion := "2.12.4"
        
libraryDependencies ++= Seq(
  "io.gatling" % "gatling-core"    % "3.0.0-SNAPSHOT",
  "io.gatling" % "gatling-commons" % "3.0.0-SNAPSHOT",
  "io.gatling" % "gatling-http"    % "3.0.0-SNAPSHOT",

  "org.scala-lang.modules"    %% "scala-swing"      % "2.0.1"       , //scalaSwing
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.2"       , //jackson
  "org.json4s"                %% "json4s-jackson"   % "3.5.3"       , //json4sJackson
  "org.bouncycastle"           % "bcpkix-jdk15on"   % "1.58"        , //bouncycastle
  "io.netty"                   % "netty-codec-http" % "4.1.17.Final", //netty
  "com.typesafe.akka"         %% "akka-actor"       % "2.5.6"       , //akkaActor

  "com.typesafe.akka" %% "akka-testkit" % "2.5.6",
  "org.scalatest"     %% "scalatest"    % "3.0.4",
  "org.scalacheck"    %% "scalacheck"   % "1.13.5",
  "org.mockito"        % "mockito-core" % "2.11.0"
)
