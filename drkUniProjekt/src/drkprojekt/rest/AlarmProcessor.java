package drkprojekt.rest;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import drkprojekt.auth.AuthHelper;

@WebServlet("/alarm/*")
public class AlarmProcessor extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			Alarm alarm = new Alarm();
			JSONObject responseJSON = alarm.getJSON();
			if(responseJSON.isEmpty())
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			Helper.setResponseJSON(response, responseJSON);
		} catch (IllegalStateException | SQLException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			AuthHelper.assertIsAdmin(request, response);
			Alarm alarm = new Alarm(Helper.getRequestJSON(request));
			alarm.create();
		} catch (ParseException | IllegalStateException | SQLException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{	
		try
		{
			AuthHelper.assertIsAdmin(request, response);
			Alarm alarm = new Alarm(Helper.getRequestJSON(request));
			alarm.change();
		} catch (ParseException | IllegalStateException | SQLException e)
		{
			Helper.handleException(e, response);
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			AuthHelper.assertIsAdmin(request, response);
			Alarm alarm = new Alarm();
			alarm.delete();
		} catch (IllegalStateException | SQLException e)
		{
			Helper.handleException(e, response);
		}
	}
}
