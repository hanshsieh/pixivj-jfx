package com.github.hanshsieh.pixivjjfx.pkce;

import java.security.SecureRandom;
import java.util.Random;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This class is used for generating the code verifier as defined by PKCE (RFC 7636).
 * https://tools.ietf.org/html/rfc7636
 * This class is thread-safe.
 */
public interface CodeVerifier {
  int MIN_LENGTH = 43;
  int MAX_LENGTH = 128;
  String ALPHABET =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
      "abcdefghijklmnopqrstuvwxyz" +
      "0123456789" +
      "-._~";

  /**
   * Generates a new code verifier of the given length.
   * The length must be within the limit as specified in RFC 7636; otherwise, an exception is
   * thrown.
   * @param length The length of the code verifier.
   * @return The code verifier.
   * @throws IllegalArgumentException The given length is invalid.
   */
  @NonNull
  String generate(int length);
}
