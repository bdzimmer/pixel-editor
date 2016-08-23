// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Experimenting with new data model.

package bdzimmer.pixeleditor.model

import scala.collection.immutable.Seq
import scala.collection.mutable.ArrayBuffer

import java.awt.Image

import bdzimmer.pixeleditor.view.{PaletteChunk, PaletteChunksWindow}

case class ColorTriple(val r: Int, val g: Int, val b: Int)


object TileCollection {

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
    palConfs: Seq[PaletteConf],
    entries:  Seq[VMapEntry]
  )


  case class VMapEntry(
    pixelsIdx: Int,
    palIdx: Int,
    flipX: Boolean,
    flipY: Boolean,
    attribs: TileProperties
  )


  case class PaletteConf(
    chunks: Seq[(Int, Seq[ColorTriple])]
  )

}



object Experiment {

  def main(args: Array[String]): Unit = {

    val settings = new TileCollection.Settings(
        bitsPerChannel  = 6,
        paletteSize   = 256,
        colorsPerTile = 16,
        tileWidth     = 16,
        tileHeight    = 16,
        vmapSize      = 256,
        viewPaletteColumns = 16,
        viewTileColumns    = 16)

    def pal = (0 until 32).map(_ => ColorTriple(0, 0, 0)).toArray
    val chunks = ArrayBuffer("Cave Floor", "Cave Walls", "Baloney", "Cheese", "Snowstorm").map(
          name => PaletteChunk(name, pal.clone()))

    val pc = new PaletteChunksWindow("Palette Chunks", chunks, settings)

    val largePal = pal.clone() ++ pal.clone()
    pc.add(PaletteChunk("Cavern", largePal))
    pc.rebuild()

  }
}
