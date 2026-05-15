package org.smaji.cjkv_toolbox.toolbox.setup

import org.smaji.cjkv_toolbox.toolbox.*

import scala.util.*
import java.io.FileNotFoundException
import java.util.concurrent.CompletableFuture

object Manager {
  import java.util.concurrent.Executors
  import java.nio.file.{Path, Paths, Files}
  import java.io.File

  val executor = Executors.newCachedThreadPool()

  val startPath= toolboxDir.resolve("cjkv_toolbox_start")

  def createCommandOpts(name: String)=
    Seq(
      "--module-dir", modulesDir.resolve(name).toString,
      "--toolbox-dir", toolboxDir.toString,
      "--config-dir", configDir.toString)

  def install(release: module.Release)= {
    val m= release.module
    val (os, archs)= release.platforms.head

    val (done, update_done)= react.Event.create[Try[Int]]()
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
          Seq(startPath.toString, installerPath.toString)
            ++ createCommandOpts(m.name),
          moduleDir.toFile()
          ).run()
        val r= p.exitValue()
        if (r != 0) {
          deleteDir(File(moduleDir.toString))
        }
        r
      }
      update_done(r)
    }
    executor.submit(perform)
    done
  }

  def fInstall(release: module.Release)= {
    val f= CompletableFuture[Try[Int]]()
    install(release).oneshot(f.complete(_))
    f
  }

  def uninstall(m: module.Module)= {
    val (done, update_done)= react.Event.create[Try[Int]]()
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
          Seq(startPath.toString, uninstallerPath.toString)
            ++ createCommandOpts(m.name),
          moduleDir.toFile()
          ).run()
        val r= p.exitValue()
        if (r == 0) {
          deleteDir(File(moduleDir.toString))
        }
        r
      }
      update_done(r)
    }
    executor.submit(perform)
    done
  }

  def fUninstall(m: module.Module)= {
    val f= CompletableFuture[Try[Int]]()
    uninstall(m).oneshot(f.complete(_))
    f
  }

  def installToolbox(release: module.Release)= {
    if debug then println(s"try to install toolbox ${release.version}")
    if (version != release.version) {

      val m= release.module
      val (os, archs)= release.platforms.head

      val r= Try {
        if debug then println(s"downloading ${m.name}, $os, ${archs.head}")
        import scala.sys.process.*
        val downloader= CjkvDownloader()
        val target= s"/module/${m.name}/${release.version}/$os/${archs.head}/${m.name}.tgz"
        downloader.downloadAndExtract(target, modulesDir) match
          case Failure(exception) => throw(exception)
          case Success(value) => ()
        val moduleDir= modulesDir.resolve(m.name)
        if debug then println(s"moduleDir is $moduleDir")
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
        if debug then println(s"installerPath is $installerPath")
        File(installerPath.toString).setExecutable(true)
        if debug then println(s"begin installing ${m.name}, $os, ${archs.head}")
        val cmd= Seq(startPath.toString, installerPath.toString) ++ createCommandOpts(m.name)
        if debug then println(s"$cmd")
        val p= Process(cmd, moduleDir.toFile()).run()
        System.exit(0)
      }
    } else {
      if debug then println(s"toolbox was already updated to ${release.version}")
    }
  }
}
