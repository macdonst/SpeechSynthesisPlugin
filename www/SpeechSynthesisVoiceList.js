var SpeechSynthesisVoiceList = function() {
  this._list = [];
  this.length;
};
    
SpeechSynthesisVoiceList.prototype.item = function(item) {
    return this._list[item];
};

module.exports = SpeechSynthesisVoiceList;
