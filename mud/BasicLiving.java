

package mud;
import java.util.HashSet;

import java.util.Set;
import java.util.Iterator;

import mud.action.Speak;
import mud.interfaces.*;
import mud.interfaces.sensa.*;

public class BasicLiving extends BasicLocated implements Living {
    protected int gender = NEUTER;
    protected String name;
    protected String title;
    protected String description;
    protected Set inventory = new HashSet();
    protected int hp;

    public void say(String s) {
        Container l = getLocation();
        if (l instanceof Sensing)
            ((Sensing) l).sense(new Speak(this, (MudObject) l, s));
    }
    
    public void tell(Living to, String s){
        if ( to instanceof Sensing )
            ((Sensing)to).sense(new Speak(this, to, s));
    }

    public boolean canAttack(Living attacker) {
        return false;
    }

    public Set getContents() {
        return inventory;
    }

    public Set findByName(String name) {
        Set ret = new HashSet();
        if ( inventory == null )
            return ret;
        Iterator i = inventory.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof Named) {
                if (((Named) o).isName(name)) {
                    ret.add(o);
                }
            }
            if (o instanceof TransparentContainer) {
                ret.addAll(((TransparentContainer) o).findByName(name));
            }
        }
        if (ret == null) {
            Container l = getLocation();
            if (l != null)
                ret = l.findByName(name);
        }
        return ret;
    }

    public void insert(Located l, Object exp) {
		if ( !this.equals(l.getLocation()) )
			l.setLocation(this, exp);
        inventory.add(l);
        if ( this instanceof Interactive )
            l.addCommands((Interactive)this);
    }

    public void remove(Located l, Object exp) {
    	l.setLocation(null, exp);
        inventory.remove(l);
        if ( this instanceof Interactive )
            l.removeCommands((Interactive)this);
    }

    public String getShort() {
        if (title == null)
            return name;
        else
            return name + " " + title;
    }

    public String getLong() {
        return description;
    }

    public String getName() {
        return name;
    }

    public boolean isName(String n) {
        return name.equalsIgnoreCase(n);
    }

    public void sense(Sensum s) {

    }

}