// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// A more flexible version of PaletteWindow using the new data model


package bdzimmer.pixeleditor.view;

import bdzimmer.pixeleditor.controller.PaletteUtils;
import bdzimmer.pixeleditor.model.ColorTriple;
import bdzimmer.pixeleditor.view.WidgetUpdater;

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



public class PaletteEditorNew extends JFrame {

  private static final long serialVersionUID = 1L;
  public static final Canvas cColorLabel = new Canvas();

  private static final int swatchSize = 32;
  private static final int cols = 16;

  private final ColorTriple[] palette;
  private final BufferedImage image;
  private final ImagePanel imagePanel;
  private final int length;
  private final int rows;
  private final int colorFactor;
  private final int bitsPerChannel;
  private final WidgetUpdater widgetUpdater;

  private JSpinner rVal = new JSpinner();
  private JSpinner gVal = new JSpinner();
  private JSpinner bVal = new JSpinner();

  private int selectedIdx = 0;

  public PaletteEditorNew(
		  String title,
		  ColorTriple[] palette,
		  int bitsPerChannel,
		  WidgetUpdater widgetUpdater) {

    setTitle(title);
    this.palette = palette;
    length = this.palette.length;
    this.widgetUpdater = widgetUpdater;

    rows = (length + cols - 1) / cols;
    image = PaletteEditorNew.imageForPalette(length, cols, swatchSize);
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
        ColorTriple cc = PaletteEditorNew.this.palette[selectedIdx];
        ColorTriple nc = new ColorTriple(red, cc.g(), cc.b());
        PaletteEditorNew.this.palette[selectedIdx] = nc;
        repaint();
      }

    });
    gVal.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        SpinnerNumberModel currentModel = (SpinnerNumberModel)((JSpinner) event.getSource()).getModel();
        int green = (Integer)currentModel.getValue();
        ColorTriple cc = PaletteEditorNew.this.palette[selectedIdx];
        ColorTriple nc = new ColorTriple(cc.r(), green, cc.b());
        PaletteEditorNew.this.palette[selectedIdx] = nc;
        repaint();
      }

    });
    bVal.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent event) {
        SpinnerNumberModel currentModel = (SpinnerNumberModel)((JSpinner) event.getSource()).getModel();
        int blue = (Integer)currentModel.getValue();
        ColorTriple cc = PaletteEditorNew.this.palette[selectedIdx];
        ColorTriple nc = new ColorTriple(cc.r(), cc.g(), blue);
        PaletteEditorNew.this.palette[selectedIdx] = nc;
        repaint();
      }

    });

    imagePanel.setToolTipText("<html>right click: grab color<br />left click: set color<br />alt-left click: interpolate colors</html>");

    imagePanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {

        int clickedColor = (int) ((event.getY() / swatchSize) * cols) + (int) (event.getX() / swatchSize);
        // System.out.println("clicked color: " + clickedColor + " " + PaletteEditorNew.this.palette[clickedColor]);

        if (clickedColor < length && clickedColor >= 0) {

          if (event.isMetaDown()) {
            // right click - grab color
            selectedIdx = clickedColor;
          } else {
            // left click -  interpolate or copy set color
            if (event.isAltDown()) {
              System.out.println("linear interpolation");
              // TODO: update this
              // PaletteUtils.interpolateLinear(pal, colorIndex, clickedColor);
            } else {
              ColorTriple srcColor = PaletteEditorNew.this.palette[selectedIdx];
              PaletteEditorNew.this.palette[clickedColor] = new ColorTriple(srcColor.r(), srcColor.g(), srcColor.b());
            }

            // update after done updating the colors / current selection
            selectedIdx = clickedColor;

          }

          repaint();

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
        repaint();
      }
    });
    setFocusable(true);

    addKeyListener(new KeyListener() {

      public void keyPressed(KeyEvent event) {

        ColorTriple color = PaletteEditorNew.this.palette[selectedIdx];
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

        PaletteEditorNew.this.palette[selectedIdx] = new ColorTriple(r, g, b);
        repaint();
      }

      public void keyReleased(KeyEvent event) {}
      public void keyTyped(KeyEvent event) {}
    });


    pack();
    setResizable(false);

  }


  public void refreshPalette() {

    // redraw palette swatches


    PaletteEditorNew.drawPalette(image, palette, bitsPerChannel, rows, cols, swatchSize);

    Graphics gr = image.getGraphics();

    // draw the selection
    final java.awt.Color selectColor = new java.awt.Color(230, 0, 230);
    gr.setColor(selectColor);
    int x = (selectedIdx % cols) * swatchSize;
    int y = (int) (selectedIdx / cols) * swatchSize;
    gr.drawRect(x, y, swatchSize, swatchSize);

    imagePanel.repaint();

    // update the color sample and spinners
    ColorTriple color = palette[selectedIdx];
    cColorLabel.setBackground(new java.awt.Color(
        color.r() * colorFactor, color.g() * colorFactor, color.b() * colorFactor));

    updateSpinners();

  }


  public void updateSpinners() {
    final ColorTriple color = palette[selectedIdx];
    rVal.setValue(color.r());
    gVal.setValue(color.g());
    bVal.setValue(color.b());
  }


  public void paint(Graphics graphics) {
    super.paint(graphics);
    refreshPalette();
    widgetUpdater.update();
  }


  public static void drawPalette(BufferedImage image, ColorTriple[] palette, int bitsPerChannel, int rows, int cols, int swatchSize) {

    int colorFactor = (1 << (8 - bitsPerChannel));
    Graphics gr = image.getGraphics();

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        final int colorIdx = i * cols + j;
        if (colorIdx < palette.length) {
          ColorTriple color = palette[colorIdx];
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



