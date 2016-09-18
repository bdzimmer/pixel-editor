// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.controller

import scala.util.Try

import java.io.{File, FileReader, BufferedReader, BufferedWriter}

import javax.xml.bind.DatatypeConverter
import java.nio.ByteBuffer

import bdzimmer.pixeleditor.model.TileCollectionModel._
import bdzimmer.pixeleditor.model.{Tile, Tileset}

import bdzimmer.util.StringUtils._


object IO {

  val Trimmer = """^(.*?)\s*$""".r

  def readSettings(file: File): Settings = {

    val br = new BufferedReader(new FileReader(file))
    val settingsMap = readMap(br)
    br.close()

    def getSetting(name: String, default: Int): Int = {
      settingsMap.get(name).map(_.toIntSafe(default)).getOrElse(default)
    }

    Settings(
      bitsPerChannel  = getSetting("bitsPerChannel", 5),
      paletteSize     = getSetting("paletteSize", 256),
      colorsPerTile   = getSetting("colorsPerTile", 16),
      tileWidth       = getSetting("tileWidth", 16),
      tileHeight      = getSetting("tileHeight", 16),
      vMapSize        = getSetting("vMapSize", 256),
      viewPaletteCols = getSetting("viewPaletteCols", 16),
      viewTileCols    = getSetting("viewTileCols", 16)
    )

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
  }


}