package drkprojekt.rest;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

@WebServlet("/alarm/*")
public class AlarmProcessor extends HttpServlet
{
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
			try
			{
				Alarm alarm = new Alarm();
				JSONObject responseJSON = alarm.getJSON();
				if(responseJSON.isEmpty())
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				Helper.setResponseJSON(response, responseJSON);
			} catch (IllegalStateException e)
			{
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_CONFLICT);
			} catch (SQLException e)
			{
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}	
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Alarm alarm = new Alarm(Helper.getRequestJSON(request));
		try
		{
			alarm.create();
		} catch (IllegalStateException e)
		{
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_CONFLICT);
		} catch (SQLException e)
		{
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Alarm alarm = new Alarm(Helper.getRequestJSON(request));
		try
		{
			alarm.change();
		} catch (IllegalStateException e)
		{
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_CONFLICT);
		} catch (SQLException e)
		{
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Alarm alarm = new Alarm(Helper.getRequestJSON(request));
		try
		{
			alarm.delete();
		} catch (IllegalStateException e)
		{
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_CONFLICT);
		} catch (SQLException e)
		{
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
