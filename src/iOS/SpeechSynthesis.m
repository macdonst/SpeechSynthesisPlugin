//
//  SpeechSynthesis.m
//  TTSPlugin
//
//  Created by @jcesarmobile on 30/04/13.
//
//

#import "SpeechSynthesis.h"
#import "ISSpeechSynthesis.h"
#import "iSpeechSDK.h"


@implementation SpeechSynthesis


-(void)startup:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    iSpeechSDK *sdk = [iSpeechSDK sharedSDK];
    sdk.APIKey = @"yourApiKeyGoesHere";
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

-(void)speak:(CDVInvokedUrlCommand*)command {

    CDVPluginResult* pluginResult = nil;
    NSDictionary* utterance = [command.arguments objectAtIndex:0];
    
    if (![utterance isEqual:[NSNull null]]) {
        
        NSString *text = [utterance objectForKey:@"text"];
        if (text) {
            ISSpeechSynthesis *synthesis = [[ISSpeechSynthesis alloc] initWithText:text];

            /* Configuration changes here: */
            //[synthesis setVoice:ISVoiceEURSpanishMale];
            //[synthesis setBitrate:48];
            //[synthesis setSpeed:0];
            
            [synthesis setDelegate:self];
            
            
            NSError *error;
            
            if(![synthesis speak:&error]) {
                [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"error speak"] callbackId:command.callbackId];
            } else {
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
        } else {
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"no text"] callbackId:command.callbackId];
        }
        
    } else {
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Arg was null"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
    }
    
    
        
}




@end
