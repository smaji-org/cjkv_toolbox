package org.smaji.cjkv_toolbox.toolbox

import java.time

case class RepositoryInfo(val name: String, val url: String)
case class InstalledModuleInfo(val name: String, val version: String, val datetime: time.OffsetDateTime)

