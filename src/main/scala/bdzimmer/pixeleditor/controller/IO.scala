// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.controller

import scala.collection.mutable.Buffer
import scala.util.Try

import java.io.{File, FileReader, FileWriter, BufferedReader, BufferedWriter}

import javax.xml.bind.DatatypeConverter
import java.nio.{ByteBuffer, ByteOrder}

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, Tile, Tileset, TileProperties}

import bdzimmer.util.StringUtils._


object IO {

  val Trimmer = """^(.*?)\s*$""".r

  case class TileCollectionFiles(filename: String) {
    val settingsFile      = new File(filename / "settings")
    val pixelsFile        = new File(filename / "pixels")
    val vMapsFile         = new File(filename / "vmaps")
    val paletteChunksFile = new File(filename / "palettechunks")
  }


  def readCollection(file: File): TileCollection = {

    val tcf = TileCollectionFiles(file.getAbsolutePath)

    val settings = IO.readSettings(tcf.settingsFile)
    val pixels = IO.readPixels(tcf.pixelsFile, settings)
    val vMaps = IO.readVMaps(tcf.vMapsFile, settings)
    val paletteChunks = IO.readPaletteChunks(tcf.paletteChunksFile)

    TileCollection(
      settings,
      pixels,
      vMaps,
      paletteChunks
    )

  }


  def writeCollection(file: File, tileCollection: TileCollection): Unit = {

    val tcf = TileCollectionFiles(file.getAbsolutePath)
    file.mkdirs()

    IO.writeSettings(tcf.settingsFile, tileCollection.settings)
    IO.writePixels(tcf.pixelsFile, tileCollection.pixels, tileCollection.settings)
    IO.writeVMaps(tcf.vMapsFile, tileCollection.vmaps, tileCollection.settings)
    IO.writePaletteChunks(tcf.paletteChunksFile, tileCollection.paletteChunks)
  }


  def getSetting(sm: Map[String, String], name: String, default: Int): Int = {
    sm.get(name).map(_.toIntSafe(default)).getOrElse(default)
  }


  def readSettings(file: File): Settings = {

    val br = new BufferedReader(new FileReader(file))
    val sm = readMap(br)
    br.close()

    Settings(
      bitsPerChannel  = getSetting(sm, "bitsPerChannel", 5),
      paletteSize     = getSetting(sm, "paletteSize", 256),
      colorsPerTile   = getSetting(sm, "colorsPerTile", 16),
      tileWidth       = getSetting(sm, "tileWidth", 16),
      tileHeight      = getSetting(sm, "tileHeight", 16),
      vMapSize        = getSetting(sm, "vMapSize", 256),
      viewPaletteCols = getSetting(sm, "viewPaletteCols", 16),
      viewTileCols    = getSetting(sm, "viewTileCols", 16)
    )
  }


  def writeSettings(file: File, settings: Settings): Unit = {

     val bw = new BufferedWriter(new FileWriter(file))

     val sm = Map(
      "bitsPerChannel"  -> settings.bitsPerChannel.toString,
      "paletteSize"     -> settings.paletteSize.toString,
      "colorsPerTile"   -> settings.colorsPerTile.toString,
      "tileWidth"       -> settings.tileWidth.toString,
      "tileHeight"      -> settings.tileHeight.toString,
      "vMapSize"        -> settings.vMapSize.toString,
      "viewPaletteCols" -> settings.viewPaletteCols.toString,
      "viewTileCols"    -> settings.viewTileCols.toString
     )

     writeMap(bw, sm)

     bw.close()
  }


  def readPaletteChunks(file: File): Buffer[Named[Array[Color]]] = {

    val br = new BufferedReader(new FileReader(file))
    val sm = readMap(br)
    val count = getSetting(sm, "count", 0)
    val chunks = (0 until count).map(i => {
      val name = br.readLine()
      val pal = base64ToInt(br.readLine())
      pal.grouped(3).map(x => Color(x(0), x(1), x(2))).toArray named name
    }).toBuffer
    br.close()

    chunks
  }


  def writePaletteChunks(file: File, chunks: Buffer[Named[Array[Color]]]): Unit = {

    val bw = new BufferedWriter(new FileWriter(file))

    writeMap(bw, Map("count" -> chunks.length.toString))
    chunks.foreach(chunk => {
      bw.write(chunk.name)
      bw.newLine()
      val pal = chunk.value.flatMap(color => List(color.r, color.g, color.b)).toArray
      bw.write(intToBase64(pal))
      bw.newLine()
    })

    bw.close()
  }


  def readPixels(file: File, settings: Settings): Pixels = {

    val br = new BufferedReader(new FileReader(file))

    val sm = readMap(br)
    val count = getSetting(sm, "count", 0)
    val tiles = (0 until count).map(i => {
      val line = br.readLine()
      IO.base64ToTile(line, settings.tileWidth, settings.tileHeight)
    }).toArray
    val defaultPalOffsets = (0 until count).map(i => {
      new Integer(br.readLine().toIntSafe(0))
    }).toArray

    br.close()

    Pixels(tiles, defaultPalOffsets)

  }


  def writePixels(file: File, pixels: Pixels, settings: Settings): Unit = {

    val bw = new BufferedWriter(new FileWriter(file))

    writeMap(bw, Map("count" -> pixels.tiles.length.toString))
    pixels.tiles.foreach(tile => {
      bw.write(tileToBase64(tile, settings.tileWidth, settings.tileHeight))
      bw.newLine()
    })
    pixels.defaultPalOffsets.foreach(offset => {
      bw.write(offset.toString)
      bw.newLine()
    })

    bw.close()

  }


  def readVMaps(file: File, settings: Settings): Buffer[Named[VMap]] = {
    val br = new BufferedReader(new FileReader(file))
    val count = getSetting(readMap(br), "count", 0)
    val vmaps = (0 until count).map(i => {
      val name = br.readLine()
      val vmap = readVMap(br, settings.vMapSize)
      vmap named name
    }).toBuffer
    br.close()

    // val vmaps: Buffer[Named[VMap]] = Buffer()
    // vmaps +=
    //    VMap(Buffer(), Array.fill(settings.vMapSize)(new VMapEntry(0, 0, false, false, TileProperties(0)))) named "Test"

    return vmaps
  }


  def readVMap(br: BufferedReader, vMapSize: Int): VMap = {

    val countConfs = getSetting(readMap(br), "count", 0)
    val confs = (0 until countConfs).map(i => {
      val name = br.readLine()
      val chunkIdxs = br.readLine().split(", ").map(_.toIntSafe(0)).toBuffer
      PaletteConf(chunkIdxs) named name
    }).toBuffer

    val entries = (0 until vMapSize).map(i => {
      val sm = readMap(br)
      VMapEntry(
        pixelsIdx = getSetting(sm, "pixelsIdx", 0),
        palOffset = getSetting(sm, "palOffset", 0),
        flipX     = sm.getOrElse("flipX", "false").equals("true"),
        flipY     = sm.getOrElse("flipY", "false").equals("true"),
        attribs   = TileProperties(0)
      )
    }).toArray

    VMap(confs, entries)

  }


  def writeVMaps(file: File, vMaps: Buffer[Named[VMap]], settings: Settings): Unit = {
    val bw = new BufferedWriter(new FileWriter(file))
    writeMap(bw, Map("count" -> vMaps.length.toString))
    for (vmap <- vMaps) {
      bw.write(vmap.name)
      bw.newLine()
      writeVMap(bw, vmap.value)
    }
    bw.close()
  }


  def writeVMap(bw: BufferedWriter, vmap: VMap): Unit = {
    writeMap(bw, Map("count" -> vmap.palConfs.length.toString))
    for (conf <- vmap.palConfs) {
      bw.write(conf.name)
      bw.newLine()
      bw.write(conf.value.chunkIdxs.mkString(", "))
      bw.newLine()
    }

    writeMap(bw, Map("count" -> vmap.entries.length.toString))
    for (entry <- vmap.entries) {
      val entryMap = Map(
        "pixelsIdx" -> entry.pixelsIdx.toString,
        "palOffset" -> entry.palOffset.toString,
        "flipX"     -> entry.flipX.toString,
        "flipY"     -> entry.flipY.toString)
      writeMap(bw, entryMap)
    }
  }


  // read key value pairs until a blank line or end of file is reached
  def readMap(br: BufferedReader): Map[String, String] = {

    import scala.collection.mutable.{HashMap => MutableMap}

    var line = br.readLine()
    var lineCount = 1

    val mutMap: MutableMap[String, String] = new MutableMap()

    Try(while (line != null) {

      val Trimmer(trimmed) = line

      if (trimmed.equals("")) {
        throw(new Exception())
      } else {
        val splitted = trimmed.split(":\\s+")
        if (splitted.length >= 2) {
          val field = splitted(0)
          val propVal = splitted.drop(1).mkString(": ")
          mutMap += ((field, propVal))
        }
      }

      ///

      line = br.readLine()
      lineCount = lineCount + 1

    })

    mutMap.toMap

  }


  def writeMap(bw: BufferedWriter, map: Map[String, String]): Unit = {
    map.foreach({case (k, v) => {
      bw.write(k + ": " + v)
      bw.newLine()
    }})
    bw.newLine()
  }


  private def base64ToInt(b64: String): Array[Int] = {
    val bb = DatatypeConverter.parseBase64Binary(b64)
    // ByteBuffer.wrap(bb).asIntBuffer.array
    val ib = ByteBuffer.wrap(bb).order(ByteOrder.BIG_ENDIAN).asIntBuffer
    val ai = new Array[Int](ib.remaining)
    ib.get(ai)
    ai
  }


  private def intToBase64(ia: Array[Int]): String = {
    val bb = ByteBuffer.allocate(ia.length * 4)
    bb.asIntBuffer.put(ia)
    DatatypeConverter.printBase64Binary(bb.array)
  }


  private def base64ToTile(b64: String, tileWidth: Int, tileHeight: Int): Tile = {

    val ia = base64ToInt(b64)
    val tile = Tileset.emptyTile(tileWidth, tileHeight)
    for (row <- 0 until tileHeight) {
      for (col <- 0 until tileWidth) {
        tile.bitmap(row)(col) = ia(row * tileWidth + col)
      }
    }

    tile
  }


  private def tileToBase64(tile: Tile, tileWidth: Int, tileHeight: Int): String = {

    val length = tileWidth * tileHeight
    val ia = new Array[Int](length)
    for (row <- 0 until tileHeight) {
      for (col <- 0 until tileWidth) {
        ia(row * tileWidth + col) = tile.bitmap(row)(col)
      }
    }

    intToBase64(ia)

  }


}
