package drkprojekt.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SignatureException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Helper 
{
	private static Logger log = LoggerFactory.getLogger(Helper.class);
	
	public static JSONObject getRequestJSON(HttpServletRequest request) throws IOException, ParseException
	{
		StringBuffer jb = new StringBuffer();
		String line = null;		
		JSONObject json = null;

		BufferedReader reader = request.getReader();
		while ((line = reader.readLine()) != null)
		{
			jb.append(line);
		}
		log.debug("Entire JSON-String: " + jb.toString());
		json = (JSONObject) new JSONParser().parse(jb.toString());
		
		return json;
	}
	
	public static void setResponseJSON(HttpServletResponse response, JSONObject text) throws IOException
	{
		response.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.print(text);
		writer.flush();
		writer.close();
	}
	
	public static void setResponseJSONArray(HttpServletResponse response, JSONArray json) throws IOException
	{
		response.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.print(json);
		writer.flush();
		writer.close();
	}
	
	public static void handleException(Exception e, HttpServletResponse response) throws IOException
	{
		log.warn("An exception was raised while executing the Request:\n " , e);
		if(e instanceof IllegalStateException)
		{
			response.sendError(HttpServletResponse.SC_CONFLICT);
		}
		else if(e instanceof SQLException)
		{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else if(e instanceof ParseException)
		{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}else if(e instanceof SignatureException)
		{		    
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
