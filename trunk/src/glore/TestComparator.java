package glore;

import java.sql.*;
import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class TestComparator extends HttpServlet{
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		Vector<double[]> aucDataCombine = new Vector<double[]>();
		aucDataCombine.add(new double[]{1.1, 1});
		aucDataCombine.add(new double[]{1.0, 0});
		aucDataCombine.add(new double[]{0.9, 1});
		aucDataCombine.add(new double[]{0.8, 0});
		aucDataCombine.add(new double[]{1.2, 1});
		
		Collections.sort(aucDataCombine, new ScoreComparator());
		
		for(int i=0; i<aucDataCombine.size(); i++){
			System.out.println(aucDataCombine.get(i)[0] + "\t" + aucDataCombine.get(i)[1]);
		}
	}
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		Vector<double[]> aucDataCombine = new Vector<double[]>();
		aucDataCombine.add(new double[]{1.1, 1});
		aucDataCombine.add(new double[]{1.0, 0});
		aucDataCombine.add(new double[]{0.9, 1});
		aucDataCombine.add(new double[]{0.8, 0});
		aucDataCombine.add(new double[]{1.2, 1});
		
		Collections.sort(aucDataCombine, new ScoreComparator());
		
		for(int i=0; i<aucDataCombine.size(); i++){
			System.out.println(aucDataCombine.get(i)[0] + "\t" + aucDataCombine.get(i)[1]);
		}
	}

}

class ScoreComparator2 implements Comparator{
	 public ScoreComparator2 (){
	 }
	@Override
	public int compare(Object a, Object b) {
		// TODO Auto-generated method stub
		if(((double[]) a)[0] > ((double[]) b)[0]) return 1;
		else return 0;
	}
	 
}