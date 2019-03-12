#import <Cordova/CDV.h>
#import <Cordova/CDVAvailability.h>
#import "SpeechSynthesis.h"
#import "NSMutableArray+QueueAdditions.h"

#if 1
#define DBG(a)          NSLog(a)
#define DBG1(a, b)      NSLog(a, b)
#define DBG2(a, b, c)   NSLog(a, b, c)
#else
#define DBG(a)
#define DBG1(a, b)
#define DBG2(a, b, c)
#endif

@implementation SpeechSynthesis

- (void)pluginInitialize {
    DBG(@"[ss] pluginInitialize()");

    synthesizer = [AVSpeechSynthesizer new];
    synthesizer.delegate = self;
    
    audioSession = [AVAudioSession sharedInstance];
    commandQueue = [[NSMutableArray<CDVInvokedUrlCommand *> alloc] init];

    // Log changes to the audio route.
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(routeChanged:) name:AVAudioSessionRouteChangeNotification object:nil];
}

- (void)routeChanged:(NSNotification *)notification {
    AVAudioSession *audioSession = [AVAudioSession sharedInstance];
    NSNumber *reason = [notification.userInfo objectForKey:AVAudioSessionRouteChangeReasonKey];
    
    DBG(@"[ss] routeChanged()");
    
    AVAudioSessionRouteDescription *route;
    AVAudioSessionPortDescription *port;
    
    if ([reason unsignedIntegerValue] == AVAudioSessionRouteChangeReasonNewDeviceAvailable) {
        NSLog(@"[ss] AVAudioSessionRouteChangeReasonNewDeviceAvailable");
        
        route = audioSession.currentRoute;
        port = route.inputs[0];
        NSLog(@"[ss] New device is %@", port.portType);
    } else if ([reason unsignedIntegerValue] == AVAudioSessionRouteChangeReasonOldDeviceUnavailable) {
        NSLog(@"[ss] AVAudioSessionRouteChangeReasonOldDeviceUnavailable");
        
        route = [notification.userInfo objectForKey:AVAudioSessionRouteChangePreviousRouteKey];
        port = route.inputs[0];
        NSLog(@"[ss] Removed device %@", port.portType);
        
        route = audioSession.currentRoute;
        port = route.inputs[0];
        NSLog(@"[ss] Now using device %@", port.portType);
    }
}

- (void)speak:(CDVInvokedUrlCommand*)command {
    NSDictionary* options = [command.arguments objectAtIndex:0];
    
    NSString* text = [options objectForKey:@"text"];

    DBG1(@"[ss] speak(%@)", text);
    [commandQueue enqueue:command];

    NSString* lang = [options objectForKey:@"lang"];
    NSDictionary* voice = [options objectForKey:@"voice"];
    float volume = [[options objectForKey:@"volume"] doubleValue];
    double rate = [[options objectForKey:@"rate"] doubleValue];
    double pitch = [[options objectForKey:@"pitch"] doubleValue];
    
    AVAudioSessionCategory category = [audioSession category];
    if(![category isEqualToString:AVAudioSessionCategoryPlayback] &&
       !![category isEqualToString:AVAudioSessionCategoryPlayAndRecord]) {
        [audioSession setActive:NO withOptions:0 error:nil];
        
        NSUInteger options = [audioSession categoryOptions] | AVAudioSessionCategoryOptionDuckOthers;
        [audioSession setCategory:AVAudioSessionCategoryPlayback
                      withOptions:options error:nil];
    }

    if (!lang || (id)lang == [NSNull null]) {
        lang = @"en-US";
    }
    
    if (!volume) {
        volume = 1.0;
    }
    
    if (!rate) {
        rate = 1.0;
    }
    
    if (!pitch) {
        pitch = 1.2;
    }
    
    AVSpeechUtterance* utterance = [[AVSpeechUtterance new] initWithString:text];
    utterance.voice = [AVSpeechSynthesisVoice voiceWithLanguage:lang];
    // The rate appears to be exponential. Adjust our linear scale accordingly.
    // The equation was worked out by mapping iOS rate to actual rate by timing between start and end.
    utterance.rate = (log(rate) + 1) / 2.14;
    if(utterance.rate > (double) AVSpeechUtteranceMaximumSpeechRate) {
        utterance.rate = (double) AVSpeechUtteranceMaximumSpeechRate;
    }
    utterance.volume = volume;
    utterance.pitchMultiplier = pitch;

    if(voice) {
        NSString *identifier = [voice valueForKey:@"voiceURI"];
        
        utterance.voice = [AVSpeechSynthesisVoice voiceWithIdentifier:identifier];
    }

    [synthesizer speakUtterance:utterance];
}

- (void)cancel:(CDVInvokedUrlCommand*)command {
    DBG(@"[ss] cancel()");
    
    [synthesizer stopSpeakingAtBoundary:AVSpeechBoundaryImmediate];
    // TODO: Need to dequeue and send cancels to any queued commands
}

- (void)pause:(CDVInvokedUrlCommand*)command {
    DBG(@"[ss] pause()");
    
    [synthesizer pauseSpeakingAtBoundary:AVSpeechBoundaryImmediate];
    // TODO: Need to send pause event
}

- (void)resume:(CDVInvokedUrlCommand*)command {
    DBG(@"[ss] resume()");
    
    if(synthesizer.paused) {
        [synthesizer continueSpeaking];
        // TODO: Need to send resume event
    }
}

- (void)startup:(CDVInvokedUrlCommand *)command {
    DBG(@"[ss] startup()");
    
    NSMutableArray* list = [[NSMutableArray alloc] init];
    NSArray *voices = [AVSpeechSynthesisVoice speechVoices];

    for (id voiceName in voices) {
        NSMutableDictionary * voiceDict = [[NSMutableDictionary alloc] init];
        [voiceDict setValue:[voiceName valueForKey:@"identifier"] forKey:@"voiceURI"];
        [voiceDict setValue:[voiceName valueForKey:@"name"] forKey:@"name"];
        [voiceDict setValue:[voiceName valueForKey:@"language"] forKey:@"lang"];
        [voiceDict setValue:[NSNumber numberWithBool:true] forKey:@"localService"];
        [voiceDict setValue:[NSNumber numberWithBool:false] forKey:@"default"];
    }

    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:list];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

-(void) sendEvent:(NSString *) eventType
{
    NSMutableDictionary * event = [[NSMutableDictionary alloc]init];
    DBG1(@"[ss] sendEvent(%@)", eventType);
    [event setValue:eventType forKey:@"type"];
    [event setValue:@"" forKey:@"name"];
    [event setValue:0 forKey:@"charIndex"];
    [event setValue:0 forKey:@"elapsedTime"];

    CDVInvokedUrlCommand *command = [commandQueue peekHead];
    if(command) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:event];
        if(![eventType isEqualToString:@"end"]) {
            [pluginResult setKeepCallbackAsBool:YES];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    DBG1(@"[ss] sendEvent(%@) complete", eventType);
}

-(void) sendErrorEvent:(NSString *) errorCode
{
    NSMutableDictionary * event = [[NSMutableDictionary alloc]init];
    DBG1(@"[ss] sendErrorEvent(%@)", errorCode);
    [event setValue:@"error" forKey:@"type"];
    [event setValue:errorCode forKey:@"error"];
    
    CDVInvokedUrlCommand *command = [commandQueue peekHead];
    if(command) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:event];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    DBG1(@"[ss] sendErrorEvent(%@) complete", errorCode);
}

- (void)speechSynthesizer:(AVSpeechSynthesizer*)synthesizer didStartSpeechUtterance:(AVSpeechUtterance*)utterance {
    DBG1(@"[ss] didStartSpeechUtterance(%@)", utterance.speechString);
    
    [self sendEvent:@"start"];
}

- (void)speechSynthesizer:(AVSpeechSynthesizer*)synthesizer didFinishSpeechUtterance:(AVSpeechUtterance*)utterance {
    DBG1(@"[ss] didFinishSpeechUtterance(%@)", utterance.speechString);
    
    [self sendEvent:@"end"];
    [commandQueue dequeue];
}

- (void)speechSynthesizer:(AVSpeechSynthesizer*)synthesizer didPauseSpeechUtterance:(AVSpeechUtterance*)utterance {
    DBG1(@"[ss] didPauseSpeechUtterance(%@)", utterance.speechString);
    
    [self sendEvent:@"pause"];
}

- (void)speechSynthesizer:(AVSpeechSynthesizer*)synthesizer didContinueSpeechUtterance:(AVSpeechUtterance*)utterance {
    DBG1(@"[ss] didContinueSpeechUtterance(%@)", utterance.speechString);
    
    [self sendEvent:@"resume"];
}

- (void)speechSynthesizer:(AVSpeechSynthesizer*)synthesizer didCancelSpeechUtterance:(AVSpeechUtterance*)utterance {
    DBG1(@"[ss] didFinishSpeechUtterance(%@)", utterance.speechString);
    
    [self sendEvent:@"end"];
    [commandQueue dequeue];
}

@end
