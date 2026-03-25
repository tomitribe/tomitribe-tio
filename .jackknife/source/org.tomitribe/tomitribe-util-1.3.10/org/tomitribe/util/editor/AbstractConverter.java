package org.tomitribe.util.editor;

import java.beans.PropertyEditorSupport;

public abstract class AbstractConverter extends PropertyEditorSupport {
   public void setAsText(String text) throws IllegalArgumentException {
      this.setValue(this.toObjectImpl(text));
   }

   protected abstract Object toObjectImpl(String var1);
}
