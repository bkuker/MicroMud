package mud.interfaces;


public interface NotifyOnLeave extends Notifiable {
	public void notifyLeave(MudObject o, Object exp);
}