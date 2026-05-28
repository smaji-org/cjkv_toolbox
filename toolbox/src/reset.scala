package org.smaji.cjkv_toolbox.toolbox

import java.util.concurrent.CompletableFuture

def reset()=
  startup.Manager.autostart.setAutostart(false)
  val clearUp= module.Manager.model.modules.ordered map
    module.Manager.uninstall
  val waitFor= CompletableFuture.allOf(clearUp*)
  waitFor.get()
  deleteDir(configDir.toFile())
  System.exit(0)
