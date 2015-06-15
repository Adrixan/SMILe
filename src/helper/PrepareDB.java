package helper;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

// Create user + db: CREATE USER 'smile'@'%' IDENTIFIED BY '***';GRANT USAGE ON *.* TO 'smile'@'%' IDENTIFIED BY '***' WITH MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0;CREATE DATABASE IF NOT EXISTS `smile`;GRANT ALL PRIVILEGES ON `smile`.* TO 'smile'@'%';

public class PrepareDB {

	public static void main(String[] args) {

		Connection connection = null;
		try
		{
			Properties properties = new Properties();
			properties.load(new FileInputStream("smile.properties"));

			Class.forName("com.mysql.jdbc.Driver");
			// create a database connection
			connection = DriverManager.getConnection("jdbc:mysql://" + properties.getProperty("rdbm.host") + "/" + properties.getProperty("rdbm.database") +"?"
					+ "user=" + properties.getProperty("rdbm.user") +"&password=" + properties.getProperty("rdbm.password"));
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			statement.executeUpdate("drop table if exists subscriptions");
			statement.executeUpdate("drop table if exists locations");
			statement.executeUpdate("drop table if exists subscriber");
			
			statement.executeUpdate("drop table if exists CAMEL_MESSAGEPROCESSED");
			statement.executeUpdate("create table subscriber (email varchar(100) PRIMARY KEY)");
			statement.executeUpdate("create table subscriptions (email varchar(100), artist varchar(100), FOREIGN KEY(email) REFERENCES subscriber(email) ON DELETE CASCADE)");
			statement.executeUpdate("create table locations (email varchar(100), location varchar(100), FOREIGN KEY(email) REFERENCES subscriber(email) ON DELETE CASCADE)");
			
			statement.executeUpdate("CREATE TABLE CAMEL_MESSAGEPROCESSED (processorName VARCHAR(255) UNIQUE, messageId VARCHAR(100) PRIMARY KEY, createdAt TIMESTAMP)");
		}
		catch(SQLException e)
		{
			// if the error message is "out of memory", 
			// it probably means no database file is found
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(connection != null)
					connection.close();
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}
	}

}
