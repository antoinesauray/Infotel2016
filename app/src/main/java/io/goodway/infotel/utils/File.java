package io.goodway.infotel.utils;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import io.goodway.infotel.sync.HttpRequest;

/**
 * Created by antoine on 5/15/16.
 */
public class File {

    private static final String TAG="File";

    public static boolean isImageFile(String path) {
        Log.d(TAG, path);
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.indexOf("image") == 0;
    }
    public static boolean isVideoFile(String path) {
        Log.d(TAG, path);
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.indexOf("video") == 0;
    }
    public static boolean isPdfFile(String path) {
        Log.d(TAG, path);
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.indexOf("application/pdf") == 0;
    }
    public static boolean isYoutubeFile(String path) {
        Log.d(TAG, path);
        return path.startsWith("https://youtu.be/");
    }

    public static final String getNameFromUrl(String url){
        return url.substring(url.lastIndexOf("/")+1);
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new java.io.File(uri.getPath())).toString());

        }

        return extension;
    }

    public static String getPath(Context context, Uri uri){
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }

    // usually, subclasses of AsyncTask are declared inside the activity class.
// that way, you can easily modify the UI thread from here
    public static class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private HttpRequest.Action<java.io.File> action;
        private ProgressDialog dialog;
        private String filename;

        public DownloadTask(Context context, String filename, ProgressDialog dialog, HttpRequest.Action<java.io.File> action) {
            this.context = context;
            this.dialog = dialog;
            this.action = action;
            this.filename = filename;
        }

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/data/"+filename);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress((int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            if(dialog!=null) {
                dialog.show();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            dialog.dismiss();
            if (result != null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            }
            else{
                action.action(new java.io.File(result));
            }
        }
    }

}