package drkprojekt.rest;

import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drkprojekt.database.DatabaseHandler;

public class Alarm
{
	private static Logger log = LoggerFactory.getLogger(Alarm.class);
	
	private JSONObject JSON;
	private int eventId;
	
	/**
	 * Designed for using with GET and DELETE (Alarm-Ressource)
	 * Designed for using with AlarmResponse-Ressource
	 * @throws SQLException
	 */
	public Alarm() throws SQLException, IllegalStateException
	{
		eventId = fetchEventIdFromDatabase();
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
		if(!fetchJSONFromDatabase().isEmpty())
			throw new IllegalStateException("An alertevent is already running!");
		
		int[] notificationTypes = convertNotificationTypes();
			
		DatabaseHandler.getdb().executeUpdate("INSERT INTO event (event_id, alertevent, starttime, ?)"
				+ " VALUES(event_id.NEXTVAL, TRUE, CURRENT_TIMESTAMP, ?)", JSON);
		
		for (int i = 0; i < notificationTypes.length; i++)
		{
			DatabaseHandler.getdb().executeUpdate("INSERT INTO alertgroup VALUES(event_id.CURRVAL,?)", DatabaseHandler.SETTINGS[notificationTypes[i]]);
		}
		
		PushService.sendMulticastAlert("Neuer Alarm!", notificationTypes);
	}

	public void change() throws SQLException, IllegalStateException
	{
		if(fetchJSONFromDatabase().isEmpty())
			throw new IllegalStateException("No alertevent found!");
		
		int[] notificationTypes = convertNotificationTypes();
		
		DatabaseHandler.getdb().executeUpdate("UPDATE event SET ? WHERE alertevent = TRUE AND endtime IS NULL", JSON);
		DatabaseHandler.getdb().executeUpdate("DELETE FROM alertgroup WHERE event_id = ?", eventId + "");
		
		for (int i = 0; i < notificationTypes.length; i++)
		{
			String[] arguments = { eventId + "", DatabaseHandler.SETTINGS[notificationTypes[i]] };
			DatabaseHandler.getdb().executeUpdate("INSERT INTO alertgroup VALUES(?,?)", arguments);
		}
		
		PushService.sendMulticastAlert("Der aktuelle Alarm wurde bearbeitet!", notificationTypes);
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
			if(eventId == 0)
				eventId = fetchEventIdFromDatabase();
			
			JSONArray tmp = DatabaseHandler.getdb().executeQuery("SELECT usergroup FROM alertgroup WHERE event_id = ?", eventId + "");
			Object[] mappingGroups = tmp.toArray();
			StringBuffer usergroupArrayString = new StringBuffer();
			
			if(tmp.isEmpty())
				throw new IllegalStateException("Inconsistent data in database - No usergroup specified for the current alert!");
			
			usergroupArrayString.append("[");
			for (int i = 0; i < mappingGroups.length; i++)
			{
				JSONObject group = (JSONObject) mappingGroups[i];
				String groupString = group.get("usergroup").toString();
				log.debug("Groupstring: " + groupString);
				
				if(i != 0)
					usergroupArrayString.append(",");
				usergroupArrayString.append("\"" + groupString + "\"");
				//usergroupArrayString[i] = groupString;
			}
			usergroupArrayString.append("]");
			
			if(!tmp.isEmpty())
				json.put("usergroup", usergroupArrayString);
			
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
	
	private int[] convertNotificationTypes() throws SQLException
	{
		log.debug("Converting Notification Type...");
		JSONArray usergroup = (JSONArray) JSON.get("usergroup");
		Object[] shortDescr = usergroup.toArray();
		JSON.remove("usergroup");
		
		if(shortDescr == null || shortDescr.length == 0)
			throw new SQLException("No group found!");
		
		int[] types = new int[shortDescr.length];
		
		log.debug("Array-Length: " + shortDescr.length);
		
		int k = 0;
		for (int i = 6; i < DatabaseHandler.SETTINGS.length; i++)
		{
			for (int j = 0; j < shortDescr.length; j++)
			{
				if (DatabaseHandler.SETTINGS[i].equalsIgnoreCase(shortDescr[j].toString()))
				{
					log.debug("Type found: " + i);
					types[k] = i;
					k++;
				}
			}
		}
		
		if(k != shortDescr.length)
			throw new SQLException("Group-Parameters are invalid!");
		
		return types;
	}
	
	public JSONObject getJSON()
	{
		return JSON;
	}
}
