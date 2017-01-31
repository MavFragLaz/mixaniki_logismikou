package network;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.views.ClientMain;
import client.views.LoginMain;

public class NetworkConnectionHandler extends IoHandlerAdapter {
	private final Logger logger = (Logger) LoggerFactory.getLogger(getClass());
	private boolean finished;
	 
	public NetworkConnectionHandler() {}
	 
	public boolean isFinished()	{
		return finished;
	}
	

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("Connection closed.");
		super.sessionClosed(session);
	}
	 
	@Override
	public void sessionOpened(IoSession session) {
		//session.write(values);
	}
	 
	@Override
	public void messageReceived(IoSession session, Object message) {
		logger.info("Message received in the client..");
		logger.info("Message is: " + message.toString());
		
		try {
			JSONObject msg = new JSONObject(message.toString());
			String action = msg.getString(Packets.PCK_ACTION);
			switch (action) {
				case Packets.ACT_LOGIN: {
					LoginMain.getInstance().loginReply(msg);
					break;
				} case Packets.ACT_LISTROUTE: {
					ClientMain.getInstance().listRouteReply(msg);
					break;
				} case Packets.ACT_LISTANNOUNCE: {
					ClientMain.getInstance().announcementReply(msg);
					break;
				}
			}
		} catch (JSONException e) {
			//Server did not send JSON response..
			e.printStackTrace();
		}
	}
	 
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		cause.printStackTrace();
		session.closeNow();
	}
}
