package mud;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import mud.interfaces.Place;

public class DBExit extends BasicExit {
   
    static PreparedStatement getExit;
    
    static{
		try{
		    getExit = Mud.con.prepareStatement(
					"SELECT room_id_from, room_id_to, direction from is_mud_world.is_mud_room_exit where id = ?" );
		}catch (Exception s){
			s.printStackTrace();
			System.exit(-1);
		}
	}
    
    public void emerge(){
        super.emerge();
        loadExit();
    }
    
    public DBExit() {
        super();
    }

    private void loadExit(){
        try{
            getExit.setLong(1, getObjectId());
			ResultSet rs = getExit.executeQuery();
			if ( !rs.next() ){
				Mud.log(this, 100, "Cant find Exit " + getObjectId());
				return;
			}
			//shortDesc = rs.getString(1);
			//longDesc = rs.getString(2);
			from = (Place)getObject( rs.getLong(1) );
			to = (Place)getObject( rs.getLong(2) );
			dir = rs.getString(3);
			

        } catch (Exception e) {
            Mud.log(this, 10, e);
        }
    }

}
