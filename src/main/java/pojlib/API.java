package pojlib;

import android.app.Activity;

import pojlib.util.JREUtils;
import pojlib.util.VLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is the only class used by the launcher to communicate and talk to pojlib. This keeps pojlib and launcher separate.
 * If we ever make breaking change to either project, we can make a new api class to accommodate for those changes without
 * having to make changes to either project deeply.
 */
public class API {
    /**
     * Launch the game
     *
     * @param activity The activity of android
     */
    @SuppressWarnings("unused")
    public static void startGame(
            Activity activity,
            String username,
            String versionName,
            String gameDir,
            String assetsDir,
            String assetIndex,
            String formattedUuid,
            String accessToken,
            String userType,
            String memoryValue,
            String questModel
    ) {
        String[] mcArgs = {"--username", username, "--version", versionName, "--gameDir", gameDir,
                "--assetsDir", assetsDir, "--assetIndex", assetIndex, "--uuid", formattedUuid,
                "--accessToken", accessToken, "--userType", userType, "--versionType", "release"};

        List<String> allArgs = new ArrayList<>(Arrays.asList(mcArgs));
        JREUtils.redirectAndPrintJRELog();
        VLoader.setAndroidInitInfo(activity);
        try {
            JREUtils.launchJavaVM(activity, allArgs, gameDir, memoryValue, questModel);
        } catch (Throwable t) {
            throw new RuntimeException("JVM has stopped.", t);
        }
    }
}
