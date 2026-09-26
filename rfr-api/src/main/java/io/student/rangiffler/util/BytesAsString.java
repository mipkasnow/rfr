package io.student.rangiffler.util;

import static java.nio.charset.StandardCharsets.UTF_8;

public record BytesAsString(byte[] content) {

  public String string() {
    return content == null ? "" : new String(content, UTF_8);
  }
}
