name := "matching"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.6"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-language:_", 
  "-Ypartial-unification", 
  "-Xfatal-warnings",
  "-P:bm4:no-filtering:y",
  "-P:bm4:no-map-id:y",
  "-P:bm4:no-tupling:y"
)

val zioV = "0.2+" // "0.2.1+0-658a986f+20180824-2042-SNAPSHOT"
val scalazV = "7.2.26"
val catsV = "1.2.0"
val catsEffectV = "1.0.0-RC3"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsV withJavadoc() withSources(),
  "org.typelevel" %% "cats-effect" % catsEffectV withJavadoc() withSources(),
  "org.scalaz" %% "scalaz-core" % scalazV withJavadoc() withSources(),
  "org.scalaz" %% "scalaz-zio" % zioV withJavadoc() withSources(),
  "com.github.mpilquist" %% "simulacrum" % "0.13.0" withJavadoc() withSources(),
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7") 
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

fork in (Test, run) := true
trapExit := false
mainClass in (Compile, run) := Some("matching.SampleApp")
