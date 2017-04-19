# SpeechSynthesisPlugin

W3C Web Speech API - Speech synthesis plugin for PhoneGap

# Installation

## Phone Gap

Using the command line tools run:

    phonegap plugin add https://github.com/macdonst/SpeechSynthesisPlugin
    cordova plugin add https://github.com/macdonst/SpeechSynthesisPlugin

## Cordova

This plugin also works with the Apache Cordova toolset. See this Github project for an example for Android:

    https://github.com/andysylvester/talk-to-me-cordova

More info on using this plugin with Cordova is available at this <a href="http://andysylvester.com/2014/02/08/first-steps-with-cordova-talk-to-me/">blog post</a>.


# Example Code


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
