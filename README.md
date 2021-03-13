# Pixivj-jfx
Supporting library for Pixivj using JavaFX.  
![Java CI](https://github.com/hanshsieh/pixivj-jfx/workflows/Java%20CI/badge.svg)  
[![Maven Central](https://img.shields.io/maven-central/v/com.github.hanshsieh/pixivjjfx.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.hanshsieh%22%20AND%20a:%22pixivjjfx%22)  

# Usage
## Dependency
Because of a dependency it uses, please add the following settings to your `pom.xml` (For Maven).
```xml
<project>
  <repositories>
    <repository>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
      <id>central</id>
      <name>bintray</name>
      <url>https://jcenter.bintray.com</url>
    </repository>
  </repositories>
</project>
```
Then, add this library to the dependencies of your project.  
Check out [here](https://mvnrepository.com/artifact/com.github.hanshsieh/pixivjjfx) for the available
versions.
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

# Contribution
## Style
Please follow the [Google coding style](https://google.github.io/styleguide/javaguide.html).  
You may apply the IntelliJ style file [here](https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml).  

## Release
Follow the guide at [here](https://central.sonatype.org/pages/apache-maven.html) to setup your PGP key and 
`settings.xml`.  
Update the version in `pom.xml` appropriately.  
Then, run
```
mvn -Duser.name="$(git config --get user.name)" clean deploy -P release
```
