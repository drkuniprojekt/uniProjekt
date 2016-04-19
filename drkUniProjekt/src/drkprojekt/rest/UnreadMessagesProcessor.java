/**
 * 
 */
package drkprojekt.rest;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import drkprojekt.auth.AuthHelper;
import drkprojekt.database.DatabaseHandler;

/**
 * @author Steffen Terheiden
 *
 */
@WebServlet("/unreadMessages/*")
public class UnreadMessagesProcessor extends HttpServlet{

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
	JSONArray array;
	JSONObject answer = new JSONObject();
	try {
	    String login_ID = AuthHelper.getToken(request).getUserId();
	    array = DatabaseHandler.getdb().executeQuery("SELECT COUNT(*) AS unreadmessages FROM messagesunread WHERE useraccount=? GROUP BY useraccount",login_ID);
	    if(array.isEmpty()){
		answer.put("unreadmessages", "0");
	    }else{
		answer = (JSONObject) array.get(0);
	    }
	    
	    Helper.setResponseJSON(response, answer);	
	} catch (SQLException e) {
	    Helper.handleException(e, response);
	}
    }
}
