// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Sketching out a new tileset class.
// Trying to separate data from saving / loading operations.

// 2015-12-13: Created.
// 2015-12-14: Bringing in image generation.


package bdzimmer.pixeleditor.model

import java.awt.image.{BufferedImage, IndexColorModel}     // scalastyle:ignore illegal.imports
import java.io.{File, FileInputStream, FileOutputStream}


case class TileProperties(value: Int)    // for now


case class Tile(bitmap: Array[Array[Int]])


case class Palette(start: Int, end: Int, colors: Array[Color], bitsPerChannel: Int) {

  val colorFactor = (1 << (8 - bitsPerChannel))

  def colorModel(transparent: Color): IndexColorModel = {

    val r = new Array[Byte](256)
    val g = new Array[Byte](256)
    val b = new Array[Byte](256)

    for (i <- 0 until colors.length) {
      r(i + start) = ((colors(i).r * colorFactor) & 0xFF).toByte
      g(i + start) = ((colors(i).g * colorFactor) & 0xFF).toByte
      b(i + start) = ((colors(i).b * colorFactor) & 0xFF).toByte
    }

    r(255) = ((transparent.r * colorFactor) & 0xFF).toByte
    g(255) = ((transparent.g * colorFactor) & 0xFF).toByte
    b(255) = ((transparent.b * colorFactor) & 0xFF).toByte

    // weird things happen when you try to set a transparent index (extra argument)
    // it seems that it will always be index 0 in a png, but also strange palette
    // shifts happen if it is set to 256. Seems best to not set this for now.
    new IndexColorModel(8, 256, r, g, b)

  }

  def colorInt(color: Color): Int = {
    255 << 24 | (color.r * colorFactor) << 16 | (color.g * colorFactor) << 8 | (color.b * colorFactor)
  }

}


class Tileset (
    val tiles: Array[Tile],
    val properties: Array[TileProperties],
    val palettes: List[Palette],
    val tilesPerRow: Int) {

  // Need to find the best way to protect this constructor.

  // We want to be able to assume that the tiles are all the same size
  // and probably that the palettes are also the same size.

  val height = tiles(0).bitmap.size
  val width = tiles(0).bitmap(0).size


  def imageRGB(paletteIndex: Int, transparent: Color = Tileset.Transparent): BufferedImage = {
    imageRGB(tilesPerRow, math.ceil(tiles.length.toFloat / tilesPerRow).toInt,
        paletteIndex, transparent)
  }


  def image(paletteIndex: Int, transparent: Color = Tileset.Transparent): BufferedImage = {
    image(tilesPerRow, math.ceil(tiles.length.toFloat / tilesPerRow).toInt,
        paletteIndex, transparent)
  }


  // get a 24-bit image of the tileset
  def imageRGB(
      tilesWide: Int,
      tilesHigh: Int,
      paletteIndex: Int,
      transparentColor: Color): BufferedImage = {

    val curPal = palettes(paletteIndex)
    val fullPal = (0 to 255).map(x => Color(0, 0, 0)).toArray
    for (x <- curPal.start to curPal.end) {
      fullPal(x) = curPal.colors(x - curPal.start)
    }
    fullPal(255) = transparentColor

    val tilesImage = new BufferedImage(
        tilesWide * width, tilesHigh * height, BufferedImage.TYPE_INT_RGB)

    for (whichTile <- 0 until tiles.length) {
      val xoff = (whichTile % tilesWide) * width
      val yoff = (whichTile / tilesWide) * height

      for (y <- 0 until height) {
        for (x <- 0 until width) {
          val color = fullPal(tiles(whichTile).bitmap(y)(x))
          tilesImage.setRGB(xoff + x, yoff + y, curPal.colorInt(color))
        }
      }
    }

    tilesImage
  }

  // get a 256-color indexed image of the tileset
  def image(
      tilesWide: Int,
      tilesHigh: Int,
      paletteIndex: Int,
      transparentColor: Color): BufferedImage = {

    val curPal = palettes(paletteIndex)


    val tilesImage = Tileset.indexedImage(
        tilesWide * width, tilesHigh * height,
        curPal, transparentColor)

    val wr = tilesImage.getRaster

    for (whichTile <- 0 until tiles.length) {
      val xoff = (whichTile % tilesWide) * width
      val yoff = (whichTile / tilesWide) * height
      for (y <- 0 until height) {
        wr.setPixels(xoff, yoff + y, width, 1, tiles(whichTile).bitmap(y))
      }
    }

    tilesImage
  }

}



object Tileset {

  val Transparent = Color(50, 0, 50)


  // create an empty (zeroed) tile of the specified width and height
  def emptyTile(width: Int, height: Int): Tile = {
    val pixels = new Array[Array[Int]](height)
    for (y <- 0 until height) {
      pixels(y) = new Array[Int](width)
    }
    Tile(pixels)
  }


  // create an indexed BufferedImage with a palette
  def indexedImage(
      width: Int, height: Int,
      palette: Palette,
      transparent: Color): BufferedImage = {

    val cm = palette.colorModel(transparent)
    new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, cm)

  }


  // modify an color triple array with a Palette object
  def modPalette(pal: Palette, fulPal: Array[Color]): Unit = {
    (pal.start to pal.end).foreach(i => {
      val color = pal.colors(i - pal.start)
      fulPal(i) = Color(color.r, color.g, color.b)
    })
  }


  // extract a new Palette object from an array of color triples
  def extractPalette(pal: Palette, fulPal: Array[Color]): Palette = {
     pal.copy(colors = (pal.start to pal.end).map(i => fulPal(i)).toArray)
  }

}

