// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.controller

import scala.collection.mutable.Buffer

import bdzimmer.pixeleditor.model.Color


object PalUtil {

  def applyPalConf(pal: Array[Color], chunks: Buffer[Array[Color]]): Unit = {
    var idx = 0
    chunks.foreach(chunk => {
      chunk.foreach(col => {
        pal(idx) = col
        idx = idx + 1
      })
    })
  }


}
