package rest;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import Database.DatabaseHandler;

/**
 * Servlet implementation class testServlet
 */
@WebServlet("/test/*")
public class testServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
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
		try {
			helper.setResponseJSONArray(response, DatabaseHandler.getdb().executeQuery("SELECT * FROM TESTTABELLE"));
		} catch (SQLException e) {
			JSONObject eo	= new JSONObject();
			eo.put("Error", e);
			helper.setResponseJSON(response, eo);
			e.printStackTrace();
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