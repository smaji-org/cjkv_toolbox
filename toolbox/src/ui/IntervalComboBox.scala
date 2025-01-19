package org.smaji.cjkv_toolbox.toolbox

import util.*
import scala.collection.*
import scala.jdk.CollectionConverters.*

import java.awt
import java.awt.*
import javax.swing.*

class IntervalComboBox extends JComboBox[String] {
  val indexedInterval= mutable.Map[Int, Int]()
  val intervalIndex= mutable.Map[Int, Int]()

  addItem("6 hours")
  indexedInterval.update(0, 60*60*6)
  intervalIndex.update(60*60*6, 0)

  addItem("1 day")
  indexedInterval.update(1, 60*60*24)
  intervalIndex.update(60*60*24, 1)

  addItem("4 day")
  indexedInterval.update(2, 60*60*24*4)
  intervalIndex.update(60*60*24*4, 2)

  def addInterval(sec: Int)= {
    if (intervalIndex.get(sec).isEmpty) {
      addItem(s"$sec seconds")
      indexedInterval.update(indexedInterval.size, sec)
      intervalIndex.update(sec, intervalIndex.size)
    }
  }

  def setSelectedInterval(sec: Int)= {
    addInterval(sec)
    setSelectedIndex(intervalIndex(sec))
  }

  def getSelectedInterval()=
    indexedInterval(getSelectedIndex())
}
