package Database;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DatabaseHandler
{
	Connection conn;
	
	
	private static DatabaseHandler db;

	private DatabaseHandler()
	{
		try
		{
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx
					.lookup("java:comp/env/jdbc/DefaultDB");
			conn = ds.getConnection();

			System.out.println("Databaseconnection successfull ");

		} catch (Exception e)
		{
			System.err.println("Databaseconnection failed " + e.getMessage());
		}
	}
	
	public static DatabaseHandler getdb()
	{
		if(db == null){
		db = new DatabaseHandler();
		}
		return db;
	}
	
	
	public void closeConnection()
	{
		try
		{
			conn.close();
		} catch (SQLException e)
		{
			System.err.println("Could not close connection");
		}
	}

	
	public String getTimeStamp()
	{
		Date now = new Date();
		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(
				now.getTime());

		return currentTimestamp.toString();
	}
	
	/**	 * 
	 * @param query The Query to execute
	 * @return An JSONObject containing the result
	 * @throws SQLException 
	 */
	public JSONArray executeQuery(String query) throws SQLException
	{
		Statement stmt 	= conn.createStatement();
		ResultSet rs 	= stmt.executeQuery(query);	
		return rsToJSON(rs);			
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
		PreparedStatement stmt 	= conn.prepareStatement(query);
		for (int i = 0; i < arguments.length; i++) 
		{
			stmt.setString(i, arguments[i]);
		}
		ResultSet rs 	= stmt.executeQuery();	
		return rsToJSON(rs);			
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
		PreparedStatement stmt 	= conn.prepareStatement(query);
		for (int i = 0; i < arguments.length; i++) 
		{
			stmt.setString(i, arguments[i]);
		}
		int rs 	= stmt.executeUpdate();	
		return rs;
	}
	
	/**
	 * Executes a simple UPDATE-Statement.
	 * @param query Statement to execute
	 * @return Numbers of rows affected
	 * @throws SQLException
	 */
	public int executeUpdate(String query) throws SQLException
	{
		Statement stmt = conn.createStatement();
		return stmt.executeUpdate(query);
	}
	
	/**
	 * Executes a an UPDATE-Statement or INSERT-Statement that can be filled in with the data of a JSONObject
	 * Use ? as placeholder for the data to be filled from the JSONObject
	 * Example1: "UPDATE table_name SET ? WHERE where_condition"
	 * Example2: "INSERT INTO table_name (?) VALUES (?)"
	 * @param query Querty to execute
	 * @param json JSONObject with all relevant data for the new or changed entry
	 * @return Number of rows affected
	 * @throws SQLException
	 */
	public int executeUpdate(String query, JSONObject json) throws SQLException
	{
		PreparedStatement stmt 	= conn.prepareStatement(query);
		
		if(json.size() == 0)
			return 0;
		
		query = query.toUpperCase();
		String tmp1 = "";
		String tmp2 = "";
		
		for(Iterator iterator = json.keySet().iterator(); iterator.hasNext();)
		{
			String column = (String) iterator.next();
			String value = (String) json.get(column);
			if(query.startsWith("UPDATE"))
			{
				tmp1 = tmp1 + column + " = " + "'" + value + "', ";
				
				if(!iterator.hasNext())
				{
					tmp1 = tmp1.substring(0, (tmp1.length()-2));
				}
			}
			else if(query.startsWith("INSERT"))
			{
				tmp1 = tmp1 + column + ", ";
				tmp2 = tmp2 + "'" + value + "', ";
				
				if(!iterator.hasNext())
				{
					tmp1 = tmp1.substring(0, (tmp1.length()-2));
					tmp2 = tmp2.substring(0, (tmp2.length()-2));
				}
			}
			else
				return 0;
		}
		
		try
		{
			stmt.setString(1, tmp1);
			stmt.setString(2, tmp2);
		} catch (SQLException e)
		{
			//Ignore Exception
		}
		
		int rs 	= stmt.executeUpdate();	
		return rs;	
	}

	private JSONArray rsToJSON(ResultSet rs) throws SQLException {
		JSONArray tmp = new JSONArray();
		
		ResultSetMetaData rsmd 	= rs.getMetaData();
	    int columnCount 		= rsmd.getColumnCount();
	    String column;    
		while (rs.next())
		{
			JSONObject row	= new JSONObject();
			for (int index = 1; index <= columnCount; index++) {
	            column 			= rsmd.getColumnName(1).toLowerCase();
	            Object value 	= rs.getObject(column);
	            row.put(column, value);

			}
			tmp.add(row);
		}
		System.out.println("Read from Dataabase" + tmp);			
		return tmp;
	}
}
