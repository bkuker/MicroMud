package mud;
import java.util.HashMap;
import java.sql.*;
import java.io.*;

import mud.exceptions.CompilationException;

public class DBClassHandle{
	private static HashMap classes = new HashMap();
	static PreparedStatement insert;
	static PreparedStatement getDates;
	static PreparedStatement getSource;
	static PreparedStatement getCode;
	static PreparedStatement setSource;
	static PreparedStatement setCode;
	
	private static class CantFindException extends Exception{}
	
	static{
		try{
			insert = Mud.con.prepareStatement(
					"INSERT INTO java.class (name) values(?)" );
			getDates = Mud.con.prepareStatement(
					"SELECT sourcedate,codedate FROM java.class WHERE name = ?" );
			getSource = Mud.con.prepareStatement(
					"SELECT source FROM java.class WHERE name = ?" );
			getCode = Mud.con.prepareStatement(
					"SELECT code FROM java.class WHERE name = ?" );
			setSource = Mud.con.prepareStatement(
					"UPDATE java.class SET source = ? WHERE name = ?" );
			setCode = Mud.con.prepareStatement(
					"UPDATE java.class SET code = ? WHERE name = ?" );
		} catch (Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

	//////////////////////////////////////////////////////////////////
	//Non Static
	String name;
	String source;
	byte[] code;
	java.sql.Date sdate;
	java.sql.Date cdate;
	Class c;

	//Set the source code for this class
	public void setSource(String s){
		source = s;
	}
 
	public String getName(){
		return name;
	}
	
	public Object newInstance() throws InstantiationException {
		try{
			if ( c == null )
				c = Mud.getClassLoader().loadClass(name, true);
			return c.newInstance();
		} catch (Exception e){
			throw new InstantiationException();
		}
	}

	//Compile the class, returning any errors as a string.
	//Note that this will not affect any new instances
	//untill reload() is called.
	public void compile() throws CompilationException{
		try{
			File f = new File("tmp/" + name + ".java");
			FileWriter o = new FileWriter(f);
			o.write(source);
			o.close();
			Process p = Runtime.getRuntime().exec("javac tmp/"+name+".java");
			BufferedReader i = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String err = "", l;
			while ( null != (l = i.readLine()) )
				err = err + "\n" + l;
			p.waitFor();
			f.delete();
			if ( 0 != p.exitValue() ){
				throw new CompilationException(err);
			}
			File c = new File("tmp/" + name + ".class");
			code = Mud.loadFile(c);
			c.delete();
			saveSource();
			saveCode();
		} catch (Exception e){
			throw new CompilationException(e);
		}
	}

	//Call this to "reload" the class, this will
	//affect all new instances
	public void reload(){
		c = null;
	}
	
	private DBClassHandle(String name, boolean create) throws SQLException, CantFindException{
		this.name = name;
		try{
			loadSource();
			loadCode();
		} catch (SQLException e){
			e.printStackTrace();
		} catch (CantFindException e){
			if ( !create )
				throw e;
			insert.setString(1,name);
			insert.execute();
		}
		Mud.log(this, 100, "New classhandle for "+name);
		classes.put(name, this);
	}

	private void loadSource() throws SQLException, CantFindException{
		getSource.setString(1, name);
		ResultSet or = getSource.executeQuery();
		if ( !or.next() ){
			Mud.log(this, 120000, "Cant find source for " + name);
			throw new CantFindException();
		}
		source = or.getString(1);
	}

	private void saveSource() throws SQLException{
		setSource.setString(1,source);
		setSource.setString(2,name);
		setSource.execute();
	}

	private void saveCode() throws SQLException{
		setCode.setBytes(1,code);
		setCode.setString(2,name);
		setCode.execute();
	}

	private void loadCode() throws SQLException, CantFindException{
		getCode.setString(1, name);
		ResultSet or = getCode.executeQuery();
		if ( !or.next() ){
			Mud.log(this, 120, "Cant find code for " + name);
			throw new CantFindException();
		}
		code = or.getBytes(1);
	}


	/////////////////////////////////////////////////////////////////////
	//Static
	public static DBClassHandle getDBClassHandle(String name){
		return getDBClassHandle(name, false);
	}

	public static DBClassHandle getDBClassHandle(String name, boolean create){
		DBClassHandle h = (DBClassHandle)classes.get(name);
		if ( h == null ){
			try{
				h = new DBClassHandle(name, create);
			} catch ( SQLException e ){
				e.printStackTrace();
				return null;
			} catch ( CantFindException e ){
				return null;
			}
			classes.put(name,h);
		}
		return h;
	}
}


