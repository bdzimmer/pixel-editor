// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// There will be quite a bit of overlap between this and PixelsWindow.
// TODO: think about how to refactor.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import java.awt.{BorderLayout, GridLayout}
import java.awt.event.{ActionEvent, ActionListener, MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import javax.swing.{JButton, JComboBox, JOptionPane, JPanel, JToolBar, JToggleButton, JSeparator, WindowConstants, SwingConstants}
import javax.swing.event.{ChangeListener, ChangeEvent}

import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.controller.TileUtil
import bdzimmer.pixeleditor.model.{Color, Tile, Tileset, TileContainer}
import bdzimmer.pixeleditor.controller.PalUtil
import bdzimmer.pixeleditor.model.IndexedGraphics


class VMapWindow(
    title: String,
    vMap: VMap,
    pixels: Pixels,
    pixelsUpdater: Updater,
    paletteChunks: Buffer[Named[Array[Color]]],
    globalPalette: Array[Color],
    globalPaletteUpdater: Updater,
    tileContainer: TileContainer,
    zoomWindow: ZoomedTileWindow,
    settings: Settings) extends CommonWindow {

  setTitle(title)

  var scale = 2

  var drawGrid = false
  var drawTileNumbers = false
  var curPalOffset = 0
  var animationWindow: Option[AnimationWindow] = None

  val rows = (vMap.entries.length + settings.viewTileCols - 1) / settings.viewTileCols;
  val updater = new VMapTilesUpdater()

  val tilesPanel = new JPanel()
  tilesPanel.add(updater.image.indexedGraphics)

  val palConfsPanel = new JPanel()
  var selectedPalConfIdx = 0
  var palConfIdx = 0
  var vMapEntryIdx = 0

  tilesPanel.addMouseListener(new MouseAdapter() {
    override def mouseClicked(event: MouseEvent): Unit = {
      handleClicks(event, true)
    }
  })

  tilesPanel.addMouseWheelListener(new MouseWheelListener() {
    override def mouseWheelMoved(event: MouseWheelEvent): Unit = {
      val notches = event.getWheelRotation().signum
      scale += notches
      if (scale < 1) {
        scale = 1
      }
      updater.buildImage()
      updater.update()
      tilesPanel.removeAll()
      tilesPanel.add(updater.image.indexedGraphics)
      pack()
      repaint()
    }
  })

  val editor = new VMapEntryEditor(vMap.entries, updater, settings)

  applyPalConf()

  build(WindowConstants.HIDE_ON_CLOSE)
  repaint()
  pack()
  setResizable(false)

  // instead of ZoomedTileWindow, we will have a window that
  // allows editing of a VMapEntry

  def handleClicks(event: MouseEvent, allowCopy: Boolean) = {

    val selectedIdxAny =
        (event.getY() / (settings.tileHeight * scale)) * settings.viewTileCols +
        (event.getX() / (settings.tileWidth * scale))

    val prevVMapEntryIdx = vMapEntryIdx

    vMapEntryIdx = if (selectedIdxAny >= pixels.tiles.length) {
      pixels.tiles.length - 1;
    } else {
      selectedIdxAny
    }

    val pixelsIdx = vMap.entries(vMapEntryIdx).pixelsIdx

    println(vMapEntryIdx + ": " + vMap.entries(vMapEntryIdx))

    if (event.isMetaDown()) {
      selectTile(pixelsIdx)
      editor.selectEntry(vMapEntryIdx)
      // show in animation window
      animationWindow.foreach(x => {
        if (x.isVisible) {
          x.setTileIndex(vMapEntryIdx)
          x.toFront
        }
      })
    } else  if (allowCopy) {
      vMap.entries(vMapEntryIdx) = vMap.entries(prevVMapEntryIdx).copy()
      updater.update()
    }

    statusBar.update(0, 0, "" + vMapEntryIdx)

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


  def applyPalConf(): Unit = {
    val palConf = vMap.palConfs(selectedPalConfIdx).value.chunkIdxs.map(i => paletteChunks(i).value)
    PalUtil.applyPalConf(globalPalette, palConf)
    globalPaletteUpdater.update()
    updater.update()
    pixelsUpdater.update()
  }

  // TODO: really only the selector needs to be rebuilt
  def rebuildPalConfsPanel(): Unit = {
    palConfsPanel.removeAll()

    val selItems = vMap.palConfs.map(_.name).toArray
    val sel = new JComboBox(selItems)
    if (selectedPalConfIdx < selItems.length) {
      sel.setSelectedIndex(selectedPalConfIdx)
      applyPalConf()
    }

    sel.addActionListener(new ActionListener {
      override def actionPerformed(ae: ActionEvent): Unit = {
        selectedPalConfIdx = ae.getSource.asInstanceOf[JComboBox[String]].getSelectedIndex
        applyPalConf()
      }
    })
    palConfsPanel.add(sel)

    val edit = new JButton("Edit")
    edit.addActionListener(new ActionListener {
      override def actionPerformed(ae: ActionEvent): Unit = {
        if (selectedPalConfIdx < vMap.palConfs.length) {
          val conf = vMap.palConfs(selectedPalConfIdx)
          val pw = new PaletteConfWindow(
              "Palette Configuration - " + conf.name, conf.value, paletteChunks, settings)
          pw.setVisible(true)
        }
      }
    })
    palConfsPanel.add(edit)

     val rename = new JButton("Rename")
    rename.addActionListener(new ActionListener() {
      def actionPerformed(event: ActionEvent): Unit = {

        if (selectedPalConfIdx < vMap.palConfs.length) {
          val conf = vMap.palConfs(selectedPalConfIdx)
          val newName = JOptionPane.showInputDialog(null, "Enter a new name:", conf.name)
          if (newName != null && newName.length > 0) {
            vMap.palConfs(selectedPalConfIdx) = conf.value named newName
            applyPalConf()
            rebuildPalConfsPanel()
          }
        }
      }
    })
    rename.setFocusable(false)
    palConfsPanel.add(rename)


    val add = new JButton("Add")
    add.addActionListener(new ActionListener {
      override def actionPerformed(ae: ActionEvent): Unit = {
        val name = JOptionPane.showInputDialog(null, "Enter name:", "Pal Conf " + vMap.palConfs.length)
        if (name != null && name.length > 0) {
          val conf = new Named[PaletteConf](name, PaletteConf(Buffer()))
          vMap.palConfs += conf
          selectedPalConfIdx = vMap.palConfs.size - 1
          applyPalConf()
          rebuildPalConfsPanel()
        }
      }
    })
    palConfsPanel.add(add)

    palConfsPanel.revalidate()
    palConfsPanel.repaint()
  }

  ////////////////////////

  override def buildPanel(): JPanel = {
    val panel = new JPanel()
    panel.setLayout(new BorderLayout())
    panel.add(tilesPanel, BorderLayout.CENTER)
    panel.add(buildToolBars(), BorderLayout.NORTH)
    panel
  }

  def buildToolBars(): JPanel = {

    val mainToolbar = new JToolBar()

    val drawGridButton = new JToggleButton("Grid")
    drawGridButton.addChangeListener(new ChangeListener() {
      override def stateChanged(e: ChangeEvent) {
        drawGrid = drawGridButton.isSelected
        updater.update()
      }
    });
    drawGridButton.setFocusable(false)
    mainToolbar.add(drawGridButton);

    val drawTileNumbersButton = new JToggleButton("Numbers")
    drawTileNumbersButton.addChangeListener(new ChangeListener() {
      override def stateChanged(e: ChangeEvent) {
        drawTileNumbers = drawTileNumbersButton.isSelected
        updater.update()
      }
    });
    drawTileNumbersButton.setFocusable(false);
    mainToolbar.add(drawTileNumbersButton);

    val animationButton = new JButton("Animation")
    animationButton.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) {
        animationWindow.foreach(_.dispose())
        animationWindow = Some(new AnimationWindow(
            updater.tiles,
            globalPalette,
            vMapEntryIdx))
        animationWindow.foreach(_.setLocationRelativeTo(VMapWindow.this))
      }
    });
    animationButton.setFocusable(false)
    mainToolbar.add(animationButton)

    mainToolbar.addSeparator()

    rebuildPalConfsPanel()
    mainToolbar.add(palConfsPanel)
    mainToolbar.setFloatable(false)

    val editorToolbar = new JToolBar()
    for (component <- editor.components()) {
      editorToolbar.add(component);
    }
    editorToolbar.setFloatable(false);

    val panel = new JPanel( new GridLayout(0, 1))
    panel.add(mainToolbar)
    panel.add(editorToolbar)

    return panel;
  }


  override def buildStatusBar(): StatusBar = {
    new StatusBar(6, 6, 20)
  }

  ///////


  class VMapTilesUpdater() extends Updater {

    var image: TilesetImage = null
    val tiles = Array.fill(vMap.entries.length)(Tileset.emptyTile(settings.tileWidth, settings.tileHeight))

    buildImage()
    draw()

    def buildImage(): Unit = {
      image = new TilesetImage(vMap.entries, pixels.tiles, globalPalette, scale, settings)
    }

    def draw(): Unit = {
      val tempTiles = image.draw(drawGrid, drawTileNumbers)
      for (idx <- 0 until tempTiles.length) {
        tiles(idx) = tempTiles(idx)
      }
    }

    def update(): Unit = {
      draw()
      repaint()
    }

  }

}


// TODO: PixelsTilesUpdater should use this

class TilesetImage(
    val entries: Array[VMapEntry],
    tiles: Array[Tile],
    globalPalette: Array[Color],
    scale: Int,
    settings: Settings) {

  val rows = (entries.length + settings.viewTileCols - 1) / settings.viewTileCols

  val indexedGraphics = new IndexedGraphics(
      globalPalette,
      settings.bitsPerChannel,
      rows * settings.tileHeight,
      settings.viewTileCols * settings.tileWidth,
      scale)

  indexedGraphics.setGridDimensions(settings.tileHeight, settings.tileWidth)

  draw(false, false)

  def draw(drawGrid: Boolean, drawTileNumbers: Boolean): Array[Tile] = {
    indexedGraphics.updateClut()

    val entryTiles = entries.map(x => {
      var tile = tiles(x.pixelsIdx)
      if (x.flipY) {
        tile = Tile(tile.bitmap.reverse)
      }
      if (x.flipX) {
        tile = Tile(tile.bitmap.map(_.reverse))
      }
      Tile(tile.bitmap.map(row => row.map(pixel => pixel + x.palOffset)))
    })

    TileUtil.drawTileset(
        indexedGraphics,
        entryTiles,
        settings.tileWidth,
        settings.tileHeight,
        settings.viewTileCols)

    if (drawGrid) {
      TileUtil.drawGrid(
          indexedGraphics.getImage,
          settings.tileWidth  * scale,
          settings.tileHeight * scale)
    }

    if (drawTileNumbers) {
      TileUtil.drawNumbers(
          indexedGraphics.getImage, entries.size,
          settings.viewTileCols, rows,
          settings.tileWidth * scale,
          settings.tileHeight * scale)
    }

    entryTiles
  }

}
