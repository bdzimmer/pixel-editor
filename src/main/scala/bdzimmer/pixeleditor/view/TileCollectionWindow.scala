// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Main window for new TileCollection concept.

package bdzimmer.pixeleditor.view

import java.io.{File, FileReader, FileWriter, BufferedReader, BufferedWriter}

import java.awt.GridLayout
import java.awt.event.{ActionListener, ActionEvent, FocusAdapter, FocusEvent, WindowAdapter, WindowEvent}
import javax.swing.{JFileChooser, JButton, JMenuBar, JMenu, JMenuItem, JPanel, WindowConstants}

import scala.collection.mutable.Buffer

import bdzimmer.pixeleditor.model.TileOptions
import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, TileContainer, TileCollectionModel}
import bdzimmer.pixeleditor.controller.{IO, PalUtil, TileUtil}

import bdzimmer.util.StringUtils._
import bdzimmer.util.PropertiesWrapper


class TileCollectionWindow(
    var name: String,
    var tileCollection: TileCollection,
    var workingDirname: String,
    var filename: String) extends CommonWindow {

  setTitle(name + " - Tile Collection")

  val tileContainer = new TileContainer
  val globalPalette = TileUtil.colorArray(tileCollection.settings.paletteSize)

  var globalPaletteWindow: PaletteWindow = null
  var paletteChunksWindow: PaletteChunksWindow = null
  var pixelsWindow: PixelsWindow = null
  // var vMapWindow: VMapWindow = null
  var vMapsWindow: VMapsWindow = null
  var zoomWindow: ZoomedTileWindow = null

  /// load window locations and initialize

  val wlocsFilename = "windowlocations.properties"
  val wlocs = new PropertiesWrapper(wlocsFilename)

  this.setLocation(
        wlocs("tilecollection.x").map(_.toIntSafe(0)).getOrElse(0),
        wlocs("tilecollection.y").map(_.toIntSafe(0)).getOrElse(0))

  val recentFilesFilename = "recent.properties"

  val recentFiles = new PropertiesWrapper(recentFilesFilename)

  initWindows()

  addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent) {
      saveWindowLocations()
    }
  })

  ////

  build(WindowConstants.EXIT_ON_CLOSE)
  packAndShow(false)

  for {
    wd <- recentFiles("workingdir")
    fn <- recentFiles("workingfile")
  } yield {
    workingDirname = wd
    filename       = fn
  }

  if (workingDirname.length > 0 && filename.length > 0) {
     readCollection(workingDirname / filename)
  }

  //////


  def newCollection(): Unit = {
    saveWindowLocations()
    val settings = SettingsDialog.getSettings()
    tileCollection = TileCollectionModel.emptyCollection(settings, 1024)
    name = "Untitled"
    initWindows()
  }


  def readCollection(filename: String): Unit = {

    saveWindowLocations()

    val file = new File(filename)
    tileCollection = IO.readCollection(file)

    name = file.getName

    setTitle(name + " - Tile Collection")
    initWindows()

  }


  def writeCollection(filename: String): Unit = {

    saveWindowLocations()

    val outFile = new File(filename)
    name = outFile.getName

    IO.writeCollection(outFile, tileCollection)

    setTitle(name + " - Tile Collection")

    pixelsWindow.setTitle(name + " - Pixels")
    vMapsWindow.setTitle(name + " - VMaps")
    paletteChunksWindow.setTitle(name + " - PaletteChunks")

  }



  // Import an old format tileset or spritesheet as a collection

  def importCollection(filename: String): Unit = {

    import bdzimmer.pixeleditor.model.TileProperties
    import bdzimmer.pixeleditor.controller.{TileUtil, OldTilesetLoader}

    saveWindowLocations()

    val attrs = TileOptions.getOptions()
    val loader = new OldTilesetLoader(filename, attrs)
    val tileset = loader.load()

    // create new collection that will gel with this
    val settings = Settings(
        6, 256, 256, attrs.width, attrs.height, attrs.count,
        16, attrs.tilesPerRow)

    val tc = TileCollectionModel.emptyCollection(settings, attrs.count)

    // copy the data from the tileset into the collection

    // 16 palette chunks of 16
    tc.paletteChunks.clear()
    for (i <- 0 until 16) {
      val chunk = TileUtil.colorArray(16)
      tc.paletteChunks += chunk named ("chunk " + i)
    }

    val pal = tileset.palettes(0).colors
    for (i <- 0 until pal.length) {
      val idx = i + tileset.palettes(0).start
      val chunk = tc.paletteChunks(idx / 16).value
      chunk(idx % 16) = pal(i)
    }

    // first palette conf of VMap is all chunks
    tc.vmaps(0).value.palConfs(0) = PaletteConf((0 until 16).toBuffer) named "Default"

    // Copy tiles data into pixels and set up trivial vMap entries
    for (i <- 0 until tc.pixels.tiles.length) {
      tc.pixels.tiles(i) = tileset.tiles(i)
      tc.vmaps(0).value.entries(i) = VMapEntry(i, 0, false, false, TileProperties(0))
    }

    tileCollection = tc
    setTitle(name + " - Tile Collection")
    initWindows()

  }


  def initWindows(): Unit = {

    List(globalPaletteWindow, pixelsWindow, vMapsWindow, paletteChunksWindow, zoomWindow).foreach(x => {
      if (x != null) {
        x.dispose()
      }
    })


    for (i <- 0 until tileCollection.settings.paletteSize) {
      globalPalette(i) = Color(0, 0, 0);
    }

    // globalPalette = (0 until tileCollection.settings.paletteSize).map(_ => Color(0, 0, 0)).toArray

    globalPaletteWindow = new PaletteWindow(
      name + " - Global Palette", globalPalette, tileCollection.settings.bitsPerChannel, null)

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
        name + " - Pixels",
        tileCollection.pixels,
        tileCollection.settings,
        globalPaletteWindow,
        tileContainer,
        zoomWindow)

    vMapsWindow = new VMapsWindow(
      name + " - VMaps",
      tileCollection.vmaps,
      tileCollection.pixels,
      pixelsWindow.updater,
      tileCollection.paletteChunks,
      globalPalette,
      new DumbUpdater(globalPaletteWindow),
      tileContainer,
      zoomWindow,
      tileCollection.settings)

    // initialize Palette Chunks window
    paletteChunksWindow = new PaletteChunksWindow(
        name + " - Palette Chunks",
        tileCollection.paletteChunks,
        tileCollection.settings)

    loadWindowLocations()

    // if the first VMap has a palConf, apply that to the global palette
    if (tileCollection.vmaps.size > 0 && tileCollection.vmaps(0).value.palConfs.size > 0) {
      val palConf = tileCollection.vmaps(0).value.palConfs(0).value.chunkIdxs.map(i => tileCollection.paletteChunks(i).value)
      PalUtil.applyPalConf(globalPalette, palConf)
      pixelsWindow.updater.update()
    }

    globalPaletteWindow.setVisible(true)
    pixelsWindow.setVisible(true)
    vMapsWindow.setVisible(true)
    paletteChunksWindow.setVisible(true)

  }


  def saveWindowLocations(): Unit = {

    wlocs.set("globalpalette.x", globalPaletteWindow.getLocation.getX.toInt.toString)
    wlocs.set("globalpalette.y", globalPaletteWindow.getLocation.getY.toInt.toString)

    wlocs.set("zoom.x", zoomWindow.getLocation.getX.toInt.toString)
    wlocs.set("zoom.y", zoomWindow.getLocation.getY.toInt.toString)

    wlocs.set("pixels.x", pixelsWindow.getLocation.getX.toInt.toString)
    wlocs.set("pixels.y", pixelsWindow.getLocation.getY.toInt.toString)

    wlocs.set("vmaps.x", vMapsWindow.getLocation.getX.toInt.toString)
    wlocs.set("vmaps.y", vMapsWindow.getLocation.getY.toInt.toString)

    wlocs.set("palettechunks.x", paletteChunksWindow.getLocation.getX.toInt.toString)
    wlocs.set("palettechunks.y", paletteChunksWindow.getLocation.getY.toInt.toString)

    wlocs.set("palettechunks.dx", paletteChunksWindow.getSize.getWidth.toInt.toString)
    wlocs.set("palettechunks.dy", paletteChunksWindow.getSize.getHeight.toInt.toString)

    wlocs.set("tilecollection.x", this.getLocation.getX.toInt.toString)
    wlocs.set("tilecollection.y", this.getLocation.getY.toInt.toString)

    wlocs.prop.store(
        new java.io.FileOutputStream(wlocsFilename),
        "created by PixelEditor")
  }


  def loadWindowLocations(): Unit = {

     globalPaletteWindow.setLocation(
        wlocs("globalpalette.x").map(_.toIntSafe(0)).getOrElse(0),
        wlocs("globalpalette.y").map(_.toIntSafe(0)).getOrElse(0))

    zoomWindow.setLocation(
        wlocs("zoom.x").map(_.toIntSafe(0)).getOrElse(0),
        wlocs("zoom.y").map(_.toIntSafe(0)).getOrElse(0))

    pixelsWindow.setLocation(
        wlocs("pixels.x").map(_.toIntSafe(0)).getOrElse(0),
        wlocs("pixels.y").map(_.toIntSafe(0)).getOrElse(0))

    vMapsWindow.setLocation(
        wlocs("vmaps.x").map(_.toIntSafe(0)).getOrElse(0),
        wlocs("vmaps.y").map(_.toIntSafe(0)).getOrElse(0))

    paletteChunksWindow.setLocation(
        wlocs("palettechunks.x").map(_.toIntSafe(0)).getOrElse(0),
        wlocs("palettechunks.y").map(_.toIntSafe(0)).getOrElse(0))

    paletteChunksWindow.setSize(
        wlocs("palettechunks.dx").map(_.toIntSafe(0)).getOrElse(480),
        wlocs("palettechunks.dy").map(_.toIntSafe(0)).getOrElse(320))

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
        TileCollectionWindow.fileChooser(workingDirname, save = false).foreach({case (wd, fn) => {
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
        TileCollectionWindow.fileChooser(workingDirname, save = true).foreach({case (wd, fn) => {
          writeCollection(wd / fn)
          workingDirname = wd
          filename       = fn
        }})
      }
    })

    val jmImport = new JMenuItem("Import")
    jmImport.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent) {
        TileCollectionWindow.fileChooser(workingDirname, save = false).foreach({case (wd, fn) =>  {
          importCollection(wd / fn)
          // workingDirname = wd
          // filename       = fn + "_update"
        }})
      }
    })


    val jmExit = new JMenuItem("Exit")
    jmExit.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent) {
        saveWindowLocations()
        recentFiles.set("workingdir", workingDirname)
        recentFiles.set("workingfile", filename)
        recentFiles.prop.store(
          new java.io.FileOutputStream(recentFilesFilename),
          "created by PixelEditor")
        sys.exit()
      }
    })

    fileMenu.add(jmNew)
    fileMenu.add(jmOpen)
    fileMenu.add(jmReload)
    fileMenu.addSeparator()
    fileMenu.add(jmSave)
    fileMenu.add(jmSaveAs)
    fileMenu.addSeparator()
    fileMenu.add(jmImport)
    fileMenu.addSeparator()
    fileMenu.add(jmExit)
    mainMenu.add(fileMenu)

    return mainMenu

  }


  override def buildPanel(): JPanel = {
    val panel = new JPanel()
    panel.setLayout(new GridLayout(4, 1, 5, 5))

    val globalPaletteButton = new JButton("Global Palette")
    globalPaletteButton.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent): Unit = {
        globalPaletteWindow.setVisible(true)
        globalPaletteWindow.toFront()
      }
    })
    panel.add(globalPaletteButton)

    val paletteChunksButton = new JButton("Palette Chunks")
    paletteChunksButton.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent): Unit = {
        paletteChunksWindow.setVisible(true)
        paletteChunksWindow.toFront()
      }
    })
    panel.add(paletteChunksButton)

    val pixelsButton = new JButton("Pixels")
    pixelsButton.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent): Unit = {
        pixelsWindow.setVisible(true)
        pixelsWindow.toFront()
      }
    })
    panel.add(pixelsButton)

    // TODO: just for testing
    val vMapButton = new JButton("VMaps")
    vMapButton.addActionListener(new ActionListener() {
      def actionPerformed(ae: ActionEvent): Unit = {
        vMapsWindow.setVisible(true)
        vMapsWindow.toFront()
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


object TileCollectionWindow {

  // TODO: put this filechooser code somewhere more general
  def fileChooser(dirname: String, save: Boolean): Option[(String, String)] = {

    val jfc = new JFileChooser()
    jfc.setCurrentDirectory(new File(dirname))
    val res = if (save) {
      jfc.setDialogType(JFileChooser.SAVE_DIALOG)
      jfc.showSaveDialog(null)

    } else {
      jfc.setDialogType(JFileChooser.OPEN_DIALOG)
      jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES)
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

}
