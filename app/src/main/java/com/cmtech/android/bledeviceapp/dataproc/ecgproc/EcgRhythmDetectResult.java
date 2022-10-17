package com.cmtech.android.bledeviceapp.dataproc.ecgproc;

import com.cmtech.android.bledeviceapp.interfac.IJsonable;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EcgRhythmDetectResult implements IJsonable {
    private static final Map<String, Map<Integer, String>> RESULT_TABLES = new HashMap<>();

    static {
        Map<Integer, String> result = new HashMap<>(){{
            put(0, "窦性心律");
            put(1, "房颤");
            put(2, "未知异常");
            put(3, "噪声");
        }};
        RESULT_TABLES.put("1.0", result);

        result = new HashMap<>(){{
            put(0, "窦性心律");
            put(1, "房颤");
            put(2, "非房颤异常");
            put(3, "噪声");
        }};
        RESULT_TABLES.put("1.1", result);
    }

    public static Map<Integer, String> getResultTable(String ver) {
        return RESULT_TABLES.get(ver);
    }

    private final String ver;

    private final Map<Integer, String> resultTable;

    private final List<EcgRhythmDetectResultItem> items;

    public EcgRhythmDetectResult(String ver) {
        this.ver = ver;
        resultTable = RESULT_TABLES.get(this.ver);
        items = new ArrayList<>();
    }

    public void addItem(EcgRhythmDetectResultItem item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
    }

    public String getDescriptionFromLabel(int label) {
        return resultTable.get(label);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException {
        String itemStr = json.getString("items");
        ViseLog.e(itemStr);
        String[] itemStrArr = itemStr.split(",");
        for(String item : itemStrArr) {
            String[] item2 = item.split(":");
            EcgRhythmDetectResultItem rhythmDetectResultItem =
                    new EcgRhythmDetectResultItem(Long.parseLong(item2[0]), Integer.parseInt(item2[1]));
            items.add(rhythmDetectResultItem);
        }
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("ver", ver);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < items.size()-1; i++)
            sb.append(items.get(i)).append(',');
        sb.append(items.get(items.size()-1));
        json.put("items", sb.toString());
        return json;
    }

    public String toString() {
        StringBuilder rtn = new StringBuilder("ver:" + ver + "\n");
        for(EcgRhythmDetectResultItem item : items) {
            rtn.append(DateTimeUtil.timeToString(item.getStartTime())).append(":").append(resultTable.get(item.getLabel())).append("\n");
        }
        return rtn.toString();
    }
}
