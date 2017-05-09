// Copyright (c) 2015 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.model;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.image.WritableRaster;

public class Map {

  public String mapDesc = "";

  public String tileFileName = "";
  public int[][] paraMap = new int[24][40];
  public int[][] map = new int[128][128];
  public int[][] overMap = new int[128][128];

  public boolean hasParallax;

  public int mud;
  public int mlr;
  public int pmud;
  public int pmlr;


  public Map() {
    // no-arg constructor - blank map
  }


  /**
   * Load the map from a file.
   *
   * @param mapFile File to load.
   */
  public Map(File mapFile) {
    // erase current map
    for (int i = 0; i < 128; i++) {
      for (int j = 0; j < 128; j++) {
        this.map[i][j] = 0;
        this.overMap[i][j] = 0;
      }
    }
    for (int i = 0; i < 24; i++) {
      for (int j = 0; j < 40; j++) {
        this.paraMap[i][j] = 0;
      }
    }

    // open file for input
    try {
      QbInputStream mapIn = new QbInputStream(new FileInputStream(mapFile));
      // read description
      int[] mapDescB = new int[30];
      char[] mapDesc = new char[30];
      for (int i = 0; i < 30; i++) {
        mapDescB[i] = mapIn.readQbUnsignedByte();
        mapDesc[i] = (char) mapDescB[i];
        // System.out.println(mapDesc[i]);
      }
      this.mapDesc = new String(mapDesc);
      // read tileset
      int[] tileFileNameB = new int[8];
      char[] tileFileName = new char[8];
      for (int i = 0; i < 8; i++) {
        tileFileNameB[i] = mapIn.readQbUnsignedByte();
        tileFileName[i] = (char) tileFileNameB[i];
      }
      this.tileFileName = new String(tileFileName).trim();

      this.mud = mapIn.readQbUnsignedShortLow();
      this.mlr = mapIn.readQbUnsignedShortLow();

      // map data
      for (int i = 0; i <= mud; i++) {
        for (int j = 0; j <= mlr; j++) {
          this.map[i][j] = mapIn.readQbUnsignedShortLow();
        }
      }
      for (int i = 0; i <= mud; i++) {
        for (int j = 0; j <= mlr; j++) {
          this.overMap[i][j] = mapIn.readQbUnsignedShortLow();
        }
      }

      // System.out.println("Loaded normal and overlay layers.");

      try {
        this.pmud = mapIn.readByte();

        if (this.pmud > -1) { // load parallax layer
          this.pmud = mapIn.readQbUnsignedShortLow();
          this.pmlr = mapIn.readQbUnsignedShortLow();
          for (int i = 0; i <= pmud; i++) {
            for (int j = 0; j <= pmlr; j++) {
              this.paraMap[i][j] = mapIn.readQbUnsignedShortLow();
            }
          }
          this.hasParallax = true;

          // System.out.println("Loaded parallax layer.");
        }

      } catch (EOFException e) {
        this.hasParallax = false;

        this.pmud = 0;
        // System.out.println("No parallax layer.");
      }

      mapIn.close();

    } catch (FileNotFoundException e) {
      System.err.println(e); // print exception if the file doesn't exist.
      return;
    } catch (IOException e) {
      System.err.println(e);
      return;
    }

  }


  /**
   * Save the map to a file.
   *
   * @param mapFile File to save
   */
  public void save(File mapFile) {

    // open file for input
    try {
      QbOutputStream mapOut = new QbOutputStream(new FileOutputStream(mapFile));
      // System.out.println("Opened output stream.");
      // read description
      int[] mapDescB = new int[30];
      char[] mapDesc = this.mapDesc.toCharArray();
      for (int i = 0; i < 30; i++) {
        if (i < mapDesc.length) {
          mapDescB[i] = (int) mapDesc[i];
        } else {
          mapDescB[i] = (int) ' ';
        }
        mapOut.writeQbUnsignedByte(mapDescB[i]);
      }
      // write tileset name
      int[] tileFileNameB = new int[8];
      char[] tileFileName = this.tileFileName.toCharArray();
      for (int i = 0; i < 8; i++) {
        if (i < tileFileName.length) {
          tileFileNameB[i] = (int) tileFileName[i];
        } else {
          tileFileNameB[i] = (int) ' ';
        }
        mapOut.writeQbUnsignedByte(tileFileNameB[i]);

      }

      // Determine size of map to save.
      this.mud = 0;
      this.mlr = 0;
      for (int i = 0; i < 128; i++) {
        for (int j = 0; j < 128; j++) {
          if (this.map[i][j] > 0) {
            if (i > this.mud) {
              mud = i;
            }
            if (j > this.mlr) {
              mlr = j;
            }
          }
        }
      }

      System.out.println("Map size: " + mud + " " + mlr);

      mapOut.writeQbUnsignedShortLow(this.mud);
      mapOut.writeQbUnsignedShortLow(this.mlr);

      // map data
      for (int i = 0; i <= mud; i++) {
        for (int j = 0; j <= mlr; j++) {
          mapOut.writeQbUnsignedShortLow(this.map[i][j]);
        }
      }
      for (int i = 0; i <= mud; i++) {
        for (int j = 0; j <= mlr; j++) {
          mapOut.writeQbUnsignedShortLow(this.overMap[i][j]);
        }
      }

      if (this.hasParallax) {
        // save the parallax map...

        // Determine size of map to save.
        this.pmud = 0;
        this.pmlr = 0;
        for (int i = 0; i < 24; i++) {
          for (int j = 0; j < 40; j++) {
            if (this.paraMap[i][j] > 0) {
              if (i > this.pmud) {
                pmud = i;
              }
              if (j > this.pmlr) {
                pmlr = j;
              }
            }
          }
        }

        System.out.println("Parallax map size: " + pmud + " " + pmlr);

        mapOut.writeQbUnsignedShortLow(this.pmud);
        mapOut.writeQbUnsignedShortLow(this.pmlr);

        // map data
        for (int i = 0; i <= pmud; i++) {
          for (int j = 0; j <= pmlr; j++) {
            mapOut.writeQbUnsignedShortLow(this.paraMap[i][j]);
          }
        }

      }

      mapOut.close();
    } catch (FileNotFoundException e) {
      System.err.println(e); // print exception if the file doesn't exist.
      return;
    } catch (IOException e) {
      System.err.println(e);
      return;
    }
  }


  /**
   * Get an image of the map.
   *
   * @param tiles         Tiles object to use
   * @param palette       palette to use
   * @return  image representation of the map
   */
  public BufferedImage image(Tileset tiles, Palette palette) {
    return image(tiles.tiles(), palette);
  }


  public BufferedImage image(Tile[] tiles, Palette palette) {

    int height = tiles[0].bitmap().length;
    int width = tiles[0].bitmap()[0].length;

    BufferedImage mapImage = Tileset.indexedImage(
        (mlr + 1) * width,
        (mud + 1) * height,
        palette, new Color(50, 0, 50));

    WritableRaster wr = mapImage.getRaster();

    for (int i = 0; i <= mud; i++) {
      for (int j = 0; j <= mlr; j++) {

        for (int k = 0; k < height; k++) {
          wr.setPixels(
              j * width, i * height + k,
              width, 1,
              tiles[this.map[i][j]].bitmap()[k]);
        }

        if (this.overMap[i][j] > 0) {
          for (int k = 0; k < height; k++) {
            for (int l = 0; l < width; l++) {
              int curColor = tiles[this.overMap[i][j]].bitmap()[k][l];
              if (curColor != 255) {
                wr.setPixels(
                    j * width + l, i * height + k,
                    1, 1,
                    new int[]{curColor});
              }
            }
          }
        }

      }
    }

    return mapImage;

  }




  /**
   * Wipe out the contents of the Map.
   *
   */
  public void erase() {
    mapDesc = "";
    tileFileName = "";
    for (int i = 0; i < 128; i++) {
      for (int j = 0; j < 128; j++) {
        map[i][j] = 0;
        overMap[i][j] = 0;
      }

    }

  }

}
