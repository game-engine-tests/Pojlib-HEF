package pojlib.input.gamepad;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import pojlib.API;
import pojlib.util.Constants;

public class GamepadMapStore {
    private static final File STORE_FILE = new File(Constants.getFilesDir(API.getActivity()), "gamepad_map.json");
    private static GamepadMapStore sMapStore;
    private GamepadMap mInMenuMap;
    private GamepadMap mInGameMap;
    private static GamepadMapStore createDefault() {
        GamepadMapStore mapStore = new GamepadMapStore();
        mapStore.mInGameMap = GamepadMap.getDefaultGameMap();
        mapStore.mInMenuMap = GamepadMap.getDefaultMenuMap();
        return mapStore;
    }

    private static void loadIfNecessary() {
        if(sMapStore == null) return;
        load();
    }

    public static void load() {
        GamepadMapStore mapStore = null;
        if(STORE_FILE.exists() && STORE_FILE.canRead()) {
            try {
                mapStore = new Gson().fromJson(new FileReader(STORE_FILE), GamepadMapStore.class);
            } catch (JsonParseException | IOException e) {
                Log.w("GamepadMapStore", "Map store failed to load!", e);
            }
        }
        if(mapStore == null) mapStore = createDefault();
        sMapStore = mapStore;
    }

    public static GamepadMap getGameMap() {
        loadIfNecessary();
        return sMapStore.mInGameMap;
    }

    public static GamepadMap getMenuMap() {
        loadIfNecessary();
        return sMapStore.mInMenuMap;
    }
}
