package WizardUI;
import javax.swing.*;
import java.io.*;
import java.net.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.Iterator;

public class WizardUI extends JFrame implements Runnable {
	private JTabbedPane tabs;
	private World world;
	ObjectInputStream in;		
	ObjectOutputStream out;
	DefaultTreeModel tm;
	JTree tree;
	HashMap nodeToDisc = new HashMap();
	HashMap discToNode = new HashMap();
	
	private class ClassEditor extends JSplitPane{
			private JTextArea src, errors;
			public ClassEditor(){
				super(VERTICAL_SPLIT);
				add(new JScrollPane(errors = new JTextArea("Err",8,80)), BOTTOM);
				add(new JScrollPane(src = new JTextArea("Src",30,80)), TOP);
			}
	}
	
	private class World extends JSplitPane{
		//private JTree tree;
		private JTextArea t;
		public World(){
			add(new JScrollPane(t = new JTextArea(20,30)),LEFT);
			add(new JScrollPane(tree = new JTree()),RIGHT);
			tree.addTreeWillExpandListener(new TL());
		}
		public void write(String s){
			t.append(s);
		}
	}
	
	private class TL implements TreeWillExpandListener {
		public void treeWillCollapse(TreeExpansionEvent ee) throws ExpandVetoException {
		}
		public void treeWillExpand(TreeExpansionEvent ee) throws ExpandVetoException {
			Object node = ee.getPath().getLastPathComponent();
			try{
				out.writeObject(nodeToDisc.get(node));
			} catch (Exception e){}
		}
	}
	
	private class ValNode{
		Object k,v;
		ODiscriptor o;
		public ValNode(ODiscriptor _o, Object _k, Object _v){
			o = _o; k = _k; v = _v;
		}
		public String toString(){
			return k.toString() + " = " + (v==null?"null":v.toString());
		}
	}
	
	private class ArrayNode{
		Object k,v;
		ODiscriptor o;
		int index;
		public ArrayNode(ODiscriptor _o, Object _k, int _i, Object _v){
			o = _o; k = _k; v = _v; index = _i;
		}
		public String toString(){
			return "[" + index + "] " + (v==null?"null":v.toString());
		}
	}
	
	public WizardUI() {
		super();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		tabs = new JTabbedPane();
		this.getContentPane().add(tabs);
		tabs.add("World", world = new World());
		tabs.add("Edit", new ClassEditor());
		tm = (DefaultTreeModel)tree.getModel();
		setSize(640,480);
		doLayout();
		new Thread(this).start();
		show();
	}
	
	public void dispatch(Object o){
		if ( o instanceof Notification ){
			Notification n = (Notification)o;
			if (n.event == Notification.MOVE){
				DefaultMutableTreeNode r = new DefaultMutableTreeNode(n.o);
				r.add(new DefaultMutableTreeNode("temp"));
				tm.setRoot(r);
				nodeToDisc.put(r,n.o);
				discToNode.put(new Integer(n.o.hashCode()),r);
				tm.nodeStructureChanged(r);
				tree.collapsePath(new TreePath(r));
			}
		}
		if ( o instanceof ODiscriptor ){
			ODiscriptor d = (ODiscriptor)o;
			DefaultMutableTreeNode n = (DefaultMutableTreeNode)discToNode.get(new Integer(d.hashCode()));
			n.removeAllChildren();
			HashMap m = d.getAttrs();
			Iterator i = m.keySet().iterator();
			while( i.hasNext() ){
				Object k = i.next();
				Object a = m.get(k);
				DefaultMutableTreeNode an;
				if ( a instanceof ODiscriptor ){
					an = new DefaultMutableTreeNode(new ValNode(d,k,a));
					nodeToDisc.put(an,a);
					discToNode.put(new Integer(a.hashCode()),an);
					an.add(new DefaultMutableTreeNode(null));
				} else if ( a != null && a.getClass().isArray()){
					an = new DefaultMutableTreeNode(k);
					ODiscriptor l[] = (ODiscriptor[])a;
					for ( int j = 0; j < l.length; j++){
						DefaultMutableTreeNode a2;
						an.add(a2 = new DefaultMutableTreeNode(new ArrayNode(d,k,j,l[j])));
						if ( l[j].hashcode == 0 )
							continue;
						nodeToDisc.put(a2,l[j]);
						discToNode.put(new Integer(l[j].hashCode()),a2);
						a2.add(new DefaultMutableTreeNode(null));
					}
				} else {
					an = new DefaultMutableTreeNode(new ValNode(d,k,a));
					an.setAllowsChildren(false);
				}
				n.add(an);
			}
			tm.nodeStructureChanged(n);
		}
	}
	
	public void run(){
		while(true){
			try{
				Socket s = new Socket("localhost",9999);
				out = new ObjectOutputStream(s.getOutputStream());
				in = new ObjectInputStream(s.getInputStream());		
				while(true){
					Object o = in.readObject();
					world.write(o.toString()+"\n");
					if ( o instanceof AgentEvent || o instanceof ODiscriptor )
						dispatch(o);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}	
	}

	public static void main(String[] args) {
		new WizardUI();
	}
}	
