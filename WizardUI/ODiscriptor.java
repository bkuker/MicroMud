/*
 * Created on Jun 28, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package WizardUI;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.io.IOException;

import mud.interfaces.Described;




public class ODiscriptor implements Serializable {
	int hashcode = 0;
	String className;
	String disc;
	HashMap attrs;
	transient Object o;
	
	static HashMap sent = new HashMap();
	

	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
		 in.defaultReadObject();
		 o = sent.get(new Integer(hashcode));
	}


	public ODiscriptor(Object _o) {
		o = _o;
		if (o == null)
			return;
		hashcode = o.hashCode();
		if (o instanceof Described) {
			disc = ((Described) o).getShort();
		} else {
			disc = o.toString();
		}
		className = o.getClass().getName();
		sent.put(new Integer(hashcode),o);
	}
	
	public String toString(){
		return disc;
	}
	
	public int hashCode(){
		return hashcode;
	}

	public void makeAttrs() {
		try {
			attrs = new HashMap();
			Class c = o.getClass();
			while (c != Object.class) {
				Field[] f = c.getDeclaredFields();
				AccessibleObject.setAccessible(f, true);
				for (int i = 0; i < f.length; i++) {
					if (!Modifier.isStatic(f[i].getModifiers())) {
						String name = f[i].getName();
						Object value = null;
						if (f[i].getType().isPrimitive()
							|| f[i].getType() == String.class) {
							value = f[i].get(o);
						} else if (f[i].getType().isArray()){
							if (f[i].getType().getComponentType().isPrimitive()){
								value = f[i].get(o);
							} else {
								value = Array.newInstance(ODiscriptor.class, Array.getLength(f[i].get(o)));
								for ( int j = 0; j < Array.getLength(f[i].get(o)); j++)
									Array.set(value, j, new ODiscriptor(Array.get(f[i].get(o),j)));
	
							}
						} else {
							if (f[i].get(o) != null) {
								value = new ODiscriptor(f[i].get(o));
								sent.put(value, f[i].get(o));
							} else {
								value = null;
							}
						}
						attrs.put(name, value);
					}
				}
				c = c.getSuperclass();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * @return
	 */
	public HashMap getAttrs() {
		return attrs;
	}

}