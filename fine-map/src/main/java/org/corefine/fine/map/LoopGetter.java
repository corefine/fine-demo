package org.corefine.fine.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Fe by 2022/12/9 14:33
 */
public class LoopGetter {

    public static void main(String[] args) {
        Map<?, ?> request = new HashMap<>();
        Context context = new Context();
        set(getValue(request, "tags", "browser.name"), context::setBrowserName);
        set(getValue(request, "exception", "values", 0), d -> {
            set(getValue(d, "type"), context::setType);
            set(getValue(d, "value"), context::setValue);
        });
    }

    private static void set(Object v, Consumer<String> consumer) {
        if (v != null) {
            consumer.accept(v.toString());
        }
    }

    private static Object getValue(Object data, Object...keys) {
        if (data == null) {
            return null;
        }
        for (Object key : keys) {
            if (key instanceof Integer) {
                int k = (Integer) key;
                if (data instanceof List) {
                    List<?> l = (List<?>) data;
                    if (l.size() > k) {
                        data = l.get(k);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                if (data instanceof Map) {
                    data =  ((Map<?, ?>) data).get(key);
                } else {
                    return null;
                }
            }
            if (data == null) {
                return null;
            }
        }
        return data;
    }

    public static class Context {
        private String browserName;
        private String type;
        private String value;

        public void setBrowserName(String browserName) {
            this.browserName = browserName;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
