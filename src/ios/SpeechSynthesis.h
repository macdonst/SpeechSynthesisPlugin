#import <Cordova/CDV.h>
#import <AVFoundation/AVFoundation.h>

@interface SpeechSynthesis : CDVPlugin <AVSpeechSynthesizerDelegate> {
    AVSpeechSynthesizer* synthesizer;
    AVAudioSession *audioSession;
    CDVPluginResult* pluginResult;
    NSMutableArray<CDVInvokedUrlCommand *>* commandQueue;
}

- (void)speak:(CDVInvokedUrlCommand*)command;
- (void)cancel:(CDVInvokedUrlCommand*)command;
- (void)pause:(CDVInvokedUrlCommand*)command;
- (void)resume:(CDVInvokedUrlCommand*)command;
- (void)startup:(CDVInvokedUrlCommand*)command;
@end
