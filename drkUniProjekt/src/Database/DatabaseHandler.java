package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

			testStm = conn.prepareStatement("SELECT * FROM artikel");	

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
			
			//System.out.println("Ausgabe: ");

			while (rs.next())
			{
				tmp = rs.getString(0);
				System.out.println("Read from Dataabase" + tmp);
			}			
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp;
	}
}
