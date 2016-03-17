package rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NullRes extends Ressource
{
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setStatus(404);
	}
	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setStatus(404);
	}
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setStatus(404);
	}
	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setStatus(404);
	}

}
