// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// GUI boilerplate

package bdzimmer.pixeleditor.view;

import java.awt.BorderLayout;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;


public abstract class CommonWindow extends JFrame {

  private static final long serialVersionUID = 0;

  protected JMenuBar  menuBar;
  protected JToolBar  toolBar;
  protected JPanel    panel;
  protected StatusBar statusBar;


  // assemble the window components
  // usually you want to set the defaultCloseOperation to JFrame.DISPOSE_ON_CLOSE
  protected void build(int defaultCloseOperation) {

    this.menuBar   = buildMenuBar();
    this.toolBar   = buildToolBar();
    this.panel     = buildPanel();
    this.statusBar = buildStatusBar();

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

    addWindowFocusListener(new WindowAdapter() {
      public void windowGainedFocus(WindowEvent event) {
	    onFocus();
	  }
    });
    setFocusable(true);
  }


  // finalize the layout and show
  void packAndShow(boolean resizable) {
    pack();
    setResizable(resizable);
    setVisible(true);
  }


  protected JMenuBar buildMenuBar() {return null;};

  protected JToolBar buildToolBar() {return null;};

  protected JPanel buildPanel() {return null;};

  protected StatusBar buildStatusBar() {return null;};

  protected void onFocus() {
    System.out.println("focus gained");
    repaint();
  }


}
