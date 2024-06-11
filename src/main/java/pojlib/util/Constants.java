package pojlib.util;

import android.os.Environment;

import java.io.File;

public class Constants {
    public static String USER_HOME = new File(Environment.getExternalStorageDirectory(),"Android/data/com.qcxr.qcxr/files").getAbsolutePath();
}