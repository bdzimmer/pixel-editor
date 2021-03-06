// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Tests for Tileset

package bdzimmer.pixeleditor.model

import org.scalatest.FunSuite
import java.io.File

import javax.imageio.ImageIO

import bdzimmer.pixeleditor.controller.OldTilesetLoader

import bdzimmer.util.StringUtils._
import bdzimmer.util.TempDirectory


class TilesetSuite extends FunSuite with TempDirectory {

  val resourceDir = getClass.getResource("/pixel").getPath

  // test new indexed png export
  test("load and save using Tileset") {

    val inputFilename = resourceDir / "amex.til"
    val outputFilename = tempDirname / "amex_new.til"
    val imageFilename = new File(tempDirname / "amex_new.png")
    val imageRGBFilename = new File(tempDirname / "amex_new_rgb.png")

    val tileAttrs = TileOptions.types.get("Tiles").get  // I don't even care

    // load the tileset
    val loader = new OldTilesetLoader(inputFilename, tileAttrs)
    val tileset = loader.load()

    // save a copy
    val saver = new OldTilesetLoader(outputFilename, tileAttrs)
    saver.save(tileset)

    // load the copy
    val newTileset = saver.load()

    // save, load, and test an indexed PNG
    val image = newTileset.image(0)
    ImageIO.write(image, "png", imageFilename)
    val loadedImage = ImageIO.read(imageFilename)
    assert(loadedImage.getColorModel.getPixelSize == 8)

    // save, load, and test an RGB PNG
    val imageRGB = newTileset.imageRGB(0)
    ImageIO.write(imageRGB, "png", imageRGBFilename)
    val loadedImageRGB = ImageIO.read(imageRGBFilename)
    assert(loadedImageRGB.getColorModel.getPixelSize == 24)

  }

}
