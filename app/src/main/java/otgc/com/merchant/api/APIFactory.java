package otgc.com.merchant.api;
//import android.util.Log;

public class APIFactory	{

    private static APIFactory instance = null;

    private APIFactory()	{ ; }

    public static APIFactory getInstance() {

        if(instance == null) {
            instance = new APIFactory();
        }

        return instance;
    }

    public String getAPIKey()    {
        return APIKey.getInstance().getKey();
    }

    public String getCallback()    {
        return APIKey.getInstance().getCallback();
    }

}
