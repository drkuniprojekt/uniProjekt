package rest;

import java.sql.SQLException;
import org.json.simple.JSONObject;
import Database.DatabaseHandler;

public class Alarm
{
	private JSONObject JSON;
	
	/**
	 * Designed for using with GET and DELETE (Alarm-Ressource)
	 * @throws SQLException
	 */
	public Alarm() throws SQLException
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
		//TODO: Attribut Name, Town und User-ID?
		DatabaseHandler.getdb().executeUpdate("INSERT INTO event (alertevent, starttime, ?)"
				+ " VALUES(TRUE, " + currentTime + ", ?)", JSON);
		//TODO: Push-Message
	}

	public void change() throws SQLException
	{
		checkAuthority();
		//TODO: Attribut Name und Town?
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
	
	private JSONObject fetchJSONFromDatabase() throws SQLException
	{
		JSONObject json;
		json = (JSONObject) DatabaseHandler.getdb().executeQuery(
				"SELECT * FROM event WHERE alertevent = TRUE AND endtime IS NULL").get(0); //TODO: Initial-Abfrage so?
		
		//TODO: Fall mehrere Datensätze abfangen?
		//TODO: Fall Kein Alarm abfangen?
		
		return json;
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
