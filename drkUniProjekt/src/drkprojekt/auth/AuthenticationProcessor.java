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

import drkprojekt.database.DatabaseHandler;
import drkprojekt.rest.Helper;

/**
 * Servlet implementation class backend
 */
@WebServlet("/authentication/*")
public class AuthenticationProcessor extends HttpServlet {

	//OINFDS
    private static final long serialVersionUID = 1L;
    private static Logger log;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AuthenticationProcessor()
    {
    	super();
    	log = LoggerFactory.getLogger(this.getClass());
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
	    String[] tmp1 = { (String) json.get("login_id"), (String) json.get("password") };
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
		String[] tmp2 = { (String) json.get("login_id"), device_id };
		db.executeUpdate(
			"INSERT INTO phonegapid (registeredUser, device_id, registertime) VALUES(?,?,CURRENT_TIMESTAMP)",
			tmp2);
	    }

	    JSONObject responseText = new JSONObject();
	    JSONObject dbJSON = (JSONObject) array.get(0);
	    
	    String loginID = (String) json.get("login_id");
	    String displayName = (String) dbJSON.get("displayname");
	    boolean admin = false;
	    if((byte)dbJSON.get("adminrole") == (byte)1) {
		admin = true;
	    }
	    String tokenString = AuthHelper.createJsonWebToken(loginID, displayName, admin, (long) 10000);
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
