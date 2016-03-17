package rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthRes extends Ressource
{
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.getWriter().write("{\"token\":\"bearer 12345678910\"}");
		response.getWriter().flush();
		response.getWriter().close();		
	}

}
