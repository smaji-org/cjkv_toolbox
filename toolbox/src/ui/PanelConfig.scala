/*
 * PanelConfig.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


package org.smaji.cjkv_toolbox.toolbox.ui

import org.smaji.cjkv_toolbox.toolbox
import toolbox.t
import java.awt
import java.awt.*
import javax.swing.*

class ConfigEvent {
  import org.smaji.cjkv_toolbox.toolbox.react
  object ui {
    val (scale, scale_update)= react.Signal.create[Int]
      (toolbox.config.Manager.ui.scale)
  }

  object update {
    val (autoToolbox, update_autoToolbox)= react.Event.create[Boolean]()
    val (autoModules, update_autoModules)= react.Event.create[Boolean]()
    val (toolbox, update_toolbox)= react.Event.create[Unit]()
    val (modules, update_modules)= react.Event.create[Unit]()
    val (interval, update_interval)= react.Event.create[Int]()
    val (check, update_check)= react.Event.create[Unit]()
  }

  object start {
    val (auto, update_auto)= react.Signal.create[Boolean]
      (toolbox.config.Manager.startup.autostart)
    val (minimized, update_minimized)= react.Signal.create[Boolean]
      (toolbox.config.Manager.startup.minimized)
  }
}

def createPanelConfig(emHeight: Int, padding: Int)= {
  import org.smaji.cjkv_toolbox.toolbox.config
  import border.*
  import ContainerOps.*

  val panelConfig= JPanel()

  val configEvent= ConfigEvent()

  val paddingSet= awt.Insets(padding, padding, padding, padding)

  // panelConfig.setLayout(GridLayout(0,1, 3,3))
  panelConfig.setPageBoxLayout()
  panelConfig.setBorder(EmptyBorder(paddingSet))

  val panelUiWrap= JPanel()
  panelUiWrap.setBorder(EtchedBorder())
  panelUiWrap.setLayout(GridLayout(1,1))
  val panelUi= JPanel()
  panelUiWrap.add(panelUi)

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

  panelConfig.add(panelUiWrap)
  panelConfig.add(panelUpdateWrap)
  panelConfig.add(panelStartupWrap)

  // panelUi ui

  panelUi.setPageBoxLayout()
  panelUi.setBorder(EmptyBorder(paddingSet))

  panelUi.add(Box.createVerticalGlue())
  val panelUiScale= JPanel()
  panelUi.add(panelUiScale)
  panelUi.add(Box.createVerticalGlue())

  panelUiScale.setLineBoxLayout()
  val scaleSelector= JComboBox()
  for i <- 1 to 8 do
    scaleSelector.addItem(i.toString)
  scaleSelector.setMaximumSize(scaleSelector.getPreferredSize())

  panelUiScale.add(scaleSelector)
  panelUiScale.add(JLabel(t("HiDPI UI Scale (Restart to apply)")))
  panelUiScale.add(Box.createHorizontalGlue())

  // panelUi signal
  //
  scaleSelector.addActionListener(_ =>
    val scale= scaleSelector.getSelectedIndex() + 1
    config.Manager.ui.scale= scale
    configEvent.ui.scale_update(scale)
  )

  // panelUi loadConfig
  scaleSelector.setSelectedIndex(config.Manager.ui.scale-1)

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
  val checkUpdateToolbox= JCheckBox(t("Auto update Toolbox"))
  panelUpdateToolbox.add(checkUpdateToolbox)
  panelUpdateToolbox.add(Box.createHorizontalGlue())
  val btnUpdateToolbox= JButton(t("Update Toolbox now"))
  panelUpdateToolbox.add(btnUpdateToolbox)

  panelUpdateModules.setLineBoxLayout()
  val checkUpdateModules= JCheckBox(t("Auto update modules"))
  panelUpdateModules.add(checkUpdateModules)
  panelUpdateModules.add(Box.createHorizontalGlue)
  val btnUpdateModules= JButton(t("Update modules now"))
  panelUpdateModules.add(btnUpdateModules)

  panelUpdatePerodic.setLineBoxLayout()
  val intervalTime= IntervalComboBox()
  intervalTime.setMaximumSize(intervalTime.getPreferredSize())

  panelUpdatePerodic.add(JLabel(t("Check for updates every:")))
  panelUpdatePerodic.add(intervalTime)
  panelUpdatePerodic.add(Box.createHorizontalGlue())
  val btnUpdateCheck= JButton(t("Check now"))
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
    configEvent.update.update_autoToolbox(isSelected)
  )

  btnUpdateToolbox.addActionListener(_ =>
    configEvent.update.update_toolbox(())
  )

  checkUpdateModules.addActionListener(_ =>
    val isSelected= checkUpdateModules.isSelected()
    btnUpdateModules.setVisible(!isSelected)
    config.Manager.update.modules= isSelected
    configEvent.update.update_autoModules(isSelected)
  )

  btnUpdateModules.addActionListener(_ =>
    configEvent.update.update_modules(())
  )

  intervalTime.addActionListener(_ =>
    val interval= intervalTime.getSelectedInterval()
    config.Manager.update.interval= interval
    configEvent.update.update_interval(interval)
  )

  btnUpdateCheck.addActionListener(_ =>
    configEvent.update.update_check(())
  )

  // panelStartup ui

  panelStartup.setPageBoxLayout()
  panelStartup.setBorder(EmptyBorder(5,5,5,5))

  panelStartup.add(Box.createVerticalGlue())
  val checkAutostart= JCheckBox(t("Launch on startup"))
  panelStartup.add(checkAutostart)
  panelStartup.add(Box.createVerticalGlue())
  val checkMinimized= JCheckBox(t("Start minimized"))
  panelStartup.add(checkMinimized)
  panelStartup.add(Box.createVerticalGlue())

  // panelStartup loadConfig

  checkAutostart.setSelected(config.Manager.startup.autostart)
  checkMinimized.setSelected(config.Manager.startup.minimized)

  // panelStartup signal

  checkAutostart.addItemListener(_ =>
    val isSelected= checkAutostart.isSelected()
    config.Manager.startup.autostart= isSelected
    configEvent.start.update_auto(isSelected)
  )

  checkMinimized.addItemListener(_ =>
    val isSelected= checkMinimized.isSelected()
    config.Manager.startup.minimized= isSelected
    configEvent.start.update_minimized(isSelected)
  )

  def postSetup()= {
    btnUpdateToolbox.setVisible(!checkUpdateToolbox.isSelected())
    btnUpdateModules.setVisible(!checkUpdateModules.isSelected())
  }

  (panelConfig, configEvent, postSetup _)
}
