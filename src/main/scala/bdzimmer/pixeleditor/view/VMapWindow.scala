// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// There will be quite a bit of overlap between this and PixelsWindow.
// TODO: think about how to refactor.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import java.awt.GridLayout
import java.awt.event.{ActionEvent, ActionListener, MouseAdapter, MouseEvent}
import javax.swing.{JButton, JComboBox, JOptionPane, JPanel, JToolBar, JToggleButton, WindowConstants}
import javax.swing.event.{ChangeListener, ChangeEvent}

import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.controller.TileUtil
import bdzimmer.pixeleditor.model.{Color, Tile, Tileset, TileContainer, Map}
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
  updater.image.indexedGraphics.addMouseListener(new MouseAdapter() {
    override def mouseClicked(event: MouseEvent): Unit = {
      handleClicks(event, true)
    }
  })


  val palConfsPanel = new JToolBar()
  palConfsPanel.setFloatable(false)

  var selectedPalConfIdx = 0
  var palConfIdx = 0
  var vMapEntryIdx = 0



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
      updater.image.indexedGraphics.addMouseListener(new MouseAdapter() {
        override def mouseClicked(event: MouseEvent): Unit = {
          handleClicks(event, true)
        }
      })
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

    // get entry index and ensure valid

    val prevVMapEntryIdx = vMapEntryIdx

    val selectedIdxAny =
      (event.getY / (settings.tileHeight * scale)) * settings.viewTileCols +
        (event.getX / (settings.tileWidth * scale))

    vMapEntryIdx = if (selectedIdxAny >= pixels.tiles.length) {
      pixels.tiles.length - 1;
    } else {
      selectedIdxAny
    }

    println("clicked index: " + vMapEntryIdx)

    // copy or update

    if (event.isMetaDown()) { // right click

      val pixelsIdx = vMap.entries(vMapEntryIdx).pixelsIdx

      println("right clicked vmap entry " + vMapEntryIdx + ": " + vMap.entries(vMapEntryIdx))

      // show the tile in the zoomedtilewindow
      selectTile(pixelsIdx)

      editor.selectEntry(vMapEntryIdx)

      // show in animation window
      animationWindow.foreach(x => {
        if (x.isVisible) {
          x.setTileIndex(vMapEntryIdx)
          x.toFront
        }
      })

    } else if (allowCopy) { // left click

      println("left clicked vmap entry " + vMapEntryIdx + ": " + vMap.entries(vMapEntryIdx))

      val dstIndex = vMapEntryIdx

      if (tileContainer.getSource.equals(this)) {
        val srcIndex = prevVMapEntryIdx
        println("copying vmap entry " + srcIndex + " -> " + dstIndex)
        vMap.entries(dstIndex) = vMap.entries(srcIndex).copy()
      } else {
        val srcIndex = tileContainer.getTileIndex
        println("setting pixels index of entry " + dstIndex + " to " + srcIndex)
        vMap.entries(dstIndex) = vMap.entries(dstIndex).copy(pixelsIdx = srcIndex)
      }

      updater.update()

    }

    for (idx <- 0 until 16) {
      println("\t" + idx + ":" + vMap.entries(idx))
    }

    statusBar.update(0, 0, "" + vMapEntryIdx)

  }


  // select a tile from the set into the tile container
  // and show it in the ZoomWindow
  def selectTile(pixelsIdx: Int): Unit = {

    // set the current tile
    tileContainer.setSource(this)
    tileContainer.setTileIndex(pixelsIdx)
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
    tilesPanel
  }

  override def buildToolBar(): JToolBar = {

    val finalToolbar = new JToolBar()
    finalToolbar.setLayout(new GridLayout(3, 0))
    finalToolbar.setFloatable(false)

    val mainToolbar = new JToolBar()
    mainToolbar.setFloatable(false)

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

    val backgroundButton = new JButton("Background")
    backgroundButton.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent) {

        val backgroundWindow = new MapEditorWindow(
          "",
          new Map(),
          "",
          globalPalette,
          updater.tiles,
          vMap.entries.map(_.attribs).toArray,
          tileContainer)
        backgroundWindow.setLocationRelativeTo(VMapWindow.this)
      }
    });
    backgroundButton.setFocusable(false)
    mainToolbar.add(backgroundButton)

    finalToolbar.add(mainToolbar)

    rebuildPalConfsPanel()
    finalToolbar.add(palConfsPanel)

    val editorToolbar = new JToolBar()
    editorToolbar.setFloatable(false)
    for (component <- editor.components()) {
      editorToolbar.add(component)
    }

    finalToolbar.add(editorToolbar)

    return finalToolbar
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

  val renderedTiles = (0 until entries.length).map(_ =>
      Tileset.emptyTile(settings.tileWidth, settings.tileHeight)).toArray

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

    for (i <- 0 until entries.length) {
      drawEntry(i)
    }

    TileUtil.drawTileset(
        indexedGraphics,
        renderedTiles,
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

    renderedTiles
  }


  def drawEntry(i: Int): Unit = {
    val x = entries(i)
    var tile = tiles(x.pixelsIdx)
    if (x.flipY) {
      tile = Tile(tile.bitmap.reverse)
    }
    if (x.flipX) {
      tile = Tile(tile.bitmap.map(_.reverse))
    }

    renderedTiles(i) = Tile(tile.bitmap.map(row => row.map(pixel => pixel + x.palOffset)))
  }

}
