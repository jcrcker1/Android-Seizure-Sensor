package com.seizuresensor;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 3;

	// Database Name
	private static final String DATABASE_NAME = "MyDatabase";

	// Contacts table name
	private static final String TABLE_CONTACTS = "MyTable";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_COMMAND = "command";
	private static final String KEY_TIME = "time";
	private static final String KEY_SIZE = "size";
	private static final String KEY_DATA = "data";
	private static final String KEY_GPS_DATA = "gps_data";
	private static final String KEY_USER_RESPONSE = "user_response";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_RECORDS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_COMMAND + " TEXT," + KEY_TIME + " TEXT," + KEY_SIZE + " TEST," + KEY_DATA + " INT," + KEY_GPS_DATA + " TEXT," + KEY_USER_RESPONSE + " TEXT" + ");";
		db.execSQL(CREATE_RECORDS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new contact
	void addPacket(Packet packet) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_COMMAND, packet.getCommand());
		values.put(KEY_TIME, packet.getTime()); // Contact Name
		values.put(KEY_SIZE, packet.getSize());
		values.put(KEY_DATA, packet.getData()); // Contact Phone
		values.put(KEY_GPS_DATA, packet.getGPSData());
		values.put(KEY_USER_RESPONSE, packet.getUserResponse());

		// Inserting Row
		db.insert(TABLE_CONTACTS, null, values);
		db.close(); // Closing database connection
	}

	// Getting single contact
	Packet getPacket(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
				KEY_TIME, KEY_DATA, KEY_GPS_DATA, KEY_USER_RESPONSE }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		Packet packet = new Packet(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2),cursor.getInt(3),  cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getString(7));
		// return contact
		return packet;
	}
	
	// Getting All Contacts
	public List<Packet> getAllPackets() {
		List<Packet> packetList = new ArrayList<Packet>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Packet packet = new Packet();
				packet.setID(Integer.parseInt(cursor.getString(0)));
				packet.setCommand(cursor.getString(1));
				packet.setTime(cursor.getString(2));
				packet.setSize(cursor.getInt(3));
				packet.setData(cursor.getInt(4));
				packet.setGPSData(cursor.getString(5));
				packet.setUserResponse(cursor.getString(6));
				// Adding contact to list
				packetList.add(packet);
			} while (cursor.moveToNext());
		}

		// return contact list
		return packetList;
	}

	// Updating single contact
	public int updatePacket(Packet packet) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_TIME, packet.getTime());
		values.put(KEY_DATA, packet.getData());
		values.put(KEY_GPS_DATA, packet.getGPSData());
		values.put(KEY_USER_RESPONSE, packet.getUserResponse());

		// updating row
		return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
				new String[] { String.valueOf(packet.getID()) });
	}

	// Deleting single contact
	public void deletePacket(Packet packet) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
				new String[] { String.valueOf(packet.getID()) });
		db.close();
	}


	// Getting contacts Count
	public int getContactsCount() {
		String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		return cursor.getCount();
	}

}
