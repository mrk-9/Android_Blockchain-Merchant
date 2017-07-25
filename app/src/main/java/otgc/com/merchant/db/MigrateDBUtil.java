package otgc.com.merchant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;

import java.util.ArrayList;

public class MigrateDBUtil {

    private static Context context = null;
    private static MigrateDBUtil instance = null;

    private MigrateDBUtil() { ; }

    public static MigrateDBUtil getInstance(Context ctx) {

        context = ctx;

        if(instance == null) {
            instance = new MigrateDBUtil();
        }

        return instance;
    }

    public void migrate() {

        DBController pdb = new DBController(context);
        DBControllerV2 pdbV2 = new DBControllerV2(context);

        ArrayList<ContentValues> v1values = pdb.getAllPayments();
        ArrayList<ContentValues> v2values = pdbV2.getAllPayments();

        if(v1values.size() > 0 && v2values.size() == 0)    {

            for(ContentValues vals : v1values)   {

                long ts = vals.getAsLong("ts");
                String iad = vals.getAsString("iad");
                long amt = vals.getAsLong("amt");
                String famt = vals.getAsString("famt");
                int cfm = vals.getAsInteger("cfm");
                String msg = vals.getAsString("msg");

                pdbV2.insertPayment(ts, iad, amt, famt, cfm, msg);

            }

            v2values = pdbV2.getAllPayments();
            if(v2values.size() == v1values.size())    {
                SQLiteDatabase db = pdb.getWritableDatabase();
                db.delete(pdb.getTableName(), null, null);
                db.close();
            }

        }

        pdb.close();
        pdbV2.close();

    }

}
