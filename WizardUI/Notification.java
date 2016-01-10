package WizardUI;

public class Notification extends AgentEvent {
	public static final int ENTER = 0;
	public static final int LEAVE = 1;
	public static final int MOVE = 2;
	
	public ODiscriptor o, exp, loc;
	int event;
	public Notification(Object _o, Object _exp, int _event) {
		super();
		o = new ODiscriptor(_o);
		exp = new ODiscriptor(_exp);
		event = _event;
		if (_o instanceof mud.interfaces.Located )
			loc = new ODiscriptor(((mud.interfaces.Located)_o).getLocation());
		else
			loc = null;
	}

}
