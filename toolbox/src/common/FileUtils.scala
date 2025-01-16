package org.smaji.cjkv_toolbox.toolbox

import java.io.File
import java.nio.file.Files
import scala.annotation.tailrec

def deleteDir(file: File): Unit= {
    val contents = file.listFiles()
    if (contents != null) {
        for (f <- contents) {
            if (! Files.isSymbolicLink(f.toPath())) {
                deleteDir(f)
            }
        }
    }
    file.delete()
}
