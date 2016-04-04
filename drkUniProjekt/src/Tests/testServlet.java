package Tests;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drkprojekt.database.DatabaseHandler;
import drkprojekt.rest.Helper;

/**
 * Servlet implementation class testServlet
 */
@WebServlet("/test/*")
public class testServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log	= LoggerFactory.getLogger(testServlet.class);
	private Helper helper;   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public testServlet() {
        super();
        helper	= new Helper();
      
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//String test			= DatabaseHandler.getdb().test();
		//JSONObject answer	= new JSONObject();
		//answer.put("Test_Output", test);		
		try 
		{
			log.debug("Test Servlet called: \n {}");
			if(Helper.getSubResource(request, true) == null)
			{
				Helper.setResponseJSONArray(response, DatabaseHandler.getdb().executeQuery("SELECT * FROM PHONEGAPID"));
			}else
			{
				Helper.setResponseJSONArray(response, DatabaseHandler.getdb().executeQuery("SELECT * FROM PHONEGAPID WHERE REGISTEREDUSER = ?", Helper.getSubResource(request, true)));
			}
			
		} catch (SQLException e) {
			JSONObject eo	= new JSONObject();
			eo.put("Error", e);
			helper.setResponseJSON(response, eo);
			e.printStackTrace();
		}
	}
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
	{
		try {
			DatabaseHandler.getdb().executeUpdate("DELETE FROM PHONEGAPID");
		} catch (SQLException e) {
			log.error("Error while deleteing: {}", e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
//Test Git
}
