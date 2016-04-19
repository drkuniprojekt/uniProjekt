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
import org.json.simple.parser.ParseException;

import drkprojekt.auth.AuthHelper;

@WebServlet("/event/*")
public class EventProcessor extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{		
		try
		{
			String sy = request.getParameter("year");
			String sm = request.getParameter("month");
			
			if(sy == null || sm == null)
				throw new IllegalArgumentException("Mandatory parameters not supplied!");
			
			int startYear = Integer.parseInt(sy);
			int startMonth = Integer.parseInt(sm);
			
			Event event = new Event(startYear, startMonth);
			JSONArray responseJSONArray = event.getJSONArray();
			if(responseJSONArray.isEmpty())
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			Helper.setResponseJSONArray(response, responseJSONArray);
		} catch (IllegalArgumentException | SQLException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			AuthHelper.assertIsAdmin(request);
			Event event = new Event(Helper.getRequestJSON(request));
			event.create();
		} catch (ParseException | SQLException | SignatureException | IllegalStateException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{	
		try
		{
			AuthHelper.assertIsAdmin(request);
			int eventId = Helper.getSubResourceID(request, false);
			
			Event event = new Event(Helper.getRequestJSON(request));
			event.change(eventId);
		} catch (ParseException | IllegalStateException | SQLException | SignatureException | NoSuchElementException e)
		{
			Helper.handleException(e, response);
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			AuthHelper.assertIsAdmin(request);
			int eventId = Helper.getSubResourceID(request, false);
			
			Event event = new Event();
			event.delete(eventId);
		} catch (IllegalStateException | SQLException | SignatureException | NoSuchElementException e)
		{
			Helper.handleException(e, response);
		}
	}
}
