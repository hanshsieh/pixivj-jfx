package com.github.hanshsieh.pixivjjfx;

import com.github.hanshsieh.pixivj.exception.AuthException;
import com.github.hanshsieh.pixivj.model.AuthResult;
import com.github.hanshsieh.pixivj.model.Credential;
import com.github.hanshsieh.pixivj.oauth.PixivOAuthClient;
import com.github.hanshsieh.pixivj.token.TokenProvider;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebViewTokenProvider implements TokenProvider {
  private static final Logger logger = LoggerFactory.getLogger(WebViewTokenProvider.class);
  private final PixivOAuthClient client;
  private String accessToken = null;
  private final PixivLoginStage stage;
  private final BlockingQueue<String> authCodeQueue = new LinkedBlockingQueue<>();
  public WebViewTokenProvider(@NonNull PixivOAuthClient client, @NonNull PixivLoginStage stage) {
    this.client = client;
    this.stage = stage;
    stage.addLoginListener(new LoginStageListener() {
      @Override
      public void onAuthCode(@NonNull String code) {
        authCodeQueue.offer(code);
      }

      @Override
      public void onWebError(@NonNull WebErrorEvent event) {
        logger.error("Web error occurs when doing web login: {}", event);
      }
    });
  }

  public WebViewTokenProvider(@NonNull PixivOAuthClient client) {
    this(client, new PixivLoginStage.Builder()
        .build());
  }

  @Override
  public String getAccessToken() throws AuthException, IOException {
    // TODO Add access token refresh mechanism
    if (accessToken != null) {
      return accessToken;
    }
    authCodeQueue.clear();
    Platform.runLater(stage::start);
    try {
      String authCode = authCodeQueue.poll(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      Credential cred = new Credential();
      cred.setGrantType("authorization_code");
      //cred.setCode(authCode);
      //cred.setCodeVerifier(stage.getCodeVerifier().orElseThrow());
      AuthResult result = client.authenticate(cred);
      accessToken = result.getAccessToken();
      return accessToken;
    } catch (Exception ex) {
      throw new IOException("Failed to get auth code", ex);
    } finally {
      Platform.runLater(stage::stop);
    }
  }

  @Override
  public void close() throws IOException {

  }
}
