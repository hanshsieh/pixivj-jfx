package com.github.hanshsieh.pixivjjfx;

import com.github.hanshsieh.pixivj.exception.AuthException;
import com.github.hanshsieh.pixivj.oauth.PixivOAuthClient;
import com.github.hanshsieh.pixivj.token.TokenProvider;
import javafx.application.Platform;
import org.checkerframework.checker.nullness.qual.NonNull;
import java.io.IOException;

public class WebViewTokenProvider implements TokenProvider {
  private final PixivOAuthClient client;
  private String accessToken = null;
  private final PixivLoginStage stage;
  public WebViewTokenProvider(@NonNull PixivOAuthClient client, @NonNull PixivLoginStage stage) {
    this.client = client;
    this.stage = stage;
  }

  public WebViewTokenProvider(@NonNull PixivOAuthClient client) {
    this(client, new PixivLoginStage.Builder()
        .build());
  }

  @Override
  public String getAccessToken() throws AuthException, IOException {
    if (accessToken != null) {
      return accessToken;
    }
    Platform.runLater(() -> {
      stage.start();
      stage.show();
    });
    return null;
  }

  @Override
  public void close() throws IOException {

  }
}
