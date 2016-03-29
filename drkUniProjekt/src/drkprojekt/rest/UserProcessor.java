package drkprojekt.rest;

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
			String pathInfo = request.getPathInfo();
			
			if(pathInfo == null || pathInfo.equals("/"))
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
				String userId;
				if(pathInfo.endsWith("/"))
					userId = pathInfo.substring(1,(pathInfo.length()-1));
				else
					userId = pathInfo.substring(1,(pathInfo.length()));
					
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
			User user = new User(Helper.getRequestJSON(request));
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
			JSONArray test = DatabaseHandler.getdb().executeQuery("SELECT COUNT(*) FROM user");
			Helper.setResponseJSONArray(response, test);
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
//		String pathInfo = request.getPathInfo();
//		
//		if(pathInfo == null || pathInfo.equals("/"))
//		{
//			response.sendError(HttpServletResponse.SC_NOT_FOUND);
//		}
//		else
//		{
//			try
//			{
//				String userId;
//				if(pathInfo.endsWith("/"))
//					userId = pathInfo.substring(1,(pathInfo.length()-1));
//				else
//					userId = pathInfo.substring(1,(pathInfo.length()));
//				
//				AuthHelper.assertIsAdmin(request, response);
//				User user = new User(userId);
//				user.delete();
//			} catch (IllegalStateException | SQLException | SignatureException e)
//			{
//				Helper.handleException(e, response);
//			}
//		}
	}
}
