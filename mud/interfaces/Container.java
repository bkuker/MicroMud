package mud.interfaces;
import java.util.Set;

public interface Container extends MudObject{
	public Set findByName(String name);
	public Set getContents();
	public void insert(Located l, Object exp);
	public void remove(Located l, Object exp);
}
