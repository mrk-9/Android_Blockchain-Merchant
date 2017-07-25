package otgc.com.merchant.api;

public class APIKey	{

    private static final String API_KEY = "1fc17339-489e-4a56-943e-a68c1a30b4b1";
    private static final String API_CALLBACK = "https://api.blockchain.info/v2/receive/ok.txt";

    private static APIKey instance = null;

    private APIKey()	{ ; }

    public static APIKey getInstance() {

        if(instance == null) {
            instance = new APIKey();
        }

        return instance;
    }

    public String getKey()    {
        return API_KEY;
    }

    public String getCallback()    {
        return API_CALLBACK;
    }

}
