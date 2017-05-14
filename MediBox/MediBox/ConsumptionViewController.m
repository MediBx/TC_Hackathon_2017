//
//  ConsumptionViewController.m
//  MediBox
//
//  Created by Android Dev on 5/13/17.
//  Copyright © 2017 commitex. All rights reserved.
//

#import "ConsumptionViewController.h"

@interface ConsumptionViewController ()

@property (strong, nonatomic) IBOutlet UITableView *tableView;


@end

@implementation ConsumptionViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

//====================== Delegate Methods ========================

//================================================================
- (UITableViewCell *)tableView:(UITableView *)aTableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [aTableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil)
    {
        // Create the cell.
        cell = [[UITableViewCell alloc]
                initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
    }
    
    //here goes any design that you wish to apply to the cell
    //for example:
    if((indexPath.row + 1) % 5 == 0 || (indexPath.row + 1) % 7 == 0)
    {
        cell.imageView.image = [UIImage imageNamed:@"red.png"];
    }
    else
    {
        cell.imageView.image = [UIImage imageNamed:@"green.png"];
    }
    
    cell.textLabel.text =
    [NSString stringWithFormat:@"%ld",
     (long)indexPath.row + 1];
    
    // Alternate the bgcolor of the rows, one gray one yellow
    cell.backgroundColor = (indexPath.row%2 == 0) ? [UIColor grayColor]: [UIColor colorWithRed:0.54 green:0.77 blue:0.96 alpha:1.0];
    
    return cell;
}
//================================================================

//================================================================
- (NSInteger)tableView:(UITableView *)tableView
 numberOfRowsInSection:(NSInteger)section
{
    return 30;
}
//================================================================

//================================================================
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 3;
}
//================================================================


- (NSString *)tableView:(UITableView *)tableView
titleForHeaderInSection:(NSInteger)section
{
    if(section==0)
    {
        return @"Medicine XYZ";
    }
    else if(section==1)
    {
        return @"Medicine ABC";
    }
    else
    {
        return @"Medicine DEF";
    }
}
//================================================================

//==================== end Delegate Methods ======================

@end
