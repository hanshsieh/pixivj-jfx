package com.github.hanshsieh.pixivjjfx.stage;

import com.github.hanshsieh.pixivjjfx.pkce.CodeChallenge;
import com.github.hanshsieh.pixivjjfx.pkce.CodeVerifier;
import com.github.hanshsieh.pixivjjfx.pkce.RandomCodeVerifier;
import com.github.hanshsieh.pixivjjfx.pkce.S256CodeChallenge;
import com.github.hanshsieh.pixivjjfx.util.PlatformUtil;
import com.widen.urlbuilder.UrlBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebErrorEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a stage for doing Pixiv login.
 */
public class PixivLoginStage extends Stage {
  public static class Builder {
    public static final String DEFAULT_LOGIN_URL = "https://app-api.pixiv.net/web/v1/login";
    public static final String DEFAULT_CALLBACK_URL = "https://app-api.pixiv.net/web/v1/users/auth/pixiv/callback";
    public static final String CLIENT_ANDROID = "pixiv-android";
    public static final String CLIENT_IOS = "pixiv-ios";
    public static final int DEFAULT_CODE_VERIFIER_LENGTH = 80;
    private String loginUrl = DEFAULT_LOGIN_URL;
    private String callbackUrl = DEFAULT_CALLBACK_URL;
    private int width = 1024;
    private int height = 768;
    private int codeVerifierLength = DEFAULT_CODE_VERIFIER_LENGTH;
    private CodeVerifier codeVerifier;
    private CodeChallenge codeChallenge;
    private String client = CLIENT_ANDROID;
    @NonNull
    public Builder setLoginUrl(@NonNull String loginUrl) {
      this.loginUrl = loginUrl;
      return this;
    }
    @NonNull
    public Builder setCallbackUrl(@NonNull String callbackUrl) {
      this.callbackUrl = callbackUrl;
      return this;
    }
    @NonNull
    public Builder setWidth(int width) {
      this.width = width;
      return this;
    }
    @NonNull
    public Builder setHeight(int height) {
      this.height = height;
      return this;
    }
    @NonNull
    public Builder setCodeVerifierLength(int length) {
      this.codeVerifierLength = length;
      return this;
    }
    @NonNull
    public Builder setCodeChallenge(@NonNull CodeChallenge codeChallenge) {
      this.codeChallenge = codeChallenge;
      return this;
    }
    @NonNull
    public Builder setClient(@NonNull String client) {
      this.client = client;
      return this;
    }
    @NonNull
    public Builder setCodeVerifier(@NonNull CodeVerifier codeVerifier) {
      this.codeVerifier = codeVerifier;
      return this;
    }
    @NonNull
    public PixivLoginStage build() {
      return new PixivLoginStage(this);
    }

    /**
     * Builds the stage in JavaFX application thread if needed.
     * If the current thread if JavaFX application thread, the stage will be created in current
     * thread. Otherwise, it creates the stage in the JavaFX application thread, and wait until
     * it is created.
     * @return Created stage.
     * @throws InterruptedException Interrupted when waiting the stage.
     * @throws ExecutionException The stage fails to be executed.
     */
    @NonNull
    public PixivLoginStage buildInFxThread() throws InterruptedException, ExecutionException {
      FutureTask<PixivLoginStage> task =
          PlatformUtil.runLater(() -> new PixivLoginStage.Builder().build());
      return task.get();
    }
  }
  private static final Logger logger = LoggerFactory.getLogger(PixivLoginStage.class);
  private static final String PARAM_CODE_CHALLENGE = "code_challenge";
  private static final String PARAM_CODE_CHALLENGE_METHOD = "code_challenge_method";
  private static final String PARAM_CODE = "code";
  private static final String PARAM_CLIENT = "client";
  private final String callbackUrl;
  private final String loginUrl;
  private final List<LoginStageListener> listeners = new ArrayList<>();
  private final CodeVerifier codeVerifier;
  private final CodeChallenge codeChallenge;
  private String codeVerifierValue;
  private final String client;
  private final int codeVerifierLen;
  private final WebView webView;
  private PixivLoginStage(@NonNull Builder builder) {
    Validate.notNull(builder.loginUrl, "Login URL not set");
    Validate.notNull(builder.callbackUrl, "Callback URL not set");
    Validate.notNull(builder.client, "Client not set");
    this.callbackUrl = builder.callbackUrl;
    this.loginUrl = builder.loginUrl;
    this.codeVerifierLen = builder.codeVerifierLength;
    this.client = builder.client;
    if (builder.codeVerifier != null) {
      this.codeVerifier = builder.codeVerifier;
    } else {
      this.codeVerifier = new RandomCodeVerifier();
    }
    if (builder.codeChallenge != null) {
      this.codeChallenge = builder.codeChallenge;
    } else {
      try {
        this.codeChallenge = new S256CodeChallenge();
      } catch (Exception ex) {
        throw new RuntimeException("Failed to construct default code challenge");
      }
    }
    setTitle("Pixiv Login");
    this.webView = new WebView();
    WebEngine engine = webView.getEngine();
    engine.locationProperty().addListener((obs, oldLocation, newLocation) ->
        onLocationChanged(engine.getLocation()));
    engine.setOnError(this::onWebError);
    StackPane pane = new StackPane(webView);
    Scene scene = new Scene(pane, builder.width, builder.height);
    setScene(scene);
  }

  /**
   * Starts the login process to ask the user to login.
   * If it is already started or has ended, it will be restarted.
   */
  public void start() {
    codeVerifierValue = codeVerifier.generate(codeVerifierLen);
    String codeChallengeValue = codeChallenge.generate(codeVerifierValue);
    String url = new UrlBuilder(loginUrl)
      .addParameter(PARAM_CODE_CHALLENGE_METHOD, "S256")
      .addParameter(PARAM_CODE_CHALLENGE, codeChallengeValue)
      .addParameter(PARAM_CLIENT, client)
      .toString();
    logger.debug("Loading login URL {}", url);
    webView.getEngine().load(url);
    show();
  }

  /**
   * Gets the generated code verifier value.
   * If a code verifier value hasn't been generated, it will be empty.
   * @return Code verifier value or empty.
   */
  @NonNull
  public Optional<String> getCodeVerifier() {
    return Optional.ofNullable(codeVerifierValue);
  }

  public void stop() {
    close();
  }

  private void onWebError(@NonNull WebErrorEvent event) {
    listeners.forEach((listener) -> {
      try {
        listener.onWebError(event);
      } catch (Exception ex) {
        logger.error("Failed to call listener for web error event: {}", event);
      }
    });
  }

  private void onLocationChanged(@Nullable String location) {
    // When directly loading from a html string, the location will be empty string.
    if (location == null || location.isEmpty()) {
      return;
    }
    try {
      UrlBuilder urlBuilder = new UrlBuilder(location);
      if (!urlBuilder.toString().startsWith(callbackUrl)) {
        return;
      }
      List<String> codes = urlBuilder.getQueryParameters().getOrDefault(PARAM_CODE, Collections.emptyList());
      if (codes.isEmpty()) {
        return;
      }
      String code = codes.get(0);
      listeners.forEach((listener) -> {
        try {
          listener.onAuthCode(code);
        } catch (Exception ex) {
          logger.error("Failed to call listener for auth code", ex);
        }
      });
      stop();
    } catch (Exception ex) {
      logger.warn("Unable to parse the location \"" + location + "\"");
    }
  }

  /**
   * Adds a listener for the events for the login process.
   * @param listener Listener to be added.
   */
  public void addLoginListener(@NonNull LoginStageListener listener) {
    this.listeners.add(listener);
  }
}
