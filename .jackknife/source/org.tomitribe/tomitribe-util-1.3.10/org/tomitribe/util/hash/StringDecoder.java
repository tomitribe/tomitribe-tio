package org.tomitribe.util.hash;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.IdentityHashMap;
import java.util.Map;

final class StringDecoder {
   private static final ThreadLocal<Map<Charset, CharsetDecoder>> decoders = new ThreadLocal<Map<Charset, CharsetDecoder>>() {
      protected Map<Charset, CharsetDecoder> initialValue() {
         return new IdentityHashMap();
      }
   };

   private StringDecoder() {
   }

   public static String decodeString(ByteBuffer src, Charset charset) {
      CharsetDecoder decoder = getDecoder(charset);
      CharBuffer dst = CharBuffer.allocate((int)((double)src.remaining() * decoder.maxCharsPerByte()));

      try {
         CoderResult cr = decoder.decode(src, dst, true);
         if (!cr.isUnderflow()) {
            cr.throwException();
         }

         cr = decoder.flush(dst);
         if (!cr.isUnderflow()) {
            cr.throwException();
         }
      } catch (CharacterCodingException var5) {
         throw new IllegalStateException(var5);
      }

      return dst.flip().toString();
   }

   private static CharsetDecoder getDecoder(Charset charset) {
      Preconditions.checkNotNull(charset, "charset is null");
      Map<Charset, CharsetDecoder> map = (Map<Charset, CharsetDecoder>)decoders.get();
      CharsetDecoder d = (CharsetDecoder)map.get(charset);
      if (d != null) {
         d.reset();
         d.onMalformedInput(CodingErrorAction.REPLACE);
         d.onUnmappableCharacter(CodingErrorAction.REPLACE);
         return d;
      } else {
         d = charset.newDecoder();
         d.onMalformedInput(CodingErrorAction.REPLACE);
         d.onUnmappableCharacter(CodingErrorAction.REPLACE);
         map.put(charset, d);
         return d;
      }
   }
}
