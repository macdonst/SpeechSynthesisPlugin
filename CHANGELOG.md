# Change Log

2018/05/30
==========

Improved compatibility with the W3C specification (https://w3c.github.io/speech-api/webspeechapi.html) and other improvements.
* Added `voicechanged` event.
* Error events from Android now include the `error` value and message.
* Removed SpeechSynthesisVoiceList.

2019/03/13
==========

Added iOS implementation. Apple's implementation of speechSynthesis
crashes in heavy use. In particular, crashes were seen when used with
wifisher's phonegap-plugin-speechsynthesis plugin.
