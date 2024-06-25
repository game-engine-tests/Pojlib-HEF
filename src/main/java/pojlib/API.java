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
     * @param username the user's username
     * @param versionName the minecraft version name
     * @param gameDir the game directory, kind of like ".minecraft" for this
     * @param assetsDir the equivelant of .minecraft/assets
     * @param assetIndex this is the assetindex minecraft lists from the api
     * @param formattedUuid this is the user's uuid
     * @param accessToken the minecraft bearer token
     * @param userType should always be "msa", used to be able to do "mojang"
     * @param memoryValue the amount of ram to give to minecraft
     * @param questModel the quest's model
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
            String questModel,
            String mainClass,
            String[] additionalArgs
    ) {
        String[] mcArgs = {"--username", username, "--version", versionName, "--gameDir", gameDir,
                "--assetsDir", assetsDir, "--assetIndex", assetIndex, "--uuid", formattedUuid,
                "--accessToken", accessToken, "--userType", userType, "--versionType", "release"};

        List<String> allArgs = new ArrayList<>(Arrays.asList(mcArgs));
        allArgs.addAll(Arrays.asList(additionalArgs));
        JREUtils.redirectAndPrintJRELog();
        VLoader.setAndroidInitInfo(activity);
        try {
            JREUtils.launchJavaVM(activity, allArgs, versionName, gameDir, memoryValue, questModel, mainClass);
        } catch (Throwable t) {
            throw new RuntimeException("JVM has stopped.", t);
        }
    }

    @SuppressWarnings("unused")
    public static String getTestString() {
        return "Successful load!\n";
    }
}
