package org.smaji.cjkv_toolbox.toolbox.config

object Manager {
  import org.smaji.cjkv_toolbox.toolbox.*
  import java.nio.file.Files
  import java.time
  import java.io.StringReader
  import collection.immutable.ArraySeq
  import javax.xml.parsers as xmlParsers
  import javax.xml.transform as xmlTransform
  import org.w3c.dom
  import javax.xml.xpath
  import XPathOps.*

  private case class CfgStartup(autostart: Boolean, minimized: Boolean)
  private val defaultStartup= CfgStartup(false, false)

  private type Second= Int
  private case class CfgUpdate(interval: Second, toolbox: Boolean, modules: Boolean)
  private val defaultUpdate= CfgUpdate(345600, true, true)

  private val defaultRepo= RepositoryInfo("smaji", "https://cjkv.smaji.org/static/repo")
  private val defaultStartupAutostart= true
  private val defaultConfig=
    s"""<?xml version="1.0" encoding="UTF-8" ?>
       |<toolbox>
       |  <version>${version}</version>
       |  <ui>
       |    <lang></lang>
       |    <scale>1</scale>
       |  </ui>
       |  <repositories>
       |    <repository>
       |      <name>${defaultRepo.name}</name>
       |      <url>${defaultRepo.url}</url>
       |    </repository>
       |  </repositories>
       |  <startup>
       |    <autostart>${defaultStartup.autostart}</autostart>
       |    <minimized>${defaultStartup.minimized}</minimized>
       |  </startup>
       |  <update>
       |    <interval>${defaultUpdate.interval}</interval>
       |    <toolbox>${defaultUpdate.toolbox}</toolbox>
       |    <modules>${defaultUpdate.modules}</modules>
       |  </update>
       |  <modules>
       |  </modules>
       |</toolbox>
       |""".stripMargin

  private val toolboxPath= configDir.resolve("toolbox.xml")
  private val toolboxFile= toolboxPath.toFile()
  private val xmlBuilder= xmlParsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
  private val transformer = xmlTransform.TransformerFactory.newInstance().newTransformer()
  private val xpathEval = xpath.XPathFactory.newInstance().newXPath()


  /*
  if !toolboxFile.exists() then
    Using(Files.newBufferedWriter(toolboxPath)){_ write defaultConfig}
  */

  private val toolboxCfg=
    if (toolboxFile.exists()) {
      xmlBuilder.parse(toolboxFile)
    } else {
      xmlBuilder.parse(org.xml.sax.InputSource(StringReader(defaultConfig)))
    }

  private val elemToolbox= getOrCreateElem("toolbox", toolboxCfg)

  private val elemRepositories= getOrCreateElem("repositories", elemToolbox)
  private val elemUi= getOrCreateElem("ui", elemToolbox)
  private val elemStartup= getOrCreateElem("startup", elemToolbox)
  private val elemUpdate= getOrCreateElem("update", elemToolbox)
  private val elemModules= getOrCreateElem("modules", elemToolbox)

  var autoSync= true
  def sync()=
    xmlToFile(toolboxCfg, toolboxFile)

  def init()= {
    Files.createDirectories(configDir)
    sync()
  }

  def repositories= {
    import DomOps.*
    try {
      xpathEval.getNodeSet("repositories/repository", elemToolbox) match
        case null=> ArraySeq(defaultRepo)
        case nodeList: dom.NodeList =>
          nodeList.asScala.map { repo =>
            val name= xpathEval.getNode("name", repo).getTextContent()
            val url= xpathEval.getNode("url", repo).getTextContent()
            RepositoryInfo(name, url)
          }
    } catch _ => {
      ArraySeq(defaultRepo)
    }
  }

  def repositories_=(repositories: ArraySeq[RepositoryInfo])= {
    val elemRepositories= toolboxCfg.createElement("repositories")
    for (repository <- repositories) {
      val elemRepository= toolboxCfg.createElement("repository")
      val elemName= toolboxCfg.createElement("name")
      elemName.setTextContent(repository.name)
      val elemUrl= toolboxCfg.createElement("url")
      elemUrl.setTextContent(repository.url)
      elemRepository.appendChild(elemName)
      elemRepository.appendChild(elemUrl)
      elemRepositories.appendChild(elemRepository)
    }
    xpathEval.getNode("repositories", elemToolbox) match {
      case null=>
        toolboxCfg.getDocumentElement().appendChild(elemRepositories)
      case repositories: org.w3c.dom.Element=>
        toolboxCfg.getDocumentElement().replaceChild(elemRepositories, repositories)
    }
    if autoSync then sync()
  }

  def modules: collection.immutable.SeqMap[String, InstalledModuleInfo]= {
    import collection.immutable.SeqMap
    import DomOps.*
    try {
      xpathEval.getNodeSet("modules/module", elemToolbox) match {
        case null=> SeqMap.empty
        case nodeList: dom.NodeList => {
          SeqMap.from(
            nodeList.asScala.map { module =>
              val name= xpathEval.getNode("name", module).getTextContent()
              val version= xpathEval.getNode("version", module).getTextContent()
              val datetime= time.OffsetDateTime.parse(
                xpathEval.getNode("datetime", module).getTextContent())
              name -> InstalledModuleInfo(name, version, datetime)
            })
        }
      }
    } catch _ => {
      SeqMap.empty
    }
  }

  def modules_=(modules: collection.immutable.SeqMap[String, InstalledModuleInfo])= {
    val elemModules= toolboxCfg.createElement("modules")
    for ((name -> module) <- modules) {
      val elemModule= toolboxCfg.createElement("module")
      val elemName= toolboxCfg.createElement("name")
      elemName.setTextContent(module.name)
      val elemVersion= toolboxCfg.createElement("version")
      elemVersion.setTextContent(module.version)
      val elemDatetime= toolboxCfg.createElement("datetime")
      elemDatetime.setTextContent(module.datetime.toString)
      elemModule.appendChild(elemName)
      elemModule.appendChild(elemVersion)
      elemModule.appendChild(elemDatetime)
      elemModules.appendChild(elemModule)
    }
    xpathEval.getNode("modules", elemToolbox) match {
      case null=>
        toolboxCfg.getDocumentElement().appendChild(elemModules)
      case modules: org.w3c.dom.Element=>
        toolboxCfg.getDocumentElement().replaceChild(elemModules, modules)
    }
    if autoSync then sync()
  }

  def getScaleFromEnv(): Int=
    try {
      System.getenv("GDK_SCALE").toFloat.round
    } catch _=> {
      try {
        System.getenv("QT_AUTO_SCREEN_SCALE_FACTOR").split(";")(0).split("=")(1).toFloat.round
      } catch _ => {
        1
      }
    }

  object ui {
    def lang=
      getOrCreateElem("lang", elemUi).getTextContent().trim
    def lang_=(lang: String)=
      getOrCreateElem("lang", elemUi).setTextContent(lang)
      if autoSync then sync()

    def scale: Int=
      getOrCreateElem("scale", elemUi).getTextContent().trim match
        case "" => getScaleFromEnv()
        case scale => scale.toFloat.round
    def scale_=(scale: Int)=
      getOrCreateElem("scale", elemUi).setTextContent(scale.toString())
      if autoSync then sync()
  }

  object startup {
    def autostart=
      getOrCreateElem("autostart", elemStartup).getTextContent().toBoolean
    def autostart_=(set: Boolean)=
      getOrCreateElem("autostart", elemStartup).setTextContent(set.toString())
      if autoSync then sync()

    def minimized=
      getOrCreateElem("minimized", elemStartup).getTextContent().toBoolean
    def minimized_=(set: Boolean)=
      getOrCreateElem("minimized", elemStartup).setTextContent(set.toString())
      if autoSync then sync()
  }

  object update {
    def interval=
      getOrCreateElem("interval", elemUpdate).getTextContent().toInt
    def interval_=(interval: Second)=
      getOrCreateElem("interval", elemUpdate).setTextContent(interval.toString())
      if autoSync then sync()

    def next=
      getOrCreateElem("next", elemUpdate).getTextContent().trim() match
        case "" => time.OffsetDateTime.now(zoneUTC)
        case content => time.OffsetDateTime.parse(content)
    def next_=(next: time.OffsetDateTime)=
      getOrCreateElem("next", elemUpdate).setTextContent(next.toString())
      if autoSync then sync()

    def toolbox=
      getOrCreateElem("toolbox", elemUpdate).getTextContent().toBoolean
    def toolbox_=(set: Boolean)=
      getOrCreateElem("toolbox", elemUpdate).setTextContent(set.toString())
      if autoSync then sync()

    def modules=
      getOrCreateElem("modules", elemUpdate).getTextContent().toBoolean
    def modules_=(set: Boolean)=
      getOrCreateElem("modules", elemUpdate).setTextContent(set.toString())
      if autoSync then sync()
  }
}

