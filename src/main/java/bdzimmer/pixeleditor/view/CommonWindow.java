// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// GUI boilerplate

package bdzimmer.pixeleditor.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;



public class CommonWindow extends JFrame {

  private static final long serialVersionUID = 0;

  protected JMenuBar menuBar;
  protected JToolBar toolBar;
  protected JPanel panel;
  protected StatusBar statusBar;


  // assemble the window components
  // usually you want to set the defaultCloseOperation to JFrame.DISPOSE_ON_CLOSE
  protected void build(int defaultCloseOperation) {

    this.menuBar   = menuBar();
    this.toolBar   = toolBar();
    this.panel     = panel();
    this.statusBar = statusBar();

    if (this.menuBar != null) {
      setJMenuBar(this.menuBar);
    }

    if (this.toolBar != null) {
      add(this.toolBar, BorderLayout.NORTH);
    }

    if (this.panel != null) {
      add(this.panel, BorderLayout.CENTER);
    }

    if (this.statusBar != null) {
      add(this.statusBar, BorderLayout.SOUTH);
    }

    setDefaultCloseOperation(defaultCloseOperation);

  }


  // finalize the layout and show
  void packAndShow(boolean resizable) {
    pack();
    setResizable(resizable);
    setVisible(true);
  }


  protected JMenuBar menuBar() {return null;};

  protected JToolBar toolBar() {return null;};

  protected JPanel panel() {return null;};

  protected StatusBar statusBar() {return null;};


}
