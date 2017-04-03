// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Class for drawing and editing a tile or sprite zoomed.

// TODO: default not visible

package bdzimmer.pixeleditor.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import java.util.ArrayList;


import bdzimmer.pixeleditor.controller.FloodFill;
import bdzimmer.pixeleditor.model.IndexedGraphics;
// import bdzimmer.pixeleditor.model.TileProperties;
import bdzimmer.pixeleditor.controller.TileUtil;

public class ZoomedTileWindow extends JFrame {

  private static final long serialVersionUID = 1L;

  private int tileHeight;
  private int tileWidth;
  private int zoomFactor;
  private int[][] tile;
  private Container<Integer> palOffset;
  private int paletteSize;
  private int[][] overlayTile;
  private int penMode;
  private boolean showGridlines;

  private PaletteWindow paletteWindow;
  private ArrayList<Updater> updaters = new ArrayList<Updater>();

  private IndexedGraphics dosGraphics;
  private IndexedGraphics tileTile;
  private JPanel graphicsPanel = new JPanel();

  // private TilesEditorWindow tileWindow;
  private JButton tpTop;
  private JButton tpBottom;
  private JButton tpLeft;
  private JButton tpRight;
  private JButton tpOverlay;
  private JButton tpLeftStair;
  private JButton tpRightStair;
  private JButton togglePen;

  /**
   * Create a new ZoomedTileWindow.
   *
   */
  public ZoomedTileWindow(
      String title,
      int[][] tile,
      Container<Integer> palOffset,
      int paletteSize,
      PaletteWindow paletteWindow) {

    this.zoomFactor = 16;

    this.paletteWindow = paletteWindow;
    this.tile = tile;
    this.palOffset = palOffset;
    this.paletteSize = paletteSize;

    if (tile != null) {
      tileHeight = tile.length;
      tileWidth = tile[0].length;
    } else {
      tileHeight = 16;
      tileWidth = 16;
    }

    dosGraphics = createDosGraphics();

    tileTile = new IndexedGraphics(tileHeight * 3, tileWidth * 3, 2);
    tileTile.setPalette(paletteWindow.getPalette());

    this.setLayout(new BorderLayout(0, 0));

    graphicsPanel.add(dosGraphics, BorderLayout.SOUTH);
    this.add(graphicsPanel);

    graphicsPanel.setToolTipText("<html>right click: grab color<br />left click: set color</html>");

    graphicsPanel.addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent event) { handleClicks(event, 3); }
      public void mouseMoved(MouseEvent arg0) {
        // Do nothing.
      }
    });

    graphicsPanel.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent event) { handleClicks(event, 3); }

      // Only repaint the tileWindow when the mouse is released
      public void mouseReleased(MouseEvent event) {
        // tileWindow.repaint();
        for (Updater updater : ZoomedTileWindow.this.updaters) {
          updater.update();
        }
      }
    });

    JPanel layoutPanel = new JPanel();
    layoutPanel.setLayout(new GridLayout(3, 1, 0, 0)); // for now

    JToolBar buttonsToolBar = new JToolBar();
    buttonsToolBar.setLayout(new GridLayout(4, 4, 0, 0)); // for now

    // Grid button
    final JToggleButton gridShow = new JToggleButton("Grid");
    gridShow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showGridlines = gridShow.isSelected();
        dosGraphics.setShowGrid(showGridlines);
        dosGraphics.repaint();
      }
    });
    gridShow.setSelected(showGridlines);
    buttonsToolBar.add(gridShow);

    // Zoom in and out buttons
    JButton zoomIn = new JButton("+");
    buttonsToolBar.add(zoomIn);
    JButton zoomOut = new JButton("-");
    buttonsToolBar.add(zoomOut);


    ActionListener toolsHandler = new ActionListener() {
      public void actionPerformed(ActionEvent event) { handleTools(event); }
    };

    // Lighten
    JButton lighten = new JButton("Lighten");
    lighten.addActionListener(toolsHandler);
    buttonsToolBar.add(lighten);

    // Darken
    JButton darken = new JButton("Darken");
    darken.addActionListener(toolsHandler);
    buttonsToolBar.add(darken);

    // Set Overlay Tile
    JButton setOverlay = new JButton("Overlay");
    setOverlay.addActionListener(toolsHandler);
    buttonsToolBar.add(setOverlay);

    // Fill
    JButton fill = new JButton("Fill");
    fill.addActionListener(toolsHandler);
    buttonsToolBar.add(fill);

    // Toggle Pen Mode
    togglePen = new JButton("Norm. Pen");
    togglePen.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {

        penMode++;
        if (penMode == 3) {
          penMode = 0;
        }

        if (penMode == 0) {
          togglePen.setText("Norm. Pen");
        } else if (penMode == 1) {
          togglePen.setText("Ovl. Pen");
        } else if (penMode == 2) {
          togglePen.setText("Fill Pen");
        }
        repaint();

      }

    });
    buttonsToolBar.add(togglePen);

    // Flip horizontal
    JButton flipHoriz = new JButton("Flip >");
    flipHoriz.addActionListener(toolsHandler);
    buttonsToolBar.add(flipHoriz);

    // Flip vertical
    JButton flipVert = new JButton("Flip ^");
    flipVert.addActionListener(toolsHandler);
    buttonsToolBar.add(flipVert);

    // Shift horizontal
    JButton shiftHoriz = new JButton("Shift >");
    shiftHoriz.addActionListener(toolsHandler);
    buttonsToolBar.add(shiftHoriz);

    // Shift vertical
    JButton shiftVert = new JButton("Shift ^");
    shiftVert.addActionListener(toolsHandler);
    buttonsToolBar.add(shiftVert);

    JPanel tpP = new JPanel();

    tpP.setLayout(new GridLayout(3, 3, 0, 0)); // for now

    tpTop = new JButton();
    tpBottom = new JButton();
    tpLeft = new JButton();
    tpRight = new JButton();
    tpOverlay = new JButton();
    tpLeftStair = new JButton();
    tpRightStair = new JButton();

    tpP.add(new JLabel(" "));
    tpP.add(tpTop);
    tpP.add(new JLabel(" "));
    tpP.add(tpRight);
    tpP.add(tpOverlay);
    tpP.add(tpLeft);
    tpP.add(tpLeftStair);
    tpP.add(tpBottom);
    tpP.add(tpRightStair);
    tpP.setPreferredSize(new Dimension(128, 128));

    layoutPanel.add(tpP);
    layoutPanel.add(buttonsToolBar);
    layoutPanel.add(tileTile);

    this.add(layoutPanel, BorderLayout.SOUTH);

    /*
    tpBottom.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { xorProperyBits(1); }
    });

    tpRight.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { xorProperyBits(2); }
    });

    tpLeft.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { xorProperyBits(4); }
    });

    tpTop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { xorProperyBits(8); }
    });

    tpOverlay.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { xorProperyBits(16); }
    });

    tpLeftStair.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { xorProperyBits(32); }

    });

    tpRightStair.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) { xorProperyBits(64); }
    });
    */

    zoomIn.addActionListener(new ActionListener() { // Anonymous Listener.
      public void actionPerformed(ActionEvent event) {
        System.out.println("Zooming in.");
        setZoomFactor(zoomFactor + 1);
      }
    });

    zoomOut.addActionListener(new ActionListener() { // Anonymous Listener.
      public void actionPerformed(ActionEvent event) {
        System.out.println("Zooming out.");
        setZoomFactor(zoomFactor - 1);
      }
    });

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    setTitle(title);
    setVisible(true);
    setResizable(false);
    updateGraphics();
    repaint();
  }

  private void handleTools(ActionEvent event) {
    String commandString = event.getActionCommand();

    if ("Lighten".equals(commandString)) {
      for (int i = 0; i < tile.length; i++) {
        for (int j = 0; j < tile[0].length; j++) {
          int curColor = tile[i][j];
          if (curColor != 0 && curColor != 255) {
            curColor++;
            if (curColor == 255) {
              curColor = 1;
            }
            tile[i][j] = curColor;
          }
        }
      }

    } else if ("Darken".equals(commandString)) {
      for (int i = 0; i < tile.length; i++) {
        for (int j = 0; j < tile[0].length; j++) {
          int curColor = tile[i][j];
          if (curColor != 0 && curColor != 255) {
            curColor--;
            if (curColor == 0) {
              curColor = 254;
            }
            tile[i][j] = curColor;
          }
        }
      }

    } else if ("Overlay".equals(commandString)) {
      this.overlayTile = tile;

    } else if ("Fill".equals(commandString)) {
      for (int i = 0; i < tile.length; i++) {
        for (int j = 0; j < tile[0].length; j++) {
          tile[i][j] = paletteWindow.getSelectedIdx();
        }
      }

    } else if ("Flip >".equals(commandString)) {
      int[][] tempTile = copyTile();
      for (int i = 0; i < tile.length; i++) {
        for (int j = 0; j < tile[0].length; j++) {
          tile[i][tile[0].length - j - 1] = tempTile[i][j];
        }
      }

    } else if ("Flip ^".equals(commandString)) {
      int[][] tempTile = copyTile();
      for (int i = 0; i < tile.length; i++) {
        for (int j = 0; j < tile[0].length; j++) {
          tile[tile.length - i - 1][j] = tempTile[i][j];
        }
      }

    } else if ("Shift ^".equals(commandString)) {
      int[][] tempTile = copyTile();
      for (int j = 0; j < tile[0].length; j++) {
        tile[0][j] = tempTile[tile.length - 1][j];
      }
      for (int i = 1; i < tile.length; i++) {
        for (int j = 0; j < tile[0].length; j++) {
          tile[i][j] = tempTile[i - 1][j];
        }
      }

    } else if ("Shift >".equals(commandString)) {
      int[][] tempTile = copyTile();
      for (int i = 0; i < tile.length; i++) {
        for (int j = 0; j < tile[0].length - 1; j++) {
          tile[i][j] = tempTile[i][j + 1];
        }
        tile[i][tile[0].length - 1] = tempTile[i][0];
      }
    }

    repaint();
    // this.tileWindow.repaint();
    for (Updater updater : this.updaters) {
      updater.update();
    }
  }

  /*
  private void xorProperyBits(int bits) {
    if (tileWindow.getTileSet().properties().length > 0) {
      int prop = tileWindow.getTileSet().properties()[currentTile].value();
      tileWindow.getTileSet().properties()[currentTile] = new TileProperties(prop ^ bits);
      updateTileProps();
      System.out.println("toggled tile property");
    } else {
      System.out.println("no tile properties!");
    }
  }
  */


  private int[][] copyTile() {
    int[][] tempTile = new int[this.tile.length][this.tile[0].length];
    for (int i = 0; i < this.tile.length; i++) {
      for (int j = 0; j < this.tile[0].length; j++) {
        tempTile[i][j] = tile[i][j];
      }
    }
    return tempTile;
  }

  private void handleClicks(MouseEvent event, int whichWindow) {

    if (whichWindow == 3) { // zoom window(
      int tud = (int) ((event.getY() - dosGraphics.getY()) / zoomFactor);
      int tlr = (int) ((event.getX() - dosGraphics.getX()) / zoomFactor);

      // System.out.println("In ZoomWindow -- " + tud + " " + tlr);

      if (tud < this.tile.length && tlr < this.tile[0].length) {
        if (!event.isMetaDown()) { // right click

          int colorIdx = 0; // color to set
          if (penMode == 0 || penMode == 1) { // normal pen
        	if (penMode == 0) {
        		colorIdx = paletteWindow.getSelectedIdx();
        	} else {
        		colorIdx = overlayTile[tud][tlr];
        	}
        	tile[tud][tlr] = colorIdx % paletteSize;
        	System.out.println("set color " + colorIdx);
          } else if (this.penMode == 2) {
        	colorIdx = paletteWindow.getSelectedIdx();
            FloodFill.floodFill(this.tile, colorIdx % paletteSize, tud, tlr);
            System.out.println("flood color " + colorIdx);
          }

          int newPalOffset = colorIdx - colorIdx % paletteSize;
          if (newPalOffset != palOffset.get()) {
        	  System.out.println("updating palette offset to " + newPalOffset);
        	  palOffset.set(newPalOffset);
        	  TileUtil.reIndex(tile, paletteSize);
          }

        } else {
          paletteWindow.setSelectedIdx(palOffset.get() + tile[tud][tlr]);
          paletteWindow.repaint();
          paletteWindow.toFront();
          System.out.println("got color " + paletteWindow.getSelectedIdx());
        }
      }

      repaint();
    }

  }


  public void setTile(
      int[][] tile,
      Container<Integer> palOffset,
      int paletteSize) {

    this.tile = tile;
    this.tileHeight = tile.length;
    this.tileWidth = tile[0].length;
    this.palOffset = palOffset;
    this.paletteSize = paletteSize;
    updateGraphics();

    System.out.println("zoomedtilewindow: set tile; palOffset: " + palOffset.get());
  }


  public int[][] getTile() {
    return tile;
  }

  /*
  public void setTileWindow(TilesEditorWindow tileWindow) {
    this.tileWindow = tileWindow;
  }
  */

  public int getZoomFactor() {
    return zoomFactor;
  }

  public void setZoomFactor(int newZoomFactor) {
    zoomFactor = newZoomFactor;
    updateGraphics();
  }

  public IndexedGraphics getDosGraphics() {
    return dosGraphics;
  }

  private void updateGraphics() {
    graphicsPanel.remove(dosGraphics);
    if (tile != null) {
      tileHeight = tile.length;
      tileWidth = tile[0].length;
    }
    dosGraphics = createDosGraphics();
    graphicsPanel.add(dosGraphics);
    graphicsPanel.validate();
    pack();
    repaint();
  }

  private IndexedGraphics createDosGraphics() {
    IndexedGraphics dg = new IndexedGraphics(
    	paletteWindow.getPalette(),
    	paletteWindow.getBitsPerChannel(),
    	tileHeight, tileWidth, zoomFactor);
    dg.setGridDimensions(1, 1);
    dg.setShowGrid(showGridlines);
    return dg;
  }

  /*
  private void updateTileProps() {
    int tpb = 0;
    if (this.tileWindow.getTileSet().properties().length > 0) {
      tpb = this.tileWindow.getTileSet().properties()[currentTile].value();
    }

    Color onColor = new Color(128, 0, 128);
    Color offColor = new Color(0, 0, 0);

    if ((tpb & 1) == 0) {
      tpBottom.setBackground(onColor);
    } else {
      tpBottom.setBackground(offColor);
    }
    if ((tpb & 2) == 0) {
      tpRight.setBackground(onColor);
    } else {
      tpRight.setBackground(offColor);
    }
    if ((tpb & 4) == 0) {
      tpLeft.setBackground(onColor);
    } else {
      tpLeft.setBackground(offColor);
    }
    if ((tpb & 8) == 0) {
      tpTop.setBackground(onColor);
    } else {
      tpTop.setBackground(offColor);
    }
    if ((tpb & 16) > 0) {
      tpOverlay.setBackground(onColor);
    } else {
      tpOverlay.setBackground(offColor);
    }
    if ((tpb & 32) > 0) {
      tpLeftStair.setBackground(onColor);
    } else {
      tpLeftStair.setBackground(offColor);
    }
    if ((tpb & 64) > 0) {
      tpRightStair.setBackground(onColor);
    } else {
      tpRightStair.setBackground(offColor);
    }

  }

   */

  /**
   * Draw the component.
   */
  public void paint(Graphics gr) {
    super.paint(gr);

    // draw the main zoomed tile
    dosGraphics.updateClut();
    TileUtil.drawTile(dosGraphics, tile, 0, 0, palOffset.get());
    dosGraphics.repaint();

    // Draw the repeated tile for tiling purposes
    tileTile.updateClut();
    if (this.tileHeight == 16) {
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          TileUtil.drawTile(
              tileTile, tile,
              j * this.tileWidth,
              i * this.tileHeight,
              palOffset.get());
        }
      }
    }
    tileTile.repaint();

    // draw the tileprops.
    // this.updateTileProps();
  }

  public ArrayList<Updater> getUpdaters() {
    return this.updaters;
  }

}
