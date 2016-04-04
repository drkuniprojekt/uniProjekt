package drkprojekt.auth;

import java.io.IOException;
import java.security.SignatureException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drkprojekt.chat.ClientFactory;
import drkprojekt.database.DatabaseHandler;
import drkprojekt.rest.Helper;

/**
 * Servlet implementation class backend
 */
@WebServlet("/authentication/*")
public class AuthenticationProcessor extends HttpServlet {
	private Logger log	= LoggerFactory.getLogger(AuthenticationProcessor.class);

	//OINFDS
    private static final long serialVersionUID = 1L;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AuthenticationProcessor()
    {
    	super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	DatabaseHandler db = DatabaseHandler.getdb();
	JSONObject json;
	JSONArray array;
	try {
	    json = Helper.getRequestJSON(request);
	    String login_id = (String) json.get("login_id");
	    String[] tmp1 = { login_id , (String) json.get("password") };
	    array = DatabaseHandler.getdb().executeQuery(
		    "Select login_id, displayname, adminrole FROM user WHERE login_id = ? AND userpassword = HASH_SHA256(TO_BINARY(?))",
		    tmp1);
	    if (array.isEmpty()) {
		JSONObject responseText = new JSONObject();
		responseText.put("successful", false);
		Helper.setResponseJSON(response, responseText);
		return;
	    }
	    String device_id = (String) json.get("device_id");
	    //TODO: If phonegap. Else what?
	    if (device_id != null) {
		log.debug("device_id: " + device_id);
		
		
		int check_pid	= db.executeQuery("SELECT * FROM phonegapid WHERE device_id = ?", device_id).size();
		log.debug("Check_pid" + check_pid);
			if(check_pid < 1)
			{
				String[] tmp2 = { login_id, device_id };
				ClientFactory.getClient(login_id).addPhonegap_id(device_id);
				db.executeUpdate(
						"INSERT INTO phonegapid (registeredUser, device_id, registertime) VALUES(?,?,CURRENT_TIMESTAMP)",
						tmp2);
			}
		
	    }else{
		log.debug("device_id is null");
	    }

	    JSONObject responseText = new JSONObject();
	    JSONObject dbJSON = (JSONObject) array.get(0);
	    
	    String displayName = (String) dbJSON.get("displayname");
	    boolean admin = Boolean.parseBoolean(dbJSON.get("adminrole").toString());

	    String tokenString = AuthHelper.createJsonWebToken(login_id, displayName, admin, (long) 10000);
	    responseText.put("successful", true);
	    responseText.put("token", tokenString);
	    responseText.put("adminrole", admin);

	    Helper.setResponseJSON(response, responseText);
	} catch (SQLException | ParseException | SignatureException e) {
	    Helper.handleException(e, response);
	}

    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	JSONObject json;
	try {
	    json = Helper.getRequestJSON(request);
	    String[] tmp = { (String) json.get("device_id"), (String) json.get("login_id") };
	    DatabaseHandler.getdb().executeUpdate("DELETE FROM phonegapid WHERE device_id= ? " +
		    " AND registereduser= ? ", tmp);

	} catch (ParseException | SQLException e) {
	    Helper.handleException(e, response);
	}

    }
}
