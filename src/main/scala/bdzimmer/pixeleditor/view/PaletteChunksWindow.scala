// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import java.awt.event.{ActionEvent, ActionListener, FocusAdapter, FocusEvent, MouseAdapter, MouseEvent}
import java.awt.{GridLayout, BorderLayout, Dimension}
import javax.swing.{JButton, JPanel, JOptionPane, JToolBar, WindowConstants}

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.Color



class PaletteChunksWindow(
    title: String,
    val chunks: Buffer[Named[Array[Color]]],
    settings: Settings) extends CommonWindow {

  setTitle(title)

  // chunks, updaters, and widgets
  val updaters = chunks.map(chunk => new PaletteChunkUpdater(chunk, settings.bitsPerChannel, settings.viewPaletteCols))
  val widgets = updaters.map(_.widget)

  val scrollPane = new WidgetScroller(widgets, selectable = true)

  build(WindowConstants.HIDE_ON_CLOSE)

  pack()
  setResizable(false)

  /////////////////////////////////////////

  def add(chunk: Named[Array[Color]]): Unit = {
    val updater = new PaletteChunkUpdater(chunk, settings.bitsPerChannel, settings.viewPaletteCols)
    val widget = updater.widget
    chunks   += chunk
    updaters += updater
    widgets  += widget
  }


  def update(idx: Int, chunk: Named[Array[Color]]): Unit = {
    val updater = new PaletteChunkUpdater(chunk, settings.bitsPerChannel, settings.viewPaletteCols)
    updater.widget.setSelected(widgets(idx).getSelected)
    updater.update()
    val widget = updater.widget
    chunks.update(idx, chunk)
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
          val chunk = chunks(idx)
          val editor = new PaletteWindow(chunk.name, chunk.value, settings.bitsPerChannel, updaters(idx))
          editor.setLocationRelativeTo(null)
          editor.setVisible(true)
        }

      }
    })
    edit.setFocusable(false)

    val rename = new JButton("Rename")
    rename.addActionListener(new ActionListener() {
      def actionPerformed(event: ActionEvent): Unit = {
        val idx = scrollPane.getSelectedIdx
        if (idx >= 0 && idx < widgets.length) {
          val chunk = chunks(idx)
          val newName = JOptionPane.showInputDialog(null, "Enter a new name:", chunk.name)
          if (newName != null && newName.length > 0) {
            val newChunk = chunk.copy(name = newName)
            PaletteChunksWindow.this.update(idx, newChunk)
            PaletteChunksWindow.this.rebuild()
          }
        }
      }
    })
    rename.setFocusable(false)

    mainToolbar.add(edit)
    mainToolbar.add(rename)
    mainToolbar.setFloatable(false)

    mainToolbar
  }


  /////////////////////////////////////////////

  // describes how to create a widget that shows a palette chunk with an edit button
  // that can be efficiently updated by the palette editor

  class PaletteChunkUpdater(
      chunk: Named[Array[Color]], bitsPerChannel: Int, cols: Int) extends WidgetUpdater {

    val rows = (chunk.value.length + cols - 1) / cols
    val image = PaletteWindow.imageForPalette(
        chunk.value.length, cols, PaletteChunksWindow.SwatchSize)

    draw()
    val widget = new ImageWidget(chunk.name, image, List(), 0, 24)

    /////

    def draw(): Unit = {
      println("PaletteChunkUpdater draw " + chunk.name)
      PaletteWindow.drawPalette(
          image, chunk.value, bitsPerChannel, rows, cols, PaletteChunksWindow.SwatchSize)
    }

    def update(): Unit = {
      println("PaletteChunkUpdater update " + chunk.name)
      draw()
      widget.repaint()
    }
  }

}


object PaletteChunksWindow {
  val SwatchSize = 16
}

