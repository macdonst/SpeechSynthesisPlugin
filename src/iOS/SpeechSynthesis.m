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
    self.pluginResult = nil;
    iSpeechSDK *sdk = [iSpeechSDK sharedSDK];
    sdk.APIKey = @"putYourAPIKeyHere";
    self.pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:self.pluginResult callbackId:command.callbackId];
}

-(void)speak:(CDVInvokedUrlCommand*)command {

    self.command = command;
    self.pluginResult = nil;
    NSDictionary* utterance = [command.arguments objectAtIndex:0];
    
    if (![utterance isEqual:[NSNull null]]) {
        
        NSString *text = [utterance objectForKey:@"text"];
        if (text) {
            
            ISSpeechSynthesis *synthesis = [[ISSpeechSynthesis alloc] initWithText:text];

            /* Configuration changes here: */
            //[synthesis setVoice:ISVoiceEURSpanishMale];
            //[synthesis setBitrate:48];
            //[synthesis setSpeed:0];
            
            NSMutableDictionary * event = [[NSMutableDictionary alloc]init];
            [event setValue:@"start" forKey:@"type"];
            [event setValue:[NSNumber numberWithInt:0] forKey:@"charIndex"];
            [event setValue:[NSNumber numberWithInt:0] forKey:@"elapsedTime"];
            [event setValue:@"" forKey:@"name"];
            
            [synthesis setDelegate:self];
            
            self.pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:event];
            [self.pluginResult setKeepCallbackAsBool:YES];
            [self.commandDelegate sendPluginResult:self.pluginResult callbackId:self.command.callbackId];
            
            
            [synthesis speakWithHandler:^(NSError *error, BOOL userCancelled) {
                
                if (userCancelled) {
                    
                    NSMutableDictionary * event = [[NSMutableDictionary alloc]init];
                    [event setValue:@"end" forKey:@"type"];
                    [self.pluginResult setKeepCallbackAsBool:NO];
                    
                    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:event] callbackId:self.command.callbackId];
                    
                } else {
                    
                    
                    if(!error){
                        
                        
                        NSMutableDictionary * event = [[NSMutableDictionary alloc]init];
                        [event setValue:@"end" forKey:@"type"];

                        
                        [self.pluginResult setKeepCallbackAsBool:NO];
                        
                        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:event] callbackId:self.command.callbackId];
                        
                        
                    } else {
                        
                        [self sendErrorCallback];
                        
                    }
                }
            }];
            
        } else {
            
            [self sendErrorCallback];
            
        }
        
    } else {
        
        [self sendErrorCallback];
        
    }
    
    
        
}

-(void)sendErrorCallback {
    
    NSMutableDictionary * error = [[NSMutableDictionary alloc]init];
    [error setValue:@"error" forKey:@"type"];
    [error setValue:[NSNumber numberWithInt:0] forKey:@"charIndex"];
    [error setValue:[NSNumber numberWithInt:0] forKey:@"elapsedTime"];
    [error setValue:@"" forKey:@"name"];
    
    self.pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:error];
    [self.commandDelegate sendPluginResult:self.pluginResult callbackId:self.command.callbackId];
}




@end
