/*
 * Info.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


package org.smaji.cjkv_toolbox.toolbox

import java.time

case class RepositoryInfo(val name: String, val url: String)
case class InstalledModuleInfo(val name: String, val version: String, val datetime: time.OffsetDateTime)

