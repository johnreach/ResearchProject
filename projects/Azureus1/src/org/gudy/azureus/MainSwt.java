package org.gudy.azureus;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * The whole GUI class.
 * (except for Console Item,PeerTableItem and Pieces that also handle GUI elements).
 * 
 * @author Olivier
 *
 */
public class MainSwt {

  private static final int upRates[] = {0,2,3,4,5,6,7,8,9,10,11,12,13,14,15,20,25,30,35,40,45,50,60,70,80,90,100,150,200,250,300,350,400,450,500,600,700,800,900,1000};  
  private static MainSwt main;

  final Display display;
  final Shell shell;
  final TabFolder tabFolder;
  Table table;
  Table tablePieces;
  Group gFile;
  Label piecesInfo;
  Canvas piecesImage;
  Image pImage;
  Label piecesPercent;
  Label fileInfo;
  Canvas fileImage;
  Image fImage;
  Label filePercent;
  Group gAvailability;
  Label availabilityInfo;
  Canvas availabilityImage;
  Image aImage;
  Label availabilityPercent;
  Group gTransfer;
  Label timeElapsed;
  Label timeRemaining;
  Label download;
  Label downloadSpeed;
  Label upload;
  Label uploadSpeed;
  Combo maxUploads;
  Label totalSpeed;
  int maxUploadsValue;
  Combo maxSpeed;
  Label seeds;
  Label peers;
  Group gInfo;
  Label fileName;
  Label fileSize;
  Label saveIn;
  Label hash;
  Label tracker;
  Label trackerUpdateIn;
  Label pieceNumber;
  Label pieceSize;

  ScrolledComposite consoleContainer;
  StyledText console;

  Composite compConfig;
  Group gGeneral;
  Group gComponents;
  final TabFolder folderComponents;
  Group gCommand;

  Button bLogEnabled;

  private boolean validOverall;
  private boolean validPieces;
  private boolean validAvailability;
  Color colorGrey;
  Color[] blues;

  private int mode;
  public static final int FULLMODE = 1;
  public static final int REDUCEDMODE = 2;
  public static final int HIDDENMODE = 3;

  private Shell splash;
  private boolean moving;
  private int xPressed;
  private int yPressed;
  private Label lDrag;
  private Label splashFile;
  private Label splashPercent;
  private Label splashDown;
  private Label splashUp;
  private int hSize;

  private Label programId;
  private Text overrideIp;

  public MainSwt(Display display) {
    final ConfigurationManager config = ConfigurationManager.getInstance();
    mode = FULLMODE;
    main = this;
    validOverall = false;
    validAvailability = false;
    validPieces = false;
    colorGrey = new Color(display, new RGB(170, 170, 170));
    blues = new Color[5];
    blues[4] = new Color(display, new RGB(0, 128, 255));
    blues[3] = new Color(display, new RGB(64, 160, 255));
    blues[2] = new Color(display, new RGB(128, 192, 255));
    blues[1] = new Color(display, new RGB(192, 224, 255));
    blues[0] = new Color(display, new RGB(255, 255, 255));
    this.display = display;
    InputStream is =
      ClassLoader.getSystemResourceAsStream("org/gudy/azureus/azureus.gif");
    Image im = new Image(display, is);
    InputStream isd =
      ClassLoader.getSystemResourceAsStream("org/gudy/azureus/dragger.gif");
    Image idragger = new Image(display, isd);
    InputStream ism =
      ClassLoader.getSystemResourceAsStream("org/gudy/azureus/minimize.gif");
    Image iminimize = new Image(display, ism);
    InputStream ism2 =
      ClassLoader.getSystemResourceAsStream("org/gudy/azureus/maximize.gif");
    Image imaximize = new Image(display, ism2);
    InputStream ism3 =
      ClassLoader.getSystemResourceAsStream("org/gudy/azureus/azureus.jpg");
    Image iAbout = new Image(display, ism3);

    //The splash Screen setup
    splash = new Shell(display, SWT.ON_TOP);
    lDrag = new Label(splash, SWT.NULL);
    lDrag.setImage(idragger);
    lDrag.pack();
    int hSizeImage = lDrag.getSize().y;
    int xSize = lDrag.getSize().x + 3;
    lDrag.setLocation(0, 0);

    final Display _display = display;
    MouseListener mListener = new MouseAdapter() {
      public void mouseDown(MouseEvent e) {
        xPressed = e.x;
        yPressed = e.y;
        moving = true;
        //System.out.println("Position : " + xPressed + " , " + yPressed);          
      }

      public void mouseUp(MouseEvent e) {
        moving = false;
      }
      public void mouseDoubleClick(MouseEvent e) {
        splash.setVisible(false);
        shell.setVisible(true);
        mode = FULLMODE;
        Logger.getLogger().setEnabled(bLogEnabled.getSelection());
        Logger.getLogger().log(
          0,
          0,
          Logger.ERROR,
          "!!! Console is restarted !!!");
        moving = false;
      }

    };
    MouseMoveListener mMoveListener = new MouseMoveListener() {
      public void mouseMove(MouseEvent e) {
        if (moving) {
          int dX = xPressed - e.x;
          int dY = yPressed - e.y;
          //System.out.println("dX,dY : " + dX + " , " + dY);
          Point currentLoc = splash.getLocation();
          splash.setLocation(currentLoc.x - dX, currentLoc.y - dY);
          //System.out.println("Position : " + xPressed + " , " + yPressed);
        }
      }
    };

    splash.setText(".: Azureus :.");
    splash.setImage(im);
    splash.setBackground(blues[0]);
    splash.setForeground(blues[4]);
    splash.addMouseListener(mListener);
    splash.addMouseMoveListener(mMoveListener);
    lDrag.addMouseListener(mListener);
    lDrag.addMouseMoveListener(mMoveListener);

    Label l1 = new Label(splash, SWT.NONE);
    l1.setBackground(blues[0]);
    l1.setForeground(blues[4]);
    l1.setText("Name:");
    l1.addMouseListener(mListener);
    l1.addMouseMoveListener(mMoveListener);
    l1.pack();
    l1.setLocation(xSize, 0);
    xSize += l1.getSize().x + 3;

    int hSizeText = l1.getSize().y;
    hSize = hSizeText > hSizeImage ? hSizeText : hSizeImage;

    splashFile = new Label(splash, SWT.NONE);
    splashFile.setBackground(blues[0]);
    splashFile.setText("");
    splashFile.addMouseListener(mListener);
    splashFile.addMouseMoveListener(mMoveListener);
    splashFile.setSize(250, hSize);
    splashFile.setLocation(xSize, 0);
    xSize += 250 + 3;

    Label l2 = new Label(splash, SWT.NONE);
    l2.setBackground(blues[0]);
    l2.setForeground(blues[4]);
    l2.setText("C:");
    l2.addMouseListener(mListener);
    l2.addMouseMoveListener(mMoveListener);
    l2.pack();
    l2.setLocation(xSize, 0);
    xSize += l2.getSize().x + 3;

    splashPercent = new Label(splash, SWT.NONE);
    splashPercent.setBackground(blues[0]);
    splashPercent.setText("");
    splashPercent.addMouseListener(mListener);
    splashPercent.addMouseMoveListener(mMoveListener);
    splashPercent.setSize(45, hSize);
    splashPercent.setLocation(xSize, 0);
    xSize += 45 + 3;

    Label l3 = new Label(splash, SWT.NONE);
    l3.setBackground(blues[0]);
    l3.setForeground(blues[4]);
    l3.setText("D:");
    l3.addMouseListener(mListener);
    l3.addMouseMoveListener(mMoveListener);
    l3.pack();
    l3.setLocation(xSize, 0);
    xSize += l3.getSize().x + 3;

    splashDown = new Label(splash, SWT.NONE);
    splashDown.setBackground(blues[0]);
    splashDown.setText("");
    splashDown.addMouseListener(mListener);
    splashDown.addMouseMoveListener(mMoveListener);
    splashDown.setSize(65, hSize);
    splashDown.setLocation(xSize, 0);
    xSize += 65 + 3;

    Label l4 = new Label(splash, SWT.NONE);
    l4.setBackground(blues[0]);
    l4.setForeground(blues[4]);
    l4.setText("U:");
    l4.addMouseListener(mListener);
    l4.addMouseMoveListener(mMoveListener);
    l4.pack();
    l4.setLocation(xSize, 0);
    xSize += l4.getSize().x + 3;

    splashUp = new Label(splash, SWT.NONE);
    splashUp.setBackground(blues[0]);
    splashUp.setText("");
    splashUp.addMouseListener(mListener);
    splashUp.addMouseMoveListener(mMoveListener);
    splashUp.setSize(65, hSize);
    splashUp.setLocation(xSize, 0);
    xSize += 65 + 3;

    Label l5 = new Label(splash, SWT.NONE);
    l5.setImage(iminimize);
    l5.pack();
    l5.setLocation(xSize, 0);
    xSize += l5.getSize().x + 3;
    l5.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        splash.setMinimized(true);               
        mode = HIDDENMODE;
      }
    });

    Label l6 = new Label(splash, SWT.NONE);
    l6.setImage(imaximize);
    l6.pack();
    l6.setLocation(xSize, 0);
    xSize += l6.getSize().x + 3;
    l6.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        splash.setVisible(false);
        shell.setVisible(true);
        mode = FULLMODE;
        Logger.getLogger().setEnabled(bLogEnabled.getSelection());
        Logger.getLogger().log(
          0,
          0,
          Logger.ERROR,
          "!!! Console is restarted !!!");
        moving = false;
      }
    });
    splash.addListener(SWT.Deiconify, new Listener() {
      public void handleEvent(Event e) {
        mode = REDUCEDMODE;
        splash.setVisible(true);
        //splash.setMaximized(true);
        splash.setActive();
      }
    });
    splash.setSize(xSize + 3, hSize + 2);
    splash.setVisible(false);

    shell = new Shell(display);
    shell.setText(".: Azureus :.");
    shell.setImage(im);
    shell.setSize(750, 550);
    tabFolder = new TabFolder(shell, SWT.BORDER);
    TabItem itemGeneral = new TabItem(tabFolder, SWT.NULL);
    itemGeneral.setText("General Information");
    TabItem itemDetails = new TabItem(tabFolder, SWT.NULL);
    itemDetails.setText("Details");
    TabItem itemPieces = new TabItem(tabFolder, SWT.NULL);
    itemPieces.setText("Pieces");
    TabItem itemConsole = new TabItem(tabFolder, SWT.NULL);
    itemConsole.setText("Console");
    TabItem itemConsoleConfig = new TabItem(tabFolder, SWT.NULL);
    itemConsoleConfig.setText("Console Config");
    TabItem itemConfig = new TabItem(tabFolder, SWT.NULL);
    itemConfig.setText("Configuration");
    TabItem itemAbout = new TabItem(tabFolder, SWT.NULL);
    itemAbout.setText("About");
    Label lAbout = new Label(tabFolder, SWT.CENTER);
    lAbout.setImage(iAbout);
    itemAbout.setControl(lAbout);

    console =
      new StyledText(tabFolder, SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
    itemConsole.setControl(console);
    Logger.createLogger(this);

    compConfig = new Composite(tabFolder, SWT.NULL);
    itemConsoleConfig.setControl(compConfig);
    GridLayout consoleConfigLayout = new GridLayout();
    consoleConfigLayout.numColumns = 1;
    compConfig.setLayout(consoleConfigLayout);
    gGeneral = new Group(compConfig, SWT.SHADOW_OUT);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
    gGeneral.setLayoutData(gridData);
    gGeneral.setText("General");
    GridLayout layoutGeneral = new GridLayout();
    layoutGeneral.numColumns = 5;
    gGeneral.setLayout(layoutGeneral);

    bLogEnabled = new Button(gGeneral, SWT.CHECK);
    bLogEnabled.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        Logger.getLogger().setEnabled(bLogEnabled.getSelection());
      }
    });
    bLogEnabled.setSelection(true);
    new Label(gGeneral, SWT.NULL).setText(
      "Enable Console\t\tNumber of logged Lines :");
    final Combo maxLines = new Combo(gGeneral, SWT.SINGLE | SWT.READ_ONLY);
    for (int i = 256; i <= 4096; i += 256)
      maxLines.add(" " + i);
    maxLines.select(4);
    maxLines.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        Logger.getLogger().setMaxLines(
          (maxLines.getSelectionIndex() + 1) * 256);
      }
    });
    new Label(gGeneral, SWT.NULL).setText("\t\t");
    final Button clearLog = new Button(gGeneral, SWT.PUSH);
    clearLog.setText("Clear Console");
    clearLog.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        Logger.getLogger().clearLog();
      }
    });

    gComponents = new Group(compConfig, SWT.SHADOW_OUT);
    gridData = new GridData(GridData.FILL_BOTH);
    gComponents.setLayoutData(gridData);
    gComponents.setText("Components");
    GridLayout componentsLayout = new GridLayout();
    componentsLayout.numColumns = 1;
    gComponents.setLayout(componentsLayout);
    gridData = new GridData(GridData.FILL_BOTH);
    folderComponents = new TabFolder(gComponents, SWT.NULL);
    folderComponents.setLayoutData(gridData);
    TabItem fSystem = new TabItem(folderComponents, SWT.NULL);
    fSystem.setText("System");
    Composite cSystem = new Composite(folderComponents, SWT.NULL);
    GridLayout layoutComponents1 = new GridLayout();
    layoutComponents1.numColumns = 6;
    cSystem.setLayout(layoutComponents1);
    fSystem.setControl(cSystem);
    new ConsoleItem(cSystem, 0, 0, "Logger Errors");

    TabItem fDiskManager = new TabItem(folderComponents, SWT.NULL);
    fDiskManager.setText("Disk Manager");
    Composite cDiskManager = new Composite(folderComponents, SWT.NULL);
    GridLayout layoutComponents2 = new GridLayout();
    layoutComponents2.numColumns = 6;
    fDiskManager.setControl(cDiskManager);

    TabItem fPeerManager = new TabItem(folderComponents, SWT.NULL);
    fPeerManager.setText("Peer Manager");
    Composite cPeerManager = new Composite(folderComponents, SWT.NULL);
    GridLayout layoutComponents3 = new GridLayout();
    layoutComponents3.numColumns = 6;
    fPeerManager.setControl(cPeerManager);

    TabItem fPeerConnections = new TabItem(folderComponents, SWT.NULL);
    fPeerConnections.setText("Peer Connections");
    Composite cPeerConnections = new Composite(folderComponents, SWT.NULL);
    GridLayout layoutComponents4 = new GridLayout();
    layoutComponents4.numColumns = 6;
    cPeerConnections.setLayout(layoutComponents4);
    fPeerConnections.setControl(cPeerConnections);
    new ConsoleItem(
      cPeerConnections,
      PeerConnection.componentID,
      PeerConnection.evtLifeCycle,
      "Life Cycle");
    new ConsoleItem(
      cPeerConnections,
      PeerConnection.componentID,
      PeerConnection.evtProtocol,
      "BT Protocol");
    new ConsoleItem(
      cPeerConnections,
      PeerConnection.componentID,
      PeerConnection.evtErrors,
      "Errors");

    TabItem fTracker = new TabItem(folderComponents, SWT.NULL);
    fTracker.setText("Tracker");
    Composite cTracker = new Composite(folderComponents, SWT.NULL);
    GridLayout layoutComponents5 = new GridLayout();
    layoutComponents5.numColumns = 6;
    cTracker.setLayout(layoutComponents5);
    fTracker.setControl(cTracker);
    new ConsoleItem(
      cTracker,
      TrackerConnection.componentID,
      TrackerConnection.evtLifeCycle,
      "Life Cycle");
    new ConsoleItem(
      cTracker,
      TrackerConnection.componentID,
      TrackerConnection.evtFullTrace,
      "Full Trace");
    new ConsoleItem(
      cTracker,
      TrackerConnection.componentID,
      TrackerConnection.evtErrors,
      "Errors");

    TabItem fServer = new TabItem(folderComponents, SWT.NULL);
    fServer.setText("Server");
    Composite cServer = new Composite(folderComponents, SWT.NULL);
    GridLayout layoutComponents6 = new GridLayout();
    layoutComponents6.numColumns = 6;
    cServer.setLayout(layoutComponents6);
    new ConsoleItem(
      cServer,
      Server.componentID,
      Server.evtLyfeCycle,
      "Life Cycle");
    new ConsoleItem(
      cServer,
      Server.componentID,
      Server.evtNewConnection,
      "Incoming Connections");
    new ConsoleItem(cServer, Server.componentID, Server.evtErrors, "Errors");
    fServer.setControl(cServer);

    TabItem fBufferPool = new TabItem(folderComponents, SWT.NULL);
    fBufferPool.setText("Buffer Pool");
    Composite cBufferPool = new Composite(folderComponents, SWT.NULL);
    GridLayout layoutComponents7 = new GridLayout();
    layoutComponents7.numColumns = 6;
    cBufferPool.setLayout(layoutComponents7);
    new ConsoleItem(
      cBufferPool,
      ByteBufferPool.componentID,
      ByteBufferPool.evtAllocation,
      "Memory Allocations");
    fBufferPool.setControl(cBufferPool);

    folderComponents.pack();

    table = new Table(tabFolder, SWT.SINGLE | SWT.FULL_SELECTION);
    table.setLinesVisible(false);
    table.setHeaderVisible(true);
    String[] titles =
      {
        "Ip",
        "Port",
        "T",
        "I",
        "C",
        "Pieces",
        "%",
        "Down Speed",
        "Down",
        "I",
        "C",
        "Up Speed",
        "Up",
        "Stat Up",
        "S",
        "Av. Lvl.",
        "Opt. Unchoke" };
    int[] align =
      {
        SWT.LEFT,
        SWT.LEFT,
        SWT.LEFT,
        SWT.CENTER,
        SWT.CENTER,
        SWT.CENTER,
        SWT.RIGHT,
        SWT.RIGHT,
        SWT.RIGHT,
        SWT.CENTER,
        SWT.CENTER,
        SWT.RIGHT,
        SWT.RIGHT,
        SWT.RIGHT,
        SWT.LEFT,
        SWT.LEFT,
        SWT.LEFT };
    for (int i = 0; i < titles.length; i++) {
      TableColumn column = new TableColumn(table, align[i]);
      column.setText(titles[i]);
    }
    table.getColumn(0).setWidth(100);
    table.getColumn(1).setWidth(0);
    table.getColumn(2).setWidth(20);
    table.getColumn(3).setWidth(20);
    table.getColumn(4).setWidth(20);
    table.getColumn(5).setWidth(100);
    table.getColumn(6).setWidth(55);
    table.getColumn(7).setWidth(65);
    table.getColumn(8).setWidth(70);
    table.getColumn(9).setWidth(20);
    table.getColumn(10).setWidth(20);
    table.getColumn(11).setWidth(65);
    table.getColumn(12).setWidth(70);
    table.getColumn(13).setWidth(70);
    table.getColumn(14).setWidth(20);
    table.getColumn(15).setWidth(40);
    table.getColumn(16).setWidth(40);

    final Menu menu = new Menu(shell, SWT.POP_UP);
    final MenuItem item = new MenuItem(menu, SWT.CHECK);
    item.setText("Snubbed");

    menu.addListener(SWT.Show, new Listener() {
      public void handleEvent(Event e) {
        TableItem[] tis = table.getSelection();
        if (tis.length == 0) {
          item.setEnabled(false);
          return;
        }
        item.setEnabled(true);
        TableItem ti = tis[0];
        PeerTableItem pti = (PeerTableItem) PeerTableItem.tableItems.get(ti);
        if (pti != null)
          item.setSelection(pti.isSnubbed());

      }
    });

    item.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        TableItem[] tis = table.getSelection();
        if (tis.length == 0) {
          return;
        }
        TableItem ti = tis[0];
        PeerTableItem pti = (PeerTableItem) PeerTableItem.tableItems.get(ti);
        if (pti != null)
          pti.setSnubbed(item.getSelection());
      }
    });
    table.setMenu(menu);

    itemDetails.setControl(table);

    tablePieces = new Table(tabFolder, SWT.SINGLE | SWT.FULL_SELECTION);
    tablePieces.setLinesVisible(false);
    tablePieces.setHeaderVisible(true);
    String[] titlesPieces =
      { "#", "Size", "Nb Blocks", "Blocks", "Completed", "Availability" };
    int[] alignPieces =
      { SWT.LEFT, SWT.RIGHT, SWT.RIGHT, SWT.CENTER, SWT.RIGHT, SWT.RIGHT };
    for (int i = 0; i < titlesPieces.length; i++) {
      TableColumn column = new TableColumn(tablePieces, alignPieces[i]);
      column.setText(titlesPieces[i]);
    }
    tablePieces.getColumn(0).setWidth(50);
    tablePieces.getColumn(1).setWidth(60);
    tablePieces.getColumn(2).setWidth(65);
    tablePieces.getColumn(3).setWidth(300);
    tablePieces.getColumn(4).setWidth(80);
    tablePieces.getColumn(5).setWidth(80);
    itemPieces.setControl(tablePieces);

    tabFolder.setSelection(3);
    Composite genComposite = new Composite(tabFolder, SWT.NULL);
    itemGeneral.setControl(genComposite);
    GridLayout genLayout = new GridLayout();
    genLayout.numColumns = 1;
    genComposite.setLayout(genLayout);

    gFile = new Group(genComposite, SWT.SHADOW_OUT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gFile.setLayoutData(gridData);
    gFile.setText("Downloaded");
    GridLayout fileLayout = new GridLayout();
    fileLayout.numColumns = 3;
    gFile.setLayout(fileLayout);

    fileInfo = new Label(gFile, SWT.LEFT);
    fileInfo.setText("File Status");
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    fileInfo.setLayoutData(gridData);

    fileImage = new Canvas(gFile, SWT.NULL);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.widthHint = 150;
    gridData.heightHint = 30;
    fileImage.setLayoutData(gridData);

    filePercent = new Label(gFile, SWT.RIGHT);
    filePercent.setText("\t");
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
    filePercent.setLayoutData(gridData);

    piecesInfo = new Label(gFile, SWT.LEFT);
    piecesInfo.setText("Pieces Status");
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    piecesInfo.setLayoutData(gridData);

    piecesImage = new Canvas(gFile, SWT.NULL);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.widthHint = 150;
    gridData.heightHint = 30;
    piecesImage.setLayoutData(gridData);

    piecesPercent = new Label(gFile, SWT.RIGHT);
    piecesPercent.setText("\t");
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
    piecesPercent.setLayoutData(gridData);

    gAvailability = new Group(genComposite, SWT.SHADOW_OUT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gAvailability.setLayoutData(gridData);

    GridLayout availabilityLayout = new GridLayout();
    availabilityLayout.numColumns = 3;
    gAvailability.setText("Availability");
    gAvailability.setLayout(availabilityLayout);

    availabilityInfo = new Label(gAvailability, SWT.LEFT);
    availabilityInfo.setText("Pieces Status");
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
    availabilityInfo.setLayoutData(gridData);

    availabilityImage = new Canvas(gAvailability, SWT.NULL);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.widthHint = 150;
    gridData.heightHint = 30;
    availabilityImage.setLayoutData(gridData);

    availabilityPercent = new Label(gAvailability, SWT.RIGHT);
    availabilityPercent.setText("\t");
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
    availabilityPercent.setLayoutData(gridData);

    gTransfer = new Group(genComposite, SWT.SHADOW_OUT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gTransfer.setLayoutData(gridData);

    gTransfer.setText("Transfer");
    GridLayout layoutTransfer = new GridLayout();
    layoutTransfer.numColumns = 6;
    gTransfer.setLayout(layoutTransfer);

    new Label(gTransfer, SWT.LEFT).setText("Time Elapsed : ");
    timeElapsed = new Label(gTransfer, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    timeElapsed.setLayoutData(gridData);
    new Label(gTransfer, SWT.LEFT).setText("   Remaining: ");
    timeRemaining = new Label(gTransfer, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    timeRemaining.setLayoutData(gridData);
    new Label(gTransfer, SWT.LEFT);
    new Label(gTransfer, SWT.LEFT);

    new Label(gTransfer, SWT.LEFT).setText("Downloaded : ");
    download = new Label(gTransfer, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    download.setLayoutData(gridData);
    new Label(gTransfer, SWT.LEFT).setText("   Download Speed: ");
    downloadSpeed = new Label(gTransfer, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    downloadSpeed.setLayoutData(gridData);
    new Label(gTransfer, SWT.LEFT).setText("   Max Uploads : ");
    maxUploads = new Combo(gTransfer, SWT.SINGLE | SWT.READ_ONLY);
    for (int i = 2; i < 101; i++)
      maxUploads.add(" " + i);
    maxUploads.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        maxUploadsValue = 2 + maxUploads.getSelectionIndex();
      }
    });

    new Label(gTransfer, SWT.LEFT).setText("Uploaded : ");
    upload = new Label(gTransfer, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    upload.setLayoutData(gridData);
    new Label(gTransfer, SWT.LEFT).setText("   Upload Speed : ");
    uploadSpeed = new Label(gTransfer, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    uploadSpeed.setLayoutData(gridData);
    new Label(gTransfer, SWT.LEFT).setText("   Max Upload Speed : ");
    maxSpeed = new Combo(gTransfer, SWT.SINGLE | SWT.READ_ONLY);
    maxSpeed.add("Unlimited");
    for (int i = 1; i < upRates.length; i++)
      maxSpeed.add(" " + upRates[i] + "kB/s");
    maxSpeed.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        SpeedLimiter.getLimiter().setLimit(1024 * upRates[maxSpeed.getSelectionIndex()]);
      }
    });

    new Label(gTransfer, SWT.LEFT).setText("Seeds : ");
    seeds = new Label(gTransfer, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    seeds.setLayoutData(gridData);

    new Label(gTransfer, SWT.LEFT).setText("   Peers : ");
    peers = new Label(gTransfer, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    peers.setLayoutData(gridData);

    new Label(gTransfer, SWT.LEFT).setText("   Total Speed : ");
    totalSpeed = new Label(gTransfer, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    totalSpeed.setLayoutData(gridData);

    gInfo = new Group(genComposite, SWT.SHADOW_OUT);
    gInfo.setText("Info");
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    gInfo.setLayoutData(gridData);

    GridLayout layoutInfo = new GridLayout();
    layoutInfo.numColumns = 4;
    gInfo.setLayout(layoutInfo);

    new Label(gInfo, SWT.LEFT).setText("File Name : ");
    fileName = new Label(gInfo, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    fileName.setLayoutData(gridData);

    new Label(gInfo, SWT.LEFT).setText("   Total Size : ");
    fileSize = new Label(gInfo, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    fileSize.setLayoutData(gridData);

    new Label(gInfo, SWT.LEFT).setText("Save In : ");
    saveIn = new Label(gInfo, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    saveIn.setLayoutData(gridData);

    new Label(gInfo, SWT.LEFT).setText("   Hash : ");
    hash = new Label(gInfo, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    hash.setLayoutData(gridData);

    new Label(gInfo, SWT.LEFT).setText("# of Pieces : ");
    pieceNumber = new Label(gInfo, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    pieceNumber.setLayoutData(gridData);

    new Label(gInfo, SWT.LEFT).setText("   Size : ");
    pieceSize = new Label(gInfo, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    pieceSize.setLayoutData(gridData);

    new Label(gInfo, SWT.LEFT).setText("Tracker : ");
    tracker = new Label(gInfo, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    tracker.setLayoutData(gridData);

    new Label(gInfo, SWT.LEFT).setText("   Update in : ");
    trackerUpdateIn = new Label(gInfo, SWT.LEFT);
    gridData = new GridData(GridData.FILL_HORIZONTAL);
    trackerUpdateIn.setLayoutData(gridData);

    final Composite gConfig = new Composite(tabFolder, SWT.NULL);
    itemConfig.setControl(gConfig);
    GridLayout configLayout = new GridLayout();
    configLayout.numColumns = 2;
    gConfig.setLayout(configLayout);
    new Label(gConfig, SWT.NULL).setText("Program Id:");
    programId = new Label(gConfig, SWT.NULL);
    byte[] bProgramId = config.getByteParameter("Program Id", null);
    if (bProgramId != null) {
      //programId.setText(BtSwt.nicePrint(bProgramId));
    } else {
      programId.setText("Not allocated yet");
    }

    new Label(gConfig, SWT.NULL).setText("Override IP address:");
    overrideIp = new Text(gConfig, SWT.BORDER);
    overrideIp.setText(config.getStringParameter("Override Ip", ""));
    gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    gridData.widthHint = 100;
    overrideIp.setLayoutData(gridData);
    new Label(gConfig, SWT.NULL).setText("Use Fast Resume Mode:");
    final Button bUseResume = new Button(gConfig, SWT.CHECK);
    bUseResume.setSelection(config.getBooleanParameter("Use Resume", true));

    new Label(gConfig, SWT.NULL).setText("Allocate New Files:");
    final Button bAllocate = new Button(gConfig, SWT.CHECK);
    bAllocate.setSelection(config.getBooleanParameter("Allocate New", true));

    new Label(gConfig, SWT.NULL).setText("Disconnect Seeds when seed:");
    final Button bDisconnect = new Button(gConfig, SWT.CHECK);
    bDisconnect.setSelection(config.getBooleanParameter("Disconnect Seed", true));
    bDisconnect.addListener(SWT.Selection, new Listener() {
         public void handleEvent(Event e) {
           config.setParameter("Disconnect Seed", bDisconnect.getSelection());
         }
       });
        
    new Label(gConfig, SWT.NULL).setText("Maximum Number of Connections:");
    final Combo maxClients = new Combo(gConfig, SWT.SINGLE | SWT.READ_ONLY);
    maxClients.add("Unlimited");
    for (int i = 1; i < 101; i++)
      maxClients.add(" " + i);
    maxClients.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        config.setParameter("Max Clients", maxClients.getSelectionIndex());
      }
    });
    
    int selection = config.getIntParameter("Max Clients", 0);
    maxClients.select(selection);

    new Label(gConfig, SWT.NULL).setText("Default Max Uploads");
    final Combo dMaxUploads = new Combo(gConfig, SWT.SINGLE | SWT.READ_ONLY);
    for (int i = 2; i < 101; i++)
      dMaxUploads.add(" " + i);
    dMaxUploads.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        config.setParameter("Max Uploads", dMaxUploads.getSelectionIndex());
      }
    });
    selection = config.getIntParameter("Max Uploads", 2);
    dMaxUploads.select(selection);
    maxUploads.select(selection);
    maxUploadsValue = selection + 2;
        
    new Label(gConfig, SWT.NULL).setText("Default Max Upload Speed");
    final Combo dMaxUploadSpeed =
      new Combo(gConfig, SWT.SINGLE | SWT.READ_ONLY);
    dMaxUploadSpeed.add("Unlimited");
    for (int i = 1; i < upRates.length; i++)
      dMaxUploadSpeed.add(" " + upRates[i] + "kB/s");
    dMaxUploadSpeed.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        config.setParameter(
          "Max Upload Speed",
          dMaxUploadSpeed.getSelectionIndex());
      }
    });
    selection = config.getIntParameter("Max Upload Speed", 0);
    dMaxUploadSpeed.select(selection);
    maxSpeed.select(selection);
    SpeedLimiter.getLimiter().setLimit(1024 * upRates[selection]);
    
    Button enter = new Button(gConfig, SWT.PUSH);
    enter.setText("Save");
    gridData =
      new GridData(
        GridData.HORIZONTAL_ALIGN_END
          | GridData.FILL_BOTH
          | GridData.VERTICAL_ALIGN_END);
    gridData.horizontalSpan = 2;
    enter.setLayoutData(gridData);

    enter.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        config.setParameter("Override Ip", overrideIp.getText());
        config.setParameter("Use Resume", bUseResume.getSelection());
        config.setParameter("Allocate New", bAllocate.getSelection());
        config.setParameter("updated", 1);
        config.save();

        maxUploads.select(dMaxUploads.getSelectionIndex());
        maxSpeed.select(dMaxUploadSpeed.getSelectionIndex());
        maxUploadsValue = dMaxUploads.getSelectionIndex() + 2;
        SpeedLimiter.getLimiter().setLimit(1024 * upRates[maxSpeed.getSelectionIndex()]);
      }
    });

    shell.addListener(SWT.Resize, new Listener() {
      public void handleEvent(Event e) {
        Rectangle r = shell.getClientArea();
        tabFolder.setSize(r.width, r.height);
        validOverall = false;
        validAvailability = false;
        validPieces = false;
      }
    });

    shell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        if (fImage != null && !fImage.isDisposed())
          fImage.dispose();
        if (aImage != null && !aImage.isDisposed())
          aImage.dispose();
        if (pImage != null && !pImage.isDisposed())
          pImage.dispose();
        if (colorGrey != null && !colorGrey.isDisposed())
          colorGrey.dispose();
        if (blues[0] != null && !blues[0].isDisposed())
          blues[0].dispose();
        if (blues[1] != null && !blues[1].isDisposed())
          blues[1].dispose();
        if (blues[2] != null && !blues[2].isDisposed())
          blues[2].dispose();
        if (blues[3] != null && !blues[3].isDisposed())
          blues[3].dispose();
        if (blues[4] != null && !blues[4].isDisposed())
          blues[4].dispose();
      }
    });    
    shell.addListener(SWT.Iconify, new Listener() {
      public void handleEvent(Event e) {
        mode = REDUCEDMODE;
        Logger.getLogger().log(
          0,
          0,
          Logger.ERROR,
          "!!! Console is stoped due to iconification of main window !!!");
        Logger.getLogger().setEnabled(false);
        shell.setVisible(false);
        shell.setMinimized(false);
        splash.setVisible(true);
      }
    });
    
    tabFolder.addKeyListener(new KeyListener() {
      public void keyReleased(KeyEvent e)
      {
        //System.out.println(e.character + " " + e.stateMask);
        if(e.character == 'm')
         {
           mode = REDUCEDMODE;
                   Logger.getLogger().log(
                     0,
                     0,
                     Logger.ERROR,
                     "!!! Console is stoped due to iconification of main window !!!");
                   Logger.getLogger().setEnabled(false);
                   shell.setVisible(false);
                   shell.setMinimized(false);
                   splash.setVisible(true);
         }
      }
      public void keyPressed(KeyEvent e)
      {
        //System.out.println("Pressed : " + e.character + " " + e.stateMask);
      }
    });

    shell.open();
  }

  public boolean isDisposed() {
    return shell.isDisposed();
  }

  public static void main(String[] args) {
    Display display = new Display();
    MainSwt main = new MainSwt(display);
    while (!main.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();

  }

  public Table getTable() {
    return table;
  }

  public Table getTablePieces() {
    return tablePieces;
  }

  public Shell getShell() {
    return shell;
  }

  public synchronized void setAvailability(int[] pieces) {
    if (mode != FULLMODE)
      return;

    final int[] available = pieces;
    if (display.isDisposed())
      return;
    final boolean _valid = validAvailability;
    validAvailability = true;

    if (availabilityImage.isDisposed())
      return;
    Rectangle bounds = availabilityImage.getClientArea();
    int width = bounds.width - 5;
    int x0 = bounds.x + 1;
    int y0 = bounds.y + 1;
    int height = bounds.height - 2;
    GC gc = new GC(availabilityImage);
    if (!_valid) {
      if (aImage != null && !aImage.isDisposed())
        aImage.dispose();

      aImage = new Image(display, width, height);
      GC gcImage = new GC(aImage);
      int allMin = 0;
      int total = 0;
      String sTotal = "000";
      if (available != null) {
        allMin = available[0];
        int nbPieces = available.length;
        for (int i = 0; i < nbPieces; i++) {
          if (available[i] < allMin)
            allMin = available[i];
        }
        for (int i = 0; i < nbPieces; i++) {
          if (available[i] > allMin)
            total++;
        }
        total = (total * 1000) / nbPieces;
        sTotal = "" + total;
        if (total < 10)
          sTotal = "0" + sTotal;
        if (total < 100)
          sTotal = "0" + sTotal;

        for (int i = 0; i < width; i++) {
          int a0 = (i * nbPieces) / width;
          int a1 = ((i + 1) * nbPieces) / width;
          if (a1 == a0)
            a1++;
          if (a1 > nbPieces)
            a1 = nbPieces;
          int max = 0;
          int min = available[a0];
          int Pi = 1000;
          for (int j = a0; j < a1; j++) {
            if (available[j] > max)
              max = available[j];
            if (available[j] < min)
              min = available[j];
            Pi *= available[j];
            Pi /= (available[j] + 1);
          }
          int pond = Pi;
          if (max == 0)
            pond = 0;
          else {
            int PiM = 1000;
            for (int j = a0; j < a1; j++) {
              PiM *= (max + 1);
              PiM /= max;
            }
            pond *= PiM;
            pond /= 1000;
            pond *= (max - min);
            pond /= 1000;
            pond += min;
          }
          int index = 0;
          if (pond > 0)
            index = 1;
          if (pond > 2)
            index = 2;
          if (pond > 5)
            index = 3;
          if (pond > 10)
            index = 4;
          gcImage.setBackground(blues[index]);
          Rectangle rect = new Rectangle(i, 1, 1, height);
          gcImage.fillRectangle(rect);
        }
      }
      gcImage.dispose();
      if (!availabilityPercent.isDisposed())
        availabilityPercent.setText(allMin + "." + sTotal);
    }
    gc.setForeground(colorGrey);
    gc.drawImage(aImage, x0, y0);
    gc.drawRectangle(new Rectangle(x0, y0, width, height));
    gc.dispose();
  }

  public synchronized void setPiecesInfo(boolean[] pieces) {
    if (mode != FULLMODE)
      return;

    final boolean[] available = pieces;
    if (display.isDisposed())
      return;
    final boolean _valid = validPieces;
    validPieces = true;
    if (piecesImage.isDisposed())
      return;
    Rectangle bounds = piecesImage.getClientArea();
    int width = bounds.width - 5;
    int x0 = bounds.x + 1;
    int y0 = bounds.y + 1;
    int height = bounds.height - 2;
    GC gc = new GC(piecesImage);
    if (!_valid) {
      if (pImage != null && !pImage.isDisposed())
        pImage.dispose();
      pImage = new Image(display, width, height);
      GC gcImage = new GC(pImage);
      if (available != null) {
        int nbPieces = available.length;
        int total = 0;
        for (int i = 0; i < nbPieces; i++) {
          if (available[i])
            total++;
        }
        for (int i = 0; i < width; i++) {
          int a0 = (i * nbPieces) / width;
          int a1 = ((i + 1) * nbPieces) / width;
          if (a1 == a0)
            a1++;
          if (a1 > nbPieces)
            a1 = nbPieces;
          int nbAvailable = 0;
          for (int j = a0; j < a1; j++) {
            if (available[j]) {
              nbAvailable++;
            }
            int index = (nbAvailable * 4) / (a1 - a0);
            gcImage.setBackground(blues[index]);
            Rectangle rect = new Rectangle(i, 1, 1, height);
            gcImage.fillRectangle(rect);
          }
        }
        gcImage.dispose();
        total = (total * 1000) / nbPieces;
        if (!piecesPercent.isDisposed())
          piecesPercent.setText((total / 10) + "." + (total % 10) + " %");
      }
    }
    gc.setForeground(colorGrey);
    gc.drawImage(pImage, x0, y0);
    gc.drawRectangle(new Rectangle(x0, y0, width, height));
    gc.dispose();
  }

  public synchronized void setOverall(int perthousands) {
    if (display.isDisposed())
      return;

    final int total = perthousands;

    if (mode == REDUCEDMODE || mode == HIDDENMODE) {
      String percent = (total / 10) + "." + (total % 10) + " %";
      if (!splashPercent.isDisposed()) {
        splashPercent.setText(percent);
      }
      if (!splash.isDisposed() && !splashFile.isDisposed())
        splash.setText(percent + " completed on " + splashFile.getText());
      if (!lDrag.isDisposed() && !splashFile.isDisposed())
        lDrag.setToolTipText(percent + " completed on " + splashFile.getText());
      return;
    }

    final boolean _valid = validOverall;
    validOverall = true;
    if (fileImage.isDisposed())
      return;
    Rectangle bounds = fileImage.getClientArea();
    int width = bounds.width - 5;
    int x0 = bounds.x + 1;
    int y0 = bounds.y + 1;
    int height = bounds.height - 2;
    GC gc = new GC(fileImage);
    if (!_valid) {
      if (fImage != null && !fImage.isDisposed())
        fImage.dispose();
      fImage = new Image(display, width, height);
      GC gcImage = new GC(fImage);
      int limit = (width * total) / 1000;
      gcImage.setBackground(blues[4]);
      Rectangle rect = new Rectangle(1, 1, limit, height);
      gcImage.fillRectangle(rect);
      gcImage.setBackground(blues[0]);
      rect = new Rectangle(limit, 1, width, height);
      gcImage.fillRectangle(rect);
      gcImage.dispose();
      if (!filePercent.isDisposed())
        filePercent.setText((total / 10) + "." + (total % 10) + " %");
      if (!shell.isDisposed() && !splashFile.isDisposed())
        shell.setText(
          (total / 10)
            + "."
            + (total % 10)
            + "% completed on "
            + splashFile.getText());
    }
    gc.setForeground(colorGrey);
    gc.drawImage(fImage, x0, y0);
    gc.drawRectangle(new Rectangle(x0, y0, width, height));
    gc.dispose();
  }

  public void setTime(String elapsed, String remaining) {
    timeElapsed.setText(elapsed);
    timeRemaining.setText(remaining);
  }

  public void setStats(
    String _dl,
    String _ul,
    String _dls,
    String _uls,
    String _ts,
    int _s,
    int _p) {
    if (display.isDisposed())
      return;

    if (mode == HIDDENMODE)
      return;

    final String dls = _dls;
    final String uls = _uls;

    if (mode == REDUCEDMODE) {
      display.asyncExec(new Runnable() {
        public void run() {
          if (splashDown.isDisposed())
            return;
          splashDown.setText(dls);

          if (splashUp.isDisposed())
            return;
          splashUp.setText(uls);
        }
      });
      return;
    }

    final String dl = _dl;
    final String ul = _ul;
    final String ts = _ts;
    final int s = _s;
    final int p = _p;
    if (download.isDisposed())
      return;
    download.setText(dl);
    if (downloadSpeed.isDisposed())
      return;
    downloadSpeed.setText(dls);
    if (upload.isDisposed())
      return;
    upload.setText(ul);
    if (uploadSpeed.isDisposed())
      return;
    uploadSpeed.setText(uls);
    if (totalSpeed.isDisposed())
      return;
    totalSpeed.setText(ts);
    if (seeds.isDisposed())
      return;
    seeds.setText("" + s);
    if (peers.isDisposed())
      return;
    peers.setText("" + p);
    if (gTransfer.isDisposed())
      return;
  }

  public void setTracker(String _status, int _time) {
    if (mode != FULLMODE)
      return;

    if (display.isDisposed())
      return;
    final String status = _status;
    final int time = _time;
    if (tracker.isDisposed())
      return;
    tracker.setText(status);
    if (trackerUpdateIn.isDisposed())
      return;
    int minutes = time / 60;
    int seconds = time % 60;
    String strSeconds = "" + seconds;
    if (seconds < 10)
      strSeconds = "0" + seconds;
    trackerUpdateIn.setText(minutes + ":" + strSeconds);
  }

  public void setInfos(
    String ifileName,
    String ifileSize,
    String ipath,
    String ihash,
    int ipieceNumber,
    String ipieceLength) {
    if (display.isDisposed())
      return;
    final String _fileName = ifileName;
    final String _fileSize = ifileSize;
    final String _path = ipath;
    final String _hash = ihash;
    final int _pieceNumber = ipieceNumber;
    final String _pieceLength = ipieceLength;
    display.asyncExec(new Runnable() {
      public void run() {
        if (fileName.isDisposed())
          return;
        fileName.setText(_fileName);
        if (splashFile.isDisposed())
          return;
        splashFile.setText(_fileName);
        if (fileSize.isDisposed())
          return;
        fileSize.setText(_fileSize);
        if (saveIn.isDisposed())
          return;
        saveIn.setText(_path);
        if (hash.isDisposed())
          return;
        hash.setText(_hash);
        if (pieceNumber.isDisposed())
          return;
        pieceNumber.setText("" + _pieceNumber);
        if (pieceSize.isDisposed())
          return;
        pieceSize.setText(_pieceLength);
        if (gInfo.isDisposed())
          return;
      }
    });
  }

  public int getMaxUploads() {
    return maxUploadsValue;
  }

  public StyledText getConsole() {
    return console;
  }

  public void setTab(int tabNumber) {
    final int _tabNumber = tabNumber;
    if (display.isDisposed())
      return;
    display.asyncExec(new Runnable() {
      public void run() {
        if (tabFolder.isDisposed())
          return;
        tabFolder.setSelection(_tabNumber);
      }
    });
  }

  public void invalidatePieces() {
    validOverall = false;
    validPieces = false;
  }

  public void invalidateAvailability() {
    validAvailability = false;
  }

  public int getMode() {
    return mode;
  }

  public static MainSwt getMain() {
    return main;
  }

  public Display getdisplay() {
    return display;
  }

  public void dispose() {
    display.asyncExec(new Runnable() {
      public void run() {
        splash.dispose();
        shell.dispose();
      }
    });
  }

}
