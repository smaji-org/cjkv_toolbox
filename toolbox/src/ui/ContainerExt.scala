package org.smaji.cjkv_toolbox.toolbox.ui

import java.awt.Container
import javax.swing.BoxLayout

object ContainerOps:
  extension(container: Container)
    def setLineBoxLayout()=
      container.setLayout(BoxLayout(container, BoxLayout.LINE_AXIS))
    def setPageBoxLayout()=
      container.setLayout(BoxLayout(container, BoxLayout.PAGE_AXIS))
