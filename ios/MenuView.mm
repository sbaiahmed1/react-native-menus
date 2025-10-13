#import "MenuView.h"

#import <react/renderer/components/MenuViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/MenuViewSpec/EventEmitters.h>
#import <react/renderer/components/MenuViewSpec/Props.h>
#import <react/renderer/components/MenuViewSpec/RCTComponentViewHelpers.h>

#import <React/RCTConversions.h>

using namespace facebook::react;

@interface MenuView () <RCTMenuViewViewProtocol>
@end

@implementation MenuView {
    UIView *_childView;
    UIButton *_menuButton;
    NSArray<NSDictionary *> *_menuItems;
    UIColor *_textColor;
    UIColor *_checkedColor;
    UIColor *_uncheckedColor;
    NSString *_selectedIdentifier;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<MenuViewComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame
{
    if (self = [super initWithFrame:frame]) {
        static const auto defaultProps = std::make_shared<const MenuViewProps>();
        _props = defaultProps;
    }
    
    return self;
}

- (void)mountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
    [super mountChildComponentView:childComponentView index:index];
    
    // Only use the first child as the menu trigger
    if (index == 0) {
        // Remove old child view if exists
        if (_childView) {
            [_childView removeFromSuperview];
            _childView = nil;
        }
        
        _childView = (UIView *)childComponentView;
        [self setupChildViewAsMenuTrigger:_childView];
    }
}

- (void)unmountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
    [super unmountChildComponentView:childComponentView index:index];
    
    if (index == 0 && _childView == childComponentView) {
        _childView = nil;
        _menuButton = nil;
    }
}

- (void)setupChildViewAsMenuTrigger:(UIView *)childView
{
    // Add the child view to our view hierarchy
    [self addSubview:childView];
    
    // Setup constraints to fill the container
    childView.translatesAutoresizingMaskIntoConstraints = NO;
    [NSLayoutConstraint activateConstraints:@[
        [childView.topAnchor constraintEqualToAnchor:self.topAnchor],
        [childView.leadingAnchor constraintEqualToAnchor:self.leadingAnchor],
        [childView.trailingAnchor constraintEqualToAnchor:self.trailingAnchor],
        [childView.bottomAnchor constraintEqualToAnchor:self.bottomAnchor]
    ]];
    
    // If the child is a UIButton, attach the menu directly to it
    if ([childView isKindOfClass:[UIButton class]]) {
        _menuButton = (UIButton *)childView;
        _menuButton.showsMenuAsPrimaryAction = YES;
        [self updateMenuItems:_menuItems];
    } else {
        // For non-button children, create an invisible button overlay to show the menu
        [self disableUserInteractionRecursively:childView];
        
        // Create an invisible button that covers the entire view
        _menuButton = [UIButton buttonWithType:UIButtonTypeSystem];
        _menuButton.backgroundColor = [UIColor clearColor];
        _menuButton.showsMenuAsPrimaryAction = YES;
        _menuButton.translatesAutoresizingMaskIntoConstraints = NO;
        
        [self addSubview:_menuButton];
        
        // Position the button on top of everything
        [NSLayoutConstraint activateConstraints:@[
            [_menuButton.topAnchor constraintEqualToAnchor:self.topAnchor],
            [_menuButton.leadingAnchor constraintEqualToAnchor:self.leadingAnchor],
            [_menuButton.trailingAnchor constraintEqualToAnchor:self.trailingAnchor],
            [_menuButton.bottomAnchor constraintEqualToAnchor:self.bottomAnchor]
        ]];
        
        [self updateMenuItems:_menuItems];
    }
}

- (void)disableUserInteractionRecursively:(UIView *)view
{
    view.userInteractionEnabled = NO;
    for (UIView *subview in view.subviews) {
        [self disableUserInteractionRecursively:subview];
    }
}

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
    const auto &oldViewProps = *std::static_pointer_cast<MenuViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<MenuViewProps const>(props);
    
    // Update text color
    if (oldViewProps.color != newViewProps.color) {
        NSString *colorString = [[NSString alloc] initWithUTF8String:newViewProps.color.c_str()];
        _textColor = [self hexStringToColor:colorString];
        if (_textColor) {
            [_menuButton setTitleColor:_textColor forState:UIControlStateNormal];
        }
    }
    
    // Update checked color
    if (oldViewProps.checkedColor != newViewProps.checkedColor) {
        NSString *colorString = [[NSString alloc] initWithUTF8String:newViewProps.checkedColor.c_str()];
        _checkedColor = [self hexStringToColor:colorString];
    }
    
    // Update unchecked color
    if (oldViewProps.uncheckedColor != newViewProps.uncheckedColor) {
        NSString *colorString = [[NSString alloc] initWithUTF8String:newViewProps.uncheckedColor.c_str()];
        _uncheckedColor = [self hexStringToColor:colorString];
    }
    
    // Update menu items
    bool menuItemsChanged = (oldViewProps.menuItems.size() != newViewProps.menuItems.size()) || (_menuItems == nil);
    
    if (!menuItemsChanged && _menuItems != nil) {
        // Check if any menu item content changed
        for (size_t i = 0; i < newViewProps.menuItems.size(); i++) {
            const auto &newItem = newViewProps.menuItems[i];
            const auto &oldItem = oldViewProps.menuItems[i];
            if (newItem.identifier != oldItem.identifier || newItem.title != oldItem.title) {
                menuItemsChanged = true;
                break;
            }
        }
    }
    
    if (menuItemsChanged) {
        NSMutableArray *items = [[NSMutableArray alloc] init];
        for (const auto &item : newViewProps.menuItems) {
            NSString *identifier = [[NSString alloc] initWithUTF8String:item.identifier.c_str()];
            NSString *title = [[NSString alloc] initWithUTF8String:item.title.c_str()];
            [items addObject:@{@"identifier": identifier, @"title": title}];
        }
        _menuItems = [items copy];
        [self updateMenuItems:_menuItems];
    }
    
    [super updateProps:props oldProps:oldProps];
}

- (void)updateMenuItems:(NSArray<NSDictionary *> *)menuItems
{
    if (!_menuButton) {
        // Menu button not set yet, will be updated when child view is added
        return;
    }
    
    if (!menuItems || menuItems.count == 0) {
        _menuButton.menu = nil;
        return;
    }
    
    NSMutableArray<UIAction *> *actions = [[NSMutableArray alloc] init];
    
    for (NSDictionary *item in menuItems) {
        NSString *identifier = item[@"identifier"];
        NSString *title = item[@"title"];
        
        UIAction *action = [UIAction actionWithTitle:title
                                               image:nil
                                          identifier:identifier
                                             handler:^(__kindof UIAction * _Nonnull action) {
            [self selectMenuItem:identifier title:title];
        }];
        
        // Set state based on current selection
        if ([identifier isEqualToString:_selectedIdentifier]) {
            action.state = UIMenuElementStateOn;
        }
        
        [actions addObject:action];
    }
    
    UIMenu *menu = [UIMenu menuWithTitle:@"" children:actions];
    _menuButton.menu = menu;
}

- (void)selectMenuItem:(NSString *)identifier title:(NSString *)title
{
    _selectedIdentifier = identifier;
    if (_menuButton) {
        [self updateMenuItems:_menuItems]; // Refresh to update checkmarks
    }
    [self sendMenuSelection:identifier title:title];
}

- (void)sendMenuSelection:(NSString *)identifier title:(NSString *)title
{
    if (_eventEmitter != nullptr) {
        std::dynamic_pointer_cast<const facebook::react::MenuViewEventEmitter>(_eventEmitter)
            ->onMenuSelect(facebook::react::MenuViewEventEmitter::OnMenuSelect{
                .identifier = std::string([identifier UTF8String]),
                .title = std::string([title UTF8String])
            });
    }
}

- (UIColor *)hexStringToColor:(NSString *)hexString
{
    if (!hexString || hexString.length == 0) {
        return nil;
    }
    
    NSString *cleanString = [hexString stringByReplacingOccurrencesOfString:@"#" withString:@""];
    if (cleanString.length != 6) {
        return nil;
    }
    
    NSScanner *scanner = [NSScanner scannerWithString:cleanString];
    unsigned hexNumber;
    if (![scanner scanHexInt:&hexNumber]) {
        return nil;
    }
    
    return [UIColor colorWithRed:((float)((hexNumber & 0xFF0000) >> 16))/255.0
                           green:((float)((hexNumber & 0x00FF00) >> 8))/255.0
                            blue:((float)(hexNumber & 0x0000FF))/255.0
                           alpha:1.0];
}

@end

Class<RCTComponentViewProtocol> MenuViewCls(void)
{
    return MenuView.class;
}
