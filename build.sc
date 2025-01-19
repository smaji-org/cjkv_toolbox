package build
import mill._, scalalib._

object toolbox extends ScalaModule {
  def publishVersion= "0.0.1"

  def scalaVersion= "3.3.4"
  def scalacOptions= Seq(
    // "-Werror",
    "-release:8",
    "-Ysafe-init",
    "-explain",
    "-deprecation",
    // "-Yexplicit-nulls",
    )

  def ivyDeps = Agg(
    ivy"com.lihaoyi::scalatags:0.13.1",
    ivy"com.lihaoyi::mainargs:0.7.6",
    ivy"org.apache.commons:commons-compress:1.27.1",
  )
  /*
  object test extends ScalaTests {
    def ivyDeps = Agg(ivy"com.lihaoyi::utest:0.8.4")
    def testFramework = "utest.runner.Framework"
  }
  */
}

object upgrader extends ScalaModule {
  def publishVersion= "0.0.1"

  def scalaVersion= "3.3.4"
  def scalacOptions= Seq(
    "-Werror",
    "-release:8",
    "-Ysafe-init",
    "-explain",
    "-deprecation",
    // "-Yexplicit-nulls",
    )

  def ivyDeps = Agg(
    ivy"com.lihaoyi::scalatags:0.13.1",
    ivy"com.lihaoyi::mainargs:0.7.6",
    ivy"org.apache.commons:commons-compress:1.27.1",
  )
  /*
  object test extends ScalaTests {
    def ivyDeps = Agg(ivy"com.lihaoyi::utest:0.8.4")
    def testFramework = "utest.runner.Framework"
  }
  */
}

