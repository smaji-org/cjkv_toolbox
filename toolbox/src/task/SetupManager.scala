package org.smaji.cjkv_toolbox.toolbox.setup

import org.smaji.cjkv_toolbox.toolbox.*

import javax.swing.SwingUtilities
import scala.util.*
import java.io.FileNotFoundException
import java.util.concurrent.CompletableFuture

object Manager {
  import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
  import java.nio.file.{Path, Paths, Files}
  import java.io.File
  // parallel tasking is not allowed
  val executor = Executors.newSingleThreadScheduledExecutor()

  val startPath= toolboxDir.resolve("cjkv_toolbox_start")

  def createCommandOpts(name: String)=
    Seq(
      "--module-dir", modulesDir.resolve(name).toString,
      "--toolbox-dir", toolboxDir.toString,
      "config-dir", configDir.toString)

  def install(release: module.Release)= {
    val m= release.module
    val (os, archs)= release.platforms.head

    val done= react.Event[Try[Int]]
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
      SwingUtilities.invokeLater(()=> done.update(r))
    }
    executor.submit(perform)
    done
  }

  def fInstall(release: module.Release)= {
    val f= CompletableFuture[Try[Int]]()
    install(release).map(f.complete(_))
    f
  }

  def uninstall(node: module.ModuleNode)= {
    val m= node.module
    val done= react.Event[Try[Int]]
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
      SwingUtilities.invokeLater(()=> done.update(r))
    }
    executor.submit(perform)
    done
  }

  def fUninstall(node: module.ModuleNode)= {
    val f= CompletableFuture[Try[Int]]()
    uninstall(node).map(f.complete(_))
    f
  }

  def installToolbox(toolbox: module.Module)= {
    val release= toolbox.releases.head
    println(s"try to install toolbox ${release.version}")
    if (version != release.version) {

      val m= release.module
      val (os, archs)= release.platforms.head

      val r= Try {
        println(s"downloading ${m.name}, $os, ${archs.head}")
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
        println(s"begin installing ${m.name}, $os, ${archs.head}")
        val p= Process(
          Seq(
            startPath.toString, installerPath.toString)
          ++ createCommandOpts(m.name)
          ).run()
        System.exit(0)
      }
    }
  }
}
