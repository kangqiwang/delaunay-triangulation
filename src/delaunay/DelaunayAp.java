package delaunay;



import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.swing.*;


@SuppressWarnings("serial")
public class DelaunayAp extends javax.swing.JApplet
        implements Runnable, ActionListener, MouseListener {

    private Component currentSwitch = null;    

    private static String windowTitle = "Voronoi/Delaunay Window";
    private JRadioButton voronoiButton = new JRadioButton("̩ɭ�����");
    private JCheckBox colorfulBox = new JCheckBox("");
    private DelaunayPanel delaunayPanel = new DelaunayPanel(this);
    private JLabel circleSwitch = new JLabel("��ʾ���������ε�����");
    private JLabel delaunaySwitch = new JLabel("����������");
       public static void main (String[] args) {
        DelaunayAp applet = new DelaunayAp();    
        applet.init();                           
        JFrame dWindow = new JFrame();           
        dWindow.setSize(700, 500);              
        dWindow.setTitle(windowTitle);           
        dWindow.setLayout(new BorderLayout());   
        dWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                                 
        dWindow.add(applet, "Center");           
        dWindow.setVisible(true);                
       
    }

    
    public void init () {
        try {SwingUtilities.invokeAndWait(this);}
        catch (Exception e) {System.err.println("Initialization failure");}
    }

    
    public void run () {
        setLayout(new BorderLayout());
        this.add(voronoiButton, "North");

        JPanel switchPanel = new JPanel();
        switchPanel.add(circleSwitch);
        switchPanel.add(delaunaySwitch);
        this.add(switchPanel, "South");

        delaunayPanel.setBackground(Color.gray);
        this.add(delaunayPanel, "Center");
        voronoiButton.addActionListener(this);
        delaunayPanel.addMouseListener(this);
        circleSwitch.addMouseListener(this);
        delaunaySwitch.addMouseListener(this);

        voronoiButton.doClick();
    }

    /**
     * button���������������ĺ����
     */
    public void actionPerformed(ActionEvent e) {
    	
    	
    	
    }

    /**
     * ������switch�������Ӧͼ��
     */
    public void mouseEntered(MouseEvent e) {
        currentSwitch = e.getComponent();
        if (currentSwitch instanceof JLabel) delaunayPanel.repaint();
        else currentSwitch = null;
    }

    /**
     * ����뿪switch�����»���
     */
    public void mouseExited(MouseEvent e) {
        currentSwitch = null;
        if (e.getComponent() instanceof JLabel) delaunayPanel.repaint();
    }

    /**
     * �������������µĵ�
     */
    public void mousePressed(MouseEvent e) {
        if (e.getSource() != delaunayPanel) return;//�������delaunaypanel�ģ����ؿ�
        Pnt point = new Pnt(e.getX(), e.getY());
         System.out.println("��� " + point);
        delaunayPanel.addSite(point);
        delaunayPanel.repaint();
    }

    /**
     * ����
     */
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}

    public boolean isColorful() {
        return colorfulBox.isSelected();
        }

    public boolean isVoronoi() {
        return voronoiButton.isSelected();
    }

    public boolean showingCircles() {
        return currentSwitch == circleSwitch;
    }

    public boolean showingDelaunay() {
        return currentSwitch == delaunaySwitch;
    }

}

@SuppressWarnings("serial")
class DelaunayPanel extends JPanel {

    public static Color voronoiColor = Color.magenta;
    public static Color delaunayColor = Color.green;
    public static int pointRadius = 3;

    private DelaunayAp controller;              
    private Triangulation dt;                   // ������������
    private Triangle initialTriangle;           
    private static int initialSize = 10000;     
    private Graphics g;                         
    private Random random = new Random();       

    public DelaunayPanel (DelaunayAp controller) {
        this.controller = controller;
        initialTriangle = new Triangle(
                new Pnt(-initialSize, -initialSize),
                new Pnt( initialSize, -initialSize),
                new Pnt(           0,  initialSize));
        dt = new Triangulation(initialTriangle);
    }

    /**
     * �����µĽڵ�.
     */
    public void addSite(Pnt point) {
        dt.delaunayPlace(point);
    }

 Color getColor (Object item) {
        Color color = new Color(Color.HSBtoRGB(random.nextFloat(), 1.0f, 1.0f));
        return color;
    }

    public void draw (Pnt point) {
        int r = pointRadius;
        int x = (int) point.coord(0);
        int y = (int) point.coord(1);
        g.fillOval(x-r, y-r, r+r, r+r);
    }

    public void draw (Pnt center, double radius, Color fillColor) {
        int x = (int) center.coord(0);
        int y = (int) center.coord(1);
        int r = (int) radius;
        if (fillColor != null) {
            Color temp = g.getColor();
            g.setColor(fillColor);
            g.fillOval(x-r, y-r, r+r, r+r);
            g.setColor(temp);
        }
        g.drawOval(x-r, y-r, r+r, r+r);
    }

    public void draw (Pnt[] polygon, Color fillColor) {
        int[] x = new int[polygon.length];
        int[] y = new int[polygon.length];
        for (int i = 0; i < polygon.length; i++) {
            x[i] = (int) polygon[i].coord(0);
            y[i] = (int) polygon[i].coord(1);
        }
        if (fillColor != null) {
            Color temp = g.getColor();
            g.setColor(fillColor);
            g.fillPolygon(x, y, polygon.length);
            g.setColor(temp);
        }
        g.drawPolygon(x, y, polygon.length);
    }



    public void paintComponent (Graphics g) {
        super.paintComponent(g);
        this.g = g;

        Color temp = g.getColor();
        if (!controller.isVoronoi()) g.setColor(delaunayColor);
        else if (dt.contains(initialTriangle)) g.setColor(this.getBackground());
        else g.setColor(voronoiColor);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(temp);

        if (controller.isVoronoi())
            drawAllVoronoi(controller.isColorful(), true);
        else drawAllDelaunay(controller.isColorful());

        temp = g.getColor();
        g.setColor(Color.white);
        if (controller.showingCircles()) drawresult();
        if (controller.showingDelaunay()) drawAllDelaunay(false);
        g.setColor(temp);
    }

   
    public void drawAllDelaunay (boolean withFill) {
        for (Triangle triangle : dt) {
            Pnt[] vertices = triangle.toArray(new Pnt[0]);
            draw(vertices, withFill? getColor(triangle) : null);
        }
    }

 
    public void drawAllVoronoi (boolean withFill, boolean withSites) {
        HashSet<Pnt> done = new HashSet<Pnt>(initialTriangle);
        for (Triangle triangle : dt)
            for (Pnt site: triangle) {
                if (done.contains(site)) continue;
                done.add(site);
                List<Triangle> list = dt.surroundingTriangles(site, triangle);
                Pnt[] vertices = new Pnt[list.size()];
                int i = 0;
                for (Triangle tri: list)
                    vertices[i++] = tri.getCircumcenter();
                draw(vertices, withFill? getColor(site) : null);
                if (withSites) draw(site);
            }
    }

    //������������
    public void drawresult () {
        for (Triangle triangle: dt) {
            if (triangle.containsAny(initialTriangle)) continue;
            Pnt c = triangle.getFocus();
            draw(c, 3, null);
            double a = triangle.getarea();
            System.out.println(a);
        }
        
    }

}
