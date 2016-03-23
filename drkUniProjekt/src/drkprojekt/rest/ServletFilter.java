package drkprojekt.rest;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drkprojekt.auth.AuthHelper;

@WebFilter("/*")
public class ServletFilter implements Filter
{
    private static Logger log;
   
    
    
    public ServletFilter() {
	super();
	 log = LoggerFactory.getLogger(this.getClass()); 
    }

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
		addCORSHeaders(request, response);
		if(!AuthHelper.isRegistered(request) && !request.getRequestURL().toString().endsWith("authentication/")){
		    log.error("Not authenticated!");
		    //response.sendError(HttpServletResponse.SC_FORBIDDEN);
		    //return;
		}
		chain.doFilter(req, res);
	}


	@Override
	public void init(FilterConfig arg0) throws ServletException
	{
		// TODO Auto-generated method stub
		
	}
	
	private static void addCORSHeaders(HttpServletRequest request, HttpServletResponse response)  
	{  
	     response.addHeader("Access-Control-Allow-Origin", "*");  
	     response.addHeader("Access-Control-Allow-Methods", "GET,POST, PUT, DELETE, OPTIONS");    
	     String requestCORSHeaders = request.getHeader("Access-Control-Request-Headers");  
	     if (requestCORSHeaders != null)  
	     {  
	         response.addHeader("Access-Control-Allow-Headers", requestCORSHeaders);  
	     }  
	}  

}
