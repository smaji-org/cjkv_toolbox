package org.smaji.cjkv_toolbox.toolbox.ui

import java.awt
import java.awt.*
import javax.swing
import javax.swing.*
import javax.swing.SwingUtilities

import javax.swing.plaf.nimbus.NimbusLookAndFeel
import java.io.File

object MainWindow {
  import org.smaji.cjkv_toolbox.toolbox.*
  import ContainerOps.*

  val scale= config.Manager.ui.scale
  System.setProperty("sun.java2d.uiScale", scale.toString)

  // NOTE! These lazy values are NOT setup before setupMenubar is called
  lazy val frame= JFrame()
  lazy val paddingSet= awt.Insets(padding, padding, padding, padding)

  lazy val trayIcon=
    val trayIcon= TrayIcon(icon.getImage(), "CJKV Toolbox")
    if scale > 1 then
      trayIcon.setImageAutoSize(false)
      val traySize= trayIcon.getSize()
      val img= icon.getImage()
      val width= (traySize.width.toFloat * 13/10 / scale).toInt
      val height= (traySize.height.toFloat * 13/10 / scale).toInt
      val imgScaled= img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
      trayIcon.setImage(imgScaled)
    else
      trayIcon.setImageAutoSize(true)
    trayIcon
  // end NOTE

  def start()= {
    swingInitialized= true
    try
      UIManager.setLookAndFeel(NimbusLookAndFeel());
    catch
      case ex: UnsupportedLookAndFeelException=>
        System.err.println("Failed to initialize LaF");

    setupMenubar()
    // after menuBar is setup, emHeight and padding are also set up

    frame.setIconImage(icon.getImage())
    frame.setTitle("CJKV Toolbox")
    frame.setLocationByPlatform(true)
    setupTray()

    val content= frame.getContentPane()
    content.setLineBoxLayout()

    val tabbedPane= JTabbedPane()

    val iconModules= ImageIcon(loadImage("images/modules.png", emHeight))
    val iconConfig= ImageIcon(loadImage("images/config.png", emHeight))

    val panelModules= createPanelModules(emHeight, padding)
    val (panelConfig, configSignal, panelConfigPostSetup)= createPanelConfig(emHeight, padding)

    tabbedPane.addTab("Config ", iconConfig, panelConfig)
    content.add(tabbedPane)
    frame.pack()
    tabbedPane.insertTab("Modules ", iconModules, panelModules, null, 0)
    tabbedPane.setSelectedIndex(0)

    val currentSize= frame.getSize()
    currentSize.height= Math.max((currentSize.width * 3 / 4), currentSize.height)
    frame.setMinimumSize(currentSize)

    setupPanelConfig(configSignal)
    panelConfigPostSetup()

    val maskPane= new JPanel {
      def p(g: Graphics)=
        val grayMask = Color(128, 128, 128, 128)
        g.setColor(grayMask)
        g.fillRect(0, 0, getWidth(), getHeight())
        println(s"${getWidth()}, ${getHeight()}")

      override def paintComponent(g: Graphics)= p(g)
    }
    frame.setGlassPane(maskPane)

    if (config.Manager.startup.minimized) {
    } else {
      frame.setVisible(true)
    }
  }

  def setupPanelConfig(signals: ConfigEvent)= {
    signals.update.check map { msg =>
      println("signals update check")
      module.Manager.updateIndexNow()
    }

    signals.update.interval map { interval =>
      module.Manager.resetTask(interval)
    }

    signals.update.toolbox map { _ =>
      module.Manager.updateToolbox()
    }

    signals.update.modules map { _ =>
      module.Manager.updateModules()
    }

    signals.start.auto map(startup.Manager.setAutostart(_))
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

    val imageHide= highDpiImageIcon(loadImage("images/hide.png"), emHeight, emHeight)
    itemHide.setIcon(imageHide)

    val imageQuit= highDpiImageIcon(loadImage("images/quit.png"), emHeight, emHeight)
    itemQuit.setIcon(imageQuit)

    itemHide.addActionListener(_ => frame.setVisible(false))
    itemQuit.addActionListener(_ => quit())

    val aboutDialog= About.create(frame)
    aboutDialog.addComponentListener:
      import java.awt.event.*
      new ComponentAdapter:
        override def componentShown (e: ComponentEvent)=
          frame.getGlassPane().setVisible(true)
        override def componentHidden (e: ComponentEvent)=
          frame.getGlassPane().setVisible(false)
    itemAbout.addActionListener(_ => aboutDialog.setVisible(true))
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
    val itemQuit = MenuItem("Exit")
    itemQuit.addActionListener(_ => quit())

    //Add components to pop-up menu
    popup.add(itemQuit)

    trayIcon.setPopupMenu(popup)
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

