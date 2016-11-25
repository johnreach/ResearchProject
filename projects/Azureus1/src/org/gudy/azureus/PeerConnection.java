package org.gudy.azureus;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Vector;

import org.eclipse.swt.widgets.Table;

/**
 * A connection with a Peer.
 * 
 * @author Olivier
 *
 */
public class PeerConnection extends Thread
{
  private final static String PROTOCOL = "BitTorrent protocol";
  private final static byte BT_CHOKED       = 0;
  private final static byte BT_UNCHOKED     = 1;
  private final static byte BT_INTERESTED   = 2;
  private final static byte BT_UNINTERESTED = 3;
  private final static byte BT_HAVE         = 4;
  private final static byte BT_BITFIELD     = 5;
  private final static byte BT_REQUEST      = 6;
  private final static byte BT_PIECE        = 7;
  private final static byte BT_CANCEL       = 8;
  
  public final static int CONNECTING  = 10;
  public final static int HANDSHAKING = 20;
  public final static int TRANSFERING = 30;
  public final static int DISCONNECTED= 40;
  public final static int BLOCKED     = 100;
  
  private PeerManager manager;
  
  private byte[] peerId;
  private String peerIp;
  private int peerPort;
  
  //The SocketChannel associated with this peer
  private SocketChannel sck;
  
  //The reference to the current ByteBuffer used...
  private ByteBuffer currentBuffer;
    
  //Statistic Class...
  private PeerStats stats;
  //Sending Class...
  private WriteThread writeThread;
  //Display elements
  private PeerTableItem peerItem;
  private Table table;
  
  private boolean choked;
  private boolean interested;
  private Vector requested;  
    
  private boolean choking;
  private boolean interesting;
  private Vector requesting;  
  
  private boolean snubbed;
  
  private boolean[] available;
  
  private boolean isIncoming;
  
  private boolean isSeed;
  
  //The loop-continue boolean
  private boolean bContinue;
  
  //The current state
  private int state;
  
  //an integer to indicate that this connection is being closed
  private int closing;
 
  
  
  
  //The Logger
  private Logger logger;
  public final static int componentID  = 1;
  public final static int evtProtocol  = 0; // Protocol Info
  public final static int evtLifeCycle = 1; // PeerConnection Life Cycle
  public final static int evtErrors    = 2; // PeerConnection Life Cycle
  
  /*
   * This object constructors will let the PeerConnection partially created,
   * but hopefully will let us gain some memory for peers not going to be
   * accepted.
   */
  
  /**
   * The Default Contructor for outgoing connections.
   * @param manager the manager that will handle this PeerConnection
   * @param table the graphical table in which this PeerConnection should display its info
   * @param peerId the other's peerId which will be checked during Handshaking
   * @param ip the peer Ip Address
   * @param port the peer port
   */
  public PeerConnection(PeerManager manager,Table table,byte[] peerId,String ip,int port)
  {    
    this.state = CONNECTING;
    this.manager = manager;    
    this.peerId = peerId;
    this.peerIp = ip;
    this.peerPort = port;
    this.isIncoming = false;
    this.table = table;
    this.closing = 0;
    this.logger = Logger.getLogger();
    logger.log(componentID,evtLifeCycle,Logger.INFORMATION,"Creating outgoing connection to " + ip + " : " + port);
  }
  
  /**
   * The default Contructor for incoming conenctions
   * @param manager the manager that will handle this PeerConnection
   * @param table the graphical table in which this PeerConnection should display its info  
   * @param sck the SocketChannel that handles the connection 
   */
  public PeerConnection(PeerManager manager,Table table,SocketChannel sck)
  {    
    this.state = CONNECTING;
    this.manager = manager;    
    this.peerId = null;
    this.sck = sck;
    this.peerIp = sck.socket().getInetAddress().getHostAddress();
    this.peerPort = sck.socket().getPort();
    this.isIncoming = true;
    this.table = table;
    this.logger = Logger.getLogger();
    logger.log(componentID,evtLifeCycle,Logger.INFORMATION,"Creating incoming connection from " + peerIp + " : " + peerPort);
  }

  /**
   * Private method that will finish fields allocation, once the handshaking is ok.
   * Hopefully, that will save some RAM.
   */
  private void allocateAll()
  {    
    isSeed = false;
    
    choked = true;
    interested = false;
    requested = new Vector();
    
    choking = true;
    interesting = false;
    requesting = new Vector();
    
    snubbed = false;
    
    bContinue = true;    
    
    available = new boolean[manager.getPiecesNumber()];
    Arrays.fill(available,false);        
        
    this.peerItem = new PeerTableItem(table,this);    
    this.state = TRANSFERING;             
  }
  
  
  //The run loop
  public void run()
  {
    this.bContinue = true;
    
    logger.log(componentID,evtLifeCycle,Logger.INFORMATION,"Initiating connection with " + peerIp + " : " + peerPort);
    //1. Open Connection
    initConnection();
    if(!this.bContinue) { closeAll(); return; }
      
    logger.log(componentID,evtLifeCycle,Logger.INFORMATION,"Handshaking with " + peerIp + " : " + peerPort);
    //2. Handshake
    handshake();
    if(!this.bContinue) { closeAll(); return; }
        
    //Handshake is ok, we can continue with allocation
    allocateAll();
    
    //3. Send bit field
    sendBitField();
    if(!this.bContinue) { closeAll(); return; }
    
    logger.log(componentID,evtLifeCycle,Logger.INFORMATION,"Transfering Data with " + peerIp + " : " + peerPort);  
    //4. Transfer
    while(this.bContinue)
      handleProtocol();
    
    //5. Close Everything  
    closeAll();
  } 
 
  //Initialisation
  private void initConnection()
  {
    stats = new PeerStats(manager.getPieceLength());
    try {
      
      //If we have a outgoing connection, then we must create the SocketChannel        
      if(!isIncoming)
      { 
        //Construct the peer's address with ip and port     
        InetSocketAddress peerAddress = new InetSocketAddress(peerIp,peerPort);
        //Create a new SocketChannel, leaved non-connected
        sck = SocketChannel.open();
        //Configure it so it's non blocking
        sck.configureBlocking(false);
        //Initiate the connection
        sck.connect(peerAddress);
        //While it's has not yet finished connecting        
        while(! sck.finishConnect())
        {
            //Wait some time
            Thread.sleep(20);
        }
      }
      //If it was an incoming connection, the Server has changed its mode to non blocking    
      writeThread = new WriteThread(this);
      writeThread.start();      
    } catch (Exception e) {
        logger.log(componentID,evtErrors,Logger.ERROR,"Error in PeerConnection::initConnection (" + peerIp + " : " + peerPort + " ) : " + e);  
      this.bContinue = false;
    }
  }
    
  //Protocol high level functions
  private void handshake()
  {
    try {
      byte[] protocol = PROTOCOL.getBytes();      
      byte[] reserved = new byte[] {0,0,0,0,0,0,0,0};
      byte[] hash = manager.getHash();
      byte[] myPeerId = manager.getPeerId();          
            
      ByteBuffer bufferHandshakeS = ByteBuffer.allocate(68);     
      bufferHandshakeS.put((byte)PROTOCOL.length()).put(protocol).put(reserved).put(hash).put(myPeerId);
      
      bufferHandshakeS.position(0);
      bufferHandshakeS.limit(68);
      write(bufferHandshakeS);
            
      currentBuffer = ByteBufferPool.getInstance().getFreeBuffer();
      currentBuffer.limit(68);
      currentBuffer.position(0);
      read(currentBuffer);
                    
      //Now test for data...
      
      if(currentBuffer.get() != (byte) PROTOCOL.length()) {
        this.bContinue = false;
      }
         
      currentBuffer.get(protocol);
      if( ! (new String(protocol)).equals(PROTOCOL)) {
        this.bContinue = false;
      }
      
      currentBuffer.get(reserved);                         
      for(int i = 0 ; i < 8 ; i++)
      {
        //if(reserved[i] != 0) {bContinue = false; System.out.println("fail3");}        
      }
      
      byte[] otherHash = new byte[20];
      currentBuffer.get(otherHash);
      for(int i = 0 ; i < 20 ; i++)
      {
        if(otherHash[i] != hash[i]) {bContinue = false;}  
      }
      
      byte[] otherPeerId = new byte[20];
      currentBuffer.get(otherPeerId);
                  
      if(bContinue && isIncoming) {        
        //HandShaking is ok so far
        //We test if the handshaking is valid (no other connections with that peer)
        if(manager.validateHandshaking(this,otherPeerId))
        {        
          //Store the peerId        
          this.peerId = otherPeerId;
        }
        else {bContinue = false;}                  
      }
      
      if(bContinue && !isIncoming) {          
        boolean same = true;
        for(int j = 0 ; j < this.peerId.length ; j++)
          same = same && (this.peerId[j] == otherPeerId[j]);
      }                   
    } catch (Exception e) {
    	e.printStackTrace();
      // Handshake failed for some reason ...
      logger.log(componentID,evtErrors,Logger.ERROR,"Error in PeerConnection::handshake (" + peerIp + " : " + peerPort + " ) : " + e); 
      this.bContinue = false;
    }
  }
  
  private void handleProtocol()
  {
    try{
      //Rewind current buffer
      currentBuffer.position(0);
      currentBuffer.limit(4);
      read(currentBuffer);
      int length = currentBuffer.getInt();
      //Keep Alive message must be filtered
      //System.out.println("r:" + length);
      if(length > 0) {
        currentBuffer.limit(4+length);
        read(currentBuffer);
        analyseBuffer(currentBuffer);
      }
    } catch(Exception e) {
        //e.printStackTrace();
        logger.log(componentID,evtErrors,Logger.ERROR,"Error in PeerConnection::handleProtocol (" + peerIp + " : " + peerPort + " ) : " + e);
      bContinue = false;
    }
  }
  
  private void analyseBuffer(ByteBuffer buffer)
  {
    int pieceNumber,pieceOffset,pieceLength;
    byte cmd = buffer.get();
    switch(cmd) {
      case BT_CHOKED:
        logger.log(componentID,evtProtocol,Logger.RECEIVED,peerIp + " is choking you");
        choked = true;
        cancelRequests();
        break;
      case BT_UNCHOKED:
        logger.log(componentID,evtProtocol,Logger.RECEIVED,peerIp + " is unchoking you");            
        choked = false;
        break;
      case BT_INTERESTED:
        logger.log(componentID,evtProtocol,Logger.RECEIVED,peerIp + " is interested");
        interesting = true;
        break;
      case BT_UNINTERESTED:
        logger.log(componentID,evtProtocol,Logger.RECEIVED,peerIp + " is not interested");
        interesting = false;
        break;
      case BT_HAVE:        
        pieceNumber = buffer.getInt();
        logger.log(componentID,evtProtocol,Logger.RECEIVED,peerIp + " has " + pieceNumber);
        have(pieceNumber);
        break;          
      case BT_BITFIELD:
        logger.log(componentID,evtProtocol,Logger.RECEIVED,peerIp + " has sent BitField");
        setBitField(buffer);            
        checkInterested();
        checkSeed();
        invalidateImage();
        break;
      case BT_REQUEST:        
        pieceNumber = buffer.getInt();
        pieceOffset = buffer.getInt();
        pieceLength = buffer.getInt();
        logger.log(componentID,evtProtocol,Logger.RECEIVED,peerIp + " has requested #" + pieceNumber + ":" + pieceOffset + "->" + (pieceOffset+pieceLength));
        respondTo(pieceNumber,pieceOffset,pieceLength);
        break;
      case BT_PIECE:        
        pieceNumber = buffer.getInt();
        pieceOffset = buffer.getInt();
        pieceLength = buffer.limit()-buffer.position();
        logger.log(componentID,evtProtocol,Logger.RECEIVED,peerIp + " has sent #" + pieceNumber + ":" + pieceOffset + "->" + (pieceOffset+pieceLength));
        Request request = new Request(pieceNumber,pieceOffset,pieceLength);
        if(requested.contains(request) && manager.checkBlock(pieceNumber,pieceOffset,buffer))
        {
            requested.remove(request);
            setUnSnubbed();
            reSetRequestsTime();
            manager.writeBlock(pieceNumber,pieceOffset,buffer);
            currentBuffer = ByteBufferPool.getInstance().getFreeBuffer();
        }
        else
        {
          logger.log(componentID,evtErrors,Logger.ERROR,peerIp + " has sent #" + pieceNumber + ":" + pieceOffset + "->" + (pieceOffset+pieceLength) + " but piece was discarded (either not requested or invalid)");
        }
        break;
      case BT_CANCEL:
        pieceNumber = buffer.getInt();
        pieceOffset = buffer.getInt();
        pieceLength = buffer.getInt();
        logger.log(componentID,evtProtocol,Logger.RECEIVED,peerIp + " has canceled #" + pieceNumber + ":" + pieceOffset + "->" + (pieceOffset+pieceLength));
        break;
     }
  }

  private void have(int pieceNumber) {
    available[pieceNumber] = true;
    stats.haveNewPiece();
    manager.haveNewPiece();
    manager.havePiece(pieceNumber,this);
    if(!interested) checkInterested(pieceNumber);
    checkSeed();
    invalidateImage();
  }   
  
  private void read(ByteBuffer buffer) throws Exception
  {
    buffer.mark();
    int size = buffer.limit() - buffer.position();
    int deltaRead = 0;
    while(buffer.hasRemaining())
    {     
      deltaRead = sck.read(buffer);
      stats.received(deltaRead);
      manager.received(deltaRead);
      if(deltaRead < 0) throw new Exception("End of Stream Reached");
      if(buffer.hasRemaining()) Thread.sleep(5);
    }
    buffer.reset();
  }
  
  private void write(ByteBuffer buffer)
  {
    if(writeThread != null)    
      writeThread.send(new SendPacket(SendPacket.INFORMATION,buffer));
  }
  
  
  private void sendSimpleCommand(byte command)
  {
    ByteBuffer buffer = ByteBuffer.allocate(5);
    buffer.putInt(1);
    buffer.put(command);
    buffer.position(0);
    buffer.limit(5);
    write(buffer);
  }
 
  private void setBitField(ByteBuffer buffer)
  {
    byte[] data = new byte[(manager.getPiecesNumber()+7)/8];
    buffer.get(data);
    for(int i=0 ; i <available.length;i++)
    {
      int index = i / 8;
      int bit = 7 - (i % 8);
      byte bData = data[index];      
      byte b = (byte) (bData >> bit);
      if((b & 0x01) == 1)
      {
        available[i] = true;
      }
      else
      {
        available[i] = false;
      }
    }
  }
  
  
  /**
   * Global checkInterested method.
   * Scans the whole pieces to determine if it's interested or not
   */
  private void checkInterested()
  {    
    boolean newInterested = false;
    boolean[] myStatus = manager.getPiecesStatus();
    for(int i = 0 ; i < myStatus.length ; i++)
    {
      if(!myStatus[i] && available[i])
      {
        newInterested = true;        
      }
    }
        
    if(newInterested && !interested)
    { 
      logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is interesting");   
      sendSimpleCommand(BT_INTERESTED);
    }
    else if(!newInterested && interested)
    {
      logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is not interesting");
      sendSimpleCommand(BT_UNINTERESTED);
    }    
    interested = newInterested;
  }
  
  /**
   * Checks interested given a new piece received
   * @param pieceNumber the piece number that has been received
   */
  private void checkInterested(int pieceNumber)
  {
    boolean[] myStatus = manager.getPiecesStatus();
    boolean newInterested = !myStatus[pieceNumber];
    if(newInterested && !interested)
    { 
      logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is interesting");    
      sendSimpleCommand(BT_INTERESTED);
    }
    else if(!newInterested && interested)
    {
        logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is not interesting");
      sendSimpleCommand(BT_UNINTERESTED);
    }
    interested = newInterested;
  }  

  /**
   * Private method to send the bitfield.
   * The bitfield will only be sent if there is at least one piece available.
   *
   */
  private void sendBitField()
  {
    ByteBuffer buffer = ByteBuffer.allocate(5 + (manager.getPiecesNumber()+7)/8);
    buffer.putInt(buffer.capacity()-4);
    buffer.put(BT_BITFIELD);
    boolean atLeastOne = false;
    boolean[] myStatus = manager.getPiecesStatus();
    int bToSend = 0;
    int i=0;
    for(; i < myStatus.length ; i++)
    {
      if((i%8) == 0) bToSend = 0;
      bToSend = bToSend << 1;
      if(myStatus[i]) {bToSend += 1; atLeastOne = true;}
      if((i%8) == 7) buffer.put((byte)bToSend);
    }
    if((i%8) != 0)
    {
      bToSend = bToSend << (8 - (i%8));
      buffer.put((byte)bToSend);
    }
    
    buffer.position(0);
    if(atLeastOne)
    {
      buffer.limit(buffer.capacity());
      write(buffer);
      logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is sent your bitfield");
    } 
  }    
  
  /**
   * private method to invalidate the image displayed
   * invalidating reasons are : new piece received, bitfield received
   */
  private void invalidateImage()
  {
      if(peerItem != null)
        peerItem.invalidate();
  }
  
  /**
   * Checks if it's a seed or not.
   */  
  private void checkSeed()
  {     
    for(int i = 0 ; i < available.length ; i++)
    {
      if(!available[i]) return;
    }    
    isSeed = true;
  }
  
  /**
   * Private method to handle a request...
   * @param pieceNumber
   * @param offset
   * @param length
   */
  private void respondTo(int pieceNumber,int offset,int length)
  {   
    if(manager.checkBlock(pieceNumber,offset,length))
    {
      logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is being sent #" + pieceNumber + ":" + offset + "->" + length);
      //byte[] data = manager.getDataBloc(pieceNumber,offset,length);
      
      ByteBuffer buffer = manager.getBlock(pieceNumber,offset,length);
      if(writeThread != null)    
            writeThread.send(new SendPacket(SendPacket.DATA,buffer));
    }
    else
    {
      logger.log(componentID,evtErrors,Logger.ERROR,peerIp + " has requested #" + pieceNumber + ":" + offset + "->" + length + " which is an invalid request.");
      closeAll();
    }
  }
  
  private void reSetRequestsTime()
  {
      for(int i = 0 ; i < requested.size() ; i++)
      {
          Request request = null;
          try { request = (Request) requested.get(i); } catch(Exception e) {}
          if(request != null)
            request.reSetTime();
      }
  }
  
  
  
  // Public Methods, ie used by the manager.
  
    /**
     * The way to close everything :)
     *
     */
    public synchronized void closeAll()
    {
        bContinue = false;
        this.state = BLOCKED;
        
        if(closing == 1)
            return;
        closing = 1;
      
        //1. Remove us from the manager, so it will stop asking us for things :p
        manager.removePeer(this);
      
        //2. Cancel any pending requests (on the manager side)
        cancelRequests();
      
        //2. Stop the WriteThread
        if(writeThread != null)
          writeThread.stopThread();
        writeThread = null;
      
        //3. Close the socket
        try {
            if(sck!=null && sck.isConnected()) sck.close(); 
        } catch(Exception e) {
            logger.log(componentID,evtErrors,Logger.ERROR,"Error in PeerConnection::closeAll-sck.close() (" + peerIp + " : " + peerPort + " ) : " + e);
        }
        
        //4. release current Buffer
        if(currentBuffer != null)
            ByteBufferPool.getInstance().freeBuffer(currentBuffer);
      
        //5. finally Remove the UI.      
        if(peerItem != null)
          peerItem.remove();
        peerItem = null;
        
        //6. Send a logger event
        logger.log(componentID,evtLifeCycle,Logger.INFORMATION,"Connection Ended with " + peerIp + " : " + peerPort );
    }   
  
  public void request(int pieceNumber,int pieceOffset,int pieceLength)
  {
    if(state != TRANSFERING)
        return;
    logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is asked for #" + pieceNumber + ":" + pieceOffset + "->" + (pieceOffset + pieceLength));
    requested.add(new Request(pieceNumber,pieceOffset,pieceLength));
    ByteBuffer buffer = ByteBuffer.allocate(17);
    buffer.putInt(13);
    buffer.put(BT_REQUEST);
    buffer.putInt(pieceNumber);
    buffer.putInt(pieceOffset);
    buffer.putInt(pieceLength);
    buffer.position(0);
    buffer.limit(17);
    write(buffer);          
  }
  
  public void sendCancel(Request request)
  {
    if(state != TRANSFERING)
      return;
    logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is canceled for #" + request.getPieceNumber() + "::" + request.getOffset() + "->" + (request.getOffset() + request.getLength()));
    if(requested.contains(request))
    {
        requested.remove(request);
        ByteBuffer buffer = ByteBuffer.allocate(17);
        buffer.putInt(13);
        buffer.put(BT_CANCEL);
        buffer.putInt(request.getPieceNumber());
        buffer.putInt(request.getOffset());
        buffer.putInt(request.getLength());
        buffer.position(0);
        buffer.limit(17);
        write(buffer);
    }          
  }
  
  public void sendHave(int pieceNumber)
  { 
    if(state != TRANSFERING)
      return;     
    logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is notified you have " + pieceNumber);
    ByteBuffer buffer = ByteBuffer.allocate(9);
    buffer.putInt(5);
    buffer.put(BT_HAVE);
    buffer.putInt(pieceNumber);
    buffer.position(0);
    buffer.limit(9);
    write(buffer);
    checkInterested();
  }
  
  public void sendChoke()
  {
    if(state != TRANSFERING)
      return;
    logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is choked");
    choking = true;
    sendSimpleCommand(BT_CHOKED);
  }
  
  public void sendUnChoke()
  {
    if(state != TRANSFERING)
       return;
    logger.log(componentID,evtProtocol,Logger.SENT,peerIp + " is unchoked");
    choking = false;
    sendSimpleCommand(BT_UNCHOKED);     
  }
  
    
  //Getters
  public int getPeerState()
  {
    return this.state;
  }
  
  public boolean[] getAvailable()
  {
    return available;
  }
  
  public boolean transfertAvailable()
  {
    return (!choked && interested);
  }
  
  public SocketChannel getSocketChannel()
  {
    return sck;
  }
  
  public PeerStats getStats()
  {
    return stats;
  }
  
  public String getPeerAddress()
  {
    return this.peerIp;    
  }
  
  public int getPeerPort()
  {
    return this.peerPort;
  }
  
  public boolean isInteresting()
  {
    return interesting;
  }
  
  public boolean isChoking()
  {
    return choking;
  }
  
  public boolean isChoked()
  {
    return choked;
  }
  
  public boolean isInterested()
  {
    return interested;
  }    
  
  public Vector getExpiredRequests()
  {
    Vector result = new Vector();
    for(int i = 0 ; i < requested.size() ; i++)
    {
      try {
        Request request = (Request) requested.get(i);
        if(request.isExpired())
        {
          result.add(request);
        }
      } catch(ArrayIndexOutOfBoundsException e)
      {
        //Keep going, most probably, piece removed...
        //Hopefully we'll find it later :p
      }      
    }
    return result;    
  }
  
  public boolean isSeed()
  {
    return isSeed;
  }  
  
  
  public int getNbRequests()
  {
    return requested.size();
  }
   
  
  /**
  * public method to update the displayed informations
  */
  public void updateAll()
  {
    if(peerItem != null)
      peerItem.updateAll();    
  }
  
  public void updateStats()
  {
    if(peerItem != null)
      peerItem.updateStats();
  }
  
  public void updateImage()
  {
    if(peerItem != null)
      peerItem.updateImage();
  }
  
  public void sent(int length)
  {
    manager.sent(length);
  }
    
  
  public void cancelRequests()
  {
   if(requested == null)
    return;
    while(requested.size() > 0)
    {
      Request request = (Request) requested.remove(0);
      manager.cancelRequest(request);
    }
  }
  
  public boolean equals(Object o)
  {
    if(!(o instanceof PeerConnection))
      return false;
    PeerConnection pc = (PeerConnection) o;
    //At least the same instance is equal to itself :p
    if(this == pc)
        return true;
    if(! (pc.peerIp).equals(this.peerIp))
      return false;
    //same ip, we'll check peerId
    byte[] otherPeerId = pc.getPeerId();
    if(otherPeerId == null) return false;
    if(this.peerId == null) return false;
    for(int i = 0 ; i < otherPeerId.length ; i++)
    {
        if(otherPeerId[i] != this.peerId[i])
            return false;
    }     
    return true;
  }
  
  public byte[] getPeerId()
  {
      return this.peerId;
  }
  
  public void setSnubbed()
  {
      if(!this.snubbed)
        logger.log(componentID,evtLifeCycle,Logger.INFORMATION,peerIp + " is snubbed");
      this.snubbed = true;      
  }
  
  public void setUnSnubbed()
  {
      if(this.snubbed)
        logger.log(componentID,evtLifeCycle,Logger.INFORMATION,peerIp + " is un-snubbed");
        
      this.snubbed = false;      
  }
  
  public boolean isSnubbed()
  {
      return this.snubbed;
  }
  
  public boolean isIncoming()
  {
      return isIncoming;
  }
  
  public Request getLastRequest()
  {
      int nbRequested = requested.size();
      if(nbRequested > 0)
      {
          Request request = (Request) requested.get(nbRequested-1);
          return request;
      }
      else return null;            
  }
  
  public int getPercentDone()
  {
    int sum = 0;
    for(int i = 0 ; i < available.length ; i++)
    {
      if(available[i]) sum++;
    }
    
    sum = (sum * 1000) / available.length;
    return sum;
  }
  
  public PeerManager getManager()
  {
    return manager;
  }
  
  public boolean isOptimisticUnchoke()
  {
    return manager.isOptimisticUnchoke(this);
  }
  
 
   
}