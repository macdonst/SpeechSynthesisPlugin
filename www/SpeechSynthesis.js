
var exec = require("cordova/exec");
var SpeechSynthesisVoiceList = require("org.apache.cordova.speech.speechsynthesis.SpeechSynthesisVoiceList");

var SpeechSynthesis = function() {
    this.pending = false;
    this.speaking = false;
    this.paused = false;
    this._voices = null;
    var that = this;
    var successCallback = function(data) {
    	that._voices = new SpeechSynthesisVoiceList(data);
    };
    exec(successCallback, null, "SpeechSynthesis", "startup", []);
};

SpeechSynthesis.prototype.speak = function(utterance) {
	var successCallback = function(event) {
		if (event.type === "start" && typeof utterance.onstart === "function") {
			utterance.onstart(event);
		} else if (event.type === "end" && typeof utterance.onend === "function") {
			utterance.onend(event);
		} else if (event.type === "pause" && typeof utterance.onpause === "function") {
			utterance.onpause(event);
		} else if (event.type === "resume" && typeof utterance.onresume === "function") {
			utterance.onresume(event);
		} else if (event.type === "mark" && typeof utterance.onmark === "function") {
			utterance.onmark(event);
		} else if (event.type === "boundry" && typeof utterance.onboundry === "function") {
			utterance.onboundry(event);
		}
	};
	var errorCallback = function() {
		if (typeof utterance.onerror === "function") {
			utterance.onerror();
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

SpeechSynthesis.prototype.getVoices = function() {
	return this._voices;
};

module.exports = new SpeechSynthesis();