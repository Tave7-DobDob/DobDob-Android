package com.tave7.dobdob;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;

public class DownloadFileTask extends AsyncTask<String, Void, Bitmap> {
    String profileUrl;

    public DownloadFileTask(String profileUrl) {
        super();
        this.profileUrl = profileUrl;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        Bitmap bmp = null;
        try {
            URL url = new URL(profileUrl);
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) { e.printStackTrace(); }

        return bmp;
    }
}
