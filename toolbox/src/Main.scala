/*
 * Main.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


package org.smaji.cjkv_toolbox.toolbox

object Main {
  def init()= {
    config.Manager.init()
    module.Manager.init()
  }

  def main(args: Array[String]): Unit= {
    init()
    if args.length > 0 && args(0) == "reset" then
      reset()
    else
      ui.MainWindow.main()
  }
}
