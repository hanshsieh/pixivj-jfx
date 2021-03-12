# Pixivj-jfx
Supporting library for Pixivj using JavaFX.  
![Java CI](https://github.com/hanshsieh/pixivj-jfx/workflows/Java%20CI/badge.svg)  

# Usage
## Getting access-token using web view login
```java
import com.github.hanshsieh.pixivj.oauth.PixivOAuthClient;
import com.github.hanshsieh.pixivj.token.ThreadedTokenRefresher;
import com.github.hanshsieh.pixivj.token.TokenRefresher;
import com.github.hanshsieh.pixivjjfx.stage.PixivLoginStage;
import java.io.Closeable;
import javafx.application.Application;
import javafx.stage.Stage;
public class Test extends Application {
  @Override
  public void start(Stage primaryStage) {
    // Simulate a worker thread
    new Thread(() -> {
      PixivOAuthClient authClient = null;
      TokenRefresher tokenRefresher = null;
      PixivLoginStage loginStage;
      try {
        authClient = new PixivOAuthClient.Builder().build();
        tokenRefresher = new ThreadedTokenRefresher.Builder()
            .setAuthClient(authClient)
            .build();
        loginStage = new PixivLoginStage.Builder().buildInFxThread();
        WebViewTokenProvider tokenProvider = new WebViewTokenProvider.Builder()
            .setAuthClient(authClient)
            .setTokenRefresher(tokenRefresher)
            .setLoginStage(loginStage)
            .build();
        String accessToken = tokenProvider.getAccessToken();
        System.out.printf("Access token: %s", accessToken);
      } catch (Exception ex) {
        ex.printStackTrace();
      } finally {
        closeQuietly(authClient);
        closeQuietly(tokenRefresher);
      }
    }).start();
  }

  private static void closeQuietly(Closeable closeable) {
    if (closeable == null) {
      return;
    }
    try {
      closeable.close();
    } catch (Exception ex) {
      // Do nothing
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
```