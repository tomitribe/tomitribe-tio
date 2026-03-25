package org.tomitribe.util.editor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

public class Editors {
   private Editors() {
   }

   public static PropertyEditor get(Class<?> type) {
      PropertyEditor editor = PropertyEditorManager.findEditor(type);
      if (editor != null) {
         return editor;
      } else {
         Class<Editors> c = Editors.class;

         try {
            Class<?> editorClass = c.getClassLoader().loadClass(c.getName().replace("Editors", type.getSimpleName() + "Editor"));
            PropertyEditorManager.registerEditor(type, editorClass);
            return PropertyEditorManager.findEditor(type);
         } catch (ClassNotFoundException var4) {
            return null;
         }
      }
   }
}
