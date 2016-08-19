// Copyright (c) 2015 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view;

import java.awt.Image;
import javax.swing.JFrame;


public class ImageWindow extends JFrame {

  private static final long serialVersionUID = 1L;

  public ImageWindow(Image image) {
    add(new ImagePanel(image));
    pack();
    setLocationRelativeTo(null);
    setResizable(false);
    setVisible(true);
    toFront();
  }

}
