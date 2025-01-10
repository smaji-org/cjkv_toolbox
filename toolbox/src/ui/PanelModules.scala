package org.smaji.cjkv_toolbox.toolbox

import util.*
import scala.jdk.CollectionConverters.*

import java.awt
import java.awt.*
import javax.swing.*

import org.netbeans.swing.outline.*
import javax.swing.tree.TreePath
import javax.swing.event.{TreeModelListener, TreeModelEvent}

class ModuleSignal {
  object module {
    val enabled= Pub[Unit]()
    val disabled= Pub[Unit]()
  }
}

def createPanelModules(emHeight: Int, padding: Int)= {
  import border.*
  import ContainerOps.*

  val panelModules= JScrollPane()
  val modulesSignal= ModuleSignal()

  val paddingSet= awt.Insets(padding, padding, padding, padding)

  // panelModules.setLayout(GridLayout(0,1, 3,3))

  val (modulesModle, releaseSignal)= module.createModulesModel()
  module.Manager.signal.updated.add { (toolbox, modules)=>
    val root= modulesModle.getRoot().asInstanceOf[module.ModuleRoot]
    root.removeAllChildren()
    modules.foreach { m =>
      val moduleNode= module.ModuleNode(m)
      moduleNode.generateChildren().foreach { r => moduleNode.add(r) }
      root.add(moduleNode)
    }
  }


  val moduleOutline= Outline(modulesModle)
  moduleOutline.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS)

  panelModules.setViewportView(moduleOutline)

  releaseSignal.add { releaseNode =>
    moduleOutline.repaint()
  }

  (panelModules, modulesSignal)
}
