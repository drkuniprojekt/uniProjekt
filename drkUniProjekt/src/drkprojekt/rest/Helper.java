package drkprojekt.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SignatureException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.NoSuchElementException;

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
		else if(e instanceof IllegalArgumentException)
		{
			response.sendError(422); //Unprocessible Entity
		}
		else if(e instanceof SQLException)
		{
			if(e instanceof SQLIntegrityConstraintViolationException)
				response.sendError(HttpServletResponse.SC_CONFLICT);
			else
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else if(e instanceof ParseException)
		{
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		else if(e instanceof SignatureException)
		{		    
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		}
		else if (e instanceof NoSuchElementException)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	public static String getSubResource(HttpServletRequest request, boolean allowMainResource) throws NoSuchElementException
	{
		String pathInfo = request.getPathInfo();
		
		if(pathInfo == null || pathInfo.equals("/"))
		{
			if(allowMainResource)
				return null;
			else
				throw new NoSuchElementException("Main Resource not allowed!");
		}
		else
		{
			String subResource = null;
			
			if(pathInfo.endsWith("/"))
				subResource = pathInfo.substring(1,(pathInfo.length()-1));
			else
				subResource = pathInfo.substring(1,(pathInfo.length()));
			
			return subResource;
		}
		
	}
}
