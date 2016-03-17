package rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class Ressource
{
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setStatus(405);
	}
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setStatus(405);
	}
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setStatus(405);
	}
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		response.setStatus(405);
	}
}
