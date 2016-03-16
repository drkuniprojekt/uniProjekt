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
	public static ArrayList<Ressource> res = new ArrayList<Ressource>();
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public backend() {
        super();
        
        // Ressourcen instanziiern
        res.add(new AuthRes());
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		switch (request.getPathInfo())
		{
		case "/auth":
			res.get(0).doGet(request, response);
			break;
		default:
			response.setStatus(404);
		}
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
		switch (request.getPathInfo())
		{
		case "/auth":
			res.get(0).doPost(request, response);
			break;
		default:
			response.setStatus(404);
		}		
	}
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		switch (request.getPathInfo())
		{
		case "/auth":
			res.get(0).doPost(request, response);
			break;
		default:
			response.setStatus(404);
		}
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
