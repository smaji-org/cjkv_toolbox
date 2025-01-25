package org.smaji.cjkv_toolbox.toolbox.module

import org.smaji.cjkv_toolbox.toolbox.*

import util.*
import control.TailCalls.*

import org.smaji.cjkv_toolbox.toolbox.{CjkvDownloader, modulesDir}
import org.smaji.cjkv_toolbox.toolbox.config

import java.nio.file.{Path, Paths, Files}
import java.time
import java.io.{File, StringWriter, FileWriter}
import java.util.stream.Collectors
import java.io.StringReader
import java.io.StringBufferInputStream
import scala.collection.immutable.ArraySeq
import java.time.OffsetDateTime
import javax.swing.SwingUtilities
import java.util.concurrent.CompletableFuture
import java.io.FileNotFoundException

class ModuleSignal {
  val updated= Pub[(Module, ArraySeq[Module])]()
  val busying= Pub[Boolean]

  var counter= 0
  def incTask()= synchronized {
    counter+= 1
    if counter == 1 then busying.pub(true)
  }
  def decTask()= synchronized {
    if counter > 0 then
      counter-= 1
      if counter == 0 then busying.pub(false)
  }
}

object Manager {
  val defaultInterval= 60*60*24 // 1 day
  if (config.Manager.update.interval <= 0) {
    config.Manager.update.interval= defaultInterval
  }
  val cjkvDownloader= CjkvDownloader()

  var toolbox: Module= null
  var modules= ArraySeq[Module]()

  val model= module.ModulesModel()
  val signal= ModuleSignal()

  import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

  val indexExecutor = Executors.newSingleThreadScheduledExecutor()
  val updateExecutor = Executors.newSingleThreadScheduledExecutor()

  model.signal.add { (node, selected)=>
    if (node.installed.isDefined && !selected) {
      uninstall(node)
    } else if (node.installed.isEmpty && selected) {
      install(node)
    }

    // modulesModle.doneModuleSetup(node.module.name, if install then Some("hi") else None)
  }

  def uninstall(node: ModuleNode, oneshot: Boolean= true)= {
    if oneshot then signal.busying.pub(true)
    val unistallSignal= setup.Manager.uninstall(node)
    unistallSignal.add { r =>
      if oneshot then signal.busying.pub(false)
      r match {
        case Failure(exception) =>
          println(exception)
        case Success(0) =>
          node.installed= None
          model.fireTableDataChanged()
          config.Manager.modules=
            config.Manager.modules.removed(node.module.name)
        case Success(_) => println("uninstall failed")
      }
    }
    unistallSignal
  }

  def fUninstall(node: ModuleNode, oneshot: Boolean= true)= {
    val f= CompletableFuture[Try[Int]]()
    val r= uninstall(node, oneshot)
    r.add(f.complete(_))
    f
  }

  def install(node: ModuleNode, oneshot: Boolean= true)= {
    if oneshot then signal.busying.pub(true)
    val release= node.module.releases.head
    val installSignal= setup.Manager.install(release)
    installSignal.add { r =>
      if oneshot then signal.busying.pub(false)
      r match {
        case Failure(exception) =>
          println(exception)
        case Success(0) =>
          val name= node.module.name
          val version= release.version
          val datetime= OffsetDateTime.now(zoneUTC)
          val moduleInfo= InstalledModuleInfo(name, version, datetime)
          node.installed= Some(release.version)
          model.fireTableDataChanged()
          config.Manager.modules=
            config.Manager.modules.updated(name, moduleInfo)
        case Success(_) => println("install failed")
      }
    }
    installSignal
  }

  def fInstall(node: ModuleNode, oneshot: Boolean= true)= {
    val f= CompletableFuture[Try[Int]]()
    val r= install(node, oneshot)
    r.add(f.complete(_))
    f
  }

  def loadIndex()= {
    import collection.immutable.ArraySeq
    import java.net.{URL, URLEncoder, URLDecoder}
    import javax.xml.parsers as xmlParsers
    import javax.xml.transform as xmlTransform
    import org.w3c.dom
    import javax.xml.xpath
    import XPathOps.*

    val modulePath= modulesDir.resolve("index.xml")
    val moduleIndexFile= modulePath.toFile()
    val xmlBuilder= xmlParsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val transformer = xmlTransform.TransformerFactory.newInstance().newTransformer()
    val xpathEval = xpath.XPathFactory.newInstance().newXPath()

    /*
    if !toolboxFile.exists() then
      Using(Files.newBufferedWriter(toolboxPath)){_ write defaultConfig}
    */

    val defaultIndex=
      """<?xml version="1.0" encoding="UTF-8" ?>
        |<cjkv>
        |  <toolbox>
        |    <description>
        |      CJKV Toolbox
        |    </description>
        |    <releases>
        |    </releases>
        |  </toolbox>
        |  <modules>
        |  </modules>
        |</cjkv>
        |""".stripMargin


    val toolboxCfg=
      if (moduleIndexFile.exists()) {
        xmlBuilder.parse(moduleIndexFile)
      } else {
        xmlBuilder.parse(org.xml.sax.InputSource(StringReader(defaultIndex)))
      }

    val elemCjkv= getOrCreateElem("cjkv", toolboxCfg)

    val elemToolbox= getOrCreateElem("toolbox", elemCjkv)
    val elemModules= getOrCreateElem("modules", elemCjkv)

    import DomOps.*

    def loadModule(elem: dom.Element)= {
      val name=
        elem.getTagName() match
          case "module" => elem.getAttribute("name")
          case name => name
      val description=
        xpathEval.getNode("description", elem) match {
          case null => ""
          case description: dom.Node => description.getTextContent()
        }

      def loadOs(os: dom.Node)= {
        val name= os.asInstanceOf[dom.Element].getAttribute("name")
        val archList= xpathEval.getNodeSet("arch", os) match {
          case null=> Set[String]()
          case nodeList: dom.NodeList =>
            nodeList.asScala
              .map { _.asInstanceOf[dom.Element].getAttribute("name") }
              .toSet
        }
        name -> archList
      }

      def loadRelease(release: dom.Node, module: Module)= {
        val version= xpathEval.getNode("version", release).getTextContent()
        val datetime= time.OffsetDateTime.parse(
          xpathEval.getNode("datetime", release).getTextContent())
        val comment= xpathEval.getNode("comment", release).getTextContent()
        val platforms= xpathEval.getNodeSet("os", release) match {
          case null=> Map[String, Set[String]]()
          case nodeList: dom.NodeList =>
            nodeList.asScala.map(loadOs).toMap
        }
        Release(module, version, datetime, comment, platforms)
      }

      val module= Module(name, description, ArraySeq())
      val releases=
        try
          xpathEval.getNodeSet("releases/release", elem) match
            case null=> ArraySeq[Release]()
            case nodeList: dom.NodeList =>
              nodeList.asScala.map(loadRelease(_, module))
        catch _ =>
          ArraySeq[Release]()
      module.releases= releases

      module
    }
    
    def fitReleases(releases: ArraySeq[Release])=
      releases
        .map { release =>
          val allPlatforms= release.platforms
          val fitPlatforms= allPlatforms
            .view
            .filter((os, _)=>
              os == anyOs || os == hostOs)
            .map((os, archs)=>
              (os, archs.filter(arch => arch == anyArch || arch == hostArch)))
            .filter((_, archs)=> archs.nonEmpty)
            .toMap
          release.copy(platforms= fitPlatforms)
        }
        .filter(_.platforms.nonEmpty)

    def fitModules(modules: ArraySeq[Module])=
      modules
        .map { module=>
          module.copy(releases= fitReleases(module.releases))
        }
        .filter(_.releases.nonEmpty)

    toolbox= loadModule(elemToolbox)
    toolbox= toolbox.copy(releases= fitReleases(toolbox.releases))
    modules=
      try
        xpathEval.getNodeSet("modules/module", elemCjkv) match
          case null=> ArraySeq[Module]()
          case nodeList: dom.NodeList =>
            fitModules(
              nodeList.asScala
                .map(_.asInstanceOf[dom.Element])
                .map(loadModule))
      catch _ =>
        ArraySeq[Module]()

    val moduleInstalled= config.Manager.modules
    val moduleNodes= modules.map { m =>
      val installedVersion= moduleInstalled
        .find((name, info)=>
          m.name == name && m.releases.exists(_.version == info.version))
        .map((name, info)=> info.version)
      module.ModuleNode(m, installedVersion)
    }
    model.loadModuleInfo(moduleNodes)

    signal.updated.pub(toolbox, modules)
    if config.Manager.update.modules then updateModules()
    if config.Manager.update.toolbox then updateToolbox()
  }

  val updateIndex: Runnable= () => {
    cjkvDownloader.downloadAndExtract("/index.xml.tgz", modulesDir)
    loadIndex()
    updateIndexTask= indexExecutor.schedule(updateIndex, config.Manager.update.interval, TimeUnit.SECONDS)
  }


  var updateIndexTask=
    if Files.exists(modulesDir.resolve("index.xml")) then
      loadIndex()
      indexExecutor.schedule(updateIndex, config.Manager.update.interval, TimeUnit.SECONDS)
    else
      indexExecutor.submit(updateIndex)

  def resetTask(interval: Int = config.Manager.update.interval)= {
    if (updateIndexTask.cancel(false)) {
      indexExecutor.schedule(updateIndex, interval, TimeUnit.SECONDS)
    }
  }

  def updateIndexNow()= {
    if (updateIndexTask.cancel(false)) {
      indexExecutor.schedule(updateIndex, 0, TimeUnit.SECONDS)
    }
  }

  def updateToolbox()= {
    signal.busying.get() match {
      case Some(true)=> signal.busying.add { busying=>
        if (!busying) {
          setup.Manager.installToolbox(toolbox)
        }
      }
      ()
      case _ => setup.Manager.installToolbox(toolbox)
    }
  }

  def updateModules()= {
    val task: Runnable= ()=> {
      // now in a new thread
      Try {
        model.nodes.foreach { node=>
          node.installed.foreach { current =>
            if (current != node.module.releases(0).version) {
              SwingUtilities.invokeAndWait { ()=>
                signal.incTask()
                fUninstall(node, false).thenAccept { r=>
                  r match {
                    case Success(0) =>
                      fInstall(node, false).thenAccept(_=> signal.decTask())
                    case _=> signal.decTask()
                  } } } } } } } }
    CompletableFuture.runAsync(task, updateExecutor)
  }

  def init()= ()

  def public()=
    signal.updated.pub(toolbox, modules)
}

