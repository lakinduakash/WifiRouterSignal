package app.ultimatex.wifiroutersignal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Connection {

    private static boolean cookieInitialized=false;
    private static CookieManager cookieManager=new CookieManager();

    private String baseUrl ="http://192.168.1.1/";
    private String api ="api/monitoring/status/";
    private String home="home/index.html";

    private HttpURLConnection urlConnection;


    public Connection()
    {
        URL url =null;
        CookieHandler.setDefault(cookieManager);


        if(!cookieInitialized)
        {
            try {
                url= new URL(baseUrl+home);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                urlConnection.getInputStream();
                cookieInitialized =true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public String getSignalLevel()
    {
        int level=0;
        StringBuffer response= new StringBuffer();
        try {
            URL url= new URL(baseUrl+api);

            urlConnection =(HttpURLConnection) url.openConnection();

            BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }
            bufferedReader.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }

        String xml=response.toString();



        return xml;
    }

}
