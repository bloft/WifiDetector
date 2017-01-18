package dk.lbloft.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class Replacer {
    private Pattern pattern;
    private HashMap<String, String> replaceMap;

    private Replacer(String start, String end) {
        pattern = Pattern.compile(Pattern.quote(start) + "(.*?)" + Pattern.quote(end));
        replaceMap = new HashMap<String, String>();
    }

    public static Replacer on() {
        return on("${", "}");
    }

    public static Replacer on(String start, String end) {
        return new Replacer(start, end);
    }

    public Replacer replace(String name, String value) {
        replaceMap.put(name, value);
        return this;
    }

    public Replacer replace(Map<String, String> map) {
        replaceMap.putAll(map);
        return this;
    }

    public Replacer replace(Properties props) {
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            replaceMap.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return this;
    }

    public String build(String input) {
        Matcher matcher = pattern.matcher(input);

        StringBuilder sb = new StringBuilder();

        int cursor = 0;
        while(cursor<input.length() && matcher.find(cursor)){
            sb.append(input.subSequence(cursor, matcher.start()));
            String value = replaceMap.get(matcher.group(1));
            sb.append(value!=null ? value : matcher.group());
            cursor = matcher.end();
        }
        sb.append(input.subSequence(cursor, input.length()));

        return sb.toString();
    }
}
