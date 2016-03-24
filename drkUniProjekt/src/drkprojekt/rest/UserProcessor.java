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
import org.json.simple.parser.ParseException;

import drkprojekt.auth.AuthHelper;

@WebServlet("/user/*")
public class UserProcessor extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			String pathInfo = request.getPathInfo();
			System.out.println("PathInfo: " + request.getPathInfo());
			
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
					
				System.out.println("userId: " + userId);
				User user = new User(userId);
				JSONObject responseJSON = user.getJSON();
				if(responseJSON.isEmpty())
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				Helper.setResponseJSON(response, responseJSON);
			}
		} catch (SQLException e)
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
		} catch (SQLException | ParseException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{	
		
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		
	}
}
