package org.apache.cordova.speech;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

public class SpeechSynthesis extends CordovaPlugin implements OnInitListener {

    private static final String LOG_TAG = "SpeechSynthesis";
    private static final int STOPPED = 0;
    private static final int INITIALIZING = 1;
    private static final int STARTED = 2;
    private TextToSpeech mTts = null;
    private int state = STOPPED;
    private CallbackContext startupCallbackContext;
    private CallbackContext callbackContext;
    private Hashtable<String, CallbackContext> contextMap = new Hashtable<String, CallbackContext>();
    private ConcurrentLinkedQueue<String> contextQueue = new ConcurrentLinkedQueue<String>();
    private Set<Voice> voiceList = null;

    private int androidAPILevel = android.os.Build.VERSION.SDK_INT;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";
        this.callbackContext = callbackContext;

        try {
            if (action.equals("speak")) {
                JSONObject utterance = args.getJSONObject(0);
                speak(callbackContext, utterance, false);
            } else if (action.equals("cancel")) {
                if (isReady()) {
                    // Speak an empty message but with flush set.
                    // Hopefully, this will allow for error events to be sent to unfinished utterances.
                    JSONObject utterance = new JSONObject();
                    utterance.put("text", "");
                    speak(callbackContext, utterance, true);
                } else {
                    fireErrorEvent(callbackContext, 6, "Not ready.");
                }
            } else if (action.equals("pause")) {
                Log.d(LOG_TAG, "Not implemented yet");
            } else if (action.equals("resume")) {
                Log.d(LOG_TAG, "Not implemented yet");
            } else if (action.equals("stop")) {
                if (isReady()) {
                    mTts.stop();
                    callbackContext.sendPluginResult(new PluginResult(status, result));
                } else {
                    fireErrorEvent(callbackContext, 6, "Not ready.");
                }
            } else if (action.equals("silence")) {
                if (isReady()) {
                    HashMap<String, String> map = null;
                    map = new HashMap<String, String>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, callbackContext.getCallbackId());
                    mTts.playSilence(args.getLong(0), TextToSpeech.QUEUE_ADD, map);
                    PluginResult pr = new PluginResult(PluginResult.Status.NO_RESULT);
                    pr.setKeepCallback(true);
                    callbackContext.sendPluginResult(pr);
                } else {
                    fireErrorEvent(callbackContext, 6, "Not ready.");
                }
            } else if (action.equals("startup")) {
                this.startupCallbackContext = callbackContext;
                if (mTts == null) {
                    state = SpeechSynthesis.INITIALIZING;
                    mTts = new TextToSpeech(cordova.getActivity().getApplicationContext(), this);
                }else{
            		getVoices(callbackContext);
                }
                PluginResult pluginResult = new PluginResult(status, SpeechSynthesis.INITIALIZING);
                pluginResult.setKeepCallback(true);
                startupCallbackContext.sendPluginResult(pluginResult);
            }
            else if (action.equals("shutdown")) {
                if (mTts != null) {
                    mTts.shutdown();
                }
                callbackContext.sendPluginResult(new PluginResult(status, result));
            }
            else if (action.equals("isLanguageAvailable")) {
                if (mTts != null) {
                    Locale loc = new Locale(args.getString(0));
                    int available = mTts.isLanguageAvailable(loc);
                    result = (available < 0) ? "false" : "true";
                    callbackContext.sendPluginResult(new PluginResult(status, result));
                }
            }
            return true;
        } catch (JSONException e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
        }
        return false;
    }

    private void speak(CallbackContext callbackContext, JSONObject utterance, boolean flush) {
        String text = null;

        // Track the utterance CallbackContext so that we can later send the events to the right place.
        contextMap.put(callbackContext.getCallbackId(), callbackContext);
        contextQueue.add(callbackContext.getCallbackId());

        try {
            text = utterance.getString("text");
        } catch (JSONException e) {
            // this should never happen
        }

        String lang = utterance.optString("lang", "en");
        mTts.setLanguage(new Locale(lang));

        String voiceCode = utterance.optString("voiceURI", null);
        if (voiceCode == null) {
            JSONObject voice = utterance.optJSONObject("voice");
            if (voice != null) {
                voiceCode = voice.optString("voiceURI", null);
            }
        }
        if (voiceCode != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Voice v : this.voiceList) {
                if (voiceCode.equals(v.getName())) {
                    mTts.setVoice(v);
                    //text+=" yay! found the voice!";
                }
            }
        }

        float pitch = (float) utterance.optDouble("pitch", 1.0);
        mTts.setPitch(pitch);

        float volume = (float) utterance.optDouble("volume", 0.5);
        // how to set volume

        float rate = (float) utterance.optDouble("rate", 1.0);
        mTts.setSpeechRate(rate);

        if (isReady()) {
            Log.d(LOG_TAG, "utterance id: " + callbackContext.getCallbackId() + ", text: " + text);
            if (androidAPILevel < 21) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, callbackContext.getCallbackId());
                map.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, Float.toString(volume));
                mTts.speak(text, flush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, map);
            } else { // android API level is 21 or higher...
                Bundle params = new Bundle();
                params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume);
                mTts.speak(text, flush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, params, callbackContext.getCallbackId());
            }
        } else {
            fireErrorEvent(callbackContext, 6, "Not ready.");
        }
    }

    private void getVoices(CallbackContext callbackContext) {
        JSONArray voices = new JSONArray();
        JSONObject voice;
        //List<TextToSpeech.EngineInfo> engines = mTts.getEngines();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
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
        }else{
            //Iterator<Locale> list = voiceList.iterator();
            Locale[] list = Locale.getAvailableLocales();
            Locale locale;
            //while (list.hasNext()) {
            //    locale = list.next();
            for (int i = 0; i < list.length; i++) {
                locale = list[i];
                voice = new JSONObject();
                if (mTts.isLanguageAvailable(locale) > 0) {     // ie LANG_COUNTRY_AVAILABLE or LANG_COUNTRY_VAR_AVAILABLE
                    try {
                        voice.put("voiceURI", locale.getLanguage()+"-"+locale.getCountry());
                        voice.put("name", locale.getDisplayLanguage(locale) + " " + locale.getDisplayCountry(locale));
                        voice.put("lang", locale.getLanguage()+"-"+locale.getCountry());
                        voice.put("localService", true);
                        voice.put("default", false);
                    } catch (JSONException e) {
                        // should never happen
                    }
                    voices.put(voice);
                }
            }
        }
        PluginResult result = new PluginResult(PluginResult.Status.OK, voices);
        result.setKeepCallback(false);
        startupCallbackContext.sendPluginResult(result);
    }

    private void fireEvent(CallbackContext callbackContext, String type) {
        JSONObject event = new JSONObject();

        Log.d(LOG_TAG, "fire event: " + type);

        try {
            event.put("type",type);
            if(type.equals("start")) {
                event.put("charIndex", 0);
                event.put("elapsedTime", 0);
                event.put("name", "");
            }
        } catch (JSONException e) {
            // this should never happen
        }
        PluginResult pr = new PluginResult(PluginResult.Status.OK, event);
        if(type.equals("end")) {
            pr.setKeepCallback(false);
        } else {
            pr.setKeepCallback(true);
        }

        if(callbackContext == null) {
            this.callbackContext.sendPluginResult(pr);
        } else {
            callbackContext.sendPluginResult(pr);
        }
    }

    private void fireErrorEvent(CallbackContext callbackContext, int errCode, String message) {
        JSONObject error = new JSONObject();
        Log.d(LOG_TAG, "fire event: error!");
        try {
            error.put("type","error");
            error.put("charIndex",0);
            error.put("elapsedTime",0);
            error.put("name","");
            error.put("error", errCode);
            error.put("message", message);
        } catch (JSONException e) {
            // this should never happen
        }
        if(callbackContext == null) {
            this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, error));
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, error));
        }
    }

    /**
     * Is the TTS service ready to play yet?
     *
     * @return
     */
    private boolean isReady() {
        return (state == SpeechSynthesis.STARTED) ? true : false;
    }

    /**
     * Called when the TTS service is initialized.
     *
     * @param status
     */
    public void onInit(int status) {
        if (mTts != null && status == TextToSpeech.SUCCESS) {
            state = SpeechSynthesis.STARTED;
            getVoices(this.startupCallbackContext);

            mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    Log.d(LOG_TAG, utteranceId + ": got completed utterance");
                    fireEvent(contextMap.get(utteranceId), "end");

                    // No longer need the CallbackContext for this utterance.
                    contextMap.remove(utteranceId);
                    if(utteranceId.equals(contextQueue.peek())) {
                        contextQueue.poll();
                    }
                }

                @Override
                public void onError(String utteranceId) {
                    this.onError(utteranceId, -99);
                }

                @Override
                public void onError(String utteranceId, int errorCode) {
                    int error;
                    String message;

                    Log.d(LOG_TAG, utteranceId + ": got utterance error");

                    switch(errorCode) {
                        case TextToSpeech.ERROR:
                            error = 6;
                            message = "Generic operation failure.";
                            break;

                        case TextToSpeech.ERROR_INVALID_REQUEST:
                            error = 6;
                            message = "Invalid request.";
                            break;

                        case TextToSpeech.ERROR_NETWORK:
                            error = 4;
                            message = "Network connectivity problem.";
                            break;

                        case TextToSpeech.ERROR_NETWORK_TIMEOUT:
                            error = 4;
                            message = "Network timeout.";
                            break;

                        case TextToSpeech.ERROR_NOT_INSTALLED_YET:
                            error = 5;
                            message = "Unfinished download of the voice data.";
                            break;

                        case TextToSpeech.ERROR_OUTPUT:
                            error = 3;
                            message = "Audio output issue.";
                            break;

                        case TextToSpeech.ERROR_SERVICE:
                            error = 6;
                            message = "Text-To-Speech failure.";
                            break;

                        case TextToSpeech.ERROR_SYNTHESIS:
                            error = 6;
                            message = "Unable to synthesize the text.";
                            break;

                        default:
                            error = 6;
                            message = "Unknown error.";
                            break;
                    }
                    fireErrorEvent(contextMap.get(utteranceId), error, message);

                    // No longer need the CallbackContext for this utterance.
                    contextMap.remove(utteranceId);
                    if(utteranceId.equals(contextQueue.peek())) {
                        contextQueue.poll();
                    }
                }

                @Override
                public void onStart(String utteranceId) {
                    String id;

                    Log.d(LOG_TAG, utteranceId + ": started talking");
                    fireEvent(contextMap.get(utteranceId),"start");

                    // Any contexts that have not been processed should be flagged with an error.
                    while(((id = contextQueue.peek()) != null) && !utteranceId.equals(id)) {
                        fireErrorEvent(contextMap.get(id), 6, "Lost event.");
                        contextMap.remove(id);
                        contextQueue.poll();
                    }
                }

            });
        } else if (status == TextToSpeech.ERROR) {
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
}
