package com.example.android;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class TextToSpeechInitializer {
    private Context context;
    private static TextToSpeech talk;
    private TextToSpeechInitListener callback;
    private Locale locale;

    public TextToSpeechInitializer(Context context, TextToSpeechInitListener initializer){
        this.context = context;
        if(initializer != null){
            callback = initializer;
        }
        this.locale = Locale.KOREAN;
        initialize();
    }

    public void initialize(){
        talk = new TextToSpeech(context, new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int status){
                if (status == TextToSpeech.SUCCESS){
                    talk.setLanguage(locale);
                    callback.onSuccess(talk);
                }else{
                    callback.onFailure(talk);
                    Log.e("TTS","TextToSpeechInitialize 에러");
                }
            }
        });
    }
}
