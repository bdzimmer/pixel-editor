// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Main window for new TileCollection concept.

package bdzimmer.pixeleditor.view

import java.awt.event.{ActionListener, ActionEvent, FocusAdapter, FocusEvent}
import javax.swing.{JButton, JMenuBar, JMenu, JMenuItem, JPanel, WindowConstants}

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, TileContainer}

class TileCollectionWindow(
    var titleString: String,
    var tileCollection: TileCollection,
    var filename: String) extends CommonWindow {

  updateTitle()

  val tileContainer = new TileContainer

  // initialize global palette window
  val globalPalette = (0 until tileCollection.settings.paletteSize).map(x => Color(0, 0, 0)).toArray
  val globalPaletteWindow = new PaletteWindow(
      "Global Palette", globalPalette, tileCollection.settings.bitsPerChannel, null)

  // initialize Palette Chunks window

  var paletteChunksWindow = new PaletteChunksWindow(
      "Palette Chunks",
      tileCollection.paletteChunks,
      tileCollection.settings)
  paletteChunksWindow.setLocationRelativeTo(null)

  // initialize Pixels window

  var pixelsWindow = new PixelsWindow(
      "Pixels", tileCollection.pixels, tileCollection.settings, globalPaletteWindow, tileContainer)
  pixelsWindow.setLocationRelativeTo(null)

  ////

  build(WindowConstants.EXIT_ON_CLOSE)
  packAndShow(false)

  //////

  def newCollection(): Unit = {}
  def openCollection(): Unit = {}
  def saveCollection(): Unit = {}

  def updateTitle(): Unit = {
    setTitle(titleString)
  }

  /////


  override def buildMenuBar(): JMenuBar = {

    val mainMenu = new JMenuBar()
    val fileMenu = new JMenu("File")

    val jmNew = new JMenuItem("New")
    jmNew.addActionListener(new ActionListener() {
      override def actionPerformed(ae: ActionEvent) {
        newCollection()
      }
    })

    val jmOpen = new JMenuItem("Open")
    jmOpen.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent) {
        openCollection()
      }
    })

    val jmSave = new JMenuItem("Save")
    jmSave.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent) {
        saveCollection()
      }
    })

    val jmSaveAs = new JMenuItem("Save As")
    jmSaveAs.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent) {
        saveCollection()
      }
    })

    fileMenu.add(jmNew)
    fileMenu.add(jmOpen)
    fileMenu.add(jmSave)
    fileMenu.add(jmSaveAs)
    mainMenu.add(fileMenu)

    return mainMenu

  }


  override def buildPanel(): JPanel = {
    val panel = new JPanel()
    val globalPaletteButton = new JButton("Global Palette")
    globalPaletteButton.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent): Unit = {
        val isVisible = globalPaletteWindow.isVisible
        globalPaletteWindow.setVisible(!isVisible)
      }
    })
    panel.add(globalPaletteButton)

    val paletteChunksButton = new JButton("Palette Chunks")
    paletteChunksButton.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent): Unit = {
        val isVisible = paletteChunksWindow.isVisible
        paletteChunksWindow.setVisible(!isVisible)
      }
    })

    panel.add(paletteChunksButton)
    val pixelsButton = new JButton("Pixels")
    pixelsButton.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent): Unit = {
        val isVisible = pixelsWindow.isVisible
        pixelsWindow.setVisible(!isVisible)
      }
    })
    panel.add(pixelsButton)

    panel
  }


}
