// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Experimenting with new data model.

package bdzimmer.pixeleditor.model

import scala.collection.mutable.{Buffer, HashMap => MutableMap}
import scala.collection.mutable.ArrayBuffer

import java.awt.Image

import bdzimmer.pixeleditor.view.{PaletteChunk, PaletteChunksWindow}

case class ColorTriple(val r: Int, val g: Int, val b: Int)


object TileCollectionModel {

  case class TileCollection(
    settings: Settings,
    pixels: Pixels,
    vmaps: Buffer[(String, VMap)],
    paletteChunks: Buffer[(String, Array[ColorTriple])]
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
    viewPaletteColumns: Int,
    viewTileColumns: Int
  )

  case class Pixels(
    colorsPerTile: Int,
    tiles: Array[Tile]
  )

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
    chunks: Buffer[(Int, Buffer[ColorTriple])]
  )

}



object Experiment {

  import TileCollectionModel._
  import bdzimmer.pixeleditor.view.TileCollectionWindow

  def main(args: Array[String]): Unit = {

    val settings = new Settings(
        bitsPerChannel  = 6,
        paletteSize   = 256,
        colorsPerTile = 16,
        tileWidth     = 16,
        tileHeight    = 16,
        vmapSize      = 256,
        viewPaletteColumns = 16,
        viewTileColumns    = 16)

    def pal = (0 until 32).map(_ => ColorTriple(0, 0, 0)).toArray
    val names = List("Cave Floor", "Cave Walls", "Baloney", "Cheese", "Snowstorm")
    val chunks = names.map(name => (name, pal.clone())).toBuffer

    val tc = TileCollection(
      settings,
      Pixels(16, Array()),
      Buffer(),
      chunks)

     new TileCollectionWindow("Test", tc, "junk").setVisible(true)

  }
}
