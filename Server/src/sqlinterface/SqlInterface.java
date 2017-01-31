package sqlinterface;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

public class SqlInterface {
	/** CONFIGURATION -- EDIT BELOW THIS LINE **/
	private final String db_user = "ticketserver";
	private final String db_pass = "UX2NC;kPp&&98Shj";
	private final String db_name = "ticket_system";
	private final String db_addr = "192.168.1.7";
	private final String db_port = "3306";
	/** CONFIGURATION -- STOP EDITING HERE **/
	
	static final String ERR_DBCORRUPT 		= "Database is corrupted. Program cannot continue.";
	static final String ERR_DBEMPTY		 	= "Database is empty. Constructing database now.";
	static final String ERR_TBMISS			= "Missing table: %s";
	static final String MSG_QUERYRUN		= "Running Query: %s";
	static final String MSG_DBOK			= "Database status OK.";
	static final String ERR_DBERR 			= "An error occured while connecting to the database. Please check the relevant settings and the SQL Service.";
	static final String ERR_SAVEUSER		= "An error occured while saving a user to the database. User ID: %s";
	static final String ERR_SAVEROUTE		= "An error occured while saving a route to the database.";
	static final String ERR_SAVEANNOUNCE	= "An error occured while saving an announcement to the database.";
	static final String SQL_CHECKIFEXISTS 	= "SELECT 1 FROM %s LIMIT 1;";
	
	static final String SQL_GETUSER			= "SELECT * from users where user = '%s'";
	static final String SQL_UPDATEUSER		= "UPDATE users SET user = '%s', pass = '%s', isAdmin = %d, last_login = '%s', login_data = '%s' where id = %d;";
	static final String SQL_INSERTUSER		= "INSERT INTO users (user, pass, isAdmin) VALUES ('%s', '%s', %d);";
	static final String SQL_INSERTROUTE		= "INSERT INTO routes (loc_src, loc_dst, time_dep, time_arr, seats_total, bus_no, seat_price) VALUES ('%s', '%s', '%s', '%s', '%s', %s, %s);";
	static final String SQL_GETROUTES		= "SELECT * from routes";
	static final String SQL_GETTICKETSROUTE = "SELECT * from tickets where route_id = %d";
	static final String SQL_INSERTANNOUNCE	= "INSERT INTO announcements (issued_by_user, issued_time, text) VALUES ('%s', NOW(), '%s');";
	static final String SQL_GETANNOUNCEMENTS = "SELECT * from announcements";
	static final String SQL_INSERTTICKET	= "INSERT INTO tickets (route_id, firstname, lastname, age, seat_number) VALUES (%s, '%s', '%s', %s, %s);";
	private Connection sqlConn = null;
	private boolean isConnected = false;
	
	private static final List<String> db_tables = new ArrayList<String>(Arrays.asList(
			"users",
			"announcements",
			"routes",
			"tickets"));
	
	private static final List<String> db_constructor_queries = new ArrayList<String>(Arrays.asList(
		"CREATE TABLE users (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, user VARCHAR(255) NOT NULL, pass TEXT NOT NULL, isAdmin BOOL DEFAULT 0  NOT NULL, last_login DATETIME, login_data TEXT);",
		"ALTER TABLE users COMMENT = 'Ticket System Users';",
		"CREATE TABLE announcements (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, issued_by_user VARCHAR(255) NOT NULL, issued_time DATETIME NOT NULL, text TEXT NOT NULL);",
		"ALTER TABLE announcements COMMENT = 'System Announcements';",
		"CREATE TABLE routes (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, bus_no INT NOT NULL, seats_total INT NOT NULL, seat_price DOUBLE NOT NULL, time_dep DATETIME NOT NULL, time_arr DATETIME NOT NULL, loc_src TEXT NOT NULL, loc_dst TEXT NOT NULL);",
		"ALTER TABLE routes COMMENT = 'The Bus Routes';",
		"CREATE TABLE tickets(id INT PRIMARY KEY NOT NULL AUTO_INCREMENT, route_id INT NOT NULL COMMENT 'The bus route id', firstname VARCHAR(255) NOT NULL, lastname VARCHAR(255) NOT NULL, age INT NOT NULL, seat_number INT NOT NULL);",
		"ALTER TABLE tickets COMMENT = 'The tickets';",
		"INSERT INTO users (user, pass, isAdmin, last_login, login_data) VALUES ('admin', '9F1460E6EDEDDE7CA6D3274DC473C244:DEFAULTSALT', 1, null, null);"
	));
	
	public SqlInterface() {
		try {
			this.sqlConn = this.getConnection();
			this.isConnected = true;
		} catch (SQLException e) {
			System.out.println(ERR_DBERR);
			e.printStackTrace();
		}
	}
	
	public boolean newAnnouncement(String user, String announceText) {
		try {
			this.executeUpdate(String.format(SQL_INSERTANNOUNCE, user, announceText));
			return true;
		} catch (SQLException e) {
			System.out.println(ERR_SAVEANNOUNCE);
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean addRoute(String loc_src, String loc_dst, String time_dep, String time_arr, String seats_total, String bus_no, String seat_price) {
		try {
			this.executeUpdate(String.format(SQL_INSERTROUTE, loc_src, loc_dst, time_dep, time_arr, seats_total, bus_no, seat_price));
			return true;
		} catch (SQLException e) {
			System.out.println(ERR_SAVEROUTE);
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean saveUser(int id, String username, String password, boolean isAdmin, String last_login, String login_data) {
		try {
			if (id == 0)
				this.executeUpdate(String.format(SQL_INSERTUSER, username, password, isAdmin ? 1 : 0));
			else
				this.executeUpdate(String.format(SQL_UPDATEUSER, username, password, isAdmin ? 1 : 0, last_login, login_data, id));
			return true;
		} catch (SQLException e) {
			System.out.println(String.format(ERR_SAVEUSER, id));
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean newTicket(String route_id, String firstname, String lastname, String age, String seat_number) {
		try {
			this.executeUpdate(String.format(SQL_INSERTTICKET, route_id, firstname, lastname, age, seat_number));
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public JSONObject getAnnouncements() {
		JSONObject announcements = new JSONObject();
		try {
			Statement stmt = null;
			stmt = this.sqlConn.createStatement();
	        ResultSet res = stmt.executeQuery(SQL_GETANNOUNCEMENTS);
	        JSONArray arr = new JSONArray();
	        while (res.next()) {
	        	JSONObject r = new JSONObject();
	        	r.put("user", res.getString("issued_by_user"));
	        	r.put("time", res.getString("issued_time"));
	        	r.put("text", res.getString("text"));
		        arr.put(r);
	        }
	        announcements.put("announcements", arr);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return announcements;
	}
	
	public JSONObject getRoutes() {
		JSONObject routes = new JSONObject();
		try {
			Statement stmt = null;
			stmt = this.sqlConn.createStatement();
	        ResultSet res = stmt.executeQuery(SQL_GETROUTES);
	        JSONArray arr = new JSONArray();
	        while (res.next()) {
	        	JSONObject r = new JSONObject();
	        	r.put("id", res.getString("id"));
	        	r.put("loc_src", res.getString("loc_src"));
	        	r.put("loc_dst", res.getString("loc_dst"));
	        	r.put("time_dep", res.getString("time_dep"));
	        	r.put("time_arr", res.getString("time_arr"));
	        	r.put("seats_total", res.getString("seats_total"));
	        	r.put("bus_no", res.getString("bus_no"));
	        	r.put("seat_price", res.getString("seat_price"));
	        	
	        	Statement resultStatement = this.sqlConn.createStatement();
		        ResultSet ticketResult = resultStatement.executeQuery(String.format(SQL_GETTICKETSROUTE, Integer.parseInt(res.getString("id"))));
		        JSONArray tickets = new JSONArray();
		        while (ticketResult.next()) {
		        	JSONObject ticket = new JSONObject();
		        	ticket.put("id", ticketResult.getString("id"));
		        	ticket.put("route_id", ticketResult.getString("route_id"));
		        	ticket.put("firstname", ticketResult.getString("firstname"));
		        	ticket.put("lastname", ticketResult.getString("lastname"));
		        	ticket.put("age", ticketResult.getString("age"));
		        	ticket.put("seat_number", ticketResult.getString("seat_number"));
		        	tickets.put(ticket);
		        }
		        r.put("tickets", tickets);
		        arr.put(r);
	        }
	        routes.put("routes", arr);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return routes;
	}
	
	public HashMap<String, String> getUser(String username) {
		Statement stmt = null;
	    try {
	        stmt = this.sqlConn.createStatement();
	        ResultSet res = stmt.executeQuery(String.format(SQL_GETUSER, username));
	        if (res.next()) {
	        	HashMap<String, String> user = new HashMap<String, String>();
	        	user.put("id", res.getString("id"));
	        	user.put("user", res.getString("user"));
	        	user.put("pass", res.getString("pass"));
	        	user.put("isAdmin", res.getString("isAdmin"));
	        	user.put("last_login", res.getString("last_login"));
	        	user.put("login_data", res.getString("login_data"));
	        	return user;
	        }
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    }
	    return null;
	}
	
	public boolean isConnected() {
		return this.isConnected;
	}
	
	public void close() {
		if (this.sqlConn != null) {
			try {
				this.sqlConn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Get a new database connection
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.db_user);
		connectionProps.put("password", this.db_pass);
		conn = DriverManager.getConnection("jdbc:mysql://" + this.db_addr + ":" + this.db_port + "/" + this.db_name, connectionProps);
		return conn;
	}
	
	/**
	 * @throws SQLException If something goes wrong
	 */
	public boolean executeUpdate(String command) throws SQLException {
	    Statement stmt = null;
        stmt = this.sqlConn.createStatement();
        stmt.executeUpdate(command);
        return true;
	}
	
	protected boolean setupDatabase() {
		for (Iterator<String> i = SqlInterface.db_constructor_queries.iterator(); i.hasNext();) {
			String query = i.next();
			try {
				Statement stmt = null;
				stmt = sqlConn.createStatement();
				System.out.println(String.format(SqlInterface.MSG_QUERYRUN, query));
				stmt.execute(query);
			} catch (SQLException e) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean isTableExist(String table) {
		Statement stmt = null;
		try {
			stmt = sqlConn.createStatement();
			stmt.execute(String.format(SqlInterface.SQL_CHECKIFEXISTS, table));
		} catch (SQLException e) {
			System.out.println(String.format(SqlInterface.ERR_TBMISS, table));
			return false;
		}
		return true;
	}
	
	public boolean isDbOperational() {
		List<String> missingTables = new ArrayList<String>();
		for (Iterator<String> i = SqlInterface.db_tables.iterator(); i.hasNext();) {
			String table = i.next();
			if (!this.isTableExist(table))
				missingTables.add(table);
		}
		if (missingTables.size() == SqlInterface.db_tables.size()) {
			System.out.println(String.format(SqlInterface.ERR_DBEMPTY));
			if (this.setupDatabase()) {
				System.out.println(String.format(SqlInterface.MSG_DBOK));
				return true;
			} else
				System.out.println(String.format(SqlInterface.ERR_DBCORRUPT));
		} else if (missingTables.size() == 0) {
			System.out.println(String.format(SqlInterface.MSG_DBOK));
			return true;
		} else
			System.out.println(String.format(SqlInterface.ERR_DBCORRUPT));
		return false;
	}
}
