package org.smaji.cjkv_toolbox.toolbox

import java.nio.file.{Path, Paths, Files}

val debug= true

lazy val hostArch= {
  System.getProperty("os.arch") match
    case "x86" => "386"
    case "x86_64" => "amd64"
    case arch => arch
}

lazy val hostOs= {
  System.getProperty("os.name") match
    case os if os.startsWith("Windows") => "windows"
    case os if os.startsWith("Mac OS X") => "darwin"
    case os => os
}

lazy val configDir= {
  val skel= Seq("smaji", "cjkv_toolbox")

  hostOs match
    case "windows" => Paths.get(System.getenv("AppData"), skel*)
    case _ => Paths.get(System.getProperty("user.home"), (".config" +: skel)*)
}

lazy val moduleDir= configDir.resolve("module")

lazy val zoneUTC= {
  import java.time.*
  ZoneId.of(ZoneOffset.UTC.getId())
}


def loadImage(path: String)= {
  import javax.swing.ImageIcon
  val classloader= Thread.currentThread().getContextClassLoader()
  val url= classloader.getResource(path)
  ImageIcon(url)
}

def loadImage(path: String, height: Int)= {
  import javax.swing.ImageIcon
  val classloader= Thread.currentThread().getContextClassLoader()
  val url= classloader.getResource(path)
  ImageIcon(url).getImage().getScaledInstance(height, height, java.awt.Image.SCALE_SMOOTH)
}

def loadImage(path: String, width: Int, height: Int)= {
  import javax.swing.ImageIcon
  val classloader= Thread.currentThread().getContextClassLoader()
  val url= classloader.getResource(path)
  ImageIcon(url).getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH)
}

