// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Object that holds the current tile bitmap and index.
// Used for copying tiles between tilesets and holding the
// current tile that is being edited in a tile editor and
// placed in in a map window.

package bdzimmer.pixeleditor.model;

public class TileContainer {

  // this is not final because it can change size
  private int[][] tileBitmap = new int[16][16];
  private int tileIndex;

  // getters and setters

  public int[][] getTileBitmap() {
    return tileBitmap;
  }

  public void setTileBitmap(int[][] tileBitmap) {
    this.tileBitmap = tileBitmap;
  }

  public int getTileIndex() {
    return tileIndex;
  }

  public void setTileIndex(int tileIndex) {
    this.tileIndex = tileIndex;
  }

}
