package mud;

import java.sql.*;

import mud.interfaces.*;

public class MudLogin {
	static PreparedStatement getPlayer;

	static {
		try {
			getPlayer =
				Mud.con.prepareStatement(
					"SELECT p.value, id FROM java.object o, java.property n, java.property p "
						+ "WHERE o.class ='mud.Player' and n.name = 'name' and p.name='password' "
						+ "and o.id = n.object_id and o.id = p.object_id and n.value=?");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	

	protected Session s;
	private String name;
	private String pass;
	private long id;
	private int state = 0;

	public MudLogin(Session _s) {
		s = _s;
		s.prompt("Name: ", this, "name");
	}

	public void name(String n) {
		name = n;
		try {
			getPlayer.setString(1, name);

			ResultSet or = getPlayer.executeQuery();
			if (!or.next()) {
				Mud.log(this, 120, "Cant find player " + name);
				s.writeln("No Player " + name + " found.");
				s.prompt("Create a new player named "+name+"? ", this, "newPlayerQ");
			} else {
				pass = or.getString(1);
				id = or.getLong(2);
				s.prompt("Password: ", this, "pass", false);
			}
		} catch (SQLException e) {
			Mud.log(this, 10, e);
		}
	}

	public void pass(String p) {
		if (p.equals(pass)) {
			//Player is A-O-K
			s.writeln("Welcome " + name);
			Interactive pl = (Interactive)DBObject.getObject(id);
			pl.setSession(s);
		} else {
			//Password is no good.
			s.writeln("Incorrect Password");
			new MudLogin(s);
		}

	}

	public void newPlayerQ(String a) {
		if (a.equalsIgnoreCase("yes") || a.equalsIgnoreCase("y")) {
			s.writeln("Creating " + name + "...");
			Player p = new Player();
			p.setSession(s);
			p.newPlayer(name);
		} else {
			new MudLogin(s);
		}
	}
	

}
