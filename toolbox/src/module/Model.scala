package org.smaji.cjkv_toolbox.toolbox.module

import javax.swing.*

import scala.jdk.CollectionConverters.*
import org.smaji.cjkv_toolbox.toolbox.react
import javax.swing.table.AbstractTableModel
import scala.collection.immutable.ArraySeq

sealed trait ModuleStatus {
  override def toString(): String =
    this match {
      case Uninstalled()=> ""
      case Installed(version)=> version
      case Uninstalling(version)=> s"${version}(installing)"
      case Installing(version)=> s"${version}(uninstalling)"
    }

  def isInstalled()=
    this match {
      case Installed(_)=> true
      case _=> false
    }

  def isUninstalled()=
    this match {
      case Uninstalled()=> true
      case _=> false
    }

  def isInstalling()=
    this match {
      case Installing(_)=> true
      case _=> false
    }

  def isUninstalling()=
    this match {
      case Uninstalling(_)=> true
      case _=> false
    }

  def isBusying()=
    this match {
      case Installing(_)=> true
      case Uninstalling(_)=> true
      case _=> false
    }
}
case class Uninstalled() extends ModuleStatus
case class Installed(version: String) extends ModuleStatus
case class Uninstalling(version: String) extends ModuleStatus
case class Installing(version: String) extends ModuleStatus

case class ModuleNode(val module: Module, var status: ModuleStatus)

class ModulesModel extends AbstractTableModel {
  type enable= Boolean
  case class Modules(nameMap: Map[String, ModuleNode], ordered: ArraySeq[ModuleNode], orderInfo: Map[ModuleNode, Int])

  val (event_enalbeModule, update_enableModule)= react.Event.create[(ModuleNode, enable)]()
  var modules= Modules(Map.empty, ArraySeq.empty, Map.empty)
  def loadModuleInfo(nodes: ArraySeq[ModuleNode])= {
    val ordered= nodes
    val named: Map[String, ModuleNode]= ordered.foldLeft(Map.empty)
      ((m, node)=> m.updated(node.module.name, node))
    val orderInfo: Map[ModuleNode, Int]= {
        var idx= -1
        ordered.foldLeft(Map.empty)
          ((m, node)=> { idx+=1; m.updated(node, idx) })
      }
    modules= Modules(named, ordered, orderInfo)
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

  def setModuleStatus(node: ModuleNode, status: ModuleStatus)=
    node.status= status
    val row= modules.orderInfo(node)
    fireTableCellUpdated(row, 3)
  def setModuleStatus(row: Int, status: ModuleStatus)=
    val node= modules.ordered(row)
    node.status= status
    fireTableCellUpdated(row, 3)

  override def getColumnName(col: Int): String=
    col match {
      case 0=> "Name"
      case 1=> "Description"
      case 2=> "Enable"
      case 3=> "Status"
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
  override def getRowCount()= modules.ordered.length
  override def getValueAt(row: Int, col: Int):
    java.lang.String | java.lang.Boolean=
  {
    if (row >=0 && row < modules.ordered.length) {
      col match {
        case 0=> modules.ordered(row).module.name
        case 1=> modules.ordered(row).module.description
        case 2=> modules.ordered(row).status.isInstalled()
        case 3=> modules.ordered(row).status.toString()
        case 4=> modules.ordered(row).module.releases(0).version
      }
    } else {
      ""
    }
  }
  override def isCellEditable(row: Int, col: Int): Boolean = {
    // col == 2 && row >=0 && row < nodes.length
    col == 2 && ! modules.ordered(row).status.isBusying()
  }
  override def setValueAt(value: Object, row: Int, col: Int): Unit = {
    if (col == 2) {
      update_enableModule(modules.ordered(row), value.asInstanceOf[Boolean])
    }
  }
}

