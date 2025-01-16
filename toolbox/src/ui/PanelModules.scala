package org.smaji.cjkv_toolbox.toolbox
import org.smaji.cjkv_toolbox.*

import util.*
import scala.jdk.CollectionConverters.*

import java.awt
import java.awt.*
import javax.swing.*

import org.netbeans.swing.outline.*
import javax.swing.tree.TreePath
import javax.swing.event.{TreeModelListener, TreeModelEvent}
import java.time.OffsetDateTime

def createPanelModules(emHeight: Int, padding: Int)= {
  import border.*
  import ContainerOps.*

  val panelModules= JScrollPane()

  val paddingSet= awt.Insets(padding, padding, padding, padding)

  // panelModules.setLayout(GridLayout(0,1, 3,3))

  val modulesModle= module.ModulesModel()
  module.Manager.signal.updated.add { (toolbox, modules)=>
    val moduleInstalled= config.Manager.modules
    val moduleNodes= modules.map { m =>
      val installedVersion= moduleInstalled
        .find((name, info)=>
          m.name == name && m.releases.exists(_.version == info.version))
        .map((name, info)=> info.version)
      module.ModuleNode(m, installedVersion)
    }
    modulesModle.loadModuleInfo(moduleNodes)
  }

  val moduleOutline= JTable(modulesModle)
  moduleOutline.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS)

  panelModules.setViewportView(moduleOutline)

  modulesModle.signal.add { (node, install)=>
    if (node.installed.isDefined && !install) {
      println("start uninstall")
      val signal= setup.Manager.uninstall(node)
      signal.add { r =>
        r match {
          case Failure(exception) =>
            println(exception)
          case Success(0) =>
            node.installed= None
            modulesModle.fireTableDataChanged()
            config.Manager.modules=
              config.Manager.modules.removed(node.module.name)
          case Success(_) => ()
        }
      }
    } else if (node.installed.isEmpty && install) {
      println("start install")
      val release= node.module.releases.head
      val signal= setup.Manager.install(release)
      signal.add { r =>
        r match {
          case Failure(exception) =>
            println(exception)
          case Success(0) =>
            val name= node.module.name
            val version= release.version
            val datetime= OffsetDateTime.now(zoneUTC)
            val moduleInfo= InstalledModuleInfo(name, version, datetime)
            node.installed= Some(release.version)
            modulesModle.fireTableDataChanged()
            config.Manager.modules=
              config.Manager.modules.updated(name, moduleInfo)
          case Success(_) => ()
        }
      }
    }
    // modulesModle.doneModuleSetup(node.module.name, if install then Some("hi") else None)
  }

  (panelModules, modulesModle.signal)
}
