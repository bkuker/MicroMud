package mud;

/*
 delete from java.property;
 
 delete from java.object where id not in (select id from is_mud_world.is_mud_area) and id not in (select id from is_mud_world.is_mud_room) and id not in (select id from is_mud_world.is_mud_room_exit)
 *  
 */
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.sql.*;
import java.io.*;

import sun.reflect.ReflectionFactory;

public class DBObject implements Serializable {
    static PreparedStatement insertObject;
    static PreparedStatement insertProperty;

    static PreparedStatement removeProperty;
    static PreparedStatement getObject;
    //static PreparedStatement getProperties;

    static HashMap lookup = new HashMap();
    static WeakHashMap toId = new WeakHashMap();

    private static final int UNLOADED = 0;
    private static final int LOADED = 1;
    private transient int loaded = UNLOADED;
    private transient long object_id = 0;
    private transient Object proxy = null;

    static {
        try {
            insertProperty = Mud.con
                    .prepareStatement("INSERT INTO java.property (object_id, name, type, value, class) VALUES (?,?,?,?,?)");
            insertObject = Mud.con
                    .prepareStatement("INSERT INTO java.object (id,class,data) VALUES (?,?,?)");
            removeProperty = Mud.con
                    .prepareStatement("DELETE FROM java.property WHERE object_id = ?");
            getObject = Mud.con
                    .prepareStatement("SELECT user, class, data FROM java.object WHERE id = ?");
        } catch (Exception s) {
            s.printStackTrace();
            System.exit(-1);
        }
    }

    //This is the proxy object written to an Objectoutputstream
    //in the place of a dbobject
    private static class DBProxy implements Serializable {
        long id;

        public DBProxy(long id) {
            this.id = id;
        }

        public Object readResolve() throws ObjectStreamException, SQLException {
            Mud.log(this, 1000, "Finding replacement..");
            return getObject(id);
        }
    }

    private static class MyObjectOutputStream extends ObjectOutputStream {
        public MyObjectOutputStream(OutputStream o) throws IOException {
            super(o);
            enableReplaceObject(true);
        }

        protected Object replaceObject(Object o) throws IOException {
            Mud.log(this, 10, "Writing " + o.toString());
            if (Proxy.isProxyClass(o.getClass())) {
                Mud.log(this, 10, "Replacing a proxy...");
                long id = ((DBOIC) Proxy.getInvocationHandler(o)).o.object_id;
                return new DBProxy(id);
            }
            return o;
        }
    }

    //This is used by the proxy to call methods in a DBObject
    private static class DBOIC implements InvocationHandler {
        public DBObject o;

        public DBOIC(DBObject _o) {
            o = _o;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            try {
                Mud.log(this, 1000, "Proxying " + method.getName()
                        + " call too " + o);
                //replace any dbobject with its proxy, like a ninja
                if (args != null)
                    for (int i = 0; i < args.length; i++)
                        if (args[i] instanceof DBObject) {
                            args[i] = ((DBObject) args[i]).proxy;
                            if (args[i] == null)
                                Mud.log(null, 1, "Object has null proxy!");
                        }
                Object ret = method.invoke(o, args);
                //If it is an upgrade method replace the current
                //object with the new one.
                if (method.getName().equals("upgrade")) {
                    o = (DBObject) getRealObject(o.object_id);
                }
                return ret;
            } catch (IllegalAccessException e) {
            } catch (IllegalArgumentException e) {
            } catch (InvocationTargetException e) {
            }
            return null;
        }
    }

    ///////////////////////////////////////////////////////////////
    //Non static
    //

    //Thanks to no operator overloading the == doesnt work if somehow
    //you are comparing an object to it's proxy, so use .is()
    public boolean equals(Object o) {
        return (this == o || proxy == o);
    }

    //Called after the object is loaded from the database in case some
    //work needs to be done
    public void emerge() {

    }

    public Object writeReplace() throws ObjectStreamException {
        return new DBProxy(object_id);
    }

    //Return the ID for this object, allocate one if
    //it is not yet given one.
    public long getObjectId() throws Exception {
        if (object_id == 0) {
            object_id = nextID();
            Long id = new Long(object_id);
            lookup.put(id, this);
            toId.put(this, id);
        }
        return object_id;
    }

    public DBObject() {
        try {
            getObjectId();
        } catch (Exception e) {
            Mud.log(this, 0, e);
        }
        updateProxy();
    }

    public void finalize() {
        Mud.log(this, 100, " is being finalized");
    }

    //Save this object to DB - may be wise to take this out and
    //only allow the global save.
    public void save() {
        saveObject(this);
    }

    //This method RETURNS an upgraded version WITHOUT replacing or affecting
    //the current object.
    public void upgrade() {
        save();
        Object n = loadObject(object_id);
        updateTransients(this, n);
        lookup.put(new Long(object_id), n);
    }

    //Create a new proxy, ie after an upgrade or when object
    //is first loaded
    private void updateProxy() {
        InvocationHandler ic;
        if (proxy == null)
            ic = new DBOIC(this);
        else
            ic = Proxy.getInvocationHandler(proxy);
        HashSet classes = new HashSet();
        Class s = getClass();
        while (s != null) {
            Class[] more = s.getInterfaces();
            for (int i = 0; i < more.length; i++)
                classes.add(more[i]);
            s = s.getSuperclass();
        }

        proxy = Proxy.newProxyInstance(Mud.classLoader, (Class[]) classes
                .toArray(new Class[0]), ic);
    }

    ////////////////////////////////////////////////////////////////
    //Static
    //

    //Set the id for a non DBOBject - should be replaced
    //with named objects instead of 'kinda well known ids'
    public static void setId(Object o, long id) {
        if (!toId.containsKey(o) && !(o instanceof DBObject)) {
            Long l = new Long(id);
            toId.put(o, l);
            lookup.put(l, o);
        }
    }

    public static void saveAll() {
        Iterator i = lookup.entrySet().iterator();
        while (i.hasNext()) {
            Object o = ((java.util.Map.Entry) i.next()).getValue();
            if (o instanceof DBObject)
                ((DBObject) o).save();
        }

    }

    public static Object newObject(String classname)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        Object o = Mud.classLoader.loadClass(classname).newInstance();
        if (o instanceof DBObject) {
            return ((DBObject) o).proxy;
        }
        return o;
    }

    public static Object newObject(DBClassHandle h)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        return newObject(h.getName());
    }

    public static Object getObject(long id) {
        Object o = lookup.get(new Long(id));
        if (o != null) {
            if (o instanceof DBObject) {
                return ((DBObject) o).proxy;
            } else {
                return o;
            }
        }
        o = loadObject(id);
        if (o instanceof DBObject) {
            return ((DBObject) o).proxy;
        } else {
            return o;
        }
    }

    //This should be used rarely, and mostly by the mud driver...
    protected Object getProxy() {
        return proxy;
    }

    private static Object getRealObject(long id) {
        Object o = lookup.get(new Long(id));
        if (o != null)
            return o;
        return loadObject(id);
    }

    private static long nextID() throws SQLException {
        ResultSet s = Mud.con.prepareStatement(
                "SELECT nextval('java.object_id_seq')").executeQuery();
        s.next();
        return s.getLong(1);
    }

    private static synchronized Object loadObject(long id) {
        Object o = null;

        Mud.log(DBObject.class, 1000, "Loading object " + id);
        try {
            getObject.setLong(1, id);
            ResultSet or = getObject.executeQuery();
            if (!or.next()) {
                Mud.log(DBObject.class, 100, "Cant find object " + id);
                return null;
            }
            Class c = Mud.classLoader.loadClass(or.getString(2));
            if (or.getBytes(3) == null) {
                //Create a new blank object without calling the ctor
                //(this is sun JVM specific for now :( )
                ReflectionFactory reflectionFactory = sun.reflect.ReflectionFactory
                        .getReflectionFactory();
                Constructor javaLangObjectConstructor = Object.class
                        .getDeclaredConstructor(new Class[0]);
                Constructor customConstructor = reflectionFactory
                        .newConstructorForSerialization(c,
                                javaLangObjectConstructor);
                o = customConstructor.newInstance(new Object[0]);
                ((DBObject) o).object_id = id;
                ((DBObject) o).updateProxy();
            } else {
                ByteArrayInputStream bs = new ByteArrayInputStream(or
                        .getBytes(3));
                ObjectInputStream os = new ObjectInputStream(bs);
                o = os.readObject();
            }

            Long lid = new Long(id);
            lookup.put(lid, o);
            toId.put(o, lid);

            if (!(o instanceof DBObject)) {
                return o;
            }

            PreparedStatement getProperties = Mud.con
                    .prepareStatement("SELECT name, type, value, class FROM java.property WHERE object_id = ?");
            getProperties.setLong(1, id);
            ResultSet ps = getProperties.executeQuery();
            while (ps.next()) {
                String n = ps.getString(1);
                String t = ps.getString(2).trim();
                String v = ps.getString(3);
                String cn = ps.getString(4);
                Mud.log(o, 1001, "Param " + n + " " + t + " " + v);
                Class s = c;
                Field f = null;
                while (!s.getName().equals(cn)) {
                    s = s.getSuperclass();
                }
                try {
                    f = s.getDeclaredField(n);
                } catch (NoSuchFieldException e) {
                    f = null;
                    if (s == DBObject.class) {
                        throw new Exception("Cant place " + n);
                    }
                }

                f.setAccessible(true);
                if (t.equals("DBObject") || t.equals("Object")) {
                    if (v == null)
                        f.set(o, null);
                    else
                        f.set(o, getObject(Long.parseLong(v)));
                } else if (t.equals("String")) {
                    f.set(o, v);
                } else if (t.equals("byte")) {
                    f.setByte(o, Byte.parseByte(v));
                } else if (t.equals("int")) {
                    f.setInt(o, Integer.parseInt(v));
                } else if (t.equals("char")) {
                    f.setChar(o, v.charAt(0));
                } else if (t.equals("float")) {
                    f.setFloat(o, Float.parseFloat(v));
                } else if (t.equals("double")) {
                    f.setDouble(o, Double.parseDouble(v));
                } else if (t.equals("long")) {
                    f.setLong(o, Long.parseLong(v));
                } else if (t.equals("short")) {
                    f.setShort(o, Short.parseShort(v));
                } else {
                    throw new Exception("Bad type " + t + " in get property");
                }
                if (ps.isLast())
                    break;
            }
            ((DBObject) o).emerge();
        } catch (Exception e) {
            Mud.log(o, 10, e);
        }
        return o;
    }

    public static long saveObject(Object o) {
        try {
            Class c = o.getClass();
            //			if ( Proxy.isProxyClass(c)){
            //				o = ((DBOIC)Proxy.getInvocationHandler(o)).o;
            //			}
            long id;

            Mud.log(o, 50, "Saving object to database.");

            if (!(o instanceof DBObject)) {
                Long oid = (Long) toId.get(o);
                if (oid == null) {
                    id = nextID();
                    toId.put(o, new Long(id));
                } else {
                    id = oid.longValue();
                }
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                ObjectOutputStream os = new MyObjectOutputStream(bs);
                os.writeObject(o);
                os.close();
                insertObject.setBytes(3, bs.toByteArray());
            } else {
                id = ((DBObject) o).getObjectId();
                insertObject.setNull(3, Types.ARRAY);
            }

            removeProperty.setLong(1, id);
            removeProperty.execute();

            insertObject.setLong(1, id);
            insertObject.setString(2, c.getName());
            insertObject.execute();

            if (!(o instanceof DBObject))
                return id;

            DBObject dbo = (DBObject) o;

            //TODO: Properties need to be saved with their class name.
            //this should make loading easier too i hope.
            while (c != DBObject.class) {
                Field[] f = c.getDeclaredFields();
                AccessibleObject.setAccessible(f, true);
                for (int i = 0; i < f.length; i++) {
                    if (!Modifier.isStatic(f[i].getModifiers())
                            && !Modifier.isTransient(f[i].getModifiers())) {
                        Mud.log(DBObject.class, 100, f[i].getName() + " ("
                                + f[i].getType() + "): " + f[i].get(dbo));
                        insertProperty.setLong(1, dbo.getObjectId());
                        insertProperty.setString(2, f[i].getName());
                        insertProperty.setString(5, c.getName());
                        if (f[i].getType().isPrimitive()
                                || f[i].getType() == String.class) {
                            if (f[i].getType() == String.class)
                                insertProperty.setString(3, "String");
                            else
                                insertProperty.setString(3, f[i].getType()
                                        .toString());
                            if (f[i].get(dbo) != null)
                                insertProperty.setString(4, "" + f[i].get(dbo));
                            else
                                insertProperty.setNull(4, Types.VARCHAR);
                            insertProperty.execute();
                        } else if (f[i].get(dbo) instanceof DBObject) {
                            insertProperty.setString(3, "DBObject");
                            insertProperty.setString(4, ""
                                    + ((DBObject) f[i].get(dbo)).getObjectId());
                            insertProperty.execute();
                        } else if (f[i].get(dbo) != null
                                && Proxy.isProxyClass(f[i].get(dbo).getClass())) {
                            insertProperty.setString(3, "DBObject");
                            insertProperty.setString(4, ""
                                    + ((DBOIC) Proxy.getInvocationHandler(f[i]
                                            .get(dbo))).o.getObjectId());
                            insertProperty.execute();
                        } else {
                            if (f[i].get(dbo) != null) {
                                insertProperty.setString(4, ""
                                        + saveObject(f[i].get(dbo)));
                            } else {
                                insertProperty.setNull(4, Types.VARCHAR);
                            }
                            insertProperty.setString(3, "Object");
                            insertProperty.execute();
                        }

                    }
                }
                c = c.getSuperclass();
            }
            return dbo.getObjectId();
        } catch (Exception e) {
            Mud.log(o, 10, e);
            return 0;
        }
    }

    private static void updateTransients(Object oldObject, Object newObject) {
        Class c = oldObject.getClass();
        while (c != DBObject.class) {
            Field[] fs = c.getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                if (Modifier.isTransient(fs[i].getModifiers())) {
                    //For every transient feild in the old object
                    Class s = newObject.getClass();
                    Field f = null;
                    //Find the coresponding feild in the new object
                    while (f == null) {
                        try {
                            f = s.getDeclaredField(fs[i].getName());
                        } catch (NoSuchFieldException e) {
                            f = null;
                            s = s.getSuperclass();
                            if (s == DBObject.class) {
                                Mud.log(DBObject.class, 100, "Cant place "
                                        + fs[i].getName());
                                break;
                            }
                        }
                    }
                    //if the new object has the feild try to set it
                    if (newObject != null) {
                        try {
                            f.set(newObject, fs[i].get(oldObject));
                        } catch (Exception e) {
                            Mud.log(DBObject.class, 100, "Got " + e
                                    + " transfering transient "
                                    + fs[i].getName());
                        }
                    }
                }
            }
            c = c.getSuperclass();
        }
    }

}

