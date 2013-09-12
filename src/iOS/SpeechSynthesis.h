//
//  SpeechSynthesis.h
//  TTSPlugin
//
//  Created by @jcesarmobile on 30/04/13.
//
//

#import <Cordova/CDV.h>
#import "iSpeechSDK.h"

@interface SpeechSynthesis : CDVPlugin <ISSpeechSynthesisDelegate>

@property (nonatomic,strong) CDVInvokedUrlCommand * command;
@property (nonatomic,strong) CDVPluginResult* pluginResult;

-(void)startup:(CDVInvokedUrlCommand*)command;

-(void)speak:(CDVInvokedUrlCommand*)command;


@end
