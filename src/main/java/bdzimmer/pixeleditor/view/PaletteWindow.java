// Copyright (c) 2015 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view;


import bdzimmer.pixeleditor.controller.PaletteUtils;
import bdzimmer.pixeleditor.model.DosGraphics;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


class PaletteWindow extends JFrame {

  private static final long serialVersionUID = 1L;
  public static final Canvas cColorLabel = new Canvas();

  private final DosGraphics dosGraphics = new DosGraphics(128, 128, 2);

  private JSpinner redNumber = new JSpinner();
  private JSpinner greenNumber = new JSpinner();
  private JSpinner blueNumber = new JSpinner();


  private int colorIndex = 0;


  public PaletteWindow(int[][] rgbPalette) {

    setLayout(new BorderLayout(5, 5));
    setTitle("Palette");

    this.dosGraphics.setRgbPalette(rgbPalette);
    add(this.dosGraphics);
    this.setSize(270, 400);

    // Currently selected color
    cColorLabel.setSize(64, 64);
    cColorLabel.setBackground(new Color(0, 0, 0));

    redNumber.setModel(new SpinnerNumberModel(0, 0, 63, 1));
    greenNumber.setModel(new SpinnerNumberModel(0, 0, 63, 1));
    blueNumber.setModel(new SpinnerNumberModel(0, 0, 63, 1));

    redNumber.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        SpinnerNumberModel currentModel = (SpinnerNumberModel) ((JSpinner) event
            .getSource()).getModel();
        dosGraphics.getRgbPalette()[colorIndex][0] = (Integer) currentModel
            .getValue();
        refreshPalette();
      }

    });
    greenNumber.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        SpinnerNumberModel currentModel = (SpinnerNumberModel) ((JSpinner) event
            .getSource()).getModel();
        dosGraphics.getRgbPalette()[colorIndex][1] = (Integer) currentModel
            .getValue();
        refreshPalette();
      }

    });
    blueNumber.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        SpinnerNumberModel currentModel = (SpinnerNumberModel) ((JSpinner) event
            .getSource()).getModel();
        dosGraphics.getRgbPalette()[colorIndex][2] = (Integer) currentModel
            .getValue();
        refreshPalette();
      }

    });

    this.dosGraphics.setToolTipText("<html>right click: grab color<br />left click: set color<br />alt-left click: interpolate colors</html>");

    this.dosGraphics.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {

        // System.out.println(event.getX() + " " + event.getY());

        int clickedColor = (int) ((event.getY() / 16) * 16) + (int) (event.getX() / 16);
        if (clickedColor < 256 && clickedColor >= 0) {

          if (event.isMetaDown()) {
            // right click - grab color
            colorIndex = clickedColor;

          } else {
            // left click -  set color

            int[][] pal = dosGraphics.getRgbPalette();

            if (event.isAltDown()) {
              PaletteUtils.interpolateLinear(pal, colorIndex, clickedColor);
            } else {
              pal[clickedColor][0] = pal[colorIndex][0];
              pal[clickedColor][1] = pal[colorIndex][1];
              pal[clickedColor][2] = pal[colorIndex][2];
            }

            // update after done updating the colors / current selection
            colorIndex = clickedColor;

          }

          repaint();

        }
      }
    });

    JPanel sp = new JPanel();
    sp.setMaximumSize(this.dosGraphics.getSize());
    sp.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    sp.add(new JLabel("R"));
    sp.add(redNumber);
    sp.add(new JLabel("G"));
    sp.add(greenNumber);
    sp.add(new JLabel("B"));
    sp.add(blueNumber);
    sp.add(cColorLabel);

    this.add(sp, BorderLayout.SOUTH);

    addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent event) {
        repaint();
        // System.out.println("Focus gained!");
      }
    });

    // Adjust current colors with a, z, s, x, d, c keys
    this.setFocusable(true);
    this.addKeyListener(new KeyListener() {

      public void keyPressed(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.VK_A) {
          dosGraphics.getRgbPalette()[colorIndex][0]++;
          if (dosGraphics.getRgbPalette()[colorIndex][0] > 63) {
            dosGraphics.getRgbPalette()[colorIndex][0] = 0;
          }

        } else if (event.getKeyCode() == KeyEvent.VK_Z) {
          dosGraphics.getRgbPalette()[colorIndex][0]--;
          if (dosGraphics.getRgbPalette()[colorIndex][0] < 0) {
            dosGraphics.getRgbPalette()[colorIndex][0] = 63;
          }

        } else if (event.getKeyCode() == KeyEvent.VK_S) {
          dosGraphics.getRgbPalette()[colorIndex][1]++;
          if (dosGraphics.getRgbPalette()[colorIndex][1] > 63) {
            dosGraphics.getRgbPalette()[colorIndex][1] = 0;
          }

        } else if (event.getKeyCode() == KeyEvent.VK_X) {
          dosGraphics.getRgbPalette()[colorIndex][1]--;
          if (dosGraphics.getRgbPalette()[colorIndex][1] < 0) {
            dosGraphics.getRgbPalette()[colorIndex][1] = 63;
          }

        } else if (event.getKeyCode() == KeyEvent.VK_D) {
          dosGraphics.getRgbPalette()[colorIndex][2]++;
          if (dosGraphics.getRgbPalette()[colorIndex][2] > 63) {
            dosGraphics.getRgbPalette()[colorIndex][2] = 0;
          }

        } else if (event.getKeyCode() == KeyEvent.VK_C) {
          dosGraphics.getRgbPalette()[colorIndex][2]--;
          if (dosGraphics.getRgbPalette()[colorIndex][2] < 0) {
            dosGraphics.getRgbPalette()[colorIndex][2] = 63;
          }

        }

        redNumber.setValue(dosGraphics.getRgbPalette()[colorIndex][0]);
        greenNumber.setValue(dosGraphics.getRgbPalette()[colorIndex][1]);
        blueNumber.setValue(dosGraphics.getRgbPalette()[colorIndex][2]);
        dosGraphics.updateClut();
        refreshPalette();

      }

      public void keyReleased(KeyEvent event) {

      }

      public void keyTyped(KeyEvent event) {

      }

    });

    this.dosGraphics.getRgbPalette()[255][0] = 50; // Funky Pink
    this.dosGraphics.getRgbPalette()[255][2] = 50;
    this.dosGraphics.getRgbPalette()[10][1] = 63; // Bright Green
    this.dosGraphics.updateClut();
    this.repaint();

    pack();
    this.setResizable(false);
  }

  public void refreshPalette() {

    dosGraphics.updateClut();

    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        for (int k = 0; k < 8; k++) {
          for (int l = 0; l < 8; l++) {
            dosGraphics.setPixel(i * 8 + k, j * 8 + l, i * 16 + j);
          }
        }
      }
    }

    dosGraphics.repaint();
    cColorLabel.setBackground(new Color(dosGraphics.getPalette()[colorIndex]));
    updateSpinners();
  }

  public void updateSpinners() {

    // update spinners
    redNumber.setValue(this.dosGraphics.getRgbPalette()[colorIndex][0]);
    greenNumber.setValue(this.dosGraphics.getRgbPalette()[colorIndex][1]);
    blueNumber.setValue(this.dosGraphics.getRgbPalette()[colorIndex][2]);

  }

  public DosGraphics getDosGraphics() {
    return this.dosGraphics;
  }

  public void paint(Graphics graphics) {
    super.paint(graphics);
    this.refreshPalette();

    Graphics gr = this.dosGraphics.getBuffer().getGraphics();
    gr.setColor(new Color(this.dosGraphics.getPalette()[255]));
    gr.drawRect((colorIndex % 16) * 16,
        (int) (colorIndex / 16) * 16, 16, 16);

  }

  public int getColorIndex() {
    return colorIndex;
  }

  public void setColorIndex(int colorIndex) {
    this.colorIndex = colorIndex;
  }


}
