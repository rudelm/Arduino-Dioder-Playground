//
//  Dioder_Colour_WellsAppDelegate.m
//  Dioder Colour Wells
//
//  Created by Daniel Kennett on 08/09/2011.
//  Copyright 2011 Daniel Kennett. All rights reserved.
//

#import "Dioder_Colour_WellsAppDelegate.h"
#import "AMSerialPortAdditions.h"
#import "AMSerialPortList.h"

@implementation Dioder_Colour_WellsAppDelegate

@synthesize window;
@synthesize port;
@synthesize ports;
@synthesize channel1Color;
@synthesize channel2Color;
@synthesize channel3Color;
@synthesize channel4Color;

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{
    // Insert code here to initialize your application
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(portsChanged:)
                                                 name:AMSerialPortListDidAddPortsNotification
                                               object:[AMSerialPortList sharedPortList]];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(portsChanged:)
                                                 name:AMSerialPortListDidRemovePortsNotification
                                               object:[AMSerialPortList sharedPortList]];
    
    [self portsChanged:nil];
    
    self.channel1Color = [NSColor blackColor];
    self.channel2Color = [NSColor blackColor];
    self.channel3Color = [NSColor blackColor];
    self.channel4Color = [NSColor blackColor];
    
    [self addObserver:self forKeyPath:@"port" options:NSKeyValueObservingOptionOld context:nil];
    
    [self addObserver:self forKeyPath:@"channel1Color" options:0 context:nil];
    [self addObserver:self forKeyPath:@"channel2Color" options:0 context:nil];
    [self addObserver:self forKeyPath:@"channel3Color" options:0 context:nil];
    [self addObserver:self forKeyPath:@"channel4Color" options:0 context:nil];
    
}

-(void)portsChanged:(NSNotification *)aNotification {
    self.ports = [[[AMSerialPortList sharedPortList] serialPorts] sortedArrayUsingComparator:^(id a, id b) {
        return [[a name] caseInsensitiveCompare:[b name]];
    }];
}

-(void)applicationWillTerminate:(NSNotification *)notification {
    self.port = nil;
}

-(void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    if ([keyPath isEqualToString:@"port"]) {
        
        id oldPort = [change valueForKey:NSKeyValueChangeOldKey];
        if (oldPort != [NSNull null])
            [oldPort close];
        
        if (self.port.available) {
            [self.port open];
            [self.port setSpeed:B9600];
        } else if (self.port != nil) {
            self.port = nil;
        }
        
    } else if ([keyPath hasPrefix:@"channel"] && [self.port isOpen]) {
        
        [self updateColoursOnChannel1:self.channel1Color
                             channel2:self.channel2Color
                             channel3:self.channel3Color
                             channel4:self.channel4Color];
        
    } else {
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

-(void)updateColoursOnChannel1:(NSColor *)channel1 channel2:(NSColor *)channel2 channel3:(NSColor *)channel3 channel4:(NSColor *)channel4 {
    
    // If you choose a greyscale colour, it won't have red, green or blue components so we defensively convert.
    NSColor *rgbChannel1 = [channel1 colorUsingColorSpace:[NSColorSpace genericRGBColorSpace]];
    NSColor *rgbChannel2 = [channel2 colorUsingColorSpace:[NSColorSpace genericRGBColorSpace]];
    NSColor *rgbChannel3 = [channel3 colorUsingColorSpace:[NSColorSpace genericRGBColorSpace]];
    NSColor *rgbChannel4 = [channel4 colorUsingColorSpace:[NSColorSpace genericRGBColorSpace]];
    
    struct ArduinoDioderControlMessage message;
    
    message.header[0] = kHeaderByte1;
    message.header[1] = kHeaderByte2;
    
    message.colours[0] = (unsigned char)([rgbChannel1 redComponent] * 255);
    message.colours[1] = (unsigned char)([rgbChannel1 greenComponent] * 255);
    message.colours[2] = (unsigned char)([rgbChannel1 blueComponent] * 255);
    
    message.colours[3] = (unsigned char)([rgbChannel2 redComponent] * 255);
    message.colours[4] = (unsigned char)([rgbChannel2 greenComponent] * 255);
    message.colours[5] = (unsigned char)([rgbChannel2 blueComponent] * 255);
    
    message.colours[6] = (unsigned char)([rgbChannel3 redComponent] * 255);
    message.colours[7] = (unsigned char)([rgbChannel3 greenComponent] * 255);
    message.colours[8] = (unsigned char)([rgbChannel3 blueComponent] * 255);
    
    message.colours[9] = (unsigned char)([rgbChannel4 redComponent] * 255);
    message.colours[10] = (unsigned char)([rgbChannel4 greenComponent] * 255);
    message.colours[11] = (unsigned char)([rgbChannel4 blueComponent] * 255);
    
    unsigned char checksum = 0;
    for (int i = 0; i < sizeof(message.colours); i++)
        checksum ^= message.colours[i];
    
    message.checksum = checksum;
    
    NSData *data = [NSData dataWithBytes:&message length:sizeof(struct ArduinoDioderControlMessage)];
    
    NSError *error = nil;
    [self.port writeData:data error:&error];
    
    if (error)
        NSLog(@"[%@ %@]: %@", NSStringFromClass([self class]), NSStringFromSelector(_cmd), error);
}

@end
