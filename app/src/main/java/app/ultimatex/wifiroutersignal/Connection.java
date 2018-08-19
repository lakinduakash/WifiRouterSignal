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
    public static final int NOT_SUPPORTED_I = -1;
    public static final String NOT_CONNECTED = "not_connected";
    public static final int NOT_CONNECTED_I = -2;
    public static final String DISCONNECTED = "Router disconnected";
    public static final String DEFAULT_ADDR = "http://homerouter.cpe";

    private static boolean cookieInitialized = false;
    private static CookieManager cookieManager = new CookieManager();
    private static Connection instance;

    private static String baseUrl = DEFAULT_ADDR + "/";
    private String statusApi = "api/monitoring/status/";
    private String trafficApi = "api/monitoring/traffic-statistics/";
    private String home = "html/home.html";

    private boolean isPrevRequestSuccess = false;


    private boolean connected = false;


    private HttpURLConnection urlConnection;

    private InputStream statusStream;
    private InputStream trafficStream;

    private Document status;
    private Document traffic;

    public static Connection getInstance() {
        if (instance == null)
            return instance = new Connection();
        else
            return instance;
    }

    public static Connection getInstance(String address) {
        baseUrl = address + "/";
        if (instance == null) {
            return instance = new Connection();
        } else
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
                connected = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    public void openConnection() {
        statusStream = null;
        trafficStream = null;
        URL apiStatusUrl = null;

        try {
            apiStatusUrl = new URL(baseUrl + statusApi);
            HttpURLConnection urlConnection1 = (HttpURLConnection) apiStatusUrl.openConnection();
            statusStream = urlConnection1.getInputStream();
            status = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(statusStream);
            if (!isCookieValid(status)) {
                reInitializeCookie();
                openConnection();
                connected = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            connected = false;
        }


        URL apiTrafficUrl = null;

        try {
            apiTrafficUrl = new URL(baseUrl + trafficApi);
            urlConnection = (HttpURLConnection) apiTrafficUrl.openConnection();
            trafficStream = urlConnection.getInputStream();
            traffic = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(trafficStream);

            if (!isCookieValid(traffic)) {
                reInitializeCookie();
                openConnection();
                connected = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            connected = false;

        }

    }


    public String getSignalLevel() {

        if (statusStream != null) {
            try {


                return getElementValue(status, "SignalIcon", NOT_SUPPORTED);


            } catch (Exception e) {
                e.printStackTrace();
                return NOT_SUPPORTED;
            }
        }

        if (!isPrevRequestSuccess)
            return NOT_CONNECTED;
        else
            return DISCONNECTED;


    }


    public String getCurrentUsers() {

        if (statusStream != null) {
            try {

                return getElementValue(status, "CurrentWifiUser", NOT_SUPPORTED);


            } catch (Exception e) {
                e.printStackTrace();
                return NOT_SUPPORTED;
            }
        }

        if (!isPrevRequestSuccess)
            return NOT_CONNECTED;
        else
            return DISCONNECTED;
    }

    public String getConnectTime() {
        if (trafficStream != null && connected) {
            try {
                String timeSec = getElementValue(traffic, "CurrentConnectTime", NOT_SUPPORTED);

                int secTotal = Integer.parseInt(timeSec);

                int sec = secTotal % 60;
                int minTotal = secTotal / 60;
                int min = minTotal % 60;
                int hours = minTotal / 60;

                return Integer.toString(hours) + ":" + Integer.toString(min) + ":" + Integer.toString(sec);




            } catch (Exception e) {
                e.printStackTrace();
                return NOT_SUPPORTED;
            }
        }

        if (!isPrevRequestSuccess)
            return NOT_CONNECTED;
        else
            return DISCONNECTED;
    }


    public String getSessionData() {

        int total = getSessionDataInBytes();

        if (total >= 0) {

            if (total > 1024 * 1024) {
                double mb = total / (1024.0 * 1024.0);
                String s = String.format(Locale.getDefault(), "%.2f", mb);
                return s + " MB";
            } else if (total > 1024) {
                double kb = total / 1024.0;
                String s = String.format(Locale.getDefault(), "%.2f", kb);
                return s + " KB";
            } else {
                return Integer.toString(total) + " Bytes";
            }
        } else if (total == NOT_CONNECTED_I)
            if (!isPrevRequestSuccess)
                return NOT_CONNECTED;
            else
                return DISCONNECTED;
        else
            return NOT_SUPPORTED;
    }

    public boolean isConnected() {
        return connected;
    }

    private String getElementValue(Document document, String element, String def) {
        NodeList list = document.getElementsByTagName(element);

        if (list.getLength() != 0) {
            isPrevRequestSuccess = true;
            return list.item(0).getTextContent();
        } else {
            isPrevRequestSuccess = false;
            return def;
        }
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

    public int getSessionDataInBytes() {
        if (trafficStream != null && connected) {
            try {
                String down = getElementValue(traffic, "CurrentDownload", NOT_SUPPORTED);
                String up = getElementValue(traffic, "CurrentUpload", NOT_SUPPORTED);

                return Integer.parseInt(down) + Integer.parseInt(up);


            } catch (Exception e) {
                e.printStackTrace();
                return NOT_SUPPORTED_I;
            }
        }
        return NOT_CONNECTED_I;

    }
}
