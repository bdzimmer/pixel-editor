// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Main window for new TileCollection concept.

package bdzimmer.pixeleditor.view

import java.io.{File, FileReader, FileWriter, BufferedReader, BufferedWriter}

import java.awt.GridLayout
import java.awt.event.{ActionListener, ActionEvent, FocusAdapter, FocusEvent}
import javax.swing.{JFileChooser, JButton, JMenuBar, JMenu, JMenuItem, JPanel, WindowConstants}

import scala.collection.mutable.Buffer

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, TileContainer, TileCollectionModel}
import bdzimmer.pixeleditor.controller.IO

import bdzimmer.util.StringUtils._


// TODO: this probably belongs in IO or somewhere similar
case class TileCollectionFiles(filename: String) {
  val settingsFile      = new File(filename / "settings")
  val pixelsFile        = new File(filename / "pixels")
  val vMapsFile         = new File(filename / "vmaps")
  val paletteChunksFile = new File(filename / "palettechunks")
}


class TileCollectionWindow(
    var titleString: String,
    var tileCollection: TileCollection,
    var filename: String,
    var workingDirname: String) extends CommonWindow {

  updateTitle()

  val tileContainer = new TileContainer

  // initialize global palette windo

  var globalPalette: Array[Color] = null

  var globalPaletteWindow: PaletteWindow = null
  var paletteChunksWindow: PaletteChunksWindow = null
  var pixelsWindow: PixelsWindow = null
  var vMapWindow: VMapWindow = null
  var zoomWindow: ZoomedTileWindow = null

  initWindows()

  ////

  build(WindowConstants.EXIT_ON_CLOSE)
  packAndShow(false)

  //////


  def newCollection(): Unit = {

    val settings = SettingsDialog.getSettings()

    tileCollection = TileCollectionModel.emptyCollection(settings, 1024)

    initWindows()

  }


  def readCollection(filename: String): Unit = {

    val tcf = TileCollectionFiles(filename)

    val settings = IO.readSettings(tcf.settingsFile)
    val pixels = IO.readPixels(tcf.pixelsFile, settings)
    val vMaps = IO.readVMaps(tcf.vMapsFile, settings)
    val paletteChunks = IO.readPaletteChunks(tcf.paletteChunksFile)

    tileCollection = new TileCollection(
      settings,
      pixels,
      vMaps,
      paletteChunks
    )

  }


  def writeCollection(filename: String): Unit = {

    val tcf = TileCollectionFiles(filename)

    new File(filename).mkdirs()

    IO.writeSettings(tcf.settingsFile, tileCollection.settings)
    IO.writePixels(tcf.pixelsFile, tileCollection.pixels, tileCollection.settings)
    IO.writeVMaps(tcf.vMapsFile, tileCollection.vmaps, tileCollection.settings)
    IO.writePaletteChunks(tcf.paletteChunksFile, tileCollection.paletteChunks)

  }


  def updateTitle(): Unit = {
    setTitle(titleString)
  }


  def initWindows(): Unit = {

    List(globalPaletteWindow, pixelsWindow, vMapWindow, paletteChunksWindow, zoomWindow).foreach(x => {
      if (x != null) {
        x.dispose()
      }
    })

    /*
    val reds =   (0 until 8).map(x => Color(x * 2, 0,     0))
    val greens = (0 until 8).map(x => Color(0,     x * 2, 0))
    val blues =  (0 until 8).map(x => Color(0,     0,     x * 2))
    val globalPalette = (reds ++ greens ++ blues ++ greens ++ (32 until 256).map(_ => Color(0, 0, 0))).toArray
    */



    globalPalette = (0 until tileCollection.settings.paletteSize).map(_ => Color(0, 0, 0)).toArray

    globalPaletteWindow = new PaletteWindow(
      "Global Palette", globalPalette, tileCollection.settings.bitsPerChannel, null)

    zoomWindow = new ZoomedTileWindow(
        "Zoom",
        tileContainer.getTileBitmap,
        new SimpleContainer(0),
        tileCollection.settings.colorsPerTile,
        globalPaletteWindow)

    zoomWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
    zoomWindow.setVisible(false)


    // initialize Pixels window
    pixelsWindow = new PixelsWindow(
        "Pixels",
        tileCollection.pixels,
        tileCollection.settings,
        globalPaletteWindow,
        tileContainer,
        zoomWindow)
    pixelsWindow.setLocationRelativeTo(null)
    zoomWindow.getUpdaters.add(pixelsWindow.updater)

    // intiialize VMap window
    vMapWindow = new VMapWindow(
        "VMap - " + tileCollection.vmaps(0).name,
        tileCollection.vmaps(0).value,
        tileCollection.pixels,
        tileCollection.paletteChunks,
        globalPalette,
        new DumbUpdater(globalPaletteWindow),
        tileContainer,
        zoomWindow,
        tileCollection.settings)
    zoomWindow.getUpdaters.add(vMapWindow.updater)

    // initialize Palette Chunks window
    paletteChunksWindow = new PaletteChunksWindow(
        "Palette Chunks",
        tileCollection.paletteChunks,
        tileCollection.settings)
    paletteChunksWindow.setLocationRelativeTo(null)

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
        fileChooser(workingDirname, save = false).foreach({case (wd, fn) => {
          readCollection(wd / fn)
          workingDirname = wd
          filename       = fn
        }})
      }
    })

    val jmReload = new JMenuItem("Reload")
    jmReload.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent) {
        readCollection(workingDirname / filename)
      }
    })

    val jmSave = new JMenuItem("Save")
    jmSave.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent) {
        writeCollection(workingDirname / filename)
      }
    })

    val jmSaveAs = new JMenuItem("Save As")
    jmSaveAs.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent) {
        fileChooser(workingDirname, save = true).foreach({case (wd, fn) => {
          writeCollection(wd / fn)
          workingDirname = wd
          filename       = fn
        }})
      }
    })

    fileMenu.add(jmNew)
    fileMenu.add(jmOpen)
    fileMenu.add(jmReload)
    fileMenu.addSeparator()
    fileMenu.add(jmSave)
    fileMenu.add(jmSaveAs)
    mainMenu.add(fileMenu)

    return mainMenu

  }


  override def buildPanel(): JPanel = {
    val panel = new JPanel()
    panel.setLayout(new GridLayout(4, 1, 5, 5))

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

    // TODO: just for testing
    val vMapButton = new JButton("VMap")
    vMapButton.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent): Unit = {
        val isVisible = vMapWindow.isVisible
        vMapWindow.setVisible(!isVisible)
      }
    })
    panel.add(vMapButton)

    panel
  }

  override def buildStatusBar(): StatusBar = {
    new StatusBar(20, 0, 0)
  }

  override def onFocus(): Unit = {
    updateMemoryUsageDisplay()
  }


  ////

  // TODO: put this filechooser code somewhere more general
  private def fileChooser(dirname: String, save: Boolean): Option[(String, String)] = {

    val jfc = new JFileChooser()
    jfc.setCurrentDirectory(new File(dirname))
    val res = if (save) {
      jfc.setDialogType(JFileChooser.SAVE_DIALOG)
      jfc.showSaveDialog(null)

    } else {
      jfc.setDialogType(JFileChooser.OPEN_DIALOG)
      jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
      jfc.showOpenDialog(null)
    }


    if (res == JFileChooser.APPROVE_OPTION) {
      val inputFile = jfc.getSelectedFile()
      if (inputFile != null) {
        Some((inputFile.getParent, inputFile.getName))
      } else {
        None
      }
    } else {
      None
    }

  }


  def updateMemoryUsageDisplay() {
    System.gc()
    val runtime = Runtime.getRuntime()
    val mb = 1024 * 1024;
    val totalMemory = runtime.totalMemory() / mb
    val freeMemory  = runtime.freeMemory() / mb
    // val maxMemory   = runtime.maxMemory()  / mb
    statusBar.update((totalMemory - freeMemory) + " / " + totalMemory + " MB", "", "")
  }


}
