//
//  ViewController.m
//  MediBox
//
//  Created by Android Dev on 5/13/17.
//  Copyright © 2017 commitex. All rights reserved.
//

#import "ViewController.h"
#import <Speech/Speech.h>
#import "UIImage+animatedGIF.h"
#import <AVFoundation/AVFoundation.h>
#import "MBProgressHUD.h"
#import <QuartzCore/QuartzCore.h>
#import <PubNub/PubNub.h>
#import "AppDelegate.h"



@interface ViewController () <PNObjectEventListener>// UI
{
    __weak IBOutlet UIButton *speakButton;
    __weak IBOutlet UIImageView *animationImageView;
    __weak IBOutlet UITextView *resultTextView;
    
    // Speech recognize
    SFSpeechRecognizer *speechRecognizer;
    SFSpeechAudioBufferRecognitionRequest *recognitionRequest;
    SFSpeechRecognitionTask *recognitionTask;
    SFSpeechURLRecognitionRequest *urlRequest;
    
    // Record speech using audio Engine
    AVAudioInputNode *inputNode;
    AVAudioEngine *audioEngine;
    
}

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.notificationView.alpha = 0.9;
    
    // Button used just for testing the notification view
    //////// ======= self.notifButton.hidden = true;
    
    // add border for text view
    resultTextView.layer.borderWidth = 1.0;
    resultTextView.layer.borderColor = [UIColor blueColor].CGColor;
    //resultTextView.layer.masksToBounds = YES;
    
    // round corners for text view
    resultTextView.layer.cornerRadius = 5;
    resultTextView.layer.masksToBounds = YES;
    
    // add border for notificationView
    self.notificationView.layer.borderWidth = 1.0;
    self.notificationView.layer.borderColor = [UIColor blueColor].CGColor;
    //resultTextView.layer.masksToBounds = YES;
    
    // round corners for notificationView
    self.notificationView.layer.cornerRadius = 5;
    self.notificationView.layer.masksToBounds = YES;
    self.notificationTextView.textColor = [UIColor colorWithRed:0.56 green:0.27 blue:0.68 alpha:1.0];


    // Do any additional setup after loading the view, typically from a nib.
    
    //[self myFunc2];
    
    [self myFunc3];
    
    AppDelegate *delegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
    PubNub *pubNub = delegate.pubNub;
    [pubNub addListener:self];
    [pubNub subscribeToChannels:@[@"notf:SKS7-a4c1"] withPresence:false];
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    audioEngine = [[AVAudioEngine alloc] init];
    
    NSLocale *local =[[NSLocale alloc] initWithLocaleIdentifier:@"en-US"];
    speechRecognizer = [[SFSpeechRecognizer alloc] initWithLocale:local];
    
    
    for (NSLocale *locate in [SFSpeechRecognizer supportedLocales]) {
        NSLog(@"%@", [locate localizedStringForCountryCode:locate.countryCode]);
    }
    // Check Authorization Status
    // Make sure you add "Privacy - Microphone Usage Description" key and reason in Info.plist to request micro permison
    // And "NSSpeechRecognitionUsageDescription" key for requesting Speech recognize permison
    [SFSpeechRecognizer requestAuthorization:^(SFSpeechRecognizerAuthorizationStatus status) {
        
        /*
         The callback may not be called on the main thread. Add an
         operation to the main queue to update the record button's state.
         */
        dispatch_async(dispatch_get_main_queue(), ^{
            switch (status) {
                case SFSpeechRecognizerAuthorizationStatusAuthorized: {
                    speakButton.enabled = YES;
                    break;
                }
                case SFSpeechRecognizerAuthorizationStatusDenied: {
                    speakButton.enabled = NO;
                    resultTextView.text = @"User denied access to speech recognition";
                }
                case SFSpeechRecognizerAuthorizationStatusRestricted: {
                    speakButton.enabled = NO;
                    resultTextView.text = @"User denied access to speech recognition";
                }
                case SFSpeechRecognizerAuthorizationStatusNotDetermined: {
                    speakButton.enabled = NO;
                    resultTextView.text = @"User denied access to speech recognition";
                }
            }
        });
        
    }];
}

// Transcript from a file
- (void)transcriptExampleFromAFile {
    NSURL *url = [[NSBundle mainBundle] URLForResource:@"checkFile" withExtension:@"m4a"];
    urlRequest = [[SFSpeechURLRecognitionRequest alloc] initWithURL:url];
    recognitionTask = [speechRecognizer recognitionTaskWithRequest:urlRequest resultHandler:^(SFSpeechRecognitionResult * _Nullable result, NSError * _Nullable error) {
        if (result != nil) {
            NSString *text = result.bestTranscription.formattedString;
            resultTextView.text = text;
        }
        else {
            NSLog(@"Error, %@", error.description);
        }
    }];
}

// recording
- (void)startRecording {
    
    NSURL *url = [[NSBundle mainBundle] URLForResource:@"recording_animate" withExtension:@"gif"];
    animationImageView.image = [UIImage animatedImageWithAnimatedGIFURL:url];
    animationImageView.hidden = NO;
    [speakButton setImage:[UIImage imageNamed:@"voice_contest_recording"] forState:UIControlStateNormal];
    
    if (recognitionTask) {
        [recognitionTask cancel];
        recognitionTask = nil;
    }
    
    AVAudioSession *session = [AVAudioSession sharedInstance];
    [session setCategory:AVAudioSessionCategoryRecord mode:AVAudioSessionModeMeasurement options:AVAudioSessionCategoryOptionDefaultToSpeaker error:nil];
    [session setActive:TRUE withOptions:AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation error:nil];
    
    inputNode = audioEngine.inputNode;
    
    recognitionRequest = [[SFSpeechAudioBufferRecognitionRequest alloc] init];
    recognitionRequest.shouldReportPartialResults = NO;
    //recognitionRequest.detectMultipleUtterances = YES;
    
    AVAudioFormat *format = [inputNode outputFormatForBus:0];
    
    [inputNode installTapOnBus:0 bufferSize:1024 format:format block:^(AVAudioPCMBuffer * _Nonnull buffer, AVAudioTime * _Nonnull when) {
        [recognitionRequest appendAudioPCMBuffer:buffer];
    }];
    [audioEngine prepare];
    NSError *error1;
    [audioEngine startAndReturnError:&error1];
    NSLog(@"%@", error1.description);
    
}


- (IBAction)speakTap:(id)sender {
    if (audioEngine.isRunning) {
        [MBProgressHUD showHUDAddedTo:self.view animated:YES];
        recognitionTask =[speechRecognizer recognitionTaskWithRequest:recognitionRequest resultHandler:^(SFSpeechRecognitionResult * _Nullable result, NSError * _Nullable error) {
            [MBProgressHUD hideHUDForView:self.view animated:YES];
            if (result != nil) {
                NSString *transcriptText = result.bestTranscription.formattedString;
                resultTextView.text = transcriptText;
            }
            else {
                [audioEngine stop];;
                recognitionTask = nil;
                recognitionRequest = nil;
            }
        }];
        // make sure you release tap on bus else your app will crash the second time you record.
        [inputNode removeTapOnBus:0];
        
        [audioEngine stop];
        [recognitionRequest endAudio];
        [speakButton setImage:[UIImage imageNamed:@"voice_contest"] forState:UIControlStateNormal];
        animationImageView.hidden = YES;
        
    }
    else {
        [self startRecording];
    }
}

- (IBAction)goToConsumptionScreen:(UIButton *)sender {
    
    [self performSegueWithIdentifier:@"ofrasegue" sender:self];

}

-(void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    
}



//=================== Methods to get data from backend =====================
     
-(void) myFunc3
{

}
     
//-----------------------------------------------------------------------
     
-(void) myFunc2
{
    NSError *error;
    
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration delegate:self delegateQueue:nil];
    
    NSURL *url = [NSURL URLWithString:@"http://ec2-34-210-191-33.us-west-2.compute.amazonaws.com:8080/GetBoxEvents/"];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url
                                                           cachePolicy:NSURLRequestUseProtocolCachePolicy
                                                       timeoutInterval:60.0];
    
    
    //[request setValue:@"application/form-data" forHTTPHeaderField:@"Content-Type"];
    
    //[request addValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    //[request addValue:@"application/json" forHTTPHeaderField:@"Accept"];
    
    [request setHTTPMethod:@"POST"];
    
    
    //req: {"boxId":"SKS7-a4c1", "slotId":"1", "startDate":{"year":2017,"month":4,"day":13}, "endDate":{"year":2017,"month":5,"day":13}}
    
    NSDictionary *startDateDict = [[NSDictionary alloc] initWithObjectsAndKeys:
                                   @"2017", @"year",
                                   @"4", @"month",
                                   @"13", @"day",
                                   nil];
    
    NSDictionary *endDateDict = [[NSDictionary alloc] initWithObjectsAndKeys: @"2017", @"year",
                                 @"5", @"month",
                                 @"13", @"day",
                                 nil];
    
    NSDictionary *theDataDict = [[NSDictionary alloc] initWithObjectsAndKeys:
                                 @"SKS7-a4c1", @"boxId",
                                 @"1", @"slotId",
                                 startDateDict, @"startDate",
                                 endDateDict, @"endDate",
                                 nil];
    
    NSDictionary *theDataDictWithKey = [[NSDictionary alloc] initWithObjectsAndKeys:
                                        theDataDict, @"req",
                                        nil];
    
    
    
    NSDictionary *stamData = [[NSDictionary alloc] initWithObjectsAndKeys: @"ofra", @"req",
                              nil];
    
    
    
    
    
    NSData *postData = [NSJSONSerialization dataWithJSONObject:stamData options:0 error:&error];
    
    [request setHTTPBody:postData];
    
    
    NSURLSessionDataTask *postDataTask = [session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        
        NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:nil];
        NSLog(@"%@", json);
        
    }];
    
    [postDataTask resume];
}


//-----------------------------------------------------------------------


-(void) myFunc
{
    //the session object uses the global NSURLCache, NSHTTPCookieStorage,
    // and NSURLCredentialStorage. This means that it works pretty similar
    //  to a default implementation of NSURLConnection.
    NSURLSession *session = [NSURLSession sharedSession];
    
    /*
     NSURLSessionDataTask *dataTask =
     [session dataTaskWithURL:[NSURL
     URLWithString:@"https://itunes.apple.com/search?term=apple&media=software"]
     completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
     NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:nil];
     NSLog(@"%@", json);
     }];
     */
    
    
    NSURLSessionDataTask *dataTask =
    [session dataTaskWithURL:[NSURL
                              URLWithString:@"http://ec2-34-210-191-33.us-west-2.compute.amazonaws.com:8080/GetBoxEvents"]
           completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
               NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:nil];
               NSLog(@"%@", json);
           }];
    
    [dataTask resume];
    
    //NSLog(@"%@", @"gg");
}
//=======================

//-----------------------------------------------------------------------
-(void) showNotificationAnimated
{
    self.notificationView.frame = CGRectMake(10,
                                       - 100,
                                       self.view.frame.size.width - 20,
                                       100);
    
    self.notificationView.hidden = false;
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:1];
    
    self.notificationView.frame = CGRectMake(10,
                                             100,
                                             self.view.frame.size.width - 20,
                                             100);
    [UIView commitAnimations];
    
}
//===================
-(void)hideNotificationAnimated
{
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:1];
    
    self.notificationView.frame = CGRectMake(40,
                                             - 100,
                                             self.view.frame.size.width - 80,
                                             100);
    
    self.notificationView.hidden = true;
    
    [UIView commitAnimations];
    
}

- (IBAction)hideNotification:(UIButton *)sender {
    [self hideNotificationAnimated];
}

- (IBAction)showNotif:(UIButton *)sender {
    [self showNotificationAnimated];
}


//-----------------------------------------------------------------------

#pragma mark - PNObjectEventListener

- (void)client:(PubNub *)client didReceiveMessage:(PNMessageResult *)message {
//    NSLog(@"message: %@", message.debugDescription);
    NSString *receivedString = (NSString *)message.data.message;
    NSLog(@"receivedString: %@", receivedString);
    
    self.notificationTextView.text = receivedString;
    
    [self showNotificationAnimated];

}

@end
