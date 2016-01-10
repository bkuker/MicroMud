package mud;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import mud.interfaces.*;
import mud.interfaces.sensa.*;

public class BasicPlace extends Corporeal implements Container, Described, Place, Sensing {
	protected String longDesc;
	protected String shortDesc;
	protected Area myArea;

	public Set getNear(Located me) {
		return new java.util.HashSet();
	}
	
	public Set findByName(String name, Located me) {
		Set ret = new HashSet();
		Iterator i = getNear(me).iterator();
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
		return ret;
	}

	//Described
	public String getShort() {
		return shortDesc;
	}

	public String getLong() {
		return longDesc;
	}

	//Container
	public Set findByName(String name) {
		Set ret = new HashSet();
		Iterator i = getContents().iterator();
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
		return ret;
	}
	
	public Set getContents(){
	    return new java.util.HashSet();
	}

	public void insert(Located l, Object exp) {
	//	if ( this.is(l.getLocation()))
			//return;
		if ( !this.equals(l.getLocation()) )
			l.setLocation(this, exp);
		Set s = getNear(l);
		Iterator i = s.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof NotifyOnEnter && o != l)
				try {
					((NotifyOnEnter) o).notifyEnter(l, exp);
				} catch (Exception e) {
					Mud.log(o, 110, e);
				}
			if (l instanceof Interactive && o instanceof MudObject)
				try {
					((MudObject) o).addCommands((Interactive) l);
				} catch (Exception e) {
					Mud.log(o, 110, e);
				}
			if (o instanceof Interactive && l instanceof MudObject) {
				try {
					((MudObject) l).addCommands((Interactive) o);
				} catch (Exception e) {
					Mud.log(l, 110, e);
				}
			}
		}
	}

	public void remove(Located l, Object exp) {
		//if ( l.getLocation() != this )
			//return;
		l.setLocation(null, exp);
		Set s = getNear(l);
		Iterator i = s.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (o instanceof NotifyOnLeave && o != l)
				try {
					((NotifyOnLeave) o).notifyLeave(l, exp);
				} catch (Exception e) {
					Mud.log(o, 110, e);
				}
			if (l instanceof Interactive && o instanceof MudObject)
				try {
					((MudObject) o).removeCommands((Interactive) l);
				} catch (Exception e) {
					Mud.log(o, 110, e);
				}
			if (o instanceof Interactive && l instanceof MudObject) {
				try {
					((MudObject) l).removeCommands((Interactive) o);
				} catch (Exception e) {
					Mud.log(l, 110, e);
				}
			}
		}
	}

	//Sensing
	
    /* (non-Javadoc)
     * @see Interfaces.Sensa.Sensing#sense(Interfaces.Sensa.Sensum)
     */
    public void sense(Sensum sn) {
        Set s = null;
        if ( sn.getSource() instanceof Located ){
           s = getNear((Located)sn.getSource());
        } else {
           s = getContents();
    	}
        
        Iterator i = s.iterator();
        while (i.hasNext()){
            Object o = i.next();
            if ( o instanceof Sensing ){
                ((Sensing)o).sense(sn);
            }
        }
        
    }
}
