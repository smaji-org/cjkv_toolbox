package org.smaji.cjkv_toolbox.toolbox

import java.awt
import java.awt.*
import javax.swing.*

class ConfigSignal {
  object update {
    val autoToolbox= Pub[Boolean]()
    val autoModules= Pub[Boolean]()
    val toobox= Pub[Unit]()
    val modules= Pub[Unit]()
    val period= Pub[Int]()
    val check= Pub[Unit]()
  }

  object start {
    val auto= Pub[Boolean]()
    val minimized= Pub[Boolean]()
  }
}

def createPanelConfig(emHeight: Int, padding: Int)= {
  import border.*
  import ContainerOps.*

  val panelConfig= JPanel()

  val configSignal= ConfigSignal()

  val paddingSet= awt.Insets(padding, padding, padding, padding)

  // panelConfig.setLayout(GridLayout(0,1, 3,3))
  panelConfig.setPageBoxLayout()
  panelConfig.setBorder(EmptyBorder(paddingSet))

  val panelUpdateWrap= JPanel()
  panelUpdateWrap.setBorder(EtchedBorder())
  panelUpdateWrap.setLayout(GridLayout(1,1))
  val panelUpdate= JPanel()
  panelUpdateWrap.add(panelUpdate)

  val panelStartupWrap= JPanel()
  panelStartupWrap.setBorder(EtchedBorder())
  panelStartupWrap.setLayout(GridLayout(1,1))
  val panelStartup= JPanel()
  panelStartupWrap.add(panelStartup)

  panelConfig.add(panelUpdateWrap)
  panelConfig.add(panelStartupWrap)


  // panelUpdate ui

  panelUpdate.setPageBoxLayout()
  panelUpdate.setBorder(EmptyBorder(paddingSet))

  panelUpdate.add(Box.createVerticalGlue())
  val panelUpdateToolbox= JPanel()
  panelUpdate.add(panelUpdateToolbox)
  panelUpdate.add(Box.createVerticalGlue())
  val panelUpdateModules= JPanel()
  panelUpdate.add(panelUpdateModules)
  panelUpdate.add(Box.createVerticalGlue())
  val panelUpdatePerodic= JPanel()
  panelUpdate.add(panelUpdatePerodic)
  panelUpdate.add(Box.createVerticalGlue())

  panelUpdateToolbox.setLineBoxLayout()
  val checkUpdateToolbox= JCheckBox("Update Toolbox automatically")
  panelUpdateToolbox.add(checkUpdateToolbox)
  panelUpdateToolbox.add(Box.createHorizontalGlue())
  val btnUpdateToolbox= JButton("Update Toolbox now")
  panelUpdateToolbox.add(btnUpdateToolbox)

  panelUpdateModules.setLineBoxLayout()
  val checkUpdateModules= JCheckBox("Update the modules automatically")
  panelUpdateModules.add(checkUpdateModules)
  panelUpdateModules.add(Box.createHorizontalGlue)
  val btnUpdateModules= JButton("Update modules now")
  panelUpdateModules.add(btnUpdateModules)

  panelUpdatePerodic.setLineBoxLayout()
  val periodicTime= JComboBox[String]()
  periodicTime.addItem("6 hours")
  periodicTime.addItem("24 hours")
  periodicTime.addItem("4 days")
  periodicTime.setMaximumSize(periodicTime.getPreferredSize())

  panelUpdatePerodic.add(periodicTime)
  panelUpdatePerodic.add(JLabel("Periodic update check"))
  panelUpdatePerodic.add(Box.createHorizontalGlue())
  val btnUpdateCheck= JButton("Check now")
  panelUpdatePerodic.add(btnUpdateCheck)

  // panelUpdate signal

  checkUpdateToolbox.addActionListener(_ =>
    configSignal.update.autoToolbox.pub(checkUpdateToolbox.isSelected())
  )

  btnUpdateToolbox.addActionListener(_ =>
    configSignal.update.toobox pub ()
  )

  checkUpdateModules.addActionListener(_ =>
    configSignal.update.autoModules.pub(checkUpdateModules.isSelected())
  )

  btnUpdateModules.addActionListener(_ =>
    configSignal.update.modules pub ()
  )

  periodicTime.addActionListener(_ =>
    configSignal.update.period.pub(periodicTime.getSelectedIndex())
  )

  btnUpdateCheck.addActionListener(_ =>
    configSignal.update.check pub ()
  )

  // panelStartup ui

  panelStartup.setPageBoxLayout()
  panelStartup.setBorder(EmptyBorder(5,5,5,5))

  panelStartup.add(Box.createVerticalGlue())
  val checkAutostart= JCheckBox("Autostart Toolbox")
  panelStartup.add(checkAutostart)
  panelStartup.add(Box.createVerticalGlue())
  val checkMinimized= JCheckBox("Start minimized")
  panelStartup.add(checkMinimized)
  panelStartup.add(Box.createVerticalGlue())

  // panelStartup signal

  checkAutostart.addItemListener(_ =>
    configSignal.start.auto.pub(checkAutostart.isSelected())
  )

  checkMinimized.addItemListener(_ =>
    configSignal.start.minimized.pub(checkMinimized.isEnabled())
  )

  (panelConfig, configSignal)
}
