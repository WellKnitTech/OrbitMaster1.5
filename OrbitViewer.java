/**
 * Orbit Projector
 *
 * Example (Comet)
 *
 *   <APPLET CODE="OrbitViewer" WIDTH=510 HEIGHT=400>
 *   <PARAM NAME="Name"  VALUE="1P/Halley">
 *   <PARAM NAME="T"     VALUE="19860209.7695">
 *   <PARAM NAME="e"     VALUE="0.967267">
 *   <PARAM NAME="q"     VALUE="0.587096">
 *   <PARAM NAME="Peri"  VALUE="111.8466">
 *   <PARAM NAME="Node"  VALUE=" 58.1440">
 *   <PARAM NAME="Incl"  VALUE="162.2393">
 *   <PARAM NAME="Eqnx"  VALUE="1950.0">
 *   </APPLET>
 *
 * Example (Minor Planet)
 *
 *   <APPLET CODE="OrbitViewer" WIDTH=510 HEIGHT=400>
 *   <PARAM NAME="Name"  VALUE="Ceres(1)">
 *   <PARAM NAME="Epoch" VALUE="19991118.5">
 *   <PARAM NAME="M"     VALUE="356.648434">
 *   <PARAM NAME="e"     VALUE="0.07831587">
 *   <PARAM NAME="a"     VALUE="2.76631592">
 *   <PARAM NAME="Peri"  VALUE=" 73.917708">
 *   <PARAM NAME="Node"  VALUE=" 80.495123">
 *   <PARAM NAME="Incl"  VALUE=" 10.583393">
 *   <PARAM NAME="Eqnx"  VALUE="2000.0">
 *   </APPLET>
 *
 * Optional parameter "Date" specifies initial date to display.
 * If "Date" parameter omitted, it start with current date.
 *
 *   <PARAM NAME="Date"  VALUE="19860209.7695">
 *
 */

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.math.*;
import java.text.DecimalFormat;	// (awp)
import astro.*;
import util.*;					// (awp)
import javax.swing.JOptionPane; // (awp)
import java.lang.Thread;		// (awp)
//import javax.swing.SwingConstants; //fiddling (awp)


/**
 * Main Applet Class
 */
public class OrbitViewer extends java.applet.Applet {
	/**
	 * Components
	 */
	public static String newline = System.getProperty("line.separator");
	
	private Color			myPaleGray = new Color(230, 230, 230);
	
	private Scrollbar		scrollA;
	private TextField		textA;
	private Scrollbar		scrollE;
	private TextField		textE;
	private Scrollbar		scrollIncl;
	private TextField		textIncl;
	private Scrollbar		scrollNode;
	private TextField		textNode;
	private Scrollbar		scrollPeri;
	private TextField		textPeri;
	private Scrollbar		scrollM;
	private TextField		textM;
	
	private Scrollbar		scrollHorz;
	private Scrollbar		scrollVert;
	private Scrollbar		scrollZoom;
	private TextField		textZoom;
	private OrbitCanvas		orbitCanvas;
	private Button			buttonDate;
    private Button			buttonMeasure;
	
	private Label			aLabel;
	public  String			QorA = "a";
	
	public  Comet			object;
    public  Comet			tempobj;
	public  Xyz[]			xyzObs;
	public  String[]		nameObs;
	
	public  int			nClones = 0;
	public  int			nClonesMax = 3000;
	public  int			nClonesPreload = 0;
	public  Comet[]			clone = new Comet[nClonesMax];
	
	private Button			buttonRevPlay;
	private Button			buttonRevStep;
	private Button			buttonStop;
	private Button			buttonForStep;
	private Button			buttonForPlay;
	private Choice			choiceTimeStep;
        private Choice                  choiceCenterObject;
        private Choice                  choiceOrbitObject;
        private Choice                  choicePrimaryObject;
	
	private Checkbox		checkPlanetName;
	private Checkbox		checkObjectName;
    private Checkbox		checkCloneName;
	private Checkbox		checkDistanceLabel;
//	private Checkbox		checkDateLabel;
	private Checkbox		checkFineControl;
	private Checkbox		checkLock;
	private Button			buttonClone;
	
	private DateDialog		dateDialog = null;
	
	private Panel mainPanel;
	
	/**
	 * Player thread
	 */
	private OrbitPlayer		orbitPlayer;
	Thread					playerThread = null;
	
	/**
	 * Current Time Setting
	 */
	private ATime atime;
	public double jdStopped = -1.;
	
	/**
	 * Time step
	 */
	static final int timeStepCount = 9; //11; // 8;
	static final String timeStepLabel[] = {
		"1 Minute", //"10 Minutes",	// (awp)
        "1 Hour",
//		"6 Hours",		// (awp)
		"1 Day",   "3 Days",   "10 Days",
		"1 Month", "(3 Months)", "(6 Months)",
		"(1 Year)"
	};
	static final TimeSpan timeStepSpan[] = {
        new TimeSpan(0, 0,  0, 0,  1, 0.0),		// (awp)
//      new TimeSpan(0, 0,  0, 0, 10, 0.0),		// (awp)
        new TimeSpan(0, 0,  0, 1,  0, 0.0),
//      new TimeSpan(0, 0,  0, 6,  0, 0.0),		// (awp)
		new TimeSpan(0, 0,  1, 0,  0, 0.0),
		new TimeSpan(0, 0,  3, 0,  0, 0.0),
		new TimeSpan(0, 0, 10, 0,  0, 0.0),
		new TimeSpan(0, 1,  0, 0,  0, 0.0),
		new TimeSpan(0, 3,  0, 0,  0, 0.0),
		new TimeSpan(0, 6,  0, 0,  0, 0.0),
		new TimeSpan(1, 0,  0, 0,  0, 0.0),	
	};
	public TimeSpan timeStep = timeStepSpan[2]; // [1];		// see also choiceTimeStep.select(timeStepLabel[...]);
	public int      playDirection = ATime.F_INCTIME;

        /**
         * Centered Object
         */
        static final int CenterObjectCount = 11;
        static final String CenterObjectLabel[] = {
                "Sun",   "Asteroid/Comet", "Mercury", "Venus", "Earth", 
                "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
        };
        public int CenterObjectSelected = 0;
	public int CometTooFar = 0;
	public Xyz bogusCenter = new Xyz(0., 0., 0.);
	public Xyz lastKnownCenter = bogusCenter;

        /**
         * Orbits Displayed
         */
        static final int OrbitDisplayCount = 14;
        static final String OrbitDisplayLabel[] = {
                "Default Orbits", "All Planetary Orbits", "No Orbits", "------",
                "Asteroid/Comet", "Mercury", "Venus", "Earth",
                "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
        };
        public int OrbitCount = 11;
        public boolean OrbitDisplay[] = {false, true, true, true, true, true, true,
                                         false, false, false, false };
        public boolean OrbitDisplayDefault[] = {false, true, true, true, true, true, true,
                                         false, false, false, false };

        /**
         * Primary Object
         */
//        public int PrimaryObjectCount = nClones;
//        public String PrimaryObjectLabel[];
        public int PrimaryObjectSelected = 0;
        public int PrimaryObjectPrevious = 0;
        public int[] cloneHash = new int[nClonesMax];

	
	/**
	 * Limit of ATime
	 */
//	private ATime minATime = new ATime(-30000,1,1,0,0,0.0,0.0);
        private ATime minATime = new ATime( 1600,1,1,0,0,0.0,0.0);
	private ATime maxATime = new ATime( 2200,1,1,0,0,0.0,0.0);
	
	/**
	 * Initial Settings
	 */
	static final int factorA	= 100000;			// (awp)
	static final int factorE	= 100000;			// (awp)
	static final int factorIncl	= 10000;			// (awp)
	static final int factorNode	= 10000;			// (awp)
	static final int factorPeri	= 10000;			// (awp)
	static final int factorM	= 10000;			// (awp)
	DecimalFormat aeFormat =  new DecimalFormat("##0.00000");	// (awp)
	DecimalFormat degFormat = new DecimalFormat("##0.0000");	// (awp)
	
	static final int initialScrollVert = 90+40;
	static final int initialScrollHorz = 255;
//	static final int initialScrollZoom = 67;
	static private final int intExtraZoom = 20;	// see extraZoom in OrbitCanvas
	public int initialScrollZoom = 67*intExtraZoom;
	static final int fontSize = 12; // 16;
	static final int textFieldFontSize = 10;
	
	/**
	 * Scroll Increment Settings (awp)
	 */
	static final double blockA        = 0.10;  // AU
	static final double  unitA        = 0.01;  // 
	static final double blockE        = 0.010; // unitless
	static final double  unitE        = 0.001; // 
	static final double blockIncl     = 1.0;   // deg
	static final double  unitIncl     = 0.1;   // 
	static final double blockNode     = 1.0;   // deg
	static final double  unitNode     = 0.1;   // 
	static final double blockPeri     = 1.0;   // deg
	static final double  unitPeri     = 0.1;   // 
	static final double blockM        = 1.0;   // deg
	static final double  unitM        = 0.1;   // 
	
	static final double blockFineA    = 0.00010; // AU
	static final double  unitFineA    = 0.00001; // 
	static final double blockFineE    = 0.00010; // unitless
	static final double  unitFineE    = 0.00001; // 
	static final double blockFineIncl = 0.010;   // deg
	static final double  unitFineIncl = 0.001;   // 
	static final double blockFineNode = 0.010;   // deg
	static final double  unitFineNode = 0.001;   // 
	static final double blockFinePeri = 0.010;   // deg
	static final double  unitFinePeri = 0.001;   // 
	static final double blockFineM    = 0.0010;   // deg
	static final double  unitFineM    = 0.0001;   // 
	
	/**
	 * Applet information
	 */
	public String getAppletInfo() {
		return "OrbitMaster v1.0 Copyright(C) 2010 by A.W.Puckett,"+newline+
			   "   based on OrbitViewer v1.3 (C) 1996-2001 by O.Ajiki/R.Baalke";
	}
	
	/**
	 * Parameter Information
	 */
	public String[][] getParameterInfo() {
		String info[][] = {
			{ "Name",	"String",	"Name of the object          ex. 1P/Halley"     },
			{ "T",		"double",	"Time of perihelion passage  ex. 19860209.7695" },
			{ "e",		"double",	"Eccentricity                ex. 0.967267"      },
			{ "q",		"double",	"Perihelion distance AU      ex. 0.587096"      },
			{ "Peri",	"double",	"Argument of perihelion deg. ex. 111.8466"      },
			{ "Node",	"double",	"Ascending node deg.         ex.  58.1440"      },
			{ "Incl",	"double",	"Inclination deg.            ex. 162.2393"      },
			{ "Eqnx",	"double",	"Year of equinox             ex. 1950.0"        },
			{ "Epoch",	"double",	"Year/Month/Day of epoch     ex. 19991118.5"    },
			{ "M",		"double",	"Mean anomaly deg.           ex. 356.648434"    },
			{ "a",		"double",	"Semimajor axis AU           ex. 2.76631592"    },
			{ "Date",	"double",	"Initial date                ex. 19860209.7695" },
		};
		return info;
	}
	
	/**
	 * Convert time in format "YYYYMMDD.D" to ATime
	 */
	private ATime ymdStringToAtime(String strYmd) {
		double fYmd = Double.valueOf(strYmd).doubleValue();
		int nYear = (int)Math.floor(fYmd / 10000.0);
		fYmd -= (double)nYear * 10000.0;
		int nMonth = (int)Math.floor(fYmd / 100.0);
		double fDay = fYmd - (double)nMonth * 100.0;
		return new ATime(nYear, nMonth, fDay, 0.0);
	}
	
	/**
	 * Get required double parameter
	 */
	private double getRequiredParameter(String strName) {
		String strValue = getParameter(strName);
		if (strValue == null) {
			throw new Error("Required parameter '"
							   + strName + "' not found.");
		}
		return Double.valueOf(strValue).doubleValue();
	}
	
	/**
	 * Get orbital parameters of the object from applet parameter [read / parse]
	 */
	private Comet getObject(int cloneNum) {
		String prefix = "";
		if (cloneNum > 0) {
			prefix = cloneNum+prefix+"-";
		//	System.out.println("Reading Clone #"+prefix);
		} else {
		//	System.out.println("Reading primary orbit");
		}
		
		//		System.out.println("Reading applet params");
		String strName = getParameter(prefix+"Name");
		if (strName == null) {
			strName = "Object";
		}
		double e, q;
		ATime T;
		String strParam;
		if ((strParam = getParameter(prefix+"e")) == null) {
			throw new Error("required parameter 'e' not found.");
		}
		e = Double.valueOf(strParam).doubleValue();
		if ((strParam = getParameter(prefix+"T")) != null) {
			T = ymdStringToAtime(strParam);
			if ((strParam = getParameter(prefix+"q")) != null) {
				q = Double.valueOf(strParam).doubleValue();
			} else if ((strParam = getParameter(prefix+"a")) != null) {
				double a = Double.valueOf(strParam).doubleValue();
				if (Math.abs(e - 1.0) < 1.0e-15) {
					throw new Error("Orbit is parabolic, but 'q' not found.");
				}
				q = a * (1.0 - e);
			} else {
				throw new Error("Required parameter 'q' or 'a' not found.");
			}
		} else if ((strParam = getParameter(prefix+"Epoch")) != null) {
			ATime Epoch = ymdStringToAtime(strParam);
			if (e > 0.95) {
				throw new
					Error("Orbit is nearly parabolic, but 'T' not found.");
			}
			double a;
			if ((strParam = getParameter(prefix+"a")) != null) {
				a = Double.valueOf(strParam).doubleValue();
				q = a * (1.0 - e);
			} else if ((strParam = getParameter(prefix+"q")) != null) {
				q = Double.valueOf(strParam).doubleValue();
				a = q / (1.0 - e);
			} else {
				throw new Error("Required parameter 'q' or 'a' not found.");
			}
			if (q < 1.0e-15) {
				throw new Error("Too small perihelion distance.");
			}
			double n = Astro.GAUSS / (a * Math.sqrt(a));
			if ((strParam = getParameter(prefix+"M")) == null) {
				throw new Error("Required parameter 'M' not found.");
			}
			double M = Double.valueOf(strParam).doubleValue() * Math.PI / 180.0;
			if (M < Math.PI) {
				T = new ATime(Epoch.getJd() - M / n, 0.0);
			} else {
				T = new ATime(Epoch.getJd() + (Math.PI*2.0 - M) / n, 0.0);
			}
		} else {
			throw new Error("Required parameter 'T' or 'Epoch' not found.");
		}
		
//		System.out.println("1 initialScrollZoom = "+initialScrollZoom);
//		initialScrollZoom = Math.min((int)Math.round(200./(q/(1.0 - e)))*intExtraZoom, initialScrollZoom);
		initialScrollZoom = Math.min((int)Math.ceil(200./(q/(1.0 - e)))*intExtraZoom, initialScrollZoom);
//		System.out.println("2 initialScrollZoom = "+initialScrollZoom);

		
		return new Comet(strName, T.getJd(), e, q,
						 getRequiredParameter(prefix+"Peri")*Math.PI/180.0,
						 getRequiredParameter(prefix+"Node")*Math.PI/180.0,
						 getRequiredParameter(prefix+"Incl")*Math.PI/180.0,
						 getRequiredParameter(prefix+"Eqnx"));
	}
	
	/**
	 * Get observation points (x,y,z) from applet parameter [read / parse]
	 */
	private Xyz[] getObs() {
//		System.out.println("Reading applet params (xyz obs)");
		
		String   strObs      = getParameter("xyz");
		if (strObs != null) {
//			System.out.println("all obs: "+strObs);
			String[] strArr;
			String[] strArr1 = strObs.split(": *");
			int numObs = strArr1.length;
//			System.out.println("#obs = "+numObs);
			Xyz[]    xyzObs  = new Xyz[numObs];
			String[] nameObs = new String[numObs];
			
			for (int i=0; i<numObs; i++) {
				strArr   = strArr1[i].trim().split(" +");
				xyzObs[i] = new Xyz(Double.valueOf(strArr[0]),
									Double.valueOf(strArr[1]),
									Double.valueOf(strArr[2]));
//				System.out.println("strArr.length = "+strArr.length);
				if (strArr.length > 3) {
					nameObs[i] = strArr[3];
				} else {
					nameObs[i] = "";
				}
//				System.out.println("obs name = "+nameObs[i]);
			}
			this.nameObs = nameObs;
			return xyzObs;
		} else {
			return null;
		}
	}
	
	/**
	 * Limit ATime between minATime and maxATime
	 */
	private ATime limitATime(ATime atime) {
		if (atime.getJd() <= minATime.getJd()) {
			return new ATime(minATime);
		} else if (maxATime.getJd() <= atime.getJd()) {
			return new ATime(maxATime);
		}
		return atime;
	}
	
	/**
	 * Set date and redraw canvas
	 */
	private void setNewDate() {				// Used for single time-steps
		this.atime = limitATime(this.atime);
		orbitCanvas.setDate(this.atime);
		orbitCanvas.repaint();
	}
	
	/**
	 * OrbitPlayer interface
	 */
	public ATime getAtime() {
		return atime;
	}
	public void setNewDate(ATime atime) {	// Used for continuous animation
		this.atime = limitATime(atime);
		orbitCanvas.setDate(this.atime);
		orbitCanvas.repaint();
	}
	
	/**
	 * Initialization of applet
	 */
	public void init() {
	
		this.setBackground(Color.white);
		
		//*********************************************************************
		// Main Panel
		//*********************************************************************
//		Panel mainPanel = new Panel();
		mainPanel = new Panel();
		GridBagLayout gblMainPanel = new GridBagLayout();
		GridBagConstraints gbcMainPanel = new GridBagConstraints();
		gbcMainPanel.fill = GridBagConstraints.BOTH;
		mainPanel.setLayout(gblMainPanel);
		
		// Orbit Canvas
		object = getObject(0);		// INIT ORBIT PARAMS ARE READ-IN HERE.
		
        // About cloneHash[]:
        //-------------------
        // spits out clone[] indices:  -1=obj, 0=zerothClone, etc
        // cloneHash[] indices are positions on choicePrimary list
        cloneHash[0] = -1;      // initially cloneHash[1]=0, cloneHash[2]=1, etc...
        
		String strValue = getParameter("nClones");
		if (strValue != null) {
			nClonesPreload = Integer.parseInt(strValue);
		//	System.out.println("Preloading "+nClonesPreload+" clones");
			
			if (nClones != 0) {
				throw new Error("nClones="+nClones+", nonzero when reading applet params!  Why?");
			}
			
			for (int i=0; i<nClonesPreload; i++) {
		//		System.out.println("i="+i+"; nClones="+nClones);
				clone[nClones] = getObject(nClones+1);
                cloneHash[i+1] = nClones;               // e.g., cloneHash[1] = 0
				nClones++;
			}
		//	System.out.println("End: nClones="+nClones);
		}
		
		xyzObs = getObs();
		String strParam;
		if ((strParam = getParameter("Date")) != null) {
			this.atime = ymdStringToAtime(strParam);
		} else {
			Date date = new Date();
			this.atime = new ATime(date.getYear() + 1900, date.getMonth() + 1,
								   (double)date.getDate(), 0.0);
		}
		orbitCanvas = new OrbitCanvas(object, this.atime, this, xyzObs, this.nameObs);
		gbcMainPanel.weightx = 1.0;
		gbcMainPanel.weighty = 1.0;
		gbcMainPanel.gridwidth = GridBagConstraints.RELATIVE;
		gblMainPanel.setConstraints(orbitCanvas, gbcMainPanel);
		mainPanel.add(orbitCanvas);
		
		// Vertical Scrollbar
		scrollVert = new Scrollbar(Scrollbar.VERTICAL,
						initialScrollVert, 12, 0, 180+12);
		gbcMainPanel.weightx = 0.0;
		gbcMainPanel.weighty = 0.0;
		gbcMainPanel.gridwidth = GridBagConstraints.REMAINDER;
		gblMainPanel.setConstraints(scrollVert, gbcMainPanel);
		mainPanel.add(scrollVert);
		orbitCanvas.setRotateVert(180 - scrollVert.getValue());
		
		// Horizontal Scrollbar
		scrollHorz = new Scrollbar(Scrollbar.HORIZONTAL,
						initialScrollHorz, 15, 0, 360+15);
		gbcMainPanel.weightx = 1.0;
		gbcMainPanel.weighty = 0.0;
		gbcMainPanel.gridwidth = 1;
		gblMainPanel.setConstraints(scrollHorz, gbcMainPanel);
		mainPanel.add(scrollHorz);
		// Initializes the scrollbar?  (Event Handler is below...)
		orbitCanvas.setRotateHorz(270 - scrollHorz.getValue());
		
		// Right-Bottom Corner Rectangle
		Panel cornerPanel = new Panel();
		gbcMainPanel.weightx = 0.0;
		gbcMainPanel.weighty = 0.0;
		gbcMainPanel.gridwidth = GridBagConstraints.REMAINDER;
		gblMainPanel.setConstraints(cornerPanel, gbcMainPanel);
		mainPanel.add(cornerPanel);
		
		
		//*********************************************************************
		// Control Panel
		//*********************************************************************
		Panel ctrlPanel = new Panel();
		GridBagLayout gblCtrlPanel = new GridBagLayout();
		GridBagConstraints gbcCtrlPanel = new GridBagConstraints();
		gbcCtrlPanel.fill = GridBagConstraints.BOTH;
		ctrlPanel.setLayout(gblCtrlPanel);
		ctrlPanel.setBackground(myPaleGray); //white);
		
		// Set Date Button
		buttonDate = new Button(" Date ");
		buttonDate.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 4; //compacting 0;
		gbcCtrlPanel.gridy = 1; //compacting 0;
		gbcCtrlPanel.weightx = 1.0;  //fiddling 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 2; // 1; //compacting 2;
//		gbcCtrlPanel.insets = new Insets(2, 0, 2, 12);	// TLBR
		gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
		gblCtrlPanel.setConstraints(buttonDate, gbcCtrlPanel);
		ctrlPanel.add(buttonDate);
		
		// Reverse-Play Button
		buttonRevPlay = new Button("<<");
		buttonRevPlay.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
		gbcCtrlPanel.gridx = 0;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 1.0;  //fiddling 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
		gblCtrlPanel.setConstraints(buttonRevPlay, gbcCtrlPanel);
		ctrlPanel.add(buttonRevPlay);
		
		// Reverse-Step Button
		buttonRevStep = new Button("|<");
		buttonRevStep.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
		gbcCtrlPanel.gridx = 1;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 1.0;  //fiddling 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
		gblCtrlPanel.setConstraints(buttonRevStep, gbcCtrlPanel);
		ctrlPanel.add(buttonRevStep);
		
		// Stop Button
		buttonStop = new Button("||");
		buttonStop.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
		gbcCtrlPanel.gridx = 2;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 1.0;  //fiddling 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
		gblCtrlPanel.setConstraints(buttonStop, gbcCtrlPanel);
		ctrlPanel.add(buttonStop);
		
		// Step Button
		buttonForStep = new Button(">|");
		buttonForStep.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
		gbcCtrlPanel.gridx = 3;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 1.0;  //fiddling 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
		gblCtrlPanel.setConstraints(buttonForStep, gbcCtrlPanel);
		ctrlPanel.add(buttonForStep);
		
		// Play Button
		buttonForPlay = new Button(">>");
		buttonForPlay.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
		gbcCtrlPanel.gridx = 4;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 1.0;  //fiddling 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
		gblCtrlPanel.setConstraints(buttonForPlay, gbcCtrlPanel);
		ctrlPanel.add(buttonForPlay);
		
        // Step choice Label
        Label stepLabel = new Label("Time Step: ");
        stepLabel.setAlignment(Label.RIGHT); // .LEFT);
        stepLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
        gbcCtrlPanel.gridx = 0;
        gbcCtrlPanel.gridy = 1;
        gbcCtrlPanel.weightx = 0.01;  //fiddling 0.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1;
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
        gblCtrlPanel.setConstraints(stepLabel, gbcCtrlPanel);
        ctrlPanel.add(stepLabel);
        
		// Step choice box
		choiceTimeStep = new Choice();
		choiceTimeStep.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 1; //compacting 1;
		gbcCtrlPanel.gridy = 1;
		gbcCtrlPanel.weightx = 1.0;  //fiddling 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 3; //compacting 5;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
		gblCtrlPanel.setConstraints(choiceTimeStep, gbcCtrlPanel);
		ctrlPanel.add(choiceTimeStep);
		for (int i = 0; i < timeStepCount; i++) {
			choiceTimeStep.addItem(timeStepLabel[i]);
                choiceTimeStep.select(timeStepLabel[2]); // [1]);	// see also timeStep = timeStepSpan[...];
		}

               // Center Object Label
                Label centerLabel = new Label("Center: ");
                centerLabel.setAlignment(Label.RIGHT); // .LEFT);
                centerLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
                gbcCtrlPanel.gridx = 0;
                gbcCtrlPanel.gridy = 2;
                gbcCtrlPanel.weightx = 0.01;  //fiddling 0.0;
                gbcCtrlPanel.weighty = 0.0;
                gbcCtrlPanel.gridwidth = 1;
                gbcCtrlPanel.gridheight = 1;
                gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
                gblCtrlPanel.setConstraints(centerLabel, gbcCtrlPanel);
                ctrlPanel.add(centerLabel);

               // Center Object choice box
                choiceCenterObject = new Choice();
                choiceCenterObject.setFont(new Font("Dialog", Font.PLAIN, fontSize));
                gbcCtrlPanel.gridx = 1;
                gbcCtrlPanel.gridy = 2;
                gbcCtrlPanel.weightx = 0.01;  //fiddling 0.0;
                gbcCtrlPanel.weighty = 0.0;
                gbcCtrlPanel.gridwidth = 3; //compacting 5;
                gbcCtrlPanel.gridheight = 1;
                gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
                gblCtrlPanel.setConstraints(choiceCenterObject, gbcCtrlPanel);
                ctrlPanel.add(choiceCenterObject);
                for (int i = 0; i < CenterObjectCount; i++) {
                        choiceCenterObject.addItem(CenterObjectLabel[i]);
                }
                orbitCanvas.SelectCenterObject(0);

               // Display Orbits Label
                Label orbitLabel = new Label("Orbits: ");
                orbitLabel.setAlignment(Label.RIGHT); // .LEFT);
                orbitLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
                gbcCtrlPanel.gridx = 0;
                gbcCtrlPanel.gridy = 3;
                gbcCtrlPanel.weightx = 0.01;  //fiddling 0.0;
                gbcCtrlPanel.weighty = 0.0;
                gbcCtrlPanel.gridwidth = 1;
                gbcCtrlPanel.gridheight = 1;
                gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
                gblCtrlPanel.setConstraints(orbitLabel, gbcCtrlPanel);
                ctrlPanel.add(orbitLabel);

              // Display Orbit choice box
                choiceOrbitObject = new Choice();
                choiceOrbitObject.setFont(new Font("Dialog", Font.PLAIN, fontSize));
                gbcCtrlPanel.gridx = 1;
                gbcCtrlPanel.gridy = 3;
                gbcCtrlPanel.weightx = 0.01;  //fiddling 0.0;
                gbcCtrlPanel.weighty = 0.0;
                gbcCtrlPanel.gridwidth = 3; //compacting 5;
                gbcCtrlPanel.gridheight = 1;
                gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
                gblCtrlPanel.setConstraints(choiceOrbitObject, gbcCtrlPanel);
                ctrlPanel.add(choiceOrbitObject);
                for (int i = 0; i < OrbitDisplayCount; i++) {
                        choiceOrbitObject.addItem(OrbitDisplayLabel[i]);
                }
                for (int i = 0; i < OrbitCount; i++) {
                        OrbitDisplay[i] = OrbitDisplayDefault[i];
                }
                orbitCanvas.SelectOrbits(OrbitDisplay, OrbitCount);

        // Display Primary Label
        Label primaryLabel = new Label("Primary: ");
        primaryLabel.setAlignment(Label.RIGHT); // .LEFT);
        primaryLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
        gbcCtrlPanel.gridx = 0;
        gbcCtrlPanel.gridy = 4;
        gbcCtrlPanel.weightx = 0.01;  //fiddling 0.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1;
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
        gblCtrlPanel.setConstraints(primaryLabel, gbcCtrlPanel);
        ctrlPanel.add(primaryLabel);
        
        // Display Primary choice box
        choicePrimaryObject = new Choice();
        choicePrimaryObject.setFont(new Font("Dialog", Font.PLAIN, fontSize));
        gbcCtrlPanel.gridx = 1;
        gbcCtrlPanel.gridy = 4;
        gbcCtrlPanel.weightx = 0.01;  //fiddling 0.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 3; //compacting 5;
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
        gblCtrlPanel.setConstraints(choicePrimaryObject, gbcCtrlPanel);
        ctrlPanel.add(choicePrimaryObject);
        choicePrimaryObject.addItem(object.getName());
        for (int i = 0; i < nClones; i++) {
            choicePrimaryObject.addItem(clone[i].getName());
        }
//        orbitCanvas.SelectPrimaryObject(0);

        
        // Display Labels Label
        Label labelLabel = new Label("Labels: ");
        labelLabel.setAlignment(Label.RIGHT); // .LEFT);
        labelLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
        gbcCtrlPanel.gridx = 0;
        gbcCtrlPanel.gridy = 5;
        gbcCtrlPanel.weightx = 0.01;  //fiddling 0.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1;
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
        gblCtrlPanel.setConstraints(labelLabel, gbcCtrlPanel);
        ctrlPanel.add(labelLabel);
		
		// Date Label Checkbox
/*        checkDateLabel = new Checkbox("Date"); // Label");
		checkDateLabel.setState(true);
		checkDateLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 1; //compacting 2; //fiddling 0;		// 6;
		gbcCtrlPanel.gridy = 4;                                     // 0;
		gbcCtrlPanel.weightx = 1.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1; //was 2
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(2, 9, 0, 0);	// TLBR
		gblCtrlPanel.setConstraints(checkDateLabel, gbcCtrlPanel);
		ctrlPanel.add(checkDateLabel);
		orbitCanvas.switchDateLabel(checkDateLabel.getState());
*/
		// Info (Distance) Label Checkbox
		checkDistanceLabel = new Checkbox("Info");
		checkDistanceLabel.setState(true);
		checkDistanceLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 1; //compacting 2; //fiddling 0;		// 6;
		gbcCtrlPanel.gridy = 5;                                     // 1;
		gbcCtrlPanel.weightx = 1.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1; //was 2
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 9, 2, 0);	// TLBR
		gblCtrlPanel.setConstraints(checkDistanceLabel, gbcCtrlPanel);
		ctrlPanel.add(checkDistanceLabel);
		orbitCanvas.switchDistanceLabel(checkDistanceLabel.getState());
        orbitCanvas.switchDateLabel(checkDistanceLabel.getState());
		
		// Object Name Checkbox
		checkObjectName = new Checkbox("Object");
		checkObjectName.setState(true);
		checkObjectName.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 3;
		gbcCtrlPanel.gridy = 5;
		gbcCtrlPanel.weightx = 1.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1; //was 2
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 9, 2, 0);	// TLBR
		gblCtrlPanel.setConstraints(checkObjectName, gbcCtrlPanel);
		ctrlPanel.add(checkObjectName);
		orbitCanvas.switchObjectName(checkObjectName.getState());
        
        // Clones/Peers Name Checkbox
//      checkCloneName = new Checkbox("Clones/Peers");
        String initPeers = getParameter("Peers");
        if (initPeers == null) {
            initPeers = "False";
        }
        if (initPeers.equals("True") || initPeers.equals("TRUE") || initPeers.equals("T") || initPeers.equals("t") ||
            initPeers.equals("Yes")  || initPeers.equals("YES")  || initPeers.equals("Y") || initPeers.equals("y")) {
            checkCloneName = new Checkbox("Peers");
            checkCloneName.setState(true);
        } else {
            checkCloneName = new Checkbox("Clones");
            checkCloneName.setState(false);
        }
        checkCloneName.setFont(new Font("Dialog", Font.PLAIN, fontSize));
        gbcCtrlPanel.gridx = 4;
        gbcCtrlPanel.gridy = 5;
        gbcCtrlPanel.weightx = 1.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1;
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(0, 9, 2, 0);	// TLBR
        gblCtrlPanel.setConstraints(checkCloneName, gbcCtrlPanel);
        ctrlPanel.add(checkCloneName);
        orbitCanvas.switchCloneName(checkCloneName.getState());
		
        // Planet Name Checkbox
        checkPlanetName = new Checkbox("Planets");
        checkPlanetName.setState(true);
        checkPlanetName.setFont(new Font("Dialog", Font.PLAIN, fontSize));
        gbcCtrlPanel.gridx = 2; //compacting 4; //fiddling 3;		// 7;
        gbcCtrlPanel.gridy = 5;                                     // 0;
        gbcCtrlPanel.weightx = 1.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1; //was 2
        gbcCtrlPanel.gridheight = 1;
        gbcCtrlPanel.insets = new Insets(0, 9, 2, 0);	// TLBR
        gblCtrlPanel.setConstraints(checkPlanetName, gbcCtrlPanel);
        ctrlPanel.add(checkPlanetName);
        orbitCanvas.switchPlanetName(checkPlanetName.getState());
        
        // Measurement Button
        buttonMeasure = new Button("Measure");
        buttonMeasure.setFont(new Font("Dialog", Font.PLAIN, fontSize));
        gbcCtrlPanel.gridx = 4;
        gbcCtrlPanel.gridy = 3;
        gbcCtrlPanel.weightx = 0.0;
        gbcCtrlPanel.weighty = 0.0;
        gbcCtrlPanel.gridwidth = 1;
        gbcCtrlPanel.gridheight = 2;
        gbcCtrlPanel.insets = new Insets(2, 0, 2, 0);	// TLBR
        gblCtrlPanel.setConstraints(buttonMeasure, gbcCtrlPanel);
        ctrlPanel.add(buttonMeasure);
        buttonMeasure.setEnabled(false);


		// Zoom Label
		Label zoomLabel = new Label("Zoom: ");
		zoomLabel.setAlignment(Label.RIGHT); //fiddling .LEFT);
		zoomLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 0;		// 6;
		gbcCtrlPanel.gridy = 6;		// 2;
		gbcCtrlPanel.weightx = 1.0;  //fiddling 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;	// 2;
		gbcCtrlPanel.gridheight = 1;
//		gbcCtrlPanel.insets = new Insets(2, 0, 0, 0);	// TLBR
		gbcCtrlPanel.insets = new Insets(4, 0, 0, 0);	// TLBR
		gblCtrlPanel.setConstraints(zoomLabel, gbcCtrlPanel);
		ctrlPanel.add(zoomLabel);
		
		// Zoom Scrollbar (orient, init value, visible, min, max)
		scrollZoom = new Scrollbar(Scrollbar.HORIZONTAL,
					initialScrollZoom, 15*intExtraZoom, 1, 450*intExtraZoom); // 5, 450);
		gbcCtrlPanel.gridx = 1;
		gbcCtrlPanel.gridy = 6;
		gbcCtrlPanel.weightx = 1.0;  //fiddling 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth  = 3;	// 5;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(4, 5, 6, 5); // (4, 0, 6, 0);	// TLBR
		gblCtrlPanel.setConstraints(scrollZoom, gbcCtrlPanel);
		ctrlPanel.add(scrollZoom);
		orbitCanvas.setZoom(scrollZoom.getValue());
		
		// Zoom Textfield (awp)
		textZoom = new TextField(""+initialScrollZoom); //scrollZoom.getValue());
		textZoom.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		textZoom.addKeyListener( new IntegerKeyListener(textZoom) );
		textZoom.addTextListener( new TextListener() {
		    public void textValueChanged(TextEvent e) {
				if (textZoom.isFocusOwner()) {		// Do nothing when Scroll changes Text
					int newZoomText;
					if (textZoom.getText().length() == 0) {
						newZoomText = 1;
					} else if (Integer.valueOf(textZoom.getText()) < 1) {
						newZoomText = 1;
						textZoom.setText(""+newZoomText);
					} else {
						newZoomText = Integer.valueOf(textZoom.getText());
					}
					orbitCanvas.setZoom(newZoomText);
					scrollZoom.setValue(newZoomText);
					orbitCanvas.repaint();
				}
		    }
		});
		gbcCtrlPanel.gridx = 4;
		gbcCtrlPanel.gridy = 6;
		gbcCtrlPanel.weightx = 1.0;  //fiddling 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(3, 3, 4, 5); // 1);	// TLBR
		gblCtrlPanel.setConstraints(textZoom, gbcCtrlPanel);
		ctrlPanel.add(textZoom);
		
		
		//*********************************************************************
		// Elements Panel
		//*********************************************************************
		
		Panel eltsPanel = new Panel();
		GridBagLayout gblEltsPanel = new GridBagLayout();
		GridBagConstraints gbcEltsPanel = new GridBagConstraints();
		gbcEltsPanel.fill = GridBagConstraints.HORIZONTAL; //BOTH;
		eltsPanel.setLayout(gblEltsPanel);
		eltsPanel.setBackground(myPaleGray); //white);

		// Elements Label (awp)
		Label eltsLabel = new Label("Orbital Parameters:");
		eltsLabel.setAlignment(Label.LEFT);
		eltsLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcEltsPanel.gridx = 0;
		gbcEltsPanel.gridy = 0;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 1.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(6, 10, 0, 0);	// TLBR
		gblEltsPanel.setConstraints(eltsLabel, gbcEltsPanel);
		eltsPanel.add(eltsLabel);
		
		// Clone Button (awp)
		buttonClone = new Button("Clone");
		buttonClone.setFont(new Font("Dialog", Font.PLAIN, fontSize-2));
		gbcEltsPanel.fill = GridBagConstraints.NONE; //BOTH;
		gbcEltsPanel.anchor = GridBagConstraints.WEST;
		gbcEltsPanel.gridx = 1;
		gbcEltsPanel.gridy = 0;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 1.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(3, 5, 0, 5);	// TLBR
		gblEltsPanel.setConstraints(buttonClone, gbcEltsPanel);
		eltsPanel.add(buttonClone);
        if (nClonesPreload == 0) {		// If clones were preloaded, then don't allow user to create more!
            buttonClone.setEnabled(true);
        } else {
            buttonClone.setEnabled(false);
        }
//		buttonClone.setEnabled(false);

		
		// Fine Control Checkbox
		checkFineControl = new Checkbox("Fine"); //fiddling Control");
		checkFineControl.setState(false);
		checkFineControl.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcEltsPanel.fill = GridBagConstraints.NONE; //BOTH;
		gbcEltsPanel.anchor = GridBagConstraints.WEST;
		gbcEltsPanel.gridx = 3;
		gbcEltsPanel.gridy = 0;
		gbcEltsPanel.weightx = 0.0; //fiddling??
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 2;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(6, 0, 0, 10);	// TLBR
		gblEltsPanel.setConstraints(checkFineControl, gbcEltsPanel);
		eltsPanel.add(checkFineControl);
		
		// Lock Checkbox comes after elements controls, which must exist first!
				
		gbcEltsPanel.fill = GridBagConstraints.HORIZONTAL; //BOTH;
		
		// A Label (awp) --------------------------------------------------------------------------
		QorA = "a";
		aLabel = new Label("Average Distance (a):");
		aLabel.setAlignment(Label.RIGHT);
		aLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
/*		aLabel.addMouseListener( new MouseListener() {
		    public void mouseClicked(MouseEvent e) {
			if (QorA == "q") {
				aLabel.setText("Average Distance (a):");
				QorA = "a";
			} else {
				aLabel.setText("Perihelion Distance (q):");
				QorA = "q";
			}
		    }
		    public void mouseEntered(MouseEvent e) {
		    }
		    public void mouseExited(MouseEvent e) {
		    }
		    public void mousePressed(MouseEvent e) {
		    }
		    public void mouseReleased(MouseEvent e) {
		    }
		});
*/		gbcEltsPanel.gridx = 0;
		gbcEltsPanel.gridy = 1;
		gbcEltsPanel.weightx = 0.001; //fiddling??
		gbcEltsPanel.weighty = 1.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(2, 10, 0, 0);	// TLBR
		gblEltsPanel.setConstraints(aLabel, gbcEltsPanel);
		eltsPanel.add(aLabel);
		
		// A Scrollbar (awp)
		scrollA = new Scrollbar(Scrollbar.HORIZONTAL,
				(int)Math.round(object.getA()*factorA), 10, 
				(int)Math.round(0.01*factorA), (10*factorA)+10);
		scrollA.setBlockIncrement((int)Math.round(blockA*factorA));
		scrollA.setUnitIncrement( (int)Math.round( unitA*factorA));
		gbcEltsPanel.gridx = 1;
		gbcEltsPanel.gridy = 1;
		gbcEltsPanel.weightx = 1.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 2; //fiddling 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(4, 5, 6, 5);	// TLBR
		gblEltsPanel.setConstraints(scrollA, gbcEltsPanel);
		eltsPanel.add(scrollA);
		
		// A Textfield (awp)
		textA = new TextField(aeFormat.format(object.getA()));
		textA.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		textA.addKeyListener( new DoubleKeyListener(textA) );
		textA.addTextListener( new TextListener() {
		    public void textValueChanged(TextEvent e) {
			if (textA.isFocusOwner()) {		// Do nothing when Scroll changes Text
				double newAtext;
				if (textA.getText().length() == 0) {
					newAtext = 0.01;
				} else if (Double.valueOf(textA.getText()) < 0.01) {
					newAtext = 0.01;
					textA.setText(aeFormat.format(newAtext));
				} else {
					newAtext = Double.valueOf(textA.getText());
				}
				orbitCanvas.setA(newAtext, atime);
				scrollA.setValue((int)Math.round(newAtext*factorA));
				orbitCanvas.repaint();
			}
		    }
		});
		gbcEltsPanel.gridx = 3; //fiddling 2;
		gbcEltsPanel.gridy = 1;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(3, 3, 4, 1);	// TLBR
		gblEltsPanel.setConstraints(textA, gbcEltsPanel);
		eltsPanel.add(textA);
		
		// A Units Label (awp)
		Label aUnitsLabel = new Label("AU");
		aUnitsLabel.setAlignment(Label.LEFT);
		aUnitsLabel.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		gbcEltsPanel.gridx = 4; //fiddling 3;
		gbcEltsPanel.gridy = 1;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(0, 0, 0, 10);	// TLBR
		gblEltsPanel.setConstraints(aUnitsLabel, gbcEltsPanel);
		eltsPanel.add(aUnitsLabel);
		
		// E Label (awp) --------------------------------------------------------------------------
		Label eLabel = new Label("Eccentricity (e):");
		eLabel.setAlignment(Label.RIGHT);
		eLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcEltsPanel.gridx = 0;
		gbcEltsPanel.gridy = 2;
		gbcEltsPanel.weightx = 0.001; //fiddling??
		gbcEltsPanel.weighty = 1.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(2, 10, 0, 0);	// TLBR
		gblEltsPanel.setConstraints(eLabel, gbcEltsPanel);
		eltsPanel.add(eLabel);
		
		// E Scrollbar (awp)
		scrollE = new Scrollbar(Scrollbar.HORIZONTAL,
				(int)Math.round(object.getE()*factorE), 10, 
//				0, (int)((.98-(1./factorE))*factorE)+10);
				0, (int)((1.0-(1./factorE))*factorE)+10);
		scrollE.setBlockIncrement((int)Math.round(blockE*factorE));
		scrollE.setUnitIncrement( (int)Math.round( unitE*factorE));
		gbcEltsPanel.gridx = 1;
		gbcEltsPanel.gridy = 2;
		gbcEltsPanel.weightx = 1.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 2; //fiddling 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(4, 5, 6, 5);	// TLBR
		gblEltsPanel.setConstraints(scrollE, gbcEltsPanel);
		eltsPanel.add(scrollE);

		// E Textfield (awp)
		textE = new TextField(aeFormat.format(object.getE()));
		textE.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		textE.addKeyListener( new DoubleKeyListener(textE) );
		textE.addTextListener( new TextListener() {
		    public void textValueChanged(TextEvent e) {
			if (textE.isFocusOwner()) {		// Do nothing when Scroll changes Text
				double newEtext;
				if (textE.getText().length() == 0) {
					newEtext = 0.;
//				} else if (Double.valueOf(textE.getText()) > .98-(1./factorE)) {
//					newEtext = .98-(1./factorE);
//					textE.setText(aeFormat.format(newEtext));
				} else if (Double.valueOf(textE.getText()) > 1.0-(1./factorE)) {
					newEtext = 1.0-(1./factorE);
					textE.setText(aeFormat.format(newEtext));
				} else {
					newEtext = Double.valueOf(textE.getText());
				}
				orbitCanvas.setE(newEtext);
				scrollE.setValue((int)Math.round(newEtext*factorE));
				orbitCanvas.repaint();
			}
		    }
		});
		gbcEltsPanel.gridx = 3; //fiddling 2;
		gbcEltsPanel.gridy = 2;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(3, 3, 4, 1);	// TLBR
		gblEltsPanel.setConstraints(textE, gbcEltsPanel);
		eltsPanel.add(textE);
		
		// E Units Label (awp)
		Label eUnitsLabel = new Label("");
		eUnitsLabel.setAlignment(Label.LEFT);
		eUnitsLabel.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		gbcEltsPanel.gridx = 4; //fiddling 3;
		gbcEltsPanel.gridy = 2;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(0, 0, 0, 10);	// TLBR
		gblEltsPanel.setConstraints(eUnitsLabel, gbcEltsPanel);
		eltsPanel.add(eUnitsLabel);
		
		// Incl Label (awp) -----------------------------------------------------------------------
		Label inclLabel = new Label("Inclination (i):");
		inclLabel.setAlignment(Label.RIGHT);
		inclLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcEltsPanel.gridx = 0;
		gbcEltsPanel.gridy = 3;
		gbcEltsPanel.weightx = 0.001; //fiddling??
		gbcEltsPanel.weighty = 1.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(2, 10, 0, 0);	// TLBR
		gblEltsPanel.setConstraints(inclLabel, gbcEltsPanel);
		eltsPanel.add(inclLabel);
		
		// Incl Scrollbar (awp)
		double initialIncl = object.getIncl()*(180./Math.PI);
		scrollIncl = new Scrollbar(Scrollbar.HORIZONTAL,
				(int)Math.round(initialIncl*factorIncl), 10, 
				0, (360*factorIncl)+10);
		scrollIncl.setBlockIncrement((int)Math.round(blockIncl*factorIncl));
		scrollIncl.setUnitIncrement( (int)Math.round( unitIncl*factorIncl));
		gbcEltsPanel.gridx = 1;
		gbcEltsPanel.gridy = 3;
		gbcEltsPanel.weightx = 1.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 2; //fiddling 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(4, 5, 6, 5);	// TLBR
		gblEltsPanel.setConstraints(scrollIncl, gbcEltsPanel);
		eltsPanel.add(scrollIncl);

		// Incl Textfield (awp)
		textIncl = new TextField(degFormat.format(initialIncl));
		textIncl.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		textIncl.addKeyListener( new DoubleKeyListener(textIncl) );
		textIncl.addTextListener( new TextListener() {
		    public void textValueChanged(TextEvent e) {
			if (textIncl.isFocusOwner()) {		// Do nothing when Scroll changes Text
				double newInclDegText;
				if (textIncl.getText().length() == 0) {
					newInclDegText = 0.;
				} else if (Double.valueOf(textIncl.getText()) > 360.) {
					newInclDegText = 360.;
					textIncl.setText(degFormat.format(newInclDegText));
				} else {
					newInclDegText = Double.valueOf(textIncl.getText());
				}
				orbitCanvas.setIncl(newInclDegText*(Math.PI/180.));
				scrollIncl.setValue((int)Math.round(newInclDegText*factorIncl));
				orbitCanvas.repaint();
			}
		    }
		});
		gbcEltsPanel.gridx = 3; //fiddling 2;
		gbcEltsPanel.gridy = 3;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(3, 3, 4, 1);	// TLBR
		gblEltsPanel.setConstraints(textIncl, gbcEltsPanel);
		eltsPanel.add(textIncl);
		
		// Incl Units Label (awp)
		Label inclUnitsLabel = new Label(""+'\u00B0');
		inclUnitsLabel.setAlignment(Label.LEFT);
		inclUnitsLabel.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		gbcEltsPanel.gridx = 4; //fiddling 3;
		gbcEltsPanel.gridy = 3;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(0, 0, 0, 10);	// TLBR
		gblEltsPanel.setConstraints(inclUnitsLabel, gbcEltsPanel);
		eltsPanel.add(inclUnitsLabel);
		
		// Node Label (awp) -----------------------------------------------------------------------
		Label nodeLabel = new Label("Ascending Node ("+'\u03A9'+"):");
		nodeLabel.setAlignment(Label.RIGHT);
		nodeLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcEltsPanel.gridx = 0;
		gbcEltsPanel.gridy = 4;
		gbcEltsPanel.weightx = 0.001; //fiddling??
		gbcEltsPanel.weighty = 1.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(2, 10, 0, 0);	// TLBR
		gblEltsPanel.setConstraints(nodeLabel, gbcEltsPanel);
		eltsPanel.add(nodeLabel);

		// Node Scrollbar (awp)
		double initialNode = object.getNode()*(180./Math.PI);
		scrollNode = new Scrollbar(Scrollbar.HORIZONTAL,
				(int)Math.round(initialNode*factorNode), 10, 
				0, (360*factorNode)+10);
		scrollNode.setBlockIncrement((int)Math.round(blockNode*factorNode));
		scrollNode.setUnitIncrement( (int)Math.round( unitNode*factorNode));
		gbcEltsPanel.gridx = 1;
		gbcEltsPanel.gridy = 4;
		gbcEltsPanel.weightx = 1.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 2; //fiddling 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(4, 5, 6, 5);	// TLBR
		gblEltsPanel.setConstraints(scrollNode, gbcEltsPanel);
		eltsPanel.add(scrollNode);
		
		// Node Textfield (awp)
		textNode = new TextField(degFormat.format(initialNode));
		textNode.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		textNode.addKeyListener( new DoubleKeyListener(textNode) );
		textNode.addTextListener( new TextListener() {
		    public void textValueChanged(TextEvent e) {
			if (textNode.isFocusOwner()) {		// Do nothing when Scroll changes Text
				double newNodeDegText;
				if (textNode.getText().length() == 0) {
					newNodeDegText = 0.;
				} else if (Double.valueOf(textNode.getText()) > 360.) {
					newNodeDegText = 360.;
					textNode.setText(degFormat.format(newNodeDegText));
				} else {
					newNodeDegText = Double.valueOf(textNode.getText());
				}
				orbitCanvas.setNode(newNodeDegText*(Math.PI/180.));
				scrollNode.setValue((int)Math.round(newNodeDegText*factorNode));
				orbitCanvas.repaint();
			}
		    }
		});
		gbcEltsPanel.gridx = 3; //fiddling 2;
		gbcEltsPanel.gridy = 4;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(3, 3, 4, 1);	// TLBR
		gblEltsPanel.setConstraints(textNode, gbcEltsPanel);
		eltsPanel.add(textNode);
		
		// Node Units Label (awp)
		Label nodeUnitsLabel = new Label(""+'\u00B0');
		nodeUnitsLabel.setAlignment(Label.LEFT);
		nodeUnitsLabel.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		gbcEltsPanel.gridx = 4; //fiddling 3;
		gbcEltsPanel.gridy = 4;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(0, 0, 0, 10);	// TLBR
		gblEltsPanel.setConstraints(nodeUnitsLabel, gbcEltsPanel);
		eltsPanel.add(nodeUnitsLabel);
		
		// Peri Label (awp) -----------------------------------------------------------------------
		Label periLabel = new Label("Argument of Perihelion ("+'\u03C9'+"):");
		periLabel.setAlignment(Label.RIGHT);
		periLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcEltsPanel.gridx = 0;
		gbcEltsPanel.gridy = 5;
		gbcEltsPanel.weightx = 0.001; //fiddling??
		gbcEltsPanel.weighty = 1.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(2, 10, 0, 0);	// TLBR
		gblEltsPanel.setConstraints(periLabel, gbcEltsPanel);
		eltsPanel.add(periLabel);

		// Peri Scrollbar (awp)
		double initialPeri = object.getPeri()*(180./Math.PI);
		scrollPeri = new Scrollbar(Scrollbar.HORIZONTAL,
				(int)Math.round(initialPeri*factorPeri), 10, 
				0, (360*factorPeri)+10);
		scrollPeri.setBlockIncrement((int)Math.round(blockPeri*factorPeri));
		scrollPeri.setUnitIncrement( (int)Math.round( unitPeri*factorPeri));
		gbcEltsPanel.gridx = 1;
		gbcEltsPanel.gridy = 5;
		gbcEltsPanel.weightx = 1.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 2; //fiddling 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(4, 5, 6, 5);	// TLBR
		gblEltsPanel.setConstraints(scrollPeri, gbcEltsPanel);
		eltsPanel.add(scrollPeri);
		
		// Peri Textfield (awp)
		textPeri = new TextField(degFormat.format(initialPeri));
		textPeri.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		textPeri.addKeyListener( new DoubleKeyListener(textPeri) );
		textPeri.addTextListener( new TextListener() {
		    public void textValueChanged(TextEvent e) {
			if (textPeri.isFocusOwner()) {		// Do nothing when Scroll changes Text
				double newPeriDegText;
				if (textPeri.getText().length() == 0) {
					newPeriDegText = 0.;
				} else if (Double.valueOf(textPeri.getText()) > 360.) {
					newPeriDegText = 360.;
					textPeri.setText(degFormat.format(newPeriDegText));
				} else {
					newPeriDegText = Double.valueOf(textPeri.getText());
				}
				orbitCanvas.setPeri(newPeriDegText*(Math.PI/180.));
				scrollPeri.setValue((int)Math.round(newPeriDegText*factorIncl));
				orbitCanvas.repaint();
			}
		    }
		});
		gbcEltsPanel.gridx = 3; //fiddling 2;
		gbcEltsPanel.gridy = 5;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(3, 3, 4, 1);	// TLBR
		gblEltsPanel.setConstraints(textPeri, gbcEltsPanel);
		eltsPanel.add(textPeri);
		
		// Peri Units Label (awp)
		Label periUnitsLabel = new Label(""+'\u00B0');
		periUnitsLabel.setAlignment(Label.LEFT);
		periUnitsLabel.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		gbcEltsPanel.gridx = 4; //fiddling 3;
		gbcEltsPanel.gridy = 5;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(0, 0, 0, 10);	// TLBR
		gblEltsPanel.setConstraints(periUnitsLabel, gbcEltsPanel);
		eltsPanel.add(periUnitsLabel);
		
		// M Label (awp) --------------------------------------------------------------------------
		Label mLabel = new Label("Current Mean Anomaly (M):");
		mLabel.setAlignment(Label.RIGHT);
		mLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcEltsPanel.gridx = 0;
		gbcEltsPanel.gridy = 6;
		gbcEltsPanel.weightx = 0.001; //fiddling??
		gbcEltsPanel.weighty = 1.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(2, 10, 4, 0);	// TLBR
		gblEltsPanel.setConstraints(mLabel, gbcEltsPanel);
		eltsPanel.add(mLabel);
		
		// M Scrollbar (awp)
		double initialM = object.getM(atime)*(180./Math.PI);
		while (initialM > 360.) initialM = initialM - 360.;
		while (initialM < 0.)   initialM = initialM + 360.;
		scrollM = new Scrollbar(Scrollbar.HORIZONTAL,
				(int)Math.round(initialM*factorM), 10, 
				0, (360*factorM)+10);
		scrollM.setBlockIncrement((int)Math.round(blockM*factorM));
		scrollM.setUnitIncrement( (int)Math.round( unitM*factorM));
		gbcEltsPanel.gridx = 1;
		gbcEltsPanel.gridy = 6;
		gbcEltsPanel.weightx = 1.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 2; //fiddling 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(4, 5, 10, 5);	// TLBR
		gblEltsPanel.setConstraints(scrollM, gbcEltsPanel);
		eltsPanel.add(scrollM);
		
		// M Textfield (awp)
		textM = new TextField(degFormat.format(initialM));
		textM.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		textM.addKeyListener( new DoubleKeyListener(textM) );
		textM.addTextListener( new TextListener() {
		    public void textValueChanged(TextEvent e) {
			if (textM.isFocusOwner()) {		// Do nothing when Scroll changes Text
				double newMdegText;
				if (textM.getText().length() == 0) {
					newMdegText = 0.;
				} else if (Double.valueOf(textM.getText()) > 360.) {
					newMdegText = 360.;
					textM.setText(degFormat.format(newMdegText));
				} else {
					newMdegText = Double.valueOf(textM.getText());
				}
				orbitCanvas.setM(newMdegText*(Math.PI/180.), atime);
				scrollM.setValue((int)Math.round(newMdegText*factorM));
				orbitCanvas.repaint();
			}
		    }
		});
		gbcEltsPanel.gridx = 3; //fiddling 2;
		gbcEltsPanel.gridy = 6;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(3, 3, 8, 1);	// TLBR
		gblEltsPanel.setConstraints(textM, gbcEltsPanel);
		eltsPanel.add(textM);
		
		// M Units Label (awp)
		Label mUnitsLabel = new Label(""+'\u00B0');
		mUnitsLabel.setAlignment(Label.LEFT);
		mUnitsLabel.setFont(new Font("Dialog", Font.PLAIN, textFieldFontSize));
		gbcEltsPanel.gridx = 4; //fiddling 3;
		gbcEltsPanel.gridy = 6;
		gbcEltsPanel.weightx = 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(0, 0, 4, 10);	// TLBR
		gblEltsPanel.setConstraints(mUnitsLabel, gbcEltsPanel);
		eltsPanel.add(mUnitsLabel);
		
		// Lock Checkbox (awp) ----------------------------------------------------------------------------------
		checkLock = new Checkbox("Lock");
		String initLock = getParameter("Lock");
		if (initLock == null) {
			initLock = "False";
		}
		if (initLock.equals("True") || initLock.equals("TRUE") || initLock.equals("T") || initLock.equals("t") ||
			initLock.equals("Yes")  || initLock.equals("YES")  || initLock.equals("Y") || initLock.equals("y")) {
				checkLock.setState(true);
				enableEltsControls(false);
		} else {
				checkLock.setState(false);
		}
		checkLock.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcEltsPanel.fill = GridBagConstraints.NONE; //BOTH;
		gbcEltsPanel.anchor = GridBagConstraints.EAST;
		gbcEltsPanel.gridx = 2;
		gbcEltsPanel.gridy = 0;
		gbcEltsPanel.weightx = 1.0; //fiddling 0.0;
		gbcEltsPanel.weighty = 0.0;
		gbcEltsPanel.gridwidth = 1;
		gbcEltsPanel.gridheight = 1;
		gbcEltsPanel.insets = new Insets(6, 0, 0, 10);	// TLBR
		gblEltsPanel.setConstraints(checkLock, gbcEltsPanel);
		eltsPanel.add(checkLock);
		
		//*********************************************************************
		// Applet Layout
		//*********************************************************************
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gbl);
		gbc.fill = GridBagConstraints.BOTH;
		
		// Main Panel
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth  = GridBagConstraints.REMAINDER;
		gbc.gridheight = GridBagConstraints.RELATIVE;
		gbc.insets = new Insets(10, 10, 5, 10);	// TLBR
		gbl.setConstraints(mainPanel, gbc);
		add(mainPanel);
		
		// Elements Panel
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridwidth  = GridBagConstraints.RELATIVE;
		gbc.gridheight = GridBagConstraints.REMAINDER;
//		gbc.ipadx = 15;	// Doesn't work right!
//		gbc.ipady = 15; // Doesn't work right!
		gbc.insets = new Insets(5, 10, 10, 5);	// TLBR
		gbl.setConstraints(eltsPanel, gbc);
		add(eltsPanel);
		
		// Control Panel
		gbc.weightx = 0.2; //fiddling 0.0;
		gbc.weighty = 0.0;
		gbc.gridwidth  = GridBagConstraints.REMAINDER;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.ipadx = 15;
		gbc.ipady = 15;
		gbc.insets = new Insets(5, 5, 10, 10);	// TLBR
		gbl.setConstraints(ctrlPanel, gbc);
		add(ctrlPanel);
		
		// Player Thread
		orbitPlayer = new OrbitPlayer(this);
		playerThread = null;
	}

	
	/**
	 * Set lock-state of Elements controls  // (awp)
	 */
	 public void enableEltsControls(boolean state) {
		scrollA.setEnabled(state);
		textA.setEnabled(state);
		scrollE.setEnabled(state);
		textE.setEnabled(state);
		scrollIncl.setEnabled(state);
		textIncl.setEnabled(state);
		scrollNode.setEnabled(state);
		textNode.setEnabled(state);
		scrollPeri.setEnabled(state);
		textPeri.setEnabled(state);
		scrollM.setEnabled(state);
		textM.setEnabled(state);
		if (nClonesPreload == 0) {		// If clones were preloaded, then don't allow user to create more!
			buttonClone.setEnabled(state);
		}
/*		if (state == true) {
			checkLock.setLabel("Lock");
		} else {
			checkLock.setLabel("Locked");
		}
*/	 }
	
	/**
	 * Get Main Panel Width	// (awp)
	 */
/*	public int getMainPanelWidth() {
		return this.mainPanel.getWidth();
	}
*/
	
	/**
	 * Get Player Thread	// (awp)
	 */
	public Thread getPlayerThread() {
		return this.playerThread;
	}
	
	/**
	 * Get Time Step Choice	// (awp)
	 */
	public int getTimeStepChoice() {
		return this.choiceTimeStep.getSelectedIndex();
	}
	
	/**
	 * Set Time Step Choice	// (awp)
	 */
	public void setTimeStepChoice(int index) {
		this.choiceTimeStep.select(index);
		this.timeStep = timeStepSpan[index];
	}
	
	/**
	 * Update both TextM and ScrollM based on current mean anomaly
	 */
	public void updateBothM(ATime atime, Comet object) {	// (awp)
		// Skip updating textM & scrollM if they haven't been inited yet!
		if (textM != null && scrollM != null) {
			double trackingM = object.getM(atime)*(180./Math.PI);
			while (trackingM > 360.) trackingM = trackingM - 360.;
			while (trackingM < 0.)   trackingM = trackingM + 360.;
			textM.setText(degFormat.format(trackingM));
			scrollM.setValue((int)Math.round(trackingM*factorM));
		}
	}
	
	/**
	 * Override Function start()
	 */
	public void start() {
		// if you want, you can initialize date here
	}
	
	/**
	 * Override Function stop()
	 */
	public void stop() {
		if (dateDialog != null) {
			dateDialog.dispose();
			endDateDialog(null);
		}
		if (playerThread != null) {
			playerThread.stop();
			playerThread = null;
			buttonDate.enable();
		}
	}
	
	/**
	 * Destroy the applet
	 */
	public void destroy() {
		removeAll();
	}
	
	/**
	 * Event Handler
	 */
    public boolean handleEvent(Event evt) {
		switch (evt.id) {
		case Event.SCROLL_ABSOLUTE:
		case Event.SCROLL_LINE_DOWN:
		case Event.SCROLL_LINE_UP:
		case Event.SCROLL_PAGE_UP:
		case Event.SCROLL_PAGE_DOWN:
			if (evt.target == scrollHorz) {
				orbitCanvas.setRotateHorz(270 - scrollHorz.getValue());
				int displayRotHorz = scrollHorz.getValue();
			//	System.out.println("Horz Rotation = "+displayRotHorz);
			} else if (evt.target == scrollVert) {
				orbitCanvas.setRotateVert(180 - scrollVert.getValue());
				int displayRotVert = scrollVert.getValue() - 90;
			//	System.out.println("Vert Rotation = "+displayRotVert);
			} else if (evt.target == scrollZoom) {
				orbitCanvas.setZoom(scrollZoom.getValue());
				textZoom.setText(""+scrollZoom.getValue());
			} else if (evt.target == scrollA) {		// (awp) ***********************
				double newA = (double)scrollA.getValue()/factorA;
				orbitCanvas.setA(newA, atime);
				textA.setText(aeFormat.format(newA));
			} else if (evt.target == scrollE) {		// (awp) ***********************
				double newE = (double)scrollE.getValue()/factorE;
				orbitCanvas.setE(newE);
				textE.setText(aeFormat.format(newE));
			} else if (evt.target == scrollIncl) {	// (awp) ***********************
//				System.out.println("new incl should be "+scrollIncl.getValue());
				double newInclDeg = (double)scrollIncl.getValue()/factorIncl;
				orbitCanvas.setIncl(newInclDeg*(Math.PI/180.));
				textIncl.setText(degFormat.format(newInclDeg));
			} else if (evt.target == scrollNode) {	// (awp) ***********************
				double newNodeDeg = (double)scrollNode.getValue()/factorNode;
				orbitCanvas.setNode(newNodeDeg*(Math.PI/180.));
				textNode.setText(degFormat.format(newNodeDeg));
			} else if (evt.target == scrollPeri) {	// (awp) ***********************
				double newPeriDeg = (double)scrollPeri.getValue()/factorPeri;
				orbitCanvas.setPeri(newPeriDeg*(Math.PI/180.));
				textPeri.setText(degFormat.format(newPeriDeg));
			} else if (evt.target == scrollM) {		// (awp) ***********************
				double newMdeg = (double)scrollM.getValue()/factorM;
				orbitCanvas.setM(newMdeg*(Math.PI/180.), atime);
				textM.setText(degFormat.format(newMdeg));
			} else {
				return false;
			}
			orbitCanvas.repaint();
			return true;
		case Event.ACTION_EVENT:
			if (evt.target == buttonDate) {				// Set Date
				dateDialog = new DateDialog(this, atime, buttonDate);
				buttonDate.disable();
				return true;
			} else if (evt.target == buttonForPlay) {		// ForPlay
				if (playerThread != null
					&&  playDirection != ATime.F_INCTIME) {
					playerThread.stop();
					playerThread = null;
				}
				if (playerThread == null) {
					buttonDate.disable();
					playDirection = ATime.F_INCTIME;
					playerThread = new Thread(orbitPlayer);
					playerThread.setPriority(Thread.MIN_PRIORITY);
					playerThread.start();
				}
			} else if (evt.target == buttonRevPlay) {		// RevPlay
				if (playerThread != null
					&&  playDirection != ATime.F_DECTIME) {
					playerThread.stop();
					playerThread = null;
				}
				if (playerThread == null) {
					buttonDate.disable();
					playDirection = ATime.F_DECTIME;
					playerThread = new Thread(orbitPlayer);
					playerThread.setPriority(Thread.MIN_PRIORITY);
					playerThread.start();
				}
			} else if (evt.target == buttonStop) {			// Stop
				if (playerThread != null) {
					playerThread.stop();
					playerThread = null;
					buttonDate.enable();
				}
			} else if (evt.target == buttonForStep) {		// +1 Step
				atime.changeDate(timeStep, ATime.F_INCTIME);
				setNewDate();
				return true;
			} else if (evt.target == buttonRevStep) {		// -1 Step
				atime.changeDate(timeStep, ATime.F_DECTIME);
				setNewDate();
				return true;
			} else if (evt.target == checkPlanetName) {		// Planet Name
				orbitCanvas.switchPlanetName(checkPlanetName.getState());
				orbitCanvas.repaint();
				return true;
			} else if (evt.target == checkObjectName) {		// Object Name
				orbitCanvas.switchObjectName(checkObjectName.getState());
				orbitCanvas.repaint();
				return true;
            } else if (evt.target == checkCloneName) {		// Clone Name
                orbitCanvas.switchCloneName(checkCloneName.getState());
                orbitCanvas.repaint();
                return true;
			} else if (evt.target == checkDistanceLabel) {	// Distance
				orbitCanvas.switchDistanceLabel(checkDistanceLabel.getState());
                orbitCanvas.switchDateLabel(checkDistanceLabel.getState());
				orbitCanvas.repaint();
				return true;
/*			} else if (evt.target == checkDateLabel) {		// Date
				orbitCanvas.switchDateLabel(checkDateLabel.getState());
				orbitCanvas.repaint();
				return true;        */
			} else if (evt.target == buttonClone) {			// (awp) ***********************
				if (nClones < nClonesMax) {
					clone[nClones] = new Comet("Clone #"+Integer.toString(nClones+1), object.getT(),
								   object.getE(),    object.getQ(),
								   object.getPeri(), object.getNode(),
								   object.getIncl(), object.getEquinox());
                    choicePrimaryObject.addItem(clone[nClones].getName());
					nClones++;
					orbitCanvas.repaint();
					return true;
				} else {
					return false;
				}
			} else if (evt.target == checkFineControl) {	// (awp) ***********************
				if (checkFineControl.getState()) { // true
					scrollA.setBlockIncrement(   (int)Math.round(blockFineA   *factorA));
					scrollA.setUnitIncrement(    (int)Math.round( unitFineA   *factorA));
					scrollE.setBlockIncrement(   (int)Math.round(blockFineE   *factorE));
					scrollE.setUnitIncrement(    (int)Math.round( unitFineE   *factorE));
					scrollIncl.setBlockIncrement((int)Math.round(blockFineIncl*factorIncl));
					scrollIncl.setUnitIncrement( (int)Math.round( unitFineIncl*factorIncl));
					scrollNode.setBlockIncrement((int)Math.round(blockFineNode*factorNode));
					scrollNode.setUnitIncrement( (int)Math.round( unitFineNode*factorNode));
					scrollPeri.setBlockIncrement((int)Math.round(blockFinePeri*factorPeri));
					scrollPeri.setUnitIncrement( (int)Math.round( unitFinePeri*factorPeri));
					scrollM.setBlockIncrement(   (int)Math.round(blockFineM   *factorM));
					scrollM.setUnitIncrement(    (int)Math.round( unitFineM   *factorM));
					scrollZoom.setValues(scrollZoom.getValue(), 15*intExtraZoom, 1, 25015*intExtraZoom);
				} else { // false
					scrollA.setBlockIncrement(   (int)Math.round(blockA   *factorA));
					scrollA.setUnitIncrement(    (int)Math.round( unitA   *factorA));
					scrollE.setBlockIncrement(   (int)Math.round(blockE   *factorE));
					scrollE.setUnitIncrement(    (int)Math.round( unitE   *factorE));
					scrollIncl.setBlockIncrement((int)Math.round(blockIncl*factorIncl));
					scrollIncl.setUnitIncrement( (int)Math.round( unitIncl*factorIncl));
					scrollNode.setBlockIncrement((int)Math.round(blockNode*factorNode));
					scrollNode.setUnitIncrement( (int)Math.round( unitIncl*factorNode));
					scrollPeri.setBlockIncrement((int)Math.round(blockPeri*factorPeri));
					scrollPeri.setUnitIncrement( (int)Math.round( unitIncl*factorPeri));
					scrollM.setBlockIncrement(   (int)Math.round(blockM   *factorM));
					scrollM.setUnitIncrement(    (int)Math.round( unitM   *factorM));
					scrollZoom.setValues(scrollZoom.getValue(), 15*intExtraZoom, 1, 450*intExtraZoom);
				}
				return true;
			} else if (evt.target == checkLock) {	// (awp) ***********************
				if (checkLock.getState()) {	// Lock it down!
					enableEltsControls(false);
				} else {					// Allow access again...
					enableEltsControls(true);
				}
			} else if (evt.target == choiceTimeStep) {		// Time Step
				for (int i = 0; i < timeStepCount; i++) {
					if ((String)evt.arg == timeStepLabel[i]) {
						timeStep = timeStepSpan[i];
						break;
					}
				}
			} else if (evt.target == choiceCenterObject) {    // Center Object 
				for (int i = 0; i < CenterObjectCount; i++) {
					if ((String)evt.arg == CenterObjectLabel[i]) {
						CenterObjectSelected = i;
						orbitCanvas.SelectCenterObject(i);
						orbitCanvas.repaint();
						break;
					}
				}
            } else if (evt.target == choicePrimaryObject) {    // Primary Object
                for (int i = 0; i < nClones+1; i++) {
//                    	System.out.println("List position i="+i+" is clone array position "+cloneHash[i]);

                        if (cloneHash[i] == -1) {
                            // This should already be the selected entry.
                            // But don't want to waste time searching for it...
                            if ((String)evt.arg == object.getName()) {
                                break;
                            }
                            
                        } else if ((String)evt.arg == clone[cloneHash[i]].getName()) {
                            PrimaryObjectPrevious = PrimaryObjectSelected;
                            PrimaryObjectSelected = i;
//                            orbitCanvas.SelectPrimaryObject(i);

                            // About cloneHash[]:
                            //-------------------
                            // spits out clone[] indices:  -1=obj, 0=zerothClone, etc
                            // cloneHash[] indices are positions on choicePrimary list
                            // initially cloneHash[0]=-1, cloneHash[1]=0, cloneHash[2]=1, etc...
                            
                            tempobj            = object;
                            object             = clone[cloneHash[i]];
                            orbitCanvas.object = clone[cloneHash[i]];
                            clone[cloneHash[i]]           = tempobj;
                            
//                            choicePrimaryObject.remove(0);
  //                          choicePrimaryObject.insert(object.getName(), 0);
    //                        choicePrimaryObject.remove(i+1);
      //                      choicePrimaryObject.insert(clone[i].getName(), i+1);
        //                    choicePrimaryObject.select(0);

                            orbitCanvas.objectOrbit = new CometOrbit(object, 120*3);
                            orbitCanvas.objectPos = object.GetPos(atime.getJd());
                            orbitCanvas.cloneOrbit[cloneHash[i]] = new CometOrbit(clone[cloneHash[i]], 120*3);
                            orbitCanvas.clonePos[cloneHash[i]] = clone[cloneHash[i]].GetPos(atime.getJd());
                            
                            cloneHash[PrimaryObjectSelected] = -1;
                            cloneHash[PrimaryObjectPrevious] = i-1;
                            
                            textA.setText(aeFormat.format(object.getA()));
                            textE.setText(aeFormat.format(object.getE()));
                            textIncl.setText(degFormat.format(object.getIncl()*180./Math.PI));
                            textNode.setText(degFormat.format(object.getNode()*180./Math.PI));
                            textPeri.setText(degFormat.format(object.getPeri()*180./Math.PI));
                            textM.setText(degFormat.format(object.getM(atime)*180./Math.PI));
                            
                            scrollA.setValue((int)Math.round(object.getA()*factorA));
                            scrollE.setValue((int)Math.round(object.getE()*factorE));
                            scrollIncl.setValue((int)Math.round(object.getIncl()*180./Math.PI*factorIncl));
                            scrollNode.setValue((int)Math.round(object.getNode()*180./Math.PI*factorNode));
                            scrollPeri.setValue((int)Math.round(object.getPeri()*180./Math.PI*factorPeri));
                            scrollM.setValue((int)Math.round(object.getM(atime)*180./Math.PI*factorM));
                            
                            orbitCanvas.repaint();
                            break;
                        }
                }
			} else if (evt.target == choiceOrbitObject) {    // Orbit Display
				for (int i = 0; i < OrbitDisplayCount; i++) {
					if ((String)evt.arg == OrbitDisplayLabel[i]) {
						if (i == 1) {
							for (int j = 0; j < OrbitCount; j++) {
								OrbitDisplay[j] = true;
							} 
							OrbitDisplay[10] = false; // excludes Pluto
						}
						else if (i == 2) {
							for (int j = 0; j < OrbitCount; j++) {
								OrbitDisplay[j] = false;
							}
						}
						else if (i == 0) {
							for (int j = 0; j < OrbitCount; j++) {
								OrbitDisplay[j] = OrbitDisplayDefault[j];
							}
						}
						else if (i > 3) {
							if (OrbitDisplay[i-3]) {
								OrbitDisplay[i-3] = false;
							}
							else {
								OrbitDisplay[i-3] = true;
							}
						}
						evt.arg = OrbitDisplayLabel[0];
						orbitCanvas.SelectOrbits(OrbitDisplay, OrbitCount);
						orbitCanvas.repaint();
						break;
					}
				}
			}
			return false;
		default:
			return false;
		}
    }
	
	/**
	 * message sent by DateDialog (when disposed)
	 */
	public void endDateDialog(ATime atime) {
		dateDialog = null;
		buttonDate.enable();
		if (atime != null) {	// OK button pressed in dateDialog
			this.atime = limitATime(atime);
			orbitCanvas.setDate(atime);
			orbitCanvas.repaint();
			if (orbitCanvas.getPrevTimeStepChoice() >= 0) {
				setTimeStepChoice(orbitCanvas.getPrevTimeStepChoice());
				orbitCanvas.setPrevTimeStepChoice(-1);
			}
		}
	}
}

/**
 * Player Class
 */
class OrbitPlayer implements Runnable {
	OrbitViewer	orbitViewer;
	
	/**
	 * Constructor
	 */
	public OrbitPlayer(OrbitViewer orbitViewer) {
		this.orbitViewer = orbitViewer;
	}
	
	/**
	 * Play forever
	 */
	public void run() {
		while (true) {
			try {
				long   msecDelay = 50;
				double msecPerClone = 0.3;
				msecDelay = msecDelay + Math.round(orbitViewer.nClones * msecPerClone);
			//	System.out.println("Waiting "+msecDelay+" msec");
				Thread.sleep(msecDelay);
			} catch (InterruptedException e) {
				break;
			}
			ATime atime = orbitViewer.getAtime();
			atime.changeDate(orbitViewer.timeStep, orbitViewer.playDirection);
			orbitViewer.setNewDate(atime);
		}
	}
}

/**
 * Orbit Canvas
 */
class OrbitCanvas extends Canvas {
	OrbitViewer	orbitViewer;	// (awp)

	/**
	 * Orbital Element (Initialized in Constructor)
	 */
    public Comet object;
//	private Comet object;

	
	/**
	 * Orbital Curve Class (Initialized in Constructor)
	 */
    public CometOrbit  objectOrbit;
    public CometOrbit[]  cloneOrbit;
//	private CometOrbit  objectOrbit;
//  private CometOrbit[]  cloneOrbit;   //	private CometOrbit  cloneOrbit = null;
	private PlanetOrbit planetOrbit[];
    
    private double epochPlanetOrbit;
	
	/**
	 * Date
	 */
	private ATime atime;
	
	/**
	 * Position of the Object and Planets
	 */
    public Xyz objectPos;
    public Xyz[] clonePos;
//	private Xyz objectPos;
//	private Xyz[] clonePos;
    private Xyz[] xyzObs;
	private String[] nameObs;
	private Xyz planetPos[];
    private int CenterObjectSelected;
//    private int PrimaryObjectSelected;
    private boolean OrbitDisplay[];
	
	private double[] sdistanceClone;
	
	/**
	 * Planet Radii and Geocentric Ranges		// (awp)
	 */
	private double oneAU       = 149597900.;	// (awp) - in KILOMETERS
	private double radiusSun   = 695500./oneAU;	// (awp) \
	private double radiusMer   =   2440./oneAU;	// (awp)  |
	private double radiusVen   =   6052./oneAU;	// (awp)  |
	private double radiusEar   =   6378./oneAU;	// (awp)  |
	private double radiusMar   =   3396./oneAU;	// (awp)  |
	private double radiusJup   =  71492./oneAU;	// (awp)  |
	private double radiusSat   =  60268./oneAU;	// (awp)  |- in AU
	private double radiusUra   =  25559./oneAU;	// (awp)  |
	private double radiusNep   =  24764./oneAU;	// (awp)  |
	private double radiusPlu   =   1153./oneAU;	// (awp)  |
	private double lunarDist   = 384399./oneAU;	// (awp)  |
	private double geosyncDist =  42164./oneAU;	// (awp)  |
	private double earthRadius =     radiusEar;	// (awp) /
	
	/**
	 * Projection Parameters
	 */
	private double fRotateH = 0.0;
	private double fRotateV = 0.0;
	private double fZoom = 5.0;

	// This is the fMaxOrbit that controls the object position points.
	// Similar parameter in Astro/CometOrbit.java controls orbit lines.
	static private final double extraZoom = 20.0;	// see intExtraZoom in Main Applet Class
	static private final double fMaxOrbit = 120.0*extraZoom; // 100.0; // 90.0;

	/**
	 * Rotation Matrix
	 */
	private Matrix mtxToEcl;
	private double epochToEcl;
	private Matrix mtxRotate;
	private int nX0, nY0;	// Origin
	
	/**
	 * Size of Canvas
	 */
	private Dimension sizeCanvas;
	
	/**
	 * Previous time-step choice?
	 */
	private int prevTimeStepChoice = -1; // null
	
	/**
	 * Colors
	 */
	private Color colorObjectOrbitUpper = new Color(0x00f5ff);
	private Color colorObjectOrbitLower = new Color(0x0000ff);
	private Color colorObject           = new Color(0x00ffff);
	private Color colorObjectName       = new Color(0x00cccc);
		private Color colorObsName         = Color.red;
		private Color colorCloneOrbitUpper = new Color(0x00f5ff).darker().darker();
		private Color colorCloneOrbitLower = new Color(0x0000ff).darker().darker();
		private Color colorClone           = new Color(0x00ffff).darker();
        private Color colorCloneName       = new Color(0x00cccc).darker();   // new Aug 2015
	private Color colorPlanetOrbitUpper = new Color(0xffffff);
	private Color colorPlanetOrbitLower = new Color(0x808080);
	private Color colorPlanet			= new Color(0x00ff00);
	private Color colorPlanetName		= new Color(0x00aa00);
	private Color colorSun              = new Color(0xff8c00); // new Color(0xd04040);
	private Color colorAxisPlus         = new Color(0xffff00);
	private Color colorAxisMinus        = new Color(0x555500);
	private Color colorInformation      = new Color(0xffffff);
	private Color colorLunar			= Color.orange;			// (awp)
	private Color colorGeosync			= Color.red;			// (awp)
	
	/**
	 * Fonts
	 */
	private Font fontObjectName  = new Font("Helvetica", Font.BOLD, 14);
	private Font fontPlanetName  = new Font("Helvetica", Font.PLAIN, 14);
	private Font fontInformation = new Font("Helvetica", Font.BOLD, 14);
	
	/**
	 * off-screen Image
	 */
	Image offscreen;
	
	/**
	 * Object Name Drawing Flag
	 */
	boolean bPlanetName;
	boolean bObjectName;
    boolean bCloneName;
	boolean bDistanceLabel;
	boolean bDateLabel;
	
	/**
	 * Constructor
	 */
	public OrbitCanvas(Comet object, ATime atime, OrbitViewer orbitViewer, Xyz[] xyzObs, String[] nameObs) {	// (awp)
		this.orbitViewer = orbitViewer;		// (awp)
			cloneOrbit     = new CometOrbit[orbitViewer.nClonesMax];
			clonePos       = new Xyz[orbitViewer.nClonesMax];
			sdistanceClone = new double[orbitViewer.nClonesMax];
		planetPos = new Xyz[9];
                OrbitDisplay = new boolean[11];
		this.object = object;
			this.xyzObs = xyzObs;
			this.nameObs = nameObs;
		this.objectOrbit = new CometOrbit(object, 120*3);
		this.planetOrbit = new PlanetOrbit[9];
		updatePlanetOrbit(atime);
		updateRotationMatrix(atime);
		// Set Initial Date
		this.atime = atime;
		setDate(this.atime);
		// no offscreen image
		offscreen = null;
		// no name labels
		bPlanetName = false;
		bObjectName = false;
        bCloneName  = false;
		bDistanceLabel = true;
		bDateLabel = true;
		repaint();
	}
	
	/**
	 * Get/Set PrevTimeStepChoice (awp)
	 */
	public void setPrevTimeStepChoice(int index) {
		this.prevTimeStepChoice = index;
	}
	public int getPrevTimeStepChoice(){
		return this.prevTimeStepChoice;
	}
	
	/**
	 * Set Object Variables (awp)
	 */
	public void setA(double a2Set, ATime atime) {
		object.setA(a2Set, atime);
		// Tell it now to update the orbit itself
		objectOrbit = new CometOrbit(object, 120*3);
		objectPos = object.GetPos(atime.getJd());
	}
	public void setE(double e2Set) {
		object.setE(e2Set);
		// Tell it now to update the orbit itself
		objectOrbit = new CometOrbit(object, 120*3);
		objectPos = object.GetPos(atime.getJd());
	}
	public void setIncl(double incl2Set) {
		object.setIncl(incl2Set);
		// Tell it now to update the orbit itself:
		objectOrbit = new CometOrbit(object, 120*3);
		objectPos = object.GetPos(atime.getJd());
	}
	public void setNode(double node2Set) {
		object.setNode(node2Set);
		// Tell it now to update the orbit itself:
		objectOrbit = new CometOrbit(object, 120*3);
		objectPos = object.GetPos(atime.getJd());
	}
	public void setPeri(double peri2Set) {
		object.setPeri(peri2Set);
		// Tell it now to update the orbit itself:
		objectOrbit = new CometOrbit(object, 120*3);
		objectPos = object.GetPos(atime.getJd());
	}
	public void setM(double m2Set, ATime atime) {
		object.setM(m2Set, atime);
		// Tell it now to update the orbit itself:
		objectOrbit = new CometOrbit(object, 120*3);
		objectPos = object.GetPos(atime.getJd());
	}


	/**
	 * Make Planet Orbit
	 */
	private void updatePlanetOrbit(ATime atime) {
		for (int i = Planet.MERCURY; i <= Planet.PLUTO; i++) {
			this.planetOrbit[i - Planet.MERCURY]
				= new PlanetOrbit(i, atime, 120*3); // 48);
		}
		this.epochPlanetOrbit = atime.getJd();
	}
	
	/**
	 * Rotation Matrix Equatorial(2000)->Ecliptic(DATE)
	 */
	private void updateRotationMatrix(ATime atime) {
//		System.out.println("");
//		System.out.println("UPDATING ROTATION MATRIX ON "+atime);
		Matrix mtxPrec = Matrix.PrecMatrix(Astro.JD2000, atime.getJd());
		Matrix mtxEqt2Ecl = Matrix.RotateX(ATime.getEp(atime.getJd()));
		this.mtxToEcl = mtxEqt2Ecl.Mul(mtxPrec);
		this.epochToEcl = atime.getJd();
	}
	
	/**
	 * Horizontal Rotation Parameter Set
	 */
	public void setRotateHorz(int nRotateH) {
		this.fRotateH = (double)nRotateH;
	}
	
	/**
	 * Vertical Rotation Parameter Set
	 */
	public void setRotateVert(int nRotateV) {
		this.fRotateV = (double)nRotateV;
	}
	
	/**
	 * Zoom Parameter Set
	 */
	public void setZoom(int nZoom) {
		this.fZoom = (double)nZoom;
	}
	
	/**
	 * Date Parameter Set
	 */
	public void setDate(ATime atime) {
		this.atime = atime;
		orbitViewer.updateBothM(atime, object);			// (awp) ***************************
		objectPos = object.GetPos(atime.getJd());
		if (orbitViewer.nClones > 0) {
			for (int i = 0; i < orbitViewer.nClones; i++) {
				clonePos[i] = orbitViewer.clone[i].GetPos(atime.getJd());
			}
		}
		for (int i = 0; i < 9; i++) {
			planetPos[i] = Planet.getPos(Planet.MERCURY+i, atime);
		}
	}
	
	/**
	 * Switch Planet Name ON/OFF
	 */
	public void switchPlanetName(boolean bPlanetName) {
		this.bPlanetName = bPlanetName;
	}


        /**
         * Select Orbits
         */
        public void SelectOrbits(boolean OrbitDisplay[], int OrbitCount) {
           for (int i=0; i< OrbitCount; i++)
           {
                this.OrbitDisplay[i] = OrbitDisplay[i];
           }
        }

        /**
         * Select Center Object
         */
        public void SelectCenterObject(int CenterObjectSelected) {
                this.CenterObjectSelected = CenterObjectSelected;
        }

        /**
         * Select Primary Object
         */
//        public void SelectPrimaryObject(int PrimaryObjectSelected) {
  //          this.PrimaryObjectSelected = PrimaryObjectSelected;
    //    }

	/**
	 * Switch Object Name ON/OFF
	 */
	public void switchObjectName(boolean bObjectName) {
		this.bObjectName = bObjectName;
	}
	
    /**
     * Switch Clone Name ON/OFF
     */
    public void switchCloneName(boolean bCloneName) {
        this.bCloneName = bCloneName;
    }
    
	/**
	 * Switch Distance Label ON/OFF
	 */
	public void switchDistanceLabel(boolean bDistanceLabel) {
		this.bDistanceLabel = bDistanceLabel;
	}
	
	/**
	 * Switch Date Label ON/OFF
	 */
	public void switchDateLabel(boolean bDateLabel) {
		this.bDateLabel = bDateLabel;
	}
	
	/**
	 * Get (X,Y) on Canvas from Xyz
	 */
	private Point getDrawPoint(Xyz xyz) {
		// 600 means 5...fZoom...100 -> 120AU...Width...6AU
		double fMul = this.fZoom * (double)sizeCanvas.width / (600.0*extraZoom)
							* (1.0 + xyz.fZ / (250.0*extraZoom));	// Parse
		int nX = this.nX0 + (int)Math.round(xyz.fX * fMul);
		int nY = this.nY0 - (int)Math.round(xyz.fY * fMul);
		return new Point(nX, nY);
	}
	
	/**
	 * Draw Planets' Orbit
	 */
	private void drawPlanetOrbit(Graphics g, PlanetOrbit planetOrbit,
						 Color colorUpper, Color colorLower) {
		Point point1, point2;
		Xyz xyz = planetOrbit.getAt(0).Rotate(this.mtxToEcl)
							  .Rotate(this.mtxRotate);
		point1 = getDrawPoint(xyz);
		for (int i = 1; i <= planetOrbit.getDivision(); i++) {
			xyz = planetOrbit.getAt(i).Rotate(this.mtxToEcl);
			if (xyz.fZ >= 0.0) {
				g.setColor(colorUpper);
			} else {
				g.setColor(colorLower);
			}
			xyz = xyz.Rotate(this.mtxRotate);
			point2 = getDrawPoint(xyz);
			g.drawLine(point1.x, point1.y, point2.x, point2.y);
			point1 = point2;
		}
	}

        /**
         * Draw Earth's Orbit
         */
        private void drawEarthOrbit(Graphics g, PlanetOrbit planetOrbit,
                                                 Color colorUpper, Color colorLower) {
                Point point1, point2;
                Xyz xyz = planetOrbit.getAt(0).Rotate(this.mtxToEcl)
                                                          .Rotate(this.mtxRotate);
                point1 = getDrawPoint(xyz);
                for (int i = 1; i <= planetOrbit.getDivision(); i++) {
                        xyz = planetOrbit.getAt(i).Rotate(this.mtxToEcl);
                        g.setColor(colorUpper);
                        xyz = xyz.Rotate(this.mtxRotate);
                        point2 = getDrawPoint(xyz);
                        g.drawLine(point1.x, point1.y, point2.x, point2.y);
                        point1 = point2;
                }
        }
	
	/**
	 * Draw Planets' Body
	 */
	private void drawPlanetBody(Graphics og, Xyz planetPos, String strName, int radPix) {
		Xyz xyz = planetPos.Rotate(this.mtxRotate);
		Point point = getDrawPoint(xyz);
		og.setColor(colorPlanet);
//		og.fillArc(point.x - 2, point.y - 2, 5, 5, 0, 360); // xUL, yUL, width, height, startAngle, endAngle
		og.fillArc(point.x - radPix, point.y - radPix, (2*radPix)+1, (2*radPix+1), 0, 360);
		if (bPlanetName) {
			og.setColor(colorPlanetName);
			og.drawString(strName, point.x + 5, point.y);
		}
	}
	
	/**
	 * Draw Ecliptic Axis
	 */
	private void drawEclipticAxis(Graphics og) {
		Xyz xyz;
		Point point;
		
		og.setColor(colorAxisMinus);
		// -X
		xyz = (new Xyz(-50.0, 0.0,  0.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
		
		// -Z
		xyz = (new Xyz(0.0, 00.0, -50.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
		
		og.setColor(colorAxisPlus);
		// +X
		xyz = (new Xyz( 50.0, 0.0,  0.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
		// +Z
		xyz = (new Xyz(0.0, 00.0,  50.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
	}
	
	/**
	 * update (paint without clearing background)
	 */
	public void update(Graphics g) {
                 Point point3;
                 Xyz xyz, xyz1;
                 Xyz xyzClone[] = new Xyz[orbitViewer.nClonesMax];
		
		// Calculate Drawing Parameter
		Matrix mtxRotH = Matrix.RotateZ(this.fRotateH * Math.PI / 180.0);
		Matrix mtxRotV = Matrix.RotateX(this.fRotateV * Math.PI / 180.0);
		this.mtxRotate = mtxRotV.Mul(mtxRotH);

		this.nX0 = this.sizeCanvas.width  / 2;
		this.nY0 = this.sizeCanvas.height / 2;

                if (Math.abs(epochToEcl - atime.getJd()) > 100.) { // 365.2422 * 5) {
                        updateRotationMatrix(atime);
                }

                // If center object is comet/asteroid  
                if (CenterObjectSelected == 1 )   {
                   xyz = this.objectOrbit.getAt(0).Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
                   xyz = this.objectPos.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);	
			
			if (orbitViewer.CometTooFar == 1) {
				if (orbitViewer.lastKnownCenter == orbitViewer.bogusCenter) {
					orbitViewer.lastKnownCenter = xyz;
				} else {
					xyz = orbitViewer.lastKnownCenter;
				}
			} else {
				if (orbitViewer.lastKnownCenter != orbitViewer.bogusCenter) {
					orbitViewer.lastKnownCenter = orbitViewer.bogusCenter;
				}
			}
			
                   point3 = getDrawPoint(xyz);

                   this.nX0 = this.sizeCanvas.width - point3.x;
                   this.nY0 = this.sizeCanvas.height - point3.y;

                   if (Math.abs(epochToEcl - atime.getJd()) > 100.) { // 365.2422 * 5) {
                        updateRotationMatrix(atime);
                   } 
                }
                // If center object is one of the planets
                else if (CenterObjectSelected > 1 )   {
                   xyz = planetPos[CenterObjectSelected -2].Rotate(this.mtxRotate);

                   point3 = getDrawPoint(xyz);

                   this.nX0 = this.sizeCanvas.width - point3.x;
                   this.nY0 = this.sizeCanvas.height - point3.y;

                   if (Math.abs(epochToEcl - atime.getJd()) > 100.) { // 365.2422 * 5) {
                        updateRotationMatrix(atime);
                   }
                }

		// Get Off-Screen Image Graphics Context
		Graphics og = offscreen.getGraphics();

		// Draw Frame
		og.setColor(Color.black);
		og.fillRect(0, 0, sizeCanvas.width - 1, sizeCanvas.height - 1);
		
		// Draw Ecliptic Axis
		drawEclipticAxis(og);
		
		// This zoom knows the correct scale (pixels/AU) for the current applet width:
		double gZoom  = fZoom*sizeCanvas.width/600.;	// (awp)
		int radSunPix = (int)Math.max(Math.round(gZoom*radiusSun/extraZoom), 2);
		int radMerPix = (int)Math.max(Math.round(gZoom*radiusMer/extraZoom), 2);
		int radVenPix = (int)Math.max(Math.round(gZoom*radiusVen/extraZoom), 2);
		int radEarPix = (int)Math.max(Math.round(gZoom*radiusEar/extraZoom), 2);
		int radMarPix = (int)Math.max(Math.round(gZoom*radiusMar/extraZoom), 2);
		int radJupPix = (int)Math.max(Math.round(gZoom*radiusJup/extraZoom), 2);
		int radSatPix = (int)Math.max(Math.round(gZoom*radiusSat/extraZoom), 2);
		int radUraPix = (int)Math.max(Math.round(gZoom*radiusUra/extraZoom), 2);
		int radNepPix = (int)Math.max(Math.round(gZoom*radiusNep/extraZoom), 2);
		int radPluPix = (int)Math.max(Math.round(gZoom*radiusPlu/extraZoom), 2);
		
		// Draw Sun
		og.setColor(colorSun);
//		og.fillArc(this.nX0 - 2, this.nY0 - 2, 5, 5, 0, 360);
		og.fillArc(this.nX0 - radSunPix, this.nY0 - radSunPix, (2*radSunPix)+1, (2*radSunPix+1), 0, 360);
		
		// Draw Orbit of Clones
		Point point1, point2;
		if (orbitViewer.nClones > 0) {
			if (cloneOrbit[orbitViewer.nClones-1] == null) {	// Check for last clone; if D.N.E., check/reload all clones.
				for (int j = 0; j < orbitViewer.nClones; j++) {
					if (cloneOrbit[j] == null) {
						cloneOrbit[j] = new CometOrbit(orbitViewer.clone[j], 120*3);
					}
				}
			}
			for (int j = 0; j < orbitViewer.nClones; j++) {
				xyzClone[j] = cloneOrbit[j].getAt(0).Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
				point1 = getDrawPoint(xyzClone[j]);
				if (OrbitDisplay[0] || OrbitDisplay[1]) {
					for (int i = 1; i <= cloneOrbit[j].getDivision(); i++) {
						xyzClone[j] = cloneOrbit[j].getAt(i).Rotate(this.mtxToEcl);
						if (xyzClone[j].fZ >= 0.0) {
							og.setColor(colorCloneOrbitUpper);
						} else {
							og.setColor(colorCloneOrbitLower);
						}
						xyzClone[j] = xyzClone[j].Rotate(this.mtxRotate);
						point2 = getDrawPoint(xyzClone[j]);
						og.drawLine(point1.x, point1.y, point2.x, point2.y);
						point1 = point2;
					}
				}
			}
		}
		
		// Draw Orbit of Object
		xyz = this.objectOrbit.getAt(0).Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
		point1 = getDrawPoint(xyz);
		  if (OrbitDisplay[0] || OrbitDisplay[1]) {
		   for (int i = 1; i <= this.objectOrbit.getDivision(); i++) {
			xyz = this.objectOrbit.getAt(i).Rotate(this.mtxToEcl);
			if (xyz.fZ >= 0.0) {
				og.setColor(colorObjectOrbitUpper);
			} else {
				og.setColor(colorObjectOrbitLower);
			}
			xyz = xyz.Rotate(this.mtxRotate);
			point2 = getDrawPoint(xyz);
			og.drawLine(point1.x, point1.y, point2.x, point2.y);
			point1 = point2;
		   }
		  }
		
		
		// Draw Other Focus
		og.setColor(Color.gray);
		xyz = new Xyz(-2.*object.getE()*object.getA(), 0., 0.);
		xyz = object.GetPos2(xyz);
		xyz = xyz.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
		Point pointF = getDrawPoint(xyz);
		int leg = 2;
		og.drawLine(pointF.x - leg, pointF.y - leg, pointF.x + leg, pointF.y + leg);
		og.drawLine(pointF.x - leg, pointF.y + leg, pointF.x + leg, pointF.y - leg);
		
		
		// Draw Clone(s) Body
		if (orbitViewer.nClones > 0) {
			if (clonePos[orbitViewer.nClones-1] == null) {
				clonePos[orbitViewer.nClones-1] = orbitViewer.clone[orbitViewer.nClones-1].GetPos(atime.getJd());
			}
			for (int j = 0; j < orbitViewer.nClones; j++) {
				xyzClone[j] = clonePos[j].Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
				sdistanceClone[j] = Math.sqrt(	(xyzClone[j].fX * xyzClone[j].fX) + 
								(xyzClone[j].fY * xyzClone[j].fY) + 
								(xyzClone[j].fZ * xyzClone[j].fZ));	// + .0005;
				if (sdistanceClone[j] < fMaxOrbit) {
					point1 = getDrawPoint(xyzClone[j]);
					og.setColor(colorClone);
					og.fillArc(point1.x - 2, point1.y - 2, 5, 5, 0, 360);
                    og.setFont(fontObjectName);         // new Aug 2015
                    if (bCloneName) {                   // new Aug 2015
                        og.setColor(colorCloneName);    // new Aug 2015
                        og.drawString(orbitViewer.clone[j].getName(), point1.x + 5, point1.y);  // new Aug 2015
                    }

				}
			}
		}
		
		// Draw observation points
		Xyz xyzObsi;
		Point pointObsi;
		if (xyzObs != null) {
			for (int i=0; i<xyzObs.length; i++) {
				xyzObsi = xyzObs[i];
				xyzObsi = xyzObsi.Rotate(this.mtxRotate);
				pointObsi = getDrawPoint(xyzObsi);
				og.setColor(Color.red);
				og.fillArc(pointObsi.x - 3, pointObsi.y - 3, 7, 7, 0, 360);
				og.setColor(Color.white);
				og.drawArc(pointObsi.x - 3, pointObsi.y - 3, 7, 7, 0, 360);
				
				if (bObjectName) {
					og.setColor(colorObsName);
					og.drawString(nameObs[i], pointObsi.x + 10, pointObsi.y);
				}
				
			}
		}
		
		
		// Draw Object Body
		xyz = this.objectPos.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
		double sdistance = Math.sqrt((xyz.fX * xyz.fX) + (xyz.fY * xyz.fY) + 
								  (xyz.fZ * xyz.fZ));	// + .0005;
		if (sdistance < fMaxOrbit) {
			point1 = getDrawPoint(xyz);
			og.setColor(colorObject);
			og.fillArc(point1.x - 2, point1.y - 2, 5, 5, 0, 360);
			og.setFont(fontObjectName);
			if (bObjectName) {
				og.setColor(colorObjectName);
				og.drawString(object.getName(), point1.x + 5, point1.y);
			}
			orbitViewer.CometTooFar = 0;
		} else {
			// On-screen warning about disappearance of object would go here.
			orbitViewer.CometTooFar = 1;
		}
		
		
		//  Draw Orbits of Planets
		if (Math.abs(epochPlanetOrbit - atime.getJd()) > 100.) { // 365.2422 * 5) {
			updatePlanetOrbit(atime);
		}
		og.setFont(fontPlanetName);
		
//		if (OrbitDisplay[0] || OrbitDisplay[10]) {
		if (OrbitDisplay[10]) {
			drawPlanetOrbit(og, planetOrbit[Planet.PLUTO-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(og, planetPos[8], "Pluto", radPluPix);
		
		if (OrbitDisplay[0] || OrbitDisplay[9]) {
			
			drawPlanetOrbit(og, planetOrbit[Planet.NEPTUNE-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(og, planetPos[7], "Neptune", radNepPix);
		
		if (OrbitDisplay[0] || OrbitDisplay[8]) {
			drawPlanetOrbit(og, planetOrbit[Planet.URANUS-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(og, planetPos[6], "Uranus", radUraPix);
		
		if (OrbitDisplay[0] || OrbitDisplay[7]) {
			drawPlanetOrbit(og, planetOrbit[Planet.SATURN-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(og, planetPos[5], "Saturn", radSatPix);
		
		if (OrbitDisplay[0] || OrbitDisplay[6]) {
			drawPlanetOrbit(og, planetOrbit[Planet.JUPITER-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(og, planetPos[4], "Jupiter", radJupPix);
		
		if (gZoom * 1.524 >= 7.5*extraZoom) {
			if (OrbitDisplay[0] || OrbitDisplay[5]) {
				
				drawPlanetOrbit(og, planetOrbit[Planet.MARS-Planet.MERCURY],
								colorPlanetOrbitUpper, colorPlanetOrbitLower);
			}
			drawPlanetBody(og, planetPos[3], "Mars", radMarPix);
		}
		if (gZoom * 1.000 >= 7.5*extraZoom) {
                        if (OrbitDisplay[0] || OrbitDisplay[4]) {

			   drawEarthOrbit(og, planetOrbit[Planet.EARTH-Planet.MERCURY],
						colorPlanetOrbitUpper, colorPlanetOrbitUpper);
                        }
			drawPlanetBody(og, planetPos[2], "Earth", radEarPix);
		}
		if (gZoom * 0.723 >= 7.5*extraZoom) {
                        if (OrbitDisplay[0] || OrbitDisplay[3]) {
			   drawPlanetOrbit(og, planetOrbit[Planet.VENUS-Planet.MERCURY],
						colorPlanetOrbitUpper, colorPlanetOrbitLower);
                        }
			drawPlanetBody(og, planetPos[1], "Venus", radVenPix);
		}
		if (gZoom * 0.387 >= 7.5*extraZoom) {
                        if (OrbitDisplay[0] || OrbitDisplay[2]) {
			   drawPlanetOrbit(og, planetOrbit[Planet.MERCURY-Planet.MERCURY],
						colorPlanetOrbitUpper, colorPlanetOrbitLower);
                        }
			drawPlanetBody(og, planetPos[0], "Mercury", radMerPix);
		}
		
		
		// Information
		og.setFont(fontInformation);
		og.setColor(colorInformation);
		FontMetrics fm = og.getFontMetrics();
		
		// Clone Count String
		if (orbitViewer.nClones > 0) {
			int nOrbits = orbitViewer.nClones + 1;
			String strNClones = "Orbits Displayed:  "+nOrbits;
			point1.x = this.sizeCanvas.width/2  - fm.stringWidth(strNClones)/2;
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
			og.drawString(strNClones, point1.x, point1.y);
		}
		
		// Object Name String
		point1.x = fm.charWidth('A');
//		point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
		point1.y = 2 * fm.charWidth('A');
		og.drawString(object.getName(), point1.x, point1.y);
		
		boolean atStopPoint = false;
		if (bDistanceLabel) {
			// Earth & Sun Distance
			DecimalFormat distFormat = new DecimalFormat("##0.000000");		// (awp)
			double edistance;	//>, sdistance;		//> See "Draw Object Body" above
			double xdiff, ydiff, zdiff;
//			BigDecimal a,v;
			String strDist;
//>			xyz  = this.objectPos.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
			xyz1 = planetPos[2].Rotate(this.mtxRotate);
//>			sdistance = Math.sqrt((xyz.fX * xyz.fX) + (xyz.fY * xyz.fY) + 
//>								  (xyz.fZ * xyz.fZ));	// + .0005;
//			sdistance = Math.round(sdistance * 100000.0)/100000.0;	// * 1000.0)/1000.0;
			xdiff = xyz.fX - xyz1.fX;
			ydiff = xyz.fY - xyz1.fY;	// geocentric coords (AU)
			zdiff = xyz.fZ - xyz1.fZ;
			edistance = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) +
								  (zdiff * zdiff));	// + .0005;
//			edistance = Math.round(edistance * 100000.0)/100000.0;	// * 1000.0)/1000.0;
			
					// Speed of the asteroid (awp)
					// -------------------------------------------------------
					DecimalFormat speedFormat = new DecimalFormat("#0.00");
					
					Xyz xyzNext, xyz1Next;
					Xyz objectPosNext, earthPosNext;
					double deltaX, deltaY, deltaZ;
					double deltaX1, deltaY1, deltaZ1;
					double xdiffNext, ydiffNext, zdiffNext;
					double deltaXdiff, deltaYdiff, deltaZdiff;
					
					double daysToNext = 5./1440.;
					double secsToNext = daysToNext*86400.;
					ATime atimeNext = new ATime(atime.getJd()+daysToNext, 0.0);
					
					objectPosNext	= this.object.GetPos(atime.getJd()+daysToNext);
					earthPosNext	= Planet.getPos(Planet.MERCURY+2, atimeNext);
					
					// Don't update mtxToEcl - Only when (Math.abs(epochToEcl - atime.getJd()) > 100. // 365.2422 * 5).
					// Don't update mtxRotate - Viewing perspective!.
					xyzNext			= objectPosNext.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
					xyz1Next		= earthPosNext.Rotate(this.mtxRotate);
					
					deltaX		= (xyzNext.fX - xyz.fX) * oneAU;	// x
					deltaY		= (xyzNext.fY - xyz.fY) * oneAU;	// change in heliocentric coords (km)
					deltaZ		= (xyzNext.fZ - xyz.fZ) * oneAU;	// x
					deltaX1		= (xyz1Next.fX - xyz1.fX) * oneAU;	// x
					deltaY1		= (xyz1Next.fY - xyz1.fY) * oneAU;	// change in Earth coords (km)
					deltaZ1		= (xyz1Next.fZ - xyz1.fZ) * oneAU;	// x
					xdiffNext	= xyzNext.fX  - xyz1Next.fX;	//
					ydiffNext	= xyzNext.fY  - xyz1Next.fY;	// NEXT geocentric coords (AU)
					zdiffNext	= xyzNext.fZ  - xyz1Next.fZ;	//
					deltaXdiff  = (xdiffNext  - xdiff ) * oneAU;	//
					deltaYdiff  = (ydiffNext  - ydiff ) * oneAU;	// change in geocentric coords (km)
					deltaZdiff  = (zdiffNext  - zdiff ) * oneAU;	//
					
					double helioSpeed = Math.sqrt( (deltaX*deltaX) + (deltaY*deltaY) + (deltaZ*deltaZ))/secsToNext;
					String strHelioSpeed = "Orbital Speed:  "+speedFormat.format(helioSpeed)+" km/s";
					point1.x = this.sizeCanvas.width  - fm.stringWidth(strHelioSpeed) - 2*fm.charWidth('A');
					point1.y = 2 * fm.charWidth('A');
					og.drawString(strHelioSpeed, point1.x, point1.y);

					double GMearth		= 6.67260*Math.pow(10.,-11) * 5.9742*Math.pow(10.,24.);
					double escSpeedHere = Math.sqrt( 2.*GMearth/(edistance  *oneAU*1000.) )/1000.;
					double escSpeedLuna = Math.sqrt( 2.*GMearth/(lunarDist  *oneAU*1000.) )/1000.;
					double escSpeedSync = Math.sqrt( 2.*GMearth/(geosyncDist*oneAU*1000.) )/1000.;
					double escSpeedSurf = Math.sqrt( 2.*GMearth/(earthRadius*oneAU*1000.) )/1000.;
					
					double geoSpeed		= Math.sqrt( (deltaXdiff*deltaXdiff) + (deltaYdiff*deltaYdiff) + 
												(deltaZdiff*deltaZdiff))/secsToNext;
					
					double impBoostLuna = Math.sqrt( 1. + Math.pow(escSpeedLuna/geoSpeed, 2.));
					double impBoostSync = Math.sqrt( 1. + Math.pow(escSpeedSync/geoSpeed, 2.));
					double impBoostSurf = Math.sqrt( 1. + Math.pow(escSpeedSurf/geoSpeed, 2.));
					
					double totGeoSpeed	= Math.sqrt( (geoSpeed*geoSpeed) + (escSpeedHere*escSpeedHere) );
					double impactSpeed	= Math.sqrt( (geoSpeed*geoSpeed) + (escSpeedSurf*escSpeedSurf) );
					
					
			// Distance Labels...
			strDist = "Earth Distance:  ";
			double inevSpeed=0.;
			if (edistance <= lunarDist*impBoostLuna) { // prevTimeStepChoice != -1) {
				double inevDist = edistance * 
								( Math.sqrt( Math.pow(escSpeedHere,4.) + 4.*Math.pow(geoSpeed,4.)) - Math.pow(escSpeedHere,2.) )
									/ (2.*Math.pow(geoSpeed,2.));
				double escSpeedInev = Math.sqrt( 2.*GMearth/(inevDist*oneAU*1000.) )/1000.;
				inevSpeed = Math.sqrt( (geoSpeed*geoSpeed) + (escSpeedInev*escSpeedInev) );
				
				strDist = strDist + distFormat.format(inevDist) + " AU *"; // + strCollide;
			} else {
				strDist = strDist + distFormat.format(edistance) + " AU"; // + strCollide;
			}
			
			point1.x = fm.charWidth('A'); 
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight()*4/3; // fm.getHeight()/3;
			og.drawString(strDist, point1.x, point1.y);
			
			int point4x = 2*fm.charWidth('A') + fm.stringWidth(strDist);
			int point4y = point1.y;

			strDist = "Sun Distance  :  " + distFormat.format(sdistance) + " AU";
			point1.x = fm.charWidth('A');
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
			og.drawString(strDist, point1.x, point1.y);
			
			
			// Report when certain lines are crossed (awp)
			String strCollide = "";
			boolean collisionFound = false;
			if (edistance > earthRadius*impBoostSurf) {
				String strTotGeoSpeed = "Speed Relative to Earth:  ";
				if (edistance <= lunarDist*impBoostLuna) { // prevTimeStepChoice != -1) {
					strTotGeoSpeed = strTotGeoSpeed + speedFormat.format(inevSpeed) + " km/s *";
				} else {
					strTotGeoSpeed = strTotGeoSpeed + speedFormat.format(totGeoSpeed) + " km/s";
				}
				point1.x = this.sizeCanvas.width  - fm.stringWidth(strTotGeoSpeed) - 2*fm.charWidth('A');
				point1.y = 2 * fm.charWidth('A') + 1*fm.getHeight();
				og.drawString(strTotGeoSpeed, point1.x, point1.y);
				
				if (edistance <= lunarDist*impBoostLuna && edistance > geosyncDist*impBoostSync) {
					strCollide = " - Closer than the Moon";
					og.setColor(colorLunar.darker());
					og.drawString(strCollide,     point4x,  point4y );
				} else if (edistance <= geosyncDist*impBoostSync && edistance > earthRadius*impBoostSurf) {
					strCollide = " - Closer than Geosync Satellites";
					og.setColor(colorGeosync.darker());
					og.drawString(strCollide,     point4x,  point4y );
				}
			} else {
				strCollide = " - COLLISION WITH EARTH!";
				collisionFound = true;
//				og.setColor(Color.darkGray);
//				String strImpactSpeed = "Impact Speed Relative to Earth:  "+speedFormat.format(impactSpeed)+" km/s";
				String strImpactSpeed = "Speed at Impact:  "+speedFormat.format(impactSpeed)+" km/s";
				point1.x = this.sizeCanvas.width  - fm.stringWidth(strImpactSpeed) - 2*fm.charWidth('A');
				point1.y = 2 * fm.charWidth('A') + 1*fm.getHeight();
				og.setColor(colorGeosync.darker().darker());
				og.drawString(strImpactSpeed, point1.x+2, point1.y+2);
				og.drawString(strCollide,     point4x +2, point4y +2);
				og.drawString(strImpactSpeed, point1.x+1, point1.y+1);
				og.drawString(strCollide,     point4x +1, point4y +1);
				og.setColor(colorGeosync.brighter().brighter()); // Change color for 3-D effect! ------------
				og.drawString(strImpactSpeed, point1.x, point1.y);
				og.drawString(strCollide,     point4x,  point4y );
				
				String strImpactSpeed2 = "(relative to Earth)";
				point1.x = this.sizeCanvas.width  - fm.stringWidth(strImpactSpeed2) - 2*fm.charWidth('A');
				point1.y = 2 * fm.charWidth('A') + 2*fm.getHeight();
				og.setColor(colorGeosync.darker().darker());
				og.drawString(strImpactSpeed2, point1.x+2, point1.y+2);
				og.drawString(strImpactSpeed2, point1.x+1, point1.y+1);
				og.setColor(colorGeosync.brighter().brighter()); // Change color for 3-D effect! ------------
				og.drawString(strImpactSpeed2, point1.x, point1.y);
//				orbitViewer.stop();
			//	orbitViewer.setTimeStepChoice(prevTimeStepChoice);
			//	prevTimeStepChoice = -1;
			}
			og.setColor(colorInformation);
			
			
			// Draw various ranges around the Earth...
			Xyz xyzEarth	 = planetPos[2].Rotate(this.mtxRotate);
			Point pointEarth = getDrawPoint(xyzEarth);
			if (gZoom * lunarDist >= 7.5*extraZoom) {						// Lunar Distance -------------------
				int lunarDistPixels  = (int)Math.round(gZoom*lunarDist/extraZoom);
				int lunarDistPixPlus = (int)Math.round(lunarDistPixels*1.25);
				og.setColor(colorLunar);
				og.drawArc(pointEarth.x - lunarDistPixels, pointEarth.y - lunarDistPixels, 
						(2*lunarDistPixels)+1, (2*lunarDistPixels)+1, 0, 360);
						// xUL, yUL, width, height, startAngle, endAngle
				og.drawLine(	pointEarth.x+lunarDistPixels+1,	pointEarth.y, 
								pointEarth.x+lunarDistPixPlus,	pointEarth.y);
				og.drawLine(	pointEarth.x-lunarDistPixels-1,	pointEarth.y, 
								pointEarth.x-lunarDistPixPlus,	pointEarth.y);
				og.drawLine(	pointEarth.x,			pointEarth.y+lunarDistPixels+1, 
								pointEarth.x,			pointEarth.y+lunarDistPixPlus);
				og.drawLine(	pointEarth.x,			pointEarth.y-lunarDistPixels-1, 
								pointEarth.x,			pointEarth.y-lunarDistPixPlus);
			}
			if (gZoom * geosyncDist >= 7.5*extraZoom) {						// Geosync Distance -----------------
				int geosyncDistPixels  = (int)Math.round(gZoom*geosyncDist/extraZoom);
				int geosyncDistPixPlus = (int)Math.round(geosyncDistPixels*1.50);
				og.setColor(colorGeosync);
				og.drawArc(pointEarth.x - geosyncDistPixels, pointEarth.y - geosyncDistPixels, 
						(2*geosyncDistPixels)+1, (2*geosyncDistPixels)+1, 0, 360);
						// xUL, yUL, width, height, startAngle, endAngle
				og.drawLine(	pointEarth.x+geosyncDistPixels+1,	pointEarth.y, 
								pointEarth.x+geosyncDistPixPlus,	pointEarth.y);
				og.drawLine(	pointEarth.x-geosyncDistPixels-1,	pointEarth.y, 
								pointEarth.x-geosyncDistPixPlus,	pointEarth.y);
				og.drawLine(	pointEarth.x,				pointEarth.y+geosyncDistPixels+1, 
								pointEarth.x,				pointEarth.y+geosyncDistPixPlus);
				og.drawLine(	pointEarth.x,				pointEarth.y-geosyncDistPixels-1, 
								pointEarth.x,				pointEarth.y-geosyncDistPixPlus);
			}
			og.setColor(colorInformation);
			
			
			// Change animation time-step when certain other lines are crossed... (slow-mo)
			if (orbitViewer.getPlayerThread() != null										// Player is running!
					&& orbitViewer.getTimeStepChoice() < orbitViewer.timeStepCount-3) {		// No time-steps over 1 month!
			
				int ovPlayDir		= orbitViewer.playDirection;
				TimeSpan ovTimeStep = orbitViewer.timeStep;										// ovTimeStep = current time step
				TimeSpan ovMoreStep = ovTimeStep;
				TimeSpan ovLessStep = ovTimeStep;
				if (orbitViewer.getTimeStepChoice() < orbitViewer.timeStepCount-1) {
					ovMoreStep = orbitViewer.timeStepSpan[orbitViewer.getTimeStepChoice() + 1];	// ovMoreStep = next-larger time step (or same!)
				}
				if (orbitViewer.getTimeStepChoice() > 0) {
					ovLessStep = orbitViewer.timeStepSpan[orbitViewer.getTimeStepChoice() - 1];	// ovMoreStep = next-smaller time step (or same!)
				}
				// Various-sized time steps, forward and backward:
				double jdStep1b= new ATime(	atime.getYear()		+ 1*ovPlayDir*ovLessStep.nYear,
											atime.getMonth()	+ 1*ovPlayDir*ovLessStep.nMonth,
											atime.getDay()		+ 1*ovPlayDir*ovLessStep.nDay,
											atime.getHour()		+ 1*ovPlayDir*ovLessStep.nHour,
											atime.getMinute()	+ 1*ovPlayDir*ovLessStep.nMin,
											atime.getSecond()	+ 1*ovPlayDir*ovLessStep.fSec,
											atime.getTimezone()									).getJd();
				double jdStep2 = new ATime(	atime.getYear()		+ 2*ovPlayDir*ovTimeStep.nYear,
											atime.getMonth()	+ 2*ovPlayDir*ovTimeStep.nMonth,
											atime.getDay()		+ 2*ovPlayDir*ovTimeStep.nDay,
											atime.getHour()		+ 2*ovPlayDir*ovTimeStep.nHour,
											atime.getMinute()	+ 2*ovPlayDir*ovTimeStep.nMin,
											atime.getSecond()	+ 2*ovPlayDir*ovTimeStep.fSec,
											atime.getTimezone()									).getJd();
				double jdStep1 = new ATime(	atime.getYear()		+ 1*ovPlayDir*ovTimeStep.nYear,
											atime.getMonth()	+ 1*ovPlayDir*ovTimeStep.nMonth,
											atime.getDay()		+ 1*ovPlayDir*ovTimeStep.nDay,
											atime.getHour()		+ 1*ovPlayDir*ovTimeStep.nHour,
											atime.getMinute()	+ 1*ovPlayDir*ovTimeStep.nMin,
											atime.getSecond()	+ 1*ovPlayDir*ovTimeStep.fSec,
											atime.getTimezone()									).getJd();
				double jdBack1 = new ATime(	atime.getYear()		- 1*ovPlayDir*ovTimeStep.nYear,
											atime.getMonth()	- 1*ovPlayDir*ovTimeStep.nMonth,
											atime.getDay()		- 1*ovPlayDir*ovTimeStep.nDay,
											atime.getHour()		- 1*ovPlayDir*ovTimeStep.nHour,
											atime.getMinute()	- 1*ovPlayDir*ovTimeStep.nMin,
											atime.getSecond()	- 1*ovPlayDir*ovTimeStep.fSec,
											atime.getTimezone()									).getJd();
				// Next-larger time steps, backward:
				double jdBack1a= new ATime(	atime.getYear()		- 1*ovPlayDir*ovMoreStep.nYear,
											atime.getMonth()	- 1*ovPlayDir*ovMoreStep.nMonth,
											atime.getDay()		- 1*ovPlayDir*ovMoreStep.nDay,
											atime.getHour()		- 1*ovPlayDir*ovMoreStep.nHour,
											atime.getMinute()	- 1*ovPlayDir*ovMoreStep.nMin,
											atime.getSecond()	- 1*ovPlayDir*ovMoreStep.fSec,
											atime.getTimezone()									).getJd();
				double jdBack2a= new ATime(	atime.getYear()		- 2*ovPlayDir*ovMoreStep.nYear,
											atime.getMonth()	- 2*ovPlayDir*ovMoreStep.nMonth,
											atime.getDay()		- 2*ovPlayDir*ovMoreStep.nDay,
											atime.getHour()		- 2*ovPlayDir*ovMoreStep.nHour,
											atime.getMinute()	- 2*ovPlayDir*ovMoreStep.nMin,
											atime.getSecond()	- 2*ovPlayDir*ovMoreStep.fSec,
											atime.getTimezone()									).getJd();
				
				ATime atimeStep1b = new ATime(jdStep1b, 0.0);	// next-smaller time steps
				ATime atimeStep2  = new ATime(jdStep2,  0.0);	// current-sized time steps
				ATime atimeStep1  = new ATime(jdStep1,  0.0);	// current-sized time steps
				ATime atimeBack1  = new ATime(jdBack1,  0.0);	// current-sized time steps
				ATime atimeBack1a = new ATime(jdBack1a, 0.0);	// next-larger time steps
				ATime atimeBack2a = new ATime(jdBack2a, 0.0);	// next-larger time steps
				
				Xyz objectPosStep1b	= this.object.GetPos(jdStep1b);
				Xyz objectPosStep2	= this.object.GetPos(jdStep2);
				Xyz objectPosStep1	= this.object.GetPos(jdStep1);
				Xyz objectPosBack1	= this.object.GetPos(jdBack1);
				Xyz objectPosBack1a	= this.object.GetPos(jdBack1a);
				Xyz objectPosBack2a	= this.object.GetPos(jdBack2a);
				
				Xyz earthPosStep1b	= Planet.getPos(Planet.MERCURY+2, atimeStep1b);
				Xyz earthPosStep2	= Planet.getPos(Planet.MERCURY+2, atimeStep2);
				Xyz earthPosStep1	= Planet.getPos(Planet.MERCURY+2, atimeStep1);
				Xyz earthPosBack1	= Planet.getPos(Planet.MERCURY+2, atimeBack1);
				Xyz earthPosBack1a	= Planet.getPos(Planet.MERCURY+2, atimeBack1a);
				Xyz earthPosBack2a	= Planet.getPos(Planet.MERCURY+2, atimeBack2a);
				
				Xyz xyzStep1b		= objectPosStep1b.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
				Xyz xyzStep2		= objectPosStep2.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
				Xyz xyzStep1		= objectPosStep1.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
				Xyz xyzBack1		= objectPosBack1.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
				Xyz xyzBack1a		= objectPosBack1a.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
				Xyz xyzBack2a		= objectPosBack2a.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
				
				Xyz xyz1Step1b		= earthPosStep1b.Rotate(this.mtxRotate);
				Xyz xyz1Step2		= earthPosStep2.Rotate(this.mtxRotate);
				Xyz xyz1Step1		= earthPosStep1.Rotate(this.mtxRotate);
				Xyz xyz1Back1		= earthPosBack1.Rotate(this.mtxRotate);
				Xyz xyz1Back1a		= earthPosBack1a.Rotate(this.mtxRotate);
				Xyz xyz1Back2a		= earthPosBack2a.Rotate(this.mtxRotate);
				
				double xdiffStep1b = xyzStep1b.fX - xyz1Step1b.fX;
				double ydiffStep1b = xyzStep1b.fY - xyz1Step1b.fY;	// geocentric coords (AU)
				double zdiffStep1b = xyzStep1b.fZ - xyz1Step1b.fZ;
				
				double xdiffStep2 = xyzStep2.fX - xyz1Step2.fX;
				double ydiffStep2 = xyzStep2.fY - xyz1Step2.fY;	// geocentric coords (AU)
				double zdiffStep2 = xyzStep2.fZ - xyz1Step2.fZ;
				
				double xdiffStep1 = xyzStep1.fX - xyz1Step1.fX;
				double ydiffStep1 = xyzStep1.fY - xyz1Step1.fY;	// geocentric coords (AU)
				double zdiffStep1 = xyzStep1.fZ - xyz1Step1.fZ;
				
				double xdiffBack1 = xyzBack1.fX - xyz1Back1.fX;
				double ydiffBack1 = xyzBack1.fY - xyz1Back1.fY;	// geocentric coords (AU)
				double zdiffBack1 = xyzBack1.fZ - xyz1Back1.fZ;
				
				double xdiffBack1a = xyzBack1a.fX - xyz1Back1a.fX;
				double ydiffBack1a = xyzBack1a.fY - xyz1Back1a.fY;	// geocentric coords (AU)
				double zdiffBack1a = xyzBack1a.fZ - xyz1Back1a.fZ;
				
				double xdiffBack2a = xyzBack2a.fX - xyz1Back2a.fX;
				double ydiffBack2a = xyzBack2a.fY - xyz1Back2a.fY;	// geocentric coords (AU)
				double zdiffBack2a = xyzBack2a.fZ - xyz1Back2a.fZ;
				
				double edistStep1b = Math.sqrt((xdiffStep1b * xdiffStep1b) + (ydiffStep1b * ydiffStep1b) + (zdiffStep1b * zdiffStep1b));
				double edistStep2  = Math.sqrt((xdiffStep2 * xdiffStep2) + (ydiffStep2 * ydiffStep2) + (zdiffStep2 * zdiffStep2));
				double edistStep1  = Math.sqrt((xdiffStep1 * xdiffStep1) + (ydiffStep1 * ydiffStep1) + (zdiffStep1 * zdiffStep1));
				double edistBack1  = Math.sqrt((xdiffBack1 * xdiffBack1) + (ydiffBack1 * ydiffBack1) + (zdiffBack1 * zdiffBack1));
				double edistBack1a = Math.sqrt((xdiffBack1a * xdiffBack1a) + (ydiffBack1a * ydiffBack1a) + (zdiffBack1a * zdiffBack1a));
				double edistBack2a = Math.sqrt((xdiffBack2a * xdiffBack2a) + (ydiffBack2a * ydiffBack2a) + (zdiffBack2a * zdiffBack2a));
				
				
				double speedFactor	= geoSpeed/6.4;		// 1.;
				double deltaDays	= Math.abs(jdStep1 - atime.getJd());
				double maxSlowDist	= .05 * Math.max(1., (deltaDays/1.)*Math.pow(object.getA()/50.0, 3./2.));
				double slowDist		= Math.min(maxSlowDist, (2.*lunarDist*impBoostLuna + 2.*geoSpeed*deltaDays*86400./oneAU)*speedFactor);
									// The slow-down distance is (2 lunar distances + 2 object motion steps), 
									// scaled up by the object's geocentric speed (relative to 6.4 km/s).
									// This slow-down distance is capped at 0.05 AU for time-steps < 1 day,
									// and this cap is scaled up by step-size and by the orbital period 
									// (relative to that of an object at 50 AU).  The notion for the latter is
									// that objects with greater periods approach Earth less frequently, so
									// these approaches deserve greater scrutiny.
				double stopDist		= 1.*lunarDist*impBoostLuna;
//				System.out.println("");
//				System.out.println(">>> "+atime);
//				System.out.println("    maxSlowDist  = "+maxSlowDist);
//				System.out.println("       slowDist  = "+slowDist);
//				System.out.println("       edistance = "+edistance);
				
				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				boolean caStopCrit = edistance < edistStep1 && edistance < edistBack1 && edistance > earthRadius*impBoostSurf;
				//                     current < next       &&   current < previous   &&   current > boosted-earth-radius
				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				
				if ( (ovPlayDir == 1 && edistance <= earthRadius*impBoostSurf && edistBack1 > earthRadius*impBoostSurf) ||
					 (ovPlayDir ==-1 && edistance <= earthRadius*impBoostSurf && edistStep1 > earthRadius*impBoostSurf) ) {
				//     forward-play  &&   current <= boosted-earth-radius     &&       prev > boosted-earth-radius
				//												OR
				//     reverse-play  &&   current <= boosted-earth-radius     &&       next > boosted-earth-radius
				// -----------------------------------------------------------------------------------------------
//					System.out.println("Collision stop!");
					orbitViewer.stop();
					atStopPoint = true;		// Collision!
					
				} else if ( (edistStep1 < edistance &&    edistStep1 < edistStep2	&&   edistStep1 < slowDist) ||
							(		caStopCrit		&&   edistStep1b < edistance	&&  edistStep1b < slowDist && prevTimeStepChoice >= 0) ) {
				//                 next < current   &&          next < after-next	&&         next < slow-mo-limit
				//												OR
				//					see-above		&&	next-smaller < current		&& next-smaller < slow-mo-limit
				// ------------------------------------------------------------------------------------------------
					if (edistStep1 < edistance &&    edistStep1 < edistStep2	&&   edistStep1 < slowDist) {
//						System.out.println("Min edistance found at NEXT step!");
					} else {
//						System.out.println("Min edist for this time-step found HERE, but downshift would get closer!");
					}
					if (prevTimeStepChoice < 0) {					// No prevTimeStepChoice registered yet!
//						System.out.println("Registering prevTimeStepChoice.");
						prevTimeStepChoice = orbitViewer.getTimeStepChoice();
					}
					if (orbitViewer.getTimeStepChoice() > 0) {		// We're not at the smallest time-step yet! 
//						System.out.println("Down-shifting...");
						orbitViewer.setTimeStepChoice(orbitViewer.getTimeStepChoice() - 1);
					}
					atStopPoint = false;
					
				} else if (caStopCrit && edistance < stopDist) {
				//           current < next       &&   current < previous	&& current > boosted-earth-radius	&&   current < stop-lim
				// --------------------------------------------------------------------------------------------------------------------
//					System.out.println("Min edistance found HERE.  Stop!");
					orbitViewer.stop();
					atStopPoint = true;
					
				} else if ((edistBack1a < edistance && edistBack1a < edistBack2a && edistBack1a < slowDist)
							|| (prevTimeStepChoice != -1 && edistStep2 > edistStep1 && edistStep1 > edistance 
								&& edistance > edistBack1a && edistBack1a > edistBack2a)) {
				// (prev-larger < current	&& prev-larger < next-prev-larger	&& prev-larger < slow-mo-limit)
				// || (slow-mo-engaged		&& after-next > next				&& next > current
				//							&& current > prev-larger			&& prev-larger > next-prev-larger)
				// -----------------------------------------------------------------------------------------------
//					System.out.println("Work on getting out of slow-mo mode...");
				/*	if (prevTimeStepChoice != -1 && edistStep2 > edistStep1 && edistStep1 > edistance 
								&& edistance > edistBack1a && edistBack1a > edistBack2a) {
						System.out.println("***** Escaping from Slow-Mo mode... *****");
					} else {
						System.out.println("Min edistance found at LAST (bigger) step!");
					}
				*/
					if (orbitViewer.getTimeStepChoice() < prevTimeStepChoice) {		// Not back up to prevTimeStepChoice yet!
//						System.out.println("Up-shifting.");
						orbitViewer.setTimeStepChoice(orbitViewer.getTimeStepChoice() + 1);
					} else {
//						System.out.println("Forgetting prevTimeStepChoice.");
						prevTimeStepChoice = -1;
					}
					atStopPoint = false;
				} else {
					atStopPoint = false;
				}
			//	System.out.println(".");
			}
			
			if (prevTimeStepChoice != -1) {
				String strSlowMo = "Slow-Mo Mode";
				if (atStopPoint) {
					if (collisionFound) {
						strSlowMo = "Stopped at Impact";
					} else {
						strSlowMo = "Stopped at Closest Approach";
					}
					orbitViewer.jdStopped = atime.getJd();
				}
				point1.x = this.sizeCanvas.width/2  - fm.stringWidth(strSlowMo)/2;
				point1.y = 2 * fm.charWidth('A');
				og.drawString(strSlowMo, point1.x, point1.y);
			}
			
			if (prevTimeStepChoice != -1 || edistance <= 3.*lunarDist*impBoostLuna) {
				if (Math.abs(epochToEcl - atime.getJd()) > 0.5) { // 365.2422 * 5) {
					updateRotationMatrix(atime);
                }
			}
			
		}
		
		if (bDateLabel) {
			// Date String
			DecimalFormat twoDigitFormat = new DecimalFormat("00");		// (awp)
			String strDate = ATime.getMonthAbbr(atime.getMonth())
				+ " " + atime.getDay() + ", " + atime.getYear();
			if (atime.getHour() != 0 || atime.getMinute() != 0 || orbitViewer.getTimeStepChoice() <= 1) {
				strDate=twoDigitFormat.format(atime.getHour())+":"+
					twoDigitFormat.format(atime.getMinute())+" "+strDate;
			}
			point1.x = this.sizeCanvas.width  - fm.stringWidth(strDate) - fm.charWidth('A');
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight()*4/3;
//			point1.y = 2 * fm.charWidth('A');
			og.drawString(strDate, point1.x, point1.y);
			
			DecimalFormat jdFormat = new DecimalFormat("0000000.0000");		// (awp)
			String strJd = "JD:  "+jdFormat.format(atime.getJd());
			point1.x = this.sizeCanvas.width  - fm.stringWidth(strJd) - fm.charWidth('A');
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
			og.drawString(strJd, point1.x, point1.y);
		}
		
		// Border
		og.clearRect(0,                    sizeCanvas.height - 1,
					 sizeCanvas.width,     sizeCanvas.height     );
		og.clearRect(sizeCanvas.width - 1, 0,
					 sizeCanvas.width,     sizeCanvas.height     );
		
		g.drawImage(offscreen, 0, 0, null);
		
		if (orbitViewer.getPlayerThread() != null && orbitViewer.playDirection > 0 && orbitViewer.jdStopped != -1 && atime.getJd()-orbitViewer.jdStopped > 1.5) {
			orbitViewer.stop();
			int confirmResult = JOptionPane.showConfirmDialog(null, 
				"These orbital parameters are not valid beyond this date,\ndue to the Earth's gravitational influence.\nDo you want to continue anyway?",
				"Orbital Parameters Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (confirmResult == JOptionPane.YES_OPTION) {
				orbitViewer.jdStopped = -1;
			}
		}
	}
	
	/**
	 * paint if invalidate the canvas
	 */
	public void paint(Graphics g) {
		if (offscreen == null) {
			this.sizeCanvas = size();
			offscreen = createImage(this.sizeCanvas.width,
									this.sizeCanvas.height);
			update(g);
			
		} else {
			g.drawImage(offscreen, 0, 0, null);
		}
	}
}

/**
 *  Date Setting Dialog
 */
class DateDialog extends Frame {
	
	protected TextField		tfYear;
	protected TextField		tfDate;
	protected TextField		tfJd;
	protected Choice		choiceMonth;
	protected Label			jdLabel;
	protected Label			statusLabel;
	
	protected Button		buttonOk;
	protected Button		buttonCancel;
	protected Button		buttonToday;
	
	DecimalFormat jdFormat = new DecimalFormat("0000000.0000");
	
	protected OrbitViewer	objectOrbit;
	
	public DateDialog(OrbitViewer objectOrbit, ATime atime, Component buttonDate) {
		this.objectOrbit = objectOrbit;
		

		// Layout
//		setLayout(new GridLayout(2, 3, 4, 4));
		
		GridBagLayout      l = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		setLayout(l);
		setFont(new Font("Dialog", Font.PLAIN, 14));
		
		// Controls - Row 0 ----------------------------------------------------
		choiceMonth = new Choice();
		for (int i = 0; i < 12; i++) {
			choiceMonth.addItem(ATime.getMonthAbbr(i + 1));
		}
		choiceMonth.select(atime.getMonth() - 1);
		choiceMonth.addItemListener( new ItemListener() {
		    public void itemStateChanged(ItemEvent e) {
				if (choiceMonth.isFocusOwner()) {
					double jdNewDate = new ATime(Integer.valueOf(tfYear.getText()).intValue(),
												 choiceMonth.getSelectedIndex() + 1,
												 Integer.valueOf(tfDate.getText()).intValue(),
												 0.0).getJd();
					tfJd.setText(jdFormat.format(jdNewDate));
					statusLabel.setText(" ");
				}
		    }
		});
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		l.setConstraints(choiceMonth, c);
		add(choiceMonth);
		
		Integer iDate = new Integer(atime.getDay());
		tfDate = new TextField(iDate.toString(), 2);
		tfDate.addKeyListener( new IntegerKeyListener(tfDate) );	// (awp)
		tfDate.addTextListener( new TextListener() {
		    public void textValueChanged(TextEvent e) {
			if (tfDate.isFocusOwner()) {
				double jdNewDate = new ATime(Integer.valueOf(tfYear.getText()).intValue(),
											 choiceMonth.getSelectedIndex() + 1,
											 Integer.valueOf(tfDate.getText()).intValue(),
											 0.0).getJd();
				tfJd.setText(jdFormat.format(jdNewDate));
				statusLabel.setText(" ");
			}
		    }
		});
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		l.setConstraints(tfDate, c);
		add(tfDate);
		
		Integer iYear = new Integer(atime.getYear());
		tfYear = new TextField(iYear.toString(), 4);
		tfYear.addKeyListener( new IntegerKeyListener(tfYear) );	// (awp)
		tfYear.addTextListener( new TextListener() {
		    public void textValueChanged(TextEvent e) {
			if (tfYear.isFocusOwner()) {
				double jdNewDate = new ATime(Integer.valueOf(tfYear.getText()).intValue(),
											 choiceMonth.getSelectedIndex() + 1,
											 Integer.valueOf(tfDate.getText()).intValue(),
											 0.0).getJd();
				tfJd.setText(jdFormat.format(jdNewDate));
				statusLabel.setText(" ");
			}
		    }
		});
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		l.setConstraints(tfYear, c);
		add(tfYear);
		
		// Controls - Row 1 ----------------------------------------------------
		jdLabel = new Label("or JD = ");
		jdLabel.setAlignment(Label.RIGHT);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		l.setConstraints(jdLabel, c);
		add(jdLabel);
		
		double dJd = new Double(atime.getJd());
		tfJd = new TextField(jdFormat.format(dJd), 10);
		tfJd.addKeyListener( new DoubleKeyListener(tfJd) );
		tfJd.addTextListener( new TextListener() {
			public void textValueChanged(TextEvent e) {
			if (tfJd.isFocusOwner()) {
				ATime atimeNewDate = new ATime(Double.valueOf(tfJd.getText()),
											   0.0);
				tfYear.setText(Integer.toString(atimeNewDate.getYear()));
				tfDate.setText(Integer.toString(atimeNewDate.getDay()));
				choiceMonth.select(atimeNewDate.getMonth()-1);
				statusLabel.setText(" ");
			}
		    }
		});
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		l.setConstraints(tfJd, c);
		add(tfJd);
		
		// Controls - Row 2 ----------------------------------------------------
		statusLabel = new Label(" ");
		statusLabel.setAlignment(Label.CENTER);
		statusLabel.setForeground(Color.red);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.gridheight = 1;
		l.setConstraints(statusLabel, c);
		add(statusLabel);

		// Controls - Row 3 ----------------------------------------------------
		buttonToday = new Button("Today");
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		l.setConstraints(buttonToday, c);
		add(buttonToday);
		
		buttonOk = new Button("OK");
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		l.setConstraints(buttonOk, c);
		add(buttonOk);
		
		buttonCancel = new Button("Cancel");
		c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		l.setConstraints(buttonCancel, c);
		add(buttonCancel);
		

		
		pack();
		
		setTitle("Date");
		setResizable(false);
		setLocationRelativeTo(buttonDate);	// (awp)
		
		show();
		toFront();
		buttonOk.requestFocus();	// (awp)
		
	}
	
	/**
	 * Event Handler
	 */
    public boolean handleEvent(Event evt) {
		if (evt.id == Event.ACTION_EVENT) {
			ATime atime = null;
			if (evt.target == buttonOk) {
				int nYear = Integer.valueOf(tfYear.getText()).intValue();
				int nMonth = choiceMonth.getSelectedIndex() + 1;
				int nDate  = Integer.valueOf(tfDate.getText()).intValue();
				if (1600 <= nYear && nYear <= 2199 &&
							1 <= nMonth && nMonth <= 12 &&
							1 <= nDate  && nDate  <= 31) {
				//	atime = new ATime(nYear, nMonth, (double)nDate, 0.0);
					atime = new ATime(Double.valueOf(tfJd.getText()), 0.0);
				} else {
					if (1600 > nYear || nYear > 2199) {
						statusLabel.setText("Bad Year (1600-2199)");
					} else if (1 > nMonth || nMonth > 12) {
						statusLabel.setText("Bad Month (1-12)");
					} else if (1 > nDate || nDate > 31) {
						statusLabel.setText("Bad Date (1-31)");
					}
					return true;
				}
			} else if (evt.target == buttonToday) {
				Date date = new Date();
				choiceMonth.select(date.getMonth());
				tfDate.setText(Integer.toString(date.getDate()));
				tfYear.setText(Integer.toString(date.getYear() + 1900));
				double jdNewDate = new ATime(Integer.valueOf(tfYear.getText()).intValue(),
											 choiceMonth.getSelectedIndex() + 1,
											 Integer.valueOf(tfDate.getText()).intValue(),
											 0.0).getJd();
				tfJd.setText(jdFormat.format(jdNewDate));
				statusLabel.setText(" ");
				return false;
			} else if (evt.target != buttonCancel) {
				return false;
			}
			dispose();
			objectOrbit.endDateDialog(atime);
			return true;
		}
		return false;	// super.handleEvent(evt);
	}
}

