# SpeechSynthesisPlugin

W3C Web Speech API - Speech synthesis plugin for Cordova

## Installation

Using the command line tools run:

    cordova plugin add @okhiroyuki/cordova-plugin-speech-synthesis

## Example Code

This code from the above Github project shows how to read the value of a text field, set up the plugin to speak that text, and vibrate the phone for 2 seconds:

     function playVibrate() {
        var u = new SpeechSynthesisUtterance();
        var x = document.getElementById("frm1");
        var txt = "";
        txt = x.elements[0].value
        u.text = txt;
        u.lang = 'en-US';
        speechSynthesis.speak(u);      
        navigator.notification.vibrate(2000);
        document.getElementById("frm1").reset();
      }
