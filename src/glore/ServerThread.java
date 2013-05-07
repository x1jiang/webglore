package glore;


//import java.io.BufferedReader;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.FileInputStream;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
//import java.net.ServerSocket;
//import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
//import java.sql.ResultSet;
import java.sql.Statement;
//import java.util.Collections;
import java.util.Comparator;
//import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Properties;

//import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Jama.Matrix;

public class ServerThread {

	  // socket connection and thread id
    private int m_thread_id;
    private String taskName = null;
    private boolean beta_comp_finish;
    private boolean cov_comp_finish;
    private boolean auc_comp_finish;
	private String dbconnection_property = null;
	private String dbusername_property = null;
	private String dbpassword_property = null;
	private String root_property = null;
	private String outAddress = null;
    
	// number of participating clients
     int num_clients;
     int maxIteration;
    // memory used for storing client data
    static Vector<Matrix> D;
    static Vector<Matrix> covD;
    static Vector<Matrix> aucD;
    static Vector<Matrix> E;
    // semaphore used to ensure all clients send data before computation occurs
    static Semaphore data_lock;
    // semaphore used to ensure all client threads wait before sending beta1
    static Semaphore beta_lock;
    // semaphore used to ensure clients send var. cov. matrices
    static Semaphore var_cov_lock;
    // semaphore used to ensure client threads wait for var. cov. matrix comp.
    static Semaphore var_cov_comp_lock;
    //semaphore used to ensure clients send data needed to compute AUC jwc 11.1
    static Semaphore var_auc_lock;
    //semaphore used to wait for AUC comp jwc 11.1
    static Semaphore var_auc_comp_lock;
    //semaphore used to ensure clients send task end status lph 11.15
    static Semaphore task_end_status;
    
    // number of features in the data
    static int m;
    //static double epsilon = Math.pow(10.0, -6.0);
    double epsilon;
    static Matrix beta0, beta1, cov_matrix, AUC;
    // count the number of iterations
    static int iter;
    

    /**
     * calculate beta and cov
     * @author Wenchao
     *
     */
    private class BetaComputation implements Runnable {
	public void run() {
	    try {
		Matrix temp_a, temp_b, temp_c;
		Matrix SD;
		
		System.out.println("Computation started");
		// iteratively update beta1
		while (max_abs((beta1.minus(beta0)).getArray()) > epsilon) {
		    // if (iter == 2)
		    // 	break;
		     if (iter == maxIteration)//added by lph
		     {
		    	 System.out.println("Has reached the maximum iteration number!");
		    	 break; 
		     }	
//		    System.out.println("value: " + max_abs((beta1.minus(beta0))
//							   .getArray()));
		    
		    // wait for all clients to write data
		    for (int i = 0; i < num_clients; i++) {
			data_lock.acquire();
//			System.out.println("Comp: One data lock acquired");
		    }
//		    System.out.println("comp: client data available for " +
//				       "iter " + iter);

		    System.out.println("Iteration " + iter);

		    beta0 = beta1.copy();

		    /* beta1<-beta0+solve(rowSums(d,dims=2)+
		       diag(0.0000001,m))%*%(rowSums(e,dims=2)) */
		    temp_a = row_sums(E);
		    temp_b = row_sums(D);
		    temp_c = diag(0.0000001, m);

		    temp_b = temp_b.plus(temp_c);
		    temp_b = temp_b.inverse();
		    temp_b = temp_b.times(temp_a);
		    beta1 = beta0.plus(temp_b);
		    System.out.println("beta1 is :");
		    beta1.print(10,12);
		
//		    System.out.println("comp: releasing beta1 lock for iter " +
//				       iter);
		    // indicate to threads that beta1 is available
		    for (int i = 0; i < num_clients; i++) {
			beta_lock.release();
		    }
		    
		    iter = iter + 1;
		}

//		System.out.println("value on exit: " + max_abs((beta1
//		        .minus(beta0)).getArray()));
		//Computation finish, set some variable to initial state, set finish flag
//		System.out.println("Beta Computation thread exiting.");
		beta_comp_finish = true;
		
	    //write beta to taskName_varOutput.txt jwc 11.9
	    File varOutput = new File(outAddress + taskName + "_varOutput.txt");
//		System.out.println("The addresss of glore server servlet is : " + (new File("")).getAbsolutePath());
		FileWriter fw = new FileWriter(varOutput, true);
		for(int i=0; i<beta1.getRowDimension() -1; i++){
			fw.write(beta1.get( i, 0) + ", ");
		}
		fw.write(beta1.get( (beta1.getRowDimension() -1), 0) +"");
		fw.write("\n");
		fw.close();
		
		//write beta to DB
		String betaString = "";
		if(beta1.getRowDimension() > 0){
			for(int i=0; i<beta1.getRowDimension() - 1; i++){
				betaString = betaString + beta1.get(i, 0) + "#";
			}
			betaString = betaString + beta1.get(beta1.getRowDimension() - 1, 0);
		}
		
	    Connection conn = DriverManager.getConnection(dbconnection_property, dbusername_property, dbpassword_property);
	    Statement stat = conn.createStatement();
	    String sql = "insert into tempresult (taskname, beta) values ('" + taskName + "', '" + betaString + "')" + ";";
	    stat.execute(sql);
		
	    }
	    catch (Exception e) {
//		System.out.println(e);
	    e.printStackTrace();
//		System.exit(-1);
	    }
	}
	
	/* Return a one dimensional array that is the sum of the E.length
	   one dimensional vectors. */
	private Matrix row_sums(Vector<Matrix> D){
	    Matrix sums;
	    sums = new Matrix(D.get(0).getRowDimension(), D.get(0).getColumnDimension(), 0);
	    for(int i=0; i<D.size(); i++){
	    	sums = sums.plus(D.get(i));
	    }
	    return sums;
	}

	/* Returns an n by n matrix where the diagonal entries are v and the
	   other entries are 0 */
	private Matrix diag(double v, int n) {
	    int i;
	    double[][] A = new double[n][n];
	    for (i = 0; i < n; i++) {
		A[i][i] = v;
	    }
	    return new Matrix(A);
	}
    }
    private class CovComputation implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				Matrix temp_a, temp_b, temp_c;
				Matrix SD;
				// wait for clients to send D matrices
				for (int i = 0; i < num_clients; i++) {
				    var_cov_lock.acquire();
				}
	
				// compute covariance matrix
				temp_b = row_sums(covD);	
				temp_c = diag(0.0000001, m);
	
				temp_b = temp_b.plus(temp_c);
				cov_matrix = temp_b.inverse();
	
				System.out.println("Covariance matrix:");
				cov_matrix.print(8,6);
	
				// compute SD matrix
				SD = new Matrix(1, m);
				for (int i = 0; i < m; i++) {
				    SD.set(0,i, Math.sqrt(cov_matrix.get(i,i)));
				}
	
				System.out.println("SD matrix:");
				SD.print(8,6);
	
				// signal client threads that the SD matrix is ready
				for (int i = 0; i < num_clients; i++) {
				    var_cov_comp_lock.release();
				}
				System.out.println("Cov Computation thread exiting.");
				//set finish flag
				cov_comp_finish = true;
				
			    //write beta to taskName_varOutput.txt jwc 11.9
			    File varOutput = new File(outAddress + taskName + "_varOutput.txt");
				FileWriter fw = new FileWriter(varOutput, true);
				for(int i=0; i<cov_matrix.getRowDimension(); i++){
					for(int j=0; j<cov_matrix.getColumnDimension() -1; j++){
						fw.write(cov_matrix.get(i, j) + ", ");
					}
					fw.write(cov_matrix.get(i, (cov_matrix.getColumnDimension() -1)) +"");
					fw.write("\n");
				}
				fw.close();
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		private Matrix row_sums(Vector<Matrix> covD){
		    Matrix sums;
		    sums = new Matrix(covD.get(0).getRowDimension(), covD.get(0).getColumnDimension(), 0);
		    for(int i=0; i<covD.size(); i++){
		    	sums = sums.plus(covD.get(i));
		    }
		    return sums;
		}

		/* Returns an n by n matrix where the diagonal entries are v and the
		   other entries are 0 */
		private Matrix diag(double v, int n) {
		    int i;
		    double[][] A = new double[n][n];
		    for (i = 0; i < n; i++) {
			A[i][i] = v;
		    }
		    return new Matrix(A);
		}
    }
    private class AUCComputation implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{

				Matrix temp_a, temp_b, temp_c;
				double[] row ;
				Matrix SD;
				// wait for clients to send D matrices
				for (int i = 0; i < num_clients; i++) {
				    var_auc_lock.acquire();
				}
	
				//combine all the auc data together
				Vector<double[]> aucDataCombine = new Vector<double[]>();
				for(int i=0; i< aucD.size(); i++){
					for(int j=0; j < aucD.get(i).getRowDimension(); j++){
						row = new double[2];
						row[0] = aucD.get(i).get(j, 0);
						row[1] = aucD.get(i).get(j, 1);
						aucDataCombine.add(row);
					}
				}

				//sort data according to the first column: X * beta
				//Collections.sort(aucDataCombine, new ScoreComparator());
				sort(aucDataCombine);
			
				//print aucDataCombine
				FileWriter fstream = new FileWriter(outAddress + taskName + "_Output.txt");
				BufferedWriter out = new BufferedWriter(fstream);
				for(int i=0; i< aucDataCombine.size(); i++){
					//System.out.println(aucDataCombine.get(i)[0] + "\t" +  aucDataCombine.get(i)[1]);
					out.write(aucDataCombine.get(i)[0] + "\t" +  aucDataCombine.get(i)[1]);
					out.write("\n");
				}
				out.close();
				
				//compute AUC by formula
			    int nt = 0;		//number of true items
			    int nf = 0;		//number of false items
			    int S = 0;		//the sequence sum of true items
			    for(int i = 0; i < aucDataCombine.size(); i++){
			    	if(aucDataCombine.get(i)[1] > 0.5){
			    		nt ++;
			    		S = S + i;
			    	}
			    	else nf++;
			    }
			    System.out.println("S is: " + S);
			    System.out.println("nt is: " + nt);
			    System.out.println("nf is: " + nf);
			    
			    //AUC = (S - 1/2 * nt * (nt-1)) / (nt * nf)
			    if(nt == 0.0 || nf == 0.0){
			    	System.out.println("AUC is not suitable to this data");
			    }
			    else{
				    AUC.set(0, 0, ((S - 0.5 * nt * (nt-1)) / (nt * nf))) ;
				    System.out.println("AUC is: " + AUC.get(0, 0));
			    }
			    
	
				// signal client threads that the SD matrix is ready
				for (int i = 0; i < num_clients; i++) {
				    var_auc_comp_lock.release();
				}
				System.out.println("AUC Computation thread exiting.");
				//set finish flag
				auc_comp_finish = true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	    private void sort(Vector<double[]> v){
	    	for(int i=0; i<v.size(); i++){
	    		for(int j=i; j< v.size(); j++){
	    			if(v.get(i)[0] > v.get(j)[0]){
	    				//change
	    				double temp1 = v.get(i)[0];
	    				double temp2 = v.get(i)[1];
	    				v.get(i)[0] = v.get(j)[0];
	    				v.get(i)[1] = v.get(j)[1];
	    				v.get(j)[0] = temp1;
	    				v.get(j)[1] = temp2;
	    			}
	    		}
	    	}
	    }
    }
	private class ScoreComparator implements Comparator{
		 public ScoreComparator (){
		 }
		@Override
		public int compare(Object a, Object b) {
			// TODO Auto-generated method stub
			if(((double[]) a)[0] > ((double[]) b)[0]) return 1;
			else return 0;
		}
		 
	}
    public void addCovData(DataIn2 dataIn, HttpServletResponse res){
    	try{
    		OutputStream out = res.getOutputStream();
    		ObjectOutputStream oos = new ObjectOutputStream(out);
    		covD.add(dataIn.getD());
    	    // signal the computation thread: this client's data has arrived
    	    var_cov_lock.release();
    	
    	    // wait for computation thread to finish computing SD matrix
    	    var_cov_comp_lock.acquire();
    	
    	//-----------------send covariance by matrix to client jwc 10.17---------------------
    	    // send covariance matrix to client
    	    oos.writeObject(new DataOut2(cov_matrix, "cov", dataIn.getTaskName()));
    	    oos.flush();
    	    oos.close();
    	//--------------------------------------------------------------------------------------	
    	    covD.clear();
    	    System.out.println("Covariance transmission finish");
    	    
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void addAUCData(DataIn2 dataIn, HttpServletResponse res){
    	try{
    		OutputStream out = res.getOutputStream();
    		ObjectOutputStream oos = new ObjectOutputStream(out);
    		aucD.add(dataIn.getD());
    	    // signal the computation thread: this client's data has arrived
    	    var_auc_lock.release();
    	
    	    // wait for computation thread to finish computing SD matrix
    	    var_auc_comp_lock.acquire();
    	
    	//-----------------send covariance by matrix to client jwc 10.17---------------------
    	    // send covariance matrix to client
    	    oos.writeObject(new DataOut2(AUC, "auc", dataIn.getTaskName()));
    	    oos.flush();
    	    oos.close();
    	//--------------------------------------------------------------------------------------	
    	    aucD.clear();
    	    System.out.println("AUC transmission finish");
    	    
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }

    public void addBetaData(DataIn2 dataIn, HttpServletResponse res){

    	try {
    	    int i, j;
 //-------------define the output to clients jwc 10.17-----------------------
    		OutputStream out = res.getOutputStream();
    		ObjectOutputStream oos = new ObjectOutputStream(out);

//    		oos.flush();
//    		oos.close();
 //--------------------------------------------------------------------------
    	    // read data from clients and send beta to clients
//    	    while (max_abs((beta1.minus(beta0)).getArray()) > epsilon) {
    		if (max_abs((beta1.minus(beta0)).getArray()) > epsilon) {		//jwc 10.18
	    		// if (iter == 2)
	    		//     break;
	//-----------------read by matrix jwc 10.17-----------------------------
	//    		// read D from client
	//    		for (i = 0; i < m; i++) {
	//    		    for (j = 0; j < m; j++) {
	//    			D[m_thread_id][i][j] = m_in.readDouble();
	//    		    }
	//    		}
	//    		// read E from client
	//    		for (i = 0; i < m; i++) {
	//    		    E[m_thread_id][i] = m_in.readDouble();
	//    		}
	    	    D.add(dataIn.getD());
	    	    E.add(dataIn.getE());
	//-----------------------------------------------------------------------------
	    		// release lock, indicating data is ready from this thread
	    		System.out.println(m_thread_id + ": releasing " +
	    				   "data lock for iter " + iter);
	    		data_lock.release();
	    		// wait for computation thread to finish computing beta1
	    		beta_lock.acquire();
	    		System.out.println(m_thread_id + ": sending beta1 " +
	    				   "for iter " + iter);
	
//--------------------------send by object and in type dataOut2 jwc 10.17-----------	
//	    		// send beta1 to clients
//	    		for (i = 0; i < m; i++) {
//	    		    m_out.writeDouble(beta1.get(i,0));
//	    		}
	    		
	    		oos.writeObject(new DataOut2(beta1, "beta", dataIn.getTaskName()));
	    		oos.flush();
	    		oos.close();
//----------------------------------------------------------------------------------
//	    		iter = iter + 1;
    	    }
    	    
    	    //D must be cleared after beta computation jwc 10.17
    	    D.clear();
    	    E.clear();
    	    System.out.println("Beta transmission finish iteration " + iter);
//    	    System.out.println("Thread " + m_thread_id + " exiting.");
    	}
    	catch (Exception e) {
//    	    System.out.println(e);
//    	    System.exit(-1);
    		e.printStackTrace();
    	}
    }
    public static void main(String[] args) {
    }

    ServerThread(DataIn2 dataIn, int numClient, int maxIteration, double epsilon, int featureNum, Properties confProperties){
    	this.taskName = dataIn.getTaskName();
    	this.num_clients = numClient;
    	this.epsilon= epsilon;
    	this.maxIteration=maxIteration;
    	
		dbconnection_property = confProperties.getProperty("dbconnection");
		dbusername_property = confProperties.getProperty("dbusername");
		dbpassword_property = confProperties.getProperty("dbpassword");
		root_property = confProperties.getProperty("root");
		this.outAddress = confProperties.getProperty("outAddress");

        beta_comp_finish = false;
        cov_comp_finish = false;
        auc_comp_finish = false;
    	iter = 0;
	    m = featureNum;	//feature
    	try {
	    int i;
	    // allocate memory for the client's data
	    D = new Vector<Matrix>();
	    covD = new Vector<Matrix>();
	    aucD = new Vector<Matrix>();
	    E = new Vector<Matrix>();
	    
		// init beta variable
		beta0 = new Matrix(m, 1, -1.0);
		beta1 = new Matrix(m, 1, 0.0);
	    
		// init auc variable
		AUC = new Matrix(1, 1, 0.0);
		
		//initiate iteration
		iter = 0;

	    // init data semaphore used to ensure all client data has arrived
	    data_lock = new Semaphore(num_clients);
	    for (i = 0; i < num_clients; i++) {
		data_lock.acquire();
	    }

	    /* init beta semaphore to block threads from sending beta before
	       the computation thread finishes computing it */
	    beta_lock = new Semaphore(num_clients);
	    for (i = 0; i < num_clients; i++) {
		beta_lock.acquire();
	    }
	    

	    /* init variance covariance semaphore used to ensure variance
	       covariance matrices have arrived from clients */
	    var_cov_lock = new Semaphore(num_clients);
	    for (i = 0; i < num_clients; i++) {
		var_cov_lock.acquire();
	    }

	    /* init variance covariance computation semaphore to block threads
	       from sending matrix until computation thread is done */
	    var_cov_comp_lock = new Semaphore(num_clients);
	    for (i = 0; i < num_clients; i++) {
		var_cov_comp_lock.acquire();
	    }


	    /*init AUC semaphore to block threads from sending AUC data jwc 11.1*/
	    var_auc_lock = new Semaphore(num_clients);
	    for (i = 0; i < num_clients; i++) {
	    	var_auc_lock.acquire();
	    }

	    /* init variance covariance computation semaphore to block threads
	       from sending matrix until computation thread is done */
	    var_auc_comp_lock = new Semaphore(num_clients);
	    for (i = 0; i < num_clients; i++) {
		var_auc_comp_lock.acquire();
	    }
	    
	    /*init task status semaphore to block threads from sending task end status  lph 11*15*/
/*	    task_end_status=new Semaphore(num_clients);
	    for(i=0;i<num_clients;i++){
	    	task_end_status.acquire();
	    }*/
	    
	    // spawn computational thread
	    (new Thread(new BetaComputation())).start();
	    (new Thread(new CovComputation())).start();
	    (new Thread(new AUCComputation())).start();
	
	    System.out.println("The address of server thread is:" + new File("").getAbsolutePath());
	    System.out.println("Main thread exiting.");
	}
	catch(Exception e) {
//	    System.out.println(e);
//	    System.exit(-1);
		e.printStackTrace();
	}
    }
    public String getTaskName() {
		return taskName;
	}
    public boolean isBetaFinish(){
    	return beta_comp_finish;
    }
    public boolean isCovFinish(){
    	return cov_comp_finish;
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

	public boolean isAUCFinish() {
		// TODO Auto-generated method stub
		return auc_comp_finish;
	}
}