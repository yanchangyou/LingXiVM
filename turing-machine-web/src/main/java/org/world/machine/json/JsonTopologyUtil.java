package org.world.machine.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.*;

/**
 * json 拓扑结构工具类
 * 1.两个JSON是否拓扑等价：当且仅当两个JSON的拓扑结构相同
 * 2.两个JSON的拓扑结构是否相同：当且仅当两个JSON的拓扑特征值相同;
 * 3.两个JSON的拓扑特征值相同：当且仅当类型的层次、顺序相同
 *
 * @author yanchangyou
 */
public class JsonTopologyUtil {

    /**
     * JSON  值类型
     */
    enum JsonValueType {
        //json值类型
        NULL("null"),
        STRING("string"),
        INT("int"),
        DOUBLE("double"),
        BOOLEAN("boolean"),
        OBJECT("object"),
        ARRAY("array");

        String code;

        JsonValueType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * 比较两个json的拓扑结构是否相同
     *
     * @param json1
     * @param json2
     * @return
     */
    static boolean compareJsonTopology(JSON json1, JSON json2) {
        return getJsonTopology(json1).equals(getJsonTopology(json2));
    }

    /**
     * 获取json的拓扑结构:字符串形式
     *
     * @param json
     * @return
     */
    static String getJsonTopologyStringByInt(JSON json) {
        return getTopologyString(getJsonTopology(json)).replaceAll(",$", "");
    }

    /**
     * 获取json的拓扑结构:字符串形式
     *
     * @param json
     * @return
     */
    static String getJsonTopologyString(JSON json) {
        return getTopologyString(getJsonTopology(json))
                .replaceAll(",$", "")
                .replace('0', 'N')
                .replace('1', 'S')
                .replace('2', 'I')
                .replace('3', 'D')
                .replace('4', 'B')
                ;
    }

    /**
     * 获取json拓扑结构(字段类型：层级、顺序)
     *
     * @param json
     * @return
     */
    static List getJsonTopology(JSON json) {
        List fieldTypes = new ArrayList();

        makeJsonTypeList(json, fieldTypes);
        sortFieldTypes(fieldTypes);
        return fieldTypes;
    }

    /**
     * 获取json的类型结构
     *
     * @param value
     * @param fieldTypes
     */
    static void makeJsonTypeList(Object value, List fieldTypes) {

        if (value == null) {
            fieldTypes.add(JsonValueType.NULL.getCode());
        } else if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            List arrayList = new LinkedList();

            if (!array.isEmpty()) {
                makeJsonTypeList(array.get(0), arrayList);
            }
            fieldTypes.add(arrayList);

        } else if (value instanceof JSONObject) {
            List objectList = new ArrayList();

            for (Map.Entry<String, Object> entry : ((JSONObject) value).entrySet()) {
                Object subValue = entry.getValue();
                makeJsonTypeList(subValue, objectList);
            }
            fieldTypes.add(objectList);

        } else if (value instanceof Number) {
            if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
                fieldTypes.add(JsonValueType.DOUBLE.getCode());
            } else {
                fieldTypes.add(JsonValueType.INT.getCode());
            }
        } else if (value instanceof String) {
            fieldTypes.add(JsonValueType.STRING.getCode());
        } else if (value instanceof Boolean) {
            fieldTypes.add(JsonValueType.BOOLEAN.getCode());
        }
    }

    /**
     * 对类型结构进行排序
     *
     * @param fieldTypes
     */
    static void sortFieldTypes(List fieldTypes) {
        for (Object item : fieldTypes) {
            if (item instanceof List) {
                sortFieldTypes((List) item);
            }
        }
        fieldTypes.sort(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return getOrderValue(o1) - getOrderValue(o2);
            }
        });
        fieldTypes.sort(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                boolean isSameTypeList = o1 instanceof ArrayList && o2 instanceof ArrayList
                        || o1 instanceof LinkedList && o2 instanceof LinkedList;

                if (isSameTypeList) {
                    String topologyString1 = getTopologyString((List) o1);
                    String topologyString2 = getTopologyString((List) o2);
                    return topologyString1.compareTo(topologyString2);
                }
                return 0;
            }
        });
    }

    /**
     * 获取json的字符串拓扑结构
     *
     * @param topologyList
     * @return
     */
    static String getTopologyString(List topologyList) {
        StringBuilder builder = new StringBuilder();
        for (Object item : topologyList) {
            if (item instanceof List) {
                if (item instanceof ArrayList) {
                    builder.append("{");
                    builder.append(getTopologyString((List) item));
                    builder.append("},");
                } else if (item instanceof LinkedList) {
                    builder.append("[");
                    builder.append(getTopologyString((List) item));
                    builder.append("],");
                }
            } else {
                builder.append(getOrderValue(item)).append(",");
            }

        }
        return builder.toString().replaceAll("(,|;)(\\]|\\})", "$2");
    }

    /**
     * 获取json值的排序规则
     *
     * @param value
     * @return
     */
    static int getOrderValue(Object value) {
        if (JsonValueType.NULL.getCode().equals(value)) {
            return 0;
        }
        if (JsonValueType.STRING.getCode().equals(value)) {
            return 1;
        }
        if (JsonValueType.INT.getCode().equals(value)) {
            return 2;
        }
        if (JsonValueType.DOUBLE.getCode().equals(value)) {
            return 3;
        }
        if (JsonValueType.BOOLEAN.getCode().equals(value)) {
            return 4;
        }
        if (value instanceof ArrayList) {
            return 5;
        }
        if (value instanceof LinkedList) {
            return 6;
        }

        return 8;
    }

    static void convertToTopologyExpress(List topologyList, List<String> expressList, String parentKey, int level) {

        int index = 0;
        for (Object item : topologyList) {
            // primitive
            if (item instanceof String) {
                String express = parentKey + "." + item + index;
                express = express.replaceAll("(\\[\\]).([\\d\\w]+)", "$2$1");
                expressList.add(express);
            }
            // object
            else if (item instanceof ArrayList) {
                String key = parentKey + "." + (char) ('a' + level - 1) + index;
                convertToTopologyExpress((List) item, expressList, key, level + 1);
            }
            // array
            else if (item instanceof LinkedList) {

                //                String key = parentKey+"." + (char)('a'+level-1) + index + "[]";
                String key = parentKey + ".[]";
                convertToTopologyExpress((List) item, expressList, key, level + 1);
            }
            index++;
        }
    }
}
