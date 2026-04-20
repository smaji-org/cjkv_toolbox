package org.smaji.cjkv_toolbox.toolbox

import util.*
import scala.jdk.CollectionConverters.*

import java.awt
import java.awt.*
import javax.swing.*

import java.time.OffsetDateTime

def createPanelModules(emHeight: Int, padding: Int)= {
  import border.*
  import ContainerOps.*

  val panelModules= JScrollPane()

  val paddingSet= awt.Insets(padding, padding, padding, padding)

  // panelModules.setLayout(GridLayout(0,1, 3,3))

  val modulesModel= module.Manager.model

  val moduleOutline= JTable(modulesModel)
  moduleOutline.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS)
  moduleOutline.setFillsViewportHeight(true)

  panelModules.setViewportView(moduleOutline)

  module.Manager.signal.busying map { busying=>
    moduleOutline.setEnabled(!busying)
  }

  (panelModules, modulesModel.eventEnalbeModule)
}
