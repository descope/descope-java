package com.descope.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.Instant;

public class InstantToMillisStringSerializer extends StdSerializer<Instant> {

  public InstantToMillisStringSerializer() {
    super(Instant.class);
  }

  @Override
  public void serialize(Instant value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(String.valueOf(value.toEpochMilli()));
  }
}
