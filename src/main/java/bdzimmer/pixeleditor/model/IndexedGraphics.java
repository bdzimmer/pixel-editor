// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.model;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;

import javax.swing.JPanel;

import bdzimmer.pixeleditor.model.Color;
import bdzimmer.pixeleditor.controller.TileUtil;

public class IndexedGraphics extends JPanel {
  private static final long serialVersionUID = 1; // Meaningless junk.

  private final BufferedImage screenBuffer;
  private final int[] buffer;

  private final int scale;
  private final int height;
  private final int width; // size

  // private final int[] palette = new int[256];
  // private int[][] rgbPalette = new int[256][3];

  // private final int bitsPerChannel;
  private final int colorFactor;

  private int[] palettePacked;
  private Color[] palette;

  private boolean showGrid = false;
  private int gridWidth  = 0;
  private int gridHeight = 0;



  /**
   * Create a new DosGraphics instance.
   *
   * @param height     vertical dimension
   * @param width      horizontal dimension
   * @param scale      pixel scaling
   */
  public IndexedGraphics(
      Color[] palette,
      int bitsPerChannel,
      int height, int width, int scale) {

    // this.bitsPerChannel = bitsPerChannel;
    this.colorFactor = (1 << (8 - bitsPerChannel));

    this.height = height;
    this.width = width;
    this.scale = scale;

    this.palette = palette;
    this.palettePacked = new int[palette.length];
    this.updateClut();

    screenBuffer = new BufferedImage(
        this.width * this.scale, this.height * this.scale, BufferedImage.TYPE_INT_RGB);
    buffer = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();

    setPreferredSize(new Dimension(this.width * this.scale, this.height * this.scale));
    setIgnoreRepaint(false);
    setVisible(true);

  }


  public IndexedGraphics() {
    // VGA mode 13h
    this(240, 320, 2);
  }


  public IndexedGraphics(int height, int width, int scale) {
    // default to old VGA settings
    this(TileUtil.colorArray(256), 6, height, width, scale);
  }


  /**
   * Set a pixel at a given location.
   *
   * @param y         vertical position
   * @param x         horizontal position
   * @param myColor   color to set
   */
  public void setPixel(int y, int x, int colorIndex) {

    // Now with bounds checks!
    if (y < 0 || y >= height || x < 0 || x >= width) {
      return;
    }

    int rowLength = width * scale;
    int upperleft = y * scale * rowLength + x * scale;
    int curColor = palettePacked[colorIndex];

    if (this.scale == 2) {

      buffer[upperleft] = curColor;
      buffer[upperleft + 1] = curColor;
      buffer[upperleft + rowLength] = curColor;
      buffer[upperleft + rowLength + 1] = curColor;

    } else if (this.scale == 3) {

      buffer[upperleft] = curColor;
      buffer[upperleft + 1] = curColor;
      buffer[upperleft + 2] = curColor;

      buffer[upperleft + rowLength] = curColor;
      buffer[upperleft + rowLength + 1] = curColor;
      buffer[upperleft + rowLength + 2] = curColor;

      rowLength *= 2;

      buffer[upperleft + rowLength] = curColor;
      buffer[upperleft + rowLength + 1] = curColor;
      buffer[upperleft + rowLength + 2] = curColor;

    } else {

      for (int i = 0; i < scale; i++) {
        int curRowOffset = rowLength * i;
        for (int j = 0; j < scale; j++) {
          this.buffer[upperleft + curRowOffset + j] = curColor;
        }
      }
    }
  }


  public BufferedImage getImage() {
    return this.screenBuffer;
  }

  public int getScale() {
    return this.scale;
  }

  /**
   * Recalculate the CLUT from the RGB palette. Requires redrawing of all
   * DosGraphics instances sharing this palette to take effect.
   *
   */
  public void updateClut() {
    // this also requires redrawing of all DosGraphics' to take effect
    for (int i = 0; i < palette.length; i++) {
      palettePacked[i] = 255 << 24 | (palette[i].r() * colorFactor) << 16 | (palette[i].g() * colorFactor) << 8 | (palette[i].b() * colorFactor);
    }

  }

  public int[] getPalettePacked() {
    return this.palettePacked;
  }

  public Color[] getPalette() {
    return this.palette;
  }

  public void setPalette(Color[] palette) {
    this.palette = palette;
    this.palettePacked = new int[this.palette.length];
  }

  public boolean getShowGrid() {
    return showGrid;
  }

  public void setShowGrid(boolean showGrid) {
    this.showGrid = showGrid;
  }

  public void setGridDimensions(int gridWidth, int gridHeight) {
    this.gridWidth  = gridWidth;
    this.gridHeight = gridHeight;
  }

  // get an IndexColorModel for part of the range
  public IndexColorModel getIndexColorModel(int start, int end) {

    byte[] r = new byte[256];
    byte[] g = new byte[256];
    byte[] b = new byte[256];

    for (int i = start; i <= end; i++) {
      r[i] = (byte)((palette[i].r() * colorFactor) & 0xFF);
      g[i] = (byte)((palette[i].g() * colorFactor) & 0xFF);
      b[i] = (byte)((palette[i].b() * colorFactor) & 0xFF);
    }

    // weird things happen when you try to set a transparent index (extra argument)
    // it seems that it will always be index 0 in a png, but also strange palette
    // shifts happen if it is set to 256. Seems best to not set this for now.

    return new IndexColorModel(8, 256, r, g, b);

  }

  /**
   * Draw the component.
   */
  public void paintComponent(Graphics graphics) {
    super.paintComponent(graphics); // Draw things in superclass

    graphics.setColor(java.awt.Color.BLACK);
    graphics.fillRect(0, 0, width * 2 - 1, height * 2 - 1);
    graphics.drawImage(screenBuffer, 0, 0, null);
    if (showGrid) drawGrid(graphics);
  }

  // helper function for drawing a grid
  private void drawGrid(Graphics g) {
    int maxIdx = palette.length - 1;
    Color topColor = palette[maxIdx];
    g.setColor(new java.awt.Color(topColor.r() * colorFactor, topColor.g() * colorFactor, topColor.b() * colorFactor));
    for (int i = 0; i < height * scale; i += gridHeight * scale) {
      g.drawLine(0, i, width * scale - 1, i);
    }
    for (int j = 0; j < width * scale; j += gridWidth * scale) {
      g.drawLine(j, 0, j, height * scale - 1);
    }
  }

}