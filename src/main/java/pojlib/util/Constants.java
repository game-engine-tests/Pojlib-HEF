package pojlib.util;

import android.annotation.SuppressLint;
import android.content.Context;

public class Constants {
    private static String filesDir;

    @SuppressLint("SdCardPath")
    public static String getFilesDir(Context activity) {
        if (filesDir == null) {
            filesDir = "/sdcard" + activity.getExternalFilesDir(null).getAbsolutePath().substring(19);
        }
        return filesDir;
    }
}
