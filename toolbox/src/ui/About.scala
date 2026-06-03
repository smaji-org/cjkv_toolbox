package org.smaji.cjkv_toolbox.toolbox.ui

import java.awt
import java.awt.*
import javax.swing
import javax.swing.*
import javax.swing.SwingUtilities
import java.awt.event.MouseEvent

object About {
  import ContainerOps.*
  import org.smaji.cjkv_toolbox.toolbox.version
  import org.smaji.cjkv_toolbox.toolbox.t

  def create(frame: JFrame)= {
    val dialog= JDialog(frame, true)
    dialog.setTitle("About")
    dialog.setIconImage(icon.getImage())
    dialog.setLocationByPlatform(true)
    val content= dialog.getContentPane()
    content.setLineBoxLayout()

    val tabbedPane= JTabbedPane()
    content.add(tabbedPane)
    tabbedPane.setAlignmentX(Component.CENTER_ALIGNMENT)

    val panelInfo= {
      val panel= JPanel()
      panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
      val wrap= JPanel()
      wrap.setAlignmentX(Component.CENTER_ALIGNMENT)
      wrap.add(panel)

      val infoContent=
        val label=
          JLabel(s"""
          |<html>
          |  <b>CJKV Toolbox</b> © 2023 - 2026 <b>Smaji</b>
          |  <br/>
          |  <br/>
          |  <b>${t("Version:")}</b> ${version}
          |  <br/>
          |  <br/>
          |  <b>${t("Links:")}</b>
          |</html>""".stripMargin.stripPrefix("\n"))
        label
      panel.add(infoContent)

      val linkSmaji=
        val link= "https://cjkv.smaji.org"
        val label=
          JLabel(s"""
          |<html>
          |  <ul>
          |    <li><a href="$link">Smaji CJKV</a></li> $link
          |  </ul>
          |</html>""".stripMargin.stripPrefix("\n"))
        label.addMouseListener(new swing.event.MouseInputAdapter {
          override def mouseClicked(e: MouseEvent): Unit = 
            browse(java.net.URI(link));
        })
        label
      panel.add(linkSmaji)

      val linkGithub=
        val link="https://github.com/smaji-org"
        val label=
          JLabel(s"""
          |<html>
          |  <ul>
          |    <li><a href="$link">Smaji github</a></li> $link
          |  </ul>
          |</html>""".stripMargin.stripPrefix("\n"))
        label.addMouseListener(new swing.event.MouseInputAdapter {
          override def mouseClicked(e: MouseEvent): Unit = 
            browse(java.net.URI(link));
        })
        label
      panel.add(linkGithub)

      wrap
    }
    tabbedPane.addTab(t("Info"), panelInfo)

    val panelContributions= {
      val panel= JPanel()
      panel.setLayout(BoxLayout(panel, BoxLayout.Y_AXIS))
      val wrap= JPanel()
      wrap.setAlignmentX(Component.CENTER_ALIGNMENT)
      wrap.add(panel)
      panel.add(JLabel(s"""
       |<html>
       |  <b>${t("Creator:")}</b>
       |  <ul>
       |    <li>ZAN DoYe &lt;zandoye@gmail.com&gt;</li>
       |  </ul>
       |</html>""".stripMargin.stripPrefix("\n")))
      wrap
    }
    tabbedPane.addTab(t("Contribution"), panelContributions)

    dialog.pack()
    dialog.setMinimumSize(dialog.getSize())
    panelInfo.grabFocus()

    dialog
  }
}

