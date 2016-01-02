// Copyright (c) 2015 Ben Zimmer. All rights reserved.

// Class implementing a view for displaying and editing maps.

// 2014-08-14: Refactored to sepearate out map view panel from functionality
//              specific to editing.

package bdzimmer.pixeleditor.view;

import bdzimmer.pixeleditor.model.Map;
import bdzimmer.pixeleditor.model.Palette;
import bdzimmer.pixeleditor.model.Tileset;
import bdzimmer.pixeleditor.view.MapViewPanel;
import bdzimmer.pixeleditor.view.TilesEditorWindow;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;



public class MapEditorWindow extends JFrame {

  private static final long serialVersionUID = 0; // Meaningless junk.

  private static final int TILE_SIZE = 16;

  private final String mapsDir;

  private Map map;
  public String mapFileName;
  private final TilesEditorWindow tilesEditorWindow;

  private MapViewPanel mapViewPanel;

  // GUI components that modify the Map object
  private final JCheckBoxMenuItem jmHasParallax = new JCheckBoxMenuItem("Parallax Layer");

  private int overlayEdit;
  private StatusBar statusBar = new StatusBar();


  /**
   * Create a new MapEditorWindow.
   *
   * @param mapsDir       main content directory
   * @param map           Map to display
   * @param fileName      file name of map to load
   * @param tileSet       Tiles object to use
   * @param rgbPalette    2d int array of palette to update the view with
   */
  public MapEditorWindow(
      String mapsDir,
      Map map,
      String fileName,
      TilesEditorWindow tilesEditorWindow) { // constructor

    this.mapsDir = mapsDir;
    this.map = map;
    this.mapFileName = fileName;
    this.tilesEditorWindow = tilesEditorWindow;

    updateTitle();

    // main menu
    setJMenuBar(mainMenu());

    // main toolbar
    add(mainToolbar(), BorderLayout.NORTH);

    // map view
    this.mapViewPanel = new MapViewPanel(
        this.map,
        this.tilesEditorWindow.getTileSet(),
        this.tilesEditorWindow.getDosGraphics().getRgbPalette());
    add(mapViewPanel, BorderLayout.CENTER);

    add(statusBar, BorderLayout.SOUTH);
    pack();

    mapViewPanel.setToolTipText("<html>right click: grab tile<br />left click: set tile<br />mouse wheel: zoom<br />arrow keys: scroll</html>");

    // dragging and moving mouse
    mapViewPanel.addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent event) {
        handleClicks(event);
      }

      public void mouseMoved(MouseEvent event) {
        statusBar.update(
            mapViewPanel.vlr + (int) (event.getX() / (TILE_SIZE * mapViewPanel.scale)),
            mapViewPanel.vud + (int) (event.getY() / (TILE_SIZE * mapViewPanel.scale)),
            "");
      }
    });

    // clicking mouse
    mapViewPanel.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent me) { handleClicks(me); }
    });

    mapViewPanel.addMouseWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent ae) {
        int notches = ae.getWheelRotation();
        notches = Integer.signum(notches);
        zoom(notches);
      }
    });

    // listener for scrolling with arrow keys
    setFocusable(true);
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent ae) { handleKeys(ae); }
    });

    setResizable(false);
    setVisible(true);
    repaint();

  }


  // / helper functions for handling events
  // ------------------------------------------------------

  private void zoom(int amount) {
    mapViewPanel.scale += amount;
    if (mapViewPanel.scale < 1) {
      mapViewPanel.scale = 1;
    }
    updateGraphics();
  }

  private void handleClicks(MouseEvent ae) {

    int ctud = mapViewPanel.vud
        + (ae.getY() / (MapEditorWindow.TILE_SIZE * mapViewPanel.scale));
    int ctlr = mapViewPanel.vlr
        + (ae.getX() / (MapEditorWindow.TILE_SIZE * mapViewPanel.scale));
    if (ctud < 0 || ctud > 127) {
      return;
    }
    if (ctlr < 0 || ctlr > 127) {
      return;
    }

    if (!ae.isMetaDown()) {
      if (overlayEdit == 0) {
        map.map[ctud][ctlr] = Main.currentTile; // setting tile
      } else if (overlayEdit == 1) {
        map.overMap[ctud][ctlr] = Main.currentTile;
      } else if (overlayEdit == 2) {
        map.paraMap[ctud][ctlr] = Main.currentTile;
      }
      repaint();
    } else {
      if (overlayEdit == 0) {
        Main.currentTile = map.map[ctud][ctlr]; // getting tile
      } else if (overlayEdit == 1) {
        Main.currentTile = map.overMap[ctud][ctlr];
      } else if (overlayEdit == 2) {
        Main.currentTile = map.paraMap[ctud][ctlr];
      }
    }

  }


  private void handleKeys(KeyEvent ae) {

    System.out.println("key pressed");

    if (ae.getKeyCode() == KeyEvent.VK_UP) {
      if (!ae.isAltDown()) {
        mapViewPanel.vud -= 1;
      } else {
        for (int i = mapViewPanel.vud; i < 127; i++) {
          for (int j = 0; j < 128; j++) {
            map.map[i][j] = map.map[i + 1][j];
            map.overMap[i][j] = map.overMap[i + 1][j];
          }
        }
      }
    } else if (ae.getKeyCode() == KeyEvent.VK_DOWN) {
      if (!ae.isAltDown()) {
        mapViewPanel.vud += 1;
      } else {
        for (int i = 127; i >= (mapViewPanel.vud + 1); i--) {
          for (int j = 0; j < 128; j++) {
            map.map[i][j] = map.map[i - 1][j];
            map.overMap[i][j] = map.overMap[i - 1][j];
          }
        }
      }
    } else if (ae.getKeyCode() == KeyEvent.VK_LEFT) {
      if (!ae.isAltDown()) {
        mapViewPanel.vlr -= 1;
      } else {
        for (int i = 0; i < 128; i++) {
          for (int j = mapViewPanel.vlr; j < 127; j++) {
            map.map[i][j] = map.map[i][j + 1];
            map.overMap[i][j] = map.overMap[i][j + 1];
          }
        }
      }
    } else if (ae.getKeyCode() == KeyEvent.VK_RIGHT) {
      if (!ae.isAltDown()) {
        mapViewPanel.vlr += 1;
      } else {
        for (int i = 0; i < 128; i++) {
          for (int j = 127; j >= mapViewPanel.vlr + 1; j--) {
            map.map[i][j] = map.map[i][j - 1];
            map.overMap[i][j] = map.overMap[i][j - 1];
          }
        }
      }
    }

    repaint();

  }

  // //// loading and saving maps
  // ---------------------------------------------------------

  /**
   * Choose a map file from a file chooser and load it.
   */
  public void chooseLoadMap() {

    JFileChooser jfc = new JFileChooser();
    jfc.setDialogType(JFileChooser.OPEN_DIALOG);
    jfc.setCurrentDirectory(new File(mapsDir));

    if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

      File selFile = jfc.getSelectedFile();
      try {
        map = new Map(selFile);
        jmHasParallax.setSelected(map.hasParallax);
        mapFileName = selFile.getAbsolutePath();
        updateTitle();

        System.out.println("Map file name: " + mapFileName);
        mapViewPanel.setMap(map);
        mapViewPanel.vud = 0;
        mapViewPanel.vlr = 0;
        mapViewPanel.updateGraphics();
        mapViewPanel.repaint();
        repaint();

      } catch (NullPointerException e) {
        System.err.println(e);
        return;
      }
    }
  }

  /**
   * Choose a map file from a file chooser and save it.
   */
  public void chooseSaveMap() {
    JFileChooser jfc = new JFileChooser();
    jfc.setDialogType(JFileChooser.SAVE_DIALOG);
    jfc.setCurrentDirectory(new File(mapsDir));
    jfc.setSelectedFile(new File(mapFileName));

    // call up the dialog and examine what it returns.

    if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {

      File mapFile = jfc.getSelectedFile();
      try {
        map.save(mapFile); // getSelectedFile returns the file that was selected
        repaint();
      } catch (NullPointerException e) {
        System.err.println(e);
        return;
      }
    }
  }


  // update the title
  private void updateTitle() {
    setTitle(map.mapDesc.trim() + " (" + map.tileFileName + ") - " + mapFileName);
  }


  // updating graphics
  // --------------------------------------------------------

  private void updateGraphics() {
    mapViewPanel.setTileset(tilesEditorWindow.getTileSet());
    mapViewPanel.updateGraphics();
    pack();
    repaint();
  }


  public void paint(Graphics gr) {
    super.paint(gr);
    mapViewPanel.repaint();
  }


  private JMenuBar mainMenu() {

    JMenuBar mainMenu = new JMenuBar();

    JMenu fileMenu = new JMenu("File");

    JMenuItem jmNew = new JMenuItem("New");
    JMenuItem jmOpen = new JMenuItem("Open");
    JMenuItem jmSave = new JMenuItem("Save");
    JMenuItem jmSaveAs = new JMenuItem("Save As");

    JMenu editMenu = new JMenu("Edit");

    JMenuItem jmSetTitle = new JMenuItem("Set title and tiles file...");

    jmHasParallax.setSelected(false);

    JMenu viewMenu = new JMenu("View");

    JMenuItem fullMap =  new JMenuItem("Full Map");


    jmNew.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        MapEditorWindow.this.map.erase();
        mapViewPanel.vud = 0;
        mapViewPanel.vlr = 0;
        repaint();
      }
    });

    jmOpen.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        chooseLoadMap();
      }
    });

    jmSave.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        MapEditorWindow.this.map.save(new File(mapFileName));
      }
    });

    jmSaveAs.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        chooseSaveMap();
      }
    });

    jmSetTitle.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        MapEditorWindow.this.map.mapDesc = JOptionPane.showInputDialog("Enter new title:");
        MapEditorWindow.this.map.tileFileName = JOptionPane
            .showInputDialog("Enter new tile file name:");
        updateTitle();
      }
    });

    fullMap.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        Tileset tiles = MapEditorWindow.this.tilesEditorWindow.getTileSet();
        Palette palette = Tileset.extractPalette(
            tiles.palettes().apply(0),
            MapEditorWindow.this.tilesEditorWindow.getDosGraphics().getRgbPalette());
        new ImageWindow(MapEditorWindow.this.map.image(tiles, palette));
      }
    });


    fileMenu.add(jmNew);
    fileMenu.add(jmOpen);
    fileMenu.add(jmSave);
    fileMenu.add(jmSaveAs);
    mainMenu.add(fileMenu);

    editMenu.add(jmSetTitle);
    editMenu.addSeparator();
    editMenu.add(jmHasParallax);
    mainMenu.add(editMenu);

    viewMenu.add(fullMap);
    mainMenu.add(viewMenu);

    return mainMenu;

  }


  private JToolBar mainToolbar() {

    final JToolBar mainToolbar = new JToolBar();
    final JToggleButton gridShow = new JToggleButton("Grid");
    gridShow.setFocusable(false);

    final JButton editLayer = new JButton("E Back");
    editLayer.setFocusable(false);

    final JToggleButton dispBack = new JToggleButton("Back");
    dispBack.setSelected(true);
    dispBack.setFocusable(false);

    final JToggleButton dispOver = new JToggleButton("Ovl");
    dispOver.setSelected(true);
    dispOver.setFocusable(false);

    final JToggleButton dispBounds = new JToggleButton("Bounds");
    dispBounds.setFocusable(false);

    gridShow.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        System.out.println("grid show: "+ gridShow.isSelected());
        mapViewPanel.setDispGridlines(gridShow.isSelected());
        repaint();
      }
    });

    editLayer.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (overlayEdit == 0) {
          editLayer.setText("E Ovl");
          overlayEdit = 1;
          mapViewPanel.setParallaxEdit(false);
        } else if (overlayEdit == 1) {
          editLayer.setText("E Px");
          overlayEdit = 2;
          mapViewPanel.setParallaxEdit(true);
        } else if (overlayEdit == 2) {
          editLayer.setText("E Back");
          overlayEdit = 0;
          mapViewPanel.setParallaxEdit(false);
        }
        repaint();
      }
    });

    dispBack.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mapViewPanel.setDispBack(dispBack.isSelected());
        repaint();
      }
    });

    dispOver.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mapViewPanel.setDispOver(dispOver.isSelected());
        repaint();
      }
    });

    dispBounds.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mapViewPanel.setDispBounds(dispBounds.isSelected());
        repaint();
      }
    });

    mainToolbar.add(gridShow);
    mainToolbar.add(editLayer);
    mainToolbar.add(dispBack);
    mainToolbar.add(dispOver);
    mainToolbar.add(dispBounds);
    mainToolbar.setFloatable(false);

    return mainToolbar;

  }

}
