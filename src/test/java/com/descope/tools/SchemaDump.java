package com.descope.tools;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Prints the JSON shape Jackson uses for the given model classes, for the SDK update agent's schema
 * check. Run with {@code mvn -q exec:java -Dexec.classpathScope=test
 * -Dexec.mainClass=com.descope.tools.SchemaDump -Dexec.args="com.descope.model.tenant.Tenant ..."}.
 */
public final class SchemaDump {
  // Same naming and inclusion as the SDK's request mapper (AbstractProxyImpl).
  private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

  private SchemaDump() {}

  public static void main(String[] args) throws Exception {
    Map<String, Object> out = new LinkedHashMap<>();
    for (String name : args) {
      out.put(name, shape(MAPPER.constructType(Class.forName(name)), new HashSet<>()));
    }
    System.out.println(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(out));
  }

  private static Map<String, Object> shape(JavaType type, Set<Class<?>> seen) {
    Map<String, Object> s = new LinkedHashMap<>();
    Class<?> c = type.getRawClass();
    if (c == byte[].class) {
      s.put("type", "string");
      s.put("format", "byte"); // Jackson writes byte[] as base64
    } else if (Date.class.isAssignableFrom(c) || c == Instant.class) {
      s.put("type", "integer");
      s.put("format", "int64"); // epoch millis: Jackson's Date default, InstantToMillisSerializer for Instant
    } else if (type.isCollectionLikeType() || type.isArrayType()) {
      s.put("type", "array");
      s.put("items", shape(type.getContentType(), seen));
    } else if (type.isMapLikeType() || JsonNode.class.isAssignableFrom(c) || c == Object.class) {
      s.put("type", "object");
      s.put("additionalProperties", true);
    } else if (c == String.class || c == Character.class || c == char.class || c.isEnum()) {
      s.put("type", "string");
    } else if (c == Boolean.class || c == boolean.class) {
      s.put("type", "boolean");
    } else if (c == Long.class || c == long.class || c == BigInteger.class) {
      s.put("type", "integer");
      s.put("format", "int64");
    } else if (Number.class.isAssignableFrom(c) && c != BigDecimal.class || c.isPrimitive()) {
      s.put("type", c == Double.class || c == double.class || c == Float.class || c == float.class ? "number" : "integer");
    } else if (c == BigDecimal.class) {
      s.put("type", "number");
    } else if (!seen.add(c)) {
      s.put("type", "object"); // recursive model, already described above
    } else {
      BeanDescription bean = MAPPER.getSerializationConfig().introspect(type);
      Map<String, Object> props = new LinkedHashMap<>();
      for (BeanPropertyDefinition p : bean.findProperties()) {
        if (p.couldSerialize()) {
          Map<String, Object> prop = shape(p.getPrimaryType(), seen);
          List<String> aliases = new ArrayList<>();
          p.findAliases().forEach(a -> aliases.add(a.getSimpleName()));
          if (!aliases.isEmpty()) {
            prop.put("aliases", aliases); // other names Jackson accepts when parsing
          }
          props.put(p.getName(), prop);
        }
      }
      s.put("type", "object");
      s.put("properties", props);
      seen.remove(c);
    }
    return s;
  }
}
