package Tests;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import drkprojekt.rest.Helper;

/**
 * Servlet implementation class backend
 */
@WebServlet("/auth/*")
public class auth extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private Helper helper;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public auth() {
        super();  
        helper	= new Helper();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		JSONObject answer	= new JSONObject();
		try
		{
			JSONObject input	= helper.getRequestJSON(request);
			if(input.get("login_id").equals("User1") && input.get("userpassword").equals("123456"))
			{
				answer.put("token", "{bearer 12345678910}");
				answer.put("successfull", true);
				answer.put("adminrole", false);
			}else
			{
				answer.put("successfull", false);
			}						
			
		} catch (Exception e)
		{
			response.setStatus(400);
			answer.put("Error", e);			
		}finally
		{
			helper.setResponseJSON(response, answer);
		}
		
	}

}
