package client.views;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.json.JSONObject;

import network.NetworkConnection;
import network.Packets;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class LoginMain {
	private String ERR_USERPASS = "Wrong username or pasword.";
	
	private static Text tbUsername;
	private static Text tbPassword;
	
	private static LoginMain instance = null;

	public static LoginMain getInstance() {
		if (instance == null)
			instance = new LoginMain();
		return instance;
	}
	
	private Shell shlTicketSystem;
	private Label lbStatus;
	
	public String getStatusText() {
		return lbStatus.getText();
	}
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			   try {
		            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
		                if ("Nimbus".equals(info.getName())) {
		                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
		                    break;
		                }
		            }
		        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
		            //java.util.logging.Logger.getLogger(BusReservation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		        }
			LoginMain window = LoginMain.getInstance();
			window.open();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		shlTicketSystem = new Shell();
		shlTicketSystem.setSize(450, 300);
		shlTicketSystem.setText("Ticket System");
		
		Label label = new Label(shlTicketSystem, SWT.NONE);
		label.setText("Login to the Ticketing system");
		label.setBounds(118, 52, 224, 18);
		
		Label label_1 = new Label(shlTicketSystem, SWT.NONE);
		label_1.setText("Username");
		label_1.setBounds(82, 105, 77, 18);
		
		tbUsername = new Text(shlTicketSystem, SWT.BORDER);
		tbUsername.setBounds(164, 99, 167, 30);
		
		Label label_2 = new Label(shlTicketSystem, SWT.NONE);
		label_2.setText("Password");
		label_2.setBounds(89, 140, 70, 18);
		
		tbPassword = new Text(shlTicketSystem, SWT.BORDER | SWT.PASSWORD);
		tbPassword.setBounds(164, 134, 167, 30);
		
		Button button = new Button(shlTicketSystem, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NetworkConnection conn = NetworkConnection.getInstance();
				JSONObject loginPacket = new JSONObject();
				loginPacket.put("action", Packets.ACT_LOGIN);
				JSONObject loginData = new JSONObject();
				loginData.put("user", tbUsername.getText());
				loginData.put("pass", tbPassword.getText());
				loginPacket.put("data", loginData);
				System.out.println(loginPacket.toString());
				if (conn.connectToServer())
					conn.writeToServer(loginPacket);
			}
		});
		button.setText("Login");
		button.setBounds(164, 169, 117, 35);
		
		lbStatus = new Label(shlTicketSystem, SWT.NONE);
		lbStatus.setBounds(118, 76, 224, 18);

		shlTicketSystem.open();
		shlTicketSystem.layout();
		while (!shlTicketSystem.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void loginReply(JSONObject pck){
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (pck.getString(Packets.PCK_STATUS).equals(Packets.PCK_SOK)) {
					shlTicketSystem.close();
					ClientMain clientMain = ClientMain.getInstance();
					clientMain.setIsAdmin(pck.getJSONObject(Packets.PCK_DATA).getString(Packets.PAR_ISADMIN).equals("1") ? true : false);
					clientMain.setLoginData(pck.getJSONObject(Packets.PCK_DATA).getJSONObject(Packets.PAR_LOGINDATA));
					clientMain.open();
				} else {
					lbStatus.setText(ERR_USERPASS);
				}
			}
		});
	}
}
