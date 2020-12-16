package site.liangbai.lrainylib.module.loreparser.processor.impl;

import site.liangbai.lrainylib.module.loreparser.processor.IProcessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class KeyValueLoreProcessor implements IProcessor {
    public static final Pattern KEY_VALUE_SPILT_PATTERN = Pattern.compile("(.+):\\s?(.+)");

    @Override
    public Matcher receive(CharSequence input) {
        Matcher matcher = KEY_VALUE_SPILT_PATTERN.matcher(input);
        if (!matcher.matches()) return null;
        return matcher;
    }
}
