package mud;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

import mud.action.Take;
import mud.commands.SimpleCommand;
import mud.interfaces.*;
import mud.interfaces.sensa.*;

public class Player extends BasicLiving implements Interactive, NotifyOnEnter,
        NotifyOnLeave, NotifyOnMove {
    private transient Session session = null;
    private HashMap playerCommands = new HashMap();
    private int state;
    private String password;
    protected int age = 0;

    private static class InventoryCommand extends SimpleCommand {
        public InventoryCommand() {
            super("inv");
        }

        public boolean process(MudObject who, String verb, String command) {
            Iterator i = ((Container)who).getContents().iterator();
            while ( i.hasNext() ){
                ((Interactive)who).write( i.next() + "\n");
            }
            
            return true;
        }
    }
    
    //Located agent;

    public Player() {

    }

    static public Player getByName(String name) {
        return null;
    }

    public void emerge() {
        setHeartbeat(1);
    }

    public void heartbeat() {
        age++;
        hp++;
        int days = age / (60 * 60 * 24);
        int hours = (age % (60 * 60 * 24)) / (60 * 60);
        int minutes = (age % (60 * 60)) / 60;
        int seconds = age % 60;
        setStats("Hp: " + hp + " Age: " + days + "d:" + hours + "h:" + minutes
                + "m:" + seconds);
        if ( location == null )
            setLocation(DBRoom.getEntry(),null);
    }

    public void newPlayer(String a) {
        int oldState = state;
        hp = 10;
        do {
            switch (state) {
            case 0:
                name = a;
                session.prompt("Choose password: ", this, "newPlayer");
                state++;
                break;
            case 1:
                password = a;
                session.prompt("Your gender? ", this, "newPlayer");
                state++;
                break;
            case 2:
                if (a.equalsIgnoreCase("m")) {
                    gender = MALE;
                    state++;
                } else if (a.equalsIgnoreCase("f")) {
                    gender = FEMALE;
                    state++;
                } else {
                    session.writeln("Sorry?");
                    state--;
                }
                break;
            }
        } while (state < oldState);
        if (state == 3) {
            //agent = new Agent();
            setHeartbeat(1);
            new mud.playerparts.Mouth().setLocation(this, null);
            new mud.playerparts.Hands().setLocation(this, null);
            new mud.playerparts.Eyes().setLocation(this, null);
            this.insertCommand(new InventoryCommand());
            
            session.writeln("New Player Done");
            setLocation(Mud.getEntry(), null);
            save();
            state++;
        }
    }

    //Sensing
    public void sense(Sensum s) {
       s.tell(this);
    }

    //Notifiable
    public void notifyEnter(MudObject o, Object exp) {
        String d = "";
        if (exp instanceof Described)
            d = " from the " + ((Described) exp).getShort();
        
        if ( exp instanceof Sensum )
            sense((Sensum)exp);
       else
            writeln(o + " enters" + d);
    }

    public void notifyLeave(MudObject o, Object exp) {
        String d = "";
        if (exp instanceof Described)
            d = " " + ((Described) exp).getShort();
        
        if ( exp instanceof Sensum )
            sense((Sensum)exp);
        else
            writeln(o + " leaves" + d);
    }

    public void notifyMove(Container c) {
        if (this instanceof Interactive && c instanceof Described) {
            ((Interactive) this).writeln(((Described) c).getShort());
            ((Interactive) this).writeln(((Described) c).getLong());
            if (c instanceof Place) {
                Iterator i = ((Place) c).getNear(this).iterator();
                String items = "";
                String exits = "";
                while (i.hasNext()) {
                    Object o = i.next();
                    if (this.equals(o))
                        continue;
                    if (o instanceof Exit) {
                        exits = exits + ((Described) o).getShort() + "\n";
                    } else if (o instanceof Described) {
                        items = items + ((Described) o).getShort() + "\n";
                    }
                }
                ((Interactive) this).write(exits);
                ((Interactive) this).write(items);
            }
        }
        //agent.setLocation(c, null);
    }

    //Interactive
    public void setSession(Session s) {
        session = s;
        if (s.getInteractive() != this)
            s.setInteractive(this);
    }

    public void write(String s) {
        if (session != null)
            session.write(s);
    }

    public void writeln(String s) {
        if (session != null)
            session.writeln(s);
    }

    public void setStats(String s) {
        if (session != null)
            session.setStats(s);
    }

    public void process(String s) {
        try {
            String verb;
            int space = s.indexOf(" ");
            if (space == -1)
                verb = s;
            else
                verb = s.substring(0, space);
            HashMap m = (HashMap) playerCommands.get(verb);
            Iterator i = m.keySet().iterator();
            while (i.hasNext()) {
                //Try catch in here so a broken command will be skipped
                try {
                    if (((Command) i.next()).process(this, verb, verb == s ? ""
                            : s.substring(verb.length() + 1, s.length())))
                        return;
                } catch (Exception e) {
                    Mud.log(this, 90, e);
                }
            }
        } catch (Exception e) {
            Mud.log(this, 90, e);
        }
        writeln("What?");
    }

    //Add a command to this player
    public void insertCommand(Command c) {
        if (c.getVerbs() instanceof Set) {
            Iterator v = ((Set) c.getVerbs()).iterator();
            while (v.hasNext()) {
                String verb = (String) v.next();
                //Find the map for the verb, create it if none
                HashMap m = (HashMap) playerCommands.get(verb);
                if (m == null) {
                    m = new HashMap();
                    playerCommands.put(verb, m);
                }
                //Find the refcount for this command, create it if none
                Integer i = (Integer) m.get(c);
                if (i == null) {
                    m.put(c, new Integer(1));
                } else {
                    m.put(c, new Integer(i.intValue() + 1));
                }
            }
        } else if (c.getVerbs() instanceof String) {
            String verb = (String) c.getVerbs();
            HashMap m = (HashMap) playerCommands.get(verb);
            if (m == null) {
                m = new HashMap();
                playerCommands.put(verb, m);
            }
            //Find the refcount for this command, create it if none
            Integer i = (Integer) m.get(c);
            if (i == null) {
                m.put(c, new Integer(1));
            } else {
                m.put(c, new Integer(i.intValue() + 1));
            }
        }
    }

    //remove a command from this player
    public void removeCommand(Command c) {
        if (c.getVerbs() instanceof Set) {
            Iterator v = ((Set) c.getVerbs()).iterator();
            while (v.hasNext()) {
                String verb = (String) v.next();
                //Find the map for the verb
                HashMap m = (HashMap) playerCommands.get(verb);
                if (m != null) {
                    //Find the refcount for this command
                    Integer i = (Integer) m.get(c);
                    if (i != null) {
                        //decrement the count, remove command if zero
                        int cnt = i.intValue();
                        cnt--;
                        if (cnt == 0) {
                            m.remove(c);
                        } else {
                            m.put(c, new Integer(cnt));
                        }
                    }
                }
            }
        } else if (c.getVerbs() instanceof String) {
            String verb = (String) c.getVerbs();
            HashMap m = (HashMap) playerCommands.get(verb);
            if (m != null) {
                //Find the refcount for this command
                Integer i = (Integer) m.get(c);
                if (i != null) {
                    //decrement the count, remove command if zero
                    int cnt = i.intValue();
                    cnt--;
                    if (cnt == 0) {
                        m.remove(c);
                    } else {
                        m.put(c, new Integer(cnt));
                    }
                }
            }
        }
    }

}