package mud.interfaces;

public interface Interactive {
	public void setSession(Session s);
	public void write(String s);
	public void writeln(String s);
	public void process(String s);
	public void setStats( String s );
	public void insertCommand( Command c );
	public void removeCommand( Command c );
}