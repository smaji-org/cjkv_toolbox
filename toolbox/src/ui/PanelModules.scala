package org.smaji.cjkv_toolbox.toolbox.ui

import org.smaji.cjkv_toolbox.toolbox
import toolbox.t

import util.*
import scala.jdk.CollectionConverters.*

import java.awt
import java.awt.*
import javax.swing.*

import java.time.OffsetDateTime


def createPanelModules(emHeight: Int, padding: Int)= {
  import org.smaji.cjkv_toolbox.toolbox.module
  import javax.swing.table.TableCellRenderer

  val panelModules= JScrollPane()

  val paddingSet= awt.Insets(padding, padding, padding, padding)

  // panelModules.setLayout(GridLayout(0,1, 3,3))

  val modulesModel= module.Manager.model

  val moduleOutline= new JTable(modulesModel) {
    val self= this
    def descriptionRenderer= new TableCellRenderer {
      import toolbox.locale
      override def getTableCellRendererComponent(table: JTable, value: Object, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component =
        val description= value.asInstanceOf[module.Description]
        val content=
          description.lang.find((name, _)=> name.toLowerCase(locale) == locale.getLanguage().toLowerCase(locale)) match
            case None => description.default
            case Some(_, lang) =>
              lang.region.find((region, _)=> region.toLowerCase(locale) == locale.getCountry().toLowerCase(locale)) match
                case None => lang.default
                case Some(_, content)=> content
        self.getDefaultRenderer(classOf[String]).getTableCellRendererComponent(table, content, isSelected, hasFocus, row, column)
    }
    override def getCellRenderer(row: Int, column: Int): TableCellRenderer =
      if module.Manager.model.Column.fromOrdinal(column) == module.Manager.model.Column.Description then
        descriptionRenderer
      else
        super.getCellRenderer(row, column)
  }

  moduleOutline.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS)
  moduleOutline.setFillsViewportHeight(true)

  panelModules.setViewportView(moduleOutline)

  module.Manager.busying map { busying=>
    moduleOutline.setEnabled(!busying)
  }

  import javax.swing.event.MouseInputAdapter
  import java.awt.event.MouseEvent
  moduleOutline.addMouseListener(new MouseInputAdapter {
    override def mouseClicked(mouse: MouseEvent)= {
      if mouse.getButton() == MouseEvent.BUTTON3 then
        val point= mouse.getPoint()
        val row= moduleOutline.rowAtPoint(point)
        val node= module.Manager.model.modules.ordered(row)
        val next= node.module
        var menu= node.status match
          case modulesModel.Uninstalled()=>
            val menu= JPopupMenu("Uninstalled")
            val itemInstall= JMenuItem(t("Install"))
            itemInstall.addActionListener(_=>
              module.Manager.install(node))
            menu.add(itemInstall)
            Some(menu)
          case modulesModel.Installed(_)=>
            if toolbox.setup.Manager.setuper(node.module).isDefined then
              val menu= JPopupMenu("InstalledWithSetup")
              val itemUpdate= JMenuItem(t("Update"))
              itemUpdate.addActionListener(_=>
                module.Manager.update(node))
              val itemSetup= JMenuItem(t("Setup"))
              itemSetup.addActionListener(_=>
                module.Manager.setup(node))
              val itemUninstall= JMenuItem(t("Uninstall"))
              itemUninstall.addActionListener(_=>
                module.Manager.uninstall(node))
              menu.add(itemUpdate)
              menu.add(itemSetup)
              menu.add(itemUninstall)
              Some(menu)
            else
              val menu= JPopupMenu("Installed")
              val itemUpdate= JMenuItem(t("Update"))
              itemUpdate.addActionListener(_=>
                module.Manager.update(node))
              val itemUninstall= JMenuItem(t("Uninstall"))
              itemUninstall.addActionListener(_=>
                module.Manager.uninstall(node))
              menu.add(itemUpdate)
              menu.add(itemUninstall)
              Some(menu)
          case _=> None
        menu foreach (_.show(mouse.getComponent(), point.x, point.y))
    }
  })
  panelModules
}
