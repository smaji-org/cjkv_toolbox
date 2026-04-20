package org.smaji.cjkv_toolbox.toolbox

object Main {
  def init()= {
    config.Manager.init()
    module.Manager.init()
  }

  def main(args: Array[String]): Unit= {
    init()
    MainWindow.main()
  }
}
