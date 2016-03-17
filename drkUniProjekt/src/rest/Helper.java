package rest;

import java.io.IOException;
import java.io.PrintWriter;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

public class Helper 
{
	public void setResponseJSON(HttpServletResponse response, JSONObject text) throws IOException
	{
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.print(text);
		writer.flush();
		writer.close();
	}
}
