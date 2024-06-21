package com.unsoldriceball.adventurenote.note_system;

import com.google.gson.*;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static com.unsoldriceball.adventurenote.ANMain.ID_MOD;


public class ANJsonEditor
{
    private static final String PATH_GAME = Loader.instance().getConfigDir().getParentFile().getAbsolutePath();     //ファイル生成先のディレクトリ
    private static final String PATH_JSON = PATH_GAME + File.separator + ID_MOD + "_data" + ".json";
    private static final File FILE_JSON = new File(PATH_JSON);
    private static final Gson GSON = new Gson();
    private static final Gson GSON_forReader = new GsonBuilder().setPrettyPrinting().create();
    public static final String KEY_FIRSTJOIN = "AlreadyPlayers";




    //getValue_Json_Raw()で取得したJsonArrayをArrayList<String>に変換する関数。
    //実際にデータを取得しているのはgetValue_Json_Raw()。
    //uuidにnullを渡すと、keyの親がID_MODになる。
    public static ArrayList<String> getValue_Json(UUID uuid, String key)
    {
        final ArrayList<String> _RESULT = new ArrayList<>();
        final JsonArray _DATA = getValue_Json_Raw(uuid, key);

        if (_DATA != null)
        {
            for(JsonElement __je : _DATA)
            {
                _RESULT.add(__je.getAsString());
            }
        }
        return _RESULT;
    }



    //FILE_JSONからデータを取得する関数。FILE_JSONには配列のデータしか保存されない。
    private static JsonArray getValue_Json_Raw(UUID uuid, String key)
    {
        final JsonObject _JSON = readJsonFromFile();
        final String _KEY_PARENT;

        //uuidがnullの場合はID_MODを親とする。
        if (uuid != null)
        {
            _KEY_PARENT = uuid.toString();
        }
        else
        {
            _KEY_PARENT = ID_MOD;
        }

        if (_JSON != null && _JSON.has(_KEY_PARENT))
        {
            final JsonObject _PARENT = _JSON.getAsJsonObject(_KEY_PARENT);

            if (_PARENT.has(key))
            {
                return _PARENT.getAsJsonArray(key);
            }
        }
        return new JsonArray();
    }



    //Jsonに保存されているデータをプログラム内で使用しやすい文字列に変換する関数。
    //実際にプログラム内で使用するStringをそのまま保存すると、データファイルを見てもわかりにくいので、
    //データファイルには別のStringを保存してある。
    //それをまた元に戻す作業をfor()内で実行している。
    public static ArrayList<String> convertIDtoClassName(ArrayList<String> data, EnumANNoteType type)
    {
        if (!data.isEmpty())
        {
            if (type == EnumANNoteType.MOBS || type ==EnumANNoteType.BOSSES)
            {
                final ArrayList<String> _ARRAY_TEMP = new ArrayList<>();
                for (String __s : data)
                {
                    _ARRAY_TEMP.add(ANDataCollector.f_entities_instance.get(__s).getClass().getCanonicalName());
                }
                return _ARRAY_TEMP;
            }
            else if (type == EnumANNoteType.BIOMES)
            {
                final ArrayList<String> _ARRAY_TEMP = new ArrayList<>();
                for (String __s : data)
                {
                    _ARRAY_TEMP.add(ANDataCollector.f_biomes_instance.get(__s).getClass().getCanonicalName());
                }
                return _ARRAY_TEMP;
            }
        }
        return data;
    }



    //FILE_JSONからJsonObjectを取得する。
    private static JsonObject readJsonFromFile()
    {
        try (FileReader reader = new FileReader(FILE_JSON))
        {
            return GSON.fromJson(reader, JsonObject.class);
        }
        catch (Exception exc)
        {
            //何らかの原因で正しく取得できなかった場合は、空のJsonObjectを返す。
            //これにより、FILE_JSON(の.jsonファイル)の内容は最終的に初期化される。
            return new JsonObject();
        }
    }



    //指定したデータを更新/追加する関数。
    //uuid_parentがnullなら、ID_MODを親とする。
    public static void updateJsonData(UUID uuid_parent, String name_key, String value)
    {
        final JsonObject _JSON = readJsonFromFile();
        final String _KEY_PARENT;
        final JsonObject _OBJ_PARENT;
        final JsonArray _OBJ_CHILD;

        //uuidがnullの場合はID_MODを親とする。
        if (uuid_parent != null)
        {
             _KEY_PARENT = uuid_parent.toString();
        }
        else
        {
            _KEY_PARENT = ID_MOD;
        }

        if (_JSON.has(_KEY_PARENT))
        {
            _OBJ_PARENT = _JSON.getAsJsonObject(_KEY_PARENT);
        }
        else
        {
            _OBJ_PARENT = new JsonObject();
        }

        if (_OBJ_PARENT.has(name_key))
        {
            _OBJ_CHILD = _OBJ_PARENT.getAsJsonArray(name_key);
        }
        else
        {
            _OBJ_CHILD = new JsonArray();
        }
        _OBJ_CHILD.add(value);
        _OBJ_PARENT.add(name_key, _OBJ_CHILD);
        _JSON.add(_KEY_PARENT, _OBJ_PARENT);
        writeJsonToFile(_JSON);
    }



    //updateJsonData()での変更を実際の.jsonに反映する関数。
    private static void writeJsonToFile(JsonObject jsonObject)
    {
        try (FileWriter writer = new FileWriter(FILE_JSON))
        {
            GSON_forReader.toJson(jsonObject, writer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
