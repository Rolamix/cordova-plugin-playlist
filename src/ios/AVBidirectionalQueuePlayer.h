//
//  AVBidirectionalQueuePlayer.h
//  IntervalPlayer
//
//  Created by Daniel Giovannelli on 2/18/13.
//  This class subclasses AVQueuePlayer to create a class with the same functionality as AVQueuePlayer
//  but with the added ability to go backwards in the queue - a function that is impossible in a normal
//  AVQueuePlayer since items on the queue are destroyed when they are finished playing.
//
//  IMPORTANT NOTE: This version of AVQueuePlayer assumes that ARC IS ENABLED. If ARC is NOT enabled and you
//  use this library, you'll get memory leaks on the two fields that have been added to the class, int
//  nowPlayingIndex and NSArray itemsForPlayer.
//
//  Note also that this classrequires that the AVFoundation framework be included in your project.

#import <AVFoundation/AVFoundation.h>

#define AVBidirectionalQueueAddedItem @"AVBidirectionalQueuePlayer.AddedItem"
#define AVBidirectionalQueueAddedAllItems @"AVBidirectionalQueuePlayer.AddedAllItems"
#define AVBidirectionalQueueRemovedItem @"AVBidirectionalQueuePlayer.RemovedItem"
#define AVBidirectionalQueueCleared @"AVBidirectionalQueuePlayer.Cleared"

@class AVBidirectionalQueuePlayer;

@interface AVBidirectionalQueuePlayer : AVQueuePlayer

@property (nonatomic, strong) NSMutableArray *itemsForPlayer;
@property () NSUInteger currentIndex;

// Two methods need to be added to the AVQueuePlayer: one which will play the last song in the queue, and one which will return if the queue is at the beginning (in case the user wishes to implement special behavior when a queue is at its first item, such as restarting a song). A getIndex method to return the current index is also provided.
-(void)playPreviousItem;
-(BOOL)isAtBeginning;
-(BOOL)isAtEnd;
-(BOOL)isPlaying;

-(CMTime)currentTimeOffsetInQueue;
-(void)seekToTimeInQueue:(CMTime)time completionHandler:(void (^)(BOOL))completionHandler;
-(void)setCurrentIndex:(NSUInteger)currentIndex completionHandler:(void (^)(BOOL)) completionHandler;
-(void)setItemsForPlayer:(NSMutableArray *)itemsForPlayer;
-(void)insertAllItems:(NSMutableArray *)itemsForPlayer;

/* The following methods of AVQueuePlayer are overridden by AVBidirectionalQueuePlayer:
 – initWithItems: to keep track of the array used to create the player
 + queuePlayerWithItems: to keep track of the array used to create the player
 – advanceToNextItem to update the now playing index
 – insertItem:afterItem: to update the now playing index
 – removeAllItems to update the now playing index
 – removeItem:  to update the now playing index
 */


@end

