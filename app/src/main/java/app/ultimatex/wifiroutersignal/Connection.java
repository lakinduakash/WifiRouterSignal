package app.ultimatex.wifiroutersignal;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;

public class Connection {

    public static final String NOT_SUPPORTED = "not_supported";
    public static final String NOT_CONNECTED = "not_connected";

    private static boolean cookieInitialized = false;
    private static CookieHandler cookieHandler;
    private static CookieManager cookieManager = new CookieManager();
    private static Connection instance;

    private static String baseUrl = "http://homerouter.cpe/";
    private String statusApi = "api/monitoring/status/";
    private String trafficApi = "api/monitoring/traffic-statistics/";
    private String home = "html/home.html";


    private HttpURLConnection urlConnection;

    private InputStream statusStream;
    private InputStream trafficStream;

    public static Connection getInstance() {
        if (instance == null)
            return instance = new Connection();
        else
            return instance;
    }


    private Connection() {
        URL url = null;
        CookieHandler.setDefault(cookieManager);


        if (!cookieInitialized) {
            try {
                url = new URL(baseUrl + home);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                urlConnection.getInputStream();
                cookieInitialized = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }


    public String getSignalLevel() {
        URL apiStatusUrl = null;

        try {
            apiStatusUrl = new URL(baseUrl + statusApi);
            urlConnection = (HttpURLConnection) apiStatusUrl.openConnection();
            statusStream = urlConnection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            return NOT_CONNECTED;
        }

        if (statusStream != null) {
            try {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(statusStream);

                if (!isCookieValid(document))
                    reInitializeCookie();
                return getElementValue(document, "SignalIcon", NOT_SUPPORTED);


            } catch (Exception e) {
                e.printStackTrace();
                return NOT_SUPPORTED;
            }
        }

        return NOT_CONNECTED;


    }


    public String getSessionData() {
        URL apiStatusUrl = null;

        try {
            apiStatusUrl = new URL(baseUrl + trafficApi);
            urlConnection = (HttpURLConnection) apiStatusUrl.openConnection();
            trafficStream = urlConnection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            return NOT_CONNECTED;
        }

        if (trafficStream != null) {
            try {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(trafficStream);

                if (!isCookieValid(document))
                    reInitializeCookie();
                String down = getElementValue(document, "CurrentDownload", NOT_SUPPORTED);
                String up = getElementValue(document, "CurrentUpload", NOT_SUPPORTED);

                int total = Integer.parseInt(down) + Integer.parseInt(up);


                if (total > 1024 * 1024) {
                    double mb = total / (1024.0 * 1024.0);
                    String s = String.format(Locale.getDefault(), "%.2f", mb);
                    return s + " Mb";
                } else if (total > 1024) {
                    double kb = total / 1024.0;
                    String s = String.format(Locale.getDefault(), "%.2f", kb);
                    return s + " Kb";
                } else {
                    return Integer.toString(total) + " Bytes";
                }


            } catch (Exception e) {
                e.printStackTrace();
                return NOT_SUPPORTED;
            }
        }

        return NOT_CONNECTED;
    }

    private String getElementValue(Document document, String element, String def) {
        NodeList list = document.getElementsByTagName(element);

        if (list.getLength() != 0)
            return list.item(0).getTextContent();
        else
            return def;
    }

    private String getRoot(Document document) {
        return document.getDocumentElement().getNodeName();
    }

    private boolean isCookieValid(Document document) {
        return !"error".equals(getRoot(document));
    }

    private void reInitializeCookie() {
        URL url = null;
        try {
            url = new URL(baseUrl + home);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            urlConnection.getInputStream();
            cookieInitialized = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
