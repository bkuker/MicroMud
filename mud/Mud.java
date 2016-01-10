package mud;

import java.sql.*;
import java.io.*;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import mud.interfaces.MudObject;
import mud.interfaces.Place;
import mud.telnet.TelnetServer;

public class Mud {
	///////////////////////////////////////////
	//Special Saved items
	static long time = 0; //id 0
	static TreeSet callbacks = new TreeSet(); //id 1
	static HashMap heartbeats = new HashMap(); //id 2


	/////////////////////////////////////////////////////////////////////////////////
	//Static Setup
	static Connection con;
	private static HashSet cbAdd = new HashSet();
	private static HashSet hbChange = new HashSet();
	private static class hbChangeItem {
		int o, n;
		MudObject m;
		public hbChangeItem(MudObject _m, int _n, int _o) {
			m = _m;
			o = _o;
			n = _n;
		}
	}
	private static class CBItem implements Comparable {
		int n;
		Object o;
		public CBItem(int _n, Object _o) {
			n = _n;
			o = _o;
		}
		public int compareTo(Object o) {
			CBItem c = (CBItem) o;
			return n - c.n;
		}
	}

	static DBClassLoader classLoader = new DBClassLoader();
	static {
		try {
			Class.forName("org.postgresql.Driver");
			con =
				DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/kukester",
					"kukester_java",
					"j4v4one");
			
			callbacks = ((TreeSet) DBObject.getObject(1));
			heartbeats = ((HashMap) DBObject.getObject(2));
			Long lt = ((Long) DBObject.getObject(4));
			
			if (lt != null)
				time = lt.longValue();
			if (callbacks == null)
				callbacks = new TreeSet();
			if (heartbeats == null)
				heartbeats = new HashMap();
			
	/*		
			if ( id == null ){
				Mud.log(null, 10, "Created a new player location");
				MudObject p = new Room();
				newLocId = p.getObjectId();
			} else {
				newLocId = id.longValue();
			}
			*/
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	private static class MudShutDown extends Thread {
		public MudShutDown() {
			Mud.log(this, 1, "B");
		}

		public void run() {
			Mud.log(this, 1, "Shutting down");
			try {

			} catch (Exception e) {
				e.printStackTrace();
				Mud.log(this, 0, "ERROR SAVING!");
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////////////
	//Utility Functions
	public static void log(Object source, int level, String m) {
		if (level < 10000)
			System.out.println(source + ": " + m);
	}

	public static void log(Object source, int level, Exception e) {
		log(source, level, e.toString());
		e.printStackTrace();
	}

	public static byte[] loadFile(File file) {
		try {
			InputStream is = new FileInputStream(file);
			long length = file.length();
			if (length > Integer.MAX_VALUE) {
				// File is too large
			}

			byte[] bytes = new byte[(int) length];

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
				offset += numRead;
			if (offset < bytes.length) {
				throw new IOException(
					"Could not completely read file " + file.getName());
			}
			is.close();
			return bytes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void save() {
		update();
		Long lt = new Long(time);
		DBObject.setId(lt, 0);
		DBObject.saveObject(lt);
		DBObject.setId(callbacks, 1);
		DBObject.saveObject(callbacks);
		DBObject.setId(heartbeats, 2);
		DBObject.saveObject(heartbeats);
		//Long id = new Long(newLocId);
		//DBObject.setId(id, 3);
		//DBObject.saveObject(id);
		DBObject.saveAll();
	}

	public static void callBack(int time, Message m) {
		cbAdd.add(new CBItem(time, m));
	}

	public static void callBack(
		int time,
		Object o,
		java.lang.reflect.Method m) {
		callBack(time, new Message(o, m, null));
	}

	public static void callBack(int time, Object o, String m) {
		try {
			callBack(time, new Message(o, m, null));
		} catch (Exception e) {
			Mud.log(o, 110, e);
		}
	}

	private static void runCallBacks() {
		while (!callbacks.isEmpty()
			&& ((CBItem) callbacks.first()).n <= time) {
			((Message) callbacks.first()).dispatch();
			callbacks.remove(callbacks.first());
		}
	}

	public static void setHeartbeat(MudObject o, int newhb, int oldhb) {
		hbChange.add(new hbChangeItem(o, newhb, oldhb));
	}

	public static void setHeartbeatImp(MudObject o, int newhb, int oldhb) {
		HashSet s = null;
		if (oldhb != 0) {
			s = (HashSet) heartbeats.get(new Integer(oldhb));
			if (s != null) {
				s.remove(o);
				if (s.size() == 0)
					heartbeats.remove(heartbeats.get(new Integer(oldhb)));
			}
		}
		if (newhb != 0) {
			s = (HashSet) heartbeats.get(new Integer(newhb));
			if (s != null) {
				s.add(o);
			} else {
				s = new HashSet();
				s.add(o);
				heartbeats.put(new Integer(newhb), s);
			}
		}
	}

	public static void runHeartbeats() {
		Iterator i = heartbeats.keySet().iterator();
		while (i.hasNext()) {
			Integer n = (Integer) i.next();
			if (time % n.intValue() == 0) {
				Iterator j = ((HashSet) heartbeats.get(n)).iterator();
				while (j.hasNext()) {
					MudObject o = (MudObject) j.next();
					try {
						o.heartbeat();
					} catch (Throwable t) {

					}
				}
			}
		}
	}

	private static void update() {
		//Heartbeat
		Iterator i = hbChange.iterator();
		while (i.hasNext()) {
			hbChangeItem c = (hbChangeItem) i.next();
			setHeartbeatImp(c.m, c.n, c.o);
		}
		hbChange.clear();
		//callbacks
		callbacks.addAll(cbAdd);
		cbAdd.clear();
	}

	/////////////////////////////////////////////////////////////////////////////////
	//Main Loop
	public static void main(String args[]) {
	    Thread t = new MudShutDown();
		Runtime.getRuntime().addShutdownHook(t);
		new TelnetServer().start();
		while (true) {
			time++;
			try {
				Thread.sleep(1000);
				runCallBacks();
				runHeartbeats();
				update();
				while (Message.size() > 0) {
					Message.dispatchMessage();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * @return
	 */
	public static DBClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * @return
	 */
	public static Place getEntry() {
	    return DBRoom.getEntry();
	}

}
