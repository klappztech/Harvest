// ------------------------------------ DBADapter.java ---------------------------------------------

// TODO: Change the package to match your project.
package fortunate.harvest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


// TO USE:
// Change the package (at top) to match your project.
// Search for "TODO", and make the appropriate changes.
public class DBAdapter {

	/////////////////////////////////////////////////////////////////////
	//	Constants & Data
	/////////////////////////////////////////////////////////////////////
	// For logging:
	private static final String TAG = "DBAdapter";
	
	// DB Fields

	/*
	 * CHANGE 1:
	 */
	// TODO: Setup your fields here:
	public static final String KEY_ROWID            = "_id";
	public static final String KEY_TITLE 			= "title";
	public static final String KEY_URL 				= "url";
	public static final String KEY_DATE_RCVD        = "date_r";
	public static final String KEY_DATE_PUB         = "date_p";
	public static final String KEY_DESCRIPRION 		= "description";
	public static final String KEY_READ		 		= "read";

	// TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
	public static final int COL_ROWID           = 0;
	public static final int COL_TITLE 	        = 1;
	public static final int COL_URL 		    = 2;
	public static final int COL_DATE_RCVD	    = 3;
	public static final int COL_DATE_PUB		= 4;
	public static final int COL_DESCRIPRION     = 5;
	public static final int COL_READ    		= 6;

	
	public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_TITLE, KEY_URL, KEY_DATE_RCVD, KEY_DATE_PUB,KEY_DESCRIPRION,KEY_READ};
	
	// DB info: it's name, and the table we are using (just one).
	public static final String DATABASE_NAME = "MyDb";
	public static final String DATABASE_TABLE = "internal_notifications";
	// Track DB version if a new version of your app changes the format.
	public static final int DATABASE_VERSION = 7;
	
	private static final String DATABASE_CREATE_SQL =
			"create table " + DATABASE_TABLE 
			+ " (" + KEY_ROWID + " integer primary key , "
			
			/*
			 * CHANGE 2:
			 */
			// TODO: Place your fields here!
			// + KEY_{...} + " {type} not null"
			//	- Key is the column name you created above.
			//	- {type} is one of: text, integer, real, blob
			//		(http://www.sqlite.org/datatype3.html)
			//  - "not null" means it is a required field (must be given a value).
			// NOTE: All must be comma separated (end of line!) Last one must have NO comma!!
			+ KEY_TITLE + " text not null, "
			+ KEY_URL + " text not null, "
			+ KEY_DATE_RCVD + " long not null,"
			+ KEY_DATE_PUB + " long not null,"
                    + KEY_DESCRIPRION + " text not null,"
					+ KEY_READ + " long not null"

			// Rest  of creation:
			+ ");";
	
	// Context of application who uses us.
	private final Context context;
	
	private DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	/////////////////////////////////////////////////////////////////////
	//	Public methods:
	/////////////////////////////////////////////////////////////////////
	
	public DBAdapter(Context ctx) {
		this.context = ctx;
		myDBHelper = new DatabaseHelper(context);
	}
	
	// Open the database connection.
	public DBAdapter open() {
		db = myDBHelper.getWritableDatabase();
		return this;
	}
	
	// Close the database connection.
	public void close() {
		myDBHelper.close();
	}
	
	// Add a new set of values to the database.
	public long insertRow(int id, String title, String url, String date_rcvd, String date_pub, String description, long read) {
		/*
		 * CHANGE 3:
		 */		
		// TODO: Update data in the row with new fields.
		// TODO: Also change the function's arguments to be what you need!
		// Create row's data:
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ROWID, id);
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_URL, url);
		initialValues.put(KEY_DATE_RCVD, date_rcvd);
		initialValues.put(KEY_DATE_PUB, date_pub);
		initialValues.put(KEY_DESCRIPRION, description);
		initialValues.put(KEY_READ, read);

		// Insert it into the database.
		return db.insert(DATABASE_TABLE, null, initialValues);
	}
	
	// Delete a row from the database, by rowId (primary key)
	public boolean deleteRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		return db.delete(DATABASE_TABLE, where, null) != 0;
	}
	
	public void deleteAll() {
		Cursor c = getAllRows();
		long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
		if (c.moveToFirst()) {
			do {
				deleteRow(c.getLong((int) rowId));				
			} while (c.moveToNext());
		}
		c.close();
	}
	
	// Return all data in the database.
	public Cursor getAllRows() {
		String where = null;
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
				where, null, null, null, KEY_DATE_PUB + " DESC", null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Get a specific row (by rowId)
	public Cursor getRow(long rowId) {
		String where = KEY_ROWID + "=" + rowId;
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
						where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	
	// Change an existing row to be equal to new data.
	public boolean updateRow(long rowId, String title, String url, int date) {
		String where = KEY_ROWID + "=" + rowId;

		/*
		 * CHANGE 4:
		 */
		// TODO: Update data in the row with new fields.
		// TODO: Also change the function's arguments to be what you need!
		// Create row's data:
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_TITLE, title);
		newValues.put(KEY_URL, url);
		newValues.put(KEY_DATE_PUB, date);
		
		// Insert it into the database.
		return db.update(DATABASE_TABLE, newValues, where, null) != 0;
	}

	// Change an existing row to be equal to new data.
	public boolean markAsRead(long rowId) {
		String where = KEY_ROWID + "=" + rowId;


		// Create row's data:
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_READ, 1);

		// Insert it into the database.
		return db.update(DATABASE_TABLE, newValues, where, null) != 0;
	}
	
	
	
	/////////////////////////////////////////////////////////////////////
	//	Private Helper Classes:
	/////////////////////////////////////////////////////////////////////
	
	/**
	 * Private class which handles database creation and upgrading.
	 * Used to handle low-level database access.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(DATABASE_CREATE_SQL);			
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading application's database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data!");
			
			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			
			// Recreate new database:
			onCreate(_db);
		}
	}
}
