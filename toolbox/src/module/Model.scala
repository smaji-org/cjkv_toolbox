package org.smaji.cjkv_toolbox.toolbox.module

import util.*
import control.TailCalls.*

import java.awt
import java.awt.*
import javax.swing.*
import tree.*

import org.netbeans.swing.outline.*
import scala.jdk.CollectionConverters.*
import org.smaji.cjkv_toolbox.toolbox.Pub


class ModuleNode(val module: Module)
  extends DefaultMutableTreeNode
{
  def releaseInfos()= children()
    .asInstanceOf[java.util.Enumeration[ReleaseNode]]
    .asScala

  def isInstalled()= releaseInfos().exists(_.installed)

  def installedVersion()= releaseInfos()
    .find(_.installed)
    .map(_.release.version)
    .getOrElse("")

  def generateChildren()= module.releases.map(ReleaseNode(_))
}

class ReleaseNode(val release: Release, var installed: Boolean= false)
  extends DefaultMutableTreeNode
{
}

class ModuleRoot()
  extends DefaultMutableTreeNode
{
  def add(module: ModuleNode)= {
    super.add(module)
  }

  override def getChildAfter(child: TreeNode): ModuleNode= {
    super.getChildAfter(child).asInstanceOf[ModuleNode]
  }

  override def getChildAt(index: Int): ModuleNode= {
    super.getChildAt(index).asInstanceOf[ModuleNode]
  }

  override def getChildBefore(child: TreeNode): ModuleNode= {
    super.getChildBefore(child).asInstanceOf[ModuleNode]
  }

  override def getFirstChild(): ModuleNode= {
    super.getFirstChild().asInstanceOf[ModuleNode]
  }

  override def getFirstLeaf(): ModuleNode | ReleaseNode= {
    super.getFirstLeaf().asInstanceOf[ModuleNode | ReleaseNode]
  }

  override def getLastChild(): ModuleNode= {
    super.getLastChild().asInstanceOf[ModuleNode]
  }

  override def getLastLeaf(): ModuleNode | ReleaseNode= {
    super.getLastLeaf().asInstanceOf[ModuleNode | ReleaseNode]
  }

  override def getNextLeaf(): ModuleNode | ReleaseNode= {
    super.getNextLeaf().asInstanceOf[ModuleNode | ReleaseNode]
  }

  override def getPreviousLeaf(): ModuleNode | ReleaseNode= {
    super.getPreviousLeaf().asInstanceOf[ModuleNode | ReleaseNode]
  }

  override def getRoot(): ModuleRoot = {
    this
  }
}

class InfoRowModel extends RowModel {
  val signal= Pub[ReleaseNode]()

  def getColumnClass(column: Int)= {
    column match
      case 0 => classOf[java.lang.String]
      case 1 => classOf[java.lang.String]
      case 2 => classOf[java.lang.Boolean]
      case 3 => classOf[java.lang.String]
      case _ => classOf[java.lang.Object]
  }

  def getColumnCount()= 4

  def getColumnName(column: Int)= {
    column match
      case 0 => "Name"
      case 1 => "Description"
      case 2 => "Installed"
      case 3 => "Installed version"
      case _ => ""
  }

  def getValueFor(node: Object, column: Int)= {
    node match
    case node: ModuleNode =>
      val module= node.module
      column match
        case 0 => module.name
        case 1 => module.description
        case 2 => node.isInstalled()
        case 3 => node.installedVersion()
    case node: ReleaseNode =>
      val release= node.release
      column match
        case 0 => release.dateTime.toString()
        case 1 => release.comment
        case 2 => node.installed
        case 3 => release.version
    case node: ModuleRoot => null
  }

  def isCellEditable(node: Object , column: Int)= {
    !node.isInstanceOf[ModuleRoot] && (column == 2)
  }

  def setValueFor(node: Object, column: Int, value: Object)= {
    node match
    case node: ModuleNode =>
      val module= node.module
      column match
        case 2 => 
          if !node.isInstalled() then
            val infos= node.releaseInfos().toSeq
            infos.unapply(0).foreach { node =>
              node.installed= true
              signal.pub(node)
            }
    case node: ReleaseNode =>
      val release= node.release
      column match
        case 2 => node.installed= value.asInstanceOf[Boolean]
        signal.pub(node)
  }
}

def createModulesModel()= {

  val root= ModuleRoot()
  val treeModel= DefaultTreeModel(root)
  val infoRowModel= InfoRowModel()

  val model= DefaultOutlineModel.createOutlineModel(
    treeModel, 
    infoRowModel,
    false)

  (model, infoRowModel.signal)
}

