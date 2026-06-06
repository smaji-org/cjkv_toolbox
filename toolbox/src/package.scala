/*
 * package.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


package org.smaji.cjkv_toolbox.toolbox

import java.nio.file.{Path, Paths, Files}

var swingInitialized= false

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

lazy val jvmFeature= Runtime.version().feature()
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

lazy val userConfigDir=
  hostOs match
    case "windows" => Paths.get(System.getenv("LocalAppData"))
    case _ => Option(System.getenv("XDG_CONFIG_HOME")).map(Paths.get(_))
      .getOrElse(Paths.get(System.getProperty("user.home"), ".config"))

lazy val userDataDir=
  hostOs match
    case "windows" => Paths.get(System.getenv("LocalAppData"))
    case _ => Option(System.getenv("XDG_DATA_HOME ")).map(Paths.get(_))
      .getOrElse(Paths.get(System.getProperty("user.home"), ".local", "share"))

lazy val userFontDir=
  hostOs match
    case "windows" => userDataDir.resolve("Microsoft/Windows/Fonts")
    case _ => userDataDir.resolve("fonts")

lazy val configDir= {
  val skel= Seq("smaji", "cjkv_toolbox")

  Paths.get(userConfigDir.toString(), skel*)
}

lazy val modulesDir= configDir.resolve("module")

lazy val zoneUTC= {
  import java.time.*
  ZoneId.of(ZoneOffset.UTC.getId())
}


def loadMultiResolutionImage(path: String)= {
  import java.awt.image.BaseMultiResolutionImage
  import javax.imageio.ImageIO
  import javax.swing.ImageIcon
  val classloader= Thread.currentThread().getContextClassLoader()
  val url= classloader.getResource(path)
  BaseMultiResolutionImage(ImageIO.read(url))
}

import javax.imageio.ImageIO
def loadImage(path: String)= {
  val classloader= Thread.currentThread().getContextClassLoader()
  val url= classloader.getResource(path)
  ImageIO.read(url)
}

def loadImage(path: String, height: Int)= {
  val classloader= Thread.currentThread().getContextClassLoader()
  val url= classloader.getResource(path)
  ImageIO.read(url).getScaledInstance(height, height, java.awt.Image.SCALE_SMOOTH)
}

def loadImage(path: String, width: Int, height: Int)= {
  import javax.swing.ImageIcon
  val classloader= Thread.currentThread().getContextClassLoader()
  val url= classloader.getResource(path)
  ImageIO.read(url).getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH)
}

import java.awt
import java.awt.*
import javax.swing
import javax.swing.*
def highDpiImageIcon(image: Image, width: Int, height: Int)= {
  new ImageIcon(image) {
    override def getIconWidth()= width
    override def getIconHeight()= height
    override def paintIcon(c: Component, g: Graphics, x: Int, y: Int)= this.synchronized {
      g.drawImage(image, x, y, width, height, null);
    }
  }
}

lazy val locale= java.util.Locale.getDefault()
lazy val uiLangBundle= java.util.ResourceBundle.getBundle("l10n/ui", locale)
def t(msg: String)= uiLangBundle getString msg
