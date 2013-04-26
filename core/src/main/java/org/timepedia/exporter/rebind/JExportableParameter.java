package org.timepedia.exporter.rebind;

import com.google.gwt.core.ext.typeinfo.JEnumType;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;

/**
 *
 */
public class JExportableParameter {

  private final JParameter param;
  private final JExportableMethod method;
  private final ExportableTypeOracle xTypeOracle;

  public JExportableParameter(JExportableMethod exportableMethod, JParameter param) {
    this.param = param;
    this.method = exportableMethod;
    xTypeOracle = getExportableEnclosingType().getExportableTypeOracle();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    JExportableParameter that = (JExportableParameter) o;
    return getJsTypeOf().equals(that.getJsTypeOf());
  }

  public JExportableClassType getExportableEnclosingType() {
    return method.getEnclosingExportType();
  }

  public JExportableType getExportableType(boolean b) {
    return method.getExportable(param.getType(), b);
  }

  public String getExportParameterValue(String argName) {
    JEnumType enumType = param.getType().isEnum();
    if (enumType != null) {
      return "@" + enumType.getQualifiedSourceName() + "::valueOf(Ljava/lang/String;)(" + argName
          + ".@java.lang.String::toUpperCase()())";
    }
    String paramTypeName = param.getType().getQualifiedSourceName();
    JExportableType type = xTypeOracle.findExportableType(paramTypeName);

    if (type != null && type.needsExport()) {
      JExportableClassType cType = (JExportableClassType) type;
      if (xTypeOracle.isClosure((JExportableClassType) type)) {
        return argName + " == null ? null : (" + argName + ".constructor == $wnd."
            + cType.getJSQualifiedExportName() + " ? " + argName + "." + ClassExporter.GWT_INSTANCE
            + " : " + "@" + cType.getQualifiedExporterImplementationName() + "::"
            + "makeClosure(Lcom/google/gwt/core/client/JavaScriptObject;)(" + argName + "))";
      } else if (xTypeOracle.isExportable((JExportableClassType) type)) {
        return argName + "." + ClassExporter.GWT_INSTANCE;
      }
    }

    if (param.getType().isClass() != null && !xTypeOracle.isJavaScriptObject(param.getType())
        && !xTypeOracle.isString(param.getType())) {
      return "@org.timepedia.exporter.client.ExporterUtil::gwtInstance(Ljava/lang/Object;)("
          + argName + ")";
    }

    return argName;
  }

  public String getJNISignature() {
    return param.getType().getJNISignature();
  }

  public String getJsTypeOf() {

    if (param == null) {
      return "null";
    } else if (param.getType().isArray() != null) {
      return "array";
    } else if (param.getType().isPrimitive() != null) {
      JPrimitiveType prim = param.getType().isPrimitive();
      return prim == JPrimitiveType.BOOLEAN ? "boolean" : "number";
    } else if (xTypeOracle.isString(param.getType())) {
      return "string";
    } else if (xTypeOracle.isJavaScriptObject(param.getType())) {
      return "object";
    } else {
      String paramTypeName = param.getType().getQualifiedSourceName();
      JExportableType type = xTypeOracle.findExportableType(paramTypeName);
      if (type != null && type instanceof JExportableClassType
          && xTypeOracle.isClosure((JExportableClassType) type)) {
        return "'function'";
      }
      return "@" + param.getType().getQualifiedSourceName() + "::class";
    }
  }

  public JParameter getParam() {
    return param;
  }

  public String getToArrayFunc(String qsn, String argName) {
    String ret = "ExporterUtil.";
    String after = ")";
    if (qsn.equals("java.lang.String[]")) {
      ret += "toArrString";
    } else if (qsn.equals("java.util.Date[]")) {
      ret += "toArrDate";
    } else if (qsn.equals("double[]")) {
      ret += "toArrDouble";
    } else if (qsn.equals("float[]")) {
      ret += "toArrFloat";
    } else if (qsn.equals("long[]")) {
      ret += "toArrLong";
    } else if (qsn.equals("int[]")) {
      ret += "toArrInt";
    } else if (qsn.equals("byte[]")) {
      ret += "toArrByte";
    } else if (qsn.equals("char[]")) {
      ret += "toArrChar";
    } else {
      ret += "toArrObject";
      after = ", new " + qsn.replace("]", "ExporterUtil.length(" + argName + ")]") + after;
    }
    return ret + "(" + argName + after;
  }

  public String getTypeName() {
    return param.getType().getQualifiedSourceName();
  }

  @Override
  public int hashCode() {
    return param != null ? getJsTypeOf().hashCode() : 0;
  }

  public boolean isExportable() {
    String js = getJsTypeOf();
    return !js.contains("@") || getExportableType(false) != null;
  }

  @Override
  public String toString() {
    return param.getType().getSimpleSourceName();
  }

}
