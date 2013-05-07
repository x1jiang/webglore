package glore;
import java.io.*;
import java.net.URL;
//import java.sql.*;
//import java.net.URLConnection;
//import java.net.MalformedURLException;
import java.awt.*;
import java.awt.event.*;
//import java.applet.*;
import javax.swing.*;
import javax.swing.border.*;
import java.lang.Math;
import Jama.Matrix;

//import java.security.AccessController;
//import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Vector;

public class LocalTestApplet extends JApplet implements ActionListener{
	private JScrollPane scrolling = null;
	private JTextPane fileBox = null;
	private JButton butFile = null;
	private JTextField tfFilename = null;
	private JButton butLoad = null;
	private JButton butSend = null;
	private final String LOAD = "load";
	private final String SEND = "send";
	private final String FILE = "file";
	private String name_ = "";
	private String passWord_ = "";
	private String task_ = "";
	private String email_ = "";
	private String root_ = "";
	private String parameters_ = "";
	private String featureNo_ = "";
	private String kernel_ = "";
	private String noFeature_ = "";
	private String noRecord_ = "";
	private String target_ = "";
	private String property_="";
	private String fileName_="";
	private String ResultString="";
	private String betaString = "0.5#0.5#0.5";
	private String OutputString="";//added lph
	
	public void init()
	{
		try
		{
		//	task_ = getParameter("task");
		//	email_ = getParameter("email");
		//	root_ = getParameter("root");
		//	parameters_ = getParameter("param");
			//the input parameter may have two names but not in the same time jwc 10.5
			if(getParameter("showFilePath")!=null){
				fileName_ = getParameter("showFilePath");
			}
			if(getParameter("userFilePath")!=null){
				fileName_ = getParameter("userFilePath");
			}
			if(getParameter("betaString")!=null){
				betaString = getParameter("betaString");
			}
			
			setSize(100, 400);
			setBackground(new Color(255, 255, 255));
			jbInit();	
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception
	{
		//add file panel
		JPanel filePane = new JPanel();
		filePane.setBackground(new Color(255, 255 ,255 ));
		Border etchedBdr = BorderFactory.createEtchedBorder();
		Border titledBdr = BorderFactory.createTitledBorder(etchedBdr, "Specify the path to your local test data file:");
		Border emptyBdr  = BorderFactory.createEmptyBorder(10,10,10,10);
		Border compoundBdr=BorderFactory.createCompoundBorder(titledBdr, emptyBdr);
		filePane.setBorder(compoundBdr);
		filePane.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		tfFilename = new JTextField();
		tfFilename.setText(fileName_);
		tfFilename.setSize(206, 29);
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.weightx = 1.0;
		c1.gridwidth = 1;
		c1.gridx = 0;
		c1.gridy = 0;
		
		filePane.add(tfFilename, c1);
		//button "browse"
		butFile = new JButton();
		butFile.setText("Browse");
		butFile.setSize(180, 29);
		c1.fill = GridBagConstraints.NONE;
		//c1.anchor = GridBagConstraints.HORIZONTAL;
		c1.weightx = 0.1;
		c1.gridwidth = 1;
		c1.gridx = 1;
		c1.gridy = 0;
		butFile.setActionCommand(FILE);
		butFile.addActionListener(this);
		filePane.add(butFile, c1);
		
		butLoad = new JButton();
		butLoad.setText("Submit");
		butLoad.setSize(180, 29);
		c1.fill = GridBagConstraints.NONE;
		c1.anchor = GridBagConstraints.EAST;
		c1.weightx = 0.1;
		c1.gridwidth = GridBagConstraints.REMAINDER;
		c1.gridx = 2;
		c1.gridy = 0;
		butLoad.setActionCommand(LOAD);
		butLoad.addActionListener(this);
		filePane.add(butLoad);		

		add(filePane, BorderLayout.NORTH);
		
	}
	
	public String getFilename(){
		return tfFilename.getText();
	}
	public String getAttributes(){
		return property_;
	}
	public String getResults(){
		return ResultString;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getActionCommand().equals(FILE))
		{
			Frame f=new Frame();
			FileDialog fd = new FileDialog(f, "Select File", FileDialog.LOAD);
			fd.setVisible(true);
			if(fd.getFile()!=null)
			{
				fileName_ = fd.getDirectory() + fd.getFile();
			}
			tfFilename.setText(fileName_);
			
			//Here is the function of "load", combine them together 11.5 jwc
			try{
				FileInputStream data = new FileInputStream(fileName_);
				DataInputStream data_file = new DataInputStream (data);
				property_ =data_file.readLine();			//added by lph
				System.out.println("input3: " + property_);
				data_file.close();
			//	getAppletContext().showDocument(new URL("javascript:window.accessAppletMethod()"));
			}catch(Exception exc){
				property_ = property_ + exc.getMessage();
			}
		}
		if (e.getActionCommand().equals(LOAD))
		{
			try{
				FileInputStream file;
				DataInputStream file_br;
				String file_line;
				String[] line_tokens;
				// number of columns and rows in data file
				int m=-1, n=0;
				// data structures used to hold the client data read in from files
				Vector< Vector<Double> > Xv = new Vector< Vector<Double> >();
				Vector<Double> Yv = new Vector<Double>();
				Vector<Double> xrow;
				// data structures used to hold client data for matrix constructors
				double[][] Xa;
				double[] Ya;
				double[] result;
				Matrix beta, aucValue, X, Y, P, E;	   
				//int i, j;

			 		file= new FileInputStream(fileName_); 
			 		file_br= new DataInputStream (file);
					file_br.readLine();
					while ((file_line = file_br.readLine()) != null) {
						n = n + 1;
						line_tokens = file_line.split("\t");
						if (m == -1) {
							m = line_tokens.length;
						}else if (m != line_tokens.length) {
							System.out.println("ERROR: data file dimensions don't " +
								"match on line " + n + ".");
							System.exit(-1);
						}
						xrow = new Vector<Double>();
						xrow.add(1.0);
						for (int i = 0; i < line_tokens.length - 1; i++) {
							xrow.add(new Double(line_tokens[i]));
						}
						Xv.add(xrow);
						Yv.add(new Double(line_tokens[line_tokens.length-1]));
					}
					//assume beta as 0.5 for all
					String[] ss = betaString.split("#");
					double[][] betaValue = new double[ss.length][1];
					for(int i=0; i<ss.length; i++){
						betaValue[i][0] = Double.parseDouble(ss[i]);
					}
					beta = new Matrix(betaValue);
//					beta = new Matrix(m, 1, 0.5);
					aucValue = new Matrix(1,1,0);
					Xa = two_dim_vec_to_arr(Xv);
					Ya = one_dim_vec_to_arr(Yv);
					//Initialize result = Y
					result= one_dim_vec_to_arr(Yv);
					X = new Matrix(Xa);
					Y = new Matrix(Ya, Ya.length);
					// calculate P <- 1 + exp(-x%*%beta0)
					P = (X.times(-1)).times(beta);
					exp(P.getArray());
					add_one(P.getArray());
					div_one(P.getArray());
					ResultString=ResultString+"<table><tr><td>Predicted Probability</td><td>Y-value</td></tr>";
				//	ResultString=ResultString+"\t"+"Predicted Probability  & "+"\t"+"Y value"+"<br>";
					OutputString=OutputString+"\t"+"Predicted Probability  & "+"\t"+"Y value"+"\n";

					for (int k=0; k<n; k++) {
						result[k]=P.getArray()[k][0];
						ResultString=ResultString+"<tr><td>"+result[k]+"</td><td>"+Ya[k]+"</td></tr>";
						System.out.println(k+1+"\t"+result[k]+"\t"+Ya[k]);
						OutputString=OutputString+"\t"+result[k]+"\t"+Ya[k]+"\n";
					}
					ResultString=ResultString+"</table>";
					getAppletContext().showDocument(new URL("javascript:window.accessAppletResult()"));
					
					Frame f=new Frame();
					FileDialog fd = new FileDialog(f, "Save Test Result", FileDialog.SAVE);
					fd.setVisible(true);
					try
					{
						 File f1 = new File(fd.getDirectory(), fd.getFile());
						 BufferedWriter out = new BufferedWriter(new FileWriter(f1));
//						 FileOutputStream out = new FileOutputStream(f1);
						 out.write(OutputString);
						 out.close();
						 OutputString = "";
					}
					catch (IOException e2)
					{
						e2.printStackTrace();
					}
					
					
					// calculate AUC
					Matrix scoreMatrix = X.times(beta);
					// e <- t(x)%*%(y-p)
					E = (X.transpose()).times(Y.plus(P.uminus()));
					double[][] combine = new double[n][2];
					for(int i = 0; i< n; i++){
						combine[i][0] = (1/ (1 + Math.pow(Math.E, -1 * scoreMatrix.get(i, 0))));
						combine[i][1] = Y.get(i, 0);
					}
					Matrix aucData = new Matrix(combine);
					Arrays.sort(combine, new ScoreComparator());
					int nt = 0,nf = 0,S = 0;		//number of true/false items, the sequence sum of true items
					for(int i = 0; i < combine.length; i++){
						if(combine[i][1] > 0.5){
							nt ++;
							S = S + i;
						}else nf++;
					}		
			}catch(Exception exc){
			
			}
		}
	}
/* Set each element of the 2D double array to e^a where a is the value of an element. */
public static void exp(double[][] A) {
	int i,j;
	for (i = 0; i < A.length; i++) {
		for (j = 0; j < A[i].length; j++) {
			A[i][j] = Math.exp(A[i][j]);
		}
	}
}	
/* Set each element of the 2D double array to 1 + a where a is the value of
 an element. */
public static void add_one(double[][] A) {
	int i,j;
	for (i = 0; i < A.length; i++) {
		for (j = 0; j < A[i].length; j++) {
			A[i][j] = 1 + A[i][j];
		}
	}
}	
/* Set each element of the 2D double array to 1/a where a is the value of
 an element. */
public static void div_one(double[][] A) {
	int i,j;
	for (i = 0; i < A.length; i++) {
		for (j = 0; j < A[i].length; j++) {
			A[i][j] = 1.0 / A[i][j];
		}
	}
}
/* Convert a 2D vector of Doubles into a 2D array of doubles. */
public static double[][] two_dim_vec_to_arr(Vector< Vector<Double> >V) {
	// allocate part of the array
	double[][] A = new double[V.size()][];
	int i;
	
	// allocate and convert rows of the vector
	for (i = 0; i < V.size(); i++) {
		A[i] = one_dim_vec_to_arr(V.get(i));
	}
	
	// return 2D array
	return A;
}
/* Convert a Vector of Doubles into an array of doubles. */
public static double[] one_dim_vec_to_arr(Vector<Double> V) {
	int size = V.size();
	int i;
	double[] A = new double[size];
	
	for (i = 0; i < size; i++) {
		A[i] = (V.get(i)).doubleValue();
	}
	
	return A;
}
}