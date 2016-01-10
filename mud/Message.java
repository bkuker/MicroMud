package mud;
import java.lang.reflect.*;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;


public class Message{
	private Object o;
	private Method m;
	private Object[] args;
	private boolean map = false;
	
	private static Class messageClass;
	private static Vector queue = new Vector();
	
	
	public static int size(){
		return queue.size();
	}

	public static Message next(){
		return (Message)queue.remove(0);
	}

	public static void dispatchMessage(){
		if ( size() > 0 )
			next().dispatch();
	}
	
	public static Message map(Method _m, Set _s, Object[] _a){
		Message m = new Message(_s, _m, _a);
		m.map = true;
		return m;		
	}
	
	public static Message map(Method _m, Set _s, Object _a){
		Object[] a = new Object[1];
		a[0] = _a;
		return map(_m, _s, a);
	}
	
	public static Message map(String _m, Class _c, Set _s, Object[] _a) {
		Class[] c = new Class[_a.length];
		for( int i = 0; i < _a.length; i++ )
			c[i] = _a[i].getClass();
		try{
			Method m = _c.getMethod(_m,c);
			return map(m, _s, _a);
		} catch (Exception e){
			Mud.log(Message.class, 100, e);
		}
		return null;
	}
	
	public static Message map(String _m, Class _c, Set _s, Object _a){
		Object[] a = new Object[1];
		a[0] = _a;
		return map(_m, _c, _s, a);
	}

	public Message(Object _o, Method _m, Object[] _a ){
		o = _o;
		m = _m;
		args = _a;
	}

	public Message(Object _o, String _m, Object[] _a ){
		o = _o;
		args = _a;
		m = null;
		Class s = o.getClass();
		Class[] c = new Class[_a.length];
		for( int i = 0; i < _a.length; i++ )
			c[i] = _a[i].getClass();
		try{
			m = s.getMethod(_m,c);
		} catch (Exception e){
			Mud.log(this, 100, e);
		}
	}

	public Message( Object _o, String _m, Object _a ){
		o = _o;
		args = new Object[1];
		args[0] = _a;
		m = null;
		Class s = o.getClass();
		Class[] c = new Class[1];
		c[0] = _a.getClass();
		try{
			m = s.getMethod(_m,c);
		} catch (Exception e){
			Mud.log(this, 100, e);
		}
	}

	public void enqueue(){
		queue.add(this);
	}

	public void dispatch(){
		try{
			Mud.log(this, 1000, "Message " + m.getName() + " call too " + o);
			if ( map ){
				if ( o instanceof Set ){
					Iterator i = ((Set)o).iterator();
					while ( i.hasNext() ){
						try{
							Object ob = i.next();
							if ( m.getDeclaringClass().isInstance(ob))
								m.invoke(ob, args);
						} catch ( Exception e ){
							Mud.log(this, 90, e);
						}
					}
				}
			} else {
				//Object ret = 
			    m.invoke(o, args);
			}
		} catch (Exception e){
			Mud.log(this, 90, e);
		}
	}
}
