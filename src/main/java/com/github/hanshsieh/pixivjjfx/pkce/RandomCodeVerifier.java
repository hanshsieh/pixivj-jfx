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
public class RandomCodeVerifier implements CodeVerifier {
  private final Random rand;

  /**
   * Creates an instance that uses the given random number generator to generate the random bytes.
   * @param rand Random number generator
   */
  public RandomCodeVerifier(@NonNull Random rand) {
    this.rand = rand;
  }

  /**
   * Creates an instance that uses {@link SecureRandom} as the source to generate the random
   * bytes.
   */
  public RandomCodeVerifier() {
    this(new SecureRandom());
  }

  /**
   * Generates a new code verifier of the given length.
   * The length must be within the limit as specified in RFC 7636; otherwise, an exception is
   * thrown.
   * @param length The length of the code verifier.
   * @return The code verifier.
   * @throws IllegalArgumentException The given length is invalid.
   */
  @NonNull
  public String generate(int length) {
    Validate.inclusiveBetween(MIN_LENGTH, MAX_LENGTH, length);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length; ++i) {
      builder.append(ALPHABET.charAt(rand.nextInt(ALPHABET.length())));
    }
    return builder.toString();
  }
}
