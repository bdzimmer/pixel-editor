// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Show animations using tiles in a tileset.

// Created 2016-05-30

package bdzimmer.pixeleditor.view;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import bdzimmer.pixeleditor.model.DosGraphics;

public class AnimationWindow extends JFrame {

  private static final long serialVersionUID = 1L;

  private int totalFrames = 16;       // total counts of frame for a single character
  private int directionFrames = 3;    // total frames of animation per direction
  private int[] frameSequence = {0, 1, 0, 2};
  private int ms = 266;
  private boolean running = false;

  private int tileIndex = 0;

  private final int scale = 4;
  private final int border = 16;

  private Thread animationThread = null;

  final TilesEditorWindow parent;
  private DosGraphics dosGraphics;


  public AnimationWindow(TilesEditorWindow parent) {
    this.parent = parent;

    add(mainToolbar(), BorderLayout.NORTH);
    dosGraphics = createDosGraphics();
    add(dosGraphics, BorderLayout.CENTER);
    pack();

    // stop the animation thread on close
    this.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e){
        running = false;
      }
    });

    setTitle("Animation");
    setResizable(false);
    setVisible(true);

  }


  private DosGraphics createDosGraphics() {
    DosGraphics dg = new DosGraphics(
        parent.getTileSet().height() + border * 2,
        parent.getTileSet().width()  + border * 2,
        this.scale);

    dg.setRgbPalette(parent.getPaletteWindow().getDosGraphics().getRgbPalette());
    return dg;
  }


  private String getFrameSequenceString() {
    String fs = "";
    if (frameSequence.length > 0) {
      fs = Integer.toString(frameSequence[0]);
    }
    if (frameSequence.length > 1) {
      for (int i = 1; i < frameSequence.length; i++) {
        fs = fs + ", " + Integer.toString(frameSequence[i]);
      }
    }
    return fs;
  }


  private JToolBar mainToolbar() {

    final JToolBar mainToolbar = new JToolBar();
    final JTextField totalFramesInput = new JTextField(Integer.toString(totalFrames), 4);
    final JTextField directionFramesInput = new JTextField(Integer.toString(directionFrames), 4);
    final JTextField frameSequenceInput = new JTextField(getFrameSequenceString(), 12);
    final JTextField msInput = new JTextField(Integer.toString(ms), 5);
    final ButtonGroup direction = new ButtonGroup();
    final JToggleButton up    = new JToggleButton("^");
    final JToggleButton down  = new JToggleButton("v");
    final JToggleButton left  = new JToggleButton("<");
    final JToggleButton right = new JToggleButton(">");
    final JToggleButton runningToggle = new JToggleButton("Run");

    totalFramesInput.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {}
      public void focusLost(FocusEvent e) {
        AnimationWindow.this.totalFrames = Integer.parseInt(totalFramesInput.getText());
      }
    });

    directionFramesInput.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {}
      public void focusLost(FocusEvent e) {
        AnimationWindow.this.totalFrames = Integer.parseInt(directionFramesInput.getText());
      }
    });

    frameSequenceInput.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {}
      public void focusLost(FocusEvent e) {

        String[] frameStrings = frameSequenceInput.getText().split(",\\s+");
        AnimationWindow.this.frameSequence = new int[frameStrings.length];
        for (int i = 0; i < frameStrings.length; i++) {
          AnimationWindow.this.frameSequence[i] = (Integer.parseInt(frameStrings[i]));
        }

      }
    });

    msInput.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {}
      public void focusLost(FocusEvent e) {
        AnimationWindow.this.ms = Integer.parseInt(msInput.getText());
      }
    });

    runningToggle.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        System.out.println("animation playing: "+ runningToggle.isSelected());

        AnimationWindow.this.running = runningToggle.isSelected();

        if (AnimationWindow.this.running) {
          if (animationThread == null || !animationThread.isAlive()) {
            animationThread = new Thread(new AnimationWorker());
            animationThread.start();
          }
        }
      }
    });

    mainToolbar.add(totalFramesInput);
    mainToolbar.add(directionFramesInput);
    mainToolbar.add(frameSequenceInput);
    mainToolbar.add(msInput);

    direction.add(up);
    direction.add(down);
    direction.add(left);
    direction.add(right);
    mainToolbar.add(up);
    mainToolbar.add(down);
    mainToolbar.add(left);
    mainToolbar.add(right);

    mainToolbar.add(runningToggle);

    return mainToolbar;
  }


  public void setTileIndex(int tileIndex) {
    this.tileIndex = tileIndex;
    if (!running) {
      dosGraphics.updateClut();
      dosGraphics.drawTile(parent.getTileSet().tiles()[tileIndex].pixels(), border, border);
      dosGraphics.repaint();
    }
  }


  public int getTileIndex(int tileIndex) {
    return this.tileIndex;
  }


  class AnimationWorker implements Runnable {

    public void run() {

      // System.out.println("animation running!");
      // index in the frame sequence
      int frameSeqIndex = 0;

      while(AnimationWindow.this.running) {

        if (frameSeqIndex > (frameSequence.length - 1)) {
          frameSeqIndex = 0;
        }

        final int offsetCharacter = tileIndex % totalFrames;
        final int offsetDirection = offsetCharacter % directionFrames;

        final int startCharacter = tileIndex - offsetCharacter;
        final int startDirection = offsetCharacter - offsetDirection;

        if (frameSeqIndex <= (frameSequence.length - 1)) {

          final int offsetFrame = frameSequence[frameSeqIndex];
          final int curFrameIndex = startCharacter + startDirection + offsetFrame;
          // System.out.println(totalFrames + " " + directionFrames);
          // System.out.println(tileIndex + " -> " + offsetCharacter + " " + offsetDirection);
          // System.out.println(startCharacter + " " + startDirection + " " + offsetFrame + " -> " + curFrameIndex);

          if (curFrameIndex < parent.getTileSet().tiles().length) {
            dosGraphics.updateClut();
            dosGraphics.drawTile(parent.getTileSet().tiles()[curFrameIndex].pixels(), border, border);
            dosGraphics.repaint();
          }

        }

        frameSeqIndex++;

        try {
          Thread.sleep(AnimationWindow.this.ms);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
