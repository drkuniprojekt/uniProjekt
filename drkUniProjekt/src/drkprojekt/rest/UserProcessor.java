package drkprojekt.rest;

import java.io.IOException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonObject;

import drkprojekt.auth.AuthHelper;
import drkprojekt.database.DatabaseHandler;

@WebServlet("/user/*")
public class UserProcessor extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			String userId = Helper.getSubResource(request, true);
			
			if(userId == null)
			{
				AuthHelper.assertIsAdmin(request, response);
				User user = new User();
				JSONArray responseJSONArray = user.getJSONArray();
				if(responseJSONArray.isEmpty())
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				Helper.setResponseJSONArray(response, responseJSONArray);
			}
			else
			{
				User user = new User(userId);
				JSONObject responseJSON = user.getJSON();
				if(responseJSON.isEmpty())
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				Helper.setResponseJSON(response, responseJSON);
			}
		} catch (SQLException | SignatureException e)
		{
			Helper.handleException(e, response);
		}
		//request.getPathInfo() --> /ID oder /ID/
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			AuthHelper.assertIsAdmin(request, response);
			User user = new User(Helper.getRequestJSON(request));
			user.create();
		} catch (SQLException | ParseException | SignatureException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{	
		try
		{
			User user = new User(Helper.getRequestJSON(request), AuthHelper.getToken(request).getUserId());
			JSONObject responseJSON = user.change();
			if(responseJSON == null)
			{
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			if(responseJSON.isEmpty())
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			Helper.setResponseJSON(response, responseJSON);
		} catch (ParseException | SQLException e)
		{
			Helper.handleException(e, response);
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{	
			String userId = Helper.getSubResource(request, false);				


			//TODO: Wieder weg
			try
			{
				JSONObject tmp = Helper.getRequestJSON(request);
				if(tmp.get("master").toString().equals("master"))
				{
					DatabaseHandler.getdb().executeUpdate("DELETE FROM setting WHERE useraccount = '" + userId + "'");
					DatabaseHandler.getdb().executeUpdate("DELETE FROM chatroommapping WHERE useraccount = '" + userId + "'");
					DatabaseHandler.getdb().executeUpdate("DELETE FROM user WHERE login_id = '" + userId + "'");
					return;
				}
			} catch (ParseException e)
			{
				e.printStackTrace();
			} catch (NullPointerException e) {}






			AuthHelper.assertIsAdmin(request, response);
			User user = new User(userId);
			user.delete(AuthHelper.getToken(request).getUserId());
		} catch (IllegalStateException | IllegalArgumentException | SQLException | SignatureException | NoSuchElementException e)
		{
			Helper.handleException(e, response);
		}
	}

}
