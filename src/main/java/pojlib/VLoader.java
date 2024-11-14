package pojlib;

import android.app.Activity;
import android.content.Context;

public class VLoader {
    static {
        System.loadLibrary("openxr_loader");
        System.loadLibrary("openvr_api");
        System.loadLibrary("mcxr-loader");
    }

    public static native void setAndroidInitInfo(Context ctx);

    public static native void setActivity(Activity activity);
}
