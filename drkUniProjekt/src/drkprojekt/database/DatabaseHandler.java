package drkprojekt.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseHandler
{
	public static final String[] SETTINGS = { "car", "gui_highcontrast", "gui_showexpirationdate", 
		"notification_event", "notification_groupchat", "notification_chat", "notification_alert_segv",
		"notification_alert_segs", "notification_alert_sbf", "notification_alert_ov" };
	
	private Connection conn;
	private ArrayList<String> columns;
	private static DatabaseHandler db;
	private static Logger log;
	private static SimpleDateFormat timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private static SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");

	private DatabaseHandler()
	{
		try
		{
			log					= LoggerFactory.getLogger(this.getClass());  
			InitialContext ctx 	= new InitialContext();
			DataSource ds 		= (DataSource) ctx.lookup("java:comp/env/jdbc/DefaultDB");
			conn 				= ds.getConnection();
			columns             = fetchColumns();

			log.info("Databaseconnection successfull ");
			timestamp.setTimeZone(TimeZone.getTimeZone("CET"));

		} catch (Exception e)
		{
			log.error("Databaseconnection failed " + e.getMessage());
		}
	}
	
	public static String getCurrentTimeStamp()
	{
		Date now = new Date();
		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(
				now.getTime());
	
		return timestamp.format(currentTimestamp);
	}

	public static DatabaseHandler getdb()
	{
		if(db == null){
		db = new DatabaseHandler();
		}
		return db;
	}
	
	/**	 * 
	 * @param query The Query to execute
	 * @return An JSONObject containing the result
	 * @throws SQLException 
	 */
	public JSONArray executeQuery(String query) throws SQLException
	{
		tryReOpen();
		log.debug("Executing query:\n {}", query);
		Statement stmt 	= conn.createStatement();
		ResultSet rs 	= stmt.executeQuery(query);
		JSONArray result = rsToJSON(rs);
		closeResources(rs, stmt);
		return result;
	}
	
	/**
	 * Executes a prepared GET-Statement. WARNING im not sure, if this will only work for Strings. Has to be tested
	 * @param query Query to execute ( Using ? as placeholder for Arguments)
	 * @param arguments String array containing the Arguments
	 * @return An JSONObject containing the result
	 * @throws SQLException
	 */
	public JSONArray executeQuery(String query, String[] arguments) throws SQLException
	{
		tryReOpen();
		log.debug("Executing query:\n {}", query);
		PreparedStatement stmt 	= conn.prepareStatement(query);
		for (int i = 0; i < arguments.length; i++) 
		{
			if(arguments[i] == null)
				throw new SQLException("Argument must not be null!");
				
			stmt.setString(i + 1, arguments[i]);
		}
		ResultSet rs 	= stmt.executeQuery();	
		JSONArray result = rsToJSON(rs);
		closeResources(rs, stmt);
		return result;
	}
	
	/**
	 * Executes a prepared GET-Statement. WARNING im not sure, if this will only work for Strings. Has to be tested
	 * @param query Query to execute ( Using ? as placeholder for Arguments)
	 * @param arguments String containing the Argument
	 * @return An JSONObject containing the result
	 * @throws SQLException
	 */
	public JSONArray executeQuery(String query, String argument) throws SQLException
	{
		tryReOpen();
		log.debug("Executing query:\n {} with arguments\n {}", query, argument);
	    if(argument == null)
		throw new SQLException("Argument must not be null!");
	    
	    	PreparedStatement stmt 	= conn.prepareStatement(query);		
		stmt.setString(1, argument);
		
		ResultSet rs 	= stmt.executeQuery();	
		JSONArray result = rsToJSON(rs);
		closeResources(rs, stmt);
		return result;
	}
	
	/**
	 * Executes a prepared UPDATE-Statement. WARNING im not sure, if this will only work for Strings. Has to be tested
	 * @param query Query to execute ( Using ? as placeholder for Arguments)
	 * @param arguments String array containing the Arguments
	 * @return Result int from the DB
	 * @throws SQLException
	 */
	public int executeUpdate(String query, String[] arguments) throws SQLException
	{
		tryReOpen();
		log.debug("Executing query:\n {}", query);
		PreparedStatement stmt 	= conn.prepareStatement(query);
		for (int i = 0; i < arguments.length; i++) 
		{
			if(arguments[i] == null)
				throw new SQLException("Argument must not be null!");
			
			stmt.setString(i + 1, arguments[i]);
		}
		int rowcount = stmt.executeUpdate();
		closeStatement(stmt);
		return rowcount;
	}
	
	/**
	 * Executes a prepared UPDATE-Statement. WARNING im not sure, if this will only work for Strings. Has to be tested
	 * @param query Query to execute ( Using ? as placeholder for Arguments)
	 * @param arguments String containing the Argument
	 * @return Result int from the DB
	 * @throws SQLException
	 */
	public int executeUpdate(String query, String argument) throws SQLException
	{
		tryReOpen();
		log.debug("Executing query:\n {}", query);
	    if(argument == null)
	    	throw new SQLException("Argument must not be null!");
	    
		PreparedStatement stmt 	= conn.prepareStatement(query);		
		stmt.setString(1, argument);		
		int rowcount = stmt.executeUpdate();
		closeStatement(stmt);
		return rowcount;
	}
	/**
	 * Executes a simple UPDATE-Statement.
	 * @param query Statement to execute
	 * @return Numbers of rows affected
	 * @throws SQLException
	 */
	public int executeUpdate(String query) throws SQLException
	{
		tryReOpen();
		log.debug("Executing " + query);
		Statement stmt = conn.createStatement();
		int rowcount = stmt.executeUpdate(query);
		closeStatement(stmt);
		return rowcount;
	}
	
	/**
	 * Executes a an UPDATE-Statement or INSERT-Statement that can be filled in with the data of a JSONObject
	 * Use ? as placeholder for the data to be filled from the JSONObject (2 times for INSERT-Statements, 1 time for UPDATE-Statements)
	 * You may also add attributes to your statement which are not in the JSONObject
	 * Example1: "UPDATE table_name SET ? WHERE where_condition"
	 * Example2: "UPDATE table_name SET some_column = some_value, some_column2 = some_value 2, ? WHERE where_condition"
	 * Example3: "INSERT INTO table_name (?) VALUES (?)"
	 * Example4: "INSERT INTO table_name (some_column, ?) VALUES (some_value, ?)"
	 * @param query Query to execute
	 * @param json JSONObject with all relevant data for the new or changed entry
	 * @return Number of rows affected
	 * @throws SQLException
	 */
	public int executeUpdate(String query, JSONObject json) throws SQLException
	{
		tryReOpen();
		log.debug("Executing query:\n {}", query);
		if(json.size() == 0)
			return 0;
			
		String tmp1 = "";
		String tmp2 = "";
		query = query.replace("?", "placeholder");
		
		for(Iterator iterator = json.keySet().iterator(); iterator.hasNext();)
		{			
			String column = (String) iterator.next();
			if(!columns.contains(column.toUpperCase()))
				throw new SQLException("The column \"" + column + "\" in the given JSON does not exist in the schema! "
						+ "The update has been refused due to security reasons.");			
			
			if(query.toUpperCase().startsWith("UPDATE"))
			{
				tmp1 = tmp1 + column + " = " + "?, ";
				
				if(!iterator.hasNext())
				{
					tmp1 = tmp1.substring(0, (tmp1.length()-2));
				}
			}
			else if(query.toUpperCase().startsWith("INSERT"))
			{
				tmp1 = tmp1 + column + ", ";
				tmp2 = tmp2 + "?, ";
				
				if(!iterator.hasNext())
				{
					tmp1 = tmp1.substring(0, (tmp1.length()-2));
					tmp2 = tmp2.substring(0, (tmp2.length()-2));
				}
			}
			else
				return 0;
		}
		
		log.debug("SQL-String: " + query);
		log.debug("First placeholder replaced with: " + tmp1);
		log.debug("Second placeholder replaced with: " + tmp2);
		
		query = query.replaceFirst("placeholder", tmp1);
		query = query.replaceFirst("placeholder", tmp2);
		
		PreparedStatement stmt 	= conn.prepareStatement(query);
		
		int i = 1;
		for(Iterator iterator = json.keySet().iterator(); iterator.hasNext();)
		{
			String column = (String) iterator.next();
			String value = (String) json.get(column).toString();
			
			stmt.setString(i, value);
			i++;
		}

		int rowcount = stmt.executeUpdate();
		closeStatement(stmt);
		return rowcount;
	}
	
	/**
	 * Executes various SQL statements as a transaction that is executed entirely or not at all
	 * @param statements Array of statements that shall be executed
	 * @param arguments Array of arrays with the arguments - e.g. arguments[1] shall contain the arguments for statement[1]
	 * @return Array of rows affected for each statement
	 * @throws SQLException
	 */
	public int[] executeTransactionUpdate(String[] statements, String[][] arguments) throws SQLException
	{
		//Bsp:
		//statements = { "INSERT INTO A VALUES(...)", "UPDATE B SET ...", "INSERT INTO C VALUES(...)" }
		//arguments[1] = Values to be inserted into A { "Peter", "PeterPW" }
		//arguments[2] = Values to be updated in B
		//arguments[3] = Values to be inserted into C
		PreparedStatement stmt = null;
		int[] affectedValues = new int[arguments.length];
		tryReOpen();

		try
		{
			conn.setAutoCommit(false);

			for (int i = 0; i < statements.length; i++)
			{
				stmt = conn.prepareStatement(statements[i]);

				for (int j = 0; j < arguments[i].length; j++) 
				{
					if(arguments[i][j] == null)
						throw new SQLException("Argument must not be null!");

					stmt.setString(j + 1, arguments[i][j]);
				}

				log.debug("Statement added: " + statements[i]);
				affectedValues[i] = stmt.executeUpdate();
			}

			conn.commit();
			conn.setAutoCommit(true);
		} catch (SQLException e)
		{
			conn.rollback();
			conn.setAutoCommit(true);
			throw e;
		}

		closeStatement(stmt);
		return affectedValues;
	}

	private JSONArray rsToJSON(ResultSet rs) throws SQLException 
	{
		JSONArray jsonarray = new JSONArray();
		
		ResultSetMetaData rsmd 	= rs.getMetaData();
	    int columnCount 		= rsmd.getColumnCount();
	    log.debug("Column Count: " + columnCount);
	    String column;    
		while (rs.next())
		{
			JSONObject row	= new JSONObject();
			for (int index = 1; index <= columnCount; index++) 
			{				
	            column 			= rsmd.getColumnLabel(index).toLowerCase();
	            Object value 	= cleanObject(rs.getObject(column));
	            //log.debug("Class of Column " + column + ": {}" , value.getClass());
	            if(column.length() == 0)
	            {
	            	column	= "column" + index;
	            }
	            row.put(column, value);
	        }
			
			jsonarray.add(row);
		}
		log.debug("Read from Dataabase" + jsonarray);			
		return jsonarray;
	}
	
	private void tryReOpen() throws SQLException
	{
		if(conn.isClosed())
		{
			try
			{
				InitialContext ctx 	= new InitialContext();
				DataSource ds 		= (DataSource) ctx.lookup("java:comp/env/jdbc/DefaultDB");
				conn 				= ds.getConnection();
				columns             = fetchColumns();

				log.info("Database-Reconnection successfull ");

			} catch (Exception e)
			{
				log.error("Databaseconnection failed " + e.getMessage());
			}
		}
	}
	
	private ArrayList<String> fetchColumns() throws SQLException
	{
		ArrayList<String> allColumns = new ArrayList<String>();
		
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getColumns(null, "NEO_8R9GH51R32X9VK9BO9GL2FX5M", null, null);
		
		while (rs.next())
		{
			String tmpName = rs.getString("COLUMN_NAME");
			log.debug("Column: " + tmpName);
			allColumns.add(tmpName.toUpperCase());
		}
		
		return allColumns;
	}
	
	private void closeResources(ResultSet rs, Statement stmt)
	{
		closeResultSet(rs);
		closeStatement(stmt);
	}
	
	private void closeResultSet(ResultSet rs)
	{
		if(rs != null)
		{
			try
			{
				rs.close();
			} catch (SQLException e) {}
		}
	}
	
	private void closeStatement(Statement stmt)
	{
		if(stmt != null)
		{
			try
			{
				stmt.close();
			} catch (SQLException e) {}
		}
	}
	
	/**
	 * Use, if there is a SQL-Datatype, which can not be added as Object, because this would cause an invalid JSON (p.e. Datetime has missing double quotes)
	 * @param in Object to clean
	 * @return JSON-Clean object
	 */
	private Object cleanObject(Object in)
	{		
		if(in instanceof Timestamp)
		{	
			Object out	= null;
			out			= timestamp.format(in);
			return out;
		} else if(in instanceof java.sql.Date)
		{
			Object out = null;
			out = date.format(in);
			return out;
		} else if(in instanceof Byte)
		{
			if((byte) in == (byte) 1)
				return true;
			else
				return false;
		}
		{
			return in;
		}
		
	}
}
