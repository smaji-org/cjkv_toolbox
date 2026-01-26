package build
import mill._, scalalib._

object toolbox extends ScalaModule {
  def publishVersion= "0.0.1"

  def scalaVersion= "3.3.7"
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

  def scalaVersion= "3.3.7"
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

trait GoMod extends Module {
  def sources=  Task.Source(millSourcePath)
  def compile= Task {
    os.call(("go", "build", "-o", Task.dest, "-C", sources().path, "."), stdout= os.Inherit)
    PathRef(Task.dest)
  }
}

trait GoCross extends Module {
  def target= "target"
  object build extends GoMod {
  }
  def compile= Task {
    val buildPath= build.compile().path
    val cross_build= (buildPath / "cross_build").toString()
    os.call((cross_build, millSourcePath / target, Task.dest), stdout=os.Inherit)
    PathRef(Task.dest)
  }
}

object native_aux extends Module {
  object start extends GoCross {
    def target= "start"
  }

  object autostart extends GoCross {
    def target= "autostart"
  }
}
