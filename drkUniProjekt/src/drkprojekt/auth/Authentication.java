package drkprojekt.auth;


import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import drkprojekt.database.DatabaseHandler;
import drkprojekt.rest.Helper;

/**
 * Servlet implementation class backend
 */
@WebServlet("/authentication/*")
public class Authentication extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Authentication() {
        super();  
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		DatabaseHandler db	= DatabaseHandler.getdb();
	    JSONObject json;
	    JSONArray array;
	    try 
	    {
			json = Helper.getRequestJSON(request);	    
			array = db.executeQuery(
			    "Select login_id, userpassword, displayname, adminrole FROM user WHERE login_id = ?", (String)json.get("login_id"));
			
			if(array.isEmpty() || !json.get("password").equals(((JSONObject)array.get(0)).get("userpassword"))) 
			{
				response.setStatus(403);
				System.out.println("Invalid login attempt for User: " + (String)json.get("login_id"));
			    JSONObject responseText = new JSONObject();
			    responseText.put("successful", false);
			    Helper.setResponseJSON(response, responseText);
			    return;
			}
			String[] tmp	= {(String) json.get("login_id"), (String) json.get("device_id")};
			db.executeUpdate("INSERT INTO phonegapid (registredUser, device_id, registertime) VALUES(?,?,CURRENT_TIMESTAMP)", tmp);
			
			JSONObject responseText = new JSONObject();		
			responseText.put("successful", true);
			responseText.put("token", 
				AuthHelper.createJsonWebToken((String)json.get("login_id"), (String) json.get("displayname"), 
					Boolean.parseBoolean((String) json.get("adminrole")), (long) 10000));
			
			Helper.setResponseJSON(response, responseText);		
	    } 
	    catch (SQLException | ParseException e)
	    {
	    	Helper.handleException(e, response);		
	    }
	    
	    JSONObject responseText = new JSONObject();
		
		Helper.setResponseJSON(response, responseText);
	}
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	    JSONObject json;
		try {
		    json = Helper.getRequestJSON(request);
		    DatabaseHandler.getdb().executeUpdate("DELETE FROM phonegapid WHERE device_id=" + (String)json.get("device_id") 
		    	+ " AND registereduser=" + (String)json.get("login_id"));
			    
		} catch (ParseException | SQLException e) {
		    Helper.handleException(e, response);
		} 
	   
			 
	}
}
