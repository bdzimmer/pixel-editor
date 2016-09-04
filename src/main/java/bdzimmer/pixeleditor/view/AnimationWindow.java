// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Show animations using tiles in a tileset.

// Created 2016-05-30

package bdzimmer.pixeleditor.view;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import bdzimmer.pixeleditor.model.Direction;
import bdzimmer.pixeleditor.model.IndexedGraphics;
import bdzimmer.pixeleditor.model.TileContainer;
import bdzimmer.pixeleditor.view.DragDrop.TileImportTransferHandler;
import bdzimmer.pixeleditor.controller.TileUtil;


public class AnimationWindow extends CommonWindow {

  private static final long serialVersionUID = 0;

  private final int scale = 4;

  // TODO: actually calculate these properly
  private final int borderX = 16;
  private final int borderY = 16;
  private final int bgTilesWide = 5;
  private final int bgTilesHigh = 5;

  final TilesEditorWindow parent;

  // default animation settings - traditional walk

  private int totalFrames = 16;       // total counts of frame for a single character
  private int directionFrames = 3;    // total frames of animation per direction
  private int[] frameSequence = {0, 1, 0, 2};
  private int fps = 60;
  private int frameCount = 8;

  private boolean running = false;
  private int tileIndex = 0;

  private Thread animationThread = null;
  private IndexedGraphics dosGraphics;

  private TileContainer tc = new TileContainer();     // for background tile
  private int bgOffsetX = 0;
  private int bgOffsetY = 0;
  private int bgDirection = 0;


  public AnimationWindow(TilesEditorWindow parent, int tileIndex) {

    this.parent = parent;

    build(JFrame.DISPOSE_ON_CLOSE);

    // initialize background tile
    for (int i = 0; i < tc.getHeight(); i++) {
      tc.getTileBitmap()[i][tc.getWidth() / 2] = 255;
    }
    for (int i = 0; i < tc.getWidth(); i++) {
      tc.getTileBitmap()[tc.getHeight() / 2][i] = 255;
    }

    // stop the animation thread on close
    this.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e){
        running = false;
      }
    });

    setTitle("Animation");
    setTileIndex(tileIndex); // draw the initial tile

    packAndShow(false);

  }


  private IndexedGraphics createDosGraphics() {
    IndexedGraphics dg = new IndexedGraphics(
        parent.getTileSet().height() + borderY * 2,
        parent.getTileSet().width()  + borderX * 2,
        this.scale);

    dg.setPalette(parent.getPaletteWindow().getPalette());
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


  public void setTileIndex(int tileIndex) {
    this.tileIndex = tileIndex;
    if (!running) {
     draw(tileIndex);
    }
  }


  private void draw(int tileIndex) {

    dosGraphics.updateClut();

    for (int i = 0; i < bgTilesHigh; i++) {
      for (int j = 0; j < bgTilesWide; j++) {
    	TileUtil.drawTile(
			dosGraphics, 
			tc.getTileBitmap(),
			j * tc.getTileBitmap()[0].length - bgOffsetX,
			i * tc.getTileBitmap().length    - bgOffsetY);
      }
    }

    TileUtil.drawTileTrans(
    	dosGraphics,
    	parent.getTileSet().tiles()[tileIndex].bitmap(),
    	borderX,
    	borderY);
    
    dosGraphics.repaint();
  }


  private void scroll() {

    if (bgDirection == Direction.Up()) { bgOffsetY -= 1; }
    else if (bgDirection == Direction.Down()) { bgOffsetY += 1; }
    else if (bgDirection == Direction.Left()) { bgOffsetX -= 1; }
    else if (bgDirection == Direction.Right()) { bgOffsetX += 1; }

    if (bgOffsetX > (tc.getWidth() - 1)) { bgOffsetX = 0; }
    if (bgOffsetX < 0) { bgOffsetX = (tc.getWidth() - 1); }
    if (bgOffsetY > (tc.getHeight() - 1)) { bgOffsetY = 0; }
    if (bgOffsetY < 0) { bgOffsetY = (tc.getHeight() - 1); }

  }


  public int getTileIndex(int tileIndex) {
    return this.tileIndex;
  }


  /// CommonWindow overrides

  @Override
  protected JPanel buildPanel() {
    dosGraphics = createDosGraphics();

    // allow dropping a tile on
    dosGraphics.setTransferHandler(new TileImportTransferHandler(tc));
    dosGraphics.setToolTipText("drop a tile to change background");

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    panel.add(dosGraphics);
    return panel;
  }


  @Override
  protected JToolBar buildToolBar() {

    final JToolBar mainToolbar = new JToolBar();
    final JTextField totalFramesInput = new JTextField(Integer.toString(totalFrames), 2);
    final JTextField directionFramesInput = new JTextField(Integer.toString(directionFrames), 2);
    final JTextField frameSequenceInput = new JTextField(getFrameSequenceString(), 6);
    final JTextField fpsInput = new JTextField(Integer.toString(fps), 2);
    final JTextField frameCountInput = new JTextField(Integer.toString(frameCount), 2);
    final ButtonGroup direction = new ButtonGroup();
    final JToggleButton up    = new JToggleButton("^");
    final JToggleButton down  = new JToggleButton("v");
    final JToggleButton left  = new JToggleButton("<");
    final JToggleButton right = new JToggleButton(">");
    final JToggleButton none  = new JToggleButton("o");
    final JToggleButton runningToggle = new JToggleButton("Run");

    totalFramesInput.setToolTipText("frames in character");
    directionFramesInput.setToolTipText("frames in face direction");
    frameSequenceInput.setToolTipText("frame sequence");
    fpsInput.setToolTipText("draw frames per second");
    frameCountInput.setToolTipText("draw frames per animation frame");

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

    fpsInput.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {}
      public void focusLost(FocusEvent e) {
        AnimationWindow.this.fps = Integer.parseInt(fpsInput.getText());
      }
    });

    frameCountInput.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {}
      public void focusLost(FocusEvent e) {
        AnimationWindow.this.frameCount = Integer.parseInt(frameCountInput.getText());
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


    up.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (up.isSelected()) { bgDirection = Direction.Up(); }
      }
    });
    down.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (down.isSelected()) { bgDirection = Direction.Down(); }
      }
    });
    left.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (left.isSelected()) { bgDirection = Direction.Left(); }
      }
    });
    right.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (right.isSelected()) { bgDirection = Direction.Right(); }
      }
    });
    none.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (none.isSelected()) { bgDirection = Direction.NoDirection(); }
      }
    });


    mainToolbar.add(totalFramesInput);
    mainToolbar.add(directionFramesInput);
    mainToolbar.add(frameSequenceInput);
    mainToolbar.add(fpsInput);
    mainToolbar.add(frameCountInput);

    direction.add(up);
    direction.add(down);
    direction.add(left);
    direction.add(right);
    direction.add(none);
    mainToolbar.add(up);
    mainToolbar.add(down);
    mainToolbar.add(left);
    mainToolbar.add(right);
    mainToolbar.add(none);

    mainToolbar.add(runningToggle);

    mainToolbar.setFloatable(false);

    return mainToolbar;
  }


  class AnimationWorker implements Runnable {

    public void run() {

      // System.out.println("animation running!");

      int frameCounter = 0;

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
            scroll();
            draw(curFrameIndex);
          }

        }

        frameCounter++;

        if (frameCounter >= frameCount) {
          frameSeqIndex++;
          frameCounter = 0;
        }

        try {
          Thread.sleep(1000 / AnimationWindow.this.fps);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
