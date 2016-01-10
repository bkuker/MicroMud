package mud.interfaces;
/*
 * Created on Jun 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author kukester
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface MudObject {
	public long getObjectId() throws Exception;

	public void save();

	public void upgrade();
	
	public void heartbeat();
	
	void addCommands( Interactive i );

	void removeCommands( Interactive i );
	
	//public boolean is(Object o);
	
}