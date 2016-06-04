// Copyright (c) 2016 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.view;

import javax.swing.BorderFactory;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;


public class StatusBar extends JPanel {

  private static final long serialVersionUID = 1L;

  private final JLabel label1;
  private final JLabel label2;
  private final JLabel label3;


  public StatusBar(int width1, int width2, int width3) {
    label1 = new JLabel(" ");
    label1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    label2 = new JLabel(" ");
    label2.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    label3 = new JLabel(" ");
    label3.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    Dimension d = label1.getPreferredSize();
    label1.setPreferredSize(new Dimension(d.width * width1,  d.height));
    label2.setPreferredSize(new Dimension(d.width * width2,  d.height));
    label3.setPreferredSize(new Dimension(d.width * width3,  d.height));

    setLayout(new FlowLayout(FlowLayout.LEFT));
    add(label1);
    add(label2);
    add(label3);

  }


  public void update(String l1, String l2, String l3) {
    label1.setText(l1);
    label2.setText(l2);
    label3.setText(l3);
    repaint();
  }

  public void update(int xval, int yval, String text) {
    update("X: " + xval, "Y: " + yval, text);
  }

}
