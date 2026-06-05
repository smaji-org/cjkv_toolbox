/*
 * DomExt.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


package org.smaji.cjkv_toolbox.toolbox

import org.w3c.dom
import javax.xml.xpath
import org.w3c.dom.{Node,NodeList, DOMStringList}

import collection.*

private class NodeListIterOnce
  (val nodes: NodeList)
  extends IterableOnce[Node]
{
  override def knownSize: Int = nodes.getLength()

  def iterator= new AbstractIterator[Node] {
    private var current= 0
    def hasNext= current < nodes.getLength()
    def next(): Node= {
      val node= nodes.item(current)
      current+= 1
      node
    }
  }
}

private class DOMStringListIterOnce
  (val strings: DOMStringList)
  extends IterableOnce[java.lang.String]
{
  override def knownSize: Int = strings.getLength()

  def iterator= new AbstractIterator[java.lang.String] {
    private var current= 0
    def hasNext= current < strings.getLength()
    def next(): String= {
      val node= strings.item(current)
      current+= 1
      node
    }
  }
}

object DomOps {
  extension(nodeList: NodeList)
    def asScala=
      immutable.ArraySeq.from(new NodeListIterOnce(nodeList))

  extension(domStringList: DOMStringList)
    def asScala=
      immutable.ArraySeq.from(new DOMStringListIterOnce(domStringList))
}

def getOrCreateElem(name: String, node: Node)= {
  import XPathOps.*
  val xpathEval = xpath.XPathFactory.newInstance().newXPath()
  xpathEval.getNode(name, node) match {
    case elem: dom.Element => elem
    case null =>
      node
        .appendChild(node.getOwnerDocument().createElement(name))
        .asInstanceOf[dom.Element]
    case malformed =>
      node.removeChild(malformed)
      node
        .appendChild(node.getOwnerDocument().createElement(name))
        .asInstanceOf[dom.Element]
  }
}

