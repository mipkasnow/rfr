package io.student.rangiffler.util;

import static java.nio.charset.StandardCharsets.UTF_8;

public record StringAsBytes(String content) {

  public byte[] bytes() {
    return content == null ? new byte[0] : content.getBytes(UTF_8);
  }
}
