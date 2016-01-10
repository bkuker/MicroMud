
package mud.interfaces;

import mud.interfaces.sensa.Sensing;

/*
 * Created on Oct 18, 2004
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
public interface Living extends Container, Described, Named, Sensing, MudObject{
    public static final int NEUTER = 0;

    public static final int MALE = 1;

    public static final int FEMALE = 2;

    public abstract void say(String s);

    public abstract void tell(Living to, String s);

    public abstract boolean canAttack(Living attacker);
}