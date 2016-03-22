package drkprojekt.auth;


import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.jsontoken.crypto.Signer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import drkprojekt.database.DatabaseHandler;
import drkprojekt.rest.Helper;

/**
 * Servlet implementation class backend
 */
@WebServlet("/authen/*")
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
	    System.out.println("Doingöö Post");
	    TokenInfo info = AuthHelper.verifyToken("eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJEUktVbmlQcm9qZWt0IiwiYXVkIjoiQ2xpZW50Rm9yRFJLTWVtYmVyIiwiaWF0IjoxNDU4NjQwMjQyLCJleHAiOjIzMjI2NDAyNDIsImluZm8iOnsidXNlcklkIjoiVXNlcjEiLCJ1c2VyTmFtZSI6IlN1c2kgU29yZ2xvcyIsInVzZXJSb2xlIjoidGVhbU1lbWJlciJ9fQ.u9xZz-JIrOJeKWVKFz5J7WNA0GShvC57zy68KkoP28E");
	    System.out.println(info.getUserId());
	    System.out.println("AuthHelper aufgerufen");
	    
	    DatabaseHandler db = DatabaseHandler.getdb();
	    JSONObject json;
	    JSONArray array;
	    try {
		json = Helper.getRequestJSON(request);	 
		System.out.println("Get Request JSON");
		array = DatabaseHandler.getdb().executeQuery(
			"Select login_id, userpassword, displayname, adminrole FROM user WHERE login_id = ?", (String)json.get("login_id"));
		System.out.println("Get database array");
		if(array.isEmpty() || !json.get("password").equals(((JSONObject)array.get(0)).get("userpassword"))) {
		    System.out.println("Password or username wrong");
		    JSONObject responseText = new JSONObject();
		    responseText.put("successful", false);
		    Helper.setResponseJSON(response, responseText);
		    return;
		}
		String[] tmp	= {(String) json.get("login_id"), (String) json.get("device_id")};
		db.executeUpdate("INSERT INTO phonegapid (registeredUser, device_id, registertime) VALUES(?,?,CURRENT_TIMESTAMP)", tmp);
		System.out.println("executeupdate succesful");
		JSONObject responseText = new JSONObject();		
		responseText.put("successful", true);
		String loginID = (String)json.get("login_id");
		String displayName = (String) json.get("displayname");
		boolean admin = Boolean.parseBoolean((String) json.get("adminrole"));
		System.out.println("LoginID: " + loginID);
		System.out.println("NAme: " + displayName);
		System.out.println("Admin: " + admin);
		String tokenString = AuthHelper.createJsonWebToken(loginID, displayName, admin, (long) 10000);
		responseText.put("token", tokenString);
		System.out.println("creates web token");
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
