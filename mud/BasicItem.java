/*
 * Created on Oct 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mud;
import mud.interfaces.*;

/**
 * @author kukester
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BasicItem extends BasicLocated implements Described, Named, Carryable {

    protected Container location;
    protected String name;
    protected String shortDesc;
    
    public BasicItem(String n) {
        super();
        name = n;
        shortDesc = "A " + name;
    }
    
    //Carryable
    public float getWeight(){
        return 0.0f;
    }

    //Described
    public String getShort() {
        return shortDesc;
    }


    public String getLong() {
        return getShort();
    }

    //Named  
    public String getName() {
        return name;
    }

    public boolean isName(String n) {
        return name.toLowerCase().lastIndexOf(n.toLowerCase()) != -1;
        //return n.equalsIgnoreCase(getName());
    }

}
