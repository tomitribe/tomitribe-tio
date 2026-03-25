package org.tomitribe.util.editor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateEditor extends AbstractConverter {
   public static final Date NO_MIN = new Date(0L);
   public static final Date NO_MAX = new Date(2147483647L);
   private List<DateFormat> formats = new ArrayList();

   protected DateEditor(List<DateFormat> formats) {
      this.formats = formats;
   }

   public DateEditor() {
      this.formats.add(DateFormat.getInstance());
      this.formats.add(DateFormat.getDateInstance());
      this.formats.add(new SimpleDateFormat("yyyy-MM-dd"));
      this.formats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"));
   }

   @Override
   protected Object toObjectImpl(String text) {
      if (text != null && !text.isEmpty() && !"0".equals(text)) {
         if (Integer.valueOf(Integer.MAX_VALUE).toString().equals(text)) {
            return NO_MAX;
         } else {
            for (DateFormat format : this.formats) {
               try {
                  return format.parse(text);
               } catch (ParseException var6) {
               }
            }

            try {
               return this.complexParse(text);
            } catch (ParseException var5) {
               throw new PropertyEditorException(var5);
            }
         }
      } else {
         return NO_MIN;
      }
   }

   private Object complexParse(String text) throws ParseException {
      Locale locale = Locale.getDefault();
      int style = 2;
      int firstSpaceIndex = text.indexOf(32);
      if (firstSpaceIndex != -1) {
         String token = text.substring(0, firstSpaceIndex).intern();
         if (token.startsWith("locale")) {
            String localeStr = token.substring(token.indexOf(61) + 1);
            int underscoreIndex = localeStr.indexOf(95);
            if (underscoreIndex != -1) {
               String language = localeStr.substring(0, underscoreIndex);
               String country = localeStr.substring(underscoreIndex + 1);
               locale = new Locale(language, country);
            } else {
               locale = new Locale(localeStr);
            }

            int nextSpaceIndex = text.indexOf(32, firstSpaceIndex + 1);
            token = text.substring(firstSpaceIndex + 1, nextSpaceIndex);
            String styleStr = token.substring(token.indexOf(61) + 1);
            if ("SHORT".equalsIgnoreCase(styleStr)) {
               style = 3;
            } else if ("MEDIUM".equalsIgnoreCase(styleStr)) {
               style = 2;
            } else if ("LONG".equalsIgnoreCase(styleStr)) {
               style = 1;
            } else if ("FULL".equalsIgnoreCase(styleStr)) {
               style = 0;
            } else {
               style = 2;
            }

            text = text.substring(nextSpaceIndex + 1);
         }
      }

      DateFormat formats = DateFormat.getDateInstance(style, locale);
      return formats.parse(text);
   }

   protected String toStringImpl(Object value) {
      Date date = (Date)value;
      return ((DateFormat)this.formats.get(0)).format(date);
   }
}
