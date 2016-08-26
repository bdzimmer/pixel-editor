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
      tileCollection.paletteChunks.map(x => PaletteChunk(x._1, x._2)).toBuffer,
      tileCollection.settings)
  paletteChunksWindow.setLocationRelativeTo(null)

  ///

  setFocusable(true)
  addFocusListener(new FocusAdapter() {
    override def focusGained(event: FocusEvent): Unit = {
      println("palette chunks window focus gained!");
      repaint()
    }
  })

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
    val jmOpen = new JMenuItem("Open")
    val jmSave = new JMenuItem("Save")
    val jmSaveAs = new JMenuItem("Save As")

    jmNew.addActionListener(new ActionListener() {
      override def actionPerformed(ae: ActionEvent) {
        newCollection()
      }
    })

    jmOpen.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent) {
        openCollection()
      }
    })

    jmSave.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent) {
        saveCollection()
      }
    })

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
    panel
  }


}