package rest;

import java.util.ArrayList;

public class RessourceFactory
{
	public static ArrayList<Ressource> res = new ArrayList<Ressource>();
	
	public RessourceFactory()
	{
		res.add(new Ressource());
		res.add(new AuthRes());
	}
	public Ressource getRessource(String path)
	{
		if(path.startsWith("/auth"))
		{
			return res.get(1);
		}
		return res.get(0);
	}
}
