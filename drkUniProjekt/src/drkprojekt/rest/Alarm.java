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
		this.JSON = json;
	}
	
	public void create() throws SQLException, IllegalStateException
	{
		checkAuthority();
		if(!fetchJSONFromDatabase().isEmpty())
			throw new IllegalStateException("An alertevent is already running!");
			
		DatabaseHandler.getdb().executeUpdate("INSERT INTO event (event_id, alertevent, starttime, ?)"
				+ " VALUES(EVENT_ID.NEXTVAL, TRUE, CURRENT_TIMESTAMP, ?)", JSON);
		//TODO: Push-Message
	}

	public void change() throws SQLException, IllegalStateException
	{
		checkAuthority();
		if(fetchJSONFromDatabase().isEmpty())
			throw new IllegalStateException("No alertevent found!");
		
		//TODO: Creator ändern zulassen?
		DatabaseHandler.getdb().executeUpdate("UPDATE event SET ? WHERE alertevent = TRUE AND endtime IS NULL", JSON);
		//TODO: Push-Message neu generieren?
	}

	public void delete() throws SQLException, IllegalStateException
	{
		checkAuthority();
		if(JSON.isEmpty())
			throw new IllegalStateException("No alertevent found!");
		
		DatabaseHandler.getdb().executeUpdate("UPDATE event SET endtime = CURRENT_TIMESTAMP WHERE alertevent = TRUE AND endtime IS NULL");
	}
	
	public void accept(boolean accepted, boolean car)
	{
		
	}

	private void checkAuthority() throws SecurityException
	{
		//TODO: Authority-Check
		//if()
		//{
		//	throw new SecurityException("Authorization failed - You are not an organizer!");
		//}
	}
	
	private JSONObject fetchJSONFromDatabase() throws SQLException, IllegalStateException
	{
		JSONArray array = DatabaseHandler.getdb().executeQuery(
				"SELECT description, requiredthings, quantitymembers, street, housenumber, zip, town "
				+ "FROM event WHERE alertevent = TRUE AND endtime IS NULL");
		
		if(array.isEmpty())
			return new JSONObject();
		
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
	
	public JSONObject getJSON()
	{
		return JSON;
	}
}
