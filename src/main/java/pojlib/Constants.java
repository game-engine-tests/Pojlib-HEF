package pojlib;

import android.annotation.SuppressLint;
import android.content.Context;

public class Constants {
    private static String filesDir;

    @SuppressLint("SdCardPath")
    public static String getFilesDir(Context activity) {
        if (filesDir == null) {
            filesDir = activity.getFilesDir().getAbsolutePath();
        }
        return filesDir;
    }
}
