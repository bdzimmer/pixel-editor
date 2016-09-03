// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.controller

import java.awt.{Color => AWTColor}
import java.awt.image.BufferedImage


object TileUtil {

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


}
