package com.seizuresensor;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.UUID;
import android.app.Activity;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.view.Window;

public class SeizureSensorActivity extends Activity {
	// Declare Button Variables
    protected Button startButton;
    protected Button startServerButton;
    protected Button stopServerButton;
    protected Button retrieveLocationButton;
    protected Button exportDatabaseButton;
    protected Button emailDatabaseButton;
    
    boolean cancelServerSocket = true;
    String MyPacketString = null;
    
    DatabaseHandler db = new DatabaseHandler(this);
    
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 20; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
	protected LocationManager locationManager;
	
	// Bluetooth Variables
	// Default Serial-Port UUID
	private String defaultUUID = "00001101-0000-1000-8000-00805F9B34FB";
	// Default Bluetooth adapter on the device.
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	// The Server thread.
	private AcceptThread server;
	// Magic number used in the bluetooth enabling request.
	//private final int REQ = 111;
	private NotificationCenter mNotificationCenter;
	private static final String MESSAGE_RECEIVED_INTENT = "com.almightybuserror.intent.MESSAGE_RECEIVED";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mNotificationCenter = new NotificationCenter();
        this.registerReceiver(mNotificationCenter, new IntentFilter(MESSAGE_RECEIVED_INTENT));
        
        retrieveLocationButton = (Button) findViewById(R.id.retrieve_location_button);
        exportDatabaseButton = (Button) findViewById(R.id.export_database);
        startServerButton = (Button) findViewById(R.id.btn_start_server);
        stopServerButton = (Button) findViewById(R.id.btn_stop_server);
        emailDatabaseButton = (Button) findViewById(R.id.email_database);
        
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener(getApplicationContext()));
				
		retrieveLocationButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showCurrentLocation();
			}
		});
		
		startServerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelServerSocket = true;
				server = new AcceptThread();
				server.start();
				startServerButton.setEnabled(false);
				stopServerButton.setEnabled(true);
			}
        });
		      
        stopServerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelServerSocket = false;
				server.cancel();
				startServerButton.setEnabled(true);
				stopServerButton.setEnabled(false);
			}
        });
        
		exportDatabaseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exportDB();
			}
        });
		
		emailDatabaseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendEmail();
			}
        });
                
    }
    
	public void processPacketA (String packet) {
		MyPacketString = packet;
		Packet MyPacket = new Packet(MyPacketString);
		// Creating alert Dialog with two Buttons
		AlertDialog.Builder alertDialog1 = new AlertDialog.Builder(SeizureSensorActivity.this);

		// Setting Dialog Title
		alertDialog1.setTitle("Seizure Deteced!");
		// Setting Dialog Message
		alertDialog1.setMessage("Packet String: " + MyPacketString + "\n" + "Command: " + MyPacket.command + "\n" + "Time: " 
					+ MyPacket.time + "\n" + "Size: " + MyPacket.size + "\n" + "Payload: " + MyPacket.payload + "\n" + "\n" + "A Seizure was detected with " + MyPacket.payload + "% probability." + "\n" + "\n" + "Did you have a seizure?");
		// Setting Icon to Dialog
		alertDialog1.setIcon(R.drawable.brain);
		// Setting Positive "Yes" Btn
		alertDialog1.setPositiveButton("YES",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Write your code here to execute after dialog
						savePacket2Database (MyPacketString, "Yes");
						Toast.makeText(getApplicationContext(),
								"You had a seizure! Level 1 has now been executed. Information logged to database.", Toast.LENGTH_SHORT)
								.show();
					}
				});
		// Setting Negative "NO" Btn
		alertDialog1.setNegativeButton("NO",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Write your code here to execute after dialog
						savePacket2Database (MyPacketString, "No");
						Toast.makeText(getApplicationContext(), "You did not have a seizure. Information logged to database.", Toast.LENGTH_SHORT).show();
						dialog.cancel();
					}
				});
			// Showing Alert Dialog
			alertDialog1.show();
	}
	
	public void processPacketB (String packet) {
		MyPacketString = packet;
		Packet MyPacket = new Packet(MyPacketString);
		// Creating alert Dialog with two Buttons
		AlertDialog alertDialog2 = new AlertDialog.Builder(SeizureSensorActivity.this).create();

		// Setting Dialog Title
		alertDialog2.setTitle("Battery Level");
		// Setting Dialog Message
		alertDialog2.setMessage("Packet String: " + MyPacketString + "\n" + "Command: " + MyPacket.command + "\n" + "Time: " 
				+ MyPacket.time + "\n" + "Size: " + MyPacket.size + "\n" + "Payload: " + MyPacket.payload + "\n" + "\n" + "Battery Level is at: " + MyPacket.payload + "%.");
		// Setting Icon to Dialog
		alertDialog2.setIcon(R.drawable.brain);
		// Setting OK Button
		alertDialog2.setButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				savePacket2Database (MyPacketString, "OK");
				Toast.makeText(getApplicationContext(), "Information logged to database.", Toast.LENGTH_SHORT).show();
			}
		});
		// Showing Alert Message
		alertDialog2.show();
	}
	
	public void processPacketD (String packet) {
		MyPacketString = packet;
		Packet MyPacket = new Packet(MyPacketString);
		// Creating alert Dialog with two Buttons
		AlertDialog alertDialog3 = new AlertDialog.Builder(SeizureSensorActivity.this).create();

		// Setting Dialog Title
		alertDialog3.setTitle("Electrode Disconnected");
		// Setting Dialog Message
		alertDialog3.setMessage("Packet String: " + MyPacketString + "\n" + "Command: " + MyPacket.command + "\n" + "Time: " 
				+ MyPacket.time + "\n" + "Size: " + MyPacket.size + "\n" + "Payload: " + MyPacket.payload + "\n" + "\n" + "Electrode " + MyPacket.payload + " is disconnected.");
		// Setting Icon to Dialog
		alertDialog3.setIcon(R.drawable.brain);
		// Setting OK Button
		alertDialog3.setButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				savePacket2Database (MyPacketString, "OK");
				Toast.makeText(getApplicationContext(),"Information logged to database.", Toast.LENGTH_SHORT).show();
			}
		});
		// Showing Alert Message
		alertDialog3.show();
	}
    	
	class AcceptThread extends Thread {
		/**Tag that will appear in the log.*/
		private final String ACCEPT_TAG = AcceptThread.class.getName();
		/**The bluetooth server socket.*/
		private final BluetoothServerSocket mServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			try {
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(ACCEPT_TAG,
						UUID.fromString(defaultUUID));
			} catch (IOException e) { 
				e.printStackTrace();
			}
			mServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			String buffer;
			while (true) {
				try {
					// Toast.makeText(SeizureSensorActivity.this, "Listening for a connection...", Toast.LENGTH_LONG).show();
					Log.i(ACCEPT_TAG, "Listening for a connection...");
					socket = mServerSocket.accept();
					Log.i(ACCEPT_TAG, "Connected to " + socket.getRemoteDevice().getName());

				} catch (IOException e) {
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					try {
						// Read the incoming string.
						while(cancelServerSocket) {
							DataInputStream in = new DataInputStream(socket.getInputStream());
							buffer = in.readUTF();
							if ((buffer.startsWith("A") || buffer.startsWith("B") || buffer.startsWith("D")) && (buffer.length() >= 15)){
								Intent i = new Intent(MESSAGE_RECEIVED_INTENT);
								i.putExtra("Message", buffer);
								getBaseContext().sendBroadcast(i);
							}
						};
					} catch (IOException e) {
						Log.e(ACCEPT_TAG, "Error obtaining InputStream from socket");
						e.printStackTrace();
					}
					break;
				}
			}
		}
		
		public void cancel() {
			try {
				mServerSocket.close();
				displayToast("Socket Closed");
			} catch (IOException e) { }
		}
				
	}
	
	class NotificationCenter extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String foo = intent.getExtras().getString("Message");
			if (intent.getAction().equals(MESSAGE_RECEIVED_INTENT)) {
				if (foo.startsWith("A")) {
					processPacketA(foo);
				} else if (foo.startsWith("B")) {
					processPacketB(foo);
				} else if (foo.startsWith("D")) {
					processPacketD(foo);
				}
			}
		}
	}
	
	public void savePacket2Database (String packet, String response) {
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		String Mylocation = null;
		if (location != null) {
			Mylocation = String.format("Current Location \n Longitude: %1$s \n Latitude: %2$s", location.getLongitude(), location.getLatitude());
		} else if (location == null) {
			Mylocation = "Location: Null";
		}
		Packet packet1 = new Packet(packet);
		 Log.d("Insert: ", "Inserting ..");
         db.addPacket(new Packet(packet1.command, packet1.time, (int)packet1.size, (int)packet1.payload, Mylocation, response));
         // Reading all contacts
         Log.d("Reading: ", "Reading all contacts..");
         List<Packet> packets = db.getAllPackets();       
 
         for (Packet cn : packets) {
        	     String log = "Id: "+ cn.getID()+ " ,Command: " + cn.getCommand() + " ,Time: " + cn.getTime() + " ,Size: " + cn.getSize() + " ,Data: " + cn.getData() + " ,GPS Data: " + cn.getGPSData() + " ,User Response: " + cn.getUserResponse();
                     // Writing Contacts to log
            Log.d("Name: ", log);
         }
	}
	
    protected void showCurrentLocation() {

		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if (location != null) {
			String message = String.format("Current Location \n Longitude: %1$s \n Latitude: %2$s", location.getLongitude(), location.getLatitude());
			Toast.makeText(SeizureSensorActivity.this, message, Toast.LENGTH_LONG).show();
		} else if (location == null) {
			Toast.makeText(SeizureSensorActivity.this, "Null Location", Toast.LENGTH_LONG).show();
		}

	}
    
    public void exportDB () {
    	Toast.makeText(SeizureSensorActivity.this, "Exporting Database file to " + Environment.getExternalStorageDirectory(), Toast.LENGTH_LONG).show();
    	File dbFile = new File(Environment.getDataDirectory() + "/data/com.androidhive.androidsqlite/databases/MyDatabase");
    	File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        File file = new File(exportDir, dbFile.getName());
        try {
            file.createNewFile();
            copyFile(dbFile, file);
         } catch (IOException e) {
         } 
    }
    
    public void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
           inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
           if (inChannel != null)
              inChannel.close();
           if (outChannel != null)
              outChannel.close();
        }
        Toast.makeText(SeizureSensorActivity.this, "Transfer Complete", Toast.LENGTH_SHORT).show();
     }
    
    public void displayToast(String message) {
    	Toast.makeText(SeizureSensorActivity.this, message, Toast.LENGTH_LONG).show();
    }
    
    public void sendEmail() {
    	Intent i = new Intent(Intent.ACTION_SEND);
    	i.setType("text/plain");
    	i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"stroshowjaron@hotmail.com"});
    	i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
    	i.putExtra(Intent.EXTRA_TEXT   , "body of email");
    	File root = Environment.getExternalStorageDirectory();
    	File file = new File(root, "MyDatabase");
    	Uri uri = Uri.fromFile(file);
    	i.putExtra(Intent.EXTRA_STREAM, uri);
    	try {
    	    startActivity(Intent.createChooser(i, "Sending database ..."));
    	} catch (android.content.ActivityNotFoundException ex) {
    	    Toast.makeText(SeizureSensorActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
    	}
    } 
}

// A202506161101236888111
// B20250616110123288