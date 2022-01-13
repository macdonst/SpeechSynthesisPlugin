/*
    Modified for use in the Speech Synthesis plugin by Wayne Fisher.
    Copyright (c) 2019 Fisherlea Systems.

    Original code from:
    Cordova Text-to-Speech Plugin
    https://github.com/vilic/cordova-plugin-tts
 
    by VILIC VANE
    https://github.com/vilic
 
    MIT License
*/

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
