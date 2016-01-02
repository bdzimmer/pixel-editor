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
import java.io.File;

import scala.collection.immutable.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


public class Main extends JFrame {

  private static final long serialVersionUID = 1L;

  public final String contentDir;
  public final List<AssetMetadata> metadata;

  private final int[][] globalPalette = new int[256][3];
  private final PaletteWindow paletteWindow = new PaletteWindow(globalPalette);
  private final TileContainer tileContainer = new TileContainer();

  /**
   * Create a new Main window.
   *
   * @param contentDir  Content directory
   * @param title       Title of the Main window
   */
  public Main(String contentDir, String title, String metadataFilename) {

    System.out.println("content directory: " + contentDir);

    this.contentDir = contentDir;
    this.metadata = AssetMetadataUtils.loadAssetMetadata(metadataFilename);

    paletteWindow.setLocationRelativeTo(null);

    setAlwaysOnTop(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle(title);

    // Menubar
    JMenuBar mainMenu = new JMenuBar();
    setJMenuBar(mainMenu);

    JMenu fileMenu = new JMenu("File");
    mainMenu.add(fileMenu);

    JMenuItem jmExit = new JMenuItem("Exit");
    jmExit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        System.exit(0);
      }
    });
    fileMenu.add(jmExit);

    // Add buttons for spawning new windows.
    this.setLayout(new GridLayout(7, 1, 5, 5));

    JButton addTileMapWindow = new JButton("Tileset / Map Editor");
    addTileMapWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        createLinkedTileAndMapWindows("", "");
      }
    });
    this.add(addTileMapWindow);

    JButton addSpriteWindow = new JButton("Sprite Editor");
    addSpriteWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        createSpriteWindow("", "NPC");
      }
    });
    this.add(addSpriteWindow);

    /// /// ///

    JButton addTilesetListWindow = new JButton("Load Map Tiles");
    addTilesetListWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        new TilesLoadWindow(Main.this).setLocationRelativeTo(null);
      }
    });
    this.add(addTilesetListWindow);

    JButton addSpritesheetListWindow = new JButton("Load Sprites");
    addSpritesheetListWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        new SpriteLoadWindow(Main.this).setLocationRelativeTo(null);
      }
    });
    this.add(addSpritesheetListWindow);

    JButton addMapListWindow = new JButton("Load Maps");
    addMapListWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        new MapLoadWindow(Main.this).setLocationRelativeTo(null);
      }
    });
    this.add(addMapListWindow);

    JButton addWorldWindow = new JButton("Load Script Files");
    addWorldWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        new ScriptLoadWindow(Main.this).setLocationRelativeTo(null);
      }
    });
    this.add(addWorldWindow);

    this.pack();
    setVisible(true);

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


}
