package com.github.hanshsieh.pixivjjfx.pkce;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This class is used to generate the code challenge as defined in PKCE (RFC 76736).
 * See https://tools.ietf.org/html/rfc7636
 */
public interface CodeChallenge {
  /**
   * Generates the code challenge from the given code verifier.
   * @param codeVerifier Code verifier.
   * @return Generated code challenge.
   */
  @NonNull
  String generate(@NonNull String codeVerifier);

  /**
   * Gets the code challenge method.
   * @return Code challenge method.
   */
  @NonNull
  String getMethod();
}
