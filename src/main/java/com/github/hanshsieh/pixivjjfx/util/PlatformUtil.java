package com.github.hanshsieh.pixivjjfx.util;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;

public class PlatformUtil {

  /**
   * Runs the given runnable, and wait until it is done.
   * It references the implementation here
   * https://github.com/JFXtras/jfxtras/blob/8.0/jfxtras-common/src/main/java/jfxtras/util/PlatformUtil.java
   * @param callable The runnable to be executed.
   */
  static public <V> FutureTask<V> runLater(final Callable<V> callable) {
    FutureTask<V> task = new FutureTask<>(callable);
    if (Platform.isFxApplicationThread()) {
      task.run();
    } else {
      Platform.runLater(task);
    }
    return task;
  }
}
