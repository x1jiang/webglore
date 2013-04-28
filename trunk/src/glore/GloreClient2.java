package glore;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;
import java.util.Arrays;
import java.lang.Math;

import Jama.Matrix;

public class GloreClient2 {
	public GloreClient2(String taskName, String dataPath, String root_property, int maxIteration, double epsilon, int taskStatus ){
		super();
		//if task has finished, do nothing
		System.out.println("taskStatus is " + taskStatus);
		if(taskStatus == 2){
			
			return;
		}
		try {
		    // data file name
		    String file_name;

		    // used for reading the data file
		    FileInputStream file_stream;
		    DataInputStream file_in;
		    BufferedReader file_br;
		    String file_line;
		    String[] line_tokens;
		    
		   // String taskName = "t1";

		    // data structures used to hold the client data read in from files
		    Vector< Vector<Double> > Xv = new Vector< Vector<Double> >();
		    Vector<Double> Yv = new Vector<Double>();
		    Vector<Double> xrow;

		    // data structures used to hold client data for matrix constructors
		    double[][] Xa;
		    double[] Ya;

		    // number of columns and rows in data file
		    int m = -1;
		    int n;

		    //double epsilon = Math.pow(10.0, -6.0);
		    Matrix beta0;
		    Matrix beta1;
		    Matrix hat_beta;
		    Matrix cov_matrix, SD, aucValue;
		    Matrix X, Y;
		    Matrix P, W, D, E;	   

		    int i, j;

		    // count the number of iterations
		    int iter = 0;

//------------------ //check of filename will be in a different way jwc 10.9 -----------------------------------
//		    // missing filename as argument
		    //file_name = "C:/Users/Wenchao/workspace/GLORE_Develop/ca_part2";	//This variable will tansmit from upper  jwc 10.9		
		    file_name = dataPath;
		    System.out.println("Using data file '" + file_name + "'.");
//---------------------------------------------------------------------------------------------------------------------------------------

		    // access the file
		    file_stream = new FileInputStream(file_name);
		    file_in = new DataInputStream(file_stream);
		    file_br = new BufferedReader(new InputStreamReader(file_in));

		    // read file and populate X and Y matrices
		    n = 0;
		    //ignore the first line  jwc 10.26
		    file_br.readLine();
		    while ((file_line = file_br.readLine()) != null) {
			// update number of rows
			n = n + 1;
			line_tokens = file_line.split("\t");

			// detect number of columns in data file
			if (m == -1) {
			    m = line_tokens.length;
			}
			// line in data file does not match dimensions
			else if (m != line_tokens.length) {
			    System.out.println("ERROR: data file dimensions don't " +
					       "match on line " + n + ".");
			    System.exit(-1);
			}

			// populate data structures with data
			xrow = new Vector<Double>();
			xrow.add(1.0);
			for (i = 0; i < line_tokens.length - 1; i++) {
			    xrow.add(new Double(line_tokens[i]));
			}
			Xv.add(xrow);
			Yv.add(new Double(line_tokens[line_tokens.length-1]));
		    }

		    // close input stream
		    file_in.close();

		    beta0 = new Matrix(m, 1, -1.0);
		    beta1 = new Matrix(m, 1, 0.0);
		    cov_matrix = new Matrix(m, m);
		    aucValue = new Matrix(1,1,0);

		    // convert data into arrays to be passed to Matrix's constructor
		    Xa = two_dim_vec_to_arr(Xv);
		    Ya = one_dim_vec_to_arr(Yv);

		    // create X and Y matrices
		    X = new Matrix(Xa);
		    Y = new Matrix(Ya, Ya.length);
		    
		    //print X, Y, jwc 11.1
//		    X.print(1, 1);
//		    Y.print(1, 1);
			
	    	System.out.println("maximum Iteration:"+maxIteration);
		    // iteratively update beta1
		    while (max_abs((beta1.minus(beta0)).getArray()) > epsilon) {
//				 if (iter == 20)
//				     break;
		    	 if (iter == maxIteration)//added by lph
			     {
			    	 System.out.println("Has reached the maximum iteration number!");
			    	 break; 
			     }
				URL url = new URL(root_property + "gloreserverservlet");
				URLConnection servletConnection = url.openConnection();			
				servletConnection.setDoInput(true);
				servletConnection.setDoOutput(true);			
				servletConnection.setUseCaches(false);
				servletConnection.setDefaultUseCaches(false);				
				servletConnection.setRequestProperty ("Content-Type", "application/x-java-serialized-object");	
				
				System.out.println("value: " + max_abs((beta1
			            .minus(beta0)).getArray()));

				System.out.println("Iteration " + iter);

				beta0 = beta1.copy();

				// P <- 1 + exp(-x%*%beta0)
				P = (X.times(-1)).times(beta0);
				exp(P.getArray());
				add_one(P.getArray());
				div_one(P.getArray());

				// w = diag(c(p*(1-p)))
				W = P.copy();
				W.timesEquals(-1.0);
				add_one(W.getArray());
				W.arrayTimesEquals(P);
				W = W.transpose();
				W = diag(W.getArray()[0]);

				// d <- t(x)%*%w%*%x
				D = ((X.transpose()).times(W)).times(X);
				// e <- t(x)%*%(y-p)
				E = (X.transpose()).times(Y.plus(P.uminus()));

//--------------//send D E and receive beta from Server jwc 10.10------------------------------------------------------------
//				// send D and E to server
				DataIn2 DE = new DataIn2(D, E, taskName, "beta");
			    OutputStream out = servletConnection.getOutputStream();				
				ObjectOutputStream oos = new ObjectOutputStream(out);
				oos.writeObject(DE);
				oos.flush();
				oos.close();
//				// receive beta1 from server
				InputStream in = servletConnection.getInputStream();
				ObjectInputStream ois;
				ois = new ObjectInputStream(in);
				DataOut2 result = (DataOut2) ois.readObject();
				ois.close();

				if(result.getType().equals("beta")){
					beta1 = result.getMatrix();
				}
				else{
					System.out.println("beta type needed");
				}
//---------------------------------------------------------------------------------------------------------------------
				// print beta1 for this iteration
				beta1.print(8, 8);
				iter = iter + 1;
		    }

		    System.out.println("value on exit: " + max_abs((beta1
		            .minus(beta0)).getArray()));
		    System.out.println("Finished iteration.");

		    // compute variance-covariance-covariance matrix
		    hat_beta = beta1.copy();

		    P = (X.times(-1)).times(hat_beta);
		    exp(P.getArray());
		    add_one(P.getArray());
		    div_one(P.getArray());

		    W = P.copy();
		    W.timesEquals(-1.0);
		    add_one(W.getArray());
		    W.arrayTimesEquals(P);
		    W = W.transpose();
		    W = diag(W.getArray()[0]);

		    D = ((X.transpose()).times(W)).times(X);

//--------------//send D and receive cov  jwc 10.10 --------------------------------------------------------------------
//		    // send conD to server		    
			URL url = new URL(root_property + "gloreserverservlet");
			URLConnection servletConnection = url.openConnection();			
			servletConnection.setDoInput(true);
			servletConnection.setDoOutput(true);			
			servletConnection.setUseCaches(false);
			servletConnection.setDefaultUseCaches(false);				
			servletConnection.setRequestProperty ("Content-Type", "application/x-java-serialized-object");	
			
		    OutputStream out = servletConnection.getOutputStream();				
			ObjectOutputStream oos;
			
			DataIn2 covD = new DataIn2(D, null, taskName, "cov");
			oos = new ObjectOutputStream(out);
			oos.writeObject(covD);
			oos.flush();
			oos.close();

//		    // read covariance matrix from server
			InputStream in = servletConnection.getInputStream();
			ObjectInputStream ois;
			ois = new ObjectInputStream(in);
			DataOut2 result = (DataOut2) ois.readObject();
			ois.close();
			
			if(result.getType().equals("cov")){
				cov_matrix = result.getMatrix();
			}
			else{
				System.out.println("covariance type needed");
			}
//----------------------------------------------------------------------------------------------------------------------
		    
		    System.out.println("Covariance matrix:");
		    cov_matrix.print(8, 6);

		    // compute SD matrix
		    SD = new Matrix(1, m);
		    for (i = 0; i < m; i++) {
			SD.set(0, i, Math.sqrt(cov_matrix.get(i,i)));
		    }

		    System.out.println("SD matrix:");
		    SD.print(8, 6);
		    

//-----------------------------------------------to calculate AUC-----------------------------------------------
		    Matrix scoreMatrix = X.times(beta1);
		    double[][] combine = new double[n][2];
		    for(i = 0; i< n; i++){
		    	combine[i][0] = (1/ (1 + Math.pow(Math.E, -1 * scoreMatrix.get(i, 0))));
		    	combine[i][1] = Y.get(i, 0);
		    }
		    Matrix aucData = new Matrix(combine);
//		    System.out.println("aucData: ");
//		    aucData.print(1, 1);
		    
		    url = new URL(root_property + "gloreserverservlet");
			servletConnection = url.openConnection();			
			servletConnection.setDoInput(true);
			servletConnection.setDoOutput(true);			
			servletConnection.setUseCaches(false);
			servletConnection.setDefaultUseCaches(false);				
			servletConnection.setRequestProperty ("Content-Type", "application/x-java-serialized-object");	
			
		    out = servletConnection.getOutputStream();
			
			DataIn2 aucD = new DataIn2(aucData, null, taskName, "auc");
			oos = new ObjectOutputStream(out);
			oos.writeObject(aucD);
			oos.flush();
			oos.close();

//		    // read covariance matrix from server
			in = servletConnection.getInputStream();
			ois = new ObjectInputStream(in);
			result = (DataOut2) ois.readObject();
			ois.close();
			
			if(result.getType().equals("auc")){
				aucValue = result.getMatrix();
			}
			else{
				System.out.println("auc type needed");
			}
			//System.out.println("end message send successfully0");
		    System.out.println("AUC Value:");
		    aucValue.print(8, 6);
		    //System.out.println("end message send successfully1");
		    //tell server that computation is stopped
		    
		    url = new URL(root_property + "gloreserverservlet");
			servletConnection = url.openConnection();			
			servletConnection.setDoInput(true);
			servletConnection.setDoOutput(true);			
			servletConnection.setUseCaches(false);
			servletConnection.setDefaultUseCaches(false);				
			servletConnection.setRequestProperty ("Content-Type", "application/x-java-serialized-object");	
			
			 out = servletConnection.getOutputStream();				
			System.out.println("start end message transmission");
			DataIn2 endTask = new DataIn2(new Matrix(1,1,1), null, taskName, "end");
			oos = new ObjectOutputStream(out);
			oos.writeObject(endTask);
			oos.flush();
			oos.close();
			
//		    // read end from server
			in = servletConnection.getInputStream();
			ois = new ObjectInputStream(in);
			result = (DataOut2) ois.readObject();
			ois.close();
			
			System.out.println("receive from end: " + result.getType());
		    //using js to store text
			
			
//		    Arrays.sort(combine, new ScoreComparator());
//		    int nt = 0;		//number of true items
//		    int nf = 0;		//number of false items
//		    int S = 0;		//the sequence sum of true items
//		    for(i = 0; i < combine.length; i++){
//		    	if(combine[i][1] > 0.5){
//		    		nt ++;
//		    		S = S + i;
//		    	}
//		    	else nf++;
//		    }
//		    System.out.println("S is: " + S);
//		    System.out.println("nt is: " + nt);
//		    System.out.println("nf is: " + nf);
//		    
//		    //AUC = (S - 1/2 * nt * (nt-1)) / (nt * nf)
//		    if(nt == 0.0 || nf == 0.0){
//		    	System.out.println("AUC is not suitable to this data");
//		    }
//		    else{
//			    double AUC = ((S - 0.5 * nt * (nt-1)) / (nt * nf));
//			    System.out.println("AUC is: " + AUC);
//		    }

		    
		}
		catch (Exception e) {
//		    System.out.println(e);
			e.printStackTrace();
//		    System.exit(-1);
		}
	}

	/**
	 * @param args
	 */
	 public static void main(String args[]) {

		    String file_name;
		 try{
		    // used for reading the data file
		    FileInputStream file_stream;
		    DataInputStream file_in;
		    BufferedReader file_br;
		    String file_line;
		    String[] line_tokens;
		    
		   // String taskName = "t1";

		    // data structures used to hold the client data read in from files
		    Vector< Vector<Double> > Xv = new Vector< Vector<Double> >();
		    Vector<Double> Yv = new Vector<Double>();
		    Vector<Double> xrow;

		    // data structures used to hold client data for matrix constructors
		    double[][] Xa;
		    double[] Ya;

		    // number of columns and rows in data file
		    int m = -1;
		    int n;

		    //double epsilon = Math.pow(10.0, -6.0);
		    Matrix beta0;
		    Matrix beta1;
		    Matrix hat_beta;
		    Matrix cov_matrix, SD;
		    Matrix X, Y;
		    Matrix P, W, D, E;	   

		    int i, j;

		    // count the number of iterations
		    int iter = 0;

//------------------ //check of filename will be in a different way jwc 10.9 -----------------------------------
//		    // missing filename as argument
		    file_name = "C:/Users/Wenchao/workspace/GLORE_Develop/ca_part2";	//This variable will tansmit from upper  jwc 10.9		
		    //file_name = dataPath;
		    System.out.println("Using data file '" + file_name + "'.");
//---------------------------------------------------------------------------------------------------------------------------------------

		    // access the file
		    file_stream = new FileInputStream(file_name);
		    file_in = new DataInputStream(file_stream);
		    file_br = new BufferedReader(new InputStreamReader(file_in));

		    // read file and populate X and Y matrices
		    n = 0;
		    //ignore the first line  jwc 10.26
		    file_br.readLine();
		    while ((file_line = file_br.readLine()) != null) {
			// update number of rows
			n = n + 1;
			line_tokens = file_line.split("\t");

			// detect number of columns in data file
			if (m == -1) {
			    m = line_tokens.length;
			}
			// line in data file does not match dimensions
			else if (m != line_tokens.length) {
			    System.out.println("ERROR: data file dimensions don't " +
					       "match on line " + n + ".");
			    System.exit(-1);
			}

			// populate data structures with data
			xrow = new Vector<Double>();
			xrow.add(1.0);
			for (i = 0; i < line_tokens.length - 1; i++) {
			    xrow.add(new Double(line_tokens[i]));
			}
			Xv.add(xrow);
			Yv.add(new Double(line_tokens[line_tokens.length-1]));
		    }

		    // close input stream
		    file_in.close();

		    beta0 = new Matrix(m, 1, -1.0);
		    beta1 = new Matrix(m, 1, 0.0);
		    cov_matrix = new Matrix(m, m);

		    // convert data into arrays to be passed to Matrix's constructor
		    Xa = two_dim_vec_to_arr(Xv);
		    Ya = one_dim_vec_to_arr(Yv);

		    // create X and Y matrices
		    X = new Matrix(Xa);
		    Y = new Matrix(Ya, Ya.length);
		    
		    //print X, Y, jwc 11.1
		    X.print(1, 1);
		    Y.print(1, 1);
		 }catch(Exception e){
			 e.printStackTrace();
		 }

	}

		    /* Returns the absolute maximum of the elements in the two dimensional
		       array matrix. */
		    public static double max_abs(double[][] matrix) {
			int i,j;
			boolean set = false;
			double max = 0;

			// iterate through matrix
			for (i = 0; i < matrix.length; i++) {
			    for (j = 0; j < matrix[i].length; j++) {

				// maintain absolute max number found
				if (!set) {
				    max = Math.abs(matrix[i][j]);
				    set = true;
				}
				else if (Math.abs(matrix[i][j]) > max) {
				    max = Math.abs(matrix[i][j]);
				}
			    }
			}

			return max;
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

		    /* Set each element of the 2D double array to e^a where a is the value of
		       an element. */
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

		    /* Given an array of length n, returns an n by n matrix M where
		       M[i][j] = A[i] if i = j and 0 otherwise. */
		    public static Matrix diag(double[] A) {
			int n = A.length;
			int i;

			Matrix M = new Matrix(n, n, 0.0);
			for (i = 0; i < n; i++) {
			    M.set(i,i,A[i]);
			}
			return M;
		    }
		}

	class ScoreComparator implements Comparator{
		 public ScoreComparator (){
		 }
		@Override
		public int compare(Object a, Object b) {
			// TODO Auto-generated method stub
			if(((double[]) a)[0] > ((double[]) b)[0]) return 1;
			else return 0;
		}
		 
	}