// Copyright (c) 2015 Ben Zimmer. All rights reserved.

package bdzimmer.pixeleditor.controller;

import bdzimmer.pixeleditor.model.Color;

public class PaletteUtils {

  // mutate palette, with linear interpolation between start and end colors
  public static void interpolateLinear(int[][] pal, int start, int end) {

    // interpolate between current color and clicked color
    int numColors = Math.abs(end - start);
    int direction = (end > start) ? 1 : -1;

    for (int i = 1; i < numColors; i++) {
      int curColor = start + i * direction;
      pal[curColor][0] = pal[start][0] + (int)((pal[end][0] - pal[start][0]) / (float)numColors * i);
      pal[curColor][1] = pal[start][1] + (int)((pal[end][1] - pal[start][1]) / (float)numColors * i);
      pal[curColor][2] = pal[start][2] + (int)((pal[end][2] - pal[start][2]) / (float)numColors * i);
    }

  }


  public static void interpolateLinear(Color[] pal, int start, int end) {

    // interpolate between current color and clicked color
    int numColors = Math.abs(end - start);
    int direction = (end > start) ? 1 : -1;

    for (int i = 1; i < numColors; i++) {
      int curColor = start + i * direction;
      int r = pal[start].r() + (int)((pal[end].r() - pal[start].r()) / (float)numColors * i);
      int g = pal[start].g() + (int)((pal[end].g() - pal[start].g()) / (float)numColors * i);
      int b = pal[start].b() + (int)((pal[end].b() - pal[start].b()) / (float)numColors * i);
      pal[curColor] = new Color(r, g, b);
    }

  }


}
