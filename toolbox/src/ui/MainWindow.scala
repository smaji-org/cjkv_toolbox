package org.smaji.cjkv_toolbox.toolbox

import java.awt
import java.awt.*
import javax.swing
import javax.swing.*
import javax.swing.SwingUtilities

import javax.swing.plaf.nimbus.NimbusLookAndFeel
import java.io.File

object MainWindow {
  import ContainerOps.*

  lazy val icon= loadImage("images/toolbox.png")
  lazy val trayIcon=
    val trayIcon= TrayIcon(icon.getImage(), "CJKV Toolbox")
    trayIcon.setImageAutoSize(true)
    trayIcon

  lazy val frame= JFrame()
  var emHeight= 0
  var padding= 0
  lazy val paddingSet= awt.Insets(padding, padding, padding, padding)

  def start()= {
    try
      UIManager.setLookAndFeel(NimbusLookAndFeel());
    catch
      case ex: UnsupportedLookAndFeelException=>
        System.err.println("Failed to initialize LaF");

    frame.setIconImage(icon.getImage())

    setupMenubar()
    // after menuBar is setup, emHeight and padding are also set up

    setupTray()

    val content= frame.getContentPane()
    content.setLineBoxLayout()

    val tabbedPane= JTabbedPane()

    val iconModules= ImageIcon(loadImage("images/modules.png", emHeight))
    val iconConfig= ImageIcon(loadImage("images/config.png", emHeight))

    val (panelModules, moduleSignal)= createPanelModules(emHeight, padding)
    val (panelConfig, configSignal)= createPanelConfig(emHeight, padding)
    module.Manager.public()

    tabbedPane.addTab("Modules ", iconModules, panelModules)
    tabbedPane.addTab("Config ", iconConfig, panelConfig)

    content.add(tabbedPane)

    frame.pack()
    val currentSize= frame.getSize()
    currentSize.height= Math.max((currentSize.width * 3 / 4), currentSize.height)
    frame.setMinimumSize(currentSize)

    setupPanelConfig(configSignal)

    frame.setVisible(true)
  }

  def setupPanelConfig(signals: ConfigSignal)= {
    signals.update.check.add { msg =>
      module.Manager.updateNow()
    }
  }

  def setupMenubar()= {
    val menuBar= JMenuBar()

    val menuFile= JMenu("File")
    val itemHide= menuFile.add("Hide")
    menuFile.addSeparator()
    val itemQuit= menuFile.add("Quit")

    val menuHelp= JMenu("Help")
    val itemAbout= menuHelp.add("About")

    menuBar.add(menuFile)
    menuBar.add(menuHelp)

    frame.setJMenuBar(menuBar)
    frame.pack()

    val menuHeight= menuFile.getHeight()
    emHeight= menuHeight * 9 / 10
    padding= menuHeight / 3

    val imageHide= ImageIcon(loadImage("images/hide.png", emHeight))
    itemHide.setIcon(imageHide)

    val imageQuit= ImageIcon(loadImage("images/quit.png", emHeight))
    itemQuit.setIcon(imageQuit)

    itemHide.addActionListener(_ => frame.setVisible(false))
    itemQuit.addActionListener(_ => quit())
  }

  def quit()= {
    System.exit(0)
  }

  def setupTray(): Unit= {
    import java.awt.{PopupMenu, TrayIcon}
    if (!SystemTray.isSupported()) {
      System.err.println("SystemTray is not supported")
      return
    }
    val popup = new PopupMenu()
    val tray = SystemTray.getSystemTray()

    // Create a pop-up menu components
    val aboutItem = MenuItem("About")
    val cb1 = CheckboxMenuItem("Set auto size")
    val cb2 = CheckboxMenuItem("Set tooltip")
    val displayMenu = Menu("Display")
    val errorItem = MenuItem("Error")
    val warningItem = MenuItem("Warning")
    val infoItem = MenuItem("Info")
    val noneItem = MenuItem("None")
    val exitItem = MenuItem("Exit")

    //Add components to pop-up menu
    popup.add(aboutItem)
    popup.addSeparator()
    popup.add(cb1)
    popup.add(cb2)
    popup.addSeparator()
    popup.add(displayMenu)
    displayMenu.add(errorItem)
    displayMenu.add(warningItem)
    displayMenu.add(infoItem)
    displayMenu.add(noneItem)
    popup.add(exitItem)

    trayIcon.setPopupMenu(popup)
    trayIcon.setImageAutoSize(true)
    trayIcon.addActionListener(_ => frame.setVisible(!frame.isVisible()))

    try
      tray.add(trayIcon)
    catch 
      case e: AWTException =>
        System.err.println("TrayIcon could not be added.")
  }

  def main()= {
    SwingUtilities.invokeLater(()=>start())
  }
}

