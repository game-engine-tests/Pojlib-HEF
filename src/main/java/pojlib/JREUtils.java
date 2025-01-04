package pojlib;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.system.ErrnoException;
import android.system.Os;
import android.util.ArrayMap;
import android.util.Log;

import com.oracle.dalvik.VMLauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import pojlib.android.BuildConfig;

public class JREUtils {
    private JREUtils() {}

    public static String LD_LIBRARY_PATH;
    public static String jvmLibraryPath;
    private static String sNativeLibDir;
    private static String runtimeDir;

    public static String findInLdLibPath(String libName) {
        if(Os.getenv("LD_LIBRARY_PATH")==null) {
            try {
                if (LD_LIBRARY_PATH != null) {
                    Os.setenv("LD_LIBRARY_PATH", LD_LIBRARY_PATH, true);
                }
            }catch (ErrnoException e) {
                e.printStackTrace();
            }
            return libName;
        }
        for (String libPath : Os.getenv("LD_LIBRARY_PATH").split(":")) {
            File f = new File(libPath, libName);
            if (f.exists() && f.isFile()) {
                return f.getAbsolutePath();
            }
        }
        return libName;
    }

    public static ArrayList<File> locateLibs(File path) {
        ArrayList<File> returnValue = new ArrayList<>();
        File[] list = path.listFiles();
        if(list != null) {
            for(File f : list) {
                if(f.isFile() && f.getName().endsWith(".so")) {
                    returnValue.add(f);
                }else if(f.isDirectory()) {
                    returnValue.addAll(locateLibs(f));
                }
            }
        }
        return returnValue;
    }

    public static void initJavaRuntime() {
        dlopen(findInLdLibPath("libjli.so"));
        if(!dlopen("libjvm.so")){
            Log.w("DynamicLoader","Failed to load with no path, trying with full path");
            dlopen(jvmLibraryPath+"/libjvm.so");
        }
        dlopen(findInLdLibPath("libverify.so"));
        dlopen(findInLdLibPath("libjava.so"));
        dlopen(findInLdLibPath("libnet.so"));
        dlopen(findInLdLibPath("libnio.so"));
        dlopen(findInLdLibPath("libawt.so"));
        dlopen(findInLdLibPath("libawt_headless.so"));
        dlopen(findInLdLibPath("libfreetype.so"));
        dlopen(findInLdLibPath("libfontmanager.so"));
        for(File f : locateLibs(new File(runtimeDir + "/lib"))) {
            dlopen(f.getAbsolutePath());
        }
        dlopen(sNativeLibDir + "/libopenal.so");
        dlopen(sNativeLibDir + "/libopuscodec.so");
    }

    public static void redirectAndPrintJRELog(Activity activity) {
        Log.v("jrelog","Log starts here");
        JREUtils.logToLogger(Logger.getInstance(activity));
        new Thread(new Runnable(){
            int failTime = 0;
            ProcessBuilder logcatPb;
            @Override
            public void run() {
                try {
                    if (logcatPb == null) {
                        logcatPb = new ProcessBuilder().command("logcat", "-v", "brief", "-s", "jrelog:I", "LIBGL:I").redirectErrorStream(true);
                    }
                            Log.i("jrelog-logcat","Clearing logcat");
                    new ProcessBuilder().command("logcat", "-c").redirectErrorStream(true).start();
                    Log.i("jrelog-logcat","Starting logcat");
                    java.lang.Process p = logcatPb.start();

                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = p.getInputStream().read(buf)) != -1) {
                        String currStr = new String(buf, 0, len);
                        Logger.getInstance(activity).appendToLog(currStr);
                    }
                            if (p.waitFor() != 0) {
                        Log.e("jrelog-logcat", "Logcat exited with code " + p.exitValue());
                        failTime++;
                        Log.i("jrelog-logcat", (failTime <= 10 ? "Restarting logcat" : "Too many restart fails") + " (attempt " + failTime + "/10");
                        if (failTime <= 10) {
                            run();
                        } else {
                            Logger.getInstance(activity).appendToLog("ERROR: Unable to get more log.");
                        }
                            }
                } catch (Throwable e) {
                    Log.e("jrelog-logcat", "Exception on logging thread", e);
                    Logger.getInstance(activity).appendToLog("Exception on logging thread:\n" + Log.getStackTraceString(e));
                }
            }
        }).start();
        Log.i("jrelog-logcat","Logcat thread started");
    }

    public static void relocateLibPath(Context ctx, String jvmHome) {
        sNativeLibDir = ctx.getApplicationInfo().nativeLibraryDir;

        LD_LIBRARY_PATH = jvmHome + "/bin:" + jvmHome + "/lib:" +
                "/system/lib64:/vendor/lib64:/vendor/lib64/hw:" +
                sNativeLibDir;
    }

    public static void setJavaEnvironment(Activity activity, String gameDir, String vrModel, String jvmHome) throws Throwable {
        Map<String, String> envMap = new ArrayMap<>();
        envMap.put("POJLIB_NATIVEDIR", activity.getApplicationInfo().nativeLibraryDir);
        envMap.put("JAVA_HOME", jvmHome);
        envMap.put("HOME", gameDir);
        envMap.put("TMPDIR", activity.getCacheDir().getAbsolutePath());
        envMap.put("VR_MODEL", vrModel);
        envMap.put("POJLIB_RENDERER", "regal");

        envMap.put("LD_LIBRARY_PATH", LD_LIBRARY_PATH);
        envMap.put("PATH", jvmHome + "/bin:" + Os.getenv("PATH"));

        File customEnvFile = new File(Constants.getFilesDir(activity), "custom_env.txt");
        if (customEnvFile.exists() && customEnvFile.isFile()) {
            BufferedReader reader = new BufferedReader(new FileReader(customEnvFile));
            String line;
            while ((line = reader.readLine()) != null) {
                // Not use split() as only split first one
                int index = line.indexOf("=");
                envMap.put(line.substring(0, index), line.substring(index + 1));
            }
            reader.close();
        }
        envMap.put("LIBGL_ES", "2");
        for (Map.Entry<String, String> env : envMap.entrySet()) {
            Logger.getInstance(activity).appendToLog("Added custom env: " + env.getKey() + "=" + env.getValue());
            Os.setenv(env.getKey(), env.getValue(), true);
        }

        File serverFile = new File(jvmHome + "/lib/server/libjvm.so");
        jvmLibraryPath = jvmHome + "/lib/" + (serverFile.exists() ? "server" : "client");
        Log.d("DynamicLoader","Base LD_LIBRARY_PATH: "+LD_LIBRARY_PATH);
        Log.d("DynamicLoader","Internal LD_LIBRARY_PATH: "+jvmLibraryPath+":"+LD_LIBRARY_PATH);
        setLdLibraryPath(jvmLibraryPath+":"+LD_LIBRARY_PATH);
    }

    public static int launchJavaVM(Activity activity, List<String> JVMArgs, String[] mcArgs, String[] mcAdditionalArgs, String gameDir, String memoryValue, String vrModel, String mainClass, String jvmHome) throws Throwable {
        relocateLibPath(activity, jvmHome);
        setJavaEnvironment(activity, gameDir, vrModel, jvmHome);

        List<String> userArgs = getJavaArgs(activity, gameDir, jvmHome);

        //Add automatically generated args

        userArgs.add("-Xms" + memoryValue + "M");
        userArgs.add("-Xmx" + memoryValue + "M");

        userArgs.add("-XX:+UseZGC");
        userArgs.add("-XX:+ZGenerational"); // Not compatible with Java 17, perhaps these should be determined by the launcher and not Pojlib?
        userArgs.add("-XX:+UnlockExperimentalVMOptions");
        userArgs.add("-XX:+UseSignalChaining");
        userArgs.add("-XX:+DisableExplicitGC");
        userArgs.add("-XX:+UseCriticalJavaThreadPriority");

        userArgs.add("-Dorg.lwjgl.opengl.libname=libtinywrapper.so");
        userArgs.add("-Dorg.lwjgl.opengles.libname=/system/lib64/libGLESv3.so");
        userArgs.add("-Dorg.lwjgl.egl.libname=/system/lib64/libEGL_dri.so");

        userArgs.addAll(JVMArgs);
        System.out.println(JVMArgs);

        runtimeDir = jvmHome;

        initJavaRuntime();
        chdir(gameDir);
        userArgs.add(0,"java"); //argv[0] is the program name according to C standard.
        userArgs.add(mainClass);
        userArgs.addAll(Arrays.asList(mcArgs));
        userArgs.addAll(Arrays.asList(mcAdditionalArgs));

        Logger.getInstance(activity).appendToLog(BuildConfig.DEBUG ? String.join("\n", userArgs) : "Release mode, not dumping args."); // Dump args if debug mode

        int exitCode = VMLauncher.launchJVM(userArgs.toArray(new String[0]));
        Logger.getInstance(activity).appendToLog("Java Exit code: " + exitCode);
        return exitCode;
    }

    /**
     *  Gives an argument list filled with both the user args
     *  and the auto-generated ones (eg. the window resolution).
     * @param ctx The application context
     * @return A list filled with args.
     */
    public static List<String> getJavaArgs(Context ctx, String gameDir, String jvmHome) {
        return new ArrayList<>(Arrays.asList(
                "-Djava.home=" + jvmHome,
                "-Djava.io.tmpdir=" + ctx.getCacheDir().getAbsolutePath(),
                "-Duser.home=" + gameDir,
                "-Duser.language=" + System.getProperty("user.language"),
                "-Dos.name=Linux",
                "-Dos.version=Android-" + Build.VERSION.RELEASE,
                "-Dorg.lwjgl.librarypath=" + ctx.getApplicationInfo().nativeLibraryDir,
                "-Djna.boot.library.path=" + ctx.getApplicationInfo().nativeLibraryDir,
                "-Djna.nosys=true",
                "-Djava.library.path=" + ctx.getApplicationInfo().nativeLibraryDir,
                "-Dglfwstub.windowWidth=" + 1280,
                "-Dglfwstub.windowHeight=" + 720,
                "-Dglfwstub.initEgl=false",
                "-Dlog4j2.formatMsgNoLookups=true", //Log4j RCE mitigation
                "-Dnet.minecraft.clientmodname=Derifted"
        ));
    }

    public static native int chdir(String path);
    public static native void logToLogger(Logger logger);
    public static native boolean dlopen(String libPath);
    public static native void setLdLibraryPath(String ldLibraryPath);

    static {
        System.loadLibrary("pojavexec");
        System.loadLibrary("istdio");
    }
}
