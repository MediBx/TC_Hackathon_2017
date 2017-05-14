//
//  ViewController.h
//  MediBox
//
//  Created by Android Dev on 5/13/17.
//  Copyright © 2017 commitex. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController <NSURLSessionDelegate>

// Vars for JSON ----------------------------------------------------------------------------------------------
@property (nonatomic, strong) NSMutableArray* infoArray;
@property (nonatomic, strong) NSString* keyFromJson1;
@property (nonatomic, strong) NSString* keyFromJson2;
@property (nonatomic, strong) NSString* keyFromJson3;
@property (nonatomic, strong) NSString* keyFromJson4;
@property (nonatomic, strong) NSMutableArray *arrayOfEvents;
@property (nonatomic, strong) NSMutableArray *initializedEventsArr;
@property (nonatomic, strong) NSMutableData *responseData;

@property (nonatomic, strong) IBOutlet UIView* notificationView;
@property (strong, nonatomic) IBOutlet UITextView *notificationTextView;
@property (strong, nonatomic) IBOutlet UIButton *notifButton;



@end

