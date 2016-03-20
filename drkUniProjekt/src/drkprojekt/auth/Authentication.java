package drkprojekt.auth;


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

import drkprojekt.rest.Helper;

/**
 * Servlet implementation class backend
 */
@WebServlet("/authentication/*")
public class Authentication extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Authentication() {
        super();  
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//TODO: Password und username mit datenbankeintrag vergleichen, register id setzen, token erzeugen und zur�ckgeben
		JSONObject answer	= new JSONObject();
		answer.put("token", "{bearer 12345678910}");
		Helper.setResponseJSON(response, answer);
	}
	protected void doDelete(HttpServletRequest request, HttpServletResponse response){
	    	//TODO: wie kann das Token gel�scht werden, wenn es nur vom Client gespeichert wird, wie setzt man es effektiv auf outdated
	}
}
