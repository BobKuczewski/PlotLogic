import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

class LogicPlot {
  String name=null;
  int x,y,w,h;
  int[] values = { 0, 0, 0, 1,   1, 0, 0, 0,   0, 0, 0, 0,   0, 0, 1, 0 };
  Color color=null;
  public LogicPlot (String name) {
    this.name = name;
    this.locate(0,0,10,10);
  }
  public void locate ( int x, int y, int w, int h ) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }
  public void load_data_vector ( Vector v ) {
    values = new int[v.size()];
    for (int i=0; i<v.size(); i++) {
      values[i] = ((Integer)v.elementAt(i)).intValue();
    }
  }
  public void dump() {
    System.out.print ( "Name: " + this.name +
                    ",   Color: [" + this.color.getRed() + "," + this.color.getGreen() + "," + this.color.getBlue() +
                    "],   Location: (" + this.x + "," + this.y + ")" );
    for (int i=0; i<values.length; i++) {
      if ((i%64) == 0) System.out.println();
      System.out.print ( values[i] );
    }
    System.out.println();
  }
  public void draw ( Graphics g, double scale, double offset ) {
    g.setClip (x,y,w,h);
    int margin = h / 5;
    if (margin < 1) margin = 1;
    g.setColor ( new Color ( 50, 50, 50 ) );
    g.drawRect ( x, y-1, w, h );
    g.setColor ( this.color );
    double n = values.length; // This forces double division below
    int x0, x1;
    for (int i=0; i<values.length; i++) {
      x0 = (int)Math.round(((x+(w*i/n))-offset)*scale);
      x1 = (int)Math.round(((x+(w*(i+1)/n))-offset)*scale);
      if (values[i] > 0.5) {
        g.drawLine ( x0, y+margin,   x1, y+margin );
      } else {
        g.drawLine ( x0, y+h-margin, x1, y+h-margin );
      }
      if (i>0) {
        if (values[i] != values[i-1]) {
          g.drawLine ( x0, y+margin, x0, y+h-margin );
        }
      }
    }
    g.setColor ( new Color ( 150, 150, 150 ) );
    g.drawString ( name, x+5, y+(h/2)+5 );
  }
}

class DisplayLogicPanel extends JPanel implements ActionListener,MouseListener,MouseWheelListener,MouseMotionListener,KeyListener {

  JFrame frame=null;

  String names[] = null;
  Vector data_vector_array[] = null;
  Vector plots = null;
  int num_data_lines = 0;

  public DisplayLogicPanel() {
    setBackground ( new Color ( 0, 0, 0 ) );
    plots = new Vector();
  }

  static int colors[][] = {
    { 255,   0,   0 },
    {   0, 255,   0 },
    {   0,   0, 255 },
    { 255, 255,   0 },
    { 255,   0, 255 },
    {   0, 255, 255 },
    { 255, 255, 255 }
    /*
    { 150,   0,   0 },
    {   0, 150,   0 },
    {   0,   0, 150 },
    { 150, 150,   0 },
    { 150,   0, 150 },
    {   0, 150, 150 },
    { 150, 150, 150 }*/
  };
  static int nc = 0;

  public void update_data_vector_array() {
    if (data_vector_array != null) {
      // Move the data into the plots
      plots = new Vector();
      nc = 0;
      for (int i=0; i<data_vector_array.length; i++) {
        LogicPlot p = new LogicPlot(names[i]);
        p.load_data_vector ( data_vector_array[i] );
        plots.add ( p );
        p.color = new Color(colors[nc][0],colors[nc][1], colors[nc][2]);
        nc += 1;
        if (nc >= colors.length) {
          nc = 0;
        }
      }
    }
  }

  public void set_frame ( JFrame f ) {
    frame = f;
  }

  int pref_w=1200, pref_h=600;

  public Dimension getPreferredSize() {
    return new Dimension(pref_w,pref_h);
  }

  public JMenuBar build_menu_bar() {
    JMenuItem mi;
    ButtonGroup bg;
    JMenuBar menu_bar = new JMenuBar();
    JMenu file_menu = new JMenu("File");
    {
      String[] items = {"Open", "", "Dump", "", "Exit"};
      for (int i=0; i<items.length; i++) {
        if (items[i].length() == 0) {
          file_menu.addSeparator();
        } else {
          mi = new JMenuItem(items[i]);
          file_menu.add ( mi );
          mi.addActionListener(this);
        }
      }
      menu_bar.add ( file_menu );
    }
    JMenu set_menu = new JMenu("Set");
    {
      String[] items = {"Zoom Fine", "", "Zoom Medium", "", "Zoom Coarse"};
      for (int i=0; i<items.length; i++) {
        if (items[i].length() == 0) {
          set_menu.addSeparator();
        } else {
          mi = new JMenuItem(items[i]);
          set_menu.add ( mi );
          mi.addActionListener(this);
        }
      }
      menu_bar.add ( set_menu );
     }
     return (menu_bar);
  }

  FileDialog fd = null;

  public boolean accept(File dir, String name) {
    if ( name.endsWith ( ".txt" ) ) {
      return true;
    } else {
      return false;
    }
  }

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();

    if (cmd.equalsIgnoreCase("Exit")) {
      System.exit(0);

    } else if (cmd.equalsIgnoreCase("Open")) {
      System.out.println ( "Open a file" );

      if (fd == null) {
        fd = new FileDialog ( frame, "Choose a file" );
      }
      fd.setTitle ( "Open a Data File" );
      fd.setMode ( FileDialog.LOAD );
      fd.setVisible(true);
      fd.toFront();

      if (fd.getFile() != null) {
        String file_name = null;
        if (fd.getDirectory() != null) {
          file_name = fd.getDirectory() + fd.getFile();
        } else {
          file_name = fd.getFile();
        }
        System.out.println ( "Reading data_vector_array from " + file_name );
        read_file ( file_name );
        this.update_data_vector_array();
      }

    } else if (cmd.startsWith("Zoom ")) {
      if (cmd.equalsIgnoreCase("Zoom Fine")) {
        zoomrate = 1.02;
      } else if (cmd.equalsIgnoreCase("Zoom Medium")) {
        zoomrate = 1.2;
      } else if (cmd.equalsIgnoreCase("Zoom Coarse")) {
        zoomrate = 2.0;
      }

    } else if (cmd.equalsIgnoreCase("Dump")) {
      System.out.println ( "======== Dump ========" );
      for (int i=0; i<plots.size(); i++) {
        System.out.println ( "=== Plot " + i + " ===" );
        LogicPlot p = (LogicPlot)(plots.elementAt(i));
        p.dump();
      }

    } else if (cmd.equalsIgnoreCase("Clear")) {
      // Eventually clear the plot
    }
    repaint();
  }

  public void keyTyped ( KeyEvent e ) {
    // System.out.println ( "Key: " + e );
    if (Character.toUpperCase(e.getKeyChar()) == 'N') {
      // Next
    } else if (Character.toUpperCase(e.getKeyChar()) == 'P') {
      // Previous
    }
    repaint();
  }
  public void keyPressed ( KeyEvent e ) {
    // System.out.println ( "Key Pressed: " + e );
    if (e.getKeyCode()==37) {          // Left Arrow
      // Show more from the left
      offset = offset - (Math.pow(10,zoomrate) / scale);
      repaint();
    } else if (e.getKeyCode()==39) {   // Right Arrow
      // Show more from the right
      offset = offset + (Math.pow(10,zoomrate) / scale);
      repaint();
    } else if (e.getKeyCode()==38) {   // Up Arrow
      // Zoom in
      int win_w = getSize().width;
      double x = win_w / 2;
      double old_scale = scale;
      scale = scale * zoomrate;
      offset += (x/old_scale) - (x/scale);
      repaint();
    } else if (e.getKeyCode()==40) {   // Down Arrow
      // Zoom out
      int win_w = getSize().width;
      double x = win_w / 2;
      double old_scale = scale;
      scale = scale / zoomrate;
      offset += (x/old_scale) - (x/scale);
      repaint();
    }
  }
  public void keyReleased ( KeyEvent e ) {
    // System.out.println ( "Key Released: " + e );
  }


  void draw_grid (Graphics g) {

    int win_w = getSize().width;
    int win_h = getSize().height;
    double offset_int = 0;
    double tpdi = 0;

    double min_index = (num_data_lines * offset) / win_w;
    double max_index = (num_data_lines / scale) + (num_data_lines * offset / win_w);
    double samps_per_screen = max_index - min_index;
    if (samps_per_screen <= 0) samps_per_screen = 1;
    long first_index = Math.round(min_index - 0.5);
    long last_index = Math.round(max_index + 0.5);

    int nice_pix_per_div = 100;
    double nice_samps_per_div = nice_pix_per_div * samps_per_screen / win_w;

    // Calculate a samps_per_div close to our nice_samps_per_div
    int powers_of_10 = 0;
    double samps_per_div = nice_samps_per_div;
    while (samps_per_div > 1) {
      samps_per_div = samps_per_div / 10;
      powers_of_10 ++;
    }
    while (samps_per_div < 1) {
      samps_per_div = samps_per_div * 10;
      powers_of_10 --;
    }
    // Samples per division should now be >= 1 and less than 10
    // Round it to an appropriate value of either 1, 2, or 5
    if (samps_per_div < 1.5) {
      samps_per_div = 1;
    } else if (samps_per_div < 3) {
      samps_per_div = 2;
    } else {
      samps_per_div = 5;
    }
    // Restore the magnitude
    while (powers_of_10 > 0) {
      samps_per_div = samps_per_div * 10;
      powers_of_10 --;
    }
    while (powers_of_10 < 0) {
      samps_per_div = samps_per_div / 10;
      powers_of_10 ++;
    }
    double pix_per_div = win_w * samps_per_div / samps_per_screen;

    long first_div = 0;
    long last_div = 0;

    // Find the starting div
    while ((first_div * samps_per_div) < first_index) {
      first_div += 1;
    }
    while ((first_div * samps_per_div) > first_index) {
      first_div += -1;
    }
    // Find the ending div
    last_div = first_div + 1;
    while ((last_div * samps_per_div) < last_index) {
      last_div += 1;
    }
    for (long i=first_div; i<=last_div; i++) {
      double samp_value = i * samps_per_div;
      int pix_num = (int)Math.round((i * pix_per_div)-(offset*scale));
      g.setColor ( new Color ( 50, 50, 50 ) );
      g.drawLine ( pix_num, 0, pix_num, win_h );
      g.setColor ( new Color ( 150, 150, 150 ) );
      g.drawString ( ""+ Math.round(1000000*samp_value)/1000000.0, pix_num, win_h-5 );
    }
  }

  public void paintComponent(Graphics g) {

    super.paintComponent(g);

    int win_w, win_h, w, h, time_h;
    win_w = getSize().width;
    win_h = getSize().height;

    // Draw a grid
    draw_grid(g);

    // Draw the data panels
    int n = plots.size();
    for (int i=0; i<n; i++) {
      LogicPlot p = (LogicPlot)(plots.elementAt(i));
      p.locate ( 0, i*(win_h-20)/n, win_w, (win_h-20)/n );
      p.draw ( g, scale, offset );
    }
  }

  int mouse_down_x = 0;
  int mouse_delta_x = 0;

  Cursor current_cursor = null;
  Cursor h_cursor = null;
  int cursor_size = 33;

  double scale = 1;
  double offset = 0;
  double zoomrate = 1.2;

  public void mouseWheelMoved(MouseWheelEvent e) {
    double x = e.getX();
    double old_scale = scale;
    int w = e.getWheelRotation();
    if (w > 0) {
      // Moving the mouse wheel backward is positive but should decrease scale
      scale = scale / (w * zoomrate);
    } else if (w < 0) {
      // Moving the mouse wheel forward is negative but should increase scale
      scale = -scale * w * zoomrate;
    }
    offset += (x/old_scale) - (x/scale);
    repaint();
  }

  boolean drag_button_down = false;

  public void mouseDragged(MouseEvent e) {
    if (drag_button_down) {
      int mouse_now = e.getX();
      mouse_delta_x = mouse_now - mouse_down_x;
      offset = offset - (mouse_delta_x / scale);
      mouse_down_x = mouse_now;
      repaint();
    }
  }

  public void mouseMoved(MouseEvent e) { }
  public void mouseClicked(MouseEvent e) { }

  public void mouseEntered ( MouseEvent e ) {
    if (h_cursor == null) {
      Toolkit tk = Toolkit.getDefaultToolkit();
      Graphics2D cg = null;
      BufferedImage cursor_image = null;
      Polygon p = null;
      int h = cursor_size;
      int w = cursor_size;

      // Create the horizontal cursor
      p = new Polygon();
      p.addPoint ( 0, h/2 );
      p.addPoint ( w/4, (h/2)-(h/4) );
      p.addPoint ( w/4, (h/2)-(h/8) );
      p.addPoint ( 3*w/4, (h/2)-(h/8) );
      p.addPoint ( 3*w/4, (h/2)-(h/4) );
      p.addPoint ( w-1, h/2 );
      p.addPoint ( 3*w/4, (h/2)+(h/4) );
      p.addPoint ( 3*w/4, (h/2)+(h/8) );
      p.addPoint ( w/4, (h/2)+(h/8) );
      p.addPoint ( w/4, (h/2)+(h/4) );

      cursor_image = new BufferedImage(cursor_size,cursor_size,BufferedImage.TYPE_4BYTE_ABGR);
      cg = cursor_image.createGraphics();
      cg.setColor ( new Color(255,255,255) );
      cg.fillPolygon ( p );
      cg.setColor ( new Color(0,0,0) );
      cg.drawPolygon ( p );

      h_cursor = tk.createCustomCursor ( cursor_image, new Point(cursor_size/2,cursor_size/2), "Horizontal" );

    }
    if (current_cursor == null) {
      current_cursor = h_cursor;
    }
    setCursor ( current_cursor );
  }
  public void mouseExited(MouseEvent e) { }
  public void mousePressed(MouseEvent e) {
    // Click to drag
    if (e.getButton() == e.BUTTON1) {
      mouse_down_x = e.getX();
      drag_button_down = true;
    } else {
      // System.out.println ( "Alternate button" );
    }
  }
  public void mouseReleased(MouseEvent e) {
    if (e.getButton() == e.BUTTON1) {
      drag_button_down = false;
    }
  }

  public void read_file ( String fname ) {
    DisplayLogicPanel dp = this;
    try {
      // This file should have a line of names followed by many lines of data
      BufferedReader in = new BufferedReader(new FileReader(fname));
      // Read in a name_line and replace tabs with spaces and trim spaces afterward
      String name_line = in.readLine().replaceAll("\t"," ").trim();
      // Remove all double spaces
      while (name_line.indexOf("  ") >= 0) {
        name_line = name_line.replaceAll("  ", " ");
      }
      System.out.println ( "Names: \"" + name_line + "\"" );
      dp.names = name_line.split(" ");
      dp.data_vector_array = new Vector[dp.names.length];
      for (int i=0; i<dp.names.length; i++) {
        dp.data_vector_array[i] = new Vector();
      }
      num_data_lines = 0;
      boolean done = false;
      while (!done) {
        try {
          String next_line = in.readLine().replaceAll("\t"," ").trim();
          while (next_line.indexOf("  ") >= 0) {
            next_line = next_line.replaceAll("  ", " ");
          }
          if (next_line.length() > 0) {
            String[] values = next_line.split(" ");
            if (values.length > 0) {
              num_data_lines += 1;
            }
            for (int i=0; i<values.length; i++) {
              if (i >= dp.names.length) {
                System.out.println ( "Error at: \"" + values[i] + "\", too many values on a line." );
              } else {
                int v=0;
                try {
                  v = Integer.parseInt(values[i]);
                } catch ( Exception e ) {
                  System.out.println ( "Error converting \"" + values[i] + "\" to a number." );
                }
                try {
                  dp.data_vector_array[i].add ( v );
                } catch ( Exception e ) {
                  System.out.println ( "Error adding \"" + v + "\" to Vector." );
                }
              }
            }
          }
        } catch ( Exception e ) {
          System.out.println ( "Done reading" );
          done = true;
        }
      }
    } catch ( Exception e ) {
      System.out.println ( "Unable to read file " + fname );
    }
    try {
      frame.setTitle ( fname );
    } catch ( Exception e ) {
      // System.out.println ( "Error setting frame title to \"" + fname );
    }
  }
}


public class PlotLogic extends JFrame implements WindowListener {

  static DisplayLogicPanel dp = null;

  PlotLogic ( String s ) {
    super(s);
  }

  public static void main(String[] args) {

    System.out.println ( "Java Logic Plotting Program ..." );

    dp = new DisplayLogicPanel();

    String fname = "";

    if (args.length > 0) {
      for (int arg=0; arg<args.length; arg++) {
        if ( (args[arg].toLowerCase().charAt(0) == 'h') || (args[arg].charAt(0) == '?') ) {
          System.out.println ( "Arguments:" );
          System.out.println ( "  f=filename - name of a file with a line of names followed by lines of data" );
          System.exit(0);
        } else if ( (args[arg].length() > 2) && (args[arg].substring(0,2).equalsIgnoreCase("f=")) ) {
          fname = args[arg].substring(2);
          System.out.println ( "Reading file " + fname + " ..." );
          dp.read_file(fname);
        } else {
         System.out.println ( "Unrecognized Argument: " + args[arg] );
         System.exit(0);
        }
      }
    }

    dp.update_data_vector_array();

    PlotLogic frame = new PlotLogic("Plot Logic");

    dp.set_frame ( frame );
    frame.setSize(400, 150);
    frame.setJMenuBar ( dp.build_menu_bar() );

    Container content = frame.getContentPane();
    content.setBackground(Color.white);
    content.setLayout(new BorderLayout());

    frame.add(dp);
    dp.addMouseWheelListener ( dp );
    dp.addMouseMotionListener ( dp );
    dp.addMouseListener ( dp );
    frame.addKeyListener ( dp );
    frame.pack();
    frame.addWindowListener(frame);
    frame.setVisible(true);

    frame.setTitle ( fname );

    content.repaint();
  }

  public void windowActivated(WindowEvent event) { }
  public void windowClosed(WindowEvent event) { }
  public void windowClosing(WindowEvent event) { System.exit(0); }
  public void windowDeactivated(WindowEvent event) { }
  public void windowDeiconified(WindowEvent event) { }
  public void windowIconified(WindowEvent event) { }
  public void windowOpened(WindowEvent event) { }

}

