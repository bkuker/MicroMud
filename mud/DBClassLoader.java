package mud;
import java.util.HashMap;

public class DBClassLoader extends ClassLoader{
	static HashMap m = new HashMap();
	static DBClassLoader cl = new DBClassLoader();

	private class SubLoader extends ClassLoader{
		String myClassName;
		
		private SubLoader(String n){
			super(DBClassLoader.this);
			Mud.log(this, 100, "New SubLoader "+this+" for "+n);
			myClassName = n;
		}
		
		protected Class findClass(String name) throws ClassNotFoundException {
			return DBClassLoader.this.loadClass(name);
		}
		
		public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
			return DBClassLoader.this.loadClass(name);
		}

		public synchronized Class loadDBClass(DBClassHandle h) throws ClassNotFoundException {
			Class c = null;
			if ( !h.name.equals(myClassName) )
				throw new ClassNotFoundException("Tried to load "+h.name+" with "+myClassName+" loader.");

			if (h.c == null){
				if ( h.code == null ){
					throw new ClassNotFoundException("No object code for "+h.name+" found.");
				}
				Mud.log(this, 100, this+" Loading class " + h.name);
				h.c = defineClass(h.name, h.code, 0, h.code.length);
			}
			c = h.c;
			m.put(h.name,c);
			return c;
		}
	}

	protected Class findClass(String name) throws ClassNotFoundException {
		Mud.log(this, 110, "Find Class");
		return super.findClass(name);	
	}

	public synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
		DBClassHandle h = DBClassHandle.getDBClassHandle(name);
		if ( h != null && h.c == null )
			m.put(name,null);

		Class c = null;
		c = (Class)m.get(name);

		if ( c == null){
			if ( h == null ){
				c = findSystemClass(name);
				Mud.log(this, 1000000, "Using system class for "+name);
				m.put(name,c);
			} else {
				if ( h.c == null)
					h.c = (new SubLoader(name).loadDBClass(h));
				c = h.c;
			}
		}

		if (resolve) resolveClass(c);
		return c;
	}
}
