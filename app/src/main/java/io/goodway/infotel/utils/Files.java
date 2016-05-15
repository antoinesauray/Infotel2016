package io.goodway.infotel.utils;

import java.net.URLConnection;

/**
 * Created by antoine on 5/15/16.
 */
public class Files {
    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.indexOf("image") == 0;
    }
    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.indexOf("video") == 0;
    }

}
