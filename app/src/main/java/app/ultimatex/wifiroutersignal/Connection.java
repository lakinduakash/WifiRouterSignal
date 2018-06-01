package app.ultimatex.wifiroutersignal;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Connection {

    private static boolean cookieInitialized=false;
    private static CookieManager cookieManager=new CookieManager();

    private String baseUrl ="http://192.168.1.1/";
    private String api ="api/monitoring/status/";
    private String home="html/home.html";

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



        } catch (Exception e) {
            e.printStackTrace();
        }

        //String xml=response.toString();
        Document document =null;

        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder =factory.newDocumentBuilder();
            document =builder.parse(urlConnection.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        NodeList list=document.getElementsByTagName("SignalIcon");
        return list.item(0).getTextContent();

    }

}
