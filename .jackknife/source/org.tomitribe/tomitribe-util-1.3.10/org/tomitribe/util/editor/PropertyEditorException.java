package org.tomitribe.util.editor;

public class PropertyEditorException extends IllegalArgumentException {
   public PropertyEditorException() {
   }

   public PropertyEditorException(Throwable cause) {
      super(cause);
   }

   public PropertyEditorException(String message) {
      super(message);
   }

   public PropertyEditorException(String message, Throwable cause) {
      super(message, cause);
   }
}
