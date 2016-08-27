// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Main window for new TileCollection concept.

package bdzimmer.pixeleditor.view

import java.awt.event.{ActionListener, ActionEvent, FocusAdapter, FocusEvent}
import javax.swing.{JButton, JMenuBar, JMenu, JMenuItem, JPanel, WindowConstants}

import bdzimmer.pixeleditor.model.TileCollectionModel._

class TileCollectionWindow(
    var titleString: String,
    var tileCollection: TileCollection,
    var filename: String) extends CommonWindow {

  updateTitle()

  // initialize Palette Chunks window

  var paletteChunksWindow = new PaletteChunksWindow(
      "Palette Chunks",
      tileCollection.paletteChunks,
      tileCollection.settings)
  paletteChunksWindow.setLocationRelativeTo(null)

  // initialize Pixels window

  var pixelsWindow = new PixelsWindow("Pixels", tileCollection.pixels, tileCollection.settings)
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


  override def menuBar(): JMenuBar = {

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


  override def panel(): JPanel = {
    val panel = new JPanel()
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