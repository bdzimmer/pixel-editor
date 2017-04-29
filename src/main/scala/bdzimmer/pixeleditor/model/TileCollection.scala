// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Experimenting with new data model.

package bdzimmer.pixeleditor.model

import scala.collection.mutable.{Buffer, HashMap => MutableMap}
import scala.collection.mutable.ArrayBuffer

import java.awt.Image

import bdzimmer.pixeleditor.view.PaletteChunksWindow
import bdzimmer.pixeleditor.controller.TileUtil

import TileCollectionModel.NamedUtils

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
    attribs: TileProperties // currently not used
  )


  case class PaletteConf(
    chunkIdxs: Buffer[Int]
  )

  case class Named[T](name: String, value: T)

  implicit class NamedUtils[T](val x: T) extends AnyVal {
    def named(s: String): Named[T] = {
      Named(s, x)
    }
  }


  def emptyCollection(settings: Settings, tilesLength: Int): TileCollection = {

    val pal = TileUtil.colorArray(settings.paletteSize)

    val tiles = (0 until tilesLength).map(_ =>
      Tileset.emptyTile(settings.tileWidth, settings.tileHeight)).toArray

    val pixels = Pixels(tiles, TileUtil.integerArray(tilesLength))
    val vMaps: Buffer[Named[VMap]] = Buffer()
    val chunks: Buffer[Named[Array[Color]]] = Buffer()

    val tc = TileCollection(settings, pixels, vMaps, chunks)

    tc.vmaps +=
        VMap(
             Buffer(PaletteConf(Buffer(0)) named "Pal Conf 0"),
             Array.fill(settings.vMapSize)(new VMapEntry(0, 0, false, false, TileProperties(0)))
        ) named "VMap 0"

    tc.paletteChunks += TileUtil.colorArray(16) named "Pal Chunk 0"

    tc

  }


}



object Experiment {

  import javax.swing.UIManager

  import TileCollectionModel._
  import bdzimmer.pixeleditor.view.TileCollectionWindow
  import bdzimmer.pixeleditor.view.SettingsDialog

  def main(args: Array[String]): Unit = {

    val tc = emptyCollection(SettingsDialog.Default, 512)

    // UIManager.setLookAndFeel(
    //     UIManager.getSystemLookAndFeelClassName())

    new TileCollectionWindow(
        "Test", tc, "junk").setVisible(true)

  }
}
