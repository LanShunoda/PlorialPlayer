package com.plorial.exoroplayer.model;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.plorial.exoroplayer.views.VideoActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by plorial on 6/28/16.
 */
public class DownloadVideoUrlsTask extends AsyncTask<String,Void, String> {

    private Context context;
    private int numberOfUrl;
    private String subRef;

    public DownloadVideoUrlsTask(Context context, int numberOfUrl, String subRef) {
        this.context = context;
        this.numberOfUrl = numberOfUrl;
        this.subRef = subRef;
    }

    @Override
    protected String doInBackground(String... url) {
        File file = downloadFromUrl(url[0] ,context);
        return getNeededLine(file, numberOfUrl);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Intent intent = new Intent(context,VideoActivity.class);
        intent.putExtra(VideoActivity.VIDEO_PATH, s);
        intent.putExtra(VideoActivity.SUB_REF, subRef);
        context.startActivity(intent);
    }

    public static File downloadFromUrl(String url, Context context){
        InputStream inputStream = null;
        OutputStream output = null;
        File file = null;
        try {
            File outputDir = context.getCacheDir();

            file = File.createTempFile("video_urls",".txt", outputDir);
            file.deleteOnExit();

            URL uri = new URL(url);
            URLConnection connection = uri.openConnection();

            inputStream = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int n = - 1;
            output = new FileOutputStream( file );
            while ( (n = inputStream.read(buffer)) != -1)
            {
                output.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private static String getNeededLine(File file, int number){
        try {
            java.util.List<String> paths = readAllLines(file);
            String[] strings = paths.toArray(new String[paths.size()]);
            return strings[number];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> readAllLines(File file) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            List<String> result = new ArrayList<String>();
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                result.add(line);
            }
            return result;
        }finally {
            reader.close();
        }
    }
}