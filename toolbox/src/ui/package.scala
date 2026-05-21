package org.smaji.cjkv_toolbox.toolbox.ui

var emHeight= 0
var padding= 0

import javax.swing.ImageIcon
import org.smaji.cjkv_toolbox.toolbox.loadImage
lazy val icon= ImageIcon(loadImage("images/toolbox.png", emHeight))


def browse(uri: java.net.URI)=
  import org.smaji.cjkv_toolbox.toolbox.hostOs
  import scala.sys.process.*
  import java.awt.Desktop
  val desktop = Desktop.getDesktop()
  if Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE) then
    desktop.browse(uri)
  else
    if hostOs.contains("linux") || hostOs.contains("bsd") then
      val p= Process(Seq("xdg-open", uri.toString())).run()
      p.exitValue()
