package server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Map;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import org.json.*;

import models.User;
import network.Packets;
import sqlinterface.SqlInterface;

public class ServerHandler extends IoHandlerAdapter {
	static final String MSG_INVALIDCLIENT = "Invalid Client! Possible hacking attempt. IP: %s";
	static final String MSG_TERMINATION   = "Client connection terminated.";
	
	static final String ERR_USERPASS		= "Wrong username or password.";
		
	@Override
	public void sessionCreated(IoSession session) {
		
	}
	
    @Override
    public void exceptionCaught( IoSession session, Throwable cause ) throws Exception {
        cause.printStackTrace();
    }

    private String getClientIp(IoSession session) {
    	InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
    	String clientIp = socketAddress.getHostString();
    	return clientIp;
    }
    
	@Override
    public void messageReceived( IoSession session, Object message ) throws Exception {
        String str = message.toString();
        str = str.trim();
        JSONObject msg = null;
        
        try {
        	msg = new JSONObject(str);
	        String action = msg.getString(Packets.PCK_ACTION);
	        //{"action":"login","data":{"pass":"admin","user":"admin"}}
	        JSONObject data = msg.getJSONObject("data");
	        try {
		        switch (action) {
		        	case Packets.ACT_LOGIN:
	        			this.handleLoginAction(data, session);
	        			//this.handleGetRoutesAction(session);
		        		break;
		        	case Packets.ACT_NEWROUTE:
		        		this.handleNewRouteAction(data, session);
		        		break;
		        	case Packets.ACT_LISTROUTE:
		        		this.handleGetRoutesAction(session);
		        		break;
		        	case Packets.ACT_NEWANNOUNCE:
		        		this.handleNewAnnouncement(data, session);
		        		break;
		        	case Packets.ACT_LISTANNOUNCE:
		        		this.handleGetAnnouncementsAction(session);
		        		break;
		        	case Packets.ACT_NEWTICKET:
		        		this.handleNewTicketAction(data, session);
		        		break;
	        		default:
	        			return;
		        }
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
        } catch (JSONException e) {
        	//Not valid JSON, not our client!
        	System.out.format(MSG_INVALIDCLIENT, this.getClientIp(session));
        	session.closeOnFlush(); //Kick him out.
        	return;
        }
    }
	
	private void handleNewTicketAction(JSONObject data, IoSession session) {
		User user = (User)session.getAttribute("userObject");
		if (user != null) {
			JSONObject r = new JSONObject();
			r.put(Packets.PCK_ACTION, Packets.ACT_NEWTICKET);
			r.put(Packets.PCK_STATUS, Packets.PCK_SNOK);
			SqlInterface sqli = new SqlInterface();	
			if (sqli.newTicket(data.getString("route_id"),
								data.getString("firstname"),
								data.getString("lastname"),
								data.getString("age"),
								data.getString("seat_number"))) {
				
				this.handleMassGetRoutesAction(session);	
			}
		}
	}
	
	private void handleNewAnnouncement(JSONObject data, IoSession session) {
		User user = (User)session.getAttribute("userObject");
		if (user != null && user.getIsAdmin()) { //only admins can announce
			JSONObject r = new JSONObject();
			r.put(Packets.PCK_ACTION, Packets.ACT_NEWANNOUNCE);
			r.put(Packets.PCK_STATUS, Packets.PCK_SNOK);
			SqlInterface sqli = new SqlInterface();	
			if (sqli.newAnnouncement(user.getUsername(), data.getString(Packets.PAR_ANNOUNCEMENT))) {
				r.put(Packets.PCK_STATUS, Packets.PCK_SOK);
				session.write(r.toString());
				this.handleMassGetAnnouncementsAction(session);
				return;
			}
			session.write(r.toString());
		}
	}
	
	private void handleGetRoutesAction(IoSession session) {
		User user = (User)session.getAttribute("userObject");
		if (user != null) { //we don't wanna send info to unathenticated users
			SqlInterface sqli = new SqlInterface();	
			JSONObject routes = sqli.getRoutes();
			if (routes.getJSONArray("routes").length() > 0) {
				routes.put(Packets.PCK_ACTION, Packets.ACT_LISTROUTE);
				routes.put(Packets.PCK_STATUS, Packets.PCK_SOK);
				session.write(routes.toString());
			}
		}
	}
	
	private void handleGetAnnouncementsAction(IoSession session) {
		User user = (User)session.getAttribute("userObject");
		if (user != null) { //we don't wanna send info to unathenticated users
			SqlInterface sqli = new SqlInterface();	
			JSONObject announcements = sqli.getAnnouncements();
			if (announcements.getJSONArray("announcements").length() > 0) {
				announcements.put(Packets.PCK_ACTION, Packets.ACT_LISTANNOUNCE);
				announcements.put(Packets.PCK_STATUS, Packets.PCK_SOK);
				session.write(announcements.toString());
			}
		}
	}
	
	private void handleMassGetAnnouncementsAction(IoSession session) {
		Map<Long, IoSession> sessions = Server.getManagedSessions();
		for (Map.Entry<Long, IoSession> entry : sessions.entrySet()) {
			this.handleGetAnnouncementsAction(entry.getValue());
		}
	}
	
	private void handleMassGetRoutesAction(IoSession session) {
		Map<Long, IoSession> sessions = Server.getManagedSessions();
		for (Map.Entry<Long, IoSession> entry : sessions.entrySet()) {
			this.handleGetRoutesAction(entry.getValue());
		}
	}
	
	private void handleNewRouteAction(JSONObject data, IoSession session) {
		User user = (User)session.getAttribute("userObject");
		JSONObject r = new JSONObject();
		r.put(Packets.PCK_ACTION, Packets.ACT_NEWROUTE);
		r.put(Packets.PCK_STATUS, Packets.PCK_SNOK);
		if (user != null && user.getIsAdmin()) { //only admins can add routes, we don't want to get hacked by packet forging
			SqlInterface sqli = new SqlInterface();
			if (sqli.isConnected()) {
				if (sqli.addRoute(data.getString("loc_src"), data.getString("loc_dst"), data.getString("time_dep"), data.getString("time_arr"), data.getString("seats_total"), data.getString("bus_no"), data.getString("seat_price"))) {
					r.put(Packets.PCK_STATUS, Packets.PCK_SOK);
					session.write(r.toString());
					this.handleMassGetRoutesAction(session);
				}
			}
			//false;
		}
	}
	
	private void handleLoginAction(JSONObject data, IoSession session) {
		String username = data.getString("user");
		String password = data.getString("pass");
		User newUser = new User();
		String err = "";
		JSONObject r = new JSONObject();
		r.put(Packets.PCK_ACTION, Packets.ACT_LOGIN);
		r.put(Packets.PCK_STATUS, Packets.PCK_SNOK);
		if (newUser.load(username)) {
			if (newUser.authenticate(password)) {
				newUser.setLastLogin(this.getClientIp(session));
				session.setAttribute("userObject", newUser);
				r.put(Packets.PCK_STATUS, Packets.PCK_SOK);
				JSONObject rData = new JSONObject();
				rData.put("isAdmin", newUser.getIsAdmin() ? "1" : "0");
				rData.put("login_data", newUser.getLoginData());
				r.put(Packets.PCK_DATA, rData);
			} else
				err = ERR_USERPASS;
		} else
			err = ERR_USERPASS;
		r.put(Packets.PCK_ERR, err);
		session.write(r.toString());
	}
		

    @Override
    public void sessionIdle( IoSession session, IdleStatus status ) throws Exception {
        //System.out.println( "IDLE " + session.getIdleCount( status ));
    }
    
    
}