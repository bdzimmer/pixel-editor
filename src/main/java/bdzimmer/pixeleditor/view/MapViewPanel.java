// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Separating out map view so that I can use the map view for other things
// 2014-08-16

package bdzimmer.pixeleditor.view;

import bdzimmer.pixeleditor.model.DosGraphics;
import bdzimmer.pixeleditor.model.Map;
import bdzimmer.pixeleditor.model.Tileset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;


public class MapViewPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private DosGraphics dosGraphics;

  private Tileset tileset;
  private int[][] rgbPalette;

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
  public MapViewPanel(Map map, Tileset tileset, int[][] rgbPalette) {
    this.map = map;
    this.tileset = tileset;
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


  private DosGraphics createDosGraphics() {

    DosGraphics dg =  new DosGraphics(
        numVerticalTiles * tileset.height(),
        numHorizontalTiles * tileset.width(),
        scale);

    dg.setGridDimensions(tileset.width(), tileset.height());
    dg.setShowGrid(dispGridlines);
    dg.setRgbPalette(rgbPalette);
    dg.updateClut();

    return dg;
  }


  private void drawMap() {
    if (this.tileset != null) {
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
            this.dosGraphics.drawTile(
                tileset.tiles()[curTile].pixels(),
                i* tileset.height(), j * tileset.width());

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
              this.dosGraphics.drawTile(
                  tileset.tiles()[curTile].pixels(),
                  i * this.tileset.height(), j * tileset.width());
            } else {
              this.dosGraphics.drawTile(
                  new int[tileset.height()][tileset.width()],
                  i * tileset.height(), j * tileset.width());
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
                this.dosGraphics.drawTileTrans(
                    tileset.tiles()[curTile].pixels(),
                    i * tileset.height(),
                    j * tileset.width());
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
    Graphics dgGraphics = dosGraphics.getBuffer().getGraphics();

    // Bounds drawing
    if (this.dispBounds && this.tileset != null) {

      dgGraphics.setColor(new Color(dosGraphics.getPalette()[255]));
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

            if ((tileset.properties()[curTile].value() & 1) == 0) {
              dgGraphics.drawLine(
                  j * tileset.width()  * scale,
                  i * tileset.height() * scale + tileset.height() * scale - 1,
                  j * tileset.width()  * scale + tileset.width()  * scale - 1,
                  i * tileset.height() * scale + tileset.height() * scale - 1);
            }
            if ((tileset.properties()[curTile].value() & 2) == 0) {
              dgGraphics.drawLine(
                  j * tileset.width()  * scale,
                  i * tileset.height() * scale,
                  j * tileset.width()  * scale,
                  i * tileset.height() * scale + tileset.height() * scale - 1);
            }
            if ((tileset.properties()[curTile].value() & 4) == 0) {
              dgGraphics.drawLine(
                  j * tileset.width()  * scale + tileset.width()  * scale - 1,
                  i * tileset.height() * scale,
                  j * tileset.width()  * scale + tileset.width()  * scale - 1,
                  i * tileset.height() * scale + tileset.height() * scale - 1);
            }
            if ((tileset.properties()[curTile].value() & 8) == 0) {
              dgGraphics.drawLine(
                  j * tileset.width()  * scale,
                  i * tileset.height() * scale,
                  j * tileset.width()  * scale + tileset.width()  * scale - 1,
                  i * tileset.height() * scale);
            }
            if ((tileset.properties()[curTile].value() & 16) != 0) {
              dgGraphics.drawLine(
                  j * tileset.width()  * scale,
                  i * tileset.height() * scale,
                  j * tileset.width()  * scale + tileset.width()  * scale - 1,
                  i * tileset.height() * scale + tileset.height() * scale - 1);
            }

          }
        }
      }

      dgGraphics.setColor(new Color(dosGraphics.getPalette()[10]));

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
            if ((tileset.properties()[curTile].value() & 1) == 0) {
              dgGraphics.drawLine(
                  j * tileset.width()  * scale,
                  i * tileset.height() * scale + tileset.height() * scale - 1,
                  j * tileset.width()  * scale + tileset.width()  * scale - 1,
                  i * tileset.height() * scale + tileset.height() * scale - 1);
            }
            if ((tileset.properties()[curTile].value() & 2) == 0) {
              dgGraphics.drawLine(
                  j * tileset.width()  * scale,
                  i * tileset.height() * scale,
                  j * tileset.width()  * scale,
                  i * tileset.height() * scale + tileset.height() * scale - 1);
            }
            if ((tileset.properties()[curTile].value() & 4) == 0) {
              dgGraphics.drawLine(
                  j * tileset.width()  * scale + tileset.width()  * scale - 1,
                  i * tileset.height() * scale,
                  j * tileset.width()  * scale + tileset.width()  * scale - 1,
                  i * tileset.height() * scale + tileset.height() * scale - 1);
            }
            if ((tileset.properties()[curTile].value() & 8) == 0) {
              dgGraphics.drawLine(
                  j * tileset.width()  * scale,
                  i * tileset.height() * scale,
                  j * tileset.width()  * scale + tileset.width()  * scale - 1,
                  i * tileset.height() * scale);
            }
            if ((tileset.properties()[curTile].value() & 16) != 0) {
              dgGraphics.drawLine(
                  j * tileset.width()  * scale,
                  i * tileset.height() * scale,
                  j * tileset.width()  * scale + tileset.width()  * scale - 1,
                  i * tileset.height() * scale + tileset.height() * scale - 1);
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

  public void setTileset(Tileset tileset) {
    this.tileset = tileset;
  }

  public DosGraphics getDosGraphics() {
    return this.dosGraphics;
  }

}
