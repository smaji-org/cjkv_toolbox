package org.smaji.cjkv_toolbox.toolbox.module

import util.*
import control.TailCalls.*

import java.awt
import java.awt.{print as awtPrint, *}
import javax.swing.*
import tree.*

import org.netbeans.swing.outline.*
import scala.jdk.CollectionConverters.*
import org.smaji.cjkv_toolbox.toolbox.Pub
import javax.swing.table.AbstractTableModel
import scala.collection.immutable.ArraySeq

case class ModuleNode(val module: Module, var installed: Option[String])

class ModulesModel extends AbstractTableModel {
  val signal= Pub[(ModuleNode, Boolean)]()
  var nodes= ArraySeq[ModuleNode]()
  def loadModuleInfo(nodes: ArraySeq[ModuleNode])= {
    this.nodes= nodes
    /*
    nodes.foreach { node =>
      println(s"${node.module.name}:")
      node.module.releases.foreach { _.platforms.foreach { (os, archs) =>
        print(s"  ${os}:")
        archs.foreach(arch=> print(s" ${arch}"))
        println()
        }
      }
    }
    */
    fireTableDataChanged()
  }
  override def getColumnName(col: Int): String=
    col match {
      case 0=> "Name"
      case 1=> "Description"
      case 2=> "Enable"
      case 3=> "Installed"
      case 4=> "Latest"
    }
  override def getColumnClass(col: Int): Class[?]=
    import java.lang.*
    col match {
      case 0=> classOf[String]
      case 1=> classOf[String]
      case 2=> classOf[Boolean]
      case 3=> classOf[String]
      case 4=> classOf[String]
    }
  override def getColumnCount()= 5
  override def getRowCount()= nodes.length
  override def getValueAt(row: Int, col: Int):
    java.lang.String | java.lang.Boolean=
  {
    if (row >=0 && row < nodes.length) {
      col match {
        case 0=> nodes(row).module.name
        case 1=> nodes(row).module.description
        case 2=> nodes(row).installed.isDefined
        case 3=> nodes(row).installed getOrElse ""
        case 4=> nodes(row).module.releases(0).version
      }
    } else {
      ""
    }
  }
  override def isCellEditable(row: Int, col: Int): Boolean = {
    col == 2 && row >=0 && row < nodes.length
  }
  override def setValueAt(value: Object, row: Int, col: Int): Unit = {
    if (col == 2) {
      signal.pub(nodes(row), value.asInstanceOf[Boolean])
    }
  }
  /*
  def doneModuleSetup(name: String, installed: Option[String]): Unit = {
    nodes.find(_.module.name == name).foreach(_.installed= installed)
    fireTableDataChanged()
  }
  */
}

