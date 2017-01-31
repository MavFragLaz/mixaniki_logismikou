package models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sqlinterface.SqlInterface;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class User {
	private int id = 0;
	private String username = "";
	private String password = "";
	private boolean isAdmin = false;
	private String last_login = "";
	private JSONObject login_data = new JSONObject();
	
	static final int MAX_LOGIN_LOG = 10;
	static final String ERR_NOTLOAD = "Could not load User object with username: %d. Stacktrace follows";
	
	public User() {}
	
	public boolean load(String username) {
		SqlInterface resource = new SqlInterface();
		HashMap<String, String> res = resource.getUser(username);
		if (res != null) {
			try {
				this.id = Integer.parseInt(res.get("id"));
				this.username = res.get("user");
				this.password = res.get("pass");
				this.isAdmin = res.get("isAdmin").equals("1") ? true : false;
				this.last_login = res.get("last_login") == null ? "" : res.get("last_login");
				this.login_data = res.get("login_data") == null ? new JSONObject("{}") : new JSONObject(res.get("login_data"));
				return true;
			} catch (JSONException e) {
				System.out.println(String.format(ERR_NOTLOAD, username));
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean save() {
		SqlInterface resource = new SqlInterface();
		if (resource.isConnected()) {
			if (resource.saveUser(this.id, this.username, this.password, this.isAdmin, this.last_login, this.login_data.toString()))
				return true;
		}
		return false;
	}
	
	public int getId() { return this.id; }
	public String getUsername() { return this.username; }
	public String getPassword() { return this.password; }
	public boolean getIsAdmin() { return this.isAdmin; }
	public String getLastLogin() { return this.last_login; }
	public JSONObject getLoginData() { return this.login_data; }
	
	public void setUsername(String username) { this.username = username; }
	
	public void setPassword(String password) {
		SecureRandom random = new SecureRandom();
		String newSalt = new BigInteger(130, random).toString(32).toUpperCase();
		this.password = this.hashPassword(this.hashPassword(password) + newSalt) + ":" + newSalt;
	}
	
	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
	public void setLastLogin(String ip_address) {
		java.util.Date dt = new java.util.Date();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.last_login = sdf.format(dt);
		
		JSONObject lastSession = new JSONObject();
		lastSession.put("ip", ip_address);
		lastSession.put("time", this.last_login);
		JSONArray arr = null;
		try {
			arr = (JSONArray)this.login_data.get("logins");
		} catch (NullPointerException | JSONException e) {}
		if (arr == null)
			arr = new JSONArray();
		else if (arr.length() >= MAX_LOGIN_LOG)	
			arr.remove(0);
		arr.put(lastSession);
		this.login_data.put("logins", arr);
		this.save();
	}
	
	public boolean authenticate(String password) {
		if (this.id != 0) {
			String[] passwordParts = this.password.split(":");
			String realPassword = passwordParts[0];
			String salt = passwordParts[1];
			String cmpPassword = this.hashPassword(this.hashPassword(password).toUpperCase() + salt).toUpperCase();
			if (realPassword.equals(cmpPassword))
				return true;
		}
		return false;
	}
	
	/** Credit: http://www.asjava.com/core-java/java-md5-example/ **/
	protected String hashPassword(String password) {
		byte[] source;
        try {
            //Get byte according by specified coding.
            source = password.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            source = password.getBytes();
        }
        String result = "";
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source);
            //The result should be one 128 integer
            byte temp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = temp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            result = new String(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
	}
}
