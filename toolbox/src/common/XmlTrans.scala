/*
 * XmlTrans.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


package org.smaji.cjkv_toolbox.toolbox

import util.*
import java.io.{File, StringWriter, FileWriter}

import javax.xml.transform as xmlTransform
import org.w3c.dom

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

