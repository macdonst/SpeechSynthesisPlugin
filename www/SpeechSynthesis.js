var exec = require("cordova/exec");

var SpeechSynthesis = function() {
    this.pending = false;
    this.speaking = false;
    this.paused = false;
    exec(null, null, "SpeechSynthesis", "startup", []);
};

SpeechSynthesis.prototype.speak = function(utterance) {
    exec(null, null, "SpeechSynthesis", "speak", [utterance]);
};

SpeechSynthesis.prototype.cancel = function() {
    exec(null, null, "SpeechSynthesis", "cancel", []);
};

SpeechSynthesis.prototype.pause = function() {
    exec(null, null, "SpeechSynthesis", "pause", []);
};

SpeechSynthesis.prototype.resume = function() {
    exec(null, null, "SpeechSynthesis", "resume", []);
};

SpeechSynthesis.prototype.getVoices = function(win, fail) {
    exec(win, fail, "SpeechSynthesis", "getVoices", []);
};

module.exports = new SpeechSynthesis();
