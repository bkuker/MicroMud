package mud.wizard;
import mud.Mud;
import mud.commands.SimpleCommand;
import mud.interfaces.MudObject;

/*
 * Created on Oct 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */



public class SaveCommand extends SimpleCommand{
	public SaveCommand(){
		super("save");
	}
	public boolean process(MudObject o, String v, String c){
		Mud.save();
		return true;
	}
}