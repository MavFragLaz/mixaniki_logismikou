package network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.eclipse.swt.widgets.Display;
import org.json.JSONObject;
 
public class NetworkConnection {
	private static final String SRV_IP		= "127.0.0.1";
	private static final int 	SRV_PORT 	= 9123;
	
	private static NetworkConnection instance = null;
	
	public static NetworkConnection getInstance() {
		if (instance == null)
			instance = new NetworkConnection();
		return instance;
	}
	
	protected NetworkConnection() {}
	
	private IoSession session = null;
	
	public void writeToServer(JSONObject data) {
		this.session.write(data.toString());
	}
	
	public boolean connectToServer() {
		try {
			IoConnector connector = new NioSocketConnector();
			connector.getSessionConfig().setReadBufferSize(2048);
			
			connector.getFilterChain().addLast("logger", new LoggingFilter());
			TextLineCodecFactory fac = new TextLineCodecFactory(Charset.forName("UTF-8"));
			fac.setDecoderMaxLineLength(512*1024); //Allow to receive up to 512kb of data
			connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(fac));
		 
			connector.setHandler(new NetworkConnectionHandler());
			ConnectFuture future = connector.connect(new InetSocketAddress(SRV_IP, SRV_PORT));
			future.awaitUninterruptibly();
		 
			if (!future.isConnected())
				return false;
			
			this.session = future.getSession();
			this.session.getConfig().setUseReadOperation(true);
			//this.session.getCloseFuture().awaitUninterruptibly();
			//System.out.println(session.read().getMessage()); //create write func - read event
			//System.out.println("After Writing");
			//connector.dispose();	
		} finally {}
		return true;
	}
}
