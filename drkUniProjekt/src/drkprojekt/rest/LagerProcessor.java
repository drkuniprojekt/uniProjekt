package drkprojekt.rest;

import java.io.IOException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drkprojekt.auth.AuthHelper;
import drkprojekt.database.DatabaseHandler;

@WebServlet("/lager/*")
public class LagerProcessor extends HttpServlet
{
	private static Logger log = LoggerFactory.getLogger(LagerProcessor.class);
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			String subId = Helper.getSubResource(request, true);
			
			if(subId != null)
			{
				if(subId.equals("expirationdate"))
				{
					JSONArray array = DatabaseHandler.getdb().executeQuery("SELECT DAYS_BETWEEN(CURRENT_TIMESTAMP, "
							+ "MIN(expirationdate)) AS days FROM storage");
					
					log.debug("Current_time: " + DatabaseHandler.getdb().executeQuery("SELECT CURRENT_TIMESTAMP FROM dummy").toString());
					log.debug("Expirationdate: " + DatabaseHandler.getdb().executeQuery("SELECT MIN(expirationdate) FROM storage").toString());
					
					if(array.isEmpty())
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					
					JSONObject answer = (JSONObject) array.get(0);
					Helper.setResponseJSON(response, answer);
					
				}
				else
					throw new NoSuchElementException("Ressource not found!");
			}	
			else
			{

				String sy = request.getParameter("year");
				String sm = request.getParameter("month");

				if(sy == null ^ sm == null)
					throw new IllegalArgumentException("Parameters not completely supplied!");

				if(sy != null & sm != null)
				{
					int startYear = Integer.parseInt(sy);
					int startMonth = Integer.parseInt(sm);

					if(startMonth < 1 || startMonth > 12)
						throw new IllegalArgumentException("Month is invalid!");

					String param1 = startYear + "-" + startMonth + "-01";
					String param2;

					switch (startMonth)
					{
					case 11:
						param2 = (startYear+1) + "-01-01";
						break;
					case 12:
						param2 = (startYear+1) + "-02-01";
					default:
						param2 = startYear + "-" + (startMonth+2) + "-01";
						break;
					}

					String arguments[] = { param1, param2 };

					JSONArray array = DatabaseHandler.getdb().executeQuery(
							"SELECT * FROM storage WHERE expirationdate >= ? AND expirationdate < ? ORDER BY expirationdate", arguments);

					if(array.isEmpty())
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					Helper.setResponseJSONArray(response, array);
				}
				else
				{
					JSONArray array = DatabaseHandler.getdb().executeQuery("SELECT * FROM storage ORDER BY expirationdate");

					if(array.isEmpty())
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					Helper.setResponseJSONArray(response, array);
				}
			}
		} catch (SQLException | IllegalArgumentException | NoSuchElementException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			AuthHelper.assertIsAdmin(request);
			DatabaseHandler.getdb().executeUpdate(
					"INSERT INTO storage (item_id, ?) VALUES (item_id.NEXTVAL, ?)", Helper.getRequestJSON(request));
			
		} catch (SQLException | ParseException | SignatureException e)
		{
			Helper.handleException(e, response);
		}
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{	
		try
		{
			AuthHelper.assertIsAdmin(request);
			
			int eventId = Helper.getSubResourceID(request, false);
			int rows = DatabaseHandler.getdb().executeUpdate(
					"UPDATE storage SET ? WHERE item_id = " + eventId, Helper.getRequestJSON(request));
			
			if(rows == 0)
				throw new IllegalStateException("The desired storage item was not found!");
		} catch (ParseException | SQLException | SignatureException | IllegalStateException | NoSuchElementException e)
		{
			Helper.handleException(e, response);
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{	
			AuthHelper.assertIsAdmin(request);
			
			int eventId = Helper.getSubResourceID(request, false);
			int rows = DatabaseHandler.getdb().executeUpdate(
					"DELETE FROM storage WHERE item_id = ?", eventId + "");

			if(rows == 0)
				throw new IllegalStateException("The desired storage item was not found!");
		} catch (IllegalStateException | SQLException | SignatureException | NoSuchElementException e)
		{
			Helper.handleException(e, response);
		}
	}
}
