package com.github.hanshsieh.pixivjjfx.token;

import com.github.hanshsieh.pixivj.model.AuthResult;
import com.github.hanshsieh.pixivj.model.Credential;
import com.github.hanshsieh.pixivj.model.GrantType;
import com.github.hanshsieh.pixivj.oauth.PixivOAuthClient;
import com.github.hanshsieh.pixivj.token.TokenProvider;
import com.github.hanshsieh.pixivj.token.TokenRefresher;
import com.github.hanshsieh.pixivjjfx.stage.LoginStageListener;
import com.github.hanshsieh.pixivjjfx.stage.PixivLoginStage;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.scene.web.WebErrorEvent;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements {@link TokenProvider} by showing a web view using JavaFX, and ask the user
 * to login to Pixiv. After the user is logged in, the authorization code is used to obtain the
 * access token. After obtaining the access token and refresh token, it tries to refresh the token
 * if possible, and only shows the web view again if needed.
 */
public class WebViewTokenProvider implements TokenProvider {

  /**
   * This class is for building an instance of {@link WebViewTokenProvider}.
   */
  public static class Builder {
    private PixivOAuthClient authClient;
    private PixivLoginStage loginStage;
    private TokenRefresher tokenRefresher;

    /**
     * Sets the authentication client.
     * Notice that the authentication client won't be closed when the {@link WebViewTokenProvider}
     * is closed.
     * @param authClient Authentication client.
     * @return This instance.
     */
    @NonNull
    public Builder setAuthClient(@NonNull PixivOAuthClient authClient) {
      this.authClient = authClient;
      return this;
    }
    @NonNull
    public Builder setLoginStage(@NonNull PixivLoginStage loginStage) {
      this.loginStage = loginStage;
      return this;
    }
    @NonNull
    public Builder setTokenRefresher(@NonNull TokenRefresher tokenRefresher) {
      this.tokenRefresher = tokenRefresher;
      return this;
    }
    public WebViewTokenProvider build() {
      return new WebViewTokenProvider(this);
    }
  }
  private static final Logger logger = LoggerFactory.getLogger(WebViewTokenProvider.class);
  private final PixivOAuthClient authClient;
  private final PixivLoginStage loginStage;
  private final TokenRefresher tokenRefresher;
  private CompletableFuture<String> futureAuthCode = new CompletableFuture<>();
  private WebViewTokenProvider(@NonNull Builder builder) {
    Validate.notNull(builder.authClient, "Auth client is not set");
    Validate.notNull(builder.tokenRefresher, "Token refresher is not set");
    Validate.notNull(builder.loginStage, "Login stage is not set");
    this.authClient = builder.authClient;
    this.tokenRefresher = builder.tokenRefresher;
    this.loginStage = builder.loginStage;
    this.loginStage.addLoginListener(new LoginStageListener() {
      @Override
      public void onAuthCode(@NonNull String code) {
        futureAuthCode.complete(code);
      }

      @Override
      public void onWebError(@NonNull WebErrorEvent event) {
        logger.error("Web error occurs when doing web login: {}", event);
      }
    });
    this.loginStage.setOnCloseRequest((event) -> {
      logger.debug("Window is closed");
      futureAuthCode.completeExceptionally(new IllegalStateException("Window is closed by user"));
    });
  }

  /**
   * Gets the access token. If there's no existing token, or the original token cannot be refreshed,
   * it shows a web view to ask the user to login.
   * This method blocks until the access token is obtained.
   * An exception is thrown if it fails to obtain the access token.
   * If the user closes the web view, an excception is also thrown.
   * @return Access token
   * @throws IOException IO error occurs.
   */
  @Override
  public synchronized String getAccessToken() throws IOException {
    try {
      return tokenRefresher.getAccessToken();
    } catch (IllegalStateException ex) {
      logger.info("No existing access token: {}", ex.getMessage());
    } catch (Exception ex) {
      logger.warn("Failed to get access token: ", ex);
    }
    logger.info("Trying to login again to get a new access token...");
    futureAuthCode = new CompletableFuture<>();
    Platform.runLater(loginStage::start);
    try {
      String authCode = futureAuthCode.get();
      Credential cred = new Credential();
      cred.setGrantType(GrantType.AUTHORIZATION_CODE);
      cred.setCode(authCode);
      cred.setCodeVerifier(loginStage.getCodeVerifier()
          .orElseThrow(() -> new IllegalStateException("No code verifier")));
      cred.setRedirectUri(Credential.REDIRECT_URI_AUTH_CALLBACK);
      cred.setIncludePolicy(true);
      AuthResult result = authClient.authenticate(cred);
      String accessToken = result.getAccessToken();
      tokenRefresher.updateTokens(
          result.getAccessToken(),
          result.getRefreshToken(),
          Instant.now().plusSeconds(result.getExpiresIn()));
      return accessToken;
    } catch (Exception ex) {
      throw new IOException("Failed to get auth code", ex);
    } finally {
      Platform.runLater(loginStage::stop);
    }
  }

  @Override
  public void close() throws IOException {

  }
}
