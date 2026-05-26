package org.smaji.cjkv_toolbox.installer

import java.nio.file.Paths

lazy val hostOs=
  val os= System.getProperty("os.name")
  if os.startsWith("Windows") then "windows"
  else if os.startsWith("Mac OS X") then "darwin"
  else os.toLowerCase()

lazy val jarPath=
  class Self {}
  val url= classOf[Self]
    .getProtectionDomain().getCodeSource()
    .getLocation().toString()

  val fileSchema= hostOs match
    case "windows" => "file:/"
    case _ => "file:"
  if url.startsWith(fileSchema) then
    Paths.get(url.drop(fileSchema.length))
  else
    Paths.get(url)

