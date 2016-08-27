// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import java.awt.{BorderLayout}
import java.awt.event.{FocusAdapter, FocusEvent}
import java.awt.image.BufferedImage
import javax.swing.{JPanel, WindowConstants}

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{ColorTriple, Tile}

class PixelsWindow(
    title: String,
    pixels: Pixels,
    settings: Settings) extends CommonWindow {

  setTitle(title)

  val updater = new TilesUpdater(pixels.tiles, settings)
  val scrollPane = new WidgetScroller(Buffer(updater.widget))

  def rebuild(): Unit = {
    scrollPane.rebuild()
    repaint()
  }

  build(WindowConstants.HIDE_ON_CLOSE)
  rebuild()
  pack()
  setResizable(false)

  ////////////////////////

  override def panel(): JPanel = {
    val panel = new JPanel()
    panel.setLayout(new BorderLayout())
    panel.add(scrollPane, BorderLayout.CENTER)
    panel.add(scrollPane.scrollBar, BorderLayout.EAST)
    panel
  }


  ///////

  class TilesUpdater(
      tiles: Array[Tile], settings: Settings) extends WidgetUpdater  {

    val rows = (tiles.length + settings.viewTileCols - 1) / settings.viewTileCols

    val image = new BufferedImage(
        settings.viewTileCols * settings.tileWidth * PixelsEditorWindow.ScaleFactor,
        rows * settings.tileHeight * PixelsEditorWindow.ScaleFactor,
        BufferedImage.TYPE_INT_RGB)

    draw()
    val widget = new ImageWidget("", image, List(), 0, 0)

    def draw(): Unit = {
      println("PixelsUpdater draw")
      // todo: draw the pixels
      val gr = image.getGraphics()
      gr.setColor(java.awt.Color.BLACK)
      gr.fillRect(0, 0, image.getWidth, image.getHeight)
    }

    def update(): Unit = {
      println("PixelsUpdater update")
      draw()
      widget.repaint()
    }
  }

}

object PixelsEditorWindow {
  val ScaleFactor = 2
}