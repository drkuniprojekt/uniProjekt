package drkprojekt.rest;

import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import drkprojekt.database.DatabaseHandler;

public class Event
{	
	private JSONObject JSON;
	private JSONArray JSONArray;

	public Event(int startYear, int startMonth) throws SQLException, IllegalArgumentException
	{
		JSONArray = fetchJSONArrayFromDatabase(startYear, startMonth);
	}

	public Event(JSONObject json)
	{
		this.JSON = json;
		checkJSON();
	}
	
	public Event()
	{}
	
	public void create() throws SQLException
	{			
		DatabaseHandler.getdb().executeUpdate("INSERT INTO event (event_id, alertevent, ?)"
				+ " VALUES(EVENT_ID.NEXTVAL, FALSE, ?)", JSON);
		//TODO: Evtl. wenigstens die Zeit übergeben?
		PushService.sendBroadCastMessage("Ein neuer Termin wurde angelegt.", PushService.NOTIFICATION_EVENT);
	}

	public void change(int eventId) throws SQLException, IllegalStateException
	{
		int rows = DatabaseHandler.getdb().executeUpdate("UPDATE event SET ? WHERE alertevent = FALSE AND event_id = " + eventId, JSON);
		
		if(rows == 0)
			throw new IllegalStateException("The desired event was not found!");
		
		//TODO: Hier auch Push?
		//PushService.sendBroadCastMessage("Ein Termin wurde bearbeitet", PushService.NOTIFICATION_EVENT);
	}

	public void delete(int eventId) throws SQLException, IllegalStateException
	{
		int rows = DatabaseHandler.getdb().executeUpdate("DELETE FROM event WHERE alertevent = FALSE AND event_id = ?", eventId + "");
		
		if(rows == 0)
			throw new IllegalStateException("The desired event was not found!");
	}
	
	public void accept(boolean accepted, String user, int eventId) throws SQLException, IllegalStateException
	{
		if(DatabaseHandler.getdb().executeQuery("SELECT event_id FROM event WHERE event_id = ? AND alertevent = FALSE", eventId + "").isEmpty())
			throw new IllegalStateException("The desired event was not found!");
		
		String[] arguments = { accepted + "", eventId + "", user };
		DatabaseHandler.getdb().executeUpdate("INSERT INTO eventanswer (answer, event, answerer) VALUES(?,?,?)", arguments);
	}
	
	public JSONArray getAllAnswers(int eventId) throws SQLException, IllegalStateException
	{
		if(DatabaseHandler.getdb().executeQuery("SELECT event_id FROM event WHERE event_id = ? AND alertevent = FALSE", eventId + "").isEmpty())
			throw new IllegalStateException("The desired event was not found!");
		
		return DatabaseHandler.getdb().executeQuery("SELECT answer, answerer, displayname FROM user, eventanswer WHERE event = ? AND login_id = eventanswer.answerer", eventId + "");
	}
	
	private JSONArray fetchJSONArrayFromDatabase(int startYear, int startMonth) throws SQLException, IllegalArgumentException
	{
		if(startMonth < 1 || startMonth > 12)
			throw new IllegalArgumentException("Month is invalid!");
		
		String param1 = startYear + "-" + startMonth + "-01 00:00:00";
		String param2;
		
		switch (startMonth)
		{
		case 11:
			param2 = (startYear+1) + "-01-01 00:00:00";
			break;
		case 12:
			param2 = (startYear+1) + "-02-01 00:00:00";
		default:
			param2 = startYear + "-" + (startMonth+2) + "-01 00:00:00";
			break;
		}
		
		String arguments[] = { param1, param2 };
		
		JSONArray array = DatabaseHandler.getdb().executeQuery(
				"SELECT event_id, starttime, endtime, name, description, street, housenumber, zip, town "
				+ "FROM event WHERE alertevent = FALSE AND starttime >= ? AND starttime < ?", arguments);
		
		return array;
	}
	
	private void checkJSON()
	{
		//TODO: Check ob Starttime vor Endtime
	}

	public JSONArray getJSONArray()
	{
		return JSONArray;
	}
}
