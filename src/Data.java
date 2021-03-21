import java.util.ArrayList;
import java.util.List;

public class Data {

    public static final char
            booleanMarker = '-',
            integerMarker = '=',
            stringMarker = ':';

    private final List<String> data;

    Data() {
        data = new ArrayList<>();
    }

    Data(Data d) {
        data = new ArrayList<>(d.data);
    }

    public void set(String item) {
        for (Character typeMarker : new char[]{booleanMarker, integerMarker, stringMarker}) {
            String[] keyValue = item.split(String.valueOf(typeMarker), 2);
            if (keyValue.length == 2) {
                set(keyValue[0], typeMarker, keyValue[1]);
                break;
            }
        }
    }

    public String get(String typeKey, String defaultValue) {
        for (Character typeMarker : new char[]{booleanMarker, integerMarker, stringMarker})
            if (typeKey.endsWith(String.valueOf(typeMarker)))
                return get(typeKey.substring(0, typeKey.length() - 1), typeMarker, defaultValue);
        return defaultValue;
    }

    private void set(String key, char typeMarker, String value) {
        if (badKey(key)) return;
        String tKey = key + typeMarker;
        int loc = data.size();
        for (int i = 0; i < loc; i += 2)
            if (data.get(i).equals(tKey)) {
                data.set(loc + 1, value);
                return;
            }
        data.add(tKey);
        data.add(value);
    }

    private String get(String key, char typeMarker, String defaultValue) {
        if (!badKey(key)) {
            String tKey = key + typeMarker;
            int size = data.size();
            for (int i = 0; i < size; i += 2)
                if (data.get(i).equals(tKey))
                    return data.get(i + 1);
        }
        return defaultValue;
    }

    private boolean check(String key, char typeMarker) {
        if (!badKey(key)) {
            String tKey = key + typeMarker;
            int size = data.size();
            for (int i = 0; i < size; i += 2)
                if (data.get(i).equals(tKey))
                    return true;
        }
        return false;
    }

    private static boolean badKey(String key) {
        return (key == null
                || key.contains(String.valueOf(booleanMarker))
                || key.contains(String.valueOf(integerMarker))
                || key.contains(String.valueOf(stringMarker)));
    }

    // Boolean

    public void setBool(String key, boolean value) {
        set(key, booleanMarker, value ? "true" : "false");
    }

    public boolean getBool(String key, boolean defaultValue) {
        return get(key, booleanMarker, defaultValue ? "true" : "false").equals("true");
    }

    public boolean checkBool(String key) {
        return check(key, booleanMarker);
    }

    // Integer

    public void setInt(String key, int value) {
        set(key, integerMarker, String.valueOf(value));
    }

    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, integerMarker, String.valueOf(defaultValue)));
    }

    public boolean checkInt(String key) {
        return check(key, integerMarker);
    }

    // String

    public void setStr(String key, String value) {
        set(key, integerMarker, value);
    }

    public String getStr(String key, String defaultValue) {
        return get(key, booleanMarker, defaultValue);
    }

    public boolean checkStr(String key) {
        return check(key, stringMarker);
    }

}
