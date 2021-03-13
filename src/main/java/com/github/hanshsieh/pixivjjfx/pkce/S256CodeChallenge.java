package com.github.hanshsieh.pixivjjfx.pkce;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This class is used for generate S256 code challenge as defined in PKCE (RFC 76736).
 * See https://tools.ietf.org/html/rfc7636
 * This class is thread-safe, but if you need to generate the challenge in high volume, it's
 * advised to use multiple instances.
 */
public class S256CodeChallenge implements CodeChallenge {
  private final MessageDigest digest;
  private final Base64.Encoder base64Encoder;
  public S256CodeChallenge() throws NoSuchAlgorithmException {
    this.digest = MessageDigest.getInstance("SHA-256");
    this.base64Encoder = Base64.getUrlEncoder()
      .withoutPadding();
  }
  @NonNull
  @Override
  public synchronized String generate(@NonNull String codeVerifier) {
    try {
      digest.reset();
      digest.update(codeVerifier.getBytes(StandardCharsets.US_ASCII));
      return new String(base64Encoder.encode(digest.digest()), StandardCharsets.US_ASCII);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Failed to generate code challenge", ex);
    }
  }

  @Override
  public @NonNull String getMethod() {
    return "S256";
  }
}
