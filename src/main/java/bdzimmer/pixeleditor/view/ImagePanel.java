// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

class ImagePanel extends JPanel {
  private static final long serialVersionUID = 1L;

  private final Image image;

  ImagePanel(Image image) {
    this.image = image;
    setPreferredSize(
        new Dimension(image.getWidth(null), image.getHeight(null)));
    setVisible(true);
  }

  protected void paintComponent(Graphics gr) {
    gr.drawImage(image, 0, 0, null);
  }

}