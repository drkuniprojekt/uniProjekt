package drkprojekt.rest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drkprojekt.database.DatabaseHandler;

public class PushService
{
	//Emanuel: APA91bHsJdpaOueOeVmXifChEGH0RPp35I3Qh_RNjvGTb3pqPBDWd3oinQXntIcT7CBXZkK0cESaEmadNya5CFFFOC6LQwo59KiTUcwqVTTrw22q4MUJ_3s
	private static final String SENDERID = "AIzaSyDcavG3GYtXKerQcxDBnUiecBHuqHUlX3U";
	private static Logger log = LoggerFactory.getLogger(PushService.class);
	
	public static final int NOTIFICATION_EVENT = 3;
	public static final int NOTIFICATION_GROUPCHAT = 4;
	public static final int NOTIFICATION_CHAT = 5;
	
	/**
	 * Sends a unicast message to one device, identified by its device ID
	 * @param message Message to send
	 * @param deviceId Phonegap-ID of the message receiver
	 * @param notificationType Type of notification (Use constants of this class!)
	 * @throws SQLException
	 */
	public static void sendUnicastMessage(String message, String deviceId, int notificationType) throws SQLException
	{
		String[] singleDevice = new String[1];
		singleDevice[0] = deviceId;
		sendMessage(message, singleDevice, notificationType);
	}
	
	/**
	 * Sends a multicast message to one special user, identified by his or her user ID
	 * The message is send to all devices registered by the given user
	 * @param message Message to send
	 * @param userId User-Id ot the message receiver
	 * @param notificationType Type of notification (Use constants of this class!)
	 * @throws SQLException
	 */
	public static void sendMulticastMessage(String message, String userId, int notificationType) throws SQLException
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
		
		sendMessage(message, allDevices, notificationType);
	}
	
	/**
	 * Sends a broadcast message to all registered devices
	 * @param message Message to send
	 * @param notificationType Type of notification (Use constants of this class!)
	 * @throws SQLException
	 */
	public static void sendBroadCastMessage(String message, int notificationType) throws SQLException
	{
		sendMulticastMessage(message, null, notificationType);
	}
	
	private static void sendMessage(String message, String[] deviceId, int notificationType) throws SQLException
	{
		DataOutputStream out = null;
		BufferedReader in = null;
		
		String[] targetDeviceId = sortOut(deviceId, notificationType);
		
		try
		{
			HttpURLConnection connection = connect();
			
			String data = prepareRequestJSON(message, targetDeviceId);

			out = new DataOutputStream (connection.getOutputStream());
			out.writeBytes(data);
			log.debug("PushService is sending a message to Google...");
			
			//TODO: vermutlich ist die Antwort egal
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
			
			try
			{
				JSONObject responseJSON = (JSONObject) new JSONParser().parse(response.toString());
				log.debug("Number of successful sent messages: " + responseJSON.get("success"));
				log.debug("Number of failed messages: " + responseJSON.get("failure"));
			}
			catch (Exception e) {}
			
			
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
	
	private static String[] sortOut(String[] devicesBefore, int notificationType) throws SQLException, IllegalStateException
	{
		ArrayList<String> devicesAfter = new ArrayList<String>();
		
		for (int i = 0; i < devicesBefore.length; i++)
		{
			JSONArray tmpArray = DatabaseHandler.getdb().executeQuery("SELECT registereduser FROM phonegapid WHERE device_id = '" + devicesBefore[i] + "'");
			JSONObject tmpObject = (JSONObject) tmpArray.get(0);
			
			try
			{
				tmpArray.get(1);
				throw new IllegalStateException("Inconsistent data in database - More than one user fore given device_id found!");
			} catch(IndexOutOfBoundsException e)
			{
				String correspondingUser = (String) tmpObject.get("registereduser");
				
				JSONArray tmpArray2 = DatabaseHandler.getdb().executeQuery("SELECT settingvalue FROM setting WHERE useraccount = '" + correspondingUser + "' AND setting = '" + DatabaseHandler.SETTINGS[notificationType] + "'");
				JSONObject tmpObject2 = (JSONObject) tmpArray2.get(0);
				
				boolean settingvalue = Boolean.parseBoolean(tmpObject2.get("settingvalue").toString());
				
				if(settingvalue)
				{
					devicesAfter.add(devicesBefore[i]);
				}
			}
		}
		
		String[] returnDevices = devicesAfter.toArray(new String[devicesAfter.size()]);

		return returnDevices;
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
