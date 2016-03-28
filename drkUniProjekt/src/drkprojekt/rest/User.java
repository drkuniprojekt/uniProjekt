package drkprojekt.rest;

import java.security.SignatureException;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drkprojekt.auth.AuthHelper;
import drkprojekt.database.DatabaseHandler;

public class User
{
	private static Logger log = LoggerFactory.getLogger(User.class);
	private static final boolean[] DEFAULTSETTINGS = { false, false, true, false, true, true };
	
	private JSONObject JSON;
	private JSONArray JSONArray;
	
	public User(String userId) throws SQLException
	{
		JSON = fetchJSONFromDatabase(userId);
	}
	
	public User(JSONObject json)
	{
		this.JSON = json;
	}
	
	public User() throws SQLException
	{
		JSONArray = fetchJSONArrayFromDatabase(null);
	}
	
	public void create() throws SQLException
	{
		String login_id = (String) JSON.get("login_id");
		String userpassword = (String) JSON.get("userpassword");
		boolean adminrole;
		try
		{
			adminrole = Boolean.parseBoolean(JSON.get("adminrole").toString());
		} catch (NullPointerException e)
		{
			throw new SQLException(e);
		}
		String[] tmp = { login_id, login_id, userpassword };
		
		DatabaseHandler.getdb().executeUpdate("INSERT INTO user VALUES(?, ?, HASH_SHA256(TO_BINARY(?)), " + adminrole + ")", tmp);
		
		for (int i = 0; i < DatabaseHandler.SETTINGS.length; i++)
		{
			try
			{
				DatabaseHandler.getdb().executeUpdate("INSERT INTO setting VALUES('" + DatabaseHandler.SETTINGS[i] + "', "
						+ User.DEFAULTSETTINGS[i] + ", '" + login_id + "')");
			} catch (SQLException e)
			{
				fallback(login_id);
				throw e;
			}
		}
	}
	
	public JSONObject change() throws SQLException
	{
		String login_id = (String) JSON.get("login_id");
		JSON.remove("login_id");
		
		DatabaseHandler.getdb().executeUpdate("UPDATE user SET ? WHERE login_id = '" + login_id + "'", JSON);
		
		JSONObject returnJSON = new JSONObject();
		
		//User has changed Password - New Token!
		if(JSON.get("userpassword") != null)
		{
			JSONObject currentJSON = fetchJSONFromDatabase(login_id);
			String displayname = (String) currentJSON.get("displayname");
			boolean admin = Boolean.parseBoolean(currentJSON.get("adminrole").toString());
			String tokenString;
			try
			{
				tokenString = AuthHelper.createJsonWebToken(login_id, displayname, admin, (long) 10000);
			} catch (SignatureException e)
			{
				log.warn("An exception was raised while executing the Request:\n " , e);
				return null;
			}
			
			returnJSON.put("token", tokenString);
		}
		
		return returnJSON;
	}
	
	public void delete() throws SQLException
	{
		//TODO: User darf sich nicht selbst löschen
		String login_id = (String) JSON.get("login_id");
		
		//TODO: Löschweise besprechen...
		int users = DatabaseHandler.getdb().executeUpdate("DELETE FROM user WHERE login_id = '" + login_id + "'");
		DatabaseHandler.getdb().executeUpdate("DELETE FROM setting WHERE useraccount = '" + login_id + "'");
		
		if(users != 1)
			throw new IllegalStateException("User not found!");
	}
	
	private JSONObject fetchJSONFromDatabase(String userId) throws SQLException
	{
		JSONArray array = fetchJSONArrayFromDatabase(userId);
		
		if(array.isEmpty())
			return new JSONObject();
		
		JSONObject json = (JSONObject) array.get(0);
		System.out.println("Entire JSON: " + json);
		
		return json;
	}
	
	private JSONArray fetchJSONArrayFromDatabase(String userId) throws SQLException
	{
		JSONArray array;
		
		if(userId == null)
			array = DatabaseHandler.getdb().executeQuery(
				"SELECT login_id, displayname, adminrole FROM user");
		else
			array = DatabaseHandler.getdb().executeQuery(
				"SELECT login_id, displayname, adminrole FROM user WHERE login_id = '" + userId + "'");
		
		return array;
	}
	
	//TODO: Anpassen...
	private void fallback(String userId)
	{
		try
		{
			DatabaseHandler.getdb().executeUpdate("DELETE FROM user WHERE login_id = '" + userId + "'");
		} catch (SQLException e) {}
		
		for (int i = 0; i < DatabaseHandler.SETTINGS.length; i++)
		{
			try
			{
				DatabaseHandler.getdb().executeUpdate("DELETE FROM setting WHERE WHERE login_id = '" + userId + "'");
			} catch (SQLException e) {}
		}
	}

	public JSONObject getJSON()
	{
		return JSON;
	}

	public JSONArray getJSONArray()
	{
		return JSONArray;
	}
}
