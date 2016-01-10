package mud;

import mud.interfaces.Reloadable;
import mud.interfaces.Place;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.HashSet;

import mud.interfaces.Exit;

public class DBRoom extends Room implements Reloadable{
    protected transient Set exits = new HashSet();
    protected String name;
    
    static PreparedStatement getRoom;
    
    static{
		try{
		    getRoom = Mud.con.prepareStatement(
					"SELECT short, long, name from is_mud_world.is_mud_room where id = ?" );
		 
		}catch (Exception s){
			s.printStackTrace();
			System.exit(-1);
		}
	}
    
    public static Place getEntry(){
        try{
            PreparedStatement getEntry = Mud.con.prepareStatement(
            	"SELECT id from is_mud_world.is_mud_room where area_id = 0 and name = 'Entry'" );
            ResultSet rs = getEntry.executeQuery();
			if ( !rs.next() ){
				Mud.log(DBRoom.class, 1, "Cant find entry");
				return null;
			}
			long id = rs.getLong(1);
			return (Place)(DBObject.getObject(id));
        } catch ( Exception e ) {
            Mud.log(DBRoom.class, 1, e);
        }
        return null;
    }
    
    public DBRoom(){}
    
    public void emerge(){
        super.emerge();
        if ( items == null )
            items = new HashSet();
        insert( new mud.wizard.ArchOrb(), null );
        reload();
    }
    
    public void reload(){
        try{
            getRoom.setLong(1, getObjectId());
			ResultSet rs = getRoom.executeQuery();
			if ( !rs.next() ){
				Mud.log(this, 100, "Cant find Room " + getObjectId());
				return;
			}
			shortDesc = rs.getString(1);
			longDesc = rs.getString(2);
			name = rs.getString(3);
			
		    PreparedStatement getExits = Mud.con.prepareStatement(
            "Select id from is_mud_world.is_mud_room_exit where room_id_from = ?");
			
			getExits.setLong(1, getObjectId());
			rs = getExits.executeQuery();
			while ( rs.next() ){
			    Object o = getObject( rs.getLong(1));
			    if ( !getContents().contains(o) && o instanceof Exit )
			        addExit( (Exit)o );
			}
        } catch (Exception e) {
            Mud.log(this, 10, e);
        }
    }
     
}
