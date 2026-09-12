/*
 * package.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


package org.smaji.cjkv_toolbox.toolbox.module
import org.smaji.cjkv_toolbox.toolbox.{zoneUTC}


import java.time.{LocalDateTime, OffsetDateTime}
import scala.util.*
import collection.immutable.ArraySeq

type ArchName= String
type OsName= String
type ArchSet= Set[ArchName]
type OsSet= Set[OsName]
type OsHostMap= Map[OsName, ArchSet]

case class Release(
  val module: Module,
  val version: String,
  val dateTime: OffsetDateTime=
    OffsetDateTime.now(zoneUTC),
  val comment: String= "",
  val data: Option[String]= None,
  val platforms: OsHostMap,
  )
{
  private val version_desc= version match
    case "" | null => Array[String]()
    case version => version.split("\\.")
  val major= version_desc.unapply(0).getOrElse("0").toInt
  val minor= version_desc.unapply(1).getOrElse("0").toInt
  val patch= version_desc.unapply(2).getOrElse("0").toInt
  val suffix= version_desc.unapply(3).getOrElse("0").toInt
  override def toString(): String = version
  override def equals(that: Any)=
    that match
      case r: Release=>
        module.name == r.module.name
        && version == r.version
        && dateTime == r.dateTime
      case _ => false
}

case class Module(val name: String, val description: Description, var releases: ArraySeq[Release]) {
  override def hashCode(): Int =
    ((name + description.default).hashCode() + releases.length).hashCode()
}

