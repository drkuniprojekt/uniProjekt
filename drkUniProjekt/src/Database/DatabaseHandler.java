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

import org.json.simple.JSONObject;

public class DatabaseHandler
{
	Connection conn;
	PreparedStatement testStm;
	
	private static DatabaseHandler db;

	private DatabaseHandler()
	{
		try
		{
			InitialContext ctx = new InitialContext();
			DataSource ds = (DataSource) ctx
					.lookup("java:comp/env/jdbc/DefaultDB");
			conn = ds.getConnection();

			testStm = conn.prepareStatement("SELECT * FROM TESTTABELLE");	

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
	
	public String test()
	{
		String tmp = null;
		try
		{
			ResultSet rs = testStm.executeQuery();	
			
			System.out.println("Ausgabe: ");

			while (rs.next())
			{
				tmp = rs.getString(1);
				System.out.println("Read from Dataabase" + tmp);
			}			
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp;
	}
	public JSONObject JsonTest()
	{
		try 
		{
			return executeQuery("SELECT * FROM TESTTABELLE");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			JSONObject j	= new JSONObject();
			j.put("Error", e);
			return j;
		}
		
	}
	/**
	 * 
	 * @param Query The Query to execute
	 * @return An JSONObject containing thhe result
	 * @throws SQLException 
	 */
	public JSONObject executeQuery(String query) throws SQLException
	{
		Statement stmt 	= conn.createStatement();
		ResultSet rs 	= stmt.executeQuery(query);	
		return rsToJSON(rs);				
		
	}

	private JSONObject rsToJSON(ResultSet rs) throws SQLException {
		JSONObject tmp	= new JSONObject();
		ResultSetMetaData rsmd = rs.getMetaData();
	    int columnCount = rsmd.getColumnCount();
	    String column;    
		while (rs.next())
		{
			for (int index = 1; index <= columnCount; index++) {
	            column = rsmd.getColumnName(1);
	            Object value = rs.getObject(column);
//	            if (value == null) {
//	                tmp.put(column, null);
//	            } else if (value instanceof Integer) {
//	                tmp.put(column, (Integer) value);
//	            } else if (value instanceof String) {
//	                tmp.put(column, (String) value);                
//	            } else if (value instanceof Boolean) {
//	                tmp.put(column, (Boolean) value);           
//	            } else if (value instanceof Date) {
//	                tmp.put(column, ((Date) value).getTime());                
//	            } else if (value instanceof Long) {
//	                tmp.put(column, (Long) value);                
//	            } else if (value instanceof Double) {
//	                tmp.put(column, (Double) value);                
//	            } else if (value instanceof Float) {
//	                tmp.put(column, (Float) value);                
//	            } else if (value instanceof Byte) {
//	                tmp.put(column, (Byte) value);
//	            } else if (value instanceof byte[]) {
//	                tmp.put(column, (byte[]) value);                
//	            } else {
	            	tmp.put(column, value);
//	            }
			}
		}
		System.out.println("Read from Dataabase" + tmp);			
		return tmp;
	}
}
