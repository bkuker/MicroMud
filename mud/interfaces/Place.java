package mud.interfaces;
import java.util.Set;


/*
 * Created on Jun 24, 2004
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
public interface Place extends Container, MudObject {
	public abstract Set getNear(Located me);
	public Set findByName(String name, Located where);
}