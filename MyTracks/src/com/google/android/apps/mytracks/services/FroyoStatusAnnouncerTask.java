/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.mytracks.services;

import static com.google.android.apps.mytracks.MyTracksConstants.TAG;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

import java.util.HashMap;

/**
 * This class will periodically announce the user's trip statistics for Froyo and future handsets.
 * This class will request and release audio focus.
 *
 * @author Sandor Dornbush
 */
public class FroyoStatusAnnouncerTask
    extends StatusAnnouncerTask
    implements OnUtteranceCompletedListener, OnAudioFocusChangeListener {
  
  private AudioManager audioManager;
  private final static HashMap<String, String> SPEECH_PARAMS = new HashMap<String, String>();
  static {
    SPEECH_PARAMS.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "not_used");
  }

  public FroyoStatusAnnouncerTask(Context context) {
    super(context);
    audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
  }

  @Override
  protected void onTtsInit(int status) {
    super.onTtsInit(status);
    if (status == TextToSpeech.SUCCESS) {
      tts.setOnUtteranceCompletedListener(this);
    }
  }

  @Override
  protected void speakAnnouncment(String announcement) {
    int result = audioManager.requestAudioFocus(this,
          TextToSpeech.Engine.DEFAULT_STREAM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
    if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
      Log.w(TAG, "FroyoStatusAnnouncerTask: Request audio focus failed.");
    }
    // We don't care about the utterance id.
    // It is supplied here to force onUtteranceCompleted to be called.
    tts.speak(announcement, TextToSpeech.QUEUE_FLUSH, SPEECH_PARAMS);
  }

  @Override
  public void onUtteranceCompleted(String utteranceId) {
    Log.d(TAG, "FroyoStatusAnnouncerTask: Abandoning audio focus.");
    audioManager.abandonAudioFocus(this);
  }

  @Override
  public void onAudioFocusChange(int focusChange) {
    // We don't care.
  }
}
