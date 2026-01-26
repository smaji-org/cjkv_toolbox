package org.smaji.cjkv_toolbox.toolbox

import java.nio.file.{Path, Paths, Files}

val debug= true

val version= "0.1.0"

lazy val hostArch= {
  System.getProperty("os.arch") match
    case "x86" => "386"
    case "x86_64" => "amd64"
    case arch => arch.toLowerCase()
}

val anyArch= "any"
val anyOs= "any"

lazy val hostOs= {
  System.getProperty("os.name") match
    case os if os.startsWith("Windows") => "windows"
    case os if os.startsWith("Mac OS X") => "darwin"
    case os => os.toLowerCase()
}

lazy val javaHome= Paths.get(System.getProperty("java.home"))

lazy val jarPath= {
  class Self {}
  val url= classOf[Self]
    .getProtectionDomain().getCodeSource()
    .getLocation().toString()

  val fileSchema= hostOs match {
    case "windows" => "file:/"
    case _ => "file:"
  }
  if url.startsWith(fileSchema) then
    Paths.get(url.drop(fileSchema.length))
  else
    Paths.get(url)
}

lazy val toolboxDir=
  jarPath.getParent()

lazy val userConfigDir= {
  hostOs match
    case "windows" => Paths.get(System.getenv("AppData"))
    case _ => Option(System.getenv("XDG_CONFIG_HOME")).map(Paths.get(_))
      .getOrElse(Paths.get(System.getProperty("user.home"), ".config"))
}

lazy val configDir= {
  val skel= Seq("smaji", "cjkv_toolbox")

  Paths.get(userConfigDir.toString(), skel*)
}

lazy val modulesDir= configDir.resolve("module")

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

