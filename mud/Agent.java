
package mud;
import WizardUI.ODiscriptor;
import WizardUI.Notification;

import java.net.*;
import java.io.*;

import mud.interfaces.Container;
import mud.interfaces.Described;
import mud.interfaces.NotifyOnEnter;
import mud.interfaces.NotifyOnLeave;
import mud.interfaces.NotifyOnMove;

public class Agent extends BasicLocated 
	implements NotifyOnEnter, NotifyOnLeave, NotifyOnMove, Described, Runnable {
	ObjectInputStream in = null;		
	ObjectOutputStream out = null;
	
	public Agent(){
		new Thread(this).start();
	}
	
	public void run(){
			try{
				Thread.sleep(1000);
				ServerSocket ss = new ServerSocket(9999);
				while (true){
					try{
						Socket s = ss.accept();
						Mud.log(this, 10, "Accepted connection");
						out = new ObjectOutputStream(s.getOutputStream());
						in = new ObjectInputStream(s.getInputStream());		
						Mud.log(this, 10, "Connected OK");
						out.writeObject("Connected");
						notifyMove(location);
						while(true){
							Object o = in.readObject();
							if ( o instanceof ODiscriptor ){
								((ODiscriptor)o).makeAttrs();
								out.writeObject(o);
							}
						}
					} catch (Exception e){
						Mud.log(this,10,e);
					}
					in = null;
					out = null;
				}
			} catch (Exception e){
				Mud.log(this,1000,e);
			}
	}
	
	public void notifyEnter(mud.interfaces.MudObject o, Object exp) {
		try{
			if ( out != null )
				out.writeObject(new Notification(o,exp,Notification.ENTER));
		} catch (Exception e){Mud.log(this,100,e);}
	}

	public void notifyLeave(mud.interfaces.MudObject o, Object exp) {
		try{
			if ( out != null )
				out.writeObject(new Notification(o,exp,Notification.LEAVE));
		} catch (Exception e){Mud.log(this,100,e);}
	}

	public void notifyMove(Container c) {
		try{
			if ( out != null )
				out.writeObject(new Notification(c,null,Notification.MOVE));
		} catch (Exception e){Mud.log(this,100,e);}
	}

	public String getShort() {
		return "An Agent";
	}

	public String getLong() {
		return "An Agent";
	}



}
