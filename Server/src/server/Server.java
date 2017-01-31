package server;

import sqlinterface.SqlInterface;
import server.ServerHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class Server {
	static String MSG_SERVERSTART 	= "Starting Server...";
	static String MSG_DIVIDER 		= "------------------------------";
	static String MSG_SETTINGS 		= "Settings list";
	
	static String MSG_LISTENING 	= "Server UP. Listening on port: %d";
	
    private static final int PORT = 9123;
    
    private static Server instance = null;
    private IoAcceptor acceptor = null;
    
    public static Server getInstance() {
    	if (instance == null)
    		instance = new Server();
    	return instance;
    }
	
	public void startServer() throws IOException {
		System.out.println(MSG_SERVERSTART);
		System.out.println(MSG_SETTINGS);
		SqlInterface sqlConn = null;
		
		sqlConn = new SqlInterface();
		if (sqlConn.isConnected() && sqlConn.isDbOperational()) {
			sqlConn.close();
			acceptor = new NioSocketAcceptor();
	        acceptor.getFilterChain().addLast( "logger", new LoggingFilter() );
	        acceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" ))));
	        acceptor.setHandler( new ServerHandler() );
	        acceptor.getSessionConfig().setReadBufferSize( 2048 );
	        acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
	        acceptor.bind( new InetSocketAddress(PORT) );
	        System.out.println(String.format(Server.MSG_LISTENING, Server.PORT));
	        instance = this;
		}
	}
	
	public static Map<Long, IoSession> getManagedSessions() {
		return getInstance().acceptor.getManagedSessions();
	}
}
