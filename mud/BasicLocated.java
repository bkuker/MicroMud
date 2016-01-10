package mud;
import mud.interfaces.*;

public class BasicLocated extends Corporeal implements Located {

	public Container location = null;

	public Container getLocation() {
		return location;
	}
	
	public void setLocation( Container c, Object exp ) {
		if ( location == c )
			return;
		if(location != null){
			Container old = location;
			location = null;
			old.remove(this, exp);
		}
		location = c;
		if ( exp instanceof Exit )
			exp = ((Exit)exp).getTwin();
		if ( location != null ){
			location.insert(this, exp);
			if ( this instanceof NotifyOnLeave ){
				((NotifyOnMove)this).notifyMove(c);
			}
		}
	}
}
