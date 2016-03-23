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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{    
	    DatabaseHandler db = DatabaseHandler.getdb();
	    JSONObject json;
	    JSONArray array;
	    try {
		json = Helper.getRequestJSON(request);
		String[] tmp1	= {(String) json.get("login_id"), (String) json.get("password")};
		array = DatabaseHandler.getdb().executeQuery(
			"Select login_id, displayname, adminrole FROM user WHERE login_id = ? AND userpassword = HASH_SHA256(TO_BINARY(?))", tmp1);
		if(array.isEmpty()) {
		    JSONObject responseText = new JSONObject();
		    responseText.put("successful", false);
		    Helper.setResponseJSON(response, responseText);
		    return;
		}
		String[] tmp2	= {(String) json.get("login_id"), (String) json.get("device_id")};
		db.executeUpdate("INSERT INTO phonegapid (registeredUser, device_id, registertime) VALUES(?,?,CURRENT_TIMESTAMP)", tmp2);
		JSONObject responseText = new JSONObject();		
		responseText.put("successful", true);
		
		String loginID = (String)json.get("login_id");
		String displayName = (String) json.get("displayname");
		boolean admin = Boolean.parseBoolean((String) json.get("adminrole"));
		String tokenString = AuthHelper.createJsonWebToken(loginID, displayName, admin, (long) 10000);
		responseText.put("token", tokenString);

		Helper.setResponseJSON(response, responseText);		
	    } catch (SQLException | ParseException e) {
		Helper.handleException(e, response);		
	    }

	}
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
	    JSONObject json;
		try {
		    json = Helper.getRequestJSON(request);
		    String[] tmp = {(String) json.get("device_id"), (String) json.get("login_id")};
		    DatabaseHandler.getdb().executeUpdate("DELETE FROM phonegapid WHERE device_id= ? "
		    	+ " AND registereduser= ? ", tmp);
			    
		} catch (ParseException | SQLException e) {
		    Helper.handleException(e, response);
		} 
	   
			 
	}
}
