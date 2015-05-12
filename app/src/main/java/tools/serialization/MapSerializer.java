package tools.serialization;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSerializer {
    private static final String LOG_TAG = MapSerializer.class.getCanonicalName();

    public static Map<String, Object> serialize(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (obj != null) {
            for (Field property : obj.getClass().getDeclaredFields()) {
                try {
                    property.setAccessible(true);

                    String key = "";

                    for (Annotation annotation : property.getAnnotations()) {
                        if (annotation instanceof SerializedName) {
                            key = ((SerializedName) annotation).value();
                        }
                    }

                    if ( !TextUtils.isEmpty(key) ) {
                        if (MapSerializable.class.isAssignableFrom(property.getType())) {
                            map.put(key, serialize(property.get(obj)));
                        }
                        else if (List.class.isAssignableFrom(property.getType())) {
                            ParameterizedType listParameterizedType = (ParameterizedType) property.getGenericType();
                            Class listType = (Class) listParameterizedType.getActualTypeArguments()[0];
                            List serializedList = new ArrayList();

                            for (Object objInList : (List) property.get(obj)) {
                                serializedList.add(
                                        MapSerializable.class.isAssignableFrom(listType) ?
                                                serialize(objInList) :
                                                objInList
                                );
                            }

                            map.put(key, serializedList);
                        } else {
                            map.put(key, property.get(obj));
                        }
                    }
                }
                catch (IllegalAccessException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
        }

        return map;
    }

    public static <T> T deserialize(Map<String, Object> map, Class<T> classOfT) {
        try {
            T reconstructedObj = classOfT.newInstance();

            if (map == null)
                return reconstructedObj;

            for (Field property : classOfT.getDeclaredFields()) {
                property.setAccessible(true);

                String key = "";

                for (Annotation annotation : property.getAnnotations()) {
                    if (annotation instanceof SerializedName) {
                        key = ((SerializedName) annotation).value();
                    }
                }

                if ( !TextUtils.isEmpty(key) ) {
                    Object value = map.get(key);
                    if ( value != null ) {
                        Class propertyType = property.getType();
                        if (MapSerializable.class.isAssignableFrom(property.getType())) {
                            property.set(reconstructedObj, deserialize((Map) value, propertyType));
                        }
                        else if (List.class.isAssignableFrom(property.getType())) {
                            ParameterizedType listParameterizedType = (ParameterizedType) property.getGenericType();
                            Class listType = (Class) listParameterizedType.getActualTypeArguments()[0];

                            List deserializedList = new ArrayList();
                            for (Object objInList : (List) value) {
                                deserializedList.add(
                                        MapSerializable.class.isAssignableFrom(listType) ?
                                                deserialize((Map) objInList, listType) :
                                                objInList
                                );
                            }

                            property.set(reconstructedObj, propertyType.cast(deserializedList));
                        }
                        else {
                            if (propertyType.isPrimitive()) {
                                switch (propertyType.getSimpleName()) {
                                    case "byte":
                                        property.setByte(reconstructedObj, ((Number) value).byteValue());
                                        break;
                                    case "short":
                                        property.setShort(reconstructedObj, ((Number) value).shortValue());
                                        break;
                                    case "int":
                                        property.setInt(reconstructedObj, ((Number) value).intValue());
                                        break;
                                    case "long":
                                        property.setLong(reconstructedObj, ((Number) value).longValue());
                                        break;
                                    case "float":
                                        property.setFloat(reconstructedObj, ((Number) value).floatValue());
                                        break;
                                    case "double":
                                        property.setDouble(reconstructedObj, ((Number) value).doubleValue());
                                        break;
                                    case "char":
                                        property.setChar(reconstructedObj, ((Character) value).charValue());
                                        break;
                                    case "boolean":
                                        property.setBoolean(reconstructedObj, ((Boolean) value).booleanValue());
                                        break;
                                }
                            }
                            else {
                                property.set(reconstructedObj, propertyType.cast(value));
                            }
                        }

                    }
                }
            }

            return reconstructedObj;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String inspectMap(Map<String, Object> map) {
        return inspectMap(map, 0);
    }

    private static String inspectMap(Map<String, Object> map, int depth) {
        if (map == null) {
            return "MAP IS NULL";
        }

        StringBuilder builder = new StringBuilder();
        for (String key : map.keySet()) {
            for (int d = 0; d < depth; d++) {
                builder.append("  ");
            }

            builder.append(key);
            builder.append(" -> ");

            if ( map.get(key) instanceof Map ) {
                Map<String, Object> nestedMap = (Map<String, Object>) map.get(key);
                builder.append("(" + map.get(key).getClass().getName() + ") " + "[" + (depth+1) + "]");
                builder.append("\n" + inspectMap(nestedMap, depth+1));
            }
            else if ( map.get(key) instanceof List ) {
                builder.append("(" + map.get(key).getClass().getName() + ") ");

                for (Object objInList : (List) map.get(key)) {
                    builder.append("[[\n" + inspectMap((Map) objInList, depth) + "\n]]\n");
                }
            }
            else {
                builder.append("(" + map.get(key).getClass().getName() + ") ");
                builder.append("\"" + map.get(key).toString() + "\"");
            }
            builder.append("\n");
        }

        return builder.toString();
    }
}
