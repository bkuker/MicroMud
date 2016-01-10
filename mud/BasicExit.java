package mud;
import mud.interfaces.Command;
import mud.interfaces.Described;
import mud.interfaces.Exit;
import mud.interfaces.Located;
import mud.interfaces.MudObject;
import mud.interfaces.Place;

public class BasicExit extends BasicLocated implements Command, Described, Exit {
	protected String dir;
	protected Place from, to;
	protected Exit twin;
	
	public BasicExit(){};
	
	public BasicExit( String d, Place f, Place t){
		from = f;
		to = t;
		dir = d;
	}
	
	public Object getVerbs() {
		return dir;
	}

	public boolean process(MudObject who, String verb, String command) {
		Mud.log(this, 90, "Exit called");
		if ( who instanceof Located ){
			return( take((Located)who));
		}
		return false;
	}
	
	public boolean take(Located l) {
		l.setLocation(to,this);
		return true;
	}

	//Described
	public String getShort() {
		return dir;
	}

	public String getLong() {
		return dir;
	}

	public void setTwin(Exit e) {
		twin = e;
	}

	public Exit getTwin() {
		return twin;
	}

}
