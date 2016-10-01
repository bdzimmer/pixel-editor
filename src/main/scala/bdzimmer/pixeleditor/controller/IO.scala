// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.controller

import scala.collection.mutable.Buffer
import scala.util.Try

import java.io.{File, FileReader, FileWriter, BufferedReader, BufferedWriter}

import javax.xml.bind.DatatypeConverter
import java.nio.ByteBuffer

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Color, Tile, Tileset}

import bdzimmer.util.StringUtils._


object IO {

  val Trimmer = """^(.*?)\s*$""".r

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


  def readVMaps(file: File, settings: Settings): Buffer[Named[VMap]] = {
    // TODO: implement readVMaps
    return Buffer()
  }


  def readPaletteChunks(file: File): Buffer[Named[Array[Color]]] = {

    val br = new BufferedReader(new FileReader(file))
    val sm = readMap(br)
    val count = getSetting(sm, "count", 0)
    val chunks = (0 until count).map(i => {
      val name = br.readLine()
      val pal = base64ToInt(br.readLine())
      Named(
        name,
        pal.grouped(3).map(x => Color(x(0), x(1), x(2))).toArray
      )
    }).toBuffer
    br.close()

    chunks
  }

  //////


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


  def writeVMaps(file: File, vMaps: Buffer[Named[VMap]], settings: Settings): Unit = {
    // TODO: implement writeVMaps
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


  private def base64ToInt(b64: String): Array[Int] = {
    val bb = DatatypeConverter.parseBase64Binary(b64)
    ByteBuffer.wrap(bb).asIntBuffer.array
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


}
