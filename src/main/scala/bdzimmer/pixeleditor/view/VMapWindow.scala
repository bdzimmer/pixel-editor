// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// There will be quite a bit of overlap between this and PixelsWindow.
// TODO: think about how to refactor.

package bdzimmer.pixeleditor.view

import scala.collection.mutable.Buffer

import java.awt.{BorderLayout}
import java.awt.event.{ActionEvent, ActionListener, MouseAdapter, MouseEvent}
import java.awt.image.BufferedImage
import javax.swing.{JButton, JComboBox, JOptionPane, JPanel, JToolBar, JToggleButton, WindowConstants}
import javax.swing.event.{ChangeListener, ChangeEvent}

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, Tile, TileContainer}
import bdzimmer.pixeleditor.controller.PalUtil


class VMapWindow(
    title: String,
    vMap: VMap,
    pixels: Pixels,
    paletteChunks: Buffer[Named[Array[Color]]],
    globalPalette: Array[Color],
    globalPaletteUpdater: Updater,
    settings: Settings) extends CommonWindow {

  setTitle(title)

  var drawGrid = false
  var drawTileNumbers = false
  var curPalOffset = 0

  val rows = (vMap.entries.length + settings.viewTileCols - 1) / settings.viewTileCols;
  val updater = new VMapTilesUpdater(vMap.entries, pixels.tiles, settings)
  val scrollPane = new WidgetScroller(Buffer(updater.widget), selectable = false)

  val palConfsPanel = new JPanel()
  var selectedPalConfIdx = 0

  var palConfIdx = 0

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

  // instead of ZoomedTileWindow, we will have a window that
  // allows editing of a VMapEntry

  def handleClicks(event: MouseEvent, allowCopy: Boolean) = {
    // TODO: implement handleClicks
  }


  // TODO: really only the selector needs to be rebuilt
  def rebuildPalConfsPanel(): Unit = {
    palConfsPanel.removeAll()

    val selItems = vMap.palConfs.map(_.name).toArray
    val sel = new JComboBox(selItems)
    if (selectedPalConfIdx < selItems.length) {
      sel.setSelectedIndex(selectedPalConfIdx)
    }
    sel.addActionListener(new ActionListener {
      override def actionPerformed(ae: ActionEvent): Unit = {
        selectedPalConfIdx = ae.getSource.asInstanceOf[JComboBox[String]].getSelectedIndex
        val palConf = vMap.palConfs(selectedPalConfIdx).value.chunkIdxs.map(i => paletteChunks(i).value)
        PalUtil.applyPalConf(globalPalette, palConf)
        globalPaletteUpdater.update()
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

    val add = new JButton("Add")
    add.addActionListener(new ActionListener {
      override def actionPerformed(ae: ActionEvent): Unit = {
        val name = JOptionPane.showInputDialog(null, "Enter name:")
        if (name != null && name.length > 0) {
          val conf = new Named[PaletteConf](name, PaletteConf(Buffer()))
          vMap.palConfs += conf
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
    mainToolbar.add(drawGridButton);

    val drawTileNumbersButton = new JToggleButton("Numbers")
    drawTileNumbersButton.addChangeListener(new ChangeListener() {
      override def stateChanged(e: ChangeEvent) {
        println("numbers show: "+ drawTileNumbersButton.isSelected)
        drawTileNumbers = drawTileNumbersButton.isSelected
        updater.update
      }
    });
    drawTileNumbersButton.setFocusable(false);
    mainToolbar.add(drawTileNumbersButton);

    mainToolbar.addSeparator()

    rebuildPalConfsPanel()
    mainToolbar.add(palConfsPanel)

    mainToolbar.setFloatable(false);

    return mainToolbar;
  }


  override def buildStatusBar(): StatusBar = {
    new StatusBar(6, 6, 20)
  }

  ///////

  // TODO: implement VMapTilesUpdater

  class VMapTilesUpdater(
      entries: Array[VMapEntry],
      tiles: Array[Tile],
      settings: Settings) extends WidgetUpdater {

    val emptyImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB)
    val widget = new ImageWidget("", emptyImage, List(), 0, 0)

    def update(): Unit = {
      // do nothing
    }

  }

}

object VMapWindow {
  val Scale = 2
}