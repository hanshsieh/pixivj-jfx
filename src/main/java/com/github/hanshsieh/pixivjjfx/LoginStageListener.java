package com.github.hanshsieh.pixivjjfx;

import javafx.scene.web.WebErrorEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface LoginStageListener {

  /**
   * Called when the OAuth authentication code is obtained.
   * @param code Code.
   */
  void onAuthCode(@NonNull String code);

  /**
   * Called when an web error occurs.
   * @param event Event.
   */
  void onWebError(@NonNull WebErrorEvent event);
}
