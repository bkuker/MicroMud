package mud;
import java.util.HashSet;
import java.util.Set;

import mud.interfaces.Exit;
import mud.interfaces.Located;
import mud.interfaces.Place;


public class Room extends BasicPlace {
	protected Set items = new HashSet();

	public Room() {

	}

	public Set getNear(Located me) {
		return items;
	}

	//Container
	public Set getContents(){
	    return items;
	}
	
	public void insert(Located l, Object exp) {
		items.add(l);
		super.insert(l, exp);
	}

	public void remove(Located l, Object exp) {
		items.remove(l);
		super.remove(l, exp);
	}
	
	//Room
	protected void addExit(String dir, Place to) {
		addExit(new BasicExit(dir, this, to));
	}

	protected void addExit(Exit e) {
		insert((Located)e, null);
	}
}
