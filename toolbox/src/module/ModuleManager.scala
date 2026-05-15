package org.smaji.cjkv_toolbox.toolbox.module

import org.smaji.cjkv_toolbox.toolbox.*

import util.*

import org.smaji.cjkv_toolbox.toolbox.{CjkvDownloader, modulesDir}
import org.smaji.cjkv_toolbox.toolbox.config

import java.nio.file.{Path, Paths, Files}
import java.time
import java.io.StringReader
import scala.collection.immutable.ArraySeq
import java.time.OffsetDateTime
import javax.swing.SwingUtilities
import java.util.concurrent.CompletableFuture
import java.security.InvalidParameterException

/*
  Event
    1. updated
  Signal
    1. busying
 */
object Manager {
  val defaultInterval= 60*60*24 // 1 day
  if (config.Manager.update.interval <= 0) {
    config.Manager.update.interval= defaultInterval
  }
  val cjkvDownloader= CjkvDownloader()

  var toolbox: Module= null
  var modules= ArraySeq[Module]()

  val model= Model()
  val (batchUpdating, setBatchUpdating)= react.Signal.create(false)
  // indexUpdating is not needed since model.loadModuleInfo already update takes care of the model.busying singal
  val busying= react.Signal.anyMatch(model.busying, batchUpdating)

  import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

  val indexExecutor = Executors.newSingleThreadScheduledExecutor()

  model.requestEnableModule.map { (node, selected)=>
    val status= node.status; import status.*
    if (isInstalled || isBroken) && !selected then
      uninstall(node)
    else if (isUninstalled || isBroken) && selected then
      install(node)
  }

  def uninstall(node: model.Node)= {
    val f= CompletableFuture[Try[Int]]()
    node.status match {
      case model.Installed(version) =>
        node.status= model.Uninstalling(version)
        val unistallEvent= setup.Manager.uninstall(node.module)
        unistallEvent map { r =>
          r match {
            case Success(0) =>
              node.status= model.Uninstalled()
              config.Manager.modules=
                config.Manager.modules.removed(node.module.name)
            case Success(_) =>
              node.status= model.Broken()
              if debug then println("uninstall failed")
            case Failure(exception) =>
              node.status= model.Broken()
              println(exception)
          }
          f.complete(r)
        }
      case _=>
        f.complete(Try[Int](0))
    }
    f
  }

  def install(node: model.Node)= {
    val f= CompletableFuture[Try[Int]]()

    node.status match {
      case model.Uninstalled() =>
        val release= node.module.releases.head
        val installEvent= setup.Manager.install(release)
        node.status= model.Installing(release.version)
        installEvent map { r =>
          r match {
            case Success(0) =>
              val name= node.module.name
              val version= release.version
              val datetime= OffsetDateTime.now(zoneUTC)
              val moduleInfo= InstalledModuleInfo(name, version, datetime)
              node.status= model.Installed(release.version)
              config.Manager.modules=
                config.Manager.modules.updated(name, moduleInfo)
            case Success(_) =>
              node.status= model.Broken()
              if debug then println("install failed")
            case Failure(exception) =>
              node.status= model.Uninstalled()
              println(exception)
          }
          f.complete(r)
        }
      case _ =>
        f.complete(Failure(InvalidParameterException()))
    }
    f
  }

  def loadIndex()= {
    if debug then println("loadIndex")
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
    val transformer= xmlTransform.TransformerFactory.newInstance().newTransformer()
    val xpathEval= xpath.XPathFactory.newInstance().newXPath()

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
      val status=
        moduleInstalled
          .find((name, info)=>
            m.name == name && m.releases.exists(_.version == info.version))
          .map((name, info)=> info.version)
          match
            case Some(version)=> model.Installed(version)
            case None=> model.Uninstalled()
      model.Node(m, status)
    }
    model.loadModuleInfo(moduleNodes)

    if config.Manager.update.modules then
      updateModules().thenRun(()=>
        if config.Manager.update.toolbox then
          updateToolbox())
  }

  val updateIndex: Runnable= () => {
    cjkvDownloader.downloadAndExtract("/index.xml.tgz", modulesDir)
    loadIndex()
    val next= time.OffsetDateTime.now(zoneUTC).plusSeconds(config.Manager.update.interval)
    config.Manager.update.next= next
    updateIndexTask= indexExecutor.schedule(updateIndex, config.Manager.update.interval, TimeUnit.SECONDS)
  }

  var updateIndexTask=
    if ! Files.exists(modulesDir.resolve("index.xml")) then {
      indexExecutor.submit(updateIndex)
    } else {
      val next= config.Manager.update.next
      val now= time.OffsetDateTime.now(zoneUTC)
      if (next compareTo now) <= 0 then {
        indexExecutor.submit(updateIndex)
      } else {
        loadIndex()
        val duration= time.Duration.between(now, next)
        indexExecutor.schedule(updateIndex, duration.getSeconds(), TimeUnit.SECONDS)
      }
    }

  def resetTask(interval: Int = config.Manager.update.interval)= {
    updateIndexTask.cancel(true)
    if (updateIndexTask.isCancelled() || updateIndexTask.isDone()) {
      updateIndexTask= indexExecutor.schedule(updateIndex, interval, TimeUnit.SECONDS)
      val next= time.OffsetDateTime.now(zoneUTC).plusSeconds(interval)
      config.Manager.update.next= next
    }
  }

  def updateIndexNow()= {
    updateIndexTask.cancel(true)
    if (updateIndexTask.isCancelled() || updateIndexTask.isDone()) {
      updateIndexTask= indexExecutor.submit(updateIndex)
    }
  }

  def updateToolbox()= {
    val latest= toolbox.releases.head
    if (busying.get()) {
      busying.oneshot { busying=>
        if (!busying) {
          setup.Manager.installToolbox(latest)
        }
      }
      ()
    } else {
      setup.Manager.installToolbox(latest)
    }
  }

  def updateModules()= {
    val task: Runnable= ()=> {
      // now in a new thread
      setBatchUpdating(true)
      try
        model.modules.ordered.foreach { node=>
          node.status match {
            case model.Installed(current)=>
              if current != node.module.releases(0).version then
                uninstall(node).thenAccept { r=>
                  r match {
                    case Success(0) =>
                      install(node).wait()
                    case _=> ()
                  }
                }
            case _=> ()
          } }
      finally
        setBatchUpdating(false)
    }
    CompletableFuture.runAsync(task)
  }

  def init()= ()

}

