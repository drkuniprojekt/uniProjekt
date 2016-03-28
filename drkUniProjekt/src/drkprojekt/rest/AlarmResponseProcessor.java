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

@WebServlet("/response/*")
public class AlarmResponseProcessor extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			Alarm alarm = new Alarm();
			JSONArray responseJSON = alarm.getAllAnswers();	
			if(responseJSON.isEmpty())
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			Helper.setResponseJSONArray(response, responseJSON);
		} catch (IllegalStateException | SQLException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			Alarm alarm = new Alarm();
			JSONObject answer = Helper.getRequestJSON(request);
			System.out.println("answer: '" + answer.get("answer") + "'");
			System.out.println("answer.toString(): '" + answer.get("answer").toString() + "'");
			System.out.println("Boolean: " + Boolean.parseBoolean(answer.get("answer").toString()));
			
			alarm.accept(Boolean.parseBoolean(answer.get("answer").toString()),
					     Boolean.parseBoolean(answer.get("availablecar").toString()), 
					     answer.get("answerer").toString());			
		} catch (NullPointerException e)
		{
			Helper.handleException(new SQLException("Argument must not be null!"), response);
		} catch (IllegalStateException | SQLException | ParseException e)
		{
			Helper.handleException(e, response);
		}
	}
}
