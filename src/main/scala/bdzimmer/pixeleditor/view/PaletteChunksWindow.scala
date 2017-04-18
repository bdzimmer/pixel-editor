// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import javax.swing.{JTextField, JComboBox, JOptionPane}

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.Color
import bdzimmer.pixeleditor.controller.TileUtil



class PaletteChunksWindow (
    title: String,
    items: Buffer[Named[Array[Color]]],
    settings: Settings)
  extends BufferNamedWindow[Array[Color]](title, items, settings) {


  override def buildUpdater(item: Named[Array[Color]]): WidgetUpdater = {
    new PaletteChunkUpdater(item, settings.bitsPerChannel, settings.viewPaletteCols)
  }


  override def buildItem(): Option[Named[Array[Color]]] = {
    val name = new JTextField("Pal Chunk " + items.length)
    val maxMultiple = settings.paletteSize / settings.colorsPerTile
    val size = new JComboBox((1 to maxMultiple).map(x => (x * settings.colorsPerTile).toString).toArray)

    val option = JOptionPane.showConfirmDialog(
        null, Array("Name:", name, "Size:", size), "Add Palette Chunk", JOptionPane.OK_CANCEL_OPTION)

    if (option == JOptionPane.OK_OPTION) {
      Some(Named(
        name.getText,
        TileUtil.colorArray((size.getSelectedIndex + 1) * settings.colorsPerTile)))
    } else {
      None
    }
  }


  override def editAction(idx: Int): Unit = {
     val item = items(idx)
     val editor = new PaletteWindow(item.name, item.value, settings.bitsPerChannel, updaters(idx))
     editor.setLocationRelativeTo(null) // TODO: set location from saved window location settings
     editor.setVisible(true)
  }


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

