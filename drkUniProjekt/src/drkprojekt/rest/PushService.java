package drkprojekt.rest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import drkprojekt.database.DatabaseHandler;

public class PushService
{
	//Emanuel: APA91bHsJdpaOueOeVmXifChEGH0RPp35I3Qh_RNjvGTb3pqPBDWd3oinQXntIcT7CBXZkK0cESaEmadNya5CFFFOC6LQwo59KiTUcwqVTTrw22q4MUJ_3s
	private static final String SENDERID = "AIzaSyDcavG3GYtXKerQcxDBnUiecBHuqHUlX3U";
	
	public static void sendUnicastMessage(String message, String deviceId) throws SQLException
	{
		String[] singleDevice = new String[1];
		singleDevice[0] = deviceId;
		sendMessage(message, singleDevice);
	}
	
	public static void sendMulticastMessage(String message, String userId) throws SQLException
	{
		JSONArray array = null;
		
		if(userId != null)
			array = DatabaseHandler.getdb().executeQuery("SELECT device_id FROM phonegapid WHERE registereduser = " + userId);
		else
			array = DatabaseHandler.getdb().executeQuery("SELECT device_id FROM phonegapid");
		
		String[] allDevices = new String[array.size()];
		JSONObject json;

		for (int i = 0; i < array.size(); i++)
		{
			json = (JSONObject) array.get(i);
			allDevices[i] = (String) json.get("device_id");
			System.out.println("Iteration " + i + ": " + allDevices[i]);
		}		
		
		sendMessage(message, allDevices);
	}
	
	public static void sendBroadCastMessage(String message) throws SQLException
	{
		sendMulticastMessage(message, null);
	}
	
	private static void sendMessage(String message, String[] deviceId) throws SQLException
	{
		DataOutputStream out = null;
		BufferedReader in = null;
		
		try
		{
			HttpURLConnection connection = connect();
			
			String data = prepareRequestJSON(message, deviceId);

			out = new DataOutputStream (connection.getOutputStream());
			out.writeBytes(data);
			
			//TODO: vermutlich ist die Antwort egal
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
			
			System.out.println("Response: " + response.toString());
		} catch (IOException e)
		{
			e.printStackTrace();
			throw new SQLException("Invalid result for sending a message!");
		}
		finally
		{
			try	{ out.close(); } catch (Exception e) {}
			try	{ in.close(); } catch (Exception e) {}
		}
	}
	
	private static HttpURLConnection connect() throws IOException
	{
		URL googleUrl = new URL("https://gcm-http.googleapis.com/gcm/send");
		
		HttpURLConnection connection = (HttpURLConnection) googleUrl.openConnection();
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "application/json");
	    connection.setRequestProperty("Authorization","key=" + SENDERID); 
	    connection.setDoOutput(true);
	    connection.setUseCaches(false);
	    
	    return connection;
	}
	
	private static String prepareRequestJSON(String message, String[] deviceId)
	{
		JSONObject request = new JSONObject();
		JSONObject data = new JSONObject();
		
		data.put("message", message);
		data.put("title", message);
		
		if(deviceId.length == 1)
			request.put("to", deviceId[0]);
		else
		{
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			for (int i = 0; i < deviceId.length; i++)
			{
				if(i != 0)
					sb.append(",");
				sb.append("\"" + deviceId[i] + "\"");
			}
			sb.append("]");
			request.put("registration_ids", sb);
		}
		request.put("data", data);
		//TODO: Collapse-Key festlegen
		//request.put("collapse_key", "DRK-Alarm"); 
		//request.put("delay_while_idle", true);
		
		System.out.println("Request: " + request.toJSONString());
		
		return request.toJSONString();
	}
}
