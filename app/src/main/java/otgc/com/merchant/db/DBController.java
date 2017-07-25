package otgc.com.merchant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
//import android.util.Log;

public class DBController extends SQLiteOpenHelper {

	private static final String LOGCAT = "DBController";
	private static final String DB = "payments.db";
	private static final String TABLE = "payment";

	public DBController(Context context) {
		super(context, DB, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		String query;
		query = "CREATE TABLE " + TABLE + " ( _id INTEGER PRIMARY KEY, ts integer, iad text, amt integer, famt text, cfm integer, msg text)";
		database.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
		String query;
		query = "DROP TABLE IF EXISTS " + TABLE;
		database.execSQL(query);
		onCreate(database);
	}

	public void insertPayment(long ts, String address, long amount, String fiat_amount, int confirmed, String message) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ts", ts);
		values.put("iad", address);
		values.put("amt", amount);
		values.put("famt", fiat_amount);
		values.put("cfm", confirmed);
		values.put("msg", message);
		database.insert(TABLE, null, values);
		database.close();
	}

	public ArrayList<ContentValues> getAllPayments() {
		ArrayList<ContentValues> data;
		data = new ArrayList<ContentValues>();
		String selectQuery = "SELECT * FROM payment ORDER BY ts DESC";
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				ContentValues vals = new ContentValues();
				vals.put("_id", cursor.getString(0));
				vals.put("ts", cursor.getLong(1));
				vals.put("iad", cursor.getString(2));
				vals.put("amt", cursor.getLong(3));
				vals.put("famt", cursor.getString(4));
				vals.put("cfm", cursor.getInt(5));
				vals.put("msg", cursor.getString(6));
				data.add(vals);
			} while (cursor.moveToNext());
		}
		cursor.close();
		database.close();

		return data;
	}

	public String getTableName()	{
		return TABLE;
	}

}
