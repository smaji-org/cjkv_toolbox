/*
 * reset.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


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
