package org.smaji.cjkv_toolbox.installer

lazy val hostOs= {
  System.getProperty("os.name") match
    case os if os.startsWith("Windows") => "windows"
    case os if os.startsWith("Mac OS X") => "darwin"
    case os => os.toLowerCase()
}

lazy val osSpecificFiles=
  hostOs match
    case "windows"=> Seq(
      "toolbox.exe",
      )
    case _=> Seq(
      "toolbox",
      )

lazy val fileList= Seq(
  "toolbox.jar",
  )
  ++ osSpecificFiles

object Main:
  import util.*
  import java.nio.file.{Files, Paths, Path, StandardCopyOption}

  def startToolbox(start: Path, dir: Path)=
    import scala.sys.process.*
    val p= Process(
      Seq(start.toString(), dir.resolve("toolbox.jar").toString()),
      dir.toFile()
      ).run()

  def makeCopy(srcRoot: Path, dstRoot: Path)(src: Path, dst: Path)=
    Files.copy(
      srcRoot.resolve(src),
      dstRoot.resolve(dst),
      StandardCopyOption.REPLACE_EXISTING)

  @mainargs.main
  def install(
    @mainargs.arg(short= 'm', doc = "")
    moduleDir: String= ".",
    @mainargs.arg(short= 't', doc = "")
    toolboxDir: String,
    @mainargs.arg(short= 'c', doc = "")
    configDir: String,
    @mainargs.arg(short= 'w', doc = "")
    wait: Int= 1,
    @mainargs.arg(short= 's', doc = "")
    startPath: Option[String],
    ) =
    val moduleDirPath= Paths get moduleDir
    val toolboxDirPath= Paths get toolboxDir
    val configDirPath= Paths get configDir
    val startPathResolved=
      startPath match
        case Some(path)=> Path.of(path)
        case None=> toolboxDirPath.resolve("cjkv_toolbox_start")
    val copy= makeCopy(moduleDirPath, toolboxDirPath)
    def copyFile(name: String)= copy(Path.of(name), Path.of(name))
    Thread.sleep(1000 * wait)
    fileList.foreach(copyFile)
    startToolbox(startPathResolved, toolboxDirPath)

  def main(args: Array[String]): Unit=
    mainargs.ParserForMethods(this).runOrExit(args.toIndexedSeq)

