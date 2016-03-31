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
		JSONObject tmp = (JSONObject) DatabaseHandler.getdb().executeQuery("SELECT COUNT(*) FROM user").get(0);
		JSONArray allUsers = fetchJSONArrayFromDatabase(null);
		boolean adminrole;
		int userCount;
		try
		{
			adminrole = Boolean.parseBoolean(JSON.get("adminrole").toString());
			userCount = Integer.parseInt(tmp.get("count(*)").toString());
			log.debug("userCount: " + userCount);
		} catch (NullPointerException e)
		{
			throw new SQLException(e);
		}
		
		String[] statements = new String[DatabaseHandler.SETTINGS.length + 2 + (3 * allUsers.size())];
		String[][] arguments = new String[DatabaseHandler.SETTINGS.length + 2 + (3 * allUsers.size())][];
		
		String insertUserStatement = "INSERT INTO user VALUES(?, ?, HASH_SHA256(TO_BINARY(?)), " + adminrole + ", false)";
		String[] insertUserArguments = { login_id, login_id, userpassword };
		statements[0] = insertUserStatement;
		arguments[0] = insertUserArguments;
		
		String[] insertSettingStatement = new String[DatabaseHandler.SETTINGS.length];
		
		for(int i = 0; i < DatabaseHandler.SETTINGS.length; i++)
		{
			insertSettingStatement[i] = "INSERT INTO setting VALUES('" + DatabaseHandler.SETTINGS[i] + "', "
					+ User.DEFAULTSETTINGS[i] + ", '" + login_id + "')";
			statements[i+2] = insertSettingStatement[i];
			arguments[i+2] = new String[0];
		}
		
		//Groupchat
		String insertChatroommappingStatementGroupChat = "INSERT INTO chatroommapping VALUES(1, '" + login_id + "')";
		statements[1] = insertChatroommappingStatementGroupChat;
		arguments[1] = new String[0];
		
		for (int i = 0; i < (3 * allUsers.size()); i=i+3)
		{
			JSONObject tmpUser = (JSONObject) allUsers.get(i/3);
			String tmpUserLoginId = tmpUser.get("login_id").toString();
			log.debug("Now processing Chats between " + tmpUserLoginId + " AND " + login_id);
			
			statements[i+2+DatabaseHandler.SETTINGS.length] =
					"INSERT INTO chatroom VALUES(chatroom_id.NEXTVAL)";
			arguments[i+2+DatabaseHandler.SETTINGS.length] = new String[0];
			
			statements[i+1+2+DatabaseHandler.SETTINGS.length] =
					"INSERT INTO chatroommapping VALUES(chatroom_id.CURRVAL, '" + login_id + "')";
			arguments[i+1+2+DatabaseHandler.SETTINGS.length] = new String[0];
			
			statements[i+2+2+DatabaseHandler.SETTINGS.length] =
					"INSERT INTO chatroommapping VALUES(chatroom_id.CURRVAL, '" + tmpUserLoginId + "')";
			arguments[i+2+2+DatabaseHandler.SETTINGS.length] = new String[0];
		}
		
		DatabaseHandler.getdb().executeTransactionUpdate(statements, arguments);
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
			
			Object put = returnJSON.put("token", tokenString);
		}
		
		return returnJSON;
	}
	
	public void delete(String sender) throws SQLException, IllegalStateException, IllegalArgumentException
	{
		String login_id = (String) JSON.get("login_id");
		
		if(login_id.equals(sender))
			throw new IllegalArgumentException("Users may not deleted themselves!");
		
		int users = DatabaseHandler.getdb().executeUpdate("UPDATE user SET userpassword = NULL, deleted = true WHERE login_id = '" + login_id + "'");
		if(users < 1)
			throw new IllegalStateException("User not found!");
		
		DatabaseHandler.getdb().executeUpdate("DELETE FROM setting WHERE useraccount = '" + login_id + "'");
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
				"SELECT login_id, displayname, adminrole FROM user WHERE deleted = false");
		else
			array = DatabaseHandler.getdb().executeQuery(
				"SELECT login_id, displayname, adminrole FROM user WHERE login_id = '" + userId + "' AND deleted = false");
		
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
