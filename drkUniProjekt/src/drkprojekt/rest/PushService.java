package drkprojekt.rest;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;


public class PushService
{
	private static final String SENDERID = "AIzaSyDcavG3GYtXKerQcxDBnUiecBHuqHUlX3U RegisterID: APA91bHfra32RNkNX7cjoqPC84lplF7m4Dl2yi1YeEwoGtIvQY6tAprUZWpPxg7hbgq0T0FKgbGceN4rQgRqwc2uvYxUDP24NJ_y0pTUL-erHgBHEj5liGxFjDTaHzWW07eUKlakJiMC";
	
	public static void sendUnicastMessage(String message, String deviceId)
	{
		List<String> androidTargets = new ArrayList<String>();
        androidTargets.add(deviceId);
        sendMessage(message, androidTargets);
	}
	
	public static void sendBroadCastMessage(String message)
	{
		List<String> androidTargets = new ArrayList<String>();
		//TODO: Aus DB alle Geräte-IDs auslesen, folgende Code-Zeile entfernen
		sendMessage(message, androidTargets);
	}
	
	private static void sendMessage(String message, List<String> androidTargets)
	{
		String collapseKey = "Alarm-Nachricht";
		 
        // Instance of com.android.gcm.server.Sender, that does the
        // transmission of a Message to the Google Cloud Messaging service.
        Sender senderObject = new Sender(SENDERID);
         
        // This Message object will hold the data that is being transmitted
        // to the Android client devices.  For this demo, it is a simple text
        // string, but could certainly be a JSON object.
        Message messageObject = new Message.Builder()
        
        // If multiple messages are sent using the same .collapseKey()
        // the android target device, if it was offline during earlier message
        // transmissions, will only receive the latest message for that key when
        // it goes back on-line.
        .collapseKey(collapseKey)
        .timeToLive(30)
        .delayWhileIdle(true)
        .addData("message", message)
        .build();
        
        try {
            // use this for multicast messages.  The second parameter
            // of senderObject.send() will need to be an array of register ids.
            MulticastResult result = senderObject.send(messageObject, androidTargets, 3);
             
            if (result.getResults() != null) {
                int canonicalRegId = result.getCanonicalIds();
                if (canonicalRegId != 0) {
                     
                }
            } else {
                int error = result.getFailure();
                System.out.println("Broadcast failure: " + error);
            }
             
        } catch (Exception e) {
            e.printStackTrace();
        }
// 
//        // We'll pass the CollapseKey and Message values back to index.jsp, only so
//        // we can display it in our form again.
//        request.setAttribute("CollapseKey", collapseKey);
//        request.setAttribute("Message", message);
//         
//        request.getRequestDispatcher("index.jsp").forward(request, response);
                 
	}
}
