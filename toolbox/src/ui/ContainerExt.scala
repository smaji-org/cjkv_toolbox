/*
 * ContainerExt.scala
 * -----------
 * Copyright : (c) 2025 - 2026, ZAN DoYe <zandoye@gmail.com>
 * Licence   : GPLv2
 *
 * This file is a part of smaji cjkv toolbox.
 */


package org.smaji.cjkv_toolbox.toolbox.ui

import java.awt.Container
import javax.swing.BoxLayout

object ContainerOps:
  extension(container: Container)
    def setLineBoxLayout()=
      container.setLayout(BoxLayout(container, BoxLayout.LINE_AXIS))
    def setPageBoxLayout()=
      container.setLayout(BoxLayout(container, BoxLayout.PAGE_AXIS))
