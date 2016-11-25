package org.gudy.azureus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Represents a Piece and the status of its different chunks (un-requested, requested, downloaded, written).
 * 
 * @author Olivier
 *
 */
public class Piece {
  private static final int blocSize = 32768;

  private int length;
  private int nbBlocs;
  private int pieceNumber;

  private int lastBlocSize;

  private boolean[] downloaded;
  private boolean[] requested;
  private boolean[] written;
  public int completed;
  public boolean isBeingChecked = false;

  private PeerManager manager;

  private Table table;
  private TableItem item;
  private Display display;

  private String[] oldTexts;

  public Piece(PeerManager manager, int length) {
    this.manager = manager;
    //System.out.println("Creating Piece of Size " + length); 
    this.length = length;
    nbBlocs = (length + blocSize - 1) / blocSize;
    downloaded = new boolean[nbBlocs];
    requested = new boolean[nbBlocs];
    written = new boolean[nbBlocs];

    if ((length % blocSize) != 0)
      lastBlocSize = length % blocSize;
    else
      lastBlocSize = blocSize;

    oldTexts = new String[6];
    for (int i = 0; i < oldTexts.length; i++) {
      oldTexts[i] = "";
    }
  }

  public Piece(Table table, PeerManager manager, int length, int pieceNumber) {
    this(manager, length);
    this.pieceNumber = pieceNumber;
    this.table = table;
    final Table _table = table;
    if (table.isDisposed())
      return;
    this.display = table.getDisplay();
    if (display.isDisposed())
      return;
    display.syncExec(new Runnable() {
      public void run() {
        if (_table.isDisposed())
          return;
        item = new TableItem(_table, SWT.NULL);
      }
    });
  }

  public void setWritten(int blocNumber) {
    written[blocNumber] = true;
  }

  public boolean isComplete() {
    boolean complete = true;
    for (int i = 0; i < nbBlocs; i++) {
      complete = complete && written[i];
    }
    return complete;
  }

  public boolean isWritten(int blockNumber) {
    return written[blockNumber];
  }

  public void setBloc(int blocNumber) {
    downloaded[blocNumber] = true;
    completed++;
  }

  // This method is used to clear the requested information
  public void clearRequested(int blocNumber) {
    requested[blocNumber] = false;
  }

  // This method will return the first non requested bloc and
  // will mark it as requested
  public synchronized int getAndMarkBlock() {
    int blocNumber = -1;
    for (int i = 0; i < nbBlocs; i++) {
      if (!requested[i]) {
        blocNumber = i;
        requested[i] = true;

        //To quit loop.
        i = nbBlocs;
      }
    }
    return blocNumber;
  }

  public synchronized void unmarkBlock(int blocNumber) {
    if (!downloaded[blocNumber])
      requested[blocNumber] = false;
  }

  public int getBlockSize(int blocNumber) {
    if (blocNumber == (nbBlocs - 1))
      return lastBlocSize;
    return blocSize;
  }

  public void free() {
    if (display.isDisposed())
      return;
    display.syncExec(new Runnable() {
      public void run() {
        if (table.isDisposed())
          return;
        table.remove(table.indexOf(item));
      }
    });
  }

  public int getCompleted() {
    return completed;
  }

  public void updateDisplay() {
    if (MainSwt.getMain().getMode() != MainSwt.FULLMODE)
      return;

    if (display.isDisposed())
      return;
    if (item == null)
      return;
    if (item.isDisposed())
      return;

    String tmp;

    tmp = "" + pieceNumber;
    if (oldTexts[0].equals("")) {
      item.setText(0, tmp);
      oldTexts[0] = tmp;
    }

    tmp = PeerStats.format(length);
    if (oldTexts[1].equals("")) {
      item.setText(1, tmp);
      oldTexts[1] = tmp;
    }

    tmp = "" + nbBlocs;
    if (oldTexts[2].equals("")) {
      item.setText(2, tmp);
      oldTexts[2] = tmp;
    }
    tmp = "" + completed;
    if (!(oldTexts[4].equals(tmp))) {
      item.setText(4, tmp);
      oldTexts[4] = tmp;
    }

    tmp = "" + manager.getAvailability(pieceNumber);
    if (!(oldTexts[5].equals(tmp))) {
      item.setText(5, tmp);
      oldTexts[5] = tmp;
    }

    Rectangle bounds = item.getBounds(3);
    int width = bounds.width - 1;
    int x0 = bounds.x;
    int y0 = bounds.y + 1;
    int height = bounds.height - 3;
    if(width < 10 || height < 3) return;
    Image image = new Image(display, width, height);
    Color blue = new Color(display, new RGB(0, 128, 255));
    Color green = new Color(display, new RGB(192, 224, 255));
    Color white = new Color(display, new RGB(255, 255, 255));
    Color color;
    GC gc = new GC(table);
    GC gcImage = new GC(image);
    for (int i = 0; i < nbBlocs; i++) {
      int a0 = (i * width) / nbBlocs;
      int a1 = ((i + 1) * width) / nbBlocs;
      color = white;
      if (requested[i])
        color = green;
      if (written[i]) {
        color = blue;
      }
      gcImage.setBackground(color);
      Rectangle rect = new Rectangle(a0, 1, a1, height);
      gcImage.fillRectangle(rect);
    }
    gcImage.dispose();
    blue.dispose();
    green.dispose();
    white.dispose();
    Color colorGrey = new Color(display, new RGB(170, 170, 170));
    gc.setForeground(colorGrey);
    gc.drawImage(image, x0, y0);
    gc.drawRectangle(new Rectangle(x0, y0, width, height));
    gc.dispose();
    colorGrey.dispose();
    image.dispose();
  }

  public void setBeingChecked() {
    this.isBeingChecked = true;
  }

  public boolean isBeingChecked() {
    return this.isBeingChecked;
  }

}