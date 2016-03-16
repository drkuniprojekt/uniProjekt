package rest;


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

/**
 * Servlet implementation class backend
 */
@WebServlet("/backend")
public class backend extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private RessourceFactory factory; 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public backend() {
        super();
        
        // Ressourcen instanziiern
        factory	= new RessourceFactory();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		factory.getRessource(request.getPathInfo()).doGet(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
//		try
//		{
//			response.addHeader("Access-Control-Allow-Origin", "*");
//			response.addHeader("Access-Control-Allow-Methods", "POST, GET");
//		    
//			String reqBody	= "";
//			BufferedReader br	= request.getReader();
//			String line;
//			while ((line = br.readLine()) != null)
//			{
//				reqBody += line;
//			}
//					
//
//			response.getWriter().write(sendGCMMessage(reqBody));
//			response.getWriter().flush();
//			response.getWriter().close();
//		}
//		catch( Exception e)
//		{
//			response.setStatus(501);
//			response.getWriter().write(e.getMessage());
//			response.getWriter().flush();
//			response.getWriter().close();
//		}
		
		factory.getRessource(request.getPathInfo()).doGet(request, response);	
	}
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		factory.getRessource(request.getPathInfo()).doGet(request, response);
	}
	
	private String sendGCMMessage(String body) throws IOException
	{
		URL url = new URL("https://gcm-http.googleapis.com/gcm/send");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "key=AIzaSyDcavG3GYtXKerQcxDBnUiecBHuqHUlX3U");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.getOutputStream().write(body.getBytes());
		conn.getOutputStream().close();
		//conn.connect();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
		String answer	= "";
		String inputLine;
		while ((inputLine = in.readLine()) != null) 
		{
			answer += inputLine;
		}	
		in.close();
		
		return answer;
	}

}
