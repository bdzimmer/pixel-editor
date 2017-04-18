// Copyright (c) 2017 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import javax.swing.{JTextField, JComboBox, JOptionPane}

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, TileProperties, TileContainer}
import bdzimmer.pixeleditor.controller.TileUtil



class VMapsWindow(
    title: String,
    items: Buffer[Named[VMap]],
    pixels: Pixels,
    paletteChunks: Buffer[Named[Array[Color]]],
    globalPalette: Array[Color],
    globalPaletteUpdater: Updater,
    tileContainer: TileContainer,
    zoomWindow: ZoomedTileWindow,
    settings: Settings)
  extends BufferNamedWindow[VMap](title, items, settings) {


  override def buildUpdater(item: Named[VMap]): WidgetUpdater = {
    new VMapUpdater(item)
  }


  override def buildItem(): Option[Named[VMap]] = {
    val name = new JTextField("VMap " + items.length)

    val option = JOptionPane.showConfirmDialog(
        null, Array("Name:", name), "Add VMap", JOptionPane.OK_CANCEL_OPTION)

    if (option == JOptionPane.OK_OPTION) {
      Some(Named(
        name.getText,
        VMap(Buffer(), Array.fill(settings.vMapSize)(new VMapEntry(0, 0, false, false, TileProperties(0))))))
    } else {
      None
    }
  }


  override def editAction(idx: Int): Unit = {
     val item = items(idx)
     val editor = new VMapWindow(
         item.name, item.value, pixels, paletteChunks,
         globalPalette, globalPaletteUpdater, tileContainer, zoomWindow, settings)
     editor.setLocationRelativeTo(null) // TODO: set location from saved window location settings
     editor.setVisible(true)
  }


  class VMapUpdater(vmap: Named[VMap]) extends WidgetUpdater {

    // TODO: use name eventually
    // TODO: use colors specified in first palette chunk, not globalPalette
    val image = new TilesetImage(vmap.value.entries, pixels.tiles, globalPalette, 1, settings)

    draw()
    val widget = new ImageWidget("", image.indexedGraphics.getImage, List(), 0, 0)

    def draw(): Unit = {
      println("VMapUpdater draw")
      image.draw(false, false)
    }

    def update(): Unit = {
      draw()
      widget.repaint()
    }

  }


}

