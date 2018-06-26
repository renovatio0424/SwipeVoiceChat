package com.example.renov.swipevoicechat.Util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

public class ImageUtil {
    public static String getFilePathFromUri(Uri uri, Context context) {
        if (uri == null) {
            return null;
        }

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();

            return path;
        } catch (Exception e) {
            if (uri.toString().startsWith("file://")) {
                return uri.toString().substring("file://".length());
            }

            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static File createTempImageFileForProfile() throws IOException {
        String imageFileName = "JPEG_PROFILE";
        String directoryName = "pesoft_crush";

        final File storageDir = new File(Environment.getExternalStorageDirectory(), directoryName);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        return new File(storageDir, imageFileName);
    }


    public static void deleteImageByUri(Uri tempUri, Context applicationContext) {
        try {
            File file = getFileFromUri(tempUri, applicationContext);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }

    public static File getFileFromUri(Uri uri, Context context) {
        String filePath = getFilePathFromUri(uri, context);
        if (filePath == null || "".equals(filePath)) {
            return null;
        }

        return new File(filePath);
    }
}
