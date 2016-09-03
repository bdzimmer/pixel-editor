// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer
import java.awt.{BorderLayout}
import java.awt.event.{FocusAdapter, FocusEvent, MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import javax.swing.{JPanel, JToolBar, JToggleButton, WindowConstants}
import javax.swing.event.{ChangeListener, ChangeEvent}


import bdzimmer.pixeleditor.controller.TileUtil
import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, Tile, TileContainer}
import bdzimmer.pixeleditor.model.IndexedGraphics


class PixelsWindow(
    title: String,
    pixels: Pixels,
    settings: Settings,
    paletteWindow: PaletteWindow,
    tileContainer: TileContainer) extends CommonWindow {

  setTitle(title)

  var drawGrid = false
  var drawTileNumbers = false

  val rows = (pixels.tiles.length + settings.viewTileCols - 1) / settings.viewTileCols - 1;

  val updater = new TilesUpdater(pixels.tiles, settings)
  val scrollPane = new WidgetScroller(Buffer(updater.widget), selectable = false)

  updater.widget.addMouseListener(new MouseAdapter() {
     override def mouseClicked(event: MouseEvent): Unit = {
       handleClicks(event, true)
     }
  })

  val zoomWindow = new ZoomedTileWindow(
          "Zoom", tileContainer.getTileBitmap,
          paletteWindow, updater)
  zoomWindow.setLocationRelativeTo(this)
  zoomWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
  zoomWindow.setVisible(false)


  def rebuild(): Unit = {
    scrollPane.rebuild()
    repaint()
  }

  build(WindowConstants.HIDE_ON_CLOSE)
  rebuild()
  pack()
  setResizable(false)

  ////////////////////////

  def handleClicks(event: MouseEvent, allowCopy: Boolean) {

    val selectedTileAny =
        (event.getY() / (settings.tileHeight * PixelsWindow.Scale)) * settings.viewTileCols +
        (event.getX() / (settings.tileWidth * PixelsWindow.Scale))


    val selectedTile = if (selectedTileAny >= pixels.tiles.length) {
      pixels.tiles.length - 1;
    } else {
      selectedTileAny
    }


    println("selected tile: " + selectedTile)

    if (event.isMetaDown()) {
      // right click grab tile
      selectTile(selectedTile)

    } else  if (allowCopy) {

      val newTile = selectedTile;

      // TODO: use drag / drop functionality for this???

      // Calculate maximum size we can copy
      // The global tile bitmap here seems kind of dumb, but it's there to allow
      // copying tiles across tileset -- important functionality.

      val maxHeight = math.min(settings.tileHeight, tileContainer.getTileBitmap().length)
      val maxWidth  = math.min(settings.tileWidth,  tileContainer.getTileBitmap()(0).length)

      for (i <- 0 until maxHeight) {
        for (j <- 0 until maxWidth) {
          pixels.tiles(newTile).bitmap(i)(j) = tileContainer.getTileBitmap()(i)(j)
        }
      }

      // set the copy as the current tile
      tileContainer.setTileIndex(selectedTile)
      tileContainer.setTileBitmap(pixels.tiles(selectedTile).bitmap)
      updater.update()

    }

    statusBar.update(0, 0, "" + selectedTile);

  }


  // select a tile from the set into the tile container
  // and show it in the ZoomWindow
  def selectTile(selectedTile: Int): Unit = {

    // set the current tile
    tileContainer.setTileIndex(selectedTile);
    val bitmap = pixels.tiles(selectedTile).bitmap
    tileContainer.setTileBitmap(bitmap)

    // show in zoom window
    zoomWindow.setTile(bitmap, selectedTile)
    zoomWindow.toFront()
    zoomWindow.setVisible(true)

  }


  ////////////////////////

  override def buildPanel(): JPanel = {
    val panel = new JPanel()
    panel.setLayout(new BorderLayout())
    panel.add(scrollPane, BorderLayout.CENTER)
    panel.add(scrollPane.scrollBar, BorderLayout.EAST)
    panel
  }

  override def buildToolBar(): JToolBar = {

    val mainToolbar = new JToolBar()

    val drawGridButton = new JToggleButton("Grid")
    drawGridButton.addChangeListener(new ChangeListener() {
      override def stateChanged(e: ChangeEvent) {
      	println("grid show: "+ drawGridButton.isSelected)
      	drawGrid = drawGridButton.isSelected
        updater.update
      }
    });
    drawGridButton.setFocusable(false)

    val drawTileNumbersButton = new JToggleButton("Numbers")
    drawTileNumbersButton.addChangeListener(new ChangeListener() {
      override def stateChanged(e: ChangeEvent) {
      	println("numbers show: "+ drawTileNumbersButton.isSelected)
      	drawTileNumbers = drawTileNumbersButton.isSelected
        updater.update
      }
    });
    drawTileNumbersButton.setFocusable(false);

    mainToolbar.add(drawGridButton);
    mainToolbar.add(drawTileNumbersButton);
    mainToolbar.setFloatable(false);

    return mainToolbar;
  }


  override def buildStatusBar(): StatusBar = {
    new StatusBar(6, 6, 20)
  }


  ///////

  class TilesUpdater(
      tiles: Array[Tile], settings: Settings) extends WidgetUpdater  {

    val rows = (tiles.length + settings.viewTileCols - 1) / settings.viewTileCols

    val indexedGraphics = new IndexedGraphics(
        paletteWindow.getPalette(),
        settings.bitsPerChannel,
        rows * settings.tileHeight,
        settings.viewTileCols * settings.tileWidth,
        PixelsWindow.Scale)

    indexedGraphics.setGridDimensions(settings.tileHeight, settings.tileWidth)

    draw()
    val widget = new ImageWidget("", indexedGraphics.getBuffer, List(), 0, 0)

    def draw(): Unit = {
      println("PixelsUpdater draw")
      indexedGraphics.updateClut()
      indexedGraphics.drawTileset(
          pixels.tiles, settings.tileWidth, settings.tileHeight,
          settings.viewTileCols)
      if (drawGrid) {
        TileUtil.drawGrid(
            indexedGraphics.getBuffer,
            settings.tileWidth * PixelsWindow.Scale,
            settings.tileHeight * PixelsWindow.Scale)
      }

      if (drawTileNumbers) {
        TileUtil.drawNumbers(
            indexedGraphics.getBuffer, pixels.tiles.size,
            settings.viewTileCols, rows,
            settings.tileWidth * PixelsWindow.Scale,
            settings.tileHeight * PixelsWindow.Scale)

      }

    }

    def update(): Unit = {
      println("PixelsUpdater update")
      draw()
      widget.repaint()
    }
  }

}

object PixelsWindow {
  val Scale = 2
}
