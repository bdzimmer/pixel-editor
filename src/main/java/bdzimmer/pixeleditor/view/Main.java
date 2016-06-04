// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Main window and editor entry point.

// Holds several things:
//  - content directory and list of assets for easy loading
//  - global palette / palette window
//  - tile container

// Also has functions for wiring groups of editor windows.

package bdzimmer.pixeleditor.view;

import bdzimmer.pixeleditor.model.AssetMetadataUtils;
import bdzimmer.pixeleditor.model.ContentStructure;
import bdzimmer.pixeleditor.model.AssetMetadata;
import bdzimmer.pixeleditor.model.Map;
import bdzimmer.pixeleditor.model.TileContainer;
import bdzimmer.pixeleditor.model.TileOptions;
import bdzimmer.pixeleditor.model.Tileset;
import bdzimmer.pixeleditor.model.TileAttributes;
import bdzimmer.pixeleditor.controller.OldTilesetLoader;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.File;

import scala.collection.immutable.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;


public class Main extends CommonWindow {

  private static final long serialVersionUID = 1L;

  public final String contentDir;
  public final List<AssetMetadata> metadata;

  private final int[][] globalPalette = new int[256][3];
  private PaletteWindow paletteWindow = new PaletteWindow(globalPalette);
  private final TileContainer tileContainer = new TileContainer();

  /**
   * Create a new Main window.
   *
   * @param contentDir  Content directory
   * @param title       Title of the Main window
   */
  public Main(String contentDir, String title, String metadataFilename) {

    System.out.println("starting pixel-editor in content directory " + contentDir);

    this.contentDir = contentDir;
    this.metadata = AssetMetadataUtils.loadAssetMetadata(metadataFilename);

    build(JFrame.EXIT_ON_CLOSE);

    // update the statusbar to show memory usage every 30 seconds
    /*
    Timer memoryUpdateTimer = new Timer(30000, new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        updateMemoryUsageDisplay();
      }
    });
    memoryUpdateTimer.setInitialDelay(0);
    memoryUpdateTimer.start();
    */

    setFocusable(true);
    addWindowFocusListener(new WindowAdapter() {
      public void windowGainedFocus(WindowEvent event) {
        updateMemoryUsageDisplay();
      }
    });
    updateMemoryUsageDisplay();

    setAlwaysOnTop(true);
    setTitle(title);

    packAndShow(false);

  }

  /**
   * Create a TileWindow / MapWindow pair that share the same tile set.
   *
   * @param tileFileName  absolute path of tiles file
   * @param mapFileName   absolute path of map file
   */
  public void createLinkedTileAndMapWindows(String tileFileName, String mapFileName) {

    TileAttributes tileAttrs = TileOptions.getOrQuit("Tiles");

    Tileset tiles;

    if (!"".equals(tileFileName)) {
      tiles = new OldTilesetLoader(tileFileName, tileAttrs).load();
      Tileset.modPalette(tiles.palettes().apply(0), globalPalette);
      paletteWindow.repaint();
    } else {
      tiles = OldTilesetLoader.fromAttributes(tileAttrs);
    }

    Map map;
    if (!"".equals(mapFileName)) {
      map = new Map(new File(mapFileName));
    } else {
      map = new Map();
    }

    TilesEditorWindow tileWindow = new TilesEditorWindow(
        Main.this.contentDir + File.separator + ContentStructure.TileDir(),
        tiles, tileAttrs, "Tileset", tileFileName,
        paletteWindow, tileContainer);

    new MapEditorWindow(
        contentDir + File.separator + ContentStructure.MapDir(),
        map,
        mapFileName,
        tileWindow);

    tileWindow.toFront();

  }


  /**
   * Create a TileWindow for editing sprites.
   *
   * @param spritesFileName  absolute path of spritesheet tiles file
   * @param tiletype         name of spritesheet attributes
   */
  public void createSpriteWindow(String spritesFileName, String tiletype) {

    TileAttributes spriteAttributes = TileOptions.getOrQuit(tiletype);

    Tileset spriteTiles;

    if (!"".equals(spritesFileName)) {
      spriteTiles = new OldTilesetLoader(spritesFileName, spriteAttributes).load();
      Tileset.modPalette(spriteTiles.palettes().apply(0), globalPalette);
      paletteWindow.repaint();
    } else {
      spriteTiles = OldTilesetLoader.fromAttributes(TileOptions.getOrQuit(tiletype));
    }

    TilesEditorWindow spriteWindow = new TilesEditorWindow(
        Main.this.contentDir + File.separator + ContentStructure.SpriteDir(),
        spriteTiles, spriteAttributes, "Sprites", spritesFileName,
        paletteWindow, tileContainer);
    spriteWindow.getDosGraphics().setRgbPalette(globalPalette);

    spriteWindow.setLocationRelativeTo(null);

  }


  /// CommonWindow overrides

  protected JMenuBar menuBar() {

    // Menubar
    JMenuBar mainMenu = new JMenuBar();

    JMenu fileMenu = new JMenu("File");
    mainMenu.add(fileMenu);

    JMenuItem jmExit = new JMenuItem("Exit");
    jmExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        System.exit(0);
      }
    });
    fileMenu.add(jmExit);

    return mainMenu;

  }

  protected JToolBar toolBar() {

    final JToolBar mainToolbar = new JToolBar();

    // button to show the palette window in case it gets closed
    final JButton palette = new JButton("Palette");
    palette.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        paletteWindow.setVisible(true);
        paletteWindow.toFront();
      }
    });
    palette.setFocusable(false);
    mainToolbar.add(palette);
    mainToolbar.setFloatable(false);

    return mainToolbar;
  }


  protected JPanel panel() {

    JPanel panel = new JPanel();

    // Add buttons for spawning new windows and sets of windows
    panel.setLayout(new GridLayout(7, 1, 5, 5));

    JButton addTileMapWindow = new JButton("Tileset / Map Editor");
    addTileMapWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        createLinkedTileAndMapWindows("", "");
      }
    });
    panel.add(addTileMapWindow);

    JButton addSpriteWindow = new JButton("Sprite Editor");
    addSpriteWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        createSpriteWindow("", "NPC");
      }
    });
    panel.add(addSpriteWindow);

    /// /// ///

    JButton addTilesetListWindow = new JButton("Load Map Tiles");
    addTilesetListWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        new TilesLoadWindow(Main.this).setLocationRelativeTo(null);
      }
    });
    panel.add(addTilesetListWindow);

    JButton addSpritesheetListWindow = new JButton("Load Sprites");
    addSpritesheetListWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        new SpriteLoadWindow(Main.this).setLocationRelativeTo(null);
      }
    });
    panel.add(addSpritesheetListWindow);

    JButton addMapListWindow = new JButton("Load Maps");
    addMapListWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        new MapLoadWindow(Main.this).setLocationRelativeTo(null);
      }
    });
    panel.add(addMapListWindow);

    JButton addWorldWindow = new JButton("Load Script Files");
    addWorldWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        new ScriptLoadWindow(Main.this).setLocationRelativeTo(null);
      }
    });
    panel.add(addWorldWindow);

    return panel;

  }


  protected StatusBar statusBar() {
    return new StatusBar(20, 0, 0);
  }


  private void updateMemoryUsageDisplay() {
    System.gc();
    Runtime runtime = Runtime.getRuntime();
    int mb = 1024 * 1024;
    long totalMemory = runtime.totalMemory() / mb;
    long freeMemory  = runtime.freeMemory() / mb;
    // long maxMemory   = runtime.maxMemory()  / mb;
    statusBar.update(
        (totalMemory - freeMemory) + " / " + totalMemory + " MB",
        "", "");
  }

}
