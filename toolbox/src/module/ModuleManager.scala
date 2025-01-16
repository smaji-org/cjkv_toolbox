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

class ModuleSignal {
  val updated= Pub[(Module, ArraySeq[Module])]()
}

object Manager {
  val defaultInterval= 60*60*6 // 6 hours
  var updateInterval= config.Manager.update.interval
  if (updateInterval <= 0) {
    updateInterval= defaultInterval
    config.Manager.update.interval= updateInterval
  }
  val cjkvDownloader= CjkvDownloader()

  var toolbox: Module= null
  var modules= ArraySeq[Module]()

  val signal= ModuleSignal()

  import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

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

    signal.updated.pub(toolbox, modules)
  }

  val updateIndex: Runnable= () => {
    cjkvDownloader.downloadAndExtract("/index.xml.tgz", modulesDir)
    loadIndex()
    updateIndexTask= executor.schedule(updateIndex, updateInterval, TimeUnit.SECONDS)
  }

  val executor = Executors.newSingleThreadScheduledExecutor()

  var updateIndexTask=
    if Files.exists(modulesDir.resolve("index.xml")) then
      loadIndex()
      executor.schedule(updateIndex, updateInterval, TimeUnit.SECONDS)
    else
      executor.schedule(updateIndex, 0, TimeUnit.SECONDS)

  def updateNow()= {
    if (updateIndexTask.cancel(false)) {
      executor.schedule(updateIndex, 0, TimeUnit.SECONDS)
    }
  }

  def init()= ()
  def public()=
    signal.updated.pub(toolbox, modules)
}

