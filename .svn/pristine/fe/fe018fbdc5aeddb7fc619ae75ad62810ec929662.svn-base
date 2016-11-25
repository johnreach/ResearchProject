package org.gudy.azureus;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

/**
 * 
 * Startup Class.
 * 
 * @author Olivier
 *
 */
public class Main {
  final static Display display;
  final static MainSwt main;
  final static Logger logger;

  static {
    display = new Display();
    main = new MainSwt(display);
    logger = Logger.getLogger();
  }

  public static void main(String[] args) {

    logger.log(
      0,
      0,
      Logger.INFORMATION,
      ".: Azureus :. Java Bittorrent Client is starting up.");
    boolean bContinue = true;
    final ConfigurationManager config = ConfigurationManager.getInstance();
    byte[] programId = config.getByteParameter("Program Id", null);
    if (programId == null) {
      main.setTab(5);
      programId = new byte[20];
      for (int i = 0; i < 20; i++) {
        programId[i] = (byte) (Math.random() * 254);
      }
      config.setParameter("Program Id", programId);
      config.setParameter("updated", 0);
      int style = SWT.APPLICATION_MODAL | SWT.OK;
      MessageBox messageBox = new MessageBox (main.getShell(), style);
      messageBox.setText ("Information");
      messageBox.setMessage ("As this is the first time you use Azureus,\nyou are invited to setup your default configuration.\n\nPlease hit 'save' when done in order to start your download.");
      messageBox.open();
      
      waitForConfig();
    }

    if (args.length < 1) {
      //logger.log(0, 0, Logger.ERROR, "Torrent File is not provided");
      //waitForDispose();
    	
    	/**
    	 * amc: Let's be more friendly, and get the user to choose a file. 
    	 */
    	FileDialog torrent_dialog = new FileDialog(main.getShell(), SWT.OPEN);
    	torrent_dialog.setFilterExtensions(new String[]{"*.torrent"});
    	String filename = torrent_dialog.open();
    	
    	// We'll hack args to simulate a torrent file was passed as an
    	// argument.
    	if (filename == null) {waitForDispose(); return;}
    	else {args = new String[]{filename};}
    }

    //TODO:: MAKE THIS WORK CORRECTLY WITH BDECODER
    TrackerConnection btCon = null;
    Server server = null;
    DiskManager diskManager = null;
    SHA1Hasher s = null;
    byte[] hashValue = null;
    Map metaData = null;
    String path = null;
    try {
      logger.log(0, 0, Logger.INFORMATION, "Opening torrent file : " + args[0]);
      
      //Buffers
	  StringBuffer metainfo = new StringBuffer();
	  String line;
	  byte[] buf = new byte[1024];
	  int nbRead;
      
      //Opening torrent file   
      if(args[0].startsWith("http://") || args[0].startsWith("ftp://")) {
		InputStream is = new URL(args[0]).openStream();
		while ((nbRead = is.read(buf)) > 0)
			metainfo.append(new String(buf, 0, nbRead, "ISO-8859-1"));
      }
      else {
      	FileInputStream fis = new FileInputStream(args[0]);
		while ((nbRead = fis.read(buf)) > 0)
			metainfo.append(new String(buf, 0, nbRead, "ISO-8859-1"));
      }
      
      logger.log(0, 0, Logger.INFORMATION, "Parsing torrent file.");
      //Parsing torrent file
      metaData = BDecoder.decode(metainfo.toString().getBytes("ISO-8859-1"));
      //append the torrent name to the metadata
      metaData.put("torrent filename", args[0].getBytes("ISO-8859-1"));

      logger.log(0, 0, Logger.INFORMATION, "Computing Hash.");
      //Computing Info Hash
      //String metaInfo = metainfo.toString();
      //int start = metaInfo.indexOf("4:info") + 6;
      //int end = metaInfo.length() - 1;
      //String infoPart = metaInfo.substring(start,end); 	  	  
      s = new SHA1Hasher();
      hashValue = s.calculateHash(BEncoder.encode((Map) metaData.get("info")));
    } catch (Exception e) {
      logger.log(0, 0, Logger.ERROR, "Error : " + e);
      waitForDispose();
      return;
    }

    //  Ask user to choose a directory for saving the file
    DirectoryDialog dDialog = new DirectoryDialog(main.getShell());
    dDialog.setText("Please choose a saving directory");
    dDialog.setFilterPath(config.getStringParameter("Save path",null));
    path = dDialog.open();
    if (path == null) {
      logger.log(0, 0, Logger.ERROR, "Canceled By User");
      waitForDispose();
      return;
    }
    config.setParameter("Save path",path);
    config.save();
    logger.log(0, 0, Logger.INFORMATION, "Saving to : " + path);

    main.setTab(0);
    final Map _metaData = metaData;
    final String _path = path;
    final byte[] tempHashValue = hashValue;

    //  Starting Server ...           
    server = new Server();

    int port = server.getPort();
    if (port == 0)
      bContinue = false;

    btCon = new TrackerConnection(metaData, hashValue, port);

    final Server _server = server;
    final TrackerConnection _btCon = btCon;
    final PeerManager manager =
      new PeerManager(tempHashValue, _server, _btCon, diskManager, main);

    Thread toto = new Thread() {
      public void run() {
        try {
          final DiskManager diskManager =
            new DiskManager(_metaData, _path, manager);
          while (diskManager.getState() == DiskManager.INITIALIZING) {
            Thread.sleep(50);
          }
          main.setInfos(
            diskManager.getFileName(),
            PeerStats.format(diskManager.getTotalLength()),
            _path,
            nicePrint(tempHashValue),
            diskManager.getPiecesNumber(),
            PeerStats.format(diskManager.getPieceLength()));
          while (diskManager.getState() != DiskManager.READY) {
            Thread.sleep(200);
            main.invalidatePieces();
            if (!display.isDisposed()) {
              display.asyncExec(new Runnable() {
                public void run() {
                  main.setOverall(diskManager.getPercentDone());
                  main.setPiecesInfo(diskManager.getPiecesStatus());
                }
              });
            }

          }
          main.invalidatePieces();
          manager.setDiskManager(diskManager);
          manager.start();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    toto.start();

    waitForDispose();
  }

  public static String nicePrint(byte[] data) {
    String out = "";
    for (int i = 0; i < data.length; i++) {
      out = out + nicePrint(data[i]);
      if (i % 4 == 3)
        out = out + " ";
    }
    return out;
  }

  public static String nicePrint(byte b) {
    byte b1 = (byte) ((b >> 4) & 0x0000000F);
    byte b2 = (byte) (b & 0x0000000F);
    return nicePrint2(b1) + nicePrint2(b2);
  }

  public static String nicePrint2(byte b) {
    String out = "";
    switch (b) {
      case 0 :
        out = "0";
        break;
      case 1 :
        out = "1";
        break;
      case 2 :
        out = "2";
        break;
      case 3 :
        out = "3";
        break;
      case 4 :
        out = "4";
        break;
      case 5 :
        out = "5";
        break;
      case 6 :
        out = "6";
        break;
      case 7 :
        out = "7";
        break;
      case 8 :
        out = "8";
        break;
      case 9 :
        out = "9";
        break;
      case 10 :
        out = "A";
        break;
      case 11 :
        out = "B";
        break;
      case 12 :
        out = "C";
        break;
      case 13 :
        out = "D";
        break;
      case 14 :
        out = "E";
        break;
      case 15 :
        out = "F";
        break;
    }
    return out;
  }

  private static void waitForDispose() {
    while (!main.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
    System.exit(0);
  }

  private static void waitForConfig() {
    while (!main.isDisposed()
      && ConfigurationManager.getInstance().getIntParameter("updated", 0) == 0) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    if (main.isDisposed()) {
      display.dispose();
      System.exit(0);
    }
  }
}
