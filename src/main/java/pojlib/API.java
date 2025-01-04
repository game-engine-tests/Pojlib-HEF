package pojlib;

import android.app.Activity;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to prepare for game launch, prepares final java arguments and sets up logging,
 * provides the aarguments for Regal and gives the JVM certain information useful for debugging.
 */
public class API {
    /**
     * Launch the game
     *
     * @param activity The activity of Android
     * @param username The user's username
     * @param versionName The Minecraft version name
     * @param gameDir The game directory, kind of like ".minecraft" for this
     * @param assetsDir The equivelant of .minecraft/assets
     * @param assetIndex This is the asset index Minecraft lists from the API
     * @param formattedUuid The user's UUID
     * @param accessToken The Minecraft login token
     * @param userType Should be "msa" always, "mojang" is legacy
     * @param memoryValue The amount of ram to allocate to Minecraft
     * @param vrModel The headset's model
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
            String versionType,
            String memoryValue,
            String vrModel,
            String mainClass,
            String[] additionalArgs,
            String[] mcAdditionalArgs,
            String jvmHome
    ) {
        String[] mcArgs = {"--username", username, "--version", versionName, "--gameDir", gameDir,
                "--assetsDir", assetsDir, "--assetIndex", assetIndex, "--uuid", formattedUuid,
                "--accessToken", accessToken, "--userType", userType, "--versionType", versionType};

        List<String> allArgs = new ArrayList<>(Arrays.asList(additionalArgs));
        JREUtils.redirectAndPrintJRELog(activity);
        VLoader.setActivity(activity); //MCXR
        VLoader.setAndroidInitInfo(activity); //Vivecraft
        try {
            JREUtils.launchJavaVM(activity, allArgs, mcArgs, mcAdditionalArgs, gameDir, memoryValue, vrModel, mainClass, jvmHome);
        } catch (Throwable t) {
            throw new RuntimeException("JVM has stopped.", t);
        }
    }

    @SuppressWarnings("unused")
    public static String getTestString() {
        return "Successful load!\n";
    }
}
