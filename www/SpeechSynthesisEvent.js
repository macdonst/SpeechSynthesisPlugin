cordova.define("org.apache.cordova.speech.speechsynthesisevent",
  function(require, exports, module) {

    var SpeechSynthesisEvent = function() {
        this.charIndex;
        this.elapsedTime;
        this.name;
    };
    
    module.exports = SpeechSynthesisEvent;
});
