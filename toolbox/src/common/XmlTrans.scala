package org.smaji.cjkv_toolbox.toolbox

import util.*
import java.io.{File, StringWriter, FileWriter}
import java.io.StringReader

import javax.xml.parsers as xmlParsers
import javax.xml.transform as xmlTransform
import org.w3c.dom
import javax.xml.xpath
import XPathOps.*

def xmlToString(xml: dom.Node)= {
  val transformer = xmlTransform.TransformerFactory.newInstance().newTransformer()
  val stringWriter= StringWriter()
  transformer.transform(
    xmlTransform.dom.DOMSource(xml),
    xmlTransform.stream.StreamResult(stringWriter))
  stringWriter.toString()
}

def xmlToFile(xml: dom.Node, file: File)=
  val transformer = xmlTransform.TransformerFactory.newInstance().newTransformer()
  Using(FileWriter(file)) { fileWriter =>
    transformer.transform(
      xmlTransform.dom.DOMSource(xml),
      xmlTransform.stream.StreamResult(fileWriter))
  }

