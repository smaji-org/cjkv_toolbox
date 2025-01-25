package org.smaji.cjkv_toolbox.toolbox

import java.awt
import java.awt.*
import javax.swing.*

class ConfigSignal {
  object update {
    val autoToolbox= Pub[Boolean]()
    val autoModules= Pub[Boolean]()
    val toolbox= Pub[Unit]()
    val modules= Pub[Unit]()
    val interval= Pub[Int]()
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
  val intervalTime= IntervalComboBox()
  intervalTime.setMaximumSize(intervalTime.getPreferredSize())

  panelUpdatePerodic.add(intervalTime)
  panelUpdatePerodic.add(JLabel("Periodic update check"))
  panelUpdatePerodic.add(Box.createHorizontalGlue())
  val btnUpdateCheck= JButton("Check now")
  panelUpdatePerodic.add(btnUpdateCheck)

  // panelUpdate loadConfig
  checkUpdateToolbox.setSelected(config.Manager.update.toolbox)
  checkUpdateModules.setSelected(config.Manager.update.modules)
  intervalTime.setSelectedInterval {
    val interval= config.Manager.update.interval
    if interval <= 0 then 60*60*24*4 else interval
  }

  // panelUpdate signal

  checkUpdateToolbox.addActionListener(_ =>
    val isSelected= checkUpdateToolbox.isSelected()
    btnUpdateToolbox.setVisible(!isSelected)
    config.Manager.update.toolbox= isSelected
    configSignal.update.autoToolbox pub isSelected
  )

  btnUpdateToolbox.addActionListener(_ =>
    configSignal.update.toolbox pub ()
  )

  checkUpdateModules.addActionListener(_ =>
    val isSelected= checkUpdateModules.isSelected()
    btnUpdateModules.setVisible(!isSelected)
    config.Manager.update.modules= isSelected
    configSignal.update.autoModules pub isSelected
  )

  btnUpdateModules.addActionListener(_ =>
    configSignal.update.modules pub ()
  )

  intervalTime.addActionListener(_ =>
    val interval= intervalTime.getSelectedInterval()
    config.Manager.update.interval= interval
    configSignal.update.interval pub interval
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

  // panelStartup loadConfig

  checkAutostart.setSelected(config.Manager.startup.autostart)
  checkMinimized.setSelected(config.Manager.startup.minimized)

  // panelStartup signal

  checkAutostart.addItemListener(_ =>
    val isSelected= checkAutostart.isSelected()
    config.Manager.startup.autostart= isSelected
    configSignal.start.auto pub isSelected
  )

  checkMinimized.addItemListener(_ =>
    val isSelected= checkMinimized.isSelected()
    config.Manager.startup.minimized= isSelected
    configSignal.start.minimized pub isSelected
  )

  def postSetup()= {
    btnUpdateToolbox.setVisible(!checkUpdateToolbox.isSelected())
    btnUpdateModules.setVisible(!checkUpdateModules.isSelected())
  }

  (panelConfig, configSignal, postSetup _)
}
