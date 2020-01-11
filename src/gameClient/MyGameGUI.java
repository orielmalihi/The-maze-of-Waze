package gameClient;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;

import Server.Fruit;
import Server.Game_Server;
import Server.game_service;
import algorithms.Graph_Algo;
import dataStructure.DGraph;
import dataStructure.Vertex;
import dataStructure.edge_data;
import dataStructure.graph;
import dataStructure.node_data;
import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_graph;
import oop_utils.OOP_Point3D;
import utils.Point3D;
import utils.Range;
/**
 * this class represents a GUI for the Graph_algo class.
 * it enables the user to build a new radom graph or to build a new custum graph
 * and to run on the graph the algorithms from the Graph_algo class.
 * @author oriel
 *
 */

public class MyGameGUI extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
//	private DGraph dg;
	private graph g;
	private Graph_Algo algo;
	private Range rx = new Range(Integer.MAX_VALUE,Integer.MIN_VALUE);
	private Range ry = new Range(Integer.MAX_VALUE,Integer.MIN_VALUE);
	private ArrayList<String> fruits;
	private ArrayList<String> robots;
	private boolean afterAdapt = false;
	private int kRADIUS = 5;
	private ArrayList<node_data> targets = new ArrayList<node_data>();

	public MyGameGUI() {
		initGUI();
	}

	public MyGameGUI(graph g) {
		initGUI();
		this.g = g;
		algo.init(this.g);
	}

	private void setRange() {
		Collection<node_data> c = g.getV();
		Iterator<node_data> itrV = c.iterator();
		while(itrV.hasNext()) {
			node_data n = itrV.next();
			Point3D p = n.getLocation();
			double x = p.x();
			double y = p.y();
			if(x<rx.get_min())
				rx.set_min(x);
			else if(x>rx.get_max())
				rx.set_max(x);
			if(y<ry.get_min())
				ry.set_min(y);
			else if(y>ry.get_max())
				ry.set_max(y);
		}
	}

	private void adaptRangeToGUI() {
		setRange();
		System.out.println("fixed rx: "+rx);
		System.out.println("fixed ry: "+ry);
		Collection<node_data> c = g.getV();
		Iterator<node_data> itrV = c.iterator();
		while(itrV.hasNext()) {
			node_data n = itrV.next();
			Point3D pBefore = n.getLocation();
			double offsetx = (pBefore.x() - rx.get_min())/(rx.get_max() - rx.get_min());
			double x = 1000 * offsetx + 100; 
			double offsety = (pBefore.y() - ry.get_min())/(ry.get_max() - ry.get_min());
			double y = 400 * offsety;
			y = (400 - y) + 100;
			Point3D pAfter = new Point3D(x, y);
			node_data fixedn = new Vertex(pAfter, n.getKey());
//			System.out.println("Pbefore: "+pBefore+",Pafter: "+pAfter);
//			System.out.println("fixedn: "+fixedn);
			DGraph dirG = (DGraph)g;
			dirG.fixNodeScale(fixedn);
			g = dirG;
		}
		if(g.nodeSize()>0)
			afterAdapt = true;

	}

	private void initGUI() {
		this.setSize(1200, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Menu");
		menuBar.add(menu);
		this.setMenuBar(menuBar);

		MenuItem item1 = new MenuItem("Save Graph");
		item1.addActionListener(this);

		MenuItem item2 = new MenuItem("Load Graph");
		item2.addActionListener(this);

		MenuItem item3 = new MenuItem("New Custom Game");
		item3.addActionListener(this);

		MenuItem item4 = new MenuItem("New Auto Game");
		item4.addActionListener(this);

		menu.add(item1);
		menu.add(item2);
		menu.add(item3);
		menu.add(item4);


		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		g = new DGraph();
		algo = new Graph_Algo(g);

	}
	
	public void repaint() {
		Graphics k;
		k = getGraphics();
		k.clearRect(0, 0, 1200, 600);
		paint(k);
	}


	public void paint(Graphics k) {
		System.out.println("paint started!");
		super.paint(k);
		if(!afterAdapt) {
			adaptRangeToGUI();
		}
		Font font = k.getFont().deriveFont((float) 16.5);
		k.setFont(font);
		Collection<node_data> c1 = g.getV();
		Iterator<node_data> itrV = c1.iterator();
		while(itrV.hasNext()) {
			node_data n = itrV.next();
			Point3D p = n.getLocation();
			k.setColor(Color.BLUE);
			k.fillOval((int)p.x() - kRADIUS, (int)p.y() - kRADIUS, 2 * kRADIUS, 2 * kRADIUS);
			k.drawString(n.getKey()+"", (int)p.x() - kRADIUS, (int)p.y() - kRADIUS-2);
			Collection<edge_data> c2 = g.getE(n.getKey());
			Iterator<edge_data> itrE = c2.iterator();
			k.setColor(Color.RED);
			while(itrE.hasNext()) {
				edge_data e = itrE.next();
				Point3D ps = g.getNode(e.getSrc()).getLocation();
				Point3D pf = g.getNode(e.getDest()).getLocation();
				k.drawLine(ps.ix(), ps.iy(), pf.ix(), pf.iy());
				k.setColor(Color.YELLOW);
				int xdir = (int)(0.8*pf.x() + 0.2*ps.x());
				int ydir = (int)(0.8*pf.y() + 0.2*ps.y());
				k.fillOval(xdir - kRADIUS , ydir - kRADIUS , 2 * kRADIUS, 2 * kRADIUS);
				xdir = (int)(0.7*pf.x() + 0.3*ps.x());
				ydir = (int)(0.7*pf.y() + 0.3*ps.y()-4);
				k.setColor(Color.RED);
				k.drawString(String.format("%.2f", e.getWeight()), xdir, ydir);
			}
		}
//		this.notify();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();

		if (str.equals("Save Graph")) {
			FileDialog chooser = new FileDialog(this, "Save your Graph", FileDialog.SAVE);
			chooser.setVisible(true);
			String filename = chooser.getFile();
			String path = chooser.getDirectory();
			System.out.println(filename);
			if(filename!=null) {
				algo.init(g);
				algo.save(path + filename +".txt");
			}
		} else if (str.equals("Load Graph")) {
			FileDialog chooser = new FileDialog(this, "Load your Graph", FileDialog.LOAD);
			chooser.setVisible(true);
			String filename = chooser.getFile();
			String path = chooser.getDirectory();
			if(filename!=null) {
				algo.init(path + filename);
				g = algo.copy();
				repaint();
			}
		}
		else if(str.equals("New Custom Game")) {
			myGame();
		}
		else if(str.equals("New Auto Game")) {
			myGame();
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println("mouseClicked");
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		node_data toChoose = null;
		Point3D temp = new Point3D(x,y);
		double min_dist = (kRADIUS * 3);
		double best_dist = 100000;
		Collection<node_data> c = g.getV();
		Iterator<node_data> itr = c.iterator();
		while(itr.hasNext()) {
			node_data n = itr.next();
			Point3D p = n.getLocation();
			double dist = temp.distance2D(p);
			if(dist<min_dist && dist<best_dist) {
				best_dist = dist;
				toChoose = n;
			}
		}

		repaint();

		System.out.println("mousePressed");
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		System.out.println("mouseReleased");
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		System.out.println("mouseEntered");

	}

	@Override
	public void mouseExited(MouseEvent e) {
		System.out.println("mouseExited");
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {

	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {

	}


	public graph getGraph() {
		return g;
	}

	public static void main(String[] args) {
		MyGameGUI gameGUI = new MyGameGUI();
		gameGUI.setVisible(true);	
	}

	public synchronized void myGame() {
		int scenario_num = 2;
//		int scenario_num = Integer.parseInt(JOptionPane.showInputDialog("Enter senario number between 0-23"));
		game_service game = Game_Server.getServer(scenario_num); // you have [0,23] games
		String gr = game.getGraph();
		DGraph dg = new DGraph();
		dg.init(gr);
		this.g = dg;
		System.out.println("g before repaint: "+g);
		repaint();
		String info = game.toString();
		JSONObject line;
		try {
			line = new JSONObject(info);
			JSONObject ttt = line.getJSONObject("GameServer");
			int rs = ttt.getInt("robots");
			System.out.println(info);
			System.out.println(g);
			// the list of fruits should be considered in your solution
			Iterator<String> f_iter = game.getFruits().iterator();
			while(f_iter.hasNext()) {
				JSONObject fruits_line = new JSONObject(f_iter.next());
				//				System.out.println(f_iter.next());
				JSONObject f = fruits_line.getJSONObject("Fruit");
				double val = f.getDouble("value");
				int type = f.getInt("type");
				String pos = f.getString("pos");
				OOP_Point3D p = new OOP_Point3D(pos);
			}
			int src_node = 0;  // arbitrary node, you should start at one of the fruits
			for(int a = 0;a<rs;a++) {
				game.addRobot(src_node+a);
			}
		}
		catch (JSONException e) {e.printStackTrace();}
		game.startGame();
		// should be a Thread!!!
		while(game.isRunning()) {
			moveRobots(game, g);
		}
		String results = game.toString();
		System.out.println("Game Over: "+results);
	}
	/** 
	 * Moves each of the robots along the edge, 
	 * in case the robot is on a node the next destination (next edge) is chosen (randomly).
	 * @param game
	 * @param gg
	 * @param log
	 */
	private void moveRobots(game_service game, graph gg) {
		List<String> log = game.move();
		if(log!=null) {
			long t = game.timeToEnd();
			for(int i=0;i<log.size();i++) {
				String robot_json = log.get(i);
				try {
					JSONObject line = new JSONObject(robot_json);
					JSONObject ttt = line.getJSONObject("Robot");
					int rid = ttt.getInt("id");
					int src = ttt.getInt("src");
					int dest = ttt.getInt("dest");

					if(dest==-1) {	
						dest = nextNode(gg, src);
						game.chooseNextEdge(rid, dest);
						System.out.println("Turn to node: "+dest+"  time to end:"+(t/1000));
						System.out.println(ttt);
					}
				} 
				catch (JSONException e) {e.printStackTrace();}
			}
		}
	}
	/**
	 * a very simple random walk implementation!
	 * @param g
	 * @param src
	 * @return
	 */
	private int nextNode(graph g, int src) {
		int ans = -1;
		Collection<edge_data> ee = g.getE(src);
		Iterator<edge_data> itr = ee.iterator();
		int s = ee.size();
		int r = (int)(Math.random()*s);
		int i=0;
		while(i<r) {itr.next();i++;}
		ans = itr.next().getDest();
		return ans;
	}

}