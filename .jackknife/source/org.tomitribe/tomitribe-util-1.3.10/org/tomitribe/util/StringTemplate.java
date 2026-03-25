package org.tomitribe.util;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTemplate {
   public static final Pattern PATTERN = Pattern.compile("(\\{)((\\.|\\w)+)(})");
   private final String template;

   public StringTemplate(String template) {
      this.template = template;
   }

   public String format(final Map<String, Object> map) {
      Function<String, String> function = new Function<String, String>() {
         public String apply(String s) {
            Object value = map.get(s);
            return value != null ? value.toString() : "";
         }
      };
      return this.apply(function);
   }

   public String apply(Function<String, String> map) {
      Matcher matcher = PATTERN.matcher(this.template);
      StringBuffer buf = new StringBuffer();

      while (matcher.find()) {
         String key = matcher.group(2);
         if (key == null) {
            throw new IllegalStateException("Key is null. Template '" + this.template + "'");
         }

         String value;
         if (key.toLowerCase().endsWith(".lc")) {
            String key1 = key.substring(0, key.length() - 3);
            value = ((String)map.apply(key1)).toLowerCase();
         } else if (key.toLowerCase().endsWith(".uc")) {
            String key1 = key.substring(0, key.length() - 3);
            value = ((String)map.apply(key1)).toUpperCase();
         } else if (key.toLowerCase().endsWith(".cc")) {
            String key1 = key.substring(0, key.length() - 3);
            value = Strings.camelCase((String)map.apply(key1));
         } else {
            value = (String)map.apply(key);
         }

         if (value == null) {
            throw new IllegalStateException("Value is null for key '" + key + "'. Template '" + this.template + "'.");
         }

         matcher.appendReplacement(buf, value);
      }

      matcher.appendTail(buf);
      return buf.toString();
   }

   public String apply(Map<String, Object> map) {
      Matcher matcher = PATTERN.matcher(this.template);
      StringBuffer buf = new StringBuffer();

      while (matcher.find()) {
         String key = matcher.group(2);
         if (key == null) {
            throw new IllegalStateException("Key is null. Template '" + this.template + "'");
         }

         String value = this.value(map, key);
         if (key.toLowerCase().endsWith(".lc")) {
            value = this.value(map, key.substring(0, key.length() - 3)).toLowerCase();
         } else if (key.toLowerCase().endsWith(".uc")) {
            value = this.value(map, key.substring(0, key.length() - 3)).toUpperCase();
         } else if (key.toLowerCase().endsWith(".cc")) {
            value = Strings.camelCase(this.value(map, key.substring(0, key.length() - 3)));
         }

         if (value == null) {
            throw new IllegalStateException("Value is null for key '" + key + "'. Template '" + this.template + "'. Keys: " + Join.join(", ", map.keySet()));
         }

         matcher.appendReplacement(buf, value);
      }

      matcher.appendTail(buf);
      return buf.toString();
   }

   private String value(Map<String, Object> map, String key) {
      Object o = map.get(key);
      if (o == null) {
         throw new IllegalStateException("Missing entry " + key);
      } else {
         return o.toString();
      }
   }

   public Set<String> keys() {
      Set<String> keys = new TreeSet();
      Matcher matcher = PATTERN.matcher(this.template);

      while (matcher.find()) {
         String key = matcher.group(2);
         String op = key.toLowerCase();
         if (op.endsWith(".lc") || op.endsWith(".uc") || op.endsWith(".cc")) {
            key = key.substring(0, key.length() - 3);
         }

         keys.add(key);
      }

      return keys;
   }
}
