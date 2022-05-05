package com.example.android;

import android.speech.tts.TextToSpeech;

public interface TextToSpeechInitListener {
    void onSuccess(TextToSpeech tts);
    void onFailure(TextToSpeech tts);
}
