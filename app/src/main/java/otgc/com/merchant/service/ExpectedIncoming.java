package otgc.com.merchant.service;

import java.util.HashMap;

public class ExpectedIncoming {

    private static HashMap<String, Long> incomingBTC = null;
    private static HashMap<String, String> incomingFiat = null;

    private static ExpectedIncoming instance = null;

    private ExpectedIncoming()  { ; }

    public static ExpectedIncoming getInstance()    {

        if(instance == null)    {
            incomingBTC = new HashMap<String, Long>();
            incomingFiat = new HashMap<String, String>();
            instance = new ExpectedIncoming();
        }

        return instance;

    }

    public HashMap<String,Long> getBTC()   {
        return incomingBTC;
    }

    public HashMap<String,String> getFiat()   {
        return incomingFiat;
    }

}
