package mud.interfaces;
public abstract interface Session{
	public void write(String s);
	public void writeln(String s);
	public void setStats( String s );
	public void prompt( String prompt, Object rec, String func );
	public void prompt( String prompt, Object rec, String func, boolean echo );
	public Interactive getInteractive() ;
	public void setInteractive(Interactive interactive);
}
