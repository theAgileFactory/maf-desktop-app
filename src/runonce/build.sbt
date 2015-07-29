name := "maf-desktop-app"

version := "dist"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean exclude("org.avaje.ebeanorm", "avaje-ebeanorm") exclude("org.avaje.ebeanorm", "avaje-ebeanorm-agent"),
  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "3.2.2" exclude("javax.persistence", "persistence-api"),
  "org.avaje.ebeanorm" % "avaje-ebeanorm-agent" % "3.2.1" exclude("javax.persistence", "persistence-api"),
  cache,
  javaWs
)