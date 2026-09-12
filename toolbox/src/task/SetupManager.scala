/*
 * SetupManager.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


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

  def wrapExe(path: String)=
    if path.endsWith(".jar") then
      Seq("java", "-jar", path)
    else
      Seq(path)

  def createCommandOpts(name: String)=
    Seq(
      "--module-dir", modulesDir.resolve(name).toString,
      "--toolbox-dir", toolboxDir.toString,
      "--config-dir", configDir.toString)

  def install(release: module.Release)= {
    val m= release.module
    val os= if release.platforms.exists((os, arch)=> os==hostOs) then hostOs else anyOs
    val osSet= release.platforms.getOrElse(hostOs, release.platforms(anyOs))
    val arch= if osSet.contains(hostArch) then hostArch else anyArch

    val (done, update_done)= react.Event.create[Try[Int]]()
    val perform: Runnable= () => {
      val r= Try {
        if debug then println(s"install ${m.name}, $os, ${arch}")
        import scala.sys.process.*
        val downloader= CjkvDownloader()
        val target= s"/module/${m.name}/${release.version}/$os/${arch}/${m.name}.tgz"
        downloader.downloadAndExtract(target, modulesDir, Some(m.name)) match
          case Failure(exception) => throw(exception)
          case Success(value) => ()
        println(s"module extracted")
        release.data.foreach(data =>
          val target= s"/module/${m.name}/${release.version}/${data}"
          downloader.downloadAndExtract(target, modulesDir.resolve(m.name)) match
            case Failure(exception) => throw(exception)
            case Success(value) => ()
          println(s"data extracted"))
        val moduleDir= modulesDir.resolve(m.name)
        val installerPath=
          installer(m) match
            case Some(path) => path
            case None => throw FileNotFoundException("installer")
        File(installerPath.toString).setExecutable(true)
        println(s"set exec ${installerPath}")
        val p= Process(
          wrapExe(installerPath.toString)
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

  def installer(m: module.Module)=
    val moduleDir= modulesDir.resolve(m.name)
    val binPath= moduleDir.resolve("installer")
    val exePath= moduleDir.resolve("installer.exe")
    val jarPath= moduleDir.resolve("installer.jar")
    if Files.exists(binPath) then
      Some(binPath)
    else if Files.exists(exePath) then
      Some(exePath)
    else if Files.exists(jarPath) then
      Some(jarPath)
    else
      None

  def uninstall(m: module.Module)= {
    val (done, update_done)= react.Event.create[Try[Int]]()
    val perform: Runnable= () => {
      val r= Try {
        import scala.sys.process.*
        val moduleDir= modulesDir.resolve(m.name)
        val uninstallerPath=
          uninstaller(m) match
            case Some(path) => path
            case None => throw FileNotFoundException("uninstaller")
        File(uninstallerPath.toString).setExecutable(true)
        val p= Process(
          wrapExe(uninstallerPath.toString)
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

  def uninstaller(m: module.Module)=
    val moduleDir= modulesDir.resolve(m.name)
    val binPath= moduleDir.resolve("uninstaller")
    val exePath= moduleDir.resolve("uninstaller.exe")
    val jarPath= moduleDir.resolve("uninstaller.jar")
    if Files.exists(binPath) then
      Some(binPath)
    else if Files.exists(exePath) then
      Some(exePath)
    else if Files.exists(jarPath) then
      Some(jarPath)
    else
      None

  def setup(m: module.Module)= {
    val (done, update_done)= react.Event.create[Try[Int]]()
    val perform: Runnable= () => {
      val r= Try {
        import scala.sys.process.*
        val moduleDir= modulesDir.resolve(m.name)
        val uninstallerPath=
          uninstaller(m) match
            case Some(path) => path
            case None => throw FileNotFoundException("setup")
        File(uninstallerPath.toString).setExecutable(true)
        val p= Process(
          wrapExe(uninstallerPath.toString)
            ++ createCommandOpts(m.name),
          moduleDir.toFile()
          ).run()
        val r= p.exitValue()
        r
      }
      update_done(r)
    }
    executor.submit(perform)
    done
  }

  def fSetup(m: module.Module)= {
    val f= CompletableFuture[Try[Int]]()
    setup(m).oneshot(f.complete(_))
    f
  }

  def setuper(m: module.Module)=
    val moduleDir= modulesDir.resolve(m.name)
    val binPath= moduleDir.resolve("setup")
    val exePath= moduleDir.resolve("setup.exe")
    val jarPath= moduleDir.resolve("setup.jar")
    if Files.exists(binPath) then
      Some(binPath)
    else if Files.exists(exePath) then
      Some(exePath)
    else if Files.exists(jarPath) then
      Some(jarPath)
    else
      None

  def update(m: module.Module)= {
    val (done, update_done)= react.Event.create[Try[Int]]()
    uninstall(m).oneshot{ r=>
      r match
        case Success(0)=>
          install(m.releases.head).oneshot(update_done)
        case _=> r
    }
    done
  }

  def fUpdate(m: module.Module)= {
    val f= CompletableFuture[Try[Int]]()
    update(m).oneshot(f.complete(_))
    f
  }

  def installToolbox(release: module.Release)= {
    if debug then println(s"try to install toolbox ${release.version}")
    if (version != release.version) {

      val m= release.module
      val os= if release.platforms.exists((os, arch)=> os==hostOs) then hostOs else anyOs
      val osSet= release.platforms.getOrElse(hostOs, release.platforms(anyOs))
      val arch= if osSet.contains(hostArch) then hostArch else anyArch

      val r= Try {
        if debug then println(s"downloading ${m.name}, $os, ${arch}")
        import scala.sys.process.*
        val downloader= CjkvDownloader()
        val target= s"/module/${m.name}/${release.version}/$os/${arch}/${m.name}.tgz"
        downloader.downloadAndExtract(target, modulesDir, Some(m.name)) match
          case Failure(exception) => throw(exception)
          case Success(value) => ()
        val moduleDir= modulesDir.resolve(m.name)
        if debug then println(s"moduleDir is $moduleDir")
        val installerPath=
          installer(m) match
            case Some(path) => path
            case None => throw FileNotFoundException("installer")
        if debug then println(s"installerPath is $installerPath")
        File(installerPath.toString).setExecutable(true)
        if debug then println(s"begin installing ${m.name}, $os, ${arch}")
        val cmd= wrapExe(installerPath.toString) ++ createCommandOpts(m.name)
        if debug then println(s"$cmd")
        val p= Process(cmd, moduleDir.toFile()).run()
        System.exit(0)
      }
    } else {
      if debug then println(s"toolbox was already updated to ${release.version}")
    }
  }
}
