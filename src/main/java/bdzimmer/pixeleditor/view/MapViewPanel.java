// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Separating out map view so that I can use the map view for other things
// 2014-08-16

package bdzimmer.pixeleditor.view;

import bdzimmer.pixeleditor.model.IndexedGraphics;
import bdzimmer.pixeleditor.model.Map;
import bdzimmer.pixeleditor.model.Tile;
import bdzimmer.pixeleditor.model.Tileset;
import bdzimmer.pixeleditor.model.TileProperties;
import bdzimmer.pixeleditor.model.Color;
import bdzimmer.pixeleditor.controller.TileUtil;

import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JPanel;


public class MapViewPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private IndexedGraphics dosGraphics;

  private Tile[] tiles;
  private int width;
  private int height;
  private TileProperties[] properties;
  private Color[] rgbPalette;

  private boolean parallaxEdit = false;

  private boolean dispOver = true;
  private boolean dispBack = true;
  private boolean dispBounds = false;
  private boolean dispGridlines = false;

  private Map map;

  private int numVerticalTiles = 12;
  private int numHorizontalTiles = 20;

  public int vud = 0;
  public int vlr = 0;
  public int scale = 3;

  /**
   * Create a new map view.
   *
   * @param map           map for the view
   * @param tileSet       tiles to use when displaying the map
   * @param rgbPalette    palette for displaying the map
   */
  public MapViewPanel(Map map, Tile[] tiles, TileProperties[] properties, Color[] rgbPalette) {
    this.map = map;

    setTiles(tiles, properties);

    this.rgbPalette = rgbPalette;

    dosGraphics = createDosGraphics();
    add(dosGraphics, BorderLayout.SOUTH);
  }

  // ## --------------------------------------------

  /**
   * Update the DosGraphics instance (used when changing scale).
   */
  public void updateGraphics() {
    remove(dosGraphics);
    dosGraphics = createDosGraphics();
    add(dosGraphics);
  }


  private IndexedGraphics createDosGraphics() {

    IndexedGraphics dg =  new IndexedGraphics(
        numVerticalTiles * height,
        numHorizontalTiles * width,
        scale);

    dg.setGridDimensions(width, height);
    dg.setShowGrid(dispGridlines);
    dg.setPalette(rgbPalette);
    dg.updateClut();

    return dg;
  }


  private void drawMap() {
    if (this.tiles != null) {
      // Draw map on screen.

      if (this.parallaxEdit) {

        for (int i = 0; i < 12; i++) {
          for (int j = 0; j < 20; j++) {
            int curTile;
            if ((vud + i <= 23) && (vud + i >= 0)
                && (vlr + j <= 39) && (vlr + j >= 0)) {
              curTile = map.paraMap[i + vud][j + vlr];
            } else {
              curTile = 0;
            }
            TileUtil.drawTile(
            	dosGraphics,
                tiles[curTile].bitmap(),
                j * width,
                i * height);

          }
        }

      } else {

        for (int i = 0; i < 12; i++) {
          for (int j = 0; j < 20; j++) {
            if (this.dispBack) {
              int curTile;
              if ((vud + i <= 127) && (vud + i >= 0)
                  && (vlr + j <= 127) && (vlr + j >= 0)) {
                curTile = map.map[i + vud][j + vlr];
              } else {
                curTile = 0;
              }
              TileUtil.drawTile(
            	  dosGraphics,
                  tiles[curTile].bitmap(),
                  j * width,
                  i * height);
            } else {
              TileUtil.drawTile(
                  dosGraphics,
                  new int[height][width],
                  j * width,
                  i * height);
            }
          }
        }

        if (this.dispOver) {
          for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 20; j++) {
              int curTile;
              if ((vud + i <= 127) && (vud + i >= 0)
                  && (vlr + j <= 127) && (vlr + j >= 0)) {
                curTile = map.overMap[i + vud][j + vlr];
              } else {
                curTile = 0;
              }
              if (curTile > 0) {
                TileUtil.drawTileTrans(
                    dosGraphics,
                    tiles[curTile].bitmap(),
                    j * width,
                    i * height);
              }
            }
          }
        }

      }

    }
  }

  /**
   * Paint the component. Draws the map, updates the DosGraphics CLUT, and repaints
   * the DosGraphics. Also draws bounds and tile properties on top.
   */
  public void paint(Graphics graphics) {

    // if you forget this....horrible flickering
    super.paint(graphics);

    drawMap();
    // System.out.println("Drew map");
    dosGraphics.updateClut();
    // System.out.println("updated clut");
    dosGraphics.repaint();
    Graphics dgGraphics = dosGraphics.getImage().getGraphics();

    // Bounds drawing
    if (this.dispBounds && this.tiles != null) {

      dgGraphics.setColor(new java.awt.Color(dosGraphics.getPalettePacked()[255]));
      for (int i = 0; i < 12; i++) {
        for (int j = 0; j < 20; j++) {

          if (this.dispBack) {
            int curTile;
            if ((vud + i) <= 127 && (vud + i) >= 0
                && (vlr + j) <= 127 && (vlr + j) >= 0) {
              curTile = map.map[i + vud][j + vlr];
            } else {
              curTile = 0;
            }

            if ((properties[curTile].value() & 1) == 0) {
              dgGraphics.drawLine(
                  j * width  * scale,
                  i * height * scale + height * scale - 1,
                  j * width  * scale + width  * scale - 1,
                  i * height * scale + height * scale - 1);
            }
            if ((properties[curTile].value() & 2) == 0) {
              dgGraphics.drawLine(
                  j * width  * scale,
                  i * height * scale,
                  j * width  * scale,
                  i * height * scale + height * scale - 1);
            }
            if ((properties[curTile].value() & 4) == 0) {
              dgGraphics.drawLine(
                  j * width  * scale + width  * scale - 1,
                  i * height * scale,
                  j * width  * scale + width  * scale - 1,
                  i * height * scale + height * scale - 1);
            }
            if ((properties[curTile].value() & 8) == 0) {
              dgGraphics.drawLine(
                  j * width  * scale,
                  i * height * scale,
                  j * width  * scale + width  * scale - 1,
                  i * height * scale);
            }
            if ((properties[curTile].value() & 16) != 0) {
              dgGraphics.drawLine(
                  j * width  * scale,
                  i * height * scale,
                  j * width  * scale + width * scale - 1,
                  i * height * scale + height * scale - 1);
            }

          }
        }
      }

      dgGraphics.setColor(new java.awt.Color(dosGraphics.getPalettePacked()[10]));

      if (dispOver) {
        for (int i = 0; i < 12; i++) {
          for (int j = 0; j < 20; j++) {
            int curTile;
            if (((vud + i) <= 127) && ((vud + i) >= 0) && ((vlr + j) <= 127)
                && ((vlr + j) >= 0)) {
              curTile = map.overMap[i + vud][j + vlr];
            } else {
              curTile = 0;
            }
            if ((properties[curTile].value() & 1) == 0) {
              dgGraphics.drawLine(
                  j * width  * scale,
                  i * height * scale + height * scale - 1,
                  j * width  * scale + width  * scale - 1,
                  i * height * scale + height * scale - 1);
            }
            if ((properties[curTile].value() & 2) == 0) {
              dgGraphics.drawLine(
                  j * width  * scale,
                  i * height * scale,
                  j * width  * scale,
                  i * height * scale + height * scale - 1);
            }
            if ((properties[curTile].value() & 4) == 0) {
              dgGraphics.drawLine(
                  j * width  * scale + width  * scale - 1,
                  i * height * scale,
                  j * width  * scale + width  * scale - 1,
                  i * height * scale + height * scale - 1);
            }
            if ((properties[curTile].value() & 8) == 0) {
              dgGraphics.drawLine(
                  j * width  * scale,
                  i * height * scale,
                  j * width  * scale + width  * scale - 1,
                  i * height * scale);
            }
            if ((properties[curTile].value() & 16) != 0) {
              dgGraphics.drawLine(
                  j * width  * scale,
                  i * height * scale,
                  j * width  * scale + width  * scale - 1,
                  i * height * scale + height * scale - 1);
            }
          }
        }
      }
    }

  }

  // ###--------Getters and setters---------------

  public void setMap(Map map) {
    this.map = map;
  }



  public int getScale() {
    return this.scale;
  }

  public int getNumVerticalTiles() {
    return numVerticalTiles;
  }

  public void setNumVerticalTiles(int numVerticalTiles) {
    this.numVerticalTiles = numVerticalTiles;
  }

  public int getNumHorizontalTiles() {
    return numHorizontalTiles;
  }

  public void setNumHorizontalTiles(int numHorizontalTiles) {
    this.numHorizontalTiles = numHorizontalTiles;
  }

  public boolean isDispBack() {
    return dispBack;
  }

  public void setDispBack(boolean dispBack) {
    this.dispBack = dispBack;
  }

  public void setDispOver(boolean dispOver) {
    this.dispOver = dispOver;
  }

  public boolean isDispBounds() {
    return dispBounds;
  }

  public void setDispBounds(boolean dispBounds) {
    this.dispBounds = dispBounds;
  }

  public void setDispGridlines(boolean dispGridlines) {
    this.dispGridlines = dispGridlines;
    this.dosGraphics.setShowGrid(this.dispGridlines);
  }

  public boolean isDispOver() {
    return dispOver;
  }

  public boolean isParallaxEdit() {
    return parallaxEdit;
  }

  public void setParallaxEdit(boolean parallaxEdit) {
    this.parallaxEdit = parallaxEdit;
  }

  public void setTiles(Tile[] tiles, TileProperties[] properties) {
    this.tiles = tiles;
    this.properties = properties;
    this.width = tiles[0].bitmap()[0].length;
    this.height = tiles[0].bitmap().length;
  }

  public IndexedGraphics getDosGraphics() {
    return this.dosGraphics;
  }

}
