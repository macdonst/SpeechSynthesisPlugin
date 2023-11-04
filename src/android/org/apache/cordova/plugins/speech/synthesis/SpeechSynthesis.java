package org.apache.cordova.plugins.speech.synthesis;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.Voice;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public class SpeechSynthesis extends CordovaPlugin implements OnInitListener, OnUtteranceCompletedListener {

    private static final String LOG_TAG = "TTS";
    private static final int STOPPED = 0;
    private static final int INITIALIZING = 1;
    private static final int STARTED = 2;
    private TextToSpeech mTts = null;
    private int state = STOPPED;
    private CallbackContext startupCallbackContext;
    private CallbackContext callbackContext;

    private Set<Voice> voiceList = null;

    //private String startupCallbackId = "";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";
        this.callbackContext = callbackContext;

        try {
          switch (action) {
            case "speak":
              JSONObject utterance = args.getJSONObject(0);
              String text = utterance.getString("text");

              String lang = utterance.optString("lang", "en");
              mTts.setLanguage(new Locale(lang));

              String voiceCode = utterance.optString("voiceURI", null);
              for (Voice v : this.voiceList) {
                if (voiceCode.equals(v.getName())) {
                  mTts.setVoice(v);
                  //text+=" yay! found the voice!";
                }
              }

              float pitch = (float) utterance.optDouble("pitch", 1.0);
              mTts.setPitch(pitch);

              float volume = (float) utterance.optDouble("volume", 0.5);
              // how to set volume

              float rate = (float) utterance.optDouble("rate", 1.0);
              mTts.setSpeechRate(rate);

              if (isReady()) {
                HashMap<String, String> map = null;
                map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, callbackContext.getCallbackId());
                JSONObject event = new JSONObject();
                event.put("type", "start");
                event.put("charIndex", 0);
                event.put("elapsedTime", 0);
                event.put("name", "");
                PluginResult pr = new PluginResult(PluginResult.Status.OK, event);
                pr.setKeepCallback(true);
                callbackContext.sendPluginResult(pr);
                mTts.speak(text, TextToSpeech.QUEUE_ADD, map);
              } else {
                fireErrorEvent(callbackContext);
              }
              break;
            case "cancel":
              if (isReady()) {
                HashMap<String, String> map;
                map = new HashMap<>();
                //map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, callbackId);
                mTts.speak("", TextToSpeech.QUEUE_FLUSH, map);
                fireEndEvent(callbackContext);
              } else {
                fireErrorEvent(callbackContext);
              }
              break;
            case "pause":
            case "resume":
              Log.d(LOG_TAG, "Not implemented yet");
              break;
            case "stop":
              if (isReady()) {
                mTts.stop();
                callbackContext.sendPluginResult(new PluginResult(status, result));
              } else {
                fireErrorEvent(callbackContext);
              }
              break;
            case "silence":
              if (isReady()) {
                HashMap<String, String> map;
                map = new HashMap<>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, callbackContext.getCallbackId());
                mTts.playSilence(args.getLong(0), TextToSpeech.QUEUE_ADD, map);
                PluginResult pr = new PluginResult(PluginResult.Status.NO_RESULT);
                pr.setKeepCallback(true);
                callbackContext.sendPluginResult(pr);
              } else {
                fireErrorEvent(callbackContext);
              }
              break;
            case "startup":
              this.startupCallbackContext = callbackContext;
              if (mTts == null) {
                state = SpeechSynthesis.INITIALIZING;
                mTts = new TextToSpeech(cordova.getActivity().getApplicationContext(), this);
              } else {
                getVoices(callbackContext);
              }
              PluginResult pluginResult = new PluginResult(status, SpeechSynthesis.INITIALIZING);
              pluginResult.setKeepCallback(true);
              startupCallbackContext.sendPluginResult(pluginResult);
              break;
            case "shutdown":
              if (mTts != null) {
                mTts.shutdown();
              }
              callbackContext.sendPluginResult(new PluginResult(status, result));
              break;
            case "isLanguageAvailable":
              if (mTts != null) {
                Locale loc = new Locale(args.getString(0));
                int available = mTts.isLanguageAvailable(loc);
                result = (available < 0) ? "false" : "true";
                callbackContext.sendPluginResult(new PluginResult(status, result));
              }
              break;
          }
            return true;
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
        }
        return false;
    }

    private void getVoices(CallbackContext callbackContext) {
        JSONArray voices = new JSONArray();
        JSONObject voice;
        //List<TextToSpeech.EngineInfo> engines = mTts.getEngines();

      this.voiceList = mTts.getVoices();
      for (Voice v : this.voiceList) {
          Locale locale = v.getLocale();
          voice = new JSONObject();
          try {
              voice.put("voiceURI", v.getName());
              voice.put("name", locale.getDisplayLanguage(locale) + " " + locale.getDisplayCountry(locale));
              //voice.put("features", v.getFeatures());
              //voice.put("displayName", locale.getDisplayLanguage(locale) + " " + locale.getDisplayCountry(locale));
              voice.put("lang", locale.getLanguage()+"-"+locale.getCountry());
              voice.put("localService", !v.isNetworkConnectionRequired());
              voice.put("quality", v.getQuality());
              voice.put("default", false);
          } catch (JSONException e) {
              // should never happen
          }
          voices.put(voice);
      }
      PluginResult result = new PluginResult(PluginResult.Status.OK, voices);
        result.setKeepCallback(false);
        startupCallbackContext.sendPluginResult(result);
        mTts.setOnUtteranceCompletedListener(this);
    }

    private void fireEndEvent(CallbackContext callbackContext) {
        JSONObject event = new JSONObject();
        try {
            event.put("type","end");
        } catch (JSONException e) {
            // this will never happen
        }
        PluginResult pr = new PluginResult(PluginResult.Status.OK, event);
        pr.setKeepCallback(false);
        callbackContext.sendPluginResult(pr);
    }

    private void fireErrorEvent(CallbackContext callbackContext)
            throws JSONException {
        JSONObject error = new JSONObject();
        error.put("type","error");
        error.put("charIndex",0);
        error.put("elapsedTime",0);
        error.put("name","");
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, error));
    }

    /**
     * Is the TTS service ready to play yet?
     *
     */
    private boolean isReady() {
        return state == SpeechSynthesis.STARTED;
    }

    /**
     * Called when the TTS service is initialized.
     *
     */
    public void onInit(int status) {
        if (mTts != null && status == TextToSpeech.SUCCESS) {
            state = SpeechSynthesis.STARTED;
            getVoices(this.startupCallbackContext);


//                Putting this code in hear as a place holder. When everything moves to API level 15 or greater
//                we'll switch over to this way of tracking progress.
//                mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
//
//                    @Override
//                    public void onDone(String utteranceId) {
//                        Log.d(LOG_TAG, "got completed utterance");
//                        PluginResult result = new PluginResult(PluginResult.Status.OK);
//                        result.setKeepCallback(false);
//                        callbackContext.sendPluginResult(result);
//                    }
//
//                    @Override
//                    public void onError(String utteranceId) {
//                        Log.d(LOG_TAG, "got utterance error");
//                        PluginResult result = new PluginResult(PluginResult.Status.ERROR);
//                        result.setKeepCallback(false);
//                        callbackContext.sendPluginResult(result);
//                    }
//
//                    @Override
//                    public void onStart(String utteranceId) {
//                        Log.d(LOG_TAG, "started talking");
//                    }
//
//                });
        }
        else if (status == TextToSpeech.ERROR) {
            state = SpeechSynthesis.STOPPED;
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, SpeechSynthesis.STOPPED);
            result.setKeepCallback(false);
            this.startupCallbackContext.sendPluginResult(result);
        }
    }

    /**
     * Clean up the TTS resources
     */
    public void onDestroy() {
        if (mTts != null) {
            mTts.shutdown();
        }
    }

    /**
     * Once the utterance has completely been played call the speaks success callback
     */
    public void onUtteranceCompleted(String utteranceId) {
        fireEndEvent(callbackContext);
    }
}
