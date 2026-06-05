/*
 * Reset.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


package org.smaji.cjkv_toolbox.toolbox.ui

import java.awt
import java.awt.*
import javax.swing
import javax.swing.*
import javax.swing.SwingUtilities
import java.awt.event.MouseEvent

object Reset {
  import ContainerOps.*
  import org.smaji.cjkv_toolbox.toolbox
  import toolbox.t

  def create(frame: JFrame)= {
    val dialog= JDialog(frame, true)
    dialog.setTitle("About")
    dialog.setIconImage(icon.getImage())
    dialog.setLocationByPlatform(true)
    val content= dialog.getContentPane()
    content.setLineBoxLayout()

    val panelReset= {
      val panel= JPanel()
      panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
      val wrap= JPanel()
      wrap.setAlignmentX(Component.CENTER_ALIGNMENT)
      wrap.add(panel)

      val infoContent=
        /*
        val label=
          JLabel(s"""
          |<html>
          |  <b><font color="red">RESET</font> Toolbox</b>
          |  <br/>
          |  All modules will be <font color="red">uninstalled</font><br/>
          |  and all footprints will be cleaned up(config files or entries in the registry table)
          |  <br/>
          |  <br/>
          |  After clicking the &lt;Reset&gt; button, resetting will be performed and this toolbox will quit.<br/>
          |  <br/>
          |  A fresh beginning by starting this toolbox again,<br/>
          |  Or purge this software completely,<br/>
          |  by removing the directory containing this software:<br/>
          |  <font color="green">${toolbox.toolboxDir}</font>
          |</html>""".stripMargin.stripPrefix("\n"))
        */
        val label=
          JLabel(s"""
          |<html>
          |  ${t("resetInfo")}
          |  <font color="green">${toolbox.toolboxDir}</font>
          |</html>""".stripMargin.stripPrefix("\n"))
        label
      panel.add(infoContent)

      panel.add(Box.createVerticalStrut(16));

      val resetBtn=
        val button=
          JButton(t("Reset"))
        button.addActionListener(_=> toolbox.reset())
        button
      panel.add(resetBtn)

      wrap
    }

    content.add(panelReset)


    dialog.pack()
    dialog.setMinimumSize(dialog.getSize())

    dialog
  }
}

