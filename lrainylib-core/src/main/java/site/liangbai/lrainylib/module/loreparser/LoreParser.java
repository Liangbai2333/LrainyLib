package site.liangbai.lrainylib.module.loreparser;

import site.liangbai.lrainylib.module.loreparser.processor.IProcessor;
import site.liangbai.lrainylib.module.loreparser.processor.impl.KeyValueLoreProcessor;
import site.liangbai.lrainylib.module.loreparser.util.Pair;

import java.util.regex.Matcher;

public final class LoreParser {
    private static final IProcessor KEY_VALUE_LORE_PROCESSOR = new KeyValueLoreProcessor();

    public static Pair<String, String> parseKeyValue(CharSequence input) {
        Matcher matcher = KEY_VALUE_LORE_PROCESSOR.receive(input);

        if (matcher == null) return null;

        return new Pair<>(matcher.group(1), matcher.group(2));
    }
}
