package drkprojekt.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Helper 
{
	public static JSONObject getRequestJSON(HttpServletRequest request) throws IOException
	{
		StringBuffer jb = new StringBuffer();
		String line = null;		
		JSONObject json = null;

		BufferedReader reader = request.getReader();
		while ((line = reader.readLine()) != null)
		{
			jb.append(line);
		}

		try
		{
			json = (JSONObject) new JSONParser().parse(jb.toString());
		} catch (ParseException e)
		{
			throw new IOException("Error parsing JSON request string");
		}
		
		return json;
	}
	
	public static void setResponseJSON(HttpServletResponse response, JSONObject text) throws IOException
	{
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.print(text);
		writer.flush();
		writer.close();
	}
	
	public static void setResponseJSONArray(HttpServletResponse response, JSONArray json) throws IOException
	{
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.print(json);
		writer.flush();
		writer.close();
	}
}
