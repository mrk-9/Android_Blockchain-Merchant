package otgc.com.merchant.util;

import android.content.Context;

import info.blockchain.wallet.util.FormatsUtil;

public class AppUtil {

    private static Context context = null;
    private static AppUtil instance = null;

    private AppUtil() { ; }

    public static AppUtil getInstance(Context ctx) {

        context = ctx;

        if(instance == null) {
            instance = new AppUtil();
        }

        return instance;
    }

    public boolean isLegacy() {
        //
        // test for app v1
        //
        try {
            Class.forName("info.blockchain.merchant.NetworkStateReceiver");
            // app v2, do nothing
            return false;
        }
        catch(ClassNotFoundException cnfe) {
            return true;
        }
    }

    public boolean isV2API() {
        String strReceiver = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");

        if(FormatsUtil.getInstance().isValidBitcoinAddress(strReceiver))    {
            return false;
        }
        else    {
            return true;
        }
    }

    public boolean hasValidReceiver() {

        String receiver = PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");

        if(FormatsUtil.getInstance().isValidBitcoinAddress(receiver) || FormatsUtil.getInstance().isValidXpub(receiver))    {
            return true;
        }
        else    {
            return false;
        }

    }

}
