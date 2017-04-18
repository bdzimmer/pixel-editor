// Copyright (c) 2017 Ben Zimmer. All rights reserved.

// Edit a buffer of named things.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.BorderLayout
import javax.swing.{JButton, JOptionPane, JPanel, JToolBar, WindowConstants}

import bdzimmer.pixeleditor.model.TileCollectionModel.{Named, Settings}


abstract class BufferNamedWindow[T](
    title: String,
    val items: Buffer[Named[T]],
    settings: Settings) extends CommonWindow {

  setTitle(title)

  // chunks, updaters, and widgets
  val updaters = items.map(x => buildUpdater(x))
  val widgets = updaters.map(_.widget)

  val scrollPane = new WidgetScroller(widgets, selectable = true)

  build(WindowConstants.HIDE_ON_CLOSE)

  pack()
  setResizable(true)


  /// abstract stuff

  def buildUpdater(item: Named[T]): WidgetUpdater

  def buildItem(): Option[Named[T]]

  def editAction(idx: Int): Unit

  /////////////////////////////////////////

  def add(item: Named[T]): Unit = {
    val updater = buildUpdater(item)
    val widget = updater.widget
    items    += item
    updaters += updater
    widgets  += widget
  }


  def update(idx: Int, item: Named[T]): Unit = {
    val updater = buildUpdater(item)
    updater.widget.setSelected(widgets(idx).getSelected)
    updater.update()
    val widget = updater.widget
    items.update(idx, item)
    updaters.update(idx, updater)
    widgets.update(idx, widget)
  }


  def rebuild(): Unit = {
    scrollPane.rebuild()
    repaint()
  }


  //////////////////////////////////////////

  override def buildPanel(): JPanel = {
    val panel = new JPanel()
    panel.setLayout(new BorderLayout())
    panel.add(scrollPane, BorderLayout.CENTER)
    panel.add(scrollPane.scrollBar, BorderLayout.EAST)
    panel
  }


  override def buildToolBar(): JToolBar = {

    val mainToolbar = new JToolBar()

    val edit = new JButton("Edit")
    edit.addActionListener(new ActionListener() {
      def actionPerformed(event: ActionEvent): Unit = {
        val idx = scrollPane.getSelectedIdx
        if (idx >= 0 && idx < widgets.length) {
          editAction(idx)
        }
      }
    })
    edit.setFocusable(false)
    mainToolbar.add(edit)

    val rename = new JButton("Rename")
    rename.addActionListener(new ActionListener() {
      def actionPerformed(event: ActionEvent): Unit = {
        val idx = scrollPane.getSelectedIdx
        if (idx >= 0 && idx < widgets.length) {
          val item = items(idx)
          val newName = JOptionPane.showInputDialog(null, "Enter a new name:", item.name)
          if (newName != null && newName.length > 0) {
            val newItem = item.copy(name = newName)
            update(idx, newItem)
            rebuild()
          }
        }
      }
    })
    rename.setFocusable(false)
    mainToolbar.add(rename)

    val add = new JButton("Add")
    add.addActionListener(new ActionListener() {
      def actionPerformed(event: ActionEvent): Unit = {
        buildItem.foreach(x => {
          BufferNamedWindow.this.add(x)
          rebuild()
        })
      }
    })
    add.setFocusable(false)
    mainToolbar.add(add)

    mainToolbar.setFloatable(false)

    mainToolbar
  }


}

