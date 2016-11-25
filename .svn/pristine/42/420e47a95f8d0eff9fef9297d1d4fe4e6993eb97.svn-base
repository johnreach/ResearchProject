package org.gudy.azureus;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Vector;

/**
 * 
 * A Thread to write on a peer Socket.
 * 
 * 
 * 
 * @author Olivier
 *
 */
public class WriteThread extends Thread {
  private PeerConnection pc;
  private SocketChannel sck;
  private PeerStats stats;

  private Vector toBeSend;
  private boolean bContinue;
  private int keepAliveCounter;

  private boolean uploading;
  private ByteBuffer uploadingBuffer;
  
  //The maximum number of bytes that this peer can get
  //over 100ms.
  private int maxUpload;

  public WriteThread(PeerConnection pc) {
    this.pc = pc;
    this.sck = pc.getSocketChannel();
    this.stats = pc.getStats();
    this.toBeSend = new Vector();
    this.bContinue = true;
    this.uploading = false;
    this.uploadingBuffer = null;
    //unlimited peers start at : 10MB/S, which is 1MB/100ms
    this.maxUpload = 1024 * 1024;
  }

  public void run() {
    while (bContinue) {
      try {
        if (toBeSend.size() > 0) {
          SendPacket sd = (SendPacket) toBeSend.remove(0);
          int type = sd.getType();
          if (type == SendPacket.INFORMATION)
            write(sd.getData());
          else {
            ByteBuffer buffer = sd.getData();
            if (!pc.isChoking()) {
              this.uploading = true;
              SpeedLimiter.getLimiter().addUploader(this);
              write(buffer);
              this.uploading = false;
              SpeedLimiter.getLimiter().removeUploader(this);
            }
            this.uploadingBuffer = null;
            ByteBufferPool.getInstance().freeBuffer(buffer);
          }
          keepAliveCounter = 0;
        }
        if (toBeSend.size() == 0) {
          keepAliveCounter++;
          Thread.sleep(50);
        }
        //1/20th s  * (20 * 60 * 2) = 2 mins
        if (keepAliveCounter == 2400) {
          //System.out.println(".");
          keepAliveCounter = 0;
          ByteBuffer buffer = ByteBuffer.allocate(4);
          buffer.putInt(0);
          buffer.position(0);
          buffer.limit(4);
          write(buffer);
        }
      } catch (Exception e) {
        //e.printStackTrace();
        bContinue = false;
        if (this.uploading) {
          SpeedLimiter.getLimiter().removeUploader(this);
        }
        if (this.uploadingBuffer != null) {
          ByteBufferPool.getInstance().freeBuffer(uploadingBuffer);
          uploadingBuffer = null;
        }
        while (toBeSend.size() > 0) {
          SendPacket sd = (SendPacket) toBeSend.remove(0);
          int type = sd.getType();
          if (type == SendPacket.DATA)
            ByteBufferPool.getInstance().freeBuffer(sd.getData());
        }
        pc.closeAll();
      }
    }
  }

  public void send(SendPacket sd) {
    if(!bContinue) return;
    int type = sd.getType();
    if (type == SendPacket.INFORMATION) {
      int insertPosition = 0;
      for (int i = 0; i < toBeSend.size(); i++) {
        SendPacket sdi = (SendPacket) toBeSend.get(i);
        if (sdi.getType() == SendPacket.INFORMATION)
          insertPosition = i + 1;
      }
      toBeSend.add(insertPosition, sd);
    }
    if (type == SendPacket.DATA) {
      toBeSend.add(sd);
    }
  }

  public void stopThread() {
    bContinue = false;
    while (toBeSend.size() > 0) {
      SendPacket sd = (SendPacket) toBeSend.remove(0);
      int type = sd.getType();
      if (type == SendPacket.DATA)
        ByteBufferPool.getInstance().freeBuffer(sd.getData());
    }
  }

  private void write(ByteBuffer buffer) throws Exception {
    int written = 0;
    int realLimit = buffer.limit() - buffer.position();
    buffer.mark();
    while (written < realLimit) {
      long timeStarted = System.currentTimeMillis();
      int deltaWritten = 0;
      int limit = realLimit;
      int uploadAllowed = 0;
      if (SpeedLimiter.getLimiter().isLimited(this)) {
        uploadAllowed = SpeedLimiter.getLimiter().getLimitPer100ms(this);
        limit =
          buffer.position() + uploadAllowed;
        if (limit > realLimit || limit == 0)
          limit = realLimit;
      }
      buffer.limit(limit);
      int iter = 0;
      while (buffer.position() < limit) {
        int subDelta = 0;
        subDelta = sck.write(buffer);
        deltaWritten += subDelta;
        stats.sent(subDelta);
        pc.sent(subDelta);
        if(SpeedLimiter.getLimiter().isLimited(this) && iter > 10)
        {
          maxUpload = deltaWritten + 100;
        }
        if (buffer.position() < limit)
        {        
          Thread.sleep(10);
          iter++;
        }
      }
      written += deltaWritten;
      if(SpeedLimiter.getLimiter().isLimited(this))
      {
        if(deltaWritten >= (95*uploadAllowed)/100)
          maxUpload = 1024 * 1024;
        if(deltaWritten <  (95*uploadAllowed)/100)
          maxUpload = deltaWritten + 100;
      }
      long timeWait = 100 - (System.currentTimeMillis() - timeStarted);
      if (timeWait < 10)
        timeWait = 10;
      if (written != realLimit)
        Thread.sleep(timeWait);
    }
  }
  
  public int getMaxUpload()
  {
    return maxUpload;
  }

}