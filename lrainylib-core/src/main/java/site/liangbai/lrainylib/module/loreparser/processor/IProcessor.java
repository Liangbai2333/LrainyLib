package site.liangbai.lrainylib.module.loreparser.processor;

import java.util.regex.Matcher;

public interface IProcessor {
    Matcher receive(CharSequence input);
}
