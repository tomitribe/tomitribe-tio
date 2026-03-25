package org.tomitribe.util.editor;

public class CharacterEditor extends AbstractConverter {
   @Override
   protected Object toObjectImpl(String text) {
      try {
         if (text.length() != 1) {
            throw new IllegalArgumentException("wrong size: " + text);
         } else {
            return new Character(text.charAt(0));
         }
      } catch (Exception var3) {
         throw new PropertyEditorException(var3);
      }
   }
}
