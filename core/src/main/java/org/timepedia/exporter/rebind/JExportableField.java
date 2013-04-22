package org.timepedia.exporter.rebind;

import com.google.gwt.core.ext.typeinfo.JField;

import org.timepedia.exporter.client.Export;

/**
 *
 */
public class JExportableField {

  private final JExportableClassType enclosingExportType;

  private final JField field;

  private String exportName;

  public JExportableField(JExportableClassType enclosingExportType, JField field) {
    this.enclosingExportType = enclosingExportType;
    this.field = field;
    Export ann = field.getAnnotation(Export.class);

    if (ann != null && ann.value().length() > 0) {
      exportName = ann.value();
    } else {
      exportName = field.getName();
    }
  }

  public String getJSExportName() {
    return exportName;
  }

  public String getJSNIReference() {
    return field.getEnclosingType().getQualifiedSourceName() + "::" + field.getName();
  }

  public String getJSQualifiedExportName() {
    return enclosingExportType.getJSQualifiedExportName() + "." + getJSExportName();
  }
}
