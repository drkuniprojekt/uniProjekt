package drkprojekt.rest;

import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import drkprojekt.database.DatabaseHandler;

public class Alarm
{
	private JSONObject JSON;
	
	/**
	 * Designed for using with GET and DELETE (Alarm-Ressource)
	 * @throws SQLException
	 */
	public Alarm() throws SQLException, IllegalStateException
	{
		JSON = fetchJSONFromDatabase();
	}

	/**
	 * Designed for using with POST and PUT (Alarm-Ressource)
	 * @param json Request JSON
	 */
	public Alarm(JSONObject json)
	{
		//TODO: GGfls (je nach Schnittstelle) müssen hier noch die überflüssigen Variablen (Username/Token) raus
		this.JSON = json;
	}
	
	public void create() throws SQLException
	{
		checkAuthority();
		String currentTime = DatabaseHandler.getdb().getTimeStamp();
		//TODO: Attribut Town und User-ID?
		DatabaseHandler.getdb().executeUpdate("INSERT INTO event (alertevent, starttime, ?)"
				+ " VALUES(TRUE, " + currentTime + ", ?)", JSON);
		//TODO: Push-Message
	}

	public void change() throws SQLException
	{
		checkAuthority();
		//TODO: Attribut Town?
		DatabaseHandler.getdb().executeUpdate("UPDATE event SET ? WHERE alertevent = TRUE AND endtime IS NOT NULL", JSON);
		//TODO: Push-Message neu generieren?
	}

	public void delete() throws SQLException
	{
		checkAuthority();
		String currentTime = DatabaseHandler.getdb().getTimeStamp();
		DatabaseHandler.getdb().executeUpdate("UPDATE event SET endtime = " + currentTime + " WHERE alertevent = TRUE AND endtime IS NULL");
	}
	
	public void accept(boolean accepted, boolean car)
	{
		
	}

	private void checkAuthority()
	{
		//TODO: Authority-Check
		//if()
		//{
			throw new SecurityException("Authorization failed - You are not an organizer!");
		//}
	}
	
	private JSONObject fetchJSONFromDatabase() throws SQLException, IllegalStateException
	{
		//TODO: Attribut town
		JSONArray array = DatabaseHandler.getdb().executeQuery(
				"SELECT event_id, description, requiredthings, quantitymembers, street, housenumber, zip, town "
				+ "FROM event WHERE alertevent = TRUE AND endtime IS NULL");
		
		JSONObject json = (JSONObject) array.get(0);
		System.out.println("Entire JSON: " + json);
		
		try
		{
			array.get(1);
			throw new IllegalStateException("Inconsistent data in database - More than one alertevent found!");
		} catch(IndexOutOfBoundsException e)
		{ 
			return json;
		}
	}
	
	public void setJSON(JSONObject JSON)
	{
		this.JSON = JSON;
	}
	
	public JSONObject getJSON()
	{
		return JSON;
	}
}
