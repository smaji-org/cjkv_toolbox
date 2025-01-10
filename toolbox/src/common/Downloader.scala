package org.smaji.cjkv_toolbox.toolbox

import util.*
import org.apache.commons.compress
import java.nio.file.{Files, Paths, Path, StandardCopyOption}
import java.nio.file.{FileAlreadyExistsException, FileSystemException}
import java.net.URL

def downloadTgzAndExtract(url: java.net.URL, output: Path)=
  Using.Manager { use =>
    val gzData= use(url.openStream())
    val gzStream= use(compress.compressors.gzip.GzipCompressorInputStream(gzData))
    val tarStream= use(compress.archivers.tar.TarArchiveInputStream(gzStream))

    if Files.notExists(output) then
      Files.createDirectories(output)

    var entry: compress.archivers.ArchiveEntry= null
    while {entry= tarStream.getNextEntry(); entry != null} do
      val extractTo = output.resolve(entry.getName())
      if entry.isDirectory() then
        Files.createDirectories(extractTo)
      else
        Files.copy(tarStream, extractTo, StandardCopyOption.REPLACE_EXISTING)
  }

class CjkvDownloader {
  val repo= if !debug then config.Manager.repositories.head else RepositoryInfo("debug", "http://localhost:8080")
  val repo_name= repo.name
  val repo_url= repo.url

  def downloadAndExtract(target: String, output: Path)= {
    downloadTgzAndExtract(URL(repo_url + target), output)
  }
}

