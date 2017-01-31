package client.views;

import java.awt.Event;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.text.TableView.TableRow;

import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import network.NetworkConnection;
import network.Packets;
import sun.net.NetworkClient;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;

public class ClientMain {

	private static ClientMain instance = null;

	public static ClientMain getInstance() {
		if (instance == null)
			instance = new ClientMain();
		return instance;
	}
	
	private boolean isAdmin = false;
	private JSONObject login_data = new JSONObject("{}");
	private JSONObject route_data = new JSONObject();
	private Table tblRoutes;
	private Text tbBusNo;
	private Text tbTicketPrice;
	private Text tbTotalSeats;
	private Text tbDstLoc;
	private Text tbSrcLoc;
	private Text tbFirstname;
	private Text tbLastname;
	private Text tbAge;
	private Table tblAnnouncements;
	private Text tbNewAnnounce;
	
	private String activeRoutedId = "";
	
	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
	public void setLoginData(JSONObject login_data) {
		this.login_data = login_data;
	}
	
	public void setRouteData(JSONObject route_data) {
		this.route_data = route_data;
	}
	
	public void announcementReply(JSONObject msg) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Table tblA = ClientMain.getInstance().tblAnnouncements;
				try {
					Thread.sleep(2000);
					tblA.removeAll();
				} catch (Exception e) {
					e.printStackTrace();
				}
				JSONArray announcements = msg.getJSONArray("announcements");
				for (int i = 0; i < announcements.length(); i++) {
					try {
					    JSONObject announcement = announcements.getJSONObject(i);
						TableItem item = new TableItem(tblA, SWT.NONE);
					    item.setText(new String[] { 
					    		announcement.getString("user"),
					    		announcement.getString("time"),
					    		announcement.getString("text"),
					    });
					} catch (Exception e) {}
				}
			}
		});
	}
	
	public void listRouteReply(JSONObject msg) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ClientMain.getInstance().setRouteData(msg);
				Table tblR = ClientMain.getInstance().tblRoutes;
				try {
					Thread.sleep(2000);
					tblR.removeAll();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				JSONArray routes = msg.getJSONArray("routes");
				for (int i = 0; i < routes.length(); i++) {
					try {
					    JSONObject route = routes.getJSONObject(i);
						TableItem item = new TableItem(tblRoutes, SWT.NONE);
					    item.setText(new String[] { 
					    		route.getString("id"),
					    		route.getString("bus_no"),
					    		route.getString("seat_price"),
					    		route.getString("seats_total"),
					    		route.getString("loc_src"),
					    		route.getString("loc_dst"),
					    		route.getString("time_dep"),
					    		route.getString("time_arr")
					    });
					} catch (Exception e) {}
				}
			}
		});
	}
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ClientMain window = new ClientMain();
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
		Shell shlTicketingSystem = new Shell();
		shlTicketingSystem.setMinimumSize(new Point(1024, 768));
		shlTicketingSystem.setSize(885, 564);
		shlTicketingSystem.setText("Ticketing System");
		shlTicketingSystem.setLayout(null);
		
		TabFolder tabFolder = new TabFolder(shlTicketingSystem, SWT.NONE);
		tabFolder.setBounds(0, 0, 1024, 768);
		
		TabItem tabCashier = new TabItem(tabFolder, SWT.NONE);
		tabCashier.setText("Cashier");
		
		Group group_1 = new Group(tabFolder, SWT.NONE);
		tabCashier.setControl(group_1);
		
		tblRoutes = new Table(group_1, SWT.BORDER | SWT.FULL_SELECTION);
		tblRoutes.setLinesVisible(true);
		tblRoutes.setHeaderVisible(true);
		tblRoutes.setBounds(0, 245, 1024, 240);
		
		TableColumn tblclmnId = new TableColumn(tblRoutes, SWT.NONE);
		tblclmnId.setWidth(100);
		tblclmnId.setText("id");
		
		TableColumn tblclmnBusno = new TableColumn(tblRoutes, SWT.NONE);
		tblclmnBusno.setWidth(100);
		tblclmnBusno.setText("bus_no");
		
		TableColumn tblclmnSeatprice = new TableColumn(tblRoutes, SWT.NONE);
		tblclmnSeatprice.setWidth(100);
		tblclmnSeatprice.setText("seat_price");
		
		TableColumn tblclmnSeatstotal = new TableColumn(tblRoutes, SWT.NONE);
		tblclmnSeatstotal.setWidth(100);
		tblclmnSeatstotal.setText("seats_total");
		
		TableColumn tblclmnLocsrc = new TableColumn(tblRoutes, SWT.NONE);
		tblclmnLocsrc.setWidth(100);
		tblclmnLocsrc.setText("loc_src");
		
		TableColumn tblclmnLocdst = new TableColumn(tblRoutes, SWT.NONE);
		tblclmnLocdst.setWidth(100);
		tblclmnLocdst.setText("loc_dst");
		
		TableColumn tblclmnTimedep = new TableColumn(tblRoutes, SWT.NONE);
		tblclmnTimedep.setWidth(100);
		tblclmnTimedep.setText("time_dep");
		
		TableColumn tblclmnTimearr = new TableColumn(tblRoutes, SWT.NONE);
		tblclmnTimearr.setWidth(100);
		tblclmnTimearr.setText("time_arr");
		
		tbFirstname = new Text(group_1, SWT.BORDER);
		tbFirstname.setBounds(42, 68, 135, 30);
		
		tbLastname = new Text(group_1, SWT.BORDER);
		tbLastname.setBounds(43, 138, 131, 30);
		
		tbAge = new Text(group_1, SWT.BORDER);
		tbAge.setBounds(41, 206, 131, 30);
		
		Combo tbSeats = new Combo(group_1, SWT.NONE);
		tbSeats.setBounds(229, 64, 201, 34);
		
		Label lblNewLabel_1 = new Label(group_1, SWT.NONE);
		lblNewLabel_1.setBounds(68, 44, 77, 18);
		lblNewLabel_1.setText("Όνομα");
		
		Label label = new Label(group_1, SWT.NONE);
		label.setText("Επώνυμο");
		label.setBounds(68, 117, 77, 18);
		
		Label label_1 = new Label(group_1, SWT.NONE);
		label_1.setText("Ηλικία");
		label_1.setBounds(68, 182, 77, 18);
		
		Label label_2 = new Label(group_1, SWT.NONE);
		label_2.setText("Θέση");
		label_2.setBounds(281, 44, 77, 18);
		
		Button button_1 = new Button(group_1, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tbFirstname.getText().isEmpty() || tbLastname.getText().isEmpty() || tbAge.getText().isEmpty() || !ClientMain.isNumber(tbAge.getText()) || tbSeats.getText().isEmpty() || !ClientMain.isNumber(tbSeats.getText())) {
					JOptionPane.showMessageDialog(null,"Τα στοιχεία που εισάγατε δεν είναι έγκυρα", "", JOptionPane.WARNING_MESSAGE);
				} else {
					NetworkConnection net = NetworkConnection.getInstance();
					JSONObject newTicket = new JSONObject();
					newTicket.put(Packets.PCK_ACTION, Packets.ACT_NEWTICKET);
					JSONObject newTicketData = new JSONObject();
					newTicketData.put("route_id", activeRoutedId);
					newTicketData.put("firstname", tbFirstname.getText());
					newTicketData.put("lastname", tbLastname.getText());
					newTicketData.put("age", tbAge.getText());
					newTicketData.put("seat_number", tbSeats.getText());
					newTicket.put("data", newTicketData);
					net.writeToServer(newTicket);
					System.out.println(String.format("NEO ΕΙΣΙΤΗΡΙΟ: %s %s , %s χρονών. Θέση %s στο δρομολόγιο %s", tbFirstname.getText(), tbLastname.getText(), tbAge.getText(), tbSeats.getText(), activeRoutedId));
				}
			}
		});
		button_1.setText("Καταχώρηση κ Εκτύπωση");
		button_1.setBounds(229, 134, 232, 34);
		
		tblAnnouncements = new Table(group_1, SWT.BORDER | SWT.FULL_SELECTION);
		tblAnnouncements.setBounds(0, 488, 1024, 207);
		tblAnnouncements.setHeaderVisible(true);
		tblAnnouncements.setLinesVisible(true);
		
		TableColumn tblclmnUser = new TableColumn(tblAnnouncements, SWT.NONE);
		tblclmnUser.setWidth(100);
		tblclmnUser.setText("user");
		
		TableColumn tblclmnTime = new TableColumn(tblAnnouncements, SWT.NONE);
		tblclmnTime.setWidth(100);
		tblclmnTime.setText("time");
		
		TableColumn tblclmnMessage = new TableColumn(tblAnnouncements, SWT.NONE);
		tblclmnMessage.setWidth(100);
		tblclmnMessage.setText("message");
	
		TabItem tabAdmin = new TabItem(tabFolder, SWT.NONE);
		tabAdmin.setText("Admin");
		
		Group group = new Group(tabFolder, SWT.NONE);
		tabAdmin.setControl(group);
		group.setLayout(new FormLayout());
		
		Group group_2 = new Group(group, SWT.NONE);
		FormData fd_group_2 = new FormData();
		fd_group_2.bottom = new FormAttachment(100);
		fd_group_2.left = new FormAttachment(0);
		fd_group_2.right = new FormAttachment(0, 954);
		fd_group_2.top = new FormAttachment(0, 606);
		group_2.setLayoutData(fd_group_2);
		group_2.setLayout(new FormLayout());
		
		Label lblNewLabel = new Label(group_2, SWT.NONE);
		FormData fd_lblNewLabel = new FormData();
		fd_lblNewLabel.left = new FormAttachment(0);
		fd_lblNewLabel.top = new FormAttachment(0, 10);
		lblNewLabel.setLayoutData(fd_lblNewLabel);
		lblNewLabel.setText("Αρ. Λεωφ");
		
		tbBusNo = new Text(group_2, SWT.BORDER);
		FormData fd_tbBusNo = new FormData();
		fd_tbBusNo.left = new FormAttachment(lblNewLabel, 0, SWT.LEFT);
		tbBusNo.setLayoutData(fd_tbBusNo);
		
		Label lblSeatprice = new Label(group_2, SWT.NONE);
		FormData fd_lblSeatprice = new FormData();
		fd_lblSeatprice.bottom = new FormAttachment(lblNewLabel, 0, SWT.BOTTOM);
		fd_lblSeatprice.left = new FormAttachment(lblNewLabel, 31);
		lblSeatprice.setLayoutData(fd_lblSeatprice);
		lblSeatprice.setText("Τιμή Εισ.");
		
		tbTicketPrice = new Text(group_2, SWT.BORDER);
		fd_tbBusNo.bottom = new FormAttachment(tbTicketPrice, 0, SWT.BOTTOM);
		FormData fd_tbTicketPrice = new FormData();
		fd_tbTicketPrice.top = new FormAttachment(lblSeatprice, 6);
		fd_tbTicketPrice.right = new FormAttachment(lblSeatprice, 0, SWT.RIGHT);
		tbTicketPrice.setLayoutData(fd_tbTicketPrice);
		
		Label lblSeatstotal = new Label(group_2, SWT.NONE);
		FormData fd_lblSeatstotal = new FormData();
		fd_lblSeatstotal.top = new FormAttachment(lblNewLabel, 0, SWT.TOP);
		fd_lblSeatstotal.left = new FormAttachment(lblSeatprice, 30);
		lblSeatstotal.setLayoutData(fd_lblSeatstotal);
		lblSeatstotal.setText("Θέσεις");
		
		tbTotalSeats = new Text(group_2, SWT.BORDER);
		FormData fd_tbTotalSeats = new FormData();
		fd_tbTotalSeats.top = new FormAttachment(lblSeatstotal, 6);
		fd_tbTotalSeats.left = new FormAttachment(tbTicketPrice, 6);
		tbTotalSeats.setLayoutData(fd_tbTotalSeats);
		
		Label lblTimedep = new Label(group_2, SWT.NONE);
		FormData fd_lblTimedep = new FormData();
		fd_lblTimedep.top = new FormAttachment(lblNewLabel, 0, SWT.TOP);
		fd_lblTimedep.left = new FormAttachment(lblSeatstotal, 46);
		lblTimedep.setLayoutData(fd_lblTimedep);
		lblTimedep.setText("Ώρα Αναχώρησης");
		
		Label lblTimearr = new Label(group_2, SWT.NONE);
		FormData fd_lblTimearr = new FormData();
		fd_lblTimearr.bottom = new FormAttachment(lblNewLabel, 0, SWT.BOTTOM);
		fd_lblTimearr.left = new FormAttachment(lblTimedep, 60);
		lblTimearr.setLayoutData(fd_lblTimearr);
		lblTimearr.setText("Ώρα Άφιξης");
		
		Label lblLocsrc = new Label(group_2, SWT.NONE);
		FormData fd_lblLocsrc = new FormData();
		fd_lblLocsrc.top = new FormAttachment(lblNewLabel, 0, SWT.TOP);
		lblLocsrc.setLayoutData(fd_lblLocsrc);
		lblLocsrc.setText("Αφετηρία");
		
		tbSrcLoc = new Text(group_2, SWT.BORDER);
		fd_lblLocsrc.left = new FormAttachment(tbSrcLoc, 0, SWT.LEFT);
		FormData fd_tbSrcLoc = new FormData();
		fd_tbSrcLoc.top = new FormAttachment(lblLocsrc, 6);
		tbSrcLoc.setLayoutData(fd_tbSrcLoc);
		
		Label lblDestination = new Label(group_2, SWT.NONE);
		FormData fd_lblDestination = new FormData();
		fd_lblDestination.top = new FormAttachment(lblNewLabel, 0, SWT.TOP);
		fd_lblDestination.right = new FormAttachment(100, -72);
		lblDestination.setLayoutData(fd_lblDestination);
		lblDestination.setText("Προορισμός");
		
		tbDstLoc = new Text(group_2, SWT.BORDER);
		fd_tbSrcLoc.right = new FormAttachment(tbDstLoc, -29);
		FormData fd_tbDstLoc = new FormData();
		fd_tbDstLoc.top = new FormAttachment(lblDestination, 6);
		fd_tbDstLoc.left = new FormAttachment(lblDestination, 0, SWT.LEFT);
		tbDstLoc.setLayoutData(fd_tbDstLoc);
		
		CDateTime tbDepTime = new CDateTime(group_2, CDT.CLOCK_24_HOUR | CDT.DROP_DOWN);
		tbDepTime.setPattern("yyyy-MM-dd HH:mm:ss");
		FormData fd_tbDepTime = new FormData();
		fd_tbDepTime.top = new FormAttachment(lblTimedep, 6);
		tbDepTime.setLayoutData(fd_tbDepTime);
		
		CDateTime tbArrTime = new CDateTime(group_2, CDT.DROP_DOWN);
		fd_tbDepTime.right = new FormAttachment(tbArrTime, -21);
		tbArrTime.setPattern("yyyy-MM-dd HH:mm:ss");
		FormData fd_tbArrTime = new FormData();
		fd_tbArrTime.top = new FormAttachment(lblTimearr, 8);
		fd_tbArrTime.right = new FormAttachment(tbSrcLoc, -34);
		tbArrTime.setLayoutData(fd_tbArrTime);
		
		Label lblAddRoute = new Label(group, SWT.NONE);
		lblAddRoute.setText("Εισαγωγή Διαρομής");
		FormData fd_lblAddRoute = new FormData();
		fd_lblAddRoute.bottom = new FormAttachment(group_2, -4);
		fd_lblAddRoute.left = new FormAttachment(0, 375);
		
		Button btnNewButton = new Button(group_2, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String busNo = tbBusNo.getText();
				String seatPrice = tbTicketPrice.getText();
				String seatsTotal = tbTotalSeats.getText();
				String timeDep = tbDepTime.getText();
				String timeArr = tbArrTime.getText();
				String locSrc = tbSrcLoc.getText();
				String locDst = tbDstLoc.getText();
				if (busNo.isEmpty() || !ClientMain.isNumber(busNo)) {
					JOptionPane.showMessageDialog(null,"Ο Αριθμός λεωφορείου πρέπει να είναι έγκυρος αριθμός.", "", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (seatPrice.isEmpty() || !ClientMain.isNumber(seatPrice)) {
					JOptionPane.showMessageDialog(null,"Η τιμή θέσης πρέπει να είναι έγκυρος αριθμός.", "", JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (seatsTotal.isEmpty() || !ClientMain.isNumber(seatsTotal)) {
					JOptionPane.showMessageDialog(null,"Οι θέσεις πρέπει να είναι έγκυρος αριθμός.", "", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				Date dtDep;
				Date dtArr;
				
				try {
					SimpleDateFormat sdfDep = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					dtDep = sdfDep.parse(timeDep);
				} catch (IllegalArgumentException | java.text.ParseException | ParseException exc) {
					JOptionPane.showMessageDialog(null,"Εισάγετε μια έγκυρη ημερομηνία/ώρα αναχώρησης", "", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				try {
					SimpleDateFormat sdfArr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					dtArr = sdfArr.parse(timeArr);
				} catch (IllegalArgumentException | java.text.ParseException | ParseException exc) {
					JOptionPane.showMessageDialog(null,"Εισάγετε μια έγκυρη ημερομηνία/ώρα άφιξης", "", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				
				if (dtDep.after(dtArr)) {
					JOptionPane.showMessageDialog(null,"Η άφιξη στον προορισμό δεν μπορεί να είναι πριν την αναχώρηση", "", JOptionPane.WARNING_MESSAGE);
				}
				
				if (locSrc.isEmpty() || locDst.isEmpty())
					JOptionPane.showMessageDialog(null,"Παρακαλώ συμπληρώστε όλα τα πεδία", "", JOptionPane.WARNING_MESSAGE);
				NetworkConnection net = NetworkConnection.getInstance();
				JSONObject newRoute = new JSONObject();
				newRoute.put(Packets.PCK_ACTION, Packets.ACT_NEWROUTE);
				JSONObject newRouteData = new JSONObject();
				newRouteData.put("bus_no", busNo);
				newRouteData.put("seat_price", seatPrice);
				newRouteData.put("seats_total", seatsTotal);
				newRouteData.put("time_dep", timeDep);
				newRouteData.put("time_arr", timeArr);
				newRouteData.put("loc_src", locSrc);
				newRouteData.put("loc_dst", locDst);
				newRoute.put("data", newRouteData);
				net.writeToServer(newRoute);
			}
		});
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.top = new FormAttachment(lblDestination, 6);
		fd_btnNewButton.left = new FormAttachment(tbDstLoc, 6);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("Add");
		lblAddRoute.setLayoutData(fd_lblAddRoute);
		
		Label label_4 = new Label(group, SWT.NONE);
		label_4.setText("Καταχώρηση Ανακοίνωσης");
		FormData fd_label_4 = new FormData();
		fd_label_4.bottom = new FormAttachment(lblAddRoute, -80);
		fd_label_4.left = new FormAttachment(0, 357);
		label_4.setLayoutData(fd_label_4);
		
		Button button_2 = new Button(group, SWT.NONE);
		button_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tbNewAnnounce.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null,"Η ανακοίνωση δεν μπορεί να είναι κενή.", "", JOptionPane.WARNING_MESSAGE);
					return;
				}
				NetworkConnection net = NetworkConnection.getInstance();
				JSONObject newAnnouncement = new JSONObject();
				newAnnouncement.put(Packets.PCK_ACTION, Packets.ACT_NEWANNOUNCE);
				JSONObject announcement = new JSONObject();
				announcement.put(Packets.PAR_ANNOUNCEMENT, tbNewAnnounce.getText());
				newAnnouncement.put(Packets.PCK_DATA, announcement);
				net.writeToServer(newAnnouncement);
			}
		});
		button_2.setText("Add");
		FormData fd_button_2 = new FormData();
		button_2.setLayoutData(fd_button_2);
		
		tbNewAnnounce = new Text(group, SWT.BORDER);
		fd_button_2.top = new FormAttachment(tbNewAnnounce, 0, SWT.TOP);
		fd_button_2.left = new FormAttachment(tbNewAnnounce, 6);
		FormData fd_tbNewAnnounce = new FormData();
		fd_tbNewAnnounce.left = new FormAttachment(0, 10);
		fd_tbNewAnnounce.right = new FormAttachment(100, -145);
		fd_tbNewAnnounce.top = new FormAttachment(label_4, 6);
		tbNewAnnounce.setLayoutData(fd_tbNewAnnounce);
		
		 tblRoutes.addListener(SWT.DefaultSelection, new Listener() {
				@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
				tbSeats.removeAll();
		        TableItem[] selection = tblRoutes.getSelection();
		        String route_id = selection[0].getText(0);
		        activeRoutedId = route_id;
		        String seats_total = selection[0].getText(3);
		        JSONObject routeData = ClientMain.getInstance().route_data;
		        try {
		        	JSONArray routes = routeData.getJSONArray("routes");
		        	JSONObject route = null;
		        	for (int i = 0; i < routes.length(); i++) {
						try {
						    JSONObject r = routes.getJSONObject(i);
						    if (r.getString("id").equals(route_id)) {
						    	route = r;
						    	break;
						    }						    	
						} catch (Exception e) {
							e.printStackTrace();
						};
		        	}
		        	if (route != null) {
		        		JSONArray tickets = route.getJSONArray("tickets");
		        		List<String> seatsTaken = new ArrayList<>();
		        		for (int i = 0; i < tickets.length(); i++) {
							try {
							    JSONObject ticket = tickets.getJSONObject(i);
							    seatsTaken.add(ticket.getString("seat_number"));
							} catch (Exception e) {
								e.printStackTrace();
							};
		        		}
		        		for (int i = 1; i < Integer.parseInt(seats_total) + 1; i++) {
		        			if (!seatsTaken.contains(Integer.toString(i))) {
		        				tbSeats.add(Integer.toString(i));
		        			}
		        		}
		        	}
		        } catch (JSONException e) {
		        	e.printStackTrace();
		        }
			}
		 });
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				NetworkConnection net = NetworkConnection.getInstance();
				JSONObject newRoute = new JSONObject();
				newRoute.put(Packets.PCK_ACTION, Packets.ACT_LISTROUTE);
				newRoute.put(Packets.PCK_DATA, new JSONObject("{}"));
				net.writeToServer(newRoute);
			}
		});
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				NetworkConnection net = NetworkConnection.getInstance();
				JSONObject ann = new JSONObject();
				ann.put(Packets.PCK_ACTION, Packets.ACT_LISTANNOUNCE);
				ann.put(Packets.PCK_DATA, new JSONObject("{}"));
				net.writeToServer(ann);
			}
		});
		
		if (!isAdmin) {
			tabAdmin.dispose();
		}
		//if is admin
		
		shlTicketingSystem.open();
		shlTicketingSystem.layout();
		while (!shlTicketingSystem.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	public static boolean isNumber(String no) {
		try {
			Double.parseDouble(no);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
