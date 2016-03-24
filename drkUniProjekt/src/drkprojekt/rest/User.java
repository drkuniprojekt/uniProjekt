package drkprojekt.rest;

import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import drkprojekt.database.DatabaseHandler;

public class User
{
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
		boolean adminrole = Boolean.parseBoolean(JSON.get("adminrole").toString());
		String[] tmp = { login_id, userpassword };
		
		//TODO: Settings setzen
		DatabaseHandler.getdb().executeUpdate("INSERT INTO user (login_id, userpassword, adminrole) VALUES(?, HASH_SHA256(TO_BINARY(?)), " + adminrole + ")", tmp);
	}
	
	public void change() throws SQLException
	{
		String login_id = (String) JSON.get("login_id");
		JSON.remove("login_id");
		
		DatabaseHandler.getdb().executeUpdate("UPDATE user SET ? WHERE login_id = '" + login_id + "'", JSON);
	}
	
	public void delete()
	{
		//TODO: Settings löschen
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

	public JSONObject getJSON()
	{
		return JSON;
	}

	public JSONArray getJSONArray()
	{
		return JSONArray;
	}
}
