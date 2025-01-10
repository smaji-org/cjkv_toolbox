package org.smaji.cjkv_toolbox.toolbox

import javax.xml.xpath.XPathConstants.*
import org.w3c.dom.{Node,NodeList}

object XPathOps:
  extension(xpath: javax.xml.xpath.XPath)
    def getNode(expression: String, item: Node)=
      xpath.evaluate(expression, item, NODE).asInstanceOf[Node]

    def getNodeSet(expression: String, item: Node)=
      xpath.evaluate(expression, item, NODESET).asInstanceOf[NodeList]

    def getBoolean(expression: String, item: Node)=
      xpath.evaluate(expression, item, BOOLEAN).asInstanceOf[java.lang.Boolean]

    def getNumber(expression: String, item: Node)=
      xpath.evaluate(expression, item, NUMBER).asInstanceOf[java.lang.Double]

    def getString(expression: String, item: Node)=
      xpath.evaluate(expression, item, STRING).asInstanceOf[java.lang.String]

  extension(xpathExpr: javax.xml.xpath.XPathExpression)
    def getNode(item: Node)=
      xpathExpr.evaluate(item, NODE).asInstanceOf[Node]

    def getNodeSet(item: Node)=
      xpathExpr.evaluate(item, NODESET).asInstanceOf[NodeList]

    def getBoolean(item: Node)=
      xpathExpr.evaluate(item, BOOLEAN).asInstanceOf[java.lang.Boolean]

    def getNumber(item: Node)=
      xpathExpr.evaluate(item, NUMBER).asInstanceOf[java.lang.Double]

    def getString(item: Node)=
      xpathExpr.evaluate(item, STRING).asInstanceOf[java.lang.String]
