//
//  AppDelegate.h
//  MediBox
//
//  Created by Android Dev on 5/13/17.
//  Copyright © 2017 commitex. All rights reserved.
//

#import <UIKit/UIKit.h>

@class PubNub;

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (nonatomic, strong) PubNub *pubNub;


@end

