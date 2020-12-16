package site.liangbai.lrainylib.module.loreparser;

import site.liangbai.lrainylib.module.loreparser.processor.IProcessor;
import site.liangbai.lrainylib.module.loreparser.processor.impl.KeyValueLoreProcessor;
import site.liangbai.lrainylib.module.loreparser.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public final class LoreParseApi {
    private static final IProcessor KEY_VALUE_LORE_PROCESSOR = new KeyValueLoreProcessor();

    public static Pair<String, String> parseKeyValue(CharSequence input) {
        Matcher matcher = KEY_VALUE_LORE_PROCESSOR.receive(input);

        if (matcher == null) return null;

        return new Pair<>(matcher.group(1), matcher.group(2));
    }

    public static Map<String, String> parseKeyValue(List<String> lore) {
        Map<String, String> map = new HashMap<>();

        for (String s : lore) {
            Pair<String, String> pair = parseKeyValue(s);

            if (pair != null) {
                map.put(pair.getKey(), pair.getValue());
            }
        }

        return map;
    }

    public static List<Pair<String, String>> parseKeyValueForList(List<String> lore) {
        List<Pair<String, String>> list = new ArrayList<>();

        for (String s : lore) {
            Pair<String, String> pair = parseKeyValue(s);

            list.add(pair);
        }

        return list;
    }
}
