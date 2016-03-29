package drkprojekt.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

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

@WebServlet("/settings/*")
public class SettingsProcessor extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String userId = AuthHelper.getToken(request).getUserId();
		try
		{
			JSONArray array = DatabaseHandler.getdb().executeQuery("SELECT setting, settingvalue FROM setting WHERE useraccount = '" + userId + "'");
			JSONObject responseJSON = new JSONObject();
			
			if(array.size() != DatabaseHandler.SETTINGS.length)
			{
				response.sendError(HttpServletResponse.SC_CONFLICT);
				return;
			}
			
			for (int i = 0; i < array.size(); i++)
			{
				JSONObject json = (JSONObject) array.get(i);
				responseJSON.put(json.get("setting"), json.get("settingvalue"));
			}
			Helper.setResponseJSON(response, responseJSON);
		} catch (SQLException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String userId = AuthHelper.getToken(request).getUserId();
		
		try
		{
			JSONObject requestJSON = Helper.getRequestJSON(request);

			for(Iterator iterator = requestJSON.keySet().iterator(); iterator.hasNext();)
			{
				String key = (String) iterator.next();
				String value = (String) requestJSON.get(key).toString();
				boolean settingsvalue = Boolean.parseBoolean(value);
				int rows = 
				DatabaseHandler.getdb().executeUpdate("UPDATE setting SET settingvalue = " + settingsvalue + 
						" WHERE useraccount = '" + userId + "' AND setting = '" + key + "'");
				
				if(rows < 1)
				{
					response.sendError(HttpServletResponse.SC_CONFLICT);
					return;
				}
			}
		} catch (SQLException | ParseException e)
		{
			Helper.handleException(e, response);
		}
	}
}
