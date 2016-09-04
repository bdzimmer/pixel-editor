// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.controller

import java.awt.{Color => AWTColor}
import java.awt.image.BufferedImage


import bdzimmer.pixeleditor.model.{Color, IndexedGraphics, Tileset, Tile}

object TileUtil {


  def drawTile(
      ig: IndexedGraphics,
      tile: Array[Array[Int]],
      x: Int, y: Int,
      palOffset: Int): Unit = {

    for (i <- 0 until tile.length) {
      for (j <- 0 until tile(i).length) {
        ig.setPixel(y + i, x + j, tile(i)(j) + palOffset)
      }
    }
  }


  def drawTile(ig: IndexedGraphics, tile: Array[Array[Int]], x: Int, y: Int): Unit = {
    drawTile(ig, tile, x, y, 0)
  }


  // TODO: customizable transparency, palOffset
  def drawTileTrans(ig: IndexedGraphics, tile: Array[Array[Int]], x: Int, y: Int): Unit = {
    for (i <- 0 until tile.length) {
      for (j <- 0 until tile(i).length) {
        val curColor = tile(i)(j)
        if (curColor != 255) {
          ig.setPixel(y + i, x + j, curColor)
        }
      }
    }
  }


  def drawTileset(ig: IndexedGraphics, tileset: Tileset, palOffsets: Array[Integer]): Unit = {
    drawTileset(ig, tileset.tiles, palOffsets, tileset.width, tileset.height, tileset.tilesPerRow)
  }


  def drawTileset(ig: IndexedGraphics, tileset: Tileset): Unit = {
    val palOffsets = integerArray(tileset.tiles.length)
    drawTileset(ig, tileset.tiles, palOffsets, tileset.width, tileset.height, tileset.tilesPerRow)
  }


  def drawTileset(
      ig: IndexedGraphics,
      tiles: Array[Tile],
      palOffsets: Array[Integer],
      tileWidth: Int, tileHeight: Int, tilesPerRow: Int): Unit = {

    val numRows = (tiles.length + tilesPerRow - 1) / tilesPerRow - 1

    for (i <- 0 until numRows) {
      for (j <- 0 until tilesPerRow) {
        val tileIdx = i * tilesPerRow + j
        if (tileIdx < tiles.length) {
          drawTile(
              ig, tiles(tileIdx).bitmap,
              j * tileWidth, i * tileHeight,
              palOffsets(tileIdx))
        }
      }
    }
  }


  ////////////////

  // update the colors in tile to stay within colorsPerTile from palIdx
  def reIndex(tile: Array[Array[Int]], colorsPerTile: Int): Unit = {

    for (i <- 0 until tile.length) {
      for (j <- 0 until tile(i).length) {
        tile(i)(j) = tile(i)(j) % colorsPerTile
      }
    }

  }


  // draw a grid on an image

  def drawGrid(
      image: BufferedImage, gridWidth: Int, gridHeight: Int,
      color: AWTColor = AWTColor.GRAY): Unit = {
    val gr = image.getGraphics
    gr.setColor(color)

    for (i <- 0 until image.getHeight by gridHeight) {
      gr.drawLine(0, i, image.getWidth - 1, i)
    }
    for (j <- 0 until image.getWidth by gridWidth) {
      gr.drawLine(j, 0, j, image.getHeight - 1)
    }
  }

  // draw numbers on an image

  def drawNumbers(
      image: BufferedImage, length: Int,
      cols: Int, rows: Int, width: Int, height: Int,
      color: AWTColor = AWTColor.GRAY): Unit = {

    val gr = image.getGraphics
    gr.setColor(color)

    for (whichTile <- 0 until length) {
      val xoff = (whichTile % cols) * width + 5
      val yoff = (whichTile / cols) * height + 20
      gr.drawString(whichTile.toString, xoff, yoff)
    }
  }

  ////

  def integerArray(length: Int): Array[Integer] = {
    (0 until length).map(_ => new Integer(0)).toArray
  }

   def colorArray(length: Int): Array[Color] = {
    (0 until length).map(_ => Color(0, 0, 0)).toArray
  }

}
