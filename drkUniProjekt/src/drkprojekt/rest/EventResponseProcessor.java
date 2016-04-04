package drkprojekt.rest;

import java.io.IOException;
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

@WebServlet("/eventresponse/*")
public class EventResponseProcessor extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			int eventId = Helper.getSubResourceID(request, false);
			
			Event event = new Event();
			JSONArray responseJSON = event.getAllAnswers(eventId);	
			if(responseJSON.isEmpty())
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			Helper.setResponseJSONArray(response, responseJSON);
		} catch (IllegalStateException | SQLException | NoSuchElementException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			int eventId = Helper.getSubResourceID(request, false);
			
			Event event = new Event();
			JSONObject eventResponse = Helper.getRequestJSON(request);
			
			event.accept(Boolean.parseBoolean(eventResponse.get("answer").toString()),
					     eventResponse.get("answerer").toString(), eventId);
		} catch (NullPointerException e)
		{
			Helper.handleException(new SQLException("Argument must not be null!"), response);
		} catch (IllegalStateException | SQLException | ParseException | NoSuchElementException e)
		{
			Helper.handleException(e, response);
		}
	}
}
