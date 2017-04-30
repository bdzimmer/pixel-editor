// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import java.awt.{BorderLayout}
import java.awt.event.{MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import javax.swing.{JPanel, JToolBar, JToggleButton, WindowConstants}
import javax.swing.event.{ChangeListener, ChangeEvent}

import bdzimmer.pixeleditor.controller.TileUtil
import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, Tile, TileContainer, TileProperties}
import bdzimmer.pixeleditor.model.IndexedGraphics


class PixelsWindow(
    title: String,
    pixels: Pixels,
    settings: Settings,
    paletteWindow: PaletteWindow,
    tileContainer: TileContainer,
    zoomWindow: ZoomedTileWindow) extends CommonWindow {

  setTitle(title)

  var drawGrid = false
  var drawTileNumbers = false
  var curPalOffset = 0

  val rows = (pixels.tiles.length + settings.viewTileCols - 1) / settings.viewTileCols;

  val updater = new VMapTilesUpdater()
  val scrollPane = new WidgetScroller(Buffer(updater.widget), selectable = false)

  updater.widget.addMouseListener(new MouseAdapter() {
     override def mouseClicked(event: MouseEvent): Unit = {
       handleClicks(event, true)
     }
  })

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

    val selectedIdxAny =
        (event.getY() / (settings.tileHeight * PixelsWindow.Scale)) * settings.viewTileCols +
        (event.getX() / (settings.tileWidth * PixelsWindow.Scale))

    val pixelsIdx = if (selectedIdxAny >= pixels.tiles.length) {
      pixels.tiles.length - 1;
    } else {
      selectedIdxAny
    }

    println("selected tile index: " + pixelsIdx)

    if (event.isMetaDown()) {
      // right click grab tile
      selectTile(pixelsIdx)

    } else  if (allowCopy) {

      // TODO: use drag / drop functionality for this???

      // Calculate maximum size we can copy
      val maxHeight = math.min(settings.tileHeight, tileContainer.getTileBitmap().length)
      val maxWidth  = math.min(settings.tileWidth,  tileContainer.getTileBitmap()(0).length)
      for (i <- 0 until maxHeight) {
        for (j <- 0 until maxWidth) {
          pixels.tiles(pixelsIdx).bitmap(i)(j) = tileContainer.getTileBitmap()(i)(j)
        }
      }
      pixels.defaultPalOffsets(pixelsIdx) = curPalOffset

      tileContainer.setTileIndex(pixelsIdx)
      tileContainer.setTileBitmap(pixels.tiles(pixelsIdx).bitmap)

      updater.update()

    }

    statusBar.update(0, 0, "" + pixelsIdx);

  }


  // select a tile from the set into the tile container
  // and show it in the ZoomWindow
  def selectTile(pixelsIdx: Int): Unit = {

    // set the current tile
    tileContainer.setTileIndex(pixelsIdx);
    val bitmap = pixels.tiles(pixelsIdx).bitmap
    tileContainer.setTileBitmap(bitmap)
    curPalOffset = pixels.defaultPalOffsets(pixelsIdx)

    // show in zoom window
    zoomWindow.setTile(
        bitmap,
        new ArrayContainer(pixels.defaultPalOffsets, pixelsIdx),
        settings.colorsPerTile)
    zoomWindow.setUpdater(updater)

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
        drawGrid = drawGridButton.isSelected
        updater.update
      }
    });
    drawGridButton.setFocusable(false)

    val drawTileNumbersButton = new JToggleButton("Numbers")
    drawTileNumbersButton.addChangeListener(new ChangeListener() {
      override def stateChanged(e: ChangeEvent) {
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

  /*
  class PixelsTilesUpdater(
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
    val widget = new ImageWidget("", indexedGraphics.getImage, List(), 0, 0)

    def draw(): Unit = {
      println("PixelsUpdater draw")
      indexedGraphics.updateClut()
      // TODO: draw using palette offsets
      TileUtil.drawTileset(
          indexedGraphics,
          pixels.tiles,
          pixels.defaultPalOffsets,
          settings.tileWidth,
          settings.tileHeight,
          settings.viewTileCols)

      if (drawGrid) {
        TileUtil.drawGrid(
            indexedGraphics.getImage,
            settings.tileWidth  * PixelsWindow.Scale,
            settings.tileHeight * PixelsWindow.Scale)
      }

      if (drawTileNumbers) {
        TileUtil.drawNumbers(
            indexedGraphics.getImage, pixels.tiles.size,
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
  *
  */


  class VMapTilesUpdater() extends WidgetUpdater {

    val entries = (0 until pixels.tiles.size).map(idx => {
      VMapEntry(idx, pixels.defaultPalOffsets(idx), false, false, new TileProperties(0))
    }).toArray
    val image = new TilesetImage(entries, pixels.tiles, paletteWindow.getPalette, PixelsWindow.Scale, settings)

    val widget = new ImageWidget("", image.indexedGraphics.getImage, List(), 0, 0)

    draw()

    def draw(): Unit = {
      // TODO: update the image "entries" as palette offsets change
      image.draw(drawGrid, drawTileNumbers)
    }

    def update(): Unit = {
      draw()
      repaint()
    }

  }

}



object PixelsWindow {
  val Scale = 2
}
