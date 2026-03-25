package org.tomitribe.util;

public class Escapes {
   private Escapes() {
   }

   public static final String unescape(String oldstr) {
      StringBuilder newString = new StringBuilder(oldstr.length());
      boolean sawBackslash = false;

      for (int i = 0; i < oldstr.length(); i++) {
         int cp = oldstr.codePointAt(i);
         if (oldstr.codePointAt(i) > 65535) {
            i++;
         }

         if (!sawBackslash) {
            if (cp == 92) {
               sawBackslash = true;
            } else {
               newString.append(Character.toChars(cp));
            }
         } else if (cp == 92) {
            sawBackslash = false;
            newString.append('\\');
            newString.append('\\');
         } else {
            switch (cp) {
               case 49:
               case 50:
               case 51:
               case 52:
               case 53:
               case 54:
               case 55:
                  i--;
               case 48:
                  if (i + 1 == oldstr.length()) {
                     newString.append(Character.toChars(0));
                  } else {
                     i++;
                     int digits = 0;

                     for (int j = 0; j <= 2 && i + j != oldstr.length(); j++) {
                        int ch = oldstr.charAt(i + j);
                        if (ch < 48 || ch > 55) {
                           break;
                        }

                        digits++;
                     }

                     if (digits == 0) {
                        i--;
                        newString.append('\u0000');
                     } else {
                        try {
                           int value = Integer.parseInt(oldstr.substring(i, i + digits), 8);
                           newString.append(Character.toChars(value));
                        } catch (NumberFormatException var12) {
                           throw new IllegalArgumentException("invalid octal value for \\0 escape");
                        }

                        i += digits - 1;
                     }
                  }
                  break;
               case 56:
               case 57:
                  throw new IllegalArgumentException("illegal octal digit");
               case 58:
               case 59:
               case 60:
               case 61:
               case 62:
               case 63:
               case 64:
               case 65:
               case 66:
               case 67:
               case 68:
               case 69:
               case 70:
               case 71:
               case 72:
               case 73:
               case 74:
               case 75:
               case 76:
               case 77:
               case 78:
               case 79:
               case 80:
               case 81:
               case 82:
               case 83:
               case 84:
               case 86:
               case 87:
               case 88:
               case 89:
               case 90:
               case 91:
               case 92:
               case 93:
               case 94:
               case 95:
               case 96:
               case 100:
               case 103:
               case 104:
               case 105:
               case 106:
               case 107:
               case 108:
               case 109:
               case 111:
               case 112:
               case 113:
               case 115:
               case 118:
               case 119:
               default:
                  newString.append('\\');
                  newString.append(Character.toChars(cp));
                  break;
               case 85:
                  if (i + 8 > oldstr.length()) {
                     throw new IllegalArgumentException("string too short for \\U escape");
                  }

                  i++;

                  int jx;
                  for (jx = 0; jx < 8; jx++) {
                     if (oldstr.charAt(i + jx) > 127) {
                        throw new IllegalArgumentException("illegal non-ASCII hex digit in \\U escape");
                     }
                  }

                  int value = 0;

                  try {
                     value = Integer.parseInt(oldstr.substring(i, i + jx), 16);
                  } catch (NumberFormatException var9) {
                     throw new IllegalArgumentException("invalid hex value for \\U escape");
                  }

                  newString.append(Character.toChars(value));
                  i += jx - 1;
                  break;
               case 97:
                  newString.append('\u0007');
                  break;
               case 98:
                  newString.append("\\b");
                  break;
               case 99:
                  if (++i == oldstr.length()) {
                     throw new IllegalArgumentException("trailing \\c");
                  }

                  cp = oldstr.codePointAt(i);
                  if (cp > 127) {
                     throw new IllegalArgumentException("expected ASCII after \\c");
                  }

                  newString.append(Character.toChars(cp ^ 64));
                  break;
               case 101:
                  newString.append('\u001b');
                  break;
               case 102:
                  newString.append('\f');
                  break;
               case 110:
                  newString.append('\n');
                  break;
               case 114:
                  newString.append('\r');
                  break;
               case 116:
                  newString.append('\t');
                  break;
               case 117:
                  if (i + 4 > oldstr.length()) {
                     throw new IllegalArgumentException("string too short for \\u escape");
                  }

                  i++;

                  int jx;
                  for (jx = 0; jx < 4; jx++) {
                     if (oldstr.charAt(i + jx) > 127) {
                        throw new IllegalArgumentException("illegal non-ASCII hex digit in \\u escape");
                     }
                  }

                  int value = 0;

                  try {
                     value = Integer.parseInt(oldstr.substring(i, i + jx), 16);
                  } catch (NumberFormatException var10) {
                     throw new IllegalArgumentException("invalid hex value for \\u escape");
                  }

                  newString.append(Character.toChars(value));
                  i += jx - 1;
                  break;
               case 120:
                  if (i + 2 > oldstr.length()) {
                     throw new IllegalArgumentException("string too short for \\x escape");
                  }

                  i++;
                  boolean sawBrace = false;
                  if (oldstr.charAt(i) == '{') {
                     i++;
                     sawBrace = true;
                  }

                  int j;
                  for (j = 0; j < 8 && (sawBrace || j != 2); j++) {
                     int ch = oldstr.charAt(i + j);
                     if (ch > 127) {
                        throw new IllegalArgumentException("illegal non-ASCII hex digit in \\x escape");
                     }

                     if (sawBrace && ch == 125) {
                        break;
                     }

                     if ((ch < 48 || ch > 57) && (ch < 97 || ch > 102) && (ch < 65 || ch > 70)) {
                        throw new IllegalArgumentException(String.format("illegal hex digit #%d '%c' in \\x", ch, ch));
                     }
                  }

                  if (j == 0) {
                     throw new IllegalArgumentException("empty braces in \\x{} escape");
                  }

                  int value = 0;

                  try {
                     value = Integer.parseInt(oldstr.substring(i, i + j), 16);
                  } catch (NumberFormatException var11) {
                     throw new IllegalArgumentException("invalid hex value for \\x escape");
                  }

                  newString.append(Character.toChars(value));
                  if (sawBrace) {
                     j++;
                  }

                  i += j - 1;
            }

            sawBackslash = false;
         }
      }

      if (sawBackslash) {
         newString.append('\\');
      }

      return newString.toString();
   }
}
