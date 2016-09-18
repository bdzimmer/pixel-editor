// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Experimenting with new data model.

package bdzimmer.pixeleditor.model

import scala.collection.mutable.{Buffer, HashMap => MutableMap}
import scala.collection.mutable.ArrayBuffer

import java.awt.Image

import bdzimmer.pixeleditor.view.PaletteChunksWindow
import bdzimmer.pixeleditor.controller.TileUtil

case class Color(val r: Int, val g: Int, val b: Int)


object TileCollectionModel {

  case class TileCollection(
    settings: Settings,
    pixels: Pixels,
    vmaps: Buffer[Named[VMap]],
    paletteChunks: Buffer[Named[Array[Color]]]
  )

  case class Settings(

    // data settings
    bitsPerChannel: Int,
    paletteSize: Int,
    colorsPerTile: Int,
    tileWidth: Int,
    tileHeight: Int,
    vMapSize: Int,

    // view settings
    viewPaletteCols: Int,
    viewTileCols: Int
  )

  case class Pixels(
    tiles: Array[Tile],
    defaultPalOffsets: Array[Integer]
  )

  case class VMap(
    palConfs: Buffer[Named[PaletteConf]],
    entries:  Array[VMapEntry]
  )


  case class VMapEntry(
    pixelsIdx: Int,
    palOffset: Int,
    flipX: Boolean,
    flipY: Boolean,
    attribs: TileProperties
  )


  case class PaletteConf(
    chunkIdxs: Buffer[Int]
  )

  case class Named[T](name: String, value: T)

}



object Experiment {

  import TileCollectionModel._
  import bdzimmer.pixeleditor.view.TileCollectionWindow

  def main(args: Array[String]): Unit = {

    val settings = new Settings(
        bitsPerChannel  = 5,
        paletteSize   = 256,
        colorsPerTile = 16,
        tileWidth     = 16,
        tileHeight    = 16,
        vMapSize      = 256,
        viewPaletteCols = 16,
        viewTileCols    = 16)

    val tilesLength = 512

    val pal = (0 until 32).map(_ => Color(0, 0, 0)).toArray
    val names = List("Cave Floor", "Cave Walls", "Baloney", "Cheese")
    val chunks = names.map(name => Named(name, pal.clone())).toBuffer
    chunks += Named("Snowstorm", (0 until 64).map(_ => Color(0, 0, 0)).toArray)

    val tiles = (0 until tilesLength).map(_ =>
      Tileset.emptyTile(settings.tileWidth, settings.tileHeight)).toArray

    val tc = TileCollection(
      settings,
      Pixels(tiles, TileUtil.integerArray(tilesLength)),
      Buffer(Named("Mountainside Cave", VMap(Buffer(), Array()))),
      chunks)

     new TileCollectionWindow("Test", tc, "junk").setVisible(true)

  }
}
