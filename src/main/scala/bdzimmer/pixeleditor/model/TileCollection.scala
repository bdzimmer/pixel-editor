// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Experimenting with new data model.

package bdzimmer.pixeleditor.model

import scala.collection.mutable.{Buffer, HashMap => MutableMap}
import scala.collection.mutable.ArrayBuffer

import java.awt.Image

import bdzimmer.pixeleditor.view.PaletteChunksWindow

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
    vmapSize: Int,

    // view settings
    viewPaletteCols: Int,
    viewTileCols: Int
  )

  case class Pixels(tiles: Array[Tile])

  case class VMap(
    palConfs: Buffer[PaletteConf],
    entries:  Buffer[VMapEntry]
  )


  case class VMapEntry(
    pixelsIdx: Int,
    palIdx: Int,
    flipX: Boolean,
    flipY: Boolean,
    attribs: TileProperties
  )


  case class PaletteConf(
    chunks: Buffer[(Int, Buffer[Color])]
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
        vmapSize      = 256,
        viewPaletteCols = 16,
        viewTileCols    = 16)

    val pal = (0 until 32).map(_ => Color(0, 0, 0)).toArray
    val names = List("Cave Floor", "Cave Walls", "Baloney", "Cheese", "Snowstorm")
    val chunks = names.map(name => Named(name, pal.clone())).toBuffer

    val tiles = (0 until 512).map(_ =>
      Tileset.emptyTile(settings.tileWidth, settings.tileHeight)).toArray

    val tc = TileCollection(
      settings,
      Pixels(tiles),
      Buffer(),
      chunks)

     new TileCollectionWindow("Test", tc, "junk").setVisible(true)

  }
}
