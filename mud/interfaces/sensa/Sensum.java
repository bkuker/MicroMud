/*
 * Created on Oct 3, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mud.interfaces.sensa;
import mud.interfaces.MudObject;
import mud.interfaces.Interactive;

/**
 * @author kukester
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface Sensum {
    public MudObject getSource();	//What caused this sensum
    
    public Object getData();	//More data the formatter might find handy

    public void tell( Interactive i );
}
