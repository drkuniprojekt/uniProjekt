package drkprojekt.rest;


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
		answer.put("token", "{bearer 12345678910}");
		helper.setResponseJSON(response, answer);
	}

}
