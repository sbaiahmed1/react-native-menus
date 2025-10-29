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
    BOOL _isChildViewButton;
    NSHashTable<UIView *> *_disabledViews;
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
        _disabledViews = [NSHashTable weakObjectsHashTable];
        _isChildViewButton = NO;
    }
    
    return self;
}

- (void)mountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
    // Only use the first child as the menu trigger
    if (index == 0) {
        // Clean up old child view if exists
        if (_childView && _childView != childComponentView) {
            [self cleanupMenuButton];
            [self restoreUserInteractionForDisabledViews];
        }
        
        _childView = (UIView *)childComponentView;
    }
    
    // Let React handle the mounting first
    [super mountChildComponentView:childComponentView index:index];
    
    // Setup menu trigger after React has properly mounted the view
    if (index == 0) {
        [self setupChildViewAsMenuTrigger:_childView];
    }
}

- (void)unmountChildComponentView:(UIView<RCTComponentViewProtocol> *)childComponentView index:(NSInteger)index
{
    // Clean up our references before React unmounts
    if (index == 0 && _childView == childComponentView) {
        [self cleanupMenuButton];
        [self restoreUserInteractionForDisabledViews];
        _childView = nil;
    }
    
    // Let React handle the unmounting
    [super unmountChildComponentView:childComponentView index:index];
}

- (void)setupChildViewAsMenuTrigger:(UIView *)childView
{    
    // If the child is a UIButton, attach the menu directly to it
    if ([childView isKindOfClass:[UIButton class]]) {
        _menuButton = (UIButton *)childView;
        _menuButton.showsMenuAsPrimaryAction = YES;
        _isChildViewButton = YES;
        [self updateMenuItems:_menuItems selectedIdentifier:nil];
    } else {
        // For non-button children, create an invisible button overlay to show the menu
        _isChildViewButton = NO;
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
        
        [self updateMenuItems:_menuItems selectedIdentifier:nil];
    }
}

- (void)disableUserInteractionRecursively:(UIView *)view
{
    if (view.userInteractionEnabled) {
        [_disabledViews addObject:view];
        view.userInteractionEnabled = NO;
    }
    for (UIView *subview in view.subviews) {
        [self disableUserInteractionRecursively:subview];
    }
}

- (void)restoreUserInteractionForDisabledViews
{
    // Create a copy of the objects to iterate over since NSHashTable with weak references
    // can have objects deallocated during iteration
    NSArray<UIView *> *viewsToRestore = [_disabledViews allObjects];
    
    for (UIView *view in viewsToRestore) {
        // The view might have been deallocated (weak reference), so check if it's still valid
        if (view && view.superview != nil) {
            view.userInteractionEnabled = YES;
        }
    }
    [_disabledViews removeAllObjects];
}

- (void)cleanupMenuButton
{
    if (_menuButton) {
        // If it's not a child view button (i.e., it's our overlay button), remove it
        if (!_isChildViewButton && _menuButton.superview == self) {
            [_menuButton removeFromSuperview];
        }
        // Clear the menu to prevent any lingering references
        _menuButton.menu = nil;
        _menuButton = nil;
    }
    _isChildViewButton = NO;
}

- (void)dealloc
{
    [self cleanupMenuButton];
    [self restoreUserInteractionForDisabledViews];
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
            bool contentChanged = (newItem.identifier != oldItem.identifier) || (newItem.title != oldItem.title);
            #ifdef __cplusplus
            // Also detect icon changes so menu can be rebuilt with updated images
            contentChanged = contentChanged || (newItem.iosSymbol != oldItem.iosSymbol);
            #endif
            if (contentChanged) {
                menuItemsChanged = true;
                break;
            }
        }
    }
    
    // Detect selectedIdentifier change
    bool selectedIdentifierChanged = (oldViewProps.selectedIdentifier != newViewProps.selectedIdentifier);
    NSString *currentSelectedIdentifier = nil;
    if (!newViewProps.selectedIdentifier.empty()) {
        currentSelectedIdentifier = [[NSString alloc] initWithUTF8String:newViewProps.selectedIdentifier.c_str()];
    }
    
    if (menuItemsChanged) {
        NSMutableArray *items = [[NSMutableArray alloc] init];
        for (const auto &item : newViewProps.menuItems) {
            NSString *identifier = [[NSString alloc] initWithUTF8String:item.identifier.c_str()];
            NSString *title = [[NSString alloc] initWithUTF8String:item.title.c_str()];
            NSString *symbol = nil;
            #ifdef __cplusplus
            if (!item.iosSymbol.empty()) {
                symbol = [[NSString alloc] initWithUTF8String:item.iosSymbol.c_str()];
            }
            #endif
            NSMutableDictionary *dict = [@{ @"identifier": identifier, @"title": title } mutableCopy];
            if (symbol) {
                dict[@"iosSymbol"] = symbol;
            }
            [items addObject:dict];
        }
        _menuItems = [items copy];
        [self updateMenuItems:_menuItems selectedIdentifier:currentSelectedIdentifier];
    } else if (selectedIdentifierChanged) {
        // Always update the menu when selectedIdentifier changes
        [self updateMenuItems:_menuItems selectedIdentifier:currentSelectedIdentifier];
    } else if (_menuButton && _menuButton.menu) {
        // Even if nothing changed, ensure the menu reflects current selectedIdentifier
        // This handles cases where the component was remounted after being unmounted
        [self updateMenuSelection:currentSelectedIdentifier];
    }
    
    [super updateProps:props oldProps:oldProps];
}

- (void)updateMenuItems:(NSArray<NSDictionary *> *)menuItems selectedIdentifier:(NSString *)selectedIdentifier
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
        NSString *symbol = item[@"iosSymbol"];
        UIImage *image = nil;
        if (symbol && symbol.length > 0) {
            image = [UIImage systemImageNamed:symbol];
        }
        
        UIAction *action = [UIAction actionWithTitle:title
                                               image:image
                                          identifier:identifier
                                             handler:^(__kindof UIAction * _Nonnull action) {
            [self selectMenuItem:identifier title:title];
        }];
        
        // Set state based on current selection (controlled via props)
        if (selectedIdentifier != nil && ![selectedIdentifier isEqualToString:@""] && [identifier isEqualToString:selectedIdentifier]) {
            action.state = UIMenuElementStateOn;
        }
        
        [actions addObject:action];
    }
    
    UIMenu *menu = [UIMenu menuWithTitle:@"" children:actions];
    _menuButton.menu = menu;
}

- (void)updateMenuSelection:(NSString *)selectedIdentifier
{
    if (!_menuButton || !_menuButton.menu) {
        return;
    }
    
    // Recreate the menu with updated selection states
    NSMutableArray<UIAction *> *actions = [[NSMutableArray alloc] init];
    
    for (UIMenuElement *element in _menuButton.menu.children) {
        if ([element isKindOfClass:[UIAction class]]) {
            UIAction *oldAction = (UIAction *)element;
            
            // Create new action with handler that captures the identifier and title
            UIAction *newAction = [UIAction actionWithTitle:oldAction.title
                                                      image:oldAction.image
                                                 identifier:oldAction.identifier
                                                    handler:^(__kindof UIAction * _Nonnull action) {
                [self selectMenuItem:action.identifier title:action.title];
            }];
            
            // Copy all relevant properties from oldAction to preserve behavior
            newAction.attributes = oldAction.attributes;
            
            // Update state based on current selection
            if (selectedIdentifier != nil && ![selectedIdentifier isEqualToString:@""] && [oldAction.identifier isEqualToString:selectedIdentifier]) {
                newAction.state = UIMenuElementStateOn;
            } else {
                newAction.state = UIMenuElementStateOff;
            }
            
            // Copy discoverability title if present
            if (oldAction.discoverabilityTitle) {
                newAction.discoverabilityTitle = oldAction.discoverabilityTitle;
            }
            
            [actions addObject:newAction];
        }
    }
    
    UIMenu *menu = [UIMenu menuWithTitle:@"" children:actions];
    _menuButton.menu = menu;
}

- (void)selectMenuItem:(NSString *)identifier title:(NSString *)title
{
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
