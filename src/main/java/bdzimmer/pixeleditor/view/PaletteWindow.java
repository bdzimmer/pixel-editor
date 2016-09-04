// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// A more flexible version of PaletteWindow using the new data model


package bdzimmer.pixeleditor.view;

import bdzimmer.pixeleditor.controller.PaletteUtils;
import bdzimmer.pixeleditor.model.Color;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



// TODO: use CommonWindow
// TODO: consider scala rewrite

public class PaletteWindow extends JFrame {

  private static final long serialVersionUID = 1L;
  public static final Canvas cColorLabel = new Canvas();

  private static final int swatchSize = 24;
  private static final int cols = 16;

  private final Color[] palette;
  private final BufferedImage image;
  private final ImagePanel imagePanel;
  private final int length;
  private final int rows;
  private final int colorFactor;
  private final int bitsPerChannel;
  private final Updater updater;

  private JSpinner rVal = new JSpinner();
  private JSpinner gVal = new JSpinner();
  private JSpinner bVal = new JSpinner();

  private int selectedIdx = 0;

  public PaletteWindow(
		  String title,
		  Color[] palette,
		  int bitsPerChannel,
		  Updater updater) {

    setTitle(title);
    this.palette = palette;
    length = this.palette.length;
    this.updater = updater;

    rows = (length + cols - 1) / cols;
    image = PaletteWindow.imageForPalette(length, cols, swatchSize);
    imagePanel = new ImagePanel(image);

    add(imagePanel);

    // Currently selected color
    cColorLabel.setSize(64, 64);
    cColorLabel.setBackground(new java.awt.Color(0, 0, 0));

    this.bitsPerChannel = bitsPerChannel;
    final int colorMax = (1 << bitsPerChannel) - 1;
    colorFactor = (1 << (8 - bitsPerChannel));

    rVal.setModel(new SpinnerNumberModel(0, 0, colorMax, 1));
    gVal.setModel(new SpinnerNumberModel(0, 0, colorMax, 1));
    bVal.setModel(new SpinnerNumberModel(0, 0, colorMax, 1));

    rVal.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        SpinnerNumberModel currentModel = (SpinnerNumberModel)((JSpinner) event.getSource()).getModel();
        int red = (Integer)currentModel.getValue();
        Color cc = PaletteWindow.this.palette[selectedIdx];
        Color nc = new Color(red, cc.g(), cc.b());
        PaletteWindow.this.palette[selectedIdx] = nc;
        update();
      }

    });
    gVal.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        SpinnerNumberModel currentModel = (SpinnerNumberModel)((JSpinner) event.getSource()).getModel();
        int green = (Integer)currentModel.getValue();
        Color cc = PaletteWindow.this.palette[selectedIdx];
        Color nc = new Color(cc.r(), green, cc.b());
        PaletteWindow.this.palette[selectedIdx] = nc;
        update();
      }

    });
    bVal.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        SpinnerNumberModel currentModel = (SpinnerNumberModel)((JSpinner) event.getSource()).getModel();
        int blue = (Integer)currentModel.getValue();
        Color cc = PaletteWindow.this.palette[selectedIdx];
        Color nc = new Color(cc.r(), cc.g(), blue);
        PaletteWindow.this.palette[selectedIdx] = nc;
        update();
      }

    });

    imagePanel.setToolTipText("<html>right click: grab color<br />left click: set color<br />alt-left click: interpolate colors</html>");

    imagePanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {

        int clickedIdx = (int) ((event.getY() / swatchSize) * cols) + (int) (event.getX() / swatchSize);
        // System.out.println("clicked color: " + clickedColor + " " + PaletteEditorNew.this.palette[clickedColor]);

        if (clickedIdx < length && clickedIdx >= 0) {

          if (event.isMetaDown()) {
            // right click - grab color
            selectedIdx = clickedIdx;
          } else {
            // left click -  interpolate or copy set color
            if (event.isAltDown()) {
              System.out.println("linear interpolation");
              PaletteUtils.interpolateLinear(
                  PaletteWindow.this.palette, selectedIdx, clickedIdx);
            } else {
              Color srcColor = PaletteWindow.this.palette[selectedIdx];
              PaletteWindow.this.palette[clickedIdx] = new Color(srcColor.r(), srcColor.g(), srcColor.b());
            }

            // update after done updating the colors / current selection
            selectedIdx = clickedIdx;

          }

          update();

        }
      }
    });

    JPanel sp = new JPanel();
    sp.setMaximumSize(imagePanel.getSize());
    sp.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    sp.add(new JLabel("R"));
    sp.add(rVal);
    sp.add(new JLabel("G"));
    sp.add(gVal);
    sp.add(new JLabel("B"));
    sp.add(bVal);
    sp.add(cColorLabel);

    add(sp, BorderLayout.SOUTH);

    addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent event) {
        System.out.println("palette editor Focus gained!");
        update();
      }
    });
    setFocusable(true);

    addKeyListener(new KeyListener() {

      public void keyPressed(KeyEvent event) {

        Color color = PaletteWindow.this.palette[selectedIdx];
        int r = color.r();
        int g = color.g();
        int b = color.b();

        if (event.getKeyCode() == KeyEvent.VK_A) {
          r++; if (r > colorMax) r = 0;
        } else if (event.getKeyCode() == KeyEvent.VK_Z) {
          r--; if (r < 0) r = colorMax;
        } else if (event.getKeyCode() == KeyEvent.VK_S) {
          g++; if (g > colorMax) g = 0;
        } else if (event.getKeyCode() == KeyEvent.VK_X) {
          g--; if (g < 0) g = colorMax;
        } else if (event.getKeyCode() == KeyEvent.VK_D) {
          b++; if (b > colorMax) b = 0;
        } else if (event.getKeyCode() == KeyEvent.VK_C) {
          b--; if (b < 0) b = colorMax;
        }

        PaletteWindow.this.palette[selectedIdx] = new Color(r, g, b);
        update();
      }

      public void keyReleased(KeyEvent event) {}
      public void keyTyped(KeyEvent event) {}
    });


    pack();
    setResizable(false);

  }


  public void refreshPalette() {

    // redraw palette swatches

    PaletteWindow.drawPalette(image, palette, bitsPerChannel, rows, cols, swatchSize);

    Graphics gr = image.getGraphics();

    // draw the selection
    final java.awt.Color selectColor = new java.awt.Color(230, 0, 230);
    gr.setColor(selectColor);
    int x = (selectedIdx % cols) * swatchSize;
    int y = (int) (selectedIdx / cols) * swatchSize;
    gr.drawRect(x, y, swatchSize, swatchSize);

    imagePanel.repaint();

    // update the color sample and spinners
    Color color = palette[selectedIdx];
    cColorLabel.setBackground(new java.awt.Color(
        color.r() * colorFactor, color.g() * colorFactor, color.b() * colorFactor));

    updateSpinners();

  }


  public void updateSpinners() {
    final Color color = palette[selectedIdx];
    rVal.setValue(color.r());
    gVal.setValue(color.g());
    bVal.setValue(color.b());
  }


  public void update() {
    if (updater != null) {
      updater.update();
    }
    refreshPalette();
  }


  public int getSelectedIdx() {
    return selectedIdx;
  }

  public void setSelectedIdx(int selectedIdx) {
    this.selectedIdx = selectedIdx;
  }

  public Color[] getPalette() {
    return palette;
  }
  
  public int getBitsPerChannel() {
    return bitsPerChannel;
  }

  ///

  public static void drawPalette(BufferedImage image, Color[] palette, int bitsPerChannel, int rows, int cols, int swatchSize) {

    int colorFactor = (1 << (8 - bitsPerChannel));
    Graphics gr = image.getGraphics();

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        final int colorIdx = i * cols + j;
        if (colorIdx < palette.length) {
          Color color = palette[colorIdx];
          gr.setColor(new java.awt.Color(
              color.r() * colorFactor, color.g() * colorFactor, color.b() * colorFactor));
          gr.fillRect(j * swatchSize, i * swatchSize, swatchSize, swatchSize);
        }
      }
    }

  }


  public static BufferedImage imageForPalette(int length, int cols, int swatchSize) {
    int rows = length / cols;
    return new BufferedImage(cols * swatchSize, rows * swatchSize, BufferedImage.TYPE_INT_RGB);
  }

}



