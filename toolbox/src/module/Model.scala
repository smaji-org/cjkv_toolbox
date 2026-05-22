package org.smaji.cjkv_toolbox.toolbox.module

import javax.swing.*

import scala.jdk.CollectionConverters.*
import org.smaji.cjkv_toolbox.toolbox.react
import javax.swing.table.AbstractTableModel
import scala.collection.immutable.ArraySeq

/*
  Type:
    1. Status
    2. Column
    3. Node
    4. Modules

  Method:
    1. setModuleStatus(node: Node, status: Status)
    2. loadModuleInfo(nodes: ArraySeq[Node])

  Event:
    1. requestEnalbeModule
    2. fireTableCellUpdated
    3. fireTableDataChanged
 */
class Model extends AbstractTableModel {
  sealed trait Status {
    override def toString(): String =
      this match {
        case Uninstalled()=> ""
        case Installed(release)=> release.version
        case Uninstalling(release)=> s"${release.version}(uninstalling)"
        case Installing(release)=> s"${release.version}(installing)"
        case Broken(release)=> s"${release.version}(broken)"
      }

    def isInstalled= isInstanceOf[Installed]
    def isUninstalled= isInstanceOf[Uninstalled]
    def isInstalling= isInstanceOf[Installing]
    def isUninstalling= isInstanceOf[Uninstalling]
    def isBroken= isInstanceOf[Broken]

    def isBusying()=
      this match {
        case Installing(_)=> true
        case Uninstalling(_)=> true
        case _=> false
      }
  }
  case class Uninstalled() extends Status
  case class Installed(version: Release) extends Status
  case class Uninstalling(version: Release) extends Status
  case class Installing(version: Release) extends Status
  case class Broken(version: Release) extends Status

  enum Column:
    case Name         extends Column
    case Description  extends Column
    case Status       extends Column
    case Latest       extends Column

  type Enable= Boolean
  case class Node private (val module: Module, status_ :Status) {
    private val (signal, update_status)= react.Signal.create[Status](status_)
    val statusSignal= signal
    def status= signal.value
    def status_=(s: Status)= {
      if s != status then
        update_status(s)
        modules.orderInfo.find((n,_)=> n == this) match
          case Some(_, row)=>
            if org.smaji.cjkv_toolbox.toolbox.swingInitialized then
              SwingUtilities.invokeLater(()=>
                fireTableCellUpdated(row, Column.Status.ordinal))
            else
              fireTableCellUpdated(row, Column.Status.ordinal)
          case None=> ()
    }
    override def hashCode(): Int = module.hashCode()
  }
  object Node {
    def apply(module: Module, status: Status= Uninstalled())= {
      val node= new Node(module, status)
      node
    }
  }

  case class Modules(nameMap: Map[String, Node], ordered: ArraySeq[Node], orderInfo: Map[Node, Int])
  var modules= Modules(Map.empty, ArraySeq.empty, Map.empty)

  private val (m_busying, update_busying)= react.Signal.create(false)
  private var busying_proxy= react.Signal.anyMatch[Node](_.status.isBusying())()
  busying_proxy.map(update_busying(_))
  val busying= m_busying

  def loadModuleInfo(nodes: ArraySeq[Node])= {
    val ordered= nodes
    val named: Map[String, Node]= ordered.foldLeft(Map.empty)
      ((m, node)=> m.updated(node.module.name, node))
    val orderInfo: Map[Node, Int]= {
        var idx= -1
        ordered.foldLeft(Map.empty)
          ((m, node)=> { idx+=1; m.updated(node, idx) })
      }
    modules= Modules(named, ordered, orderInfo)

    busying_proxy.reset()
    busying_proxy= react.Signal.anyMatch[Status]
      (_.isBusying())
      (ordered.map(_.statusSignal)*)
    busying_proxy.map(update_busying(_))
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
    if org.smaji.cjkv_toolbox.toolbox.swingInitialized then
      SwingUtilities.invokeLater(()=>fireTableDataChanged())
    else
      fireTableDataChanged()
  }

  def setRowStatus(row: Int, status: Status)=
    val node= modules.ordered(row)
    node.status= status

  override def getColumnName(col: Int): String=
    Column.fromOrdinal(col).toString()
  override def getColumnClass(col: Int): Class[?]=
    import java.lang.*
    import Column.*
    Column.fromOrdinal(col) match {
      case Name       => classOf[String]
      case Description=> classOf[String]
      case Status     => classOf[String]
      case Latest     => classOf[String]
    }
  override def getColumnCount()= Column.values.length
  override def getRowCount()= modules.ordered.length
  override def getValueAt(row: Int, col: Int):
    java.lang.String | java.lang.Boolean=
  {
    if (row >=0 && row < modules.ordered.length) {
      import Column.*
      Column.fromOrdinal(col) match {
        case Name       => modules.ordered(row).module.name
        case Description=> modules.ordered(row).module.description
        case Status     => modules.ordered(row).status.toString()
        case Latest     => modules.ordered(row).module.releases(0).version
      }
    } else {
      ""
    }
  }
  override def isCellEditable(row: Int, col: Int): Boolean = {
    false
  }
  override def setValueAt(value: Object, row: Int, col: Int): Unit = {
  }
}

