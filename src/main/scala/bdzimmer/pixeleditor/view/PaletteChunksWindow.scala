// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import java.awt.event.{ActionEvent, ActionListener, FocusAdapter, FocusEvent, MouseAdapter, MouseEvent}
import java.awt.{GridLayout, BorderLayout, Dimension}
import javax.swing.{JButton, JPanel, JOptionPane, JToolBar, WindowConstants}

import bdzimmer.pixeleditor.model.TileCollectionModel.Settings
import bdzimmer.pixeleditor.model.ColorTriple


// eventually this will go in a more general place
trait WidgetUpdater {
  val widget: ImageWidget
  def update(): Unit
}


// a UI helper case class
case class PaletteChunk(val name: String, pal: Array[ColorTriple])


class PaletteChunksWindow(
    title: String,
    val chunks: Buffer[PaletteChunk],
    settings: Settings) extends CommonWindow {

  setTitle(title)

  // chunks, updaters, and widgets
  val updaters = chunks.map(chunk => new PaletteChunkUpdater(chunk, settings.bitsPerChannel, settings.viewPaletteColumns))
  val widgets = updaters.map(_.widget)

  val scrollPane = new WidgetScroller(widgets)

  build(WindowConstants.HIDE_ON_CLOSE)

  setFocusable(true)
  addFocusListener(new FocusAdapter() {
    override def focusGained(event: FocusEvent): Unit = {
      println("palette chunks window focus gained!");
      repaint()
    }
  })
  rebuild()

  pack()
  setResizable(false)


  /////////////////////////////////////////


  def add(chunk: PaletteChunk): Unit = {
    val updater = new PaletteChunkUpdater(chunk, settings.bitsPerChannel, settings.viewPaletteColumns)
    val widget = updater.widget
    chunks   += chunk
    updaters += updater
    widgets  += widget
  }


  def update(idx: Int, chunk: PaletteChunk): Unit = {
    val updater = new PaletteChunkUpdater(chunk, settings.bitsPerChannel, settings.viewPaletteColumns)
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

  override def panel(): JPanel = {
    val panel = new JPanel()
    panel.setLayout(new BorderLayout())
    panel.add(scrollPane, BorderLayout.CENTER)
    panel.add(scrollPane.scrollBar, BorderLayout.EAST)
    panel
  }


  override def toolBar(): JToolBar = {

    val mainToolbar = new JToolBar()

    val edit = new JButton("Edit")
    edit.addActionListener(new ActionListener() {
      def actionPerformed(event: ActionEvent): Unit = {
        val idx = scrollPane.getSelectedIdx
        if (idx < widgets.length) {
          val chunk = chunks(idx)
          val editor = new PaletteEditorNew(chunk.name, chunk.pal, 6, updaters(idx))
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
        if (idx < widgets.length) {
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
      chunk: PaletteChunk, bitsPerChannel: Int, cols: Int) extends WidgetUpdater {

    val rows = (chunk.pal.length + cols - 1) / cols
    val image = PaletteEditorNew.imageForPalette(
        chunk.pal.length, cols, PaletteChunksWindow.SwatchSize)

    draw()
    val widget = new ImageWidget(chunk.name, image, List(), 0, 24)

    /////

    def draw(): Unit = {
      println("PaletteChunkUpdater drawing " + chunk.name)
      PaletteEditorNew.drawPalette(
          image, chunk.pal, bitsPerChannel, rows, cols, PaletteChunksWindow.SwatchSize)
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

