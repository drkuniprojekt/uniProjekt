package drkprojekt.rest;

import java.sql.SQLException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import drkprojekt.database.DatabaseHandler;

public class Alarm
{
	private static final String[] ALERT_GROUPS = { "SEGV", "SEGS", "SBF", "OV" };
	
	private JSONObject JSON;
	private int eventId;
	
	/**
	 * Designed for using with GET and DELETE (Alarm-Ressource)
	 * Designed for using with AlarmResponse-Ressource
	 * @throws SQLException
	 */
	public Alarm() throws SQLException, IllegalStateException
	{
		JSON = fetchJSONFromDatabase();
		eventId = fetchEventIdFromDatabase();
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
		if(!fetchJSONFromDatabase().isEmpty())
			throw new IllegalStateException("An alertevent is already running!");
		
		int notificationType = getNotificationType();
			
		DatabaseHandler.getdb().executeUpdate("INSERT INTO event (event_id, alertevent, starttime, ?)"
				+ " VALUES(EVENT_ID.NEXTVAL, TRUE, CURRENT_TIMESTAMP, ?)", JSON);
		PushService.sendBroadCastMessage("Neuer Alarm!", notificationType);
	}

	public void change() throws SQLException, IllegalStateException
	{
		if(fetchJSONFromDatabase().isEmpty())
			throw new IllegalStateException("No alertevent found!");
		
		int notificationType = getNotificationType();
		
		DatabaseHandler.getdb().executeUpdate("UPDATE event SET ? WHERE alertevent = TRUE AND endtime IS NULL", JSON);
		PushService.sendBroadCastMessage("Der aktuelle Alarm wurde bearbeitet!", notificationType);
	}

	public void delete() throws SQLException, IllegalStateException
	{
		if(JSON.isEmpty())
			throw new IllegalStateException("No alertevent found!");
		
		DatabaseHandler.getdb().executeUpdate("UPDATE event SET endtime = CURRENT_TIMESTAMP WHERE alertevent = TRUE AND endtime IS NULL");
	}
	
	public void accept(boolean accepted, boolean car, String user) throws SQLException, IllegalStateException
	{
		if(JSON.isEmpty())
			throw new IllegalStateException("No alertevent found!");
		
		String[] arguments = { accepted + "", car + "", user + "" };
		DatabaseHandler.getdb().executeUpdate("INSERT INTO eventanswer VALUES(?,?, " + eventId + ", ?)", arguments);
	}
	
	public JSONArray getAllAnswers() throws SQLException, IllegalStateException
	{
		if(JSON.isEmpty())
			throw new IllegalStateException("No alertevent found!");
		
		return DatabaseHandler.getdb().executeQuery("SELECT answer, availablecar, answerer, displayname FROM user, eventanswer WHERE event = ? AND login_id = eventanswer.answerer", eventId + "");
	}
	
	private JSONObject fetchJSONFromDatabase() throws SQLException, IllegalStateException
	{
		JSONArray array = DatabaseHandler.getdb().executeQuery(
				"SELECT description, requiredthings, quantitymembers, street, housenumber, zip, town, usergroup "
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
	
	private int fetchEventIdFromDatabase() throws SQLException
	{
		JSONArray array = DatabaseHandler.getdb().executeQuery("SELECT event_id "
				+ "FROM event WHERE alertevent = TRUE AND endtime IS NULL");
		
		if(array.isEmpty())
			return 0;

		JSONObject json = (JSONObject) array.get(0);

		return (int) json.get("event_id");
	}
	
	private int getNotificationType() throws SQLException
	{
		String shortDescr = (String) JSON.get("usergroup");
		if(shortDescr == null)
			throw new SQLException("Group not found!");
		
		for (int i = 0; i < ALERT_GROUPS.length; i++)
		{
			if(shortDescr.equals(ALERT_GROUPS[i]))
			{
				return (i+6); 
			}
		}
		
		throw new SQLException("Group not found!");
	}
	
	public JSONObject getJSON()
	{
		return JSON;
	}
}
