// Copyright (c) 2016 Ben Zimmer. All rights reserved.

// Drag and drop functionality experimentation.

// 2016-06-02

package bdzimmer.pixeleditor.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import bdzimmer.pixeleditor.model.TileContainer;

public class DragDrop {

  public static final DataFlavor TILE_DATA_FLAVOR = new DataFlavor(int[].class, "Integer Array");


  static class TileTransferable implements Transferable {

    private int[][] tile;

    public TileTransferable(int[][] tile) {
      this.tile = tile;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] { TILE_DATA_FLAVOR };
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return flavor.equals(TILE_DATA_FLAVOR);
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {

      if (flavor.equals(TILE_DATA_FLAVOR)) {
        return tile;
      } else {
        throw new UnsupportedFlavorException(flavor);
      }
    }
  }


  // drag a tile off

  public static class TileExportTransferHandler extends TransferHandler {

    private static final long serialVersionUID = 1L;

    private final TileContainer tc;

    public TileExportTransferHandler(TileContainer tc) {
      this.tc = tc;
    }

    @Override
    public int getSourceActions(JComponent c) {
      return DnDConstants.ACTION_COPY_OR_MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
      Transferable t = new TileTransferable(tc.getTileBitmap());
      return t;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        super.exportDone(source, data, action);
    }

  }



  // drop a tile on!

  public static class TileImportTransferHandler extends TransferHandler {

    private static final long serialVersionUID = 1L;

    private final TileContainer tc;

    public TileImportTransferHandler(TileContainer tc) {
      this.tc = tc;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
      return support.isDataFlavorSupported(TILE_DATA_FLAVOR);
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
      boolean accept = false;
      if (canImport(support)) {
        try {
          Transferable t = support.getTransferable();
          Object value = t.getTransferData(TILE_DATA_FLAVOR);
          if (value instanceof int[][]) {
            tc.setTileBitmap((int[][])value);
          }
        } catch (Exception exp) {
          exp.printStackTrace();
        }
      }
      return accept;
    }
  }

}
