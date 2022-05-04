 /* Pulse_Oxi.Java: Poxi GUI File
   Author: Kartik Ramesh
   Date: 15.04.2022
 */

package poxi_S22;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;
//import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Locale;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.SystemColor;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;


public class PoxiMain 
{

	private JFrame FG_frame;
	private JTextField cmd_textField;
	protected String dev_name;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		SerialNetw.initSerialNetw(); //Invoke the serialNetw class
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PoxiMain window = new PoxiMain();
					window.FG_frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PoxiMain() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() 
	{
		FG_frame = new JFrame();
		FG_frame.setBounds(100, 100, 1161, 738);
		FG_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel Cmd_panel = new JPanel();
		Cmd_panel.setBackground(SystemColor.activeCaption);
		Cmd_panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new LineBorder(Color.BLUE, 3));
		
		JLabel lblNewLabel = new JLabel("Cmd >> ");
		lblNewLabel.setFont(new Font("Times New Roman", Font.BOLD, 13));
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		
		cmd_textField = new JTextField();
		cmd_textField.setHorizontalAlignment(SwingConstants.LEFT);
		cmd_textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CommandHandler(cmd_textField.getText());
				cmd_textField.setText("");
			}
		});
		cmd_textField.setColumns(90);
		
		Gr_panel = new JPanel();
		Gr_panel.setBackground(new Color(245, 255, 250));
		Gr_panel.setBorder(new LineBorder(new Color(0, 0, 255)));
		
		tout_textPane = new JTextPane();
		tout_textPane.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		tout_textPane.setBackground(UIManager.getColor("InternalFrame.inactiveTitleGradient"));
		tout_textPane.setEditable(false);
		scrollPane.setViewportView(tout_textPane);
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBackground(SystemColor.activeCaption);
		
		JComboBox DeviceList = new JComboBox();
		DeviceList.setBackground(new Color(30, 144, 255));
		DeviceList.setMaximumRowCount(4);
		DeviceList.setFont(new Font("Tahoma", Font.BOLD, 11));
		DeviceList.setModel(new DefaultComboBoxModel(new String[] {"COM1", "COM2", "COM3", "COM4"}));
		DeviceList.setSelectedIndex(2);
		DeviceList.setEditable(true);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CommandHandler("con " + DeviceList.getSelectedItem().toString());
			}
		});
		btnConnect.setBackground(new Color(60, 179, 113));
		btnConnect.setFont(new Font("Dialog", Font.BOLD, 11));
		btnConnect.setIcon(null);
		
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setBackground(new Color(255, 51, 51));
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CommandHandler("discon");
			}
		});
		btnDisconnect.setIcon(null);
		btnDisconnect.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		JPanel panel_plot = new JPanel();
		panel_plot.setBorder(new LineBorder(new Color(0, 0, 0)));
		
		btnClear = new JButton("Clear Text");
		btnClear.setIcon(null);
		btnClear.setForeground(new Color(0, 0, 0));
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CommandHandler("clc");				
			}
		});
		btnClear.setBackground(new Color(102, 204, 153));
		btnClear.setFont(new Font("Dialog", Font.BOLD, 11));
		
		JButton btnHelp = new JButton("Help");
		btnHelp.setIcon(new ImageIcon(PoxiMain.class.getResource("/com/jidesoft/swing/icons/overlay_info.png")));
		btnHelp.setBackground(new Color(30, 144, 255));
		btnHelp.setForeground(new Color(0, 0, 0));
		btnHelp.setFont(new Font("Dialog", Font.BOLD, 11));
		
		btnExit = new JButton("Exit");
		btnExit.setIcon(new ImageIcon(PoxiMain.class.getResource("/com/jidesoft/swing/icons/close.png")));
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				CommandHandler("exit");			
			}
		});
		btnExit.setBackground(new Color(255, 51, 51));
		btnExit.setFont(new Font("Dialog", Font.BOLD, 11));
		btnExit.setForeground(new Color(0, 0, 0));
		
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(67)
					.addComponent(DeviceList, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
					.addGap(60)
					.addComponent(btnConnect, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(40)
					.addComponent(btnDisconnect, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(39)
					.addComponent(btnClear, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(45)
					.addComponent(btnHelp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(40)
					.addComponent(btnExit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(39))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(DeviceList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnConnect)
						.addComponent(btnExit, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnHelp)
						.addComponent(btnDisconnect)
						.addComponent(btnClear))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
		
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CommandHandler("help");
			}
		});
		panel_plot.setBackground(SystemColor.activeCaption);
		
		JButton btnMeasure = new JButton("Measure");
		btnMeasure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ClearChart();
				SerialNetw.SendString("measure\n"); 
				flagMeasure = true;
			}
		});
		btnMeasure.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 11));
		btnMeasure.setBackground(new Color(255, 255, 153));
		
		JButton btnClearBuf = new JButton("Stop");
		btnClearBuf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SerialNetw.SendString("stop\n");// stop the interrupt timer
				SerialNetw.SendString("off\n");// stop the interrupt timer
				SerialNetw.SendString("rm\n");// reset measurement buffer
				flagMeasure = false;
				Pb_NValues_RED=0;// comment this line for accumulating the data with previous measurements
				Pb_NValues_IRED=0;
			}
		});
		btnClearBuf.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 11));
		btnClearBuf.setBackground(new Color(255, 255, 153));
		
		JLabel lblNewLabel_1 = new JLabel("Pulse Rate:");
		lblNewLabel_1.setFont(new Font("Times New Roman", Font.BOLD, 14));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		
		textField = new JTextField();
		textField.setBackground(Color.WHITE);
		textField.setForeground(new Color(0, 0, 255));
		textField.setEditable(false);
		textField.setFont(new Font("Times New Roman", Font.BOLD, 14));
		textField.setColumns(12);
		
		JLabel lblNewLabel_2 = new JLabel("beats/min");
		lblNewLabel_2.setFont(new Font("Times New Roman", Font.BOLD, 13));
		
		GroupLayout gl_panel_plot = new GroupLayout(panel_plot);
		gl_panel_plot.setHorizontalGroup(
			gl_panel_plot.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_plot.createSequentialGroup()
					.addGap(67)
					.addComponent(lblNewLabel_1, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(textField, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(lblNewLabel_2)
					.addGap(96)
					.addComponent(btnMeasure, GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
					.addGap(111)
					.addComponent(btnClearBuf, GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
					.addGap(189))
		);
		gl_panel_plot.setVerticalGroup(
			gl_panel_plot.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_plot.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_plot.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_1, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_2)
						.addComponent(btnClearBuf, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnMeasure, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap())
		);
		panel_plot.setLayout(gl_panel_plot);
		
		Gr_panel1 = new JPanel();
		Gr_panel1.setBackground(new Color(245, 255, 250));
		Gr_panel1.setBorder(new LineBorder(new Color(0, 0, 255), 2));
		
		textRaw = new JTextField();
		textRaw.setHorizontalAlignment(SwingConstants.CENTER);
		textRaw.setForeground(new Color(0, 0, 0));
		textRaw.setText("RED Filtered Data ( 0.3 Hz - 3 Hz )");
		textRaw.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 13));
		textRaw.setBackground(SystemColor.activeCaption);
		textRaw.setColumns(10);
		
		textFiltered = new JTextField();
		textFiltered.setHorizontalAlignment(SwingConstants.CENTER);
		textFiltered.setText("IRED Filtered Data ( 0.3 Hz - 3 Hz )");
		textFiltered.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 13));
		textFiltered.setColumns(10);
		textFiltered.setBackground(SystemColor.activeCaption);
		GroupLayout gl_Cmd_panel = new GroupLayout(Cmd_panel);
		gl_Cmd_panel.setHorizontalGroup(
			gl_Cmd_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_Cmd_panel.createSequentialGroup()
					.addGap(10)
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(cmd_textField, GroupLayout.DEFAULT_SIZE, 742, Short.MAX_VALUE)
					.addGap(304))
		);
		gl_Cmd_panel.setVerticalGroup(
			gl_Cmd_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_Cmd_panel.createSequentialGroup()
					.addGap(6)
					.addGroup(gl_Cmd_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(cmd_textField, GroupLayout.PREFERRED_SIZE, 18, Short.MAX_VALUE))
					.addGap(9))
		);
		Cmd_panel.setLayout(gl_Cmd_panel);
		GroupLayout groupLayout = new GroupLayout(FG_frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 296, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(62)
							.addComponent(textRaw, GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
							.addGap(164)
							.addComponent(textFiltered, GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
							.addGap(55))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(Gr_panel, GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(Gr_panel1, GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE))
								.addComponent(Cmd_panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 815, Short.MAX_VALUE)
								.addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 815, Short.MAX_VALUE)
								.addComponent(panel_plot, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 815, Short.MAX_VALUE))
							.addGap(10)))
					.addGap(10))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(10)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(textFiltered, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(textRaw, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(Gr_panel, GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
								.addComponent(Gr_panel1, GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE))
							.addGap(18)
							.addComponent(panel_plot, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(panel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addGap(14)
							.addComponent(Cmd_panel, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		FG_frame.getContentPane().setLayout(groupLayout);
		
		JMenuBar menuBar = new JMenuBar();
		FG_frame.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Help");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CommandHandler("help");
			}
		});
		mnNewMenu.add(mntmNewMenuItem);
		
		JMenuItem save = new JMenuItem("Save");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveToFile();
				PrintTxtWin("Data Saved to the File"+"\n",1,true);
			}
		});
		mnNewMenu.add(save);
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Exit");
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnNewMenu.add(mntmNewMenuItem_1);

		CreateChart();
        DispUpdate_Timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UpdateDynamic();
                DispUpdate_Timer.restart();
            }
        });
        DispUpdate_Timer.start();
		Pb_NValues_RED=0;
		Pb_NValues_IRED=0;
    }


	private void CommandHandler(String cmds) 
	{
		String command = cmds, dev_name;
		int  k, stat;
				
		if (command.equals("clc"))
		{
			tout_textPane.setText("");
		} 
		else if (command.equals("clg")) 
		{
			ClearChart();
		} 
		else if (command.equals("help"))
		{
			PrintTxtWin("Click the Button / Type Below listed command in cmd window \n", 1, true);
			
			PrintTxtWin(" clc - clear text window", 1, true);	
			PrintTxtWin(" clg - clear chart window", 1, true);	
			PrintTxtWin(" list - list avaliable devices", 1, true);			
			PrintTxtWin(" con COM<Port No>  - connect", 1, true);			
			PrintTxtWin(" discon - disconnect", 1, true);			
			PrintTxtWin(" .<sendstring>", 1, true);
			PrintTxtWin(" exit - exit the application", 1, true);
		} 
		else if (command.equals("list")) 
		{
			PrintTxtWin(" Serial Ports . . . ", 0, true);
			for (k = 0; k < SerialNetw.getNDev(); k++)
			{
				dev_name = SerialNetw.getDevName(k);
				stat = SerialNetw.getDevStat(dev_name);
				if (stat == 2) 
				{
					PrintTxtWin("  " + dev_name + " [connected]\n", 4, true);									
				} 
				else 
				{
					PrintTxtWin("  " + dev_name + " [available]\n", 4, true);																
				}
			}
		}
		else if (command.startsWith("con ")) 
		{
			dev_name = command.substring(4).toUpperCase();
			if (SerialNetw.getDevStat(dev_name) != 1) {
				PrintTxtWin("*** Connection Failed ***", 3, true);
				PrintTxtWin("*** device unavailable ***\n", 3, true);			
				System.out.println(SerialNetw.getDevStat(dev_name));
			} 
			else {
				(new SerialNetw()).spConnect(dev_name);
				System.out.println(SerialNetw.getDevStat(dev_name));
                PrintTxtWin("\nSuccess. Connected to " + dev_name+"\n", 1, true);	
								
			}
		} 
		else if (command.equals("discon")) 
		{
			dev_name = SerialNetw.getConName();
			if (dev_name != null) {
				SerialNetw.spDisconn(dev_name);
				PrintTxtWin("Success. Disconnected from " + dev_name+"\n", 1, true);				
			} 
			else {
				PrintTxtWin("*** No device connected ***\n", 3, true);								
			}
		}
		
		else if (command.equals("exit")) 
		{
			dev_name = SerialNetw.getConName();
			if (dev_name != null) {
				SerialNetw.spDisconn(dev_name);
			}
	        DispUpdate_Timer.stop();
	        System.exit(0);
		}
		
		else if (command.startsWith(".")) 
		{
			if (SerialNetw.getConName() != null) 
			{
				SerialNetw.SendString(command.substring(1) + "\n");
			} 
			else 
			{
				PrintTxtWin("*** No connected device ***\n", 3, true);				
			}
		} 
		else if (command.length() > 0) 
		{
			PrintTxtWin("*** Incorrect Command *** " , 3, true);
			PrintTxtWin("*** Press Help ***  \n", 3, true);
		}
	}


    private void PrintTxtWin(String twstr, int twstyle, boolean newline) 
    {
        try 
        {
            Document doc = tout_textPane.getStyledDocument();
            StyleConstants.setItalic(TextSet, false);
            StyleConstants.setBold(TextSet, false);
            StyleConstants.setForeground(TextSet, Color.BLACK);
            switch (twstyle) 
            {
                case 0:
                    StyleConstants.setBold(TextSet, true);
                    StyleConstants.setForeground(TextSet, Color.DARK_GRAY);
                    break;
                case 1: 
                	StyleConstants.setBold(TextSet, true);
                	StyleConstants.setForeground(TextSet, Color.BLUE);
                    break;
                case 2: 
                	StyleConstants.setBold(TextSet, true);
                	StyleConstants.setForeground(TextSet, Color.BLACK);
                    break;
                case 3: 
                	StyleConstants.setBold(TextSet, true);
                	StyleConstants.setForeground(TextSet, Color.RED);
                	break;
                case 4:
                	StyleConstants.setBold(TextSet, true);
                	StyleConstants.setForeground(TextSet, Color.RED);
                	break;
                case 5:
                	StyleConstants.setBold(TextSet, true);
                	StyleConstants.setForeground(TextSet, Color.CYAN);
                	break;
                default:
                    doc.remove(0, doc.getLength());
            }
            if (twstyle >= 0) 
            {
            	tout_textPane.setCharacterAttributes(TextSet, true);
            	if (newline) {
                    doc.insertString(doc.getLength(), twstr+"\n", TextSet);            		
            	} else {
                    doc.insertString(doc.getLength(), twstr, TextSet);
            	}
            }
        } catch (BadLocationException ex) {
            System.out.println(ex.toString());
        }
    }

	private void ClearChart()
	{
		int  k;
		for (k = 0; k < NTRACES; k++) {
			mtraces[k].removeAllPoints();
			mtraces[k].addPoint(0.0, 0.0);
		}				
	}
	
	private void CreateChart()
	{
		chart2 = new Chart2D();
		chart1 = new Chart2D();
		chart1.setBorder(new LineBorder(Color.BLUE));
		
		mtraces[0] = new Trace2DLtd(100);
		mtraces[0].setPhysicalUnits("Time (Seconds)","Magnitude");
		chart1.addTrace(mtraces[0]);

		mtraces[1] = new Trace2DLtd(100);
		mtraces[1].setPhysicalUnits("Time (Seconds)","Magnitude");
		chart2.addTrace(mtraces[1]);

        mtraces[0].setColor(Color.red);     
        mtraces[0].setName("Red LED Filtered data");        
        mtraces[1].setColor(Color.blue);      
        mtraces[1].setName("IR LED Filtered data");
        
        Gr_panel.setSize(100,200);
        GroupLayout gl_Gr_panel = new GroupLayout(Gr_panel);
        gl_Gr_panel.setHorizontalGroup(
        	gl_Gr_panel.createParallelGroup(Alignment.LEADING)
        		.addComponent(chart1, GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
        );
        gl_Gr_panel.setVerticalGroup(
        	gl_Gr_panel.createParallelGroup(Alignment.LEADING)
        		.addComponent(chart1, GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)
        );
        GroupLayout gl_chart1 = new GroupLayout(chart1);
        gl_chart1.setHorizontalGroup(
        	gl_chart1.createParallelGroup(Alignment.LEADING)
        		.addGap(0, 430, Short.MAX_VALUE)
        );
        gl_chart1.setVerticalGroup(
        	gl_chart1.createParallelGroup(Alignment.LEADING)
        		.addGap(0, 514, Short.MAX_VALUE)
        );
        chart1.setLayout(gl_chart1);
        Gr_panel.setLayout(gl_Gr_panel);
        chart1.setVisible(true);
        Gr_panel.setVisible(true); 
        Gr_panel.repaint();
        Gr_panel1.setSize(100,200);
        GroupLayout gl_Gr_panel1 = new GroupLayout(Gr_panel1);
        gl_Gr_panel1.setHorizontalGroup(
        	gl_Gr_panel1.createParallelGroup(Alignment.LEADING)
        		.addComponent(chart2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
        );
        gl_Gr_panel1.setVerticalGroup(
        	gl_Gr_panel1.createParallelGroup(Alignment.LEADING)
        		.addComponent(chart2, GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
        );
        GroupLayout gl_chart2 = new GroupLayout(chart2);
        gl_chart2.setAutoCreateGaps(true);
        gl_chart2.setAutoCreateContainerGaps(true);
        gl_chart2.setHorizontalGroup(
        	gl_chart2.createParallelGroup(Alignment.LEADING)
        		.addGap(0, 433, Short.MAX_VALUE)
        );
        gl_chart2.setVerticalGroup(
        	gl_chart2.createParallelGroup(Alignment.LEADING)
        		.addGap(0, 514, Short.MAX_VALUE)
        );
        chart2.setLayout(gl_chart2);
        Gr_panel1.setLayout(gl_Gr_panel1);
        chart2.setVisible(true);
        Gr_panel1.setVisible(true); 
        Gr_panel1.repaint();
               
	}
	
	//Saving the data to the file
	private void saveToFile()
	{
		Iterator<ITracePoint2D> itPoints;
		ITracePoint2D point;
		
		Path logFile = Paths.get("poxi_data.dat");

        try (BufferedWriter writer = Files.newBufferedWriter(logFile,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE))
        {
        	itPoints = mtraces[0].iterator();
        	
            while (itPoints.hasNext())
            {
            	point = itPoints.next();
                writer.write(String.format(Locale.US,"%.3f, %.4f\n", point.getX(), point.getY()));
            }
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}
	
	//This function gets called repeatedly according to display timer
	private void UpdateDynamic() 
	{
    	String  rcvString,cutString;
    	int  Length_FRED= 0, Length_FIRED= 0, Length_Pulse= 0, FRED_StartsWith=0, FIRED_StartsWith=0, Pulse_StartsWith=0;
		rcvString = SerialNetw.ReadString();
		
    	if(rcvString  != null)
    	{
    		if(flagMeasure)
    		{
	    		if((rcvString.length()!=1) & (!(rcvString.endsWith("-"))))
	    		{
					String  FRED_Elements[]= {""};
		
					FRED_StartsWith=0;
					cutString = rcvString;
					//System.out.println(rcvString);
					if (((rcvString.contains("~"))|(rcvString.contains("/"))|(rcvString.contains("§")))&(!rcvString.contains("*")))
					{
						if (rcvString.contains("~"))
						{
							 if (rcvString.startsWith("~"))
							{
								FRED_StartsWith=1;
							}
							FRED_Elements = cutString.split("~");
							System.arraycopy(FRED_Elements, FRED_StartsWith, FRED_Elements, 0, (cutString.split("~")).length-FRED_StartsWith);
							Length_FRED = FRED_Elements.length-FRED_StartsWith;
						}
						else
						{
							FRED_Elements[0] = cutString;
							Length_FRED=1;
						}
						for (int i=0;i<Length_FRED;i++)
						{
							String  FIRED_Elements[]={""};
							FIRED_StartsWith=0;
							cutString = FRED_Elements[i];
							if (FRED_Elements[i].contains("/")){
								 if (FRED_Elements[i].startsWith("/"))
								{
									FIRED_StartsWith=1;
								}
								FIRED_Elements = cutString.split("/");
								System.arraycopy(FIRED_Elements, FIRED_StartsWith, FIRED_Elements, 0, (cutString.split("/")).length-FIRED_StartsWith);
								Length_FIRED = FIRED_Elements.length-FIRED_StartsWith;
							}
							else{
								FIRED_Elements[0] = cutString;
								Length_FIRED=1;
							}
							for (int j=0;j<Length_FIRED;j++)
							{
								String Pulse_Elements[]={""};
								Pulse_StartsWith=0;
								cutString = FIRED_Elements[j];
								if (FIRED_Elements[j].contains("§")){
									 if (FIRED_Elements[j].startsWith("§"))
									{
										Pulse_StartsWith=1;
									}
									Pulse_Elements = cutString.split("§");
									System.arraycopy(Pulse_Elements, Pulse_StartsWith, Pulse_Elements, 0, (cutString.split("§")).length-Pulse_StartsWith);
									Length_Pulse = Pulse_Elements.length-Pulse_StartsWith;
								}
								else
								{
									Pulse_Elements[0] = cutString;
									Length_Pulse=1;
								}
								if(j==0 &((i!=0)|(FRED_StartsWith==1)))
								{
			
			        				mtraces[0].addPoint((double)Pb_NValues_RED*SamplingTime, Double.parseDouble(Pulse_Elements[0]));
									Pb_NValues_RED++;
								}
								if((j!=0)|(FIRED_StartsWith==1))
								{
									mtraces[1].addPoint((double)Pb_NValues_IRED*SamplingTime,  Double.parseDouble(Pulse_Elements[0]));
									Pb_NValues_IRED++;
								}
								for (int kk=0;kk<Length_Pulse;kk++)
								{
									if((kk!=0)|(Pulse_StartsWith==1))
									{
										pRate=  Double.parseDouble(Pulse_Elements[kk]);
										textField.setText(String.format("%.0f",pRate));
									}
								}
							}
						}
	    		}
			}			
    	}  
		//Print received string on command window
		else 
		{
		PrintTxtWin(rcvString, 0, true);    			
		}
    }
}

    double SamplingTime = 0.02;
    static final int ROWS = 10000, COLS = 2;
    static double Plot_Data[][] = new double[ROWS][COLS];
    static double pRate = 0;
    static int Pb_NValues_RED=0, Pb_NValues_IRED=0,mCount=0;//mCount = measurement count
    static int NMeasurement=1;//change here for increasing number of measurements
    static Chart2D chart1;
    static Chart2D chart2;
    static boolean flag = false;
    static boolean flagMeasure = false;
    static final int NTRACES = 2;
    static ITrace2D mtraces[] = new ITrace2D[2];
    private SimpleAttributeSet TextSet = new SimpleAttributeSet();
    private Timer DispUpdate_Timer;
    private JPanel Gr_panel;
    private JPanel Gr_panel1;
    private JTextPane tout_textPane;
    private JButton btnExit;
    private JButton btnClear;
    private JButton btnDisconnect;
    private JTextField textRaw;
    private JTextField textFiltered;
    private JTextField textField;
}
