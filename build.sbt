enablePlugins(ScalaJSPlugin)
scalaVersion := "2.12.7"

organization := "ph.samson"
name := "Asana Assignee Filter"
version := "0.1.0-SNAPSHOT"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6"
scalaJSUseMainModuleInitializer := true
