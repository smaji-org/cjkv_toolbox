package org.smaji.cjkv_toolbox.toolbox.setup

import org.smaji.cjkv_toolbox.toolbox.*

import javax.swing.SwingUtilities
import scala.util.*
import java.io.FileNotFoundException

object Manager {
  import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
  import java.nio.file.{Path, Paths, Files}
  import java.io.File
  val executor = Executors.newSingleThreadScheduledExecutor()

  val toolboxDir= jarPath.getParent()
  val startPath= toolboxDir.resolve("start")

  def createCommandOpts(name: String)=
    Seq(
      "--module-dir", modulesDir.resolve(name).toString,
      "--toolbox-dir", jarPath.getParent().toString,
      "config-dir", configDir.toString)

  def install(release: module.Release)= {
    val m= release.module
    val (os, archs)= release.platforms.head

    val done= Pub[Try[Int]]
    val perform: Runnable= () => {
      val r= Try {
        println(s"install ${m.name}, $os, ${archs.head}")
        import scala.sys.process.*
        val downloader= CjkvDownloader()
        val target= s"/module/${m.name}/${release.version}/$os/${archs.head}/${m.name}.tgz"
        downloader.downloadAndExtract(target, modulesDir) match
          case Failure(exception) => throw(exception)
          case Success(value) => ()
        val moduleDir= modulesDir.resolve(m.name)
        val installerPath= {
          val exePath= moduleDir.resolve("installer.exe")
          val jarPath= moduleDir.resolve("installer.jar")
          if (Files.exists(exePath)) {
            exePath
          } else if (Files.exists(jarPath)) {
            jarPath
          } else {
            throw FileNotFoundException(exePath.toString)
          }
        }
        File(installerPath.toString).setExecutable(true)
        val p= Process(
          Seq(
            startPath.toString, installerPath.toString)
          ++ createCommandOpts(m.name)
          ).run()
        val r= p.exitValue()
        if (r != 0) {
          deleteDir(File(moduleDir.toString))
        }
        r
      }
      SwingUtilities.invokeLater(()=> done.pub(r))
    }
    executor.submit(perform)
    done
  }

  def uninstall(node: module.ModuleNode)= {
    val m= node.module
    val done= Pub[Try[Int]]
    val perform: Runnable= () => {
      val r= Try {
        import scala.sys.process.*
        val moduleDir= modulesDir.resolve(m.name)
        val uninstallerPath= {
          val exePath= moduleDir.resolve("uninstaller.exe")
          val jarPath= moduleDir.resolve("uninstaller.jar")
          if (Files.exists(exePath)) {
            exePath
          } else if (Files.exists(jarPath)) {
            jarPath
          } else {
            throw FileNotFoundException(exePath.toString)
          }
        }
        File(uninstallerPath.toString).setExecutable(true)
        val p= Process(
          Seq(
            startPath.toString, uninstallerPath.toString)
          ++ createCommandOpts(m.name)
          ).run()
        val r= p.exitValue()
        if (r == 0) {
          deleteDir(File(moduleDir.toString))
        }
        r
      }
      SwingUtilities.invokeLater(()=> done.pub(r))
    }
    executor.submit(perform)
    done
  }
}
