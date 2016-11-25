/*
 * Created on 8 juin 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.gudy.azureus;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * 
 * A singleton used to Log text into the console window.
 * 
 * @author Olivier
 *
 */
public class Logger {
    
    private static Logger logger = null;
    
    private StyledText console;
    private Display display;
    private int nbLinesMax = 1280;
    private boolean enabled = true;
    private boolean[][] settings;
    private int lastTopIndex;
    
    
    public static final int INFORMATION = 0;
    public static final int RECEIVED    = 1;
    public static final int SENT        = 2;
    public static final int ERROR       = 3;
    
    private Color[] colors;
    
    private Logger(MainSwt main)
    {
        this.console = main.getConsole(); 
        this.display = console.getDisplay();
        this.settings = new boolean[6][];
        this.settings[0] = new boolean[1];
        this.settings[1] = new boolean[3];
        this.settings[2] = new boolean[3];
        this.settings[3] = new boolean[1];
        this.settings[4] = new boolean[3];
        this.settings[5] = new boolean[1];
        this.settings[0][0] = true;
        this.settings[1][0] = true;
        this.settings[1][1] = true;
        this.settings[1][2] = true;
        this.settings[2][0] = true;
        this.settings[2][1] = true;
        this.settings[2][2] = true;
        this.settings[3][0] = true;
        this.settings[4][0] = true;
        this.settings[4][1] = true;
        this.settings[4][2] = true;
        colors = new Color[4];
        colors[0] = new Color(display,new RGB(64,160,255));
        colors[1] = new Color(display,new RGB(128,192,255));
        colors[2] = new Color(display,new RGB(192,224,255));                
        colors[3] = new Color(display,new RGB(255,192,192));
        display.addListener(SWT.Dispose,new Listener() {
            public void handleEvent(Event e)
            {
                colors[0].dispose();
                colors[1].dispose();
                colors[2].dispose();
                colors[3].dispose();
            }
        }
        );
    }
    
    public static final Logger getLogger()
    {
        return logger;
    }
    
    public synchronized static final void createLogger(MainSwt main)
    {
        if(logger == null)
            logger = new Logger(main);
    }
    
    public void log(int componentId,int event,int color,String text)
    {
        if(!enabled)
            return;
        try{
            if(!settings[componentId][event])   return;
        } catch(Exception e) {
            if(settings[0][0])
            {
              doLog(3,"Error while logging - invalid componentId/event : " + componentId + "/" + event);
            }
            return;            
        }
        if(color < 0 || color > colors.length)
            return;
        doLog(color,text);
    }
    
    private void doLog(int color,String text)
    {
        final String _text = text;
        final int _color = color;
        if(display.isDisposed())
            return;
        display.asyncExec( new Runnable () {
                public void run()
                {
                    if(console.isDisposed())
                        return;
                  ScrollBar sb = console.getVerticalBar();
                  
                  //System.out.println(sb.getSelection()+ "/" + (sb.getMaximum() - sb.getThumb()));
                  boolean autoScroll = sb.getSelection() == (sb.getMaximum() - sb.getThumb());
                    int nbLines = console.getLineCount();
                    if(nbLines > nbLinesMax + 256)
                        console.replaceTextRange(  0,
                                    console.getOffsetAtLine(256),"");
                    Calendar now = GregorianCalendar.getInstance();
                    String timeStamp = "[" +
                                       now.get(Calendar.HOUR_OF_DAY) +
                                       ":" +
                                       now.get(Calendar.MINUTE) +
                                       ":" +
                                       now.get(Calendar.SECOND) +
                                       "]  ";
                    nbLines = console.getLineCount();                   
                    console.append(timeStamp + _text + "\n");
                    console.setLineBackground(nbLines-1,1,colors[_color]);                                       
                    if(autoScroll)
                      console.setTopIndex(nbLines);                                        
                }
            }
        );
    }
    
    public void setMaxLines(int nbLines)
    {
        this.nbLinesMax = nbLines;
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    public void clearLog()
    {
        console.replaceTextRange( 0,
                                   console.getCharCount(),"");
    }
    
    public void setSetting(int componentId,int eventId,boolean enabled)
    {
     try {
         settings[componentId][eventId] = enabled;   
     }
     catch(Exception e)
     {
         e.printStackTrace();
     }
    }
    
    public boolean getSetting(int componentId,int eventId)
    {
        boolean result = false;
        try {
         result =settings[componentId][eventId];   
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         return result;
    }

}
