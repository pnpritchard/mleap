import sbt._

object Dependencies {
  val sparkVersion = "1.6.3"

  lazy val baseDependencies = Seq("org.scalatest" %% "scalatest" % "3.0.0" % "test")

  lazy val sparkLocalDependencies = Seq("org.apache.spark" %% "spark-mllib" % sparkVersion)
  lazy val sparkDependencies = Seq("org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-mllib" % sparkVersion,
    "org.apache.spark" %% "spark-catalyst" % sparkVersion).map(_ % "provided")

  lazy val mleapCoreDependencies = baseDependencies.union(sparkLocalDependencies)

  def mleapRuntimeDependencies(scalaVersion: String) = {
    baseDependencies.
      union(sparkLocalDependencies).
      union(Seq("org.scala-lang" % "scala-reflect" % scalaVersion))
  }

  lazy val mleapSparkDependencies = baseDependencies.
    union(sparkDependencies).
    union(Seq("com.databricks" %% "spark-avro" % "2.0.1" % "test"))

  lazy val mleapAvroDependencies = Seq("org.apache.avro" % "avro" % "1.8.1")

  lazy val bundleMlDependencies = baseDependencies.
    union(Seq("io.spray" %% "spray-json" % "1.3.2",
      "com.jsuereth" %% "scala-arm" % "2.0-RC1",
      "com.typesafe" % "config" % "1.3.0"))
}