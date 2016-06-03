// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Modified around 2015-12-17 for new Tileset class. A lot of the code
// in here should be moved to controller classes.

package bdzimmer.pixeleditor.view;

import bdzimmer.pixeleditor.controller.OldTilesetLoader;
import bdzimmer.pixeleditor.model.DosGraphics;
import bdzimmer.pixeleditor.model.Palette;
import bdzimmer.pixeleditor.model.TileContainer;
import bdzimmer.pixeleditor.model.TileOptions;
import bdzimmer.pixeleditor.model.Tileset;
import bdzimmer.pixeleditor.model.TileAttributes;
import bdzimmer.pixeleditor.view.DragDrop.TileExportTransferHandler;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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



public class TilesEditorWindow extends JFrame {

  private static final long serialVersionUID = 0;

  private final String tilesDir;
  private final String title;

  private final StatusBar statusBar = new StatusBar();
  private final int scale = 3;

  private Tileset tileset;
  private TileAttributes attrs;
  private String tileFilename;

  private DosGraphics dosGraphics;
  private ZoomedTileWindow zoomWindow;
  private AnimationWindow animationWindow;


  private final PaletteWindow paletteWindow;
  private final TileContainer tileContainer;

  private JPanel graphicsPanel = new JPanel();

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

    // UI stuff

    // redraw on focus gained
    addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent event) { repaint(); }
    });

    // main menu
    setJMenuBar(mainMenu());

    // toolbar
    add(mainToolbar(), BorderLayout.NORTH);

    // tileset visualization
    dosGraphics = createDosGraphics();


    graphicsPanel.setToolTipText("<html>right click: grab tile<br />left click: set tile<br /></html>");

    // clicking to select and manipulate tiles
    graphicsPanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) { handleClicks(event, true); }
    });

    graphicsPanel.setTransferHandler(new TileExportTransferHandler(tileContainer));

    // drag tiles onto other components
    graphicsPanel.addMouseMotionListener(new MouseAdapter() {
      public void mouseDragged(MouseEvent e) {
        handleClicks(e, false);
        JPanel jp = (JPanel) e.getSource();
        jp.getTransferHandler().exportAsDrag(jp, e, TransferHandler.COPY);
      }
    });

    graphicsPanel.add(dosGraphics);
    add(graphicsPanel, BorderLayout.CENTER);

    // status bar
    add(statusBar, BorderLayout.SOUTH);

    pack();
    setResizable(false);
    setVisible(true);

  }


  // create an appropriately sized and scaled DosGraphics for the tileset
  private DosGraphics createDosGraphics() {
    DosGraphics dg = new DosGraphics(
        (int)Math.ceil((float)tileset.tiles().length / tileset.tilesPerRow()) * tileset.height(),
        tileset.tilesPerRow() * tileset.width(),
        this.scale);

    dg.setGridDimensions(tileset.width(), tileset.height());
    dg.setRgbPalette(paletteWindow.getDosGraphics().getRgbPalette());

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

      // TODO: move this tile copying logic to a controller class???

      // TODO: use drag / drop functionality for this

      // Calculate maximum size we can copy
      // The global tile bitmap here seems kind of dumb, but it's there to allow
      // copying tiles across tileset -- important functionality.

      int udlength = Math.min(tileset.height(), tileContainer.getTileBitmap().length);
      int lrlength = Math.min(tileset.width(), tileContainer.getTileBitmap()[0].length);

      for (int i = 0; i < udlength; i++) {
        for (int j = 0; j < lrlength; j++) {
          tileset.tiles()[newTile].pixels()[i][j] = tileContainer.getTileBitmap()[i][j];
        }
      }

      // set the copy as the current tile
      tileContainer.setTileIndex(selectedTile);
      tileContainer.setTileBitmap(tileset.tiles()[selectedTile].pixels());

      repaint();


    }

    statusBar.update(0, 0, "" + selectedTile);

  }


  // select a tile from the set into the tile container
  // and show it in the ZoomWindow
  public void selectTile(int selectedTile) {

    // set the current tile
    tileContainer.setTileIndex(selectedTile);
    tileContainer.setTileBitmap(tileset.tiles()[selectedTile].pixels());

    // show in zoom window
    if (zoomWindow == null || !zoomWindow.isVisible()) {
      zoomWindow = new ZoomedTileWindow(
          "Zoom",
          tileset.tiles()[selectedTile].pixels(),
          paletteWindow);
      zoomWindow.setTileWindow(this);
      zoomWindow.setLocationRelativeTo(this);
    } else {
      zoomWindow.setTile(tileset.tiles()[selectedTile].pixels(), selectedTile);
    }
    zoomWindow.toFront();

    // show in animation window
    if (animationWindow != null && animationWindow.isVisible()) {
      animationWindow.setTileIndex(selectedTile);
      animationWindow.toFront();
    }

  }


  // convert all color indices that are RGB(0, 0, 0) to 0
  private void blacken() {

    boolean[] blackColors = new boolean[256];

    // determine which colors are equiv to black
    for (int i = 0; i < 256; i++) {
      if (this.dosGraphics.getRgbPalette()[i][0] == 0
          && this.dosGraphics.getRgbPalette()[i][1] == 0
          && this.dosGraphics.getRgbPalette()[i][2] == 0) {
        blackColors[i] = true;
      }
    }

    for (int i = 0; i < tileset.tiles().length; i++) {
      for (int j = 0; j < tileset.tiles()[0].pixels().length; j++) {
        for (int k = 0; k < tileset.tiles()[0].pixels()[0].length; k++) {
          if (blackColors[tileset.tiles()[i].pixels()[j][k]]) {
            // temporarily...
            tileset.tiles()[i].pixels()[j][k] = 0;
          }
        }
      }
    }

  }


  // swap colors 0 and 255
  private void swapTransparency() {
    for (int i = 0; i < tileset.tiles().length; i++) {
      for (int j = 0; j < tileset.tiles()[0].pixels().length; j++) {
        for (int k = 0; k < tileset.tiles()[0].pixels()[0].length; k++) {
          int tempColor = tileset.tiles()[i].pixels()[j][k];
          if (tempColor == 0) {
            tileset.tiles()[i].pixels()[j][k] = 255;
          }
          if (tempColor == 255) {
            tileset.tiles()[i].pixels()[j][k] = 0;
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

    graphicsPanel.remove(dosGraphics);
    dosGraphics = createDosGraphics();
    graphicsPanel.add(dosGraphics);

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
    Tileset.modPalette(tileset.palettes().apply(0), dosGraphics.getRgbPalette());
    tileFilename = filename;
    paletteWindow.repaint();
    updateTitle();
    repaint();
  }


  // grab the palette, update the tileset, and save it
  private void saveTileset(String filename) {

    // mutate the tileset's default palette before saving!!!
    Palette newPal = Tileset.extractPalette(tileset.palettes().apply(0), dosGraphics.getRgbPalette());
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
    dosGraphics.drawTileset(tileset);
    dosGraphics.repaint();

  }


  private JMenuBar mainMenu() {

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

  private JToolBar mainToolbar() {

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
            TilesEditorWindow.this,
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

  public Tileset getTileSet() {
    return this.tileset;
  }

  public DosGraphics getDosGraphics() {
    return this.dosGraphics;
  }

  public TileContainer getTileContainer() {
    return this.tileContainer;
  }

  public PaletteWindow getPaletteWindow() {
    return this.paletteWindow;
  }

}
