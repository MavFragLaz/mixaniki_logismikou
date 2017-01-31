package tests;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import models.User;
import sqlinterface.SqlInterface;

public class AllTests {

	SqlInterface sqli;
	
	public AllTests() {
		 sqli = new SqlInterface();
	}
	
	@Test
	public void testNewAnnouncement() {
		assertTrue(sqli.newAnnouncement("admin", "testing"));
	}

	@Test
	public void testAddRoute() {
		assertTrue(sqli.addRoute("ATHENS", "THESSALONIKI", "2017-01-10 20:16:12", "2017-01-11 20:16:12", "25", "15", "433.5"));
	}

	@Test
	public void testSaveUser() {
		User a = new User();
		a.setIsAdmin(false);
		a.setPassword("test");
		a.setUsername("testuser");
		a.setLastLogin("127.0.0.1");
		assertTrue(a.save());
	}

	@Test
	public void testNewTicket() {
		assertTrue(sqli.newTicket("1", "a", "b", "23", "5"));
	}

	@Test
	public void testGetAnnouncements() {
		JSONObject ann = sqli.getAnnouncements();
		try {
			ann.getJSONArray("announcements");
			assertTrue(true);
		} catch (JSONException e) {
			assertFalse(true);
		}
	}

	@Test
	public void testGetRoutes() {
		JSONObject r = sqli.getRoutes();
		try {
			r.getJSONArray("routes");
			assertTrue(true);
		} catch (JSONException e) {
			assertFalse(true);
		}
	}

	@Test
	public void testGetUser() {
		assertTrue(sqli.getUser("admin") != null);
	}

	@Test
	public void testConn() {
		assertTrue(sqli.isConnected());
	}

	@Test
	public void testGetConnection() {
		try {
			assertTrue(sqli.getConnection() != null);
		} catch (SQLException e) {
			assertFalse(true);
		}
	}

	@Test
	public void testExecuteUpdate() {
		try {
			assertTrue(sqli.executeUpdate("UPDATE users SET user = 'admin' where user = 'admin';"));
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}

	@Test
	public void testIsDbOperational() {
		assertTrue(sqli.isDbOperational());
	}

}
