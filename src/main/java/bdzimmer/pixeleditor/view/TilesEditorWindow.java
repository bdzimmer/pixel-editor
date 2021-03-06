// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Modified around 2015-12-17 for new Tileset class. A lot of the code
// in here should be moved to controller classes.

package bdzimmer.pixeleditor.view;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import bdzimmer.pixeleditor.model.IndexedGraphics;
import bdzimmer.pixeleditor.model.Palette;
import bdzimmer.pixeleditor.model.TileContainer;
import bdzimmer.pixeleditor.model.TileOptions;
import bdzimmer.pixeleditor.model.Tileset;
import bdzimmer.pixeleditor.model.TileAttributes;
import bdzimmer.pixeleditor.model.Color;
import bdzimmer.pixeleditor.view.DragDrop.TileExportTransferHandler;
import bdzimmer.pixeleditor.controller.OldTilesetLoader;
import bdzimmer.pixeleditor.controller.TileUtil;

public class TilesEditorWindow extends CommonWindow {

  private static final long serialVersionUID = 0;

  private final String tilesDir;
  private final String title;

  private final int scale = 3;

  private Tileset tileset;
  private TileAttributes attrs;
  private String tileFilename;

  private IndexedGraphics dosGraphics;
  private ZoomedTileWindow zoomWindow;
  private AnimationWindow animationWindow;

  private final PaletteWindow paletteWindow;
  private final TileContainer tileContainer;

  // private int currentTile;


  /**
   * Create a TilesEditorWindow.
   *
   * @param tilesDir            tiles directory
   * @param tiles               tile set to edit in the window
   * @param attribute           TileAttributes for the tileset
   * @param title               general title for window
   * @param fileName            file name of tiles (for save menu option)
   * @param paletteWindow       palette window to edit
   * @param tileContainer       tile container for current tile
   */
  public TilesEditorWindow(
      String tilesDir,
      Tileset tiles,
      TileAttributes attributes,
      String title,
      String filename,
      PaletteWindow paletteWindow,
      TileContainer tileContainer) {

    this.tilesDir = tilesDir;
    this.tileset = tiles;
    this.attrs = attributes;

    this.tileFilename = filename;
    this.title = title;
    updateTitle();

    this.paletteWindow = paletteWindow;
    this.tileContainer = tileContainer;

    build(JFrame.DISPOSE_ON_CLOSE);

    // redraw on focus gained
    setFocusable(true);
    addWindowFocusListener(new WindowAdapter() {
      public void windowGainedFocus(WindowEvent event) {
        repaint();
      }
    });

    packAndShow(false);

  }


  // create an appropriately sized and scaled DosGraphics for the tileset
  private IndexedGraphics createDosGraphics() {
    IndexedGraphics dg = new IndexedGraphics(
        (int)Math.ceil((float)tileset.tiles().length / tileset.tilesPerRow()) * tileset.height(),
        tileset.tilesPerRow() * tileset.width(),
        this.scale);

    dg.setGridDimensions(tileset.width(), tileset.height());
    dg.setPalette(paletteWindow.getPalette());

    return dg;
  }


  private void handleClicks(MouseEvent event, boolean allowCopy) {

    int selectedTile =
        (int)(event.getY() / (tileset.height() * scale)) * tileset.tilesPerRow()
        + (int)(event.getX() / (tileset.width() * scale));

    if (selectedTile > tileset.tiles().length) {
      selectedTile = tileset.tiles().length;
    }

    if (event.isMetaDown()) {

      // right click grab tile

      selectTile(selectedTile);

    } else  if (allowCopy) {

      int newTile = selectedTile;

      // TODO: use drag / drop functionality for this???

      // Calculate maximum size we can copy
      // The global tile bitmap here seems kind of dumb, but it's there to allow
      // copying tiles across tileset -- important functionality.

      int udlength = Math.min(tileset.height(), tileContainer.getTileBitmap().length);
      int lrlength = Math.min(tileset.width(), tileContainer.getTileBitmap()[0].length);

      for (int i = 0; i < udlength; i++) {
        for (int j = 0; j < lrlength; j++) {
          tileset.tiles()[newTile].bitmap()[i][j] = tileContainer.getTileBitmap()[i][j];
        }
      }

      // set the copy as the current tile
      tileContainer.setTileIndex(selectedTile);
      tileContainer.setTileBitmap(tileset.tiles()[selectedTile].bitmap());
      repaint();

    }

    statusBar.update(0, 0, "" + selectedTile);

  }


  // select a tile from the set into the tile container
  // and show it in the ZoomWindow
  public void selectTile(int selectedIdx) {

    // set the current tile
    tileContainer.setTileIndex(selectedIdx);
    tileContainer.setTileBitmap(tileset.tiles()[selectedIdx].bitmap());

    // show in zoom window
    // TODO: why this type safety warning?
    Container<Integer> dummyContainer = new SimpleContainer<Integer>(0);

    if (zoomWindow == null || !zoomWindow.isVisible()) {
      zoomWindow = new ZoomedTileWindow(
          "Zoom",
          tileset.tiles()[selectedIdx].bitmap(),
          dummyContainer,
          256,
          paletteWindow);
      zoomWindow.setUpdater(new DumbUpdater(this));
      zoomWindow.setLocationRelativeTo(this);
    } else {
      zoomWindow.setTile(
          tileset.tiles()[selectedIdx].bitmap(),
          dummyContainer,
          256);
    }
    zoomWindow.toFront();

    // show in animation window
    if (animationWindow != null && animationWindow.isVisible()) {
      animationWindow.setTileIndex(selectedIdx);
      animationWindow.toFront();
    }

  }


  // convert all color indices that are RGB(0, 0, 0) to 0
  private void blacken() {

    boolean[] blackColors = new boolean[256];

    // determine which colors are equiv to black
    for (int i = 0; i < 256; i++) {

      Color color = dosGraphics.getPalette()[i];
      if (color.r() == 0 && color.g() == 0 && color.b() == 0) {
        blackColors[i] = true;
      }
    }

    for (int i = 0; i < tileset.tiles().length; i++) {
      for (int j = 0; j < tileset.tiles()[0].bitmap().length; j++) {
        for (int k = 0; k < tileset.tiles()[0].bitmap()[0].length; k++) {
          if (blackColors[tileset.tiles()[i].bitmap()[j][k]]) {
            // temporarily...
            tileset.tiles()[i].bitmap()[j][k] = 0;
          }
        }
      }
    }

  }


  // swap colors 0 and 255
  private void swapTransparency() {
    for (int i = 0; i < tileset.tiles().length; i++) {
      for (int j = 0; j < tileset.tiles()[0].bitmap().length; j++) {
        for (int k = 0; k < tileset.tiles()[0].bitmap()[0].length; k++) {
          int tempColor = tileset.tiles()[i].bitmap()[j][k];
          if (tempColor == 0) {
            tileset.tiles()[i].bitmap()[j][k] = 255;
          }
          if (tempColor == 255) {
            tileset.tiles()[i].bitmap()[j][k] = 0;
          }
        }
      }
    }
  }

  /**
   * Show a tile attributes chooser, change the tile set type,
   * resize, and redraw the window.
   */
  private void changeTiles() {

    // mutate the TileAttributes and create a new tileset.
    attrs = TileOptions.getOptions();
    tileset = OldTilesetLoader.fromAttributes(attrs);

    panel.remove(dosGraphics);
    dosGraphics = createDosGraphics();
    panel.add(dosGraphics);

    pack();
    repaint();

  }


  // update the title of the window
  private void updateTitle() {
    setTitle(title + " - " + tileFilename);
  }


  /**
   * Select a tiles file to load with a file chooser, then load it
   * and redraw.
   *
   */
  private void chooseLoadTileset() {
    JFileChooser jfc = new JFileChooser();
    jfc.setDialogType(JFileChooser.OPEN_DIALOG);
    jfc.setCurrentDirectory(new File(tilesDir));
    if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      File tilesFile = jfc.getSelectedFile();
      if (tilesFile != null) {
        loadTileset(tilesFile.getPath());
      }
    }
  }


  /**
   * Select a tiles file to save with a file chooser, then save it.
   */
  private void chooseSaveTileset() {
    JFileChooser jfc = new JFileChooser();
    jfc.setDialogType(JFileChooser.SAVE_DIALOG);
    jfc.setCurrentDirectory(new File(tilesDir));
    if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
      File tilesFile = jfc.getSelectedFile();
      if (tilesFile != null) {
        saveTileset(tilesFile.getPath());
      }
    }
  }


  // load the tileset and update the palette
  private void loadTileset(String filename) {
    tileset = new OldTilesetLoader(filename, attrs).load();
    Tileset.modPalette(tileset.palettes().apply(0), dosGraphics.getPalette());
    tileFilename = filename;
    paletteWindow.repaint();
    updateTitle();
    repaint();
  }


  // grab the palette, update the tileset, and save it
  private void saveTileset(String filename) {

    // mutate the tileset's default palette before saving!!!
    Palette newPal = Tileset.extractPalette(tileset.palettes().apply(0), dosGraphics.getPalette());
    Palette pal = tileset.palettes().apply(0);
    for (int i = 0; i < pal.colors().length; i++) {
      pal.colors()[i] = newPal.colors()[i];
    }
    new OldTilesetLoader(filename, attrs).save(tileset);

    tileFilename = filename;
    updateTitle();
    repaint();

  }


  /**
   * Draw the component.
   */
  @Override
  public void paint(Graphics graphics) {
    super.paint(graphics);

    dosGraphics.updateClut();
    TileUtil.drawTileset(dosGraphics, tileset);
    dosGraphics.repaint();

  }


  @Override
  protected JMenuBar buildMenuBar() {

    final JMenuBar mainMenu = new JMenuBar();

    final JMenu fileMenu = new JMenu("File");

    final JMenuItem jmOpen = new JMenuItem("Open");
    final JMenuItem jmSave = new JMenuItem("Save");
    final JMenuItem jmSaveAs = new JMenuItem("Save As..");
    final JMenuItem jmReload = new JMenuItem("Reload");

    final JMenu toolsMenu = new JMenu("Tools");

    final JMenuItem jmSwap = new JMenuItem("Swap transparency");
    final JMenuItem jmBlacken = new JMenuItem("Blacken");

    jmOpen.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        chooseLoadTileset();
      }
    });

    jmSave.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (!tileFilename.equals("")) {
          saveTileset(tileFilename);
        } else {
          chooseSaveTileset();
        }
      }
    });

    jmSaveAs.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        chooseSaveTileset();
      }
    });

    jmReload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (!tileFilename.equals("")) {
          loadTileset(tileFilename);
        } else {
          chooseLoadTileset();
        }
      }
    });

    jmSwap.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        swapTransparency();
        repaint();
      }
    });

    jmBlacken.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        blacken();
        repaint();
      }
    });

    fileMenu.add(jmOpen);
    fileMenu.add(jmSave);
    fileMenu.add(jmSaveAs);
    fileMenu.add(jmReload);
    mainMenu.add(fileMenu);

    toolsMenu.add(jmSwap);
    toolsMenu.add(jmBlacken);
    mainMenu.add(toolsMenu);

    return mainMenu;
  }


  @Override
  protected JToolBar buildToolBar() {

    final JToolBar mainToolbar = new JToolBar();
    final JToggleButton gridShow = new JToggleButton("Grid");
    final JButton changeSettings = new JButton("Settings");
    final JButton animation = new JButton("Animation");

    gridShow.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	System.out.println("grid show: "+ gridShow.isSelected());
        dosGraphics.setShowGrid(gridShow.isSelected());
        dosGraphics.repaint();
      }
    });
    gridShow.setFocusable(false);

    changeSettings.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { changeTiles(); }
    });
    changeSettings.setFocusable(false);

    animation.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (animationWindow != null) {
          animationWindow.dispose();
        }
        animationWindow = new AnimationWindow(
            TilesEditorWindow.this.tileset.tiles(),
            TilesEditorWindow.this.paletteWindow.getPalette(),
            TilesEditorWindow.this.tileContainer.getTileIndex());
        animationWindow.setLocationRelativeTo(TilesEditorWindow.this);
      }
    });
    animation.setFocusable(false);

    mainToolbar.add(gridShow);
    mainToolbar.add(changeSettings);
    mainToolbar.add(animation);
    mainToolbar.setFloatable(false);

    return mainToolbar;
  }


  @Override
  protected JPanel buildPanel() {

    // tileset visualization
    dosGraphics = createDosGraphics();

    JPanel graphicsPanel = new JPanel();

    graphicsPanel.setToolTipText("<html>right click: grab tile<br />left click: set tile<br /></html>");

    // clicking to select and manipulate tiles
    graphicsPanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) { handleClicks(event, true); }
    });

    // drag tiles onto other components
    graphicsPanel.setTransferHandler(new TileExportTransferHandler(tileContainer));
    graphicsPanel.addMouseMotionListener(new MouseAdapter() {
      public void mouseDragged(MouseEvent e) {
        handleClicks(e, false);
        JPanel jp = (JPanel) e.getSource();
        jp.getTransferHandler().exportAsDrag(jp, e, TransferHandler.COPY);
      }
    });

    graphicsPanel.add(dosGraphics);

    return graphicsPanel;

  }


  @Override
  protected StatusBar buildStatusBar() {
    return new StatusBar(6, 6, 20);
  }


  public Tileset getTileSet() {
    return this.tileset;
  }

  public IndexedGraphics getDosGraphics() {
    return this.dosGraphics;
  }

  public TileContainer getTileContainer() {
    return this.tileContainer;
  }

  public PaletteWindow getPaletteWindow() {
    return this.paletteWindow;
  }

}
