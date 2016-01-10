package mud.interfaces;
public interface Command {
	//This MUST return either a string or a Set.
	public Object getVerbs();
	public boolean process(MudObject who, String verb, String command);
}
