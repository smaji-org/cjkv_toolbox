package org.smaji.cjkv_toolbox.upgrader

object Main:
  import util.*
  import org.apache.commons.compress
  import java.nio.file.{Files, Paths, Path, StandardCopyOption}
  import java.nio.file.{FileAlreadyExistsException, FileSystemException}

  def main(args: Array[String]): Unit=
    mainargs.ParserForMethods(this).runOrExit(args.toIndexedSeq)

  def downloadAndExtract(url: java.net.URL, output: Path)=
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

  def startToolbox(start: String, dir: Path)=
    import scala.sys.process.*
    val p= Process(Seq(start, dir.resolve("toolbox.jar").toString())).run()

  @mainargs.main
  def upgrade(
    @mainargs.arg(short= 'l', doc = "try to download <url>")
    url: String,
    @mainargs.arg(short= 'o', doc = "save and extract the file to the <directory>")
    output: String= ".",
    @mainargs.arg(short= 's', doc = "the <path> of the start program")
    start: String,
    @mainargs.arg(short= 'w', doc = "delay for <second> seconds before upgrading")
    wait: Int= 1)
  =
    val outputPath= Paths.get(output)
    Thread.sleep(1000 * wait)
    downloadAndExtract(java.net.URL(url), outputPath)
    match
      case Failure(e) => System.err.printf("Error: %s\n", e.toString())
      case Success(()) => startToolbox(start, outputPath)

  @mainargs.main
  def download(
    @mainargs.arg(short= 'l', doc = "try to download <url>")
    url: String,
    @mainargs.arg(short= 'o', doc = "save and extract the file to the <directory>")
    output: String= ".")
  =
    downloadAndExtract(java.net.URL(url), Paths.get(output))

