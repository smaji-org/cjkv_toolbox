package org.smaji.cjkv_toolbox.toolbox.startup

import org.smaji.cjkv_toolbox.toolbox.*

import javax.swing.SwingUtilities
import scala.util.*
import java.io.FileNotFoundException
import java.util.concurrent.CompletableFuture

trait autostart {
  def enableAutostart(): Unit
  def disenableAutostart(): Unit
  def setAutostart(enable: Boolean)= {
    if enable then
      enableAutostart()
    else
      disenableAutostart()
  }
}

object freedesktop extends autostart {
  import java.nio.file.{Files, Path, Paths}

  private val desktopEntry=
    s"""[Desktop Entry]
       |Comment=Smaji Cjkv Toolbox
       |Exec=java -jar ${jarPath}
       |Name=Smaji Cjkv Toolbox
       |Hidden=false
       |NoDisplay=false
       |Type=Application
       |X-GNOME-Autostart-enabled=true
       |""".stripMargin

  val autostartDir= userConfigDir.resolve("autostart")
  Files.createDirectories(autostartDir)
  val entryPath= autostartDir.resolve("org.smaji.cjkv_toolbox.desktop")

  def enableAutostart()= {
    import java.io.FileWriter
    Using(FileWriter(entryPath.toString, false)) { fileWriter =>
      fileWriter.write(desktopEntry)
    }
  }
  def disenableAutostart()= {
    import java.io.File
    File(entryPath.toString).delete()
  }
}

object windows extends autostart {
  import scala.sys.process.*
  val autostartPath= toolboxDir.resolve("cjkv_toolbox_autostart")
  val key= "org.smaji.cjkv_toolbox"
  val javaw= javaHome.resolve("bin/javaw")
  val run= s"${javaw} -jar ${jarPath}"
  val cmd= Seq(autostartPath.toString, key)

  def enableAutostart()= {
    println(cmd :+ run)
    Process(cmd :+ run).run()
  }
  def disenableAutostart()= {
    println(cmd)
    Process(cmd).run()
  }
}

object darwin extends autostart {
  private val agentEntry=
    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
       |<plist version="1.0">
       |  <dict>
       |    <key>Label</key>
       |    <string>org.smaji.cjkv_toolbox</string>
       |    <key>ProgramArguments</key>
       |    <array>
       |      <string>open</string>
       |      <string>${jarPath}</string>
       |    </array>
       |    <key>RunAtLoad</key>
       |    <true/> <!-- run the program at login -->
       |    <key>KeepAlive</key>
       |    <false/> <!-- run the program again if it terminates -->
       |  </dict>
       |</plist>
       |""".stripMargin

  import java.nio.file.{Paths,Files}

  val autostartDir= Paths.get(System.getProperty("user.home"), "Library", "LaunchAgents")
  Files.createDirectories(autostartDir)
  val agentPath= autostartDir.resolve("org.smaji.cjkv_toolbox.plist")

  def enableAutostart()= {
    import java.io.FileWriter
    Using(FileWriter(agentPath.toString, false)) { fileWriter =>
      fileWriter.write(agentEntry)
    }
  }
  def disenableAutostart()= {
    import java.io.File
    File(agentPath.toString).delete()
  }
}

object Manager {
  import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
  import java.nio.file.{Path, Paths, Files}
  import java.io.File
  // parallel tasking is not allowed
  val executor = Executors.newSingleThreadScheduledExecutor()

  val toolboxDir= jarPath.getParent()
  val startPath= toolboxDir.resolve("cjkv_toolbox_start")
  
  val autostart= hostOs match {
    case "darwin"=> darwin
    case "windows"=> windows
    case _=> freedesktop
  }
  def setAutostart= autostart.setAutostart
}

