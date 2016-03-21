package drkprojekt.rest;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

@WebFilter("/*")
public class ServletFilter implements Filter
{

	@Override
	public void destroy()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		HttpSession session = request.getSession(true);
		
		System.out.println("Filter durchlaufen");
		addCORSHeaders(request, response);
		String role	= checkToken(request.getHeader("Authorization")); //Verwenden?!?
		chain.doFilter(req, res);
	}

	private String checkToken(String token)
	{
		//Pseudo Code -> Hier sollte überprüft werden !!!!!!!
		if(token != null)
		{
			return "admin";
		}
		return "NA";
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException
	{
		// TODO Auto-generated method stub
		
	}
	
	private static void addCORSHeaders(HttpServletRequest request, HttpServletResponse response)  
	{  
	     response.addHeader("Access-Control-Allow-Origin", "https://drkprojekt-p1941893014trial.dispatcher.hanatrial.ondemand.com");  
	     response.addHeader("Access-Control-Allow-Methods", "GET,POST, PUT, DELETE, OPTIONS");    
	     String requestCORSHeaders = request.getHeader("Access-Control-Request-Headers");  
	     if (requestCORSHeaders != null)  
	     {  
	         response.addHeader("Access-Control-Allow-Headers", requestCORSHeaders);  
	     }  
	}  

}
