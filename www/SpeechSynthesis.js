var exec = require("cordova/exec");

var SpeechSynthesis = function() {
    this.pending = false;
    this.speaking = false;
    this.paused = false;
    exec(null, null, "SpeechSynthesis", "startup", []);
};

SpeechSynthesis.prototype.speak = function(utterance) {
	var successCallback = function(event) {
		if (event.type === "start" && typeof utterance.onStart === "function") {
			utterance.onStart(event);
		} else if (event.type === "end" && typeof utterance.onEnd === "function") {
			utterance.onEnd(event);
		} else if (event.type === "pause" && typeof utterance.onPause === "function") {
			utterance.onPause(event);
		} else if (event.type === "resume" && typeof utterance.onResume === "function") {
			utterance.onResume(event);
		} else if (event.type === "mark" && typeof utterance.onMark === "function") {
			utterance.onMark(event);
		} else if (event.type === "boundry" && typeof utterance.onBoundry === "function") {
			utterance.onBoundry(event);
		}
	};
	var errorCallback = function() {
		if (typeof utterance.onError === "function") {
			utterance.onError();
		}
	};


    exec(successCallback, errorCallback, "SpeechSynthesis", "speak", [utterance]);
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
