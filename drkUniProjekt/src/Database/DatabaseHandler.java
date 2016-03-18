package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

	
	private String getTimeStamp()
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
