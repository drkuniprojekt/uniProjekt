package rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Ressource
{
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	{
		response.setStatus(405);
	}
	public void doPut(HttpServletRequest request, HttpServletResponse response)
	{
		response.setStatus(405);
	}
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	{
		response.setStatus(405);
	}
	public void doDelete(HttpServletRequest request, HttpServletResponse response)
	{
		response.setStatus(405);
	}
}
