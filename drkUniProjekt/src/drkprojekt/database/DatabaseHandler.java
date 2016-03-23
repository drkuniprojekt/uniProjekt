package drkprojekt.database;

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
		PreparedStatement stmt 	= conn.prepareStatement(query);
		for (int i = 0; i < arguments.length; i++) 
		{
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
		PreparedStatement stmt 	= conn.prepareStatement(query);
		for (int i = 0; i < arguments.length; i++) 
		{
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
		System.out.println("Executing " + query);
		Statement stmt = conn.createStatement();
		int rowcount = stmt.executeUpdate(query);
		closeStatement(stmt);
		return rowcount;
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
		if(json.size() == 0)
			return 0;
		
		Statement stmt 	= conn.createStatement();
			
		query = query.toUpperCase();
		String tmp1 = "";
		String tmp2 = "";
		
		for(Iterator iterator = json.keySet().iterator(); iterator.hasNext();)
		{
			String column = (String) iterator.next();
			String value = (String) json.get(column).toString();
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
			{
				closeStatement(stmt);
				return 0;
			}
		}
		
		System.out.println("SQL-String: " + query);
		System.out.println("First '?' replaced with: " + tmp1);
		System.out.println("First '?' replaced with: " + tmp2);
		
		query = query.replace("?", "placeholder");
		query = query.replaceFirst("placeholder", tmp1);
		query = query.replaceFirst("placeholder", tmp2);
		
		int rowcount = stmt.executeUpdate(query);
		closeStatement(stmt);
		return rowcount;
	}

	private JSONArray rsToJSON(ResultSet rs) throws SQLException {
		JSONArray jsonarray = new JSONArray();
		
		ResultSetMetaData rsmd 	= rs.getMetaData();
	    int columnCount 		= rsmd.getColumnCount();
	    System.out.println("Column Count: " + columnCount);
	    String column;    
		while (rs.next())
		{
			JSONObject row	= new JSONObject();
			for (int index = 1; index <= columnCount; index++) {
				System.out.println("Iteration " + index);
	            column 			= rsmd.getColumnName(index).toLowerCase();
	            Object value 	= rs.getObject(column);
	            row.put(column, value);
	            System.out.println("Row changed: " + row);
			}
			
			jsonarray.add(row);
		}
		System.out.println("Read from Dataabase" + jsonarray);			
		return jsonarray;
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
}
