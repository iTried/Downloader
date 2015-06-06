package com.example.abc.downloader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;



public class DownloaderMain extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_downloader_main);

        ImageView img = (ImageView) findViewById(R.id.img);

        new DownloadImageTask(img).execute("http://cdn01.wallconvert.com/_media/thumbs/1/17/164754.jpg");
        new DownloadTextTask().execute("http://isu.ru");
    }

    private InputStream OpenHttpConnection(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();

            if (response == HttpURLConnection.HTTP_OK){
                in = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            throw new IOException("Error connecting");
        }
        return in;
    }

    private static InputStream openHttpConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setAllowUserInteraction(false);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Connection", "close");
        conn.connect();
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) return conn.getInputStream();
        else throw new IOException("Bad request");
    }

    private Bitmap downloadImage(String URL){
        Bitmap bitmap = null;
        InputStream in;
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return bitmap;
    }

    private String DownloadText(String URL){
        int BUFFER_SIZE = 2000;
        InputStream in;
        try {
            in = openHttpGETConnection(URL);
        } catch (Exception e) {
            Log.d("DownloadText", e.getLocalizedMessage());
            return "";
        }
        InputStreamReader isr = new InputStreamReader(in);
        int charRead;
        String str = "";
        char[] inputBuffer = new char[BUFFER_SIZE];
        try {
            while ((charRead = isr.read(inputBuffer)) > 0) {
                String readString = String.copyValueOf(inputBuffer, 0, charRead);
                str += readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
            in.close();
        } catch (IOException e) {
            Log.d("DownloadText", e.getLocalizedMessage());
            return "";
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    private class DownloadTextTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return DownloadText(urls[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
            Log.d("DownloadTextTask", result);
        }
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView view;
        public DownloadImageTask(ImageView v) { view = v; }
        protected Bitmap doInBackground(String... params) {
            return downloadImage(params[0]);
        }
        protected void onPostExecute(final Bitmap result) {
            view.post(new Runnable() {
                public void run() {
                    ((ImageView) view.findViewById(R.id.img)).setImageBitmap(result);
                }
            });
        }
    }

    private static InputStream openHttpGETConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setAllowUserInteraction(false);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Connection", "close");
        conn.connect();
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
            return conn.getInputStream();
        else
            throw new IOException("Bad request");
    }
}
