var SpeechSynthesisErrorEvent = function () {
    SpeechSynthesisEvent.call(this);

    this.type = "error";
    this.error = null;
    this.message = null;
};

SpeechSynthesisErrorEvent.prototype = new SpeechSynthesisEvent;
SpeechSynthesisErrorEvent.prototype.constructor = SpeechSynthesisErrorEvent;

SpeechSynthesisErrorEvent['canceled'] = 0;
SpeechSynthesisErrorEvent['interrupted'] = 1;
SpeechSynthesisErrorEvent['audio-busy'] = 2;
SpeechSynthesisErrorEvent['audio-hardware'] = 3;
SpeechSynthesisErrorEvent['network'] = 4;
SpeechSynthesisErrorEvent['synthesis-unavailable'] = 5;
SpeechSynthesisErrorEvent['synthesis-failed'] = 6;
SpeechSynthesisErrorEvent['language-unavailable'] = 7;
SpeechSynthesisErrorEvent['voice-unavailable'] = 8;
SpeechSynthesisErrorEvent['text-too-long'] = 9;
SpeechSynthesisErrorEvent['invalid-argument'] = 10;

SpeechSynthesisErrorEvent._errorCodes = [
    'canceled',
    'interrupted',
    'audio-busy',
    'audio-hardware',
    'network',
    'synthesis-unavailable',
    'synthesis-failed',
    'language-unavailable',
    'voice-unavailable',
    'text-too-long',
    'invalid-argument'
];

module.exports = SpeechSynthesisErrorEvent;
