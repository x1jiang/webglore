package glore;
import java.sql.*;
import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * This class is similar to ClientServlet.java in ppsvm
 * This servlet receive username and passwd as imput, compare with items in table registeduser, return DataOut as response.
 * @author Wenchao
 *
 */
public class GetTaskInfo extends HttpServlet{

	private String dbconnection_property = null;
	private String dbusername_property = null;
	private String dbpassword_property = null;
	private String root_property = null;
	/**
	 * read parameters from file config.properties to initialize db: 
	 * dbconnection;
	 * dbusername;
	 * dbpassword;
	 */
	public void init(ServletConfig conf) throws ServletException {
		File f = new File("config.properties");
		Properties properties = new Properties();
		try{
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/config.properties");
			properties.load(is);
			properties.list(System.out);
			dbconnection_property = properties.getProperty("dbconnection");
			dbusername_property = properties.getProperty("dbusername");
			dbpassword_property = properties.getProperty("dbpassword");
			root_property = properties.getProperty("root");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		System.out.println("In the GetTaskInfo servlet");
		String initEmail=(String)req.getSession().getAttribute("ownerEmail");
		String taskName = (String)req.getSession().getAttribute("taskName");
		String err = "";
		String anonymous = "TRUE";
//		if(email.equals("anonymous")){
//			/* set flag to anonymous (forbid change) and */
//			System.out.println("This is an anonymous user");
//		}
//		else{
//			anonymous = "FALSE";
//			System.out.println("The email of the user is: " + email);
//			String sql = "" ;
//			try{
////				Class.forName("com.mysql.jdbc.Driver");					
////				Connection conn = DriverManager.getConnection(dbconnection_property, dbusername_property, dbpassword_property);
////				Statement stat = conn.createStatement();
////				sql = "select name, password from registeduser where email = '" + email +"';";
////				ResultSet rs = stat.executeQuery(sql);//
////				if(rs.next()) {
////					name = rs.getString("name").split("#");
////					password = rs.getString("password");
////					err = "get profile success";
////				}else{
////					throw new UserNotRegistException("This user is not registed");
////				}
////				//close database
////				if(stat!=null){
////					stat.close();
////				}
////				if(rs!=null){
////					rs.close();
////				}
////				if(conn!=null){
////					conn.close();
////				}
//			}catch(SQLException e){
//				e.printStackTrace();
//				err += e.getMessage();
//				req.getSession().setAttribute("error", err);
//			}catch(ClassNotFoundException e){
//				err += e.getMessage();
//				req.getSession().setAttribute("error", err);
//			}catch(UserNotRegistException e){
//				err += e.getMessage();
//				anonymous = "TRUE";
//				req.getSession().setAttribute("error", err);
//			}finally{
				res.setContentType("text/html");
				res.getWriter().write("[{taskName:'" + taskName + "', initEmail:'" + initEmail + "'}]");
//			}
//		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		doGet(req, res);
	}
}
