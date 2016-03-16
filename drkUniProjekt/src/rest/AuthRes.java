package rest;

public class AuthRes extends Ressource
{
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	{
		response.getWriter().write("{\"token\":\"bearer 12345678910\"}");
		response.getWriter().flush();
		response.getWriter().close();		
	}

}
