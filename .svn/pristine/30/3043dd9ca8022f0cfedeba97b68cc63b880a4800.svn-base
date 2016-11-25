/*
 * Created on 9 juin 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.gudy.azureus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * 
 * A set of Label + checkBox used for the console configuration panel.
 * 
 * @author Olivier
 *
 */
public class ConsoleItem {
    
    private int _componentId;
    private int _eventId;
    
    public ConsoleItem(Composite parent,int componentId,int eventId,String eventName)
    {
        this._componentId = componentId;
        this._eventId = eventId;
        final Button bCheck = new Button(parent,SWT.CHECK);
        bCheck.addListener(SWT.Selection,new Listener () {
            public void handleEvent(Event e)
            {
                Logger.getLogger().setSetting(_componentId,_eventId,bCheck.getSelection());
            }
        });
       new Label(parent,SWT.NULL).setText(eventName + "\t\t");
       bCheck.setSelection(Logger.getLogger().getSetting(componentId,eventId));
    }

}
