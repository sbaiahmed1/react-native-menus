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
    BOOL _disabled;
    NSString *_accessibilityLabelProp;
    NSString *_menuAccessibilityHint;
    NSString *_menuTitle;
    NSString *_selectedIdentifier;
    BOOL _enforceMinimumTouchTarget;
}

// WCAG 2.2 target size (minimum). Applied to hit-testing only, never to layout.
static const CGFloat kMinimumTouchTargetSize = 44.0;

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
        _enforceMinimumTouchTarget = YES;
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
        [self updateMenuItems:_menuItems selectedIdentifier:_selectedIdentifier];
        [self updateAccessibility];
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

        [self updateMenuItems:_menuItems selectedIdentifier:_selectedIdentifier];
        [self updateAccessibility];
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
        // Dismiss any menu still on screen so it doesn't outlive the view that owns it
        [self close];
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

    // Update disabled state
    if (oldViewProps.disabled != newViewProps.disabled) {
        _disabled = newViewProps.disabled;
        [self updateDisabledState];
    }

    // Accessibility inputs. `accessibilityLabel` comes from the base ViewProps, so an app
    // sets it exactly as it would on any other View.
    _accessibilityLabelProp = newViewProps.accessibilityLabel.empty()
        ? nil
        : [[NSString alloc] initWithUTF8String:newViewProps.accessibilityLabel.c_str()];
    _menuAccessibilityHint = newViewProps.menuAccessibilityHint.empty()
        ? nil
        : [[NSString alloc] initWithUTF8String:newViewProps.menuAccessibilityHint.c_str()];
    _menuTitle = newViewProps.title.empty()
        ? nil
        : [[NSString alloc] initWithUTF8String:newViewProps.title.c_str()];
    _enforceMinimumTouchTarget = newViewProps.enforceMinimumTouchTarget;

    // Update themeVariant
    if (oldViewProps.themeVariant != newViewProps.themeVariant) {
        switch (newViewProps.themeVariant) {
            case MenuViewThemeVariant::Dark:
                self.overrideUserInterfaceStyle = UIUserInterfaceStyleDark;
                break;
            case MenuViewThemeVariant::Light:
                self.overrideUserInterfaceStyle = UIUserInterfaceStyleLight;
                break;
            case MenuViewThemeVariant::System:
            default:
                self.overrideUserInterfaceStyle = UIUserInterfaceStyleUnspecified;
                break;
        }
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
            // Detect new properties
            contentChanged = contentChanged || (newItem.subtitle != oldItem.subtitle);
            contentChanged = contentChanged || (newItem.destructive != oldItem.destructive);
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
    _selectedIdentifier = currentSelectedIdentifier;

    if (menuItemsChanged) {
        NSMutableArray *items = [[NSMutableArray alloc] init];
        for (const auto &item : newViewProps.menuItems) {
            NSString *identifier = [[NSString alloc] initWithUTF8String:item.identifier.c_str()];
            NSString *title = [[NSString alloc] initWithUTF8String:item.title.c_str()];
            NSString *symbol = nil;
            NSString *subtitle = nil;
            BOOL destructive = NO;
            
            #ifdef __cplusplus
            if (!item.iosSymbol.empty()) {
                symbol = [[NSString alloc] initWithUTF8String:item.iosSymbol.c_str()];
            }
            if (!item.subtitle.empty()) {
                subtitle = [[NSString alloc] initWithUTF8String:item.subtitle.c_str()];
            }
            destructive = item.destructive;
            #endif
            
            NSMutableDictionary *dict = [@{ @"identifier": identifier, @"title": title } mutableCopy];
            if (symbol) {
                dict[@"iosSymbol"] = symbol;
            }
            if (subtitle) {
                dict[@"subtitle"] = subtitle;
            }
            if (destructive) {
                dict[@"destructive"] = @(YES);
            }
            if (!item.accessibilityLabel.empty()) {
                dict[@"accessibilityLabel"] = [[NSString alloc] initWithUTF8String:item.accessibilityLabel.c_str()];
            }
            if (!item.accessibilityHint.empty()) {
                dict[@"accessibilityHint"] = [[NSString alloc] initWithUTF8String:item.accessibilityHint.c_str()];
            }
            if (!item.testID.empty()) {
                dict[@"testID"] = [[NSString alloc] initWithUTF8String:item.testID.c_str()];
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

    [self updateAccessibility];

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
        NSString *subtitle = item[@"subtitle"];
        BOOL destructive = [item[@"destructive"] boolValue];
        
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

        // Set attributes
        if (destructive) {
            action.attributes = UIMenuElementAttributesDestructive;
        }
        
        // Set subtitle if available (iOS 15+)
        if (subtitle && subtitle.length > 0) {
            if (@available(iOS 15.0, *)) {
                action.subtitle = subtitle;
            } else {
                // Fallback for older iOS versions
                action.discoverabilityTitle = subtitle;
            }
        }

        // Set state based on current selection (controlled via props)
        if (selectedIdentifier != nil && ![selectedIdentifier isEqualToString:@""] && [identifier isEqualToString:selectedIdentifier]) {
            action.state = UIMenuElementStateOn;
        }

        // Test identifier, defaulting to the item's own identifier so every item is
        // addressable without extra props. UIAction does not declare
        // UIAccessibilityIdentification, but it does implement the setter, and the value
        // reaches the accessibility tree — verified against the live AX tree on iOS 26.
        NSString *itemTestID = item[@"testID"];
        NSString *resolvedTestID = itemTestID.length > 0 ? itemTestID : identifier;
        if (resolvedTestID.length > 0 &&
            [action respondsToSelector:@selector(setAccessibilityIdentifier:)]) {
            [(id<UIAccessibilityIdentification>)action setAccessibilityIdentifier:resolvedTestID];
        }

        // Best-effort per-item accessibility. UIKit builds the menu's own UI and does not
        // document honouring these on a UIMenuElement, so `title`/`subtitle` remain the
        // reliable announcement on iOS; setting them is harmless where ignored.
        NSString *itemLabel = item[@"accessibilityLabel"];
        NSString *itemHint = item[@"accessibilityHint"];
        if (itemLabel.length > 0) {
            action.accessibilityLabel = itemLabel;
        }
        if (itemHint.length > 0) {
            action.accessibilityHint = itemHint;
        }

        [actions addObject:action];
    }

    // Create menu
    UIMenu *menu;
    
    // If title is provided in props, use it (need to store it first or pass it down)
    // For now, since title was removed from props based on user request, we use empty string
    // But if we wanted to support title later:
    NSString *menuTitle = @"";
    if (_props) {
        const auto &viewProps = *std::static_pointer_cast<MenuViewProps const>(_props);
        if (!viewProps.title.empty()) {
             menuTitle = [[NSString alloc] initWithUTF8String:viewProps.title.c_str()];
        }
    }
    
    menu = [UIMenu menuWithTitle:menuTitle children:actions];
    _menuButton.menu = menu;
}

- (void)updateMenuSelection:(NSString *)selectedIdentifier
{
    if (!_menuButton || !_menuButton.menu) {
        return;
    }

    // Rebuild from `_menuItems` rather than from the live menu's children. Reconstructing
    // UIActions from the existing menu only carried across title/image/attributes, so a
    // selection-only update silently dropped each item's subtitle and accessibility label.
    [self updateMenuItems:_menuItems selectedIdentifier:selectedIdentifier];
}

#pragma mark - Accessibility

/**
 * The trigger is exposed to VoiceOver as a SINGLE button element. Without this the menu
 * button carries no label at all, and the child content surfaces as plain static text —
 * so VoiceOver never announces that anything here is actionable.
 */
- (void)updateAccessibility
{
    // The HOST VIEW is the accessibility element, not the overlay button. React Native
    // already applies accessibilityLabel/hint to this view with the correct frame, so
    // annotating the button instead produced a duplicate element at a stale position.
    self.isAccessibilityElement = YES;

    UIAccessibilityTraits traits = UIAccessibilityTraitButton;
    if (_disabled) {
        traits |= UIAccessibilityTraitNotEnabled;
    }
    self.accessibilityTraits = traits;

    // One element only: the overlay button must not surface separately.
    _menuButton.isAccessibilityElement = NO;

    // Child content is decorative — this view announces the same text. Hiding it from the
    // tree does not stop the label fallback reading it: RCTRecursiveAccessibilityLabel
    // walks the view hierarchy directly, not the accessibility tree.
    if (_childView && !_isChildViewButton) {
        _childView.accessibilityElementsHidden = YES;
    }
}

/**
 * Resolved on demand rather than assigned during updateProps. React Native lays text out
 * asynchronously, so at prop time the child-content fallback is usually still empty — a
 * one-shot assignment left the trigger permanently nameless whenever the app supplied
 * neither an explicit label, a `selectedIdentifier`, nor a `title`.
 */
- (NSString *)accessibilityLabel
{
    if (_accessibilityLabelProp.length > 0) {
        return _accessibilityLabelProp;
    }

    NSString *fromSelection = [self labelFromSelectedItem];
    if (fromSelection.length > 0) {
        return fromSelection;
    }

    if (_menuTitle.length > 0) {
        return _menuTitle;
    }

    // Last resort: RCTViewComponentView already flattens child text for an accessibility
    // element (via RCTRecursiveAccessibilityLabel), so there is nothing to hand-roll here.
    // It must stay LAST — calling super first would let child text beat the selected item.
    return [super accessibilityLabel];
}

/**
 * Resolved on demand for the same reason as the label. Assigning it once left the hint
 * permanently stale, since the guard that stopped it overwriting itself also stopped any
 * later `menuAccessibilityHint` change from ever reaching VoiceOver.
 */
- (NSString *)accessibilityHint
{
    NSString *explicitHint = [super accessibilityHint];
    if (explicitHint.length > 0) {
        return explicitHint;
    }

    if (_menuAccessibilityHint.length > 0) {
        return _menuAccessibilityHint;
    }

    return NSLocalizedString(@"Opens a menu", @"Accessibility hint for a menu trigger");
}

/** The selected item's own accessibilityLabel, falling back to its visible title. */
- (NSString *)labelFromSelectedItem
{
    if (_selectedIdentifier.length == 0) {
        return nil;
    }

    for (NSDictionary *item in _menuItems) {
        if ([item[@"identifier"] isEqualToString:_selectedIdentifier]) {
            NSString *itemLabel = item[@"accessibilityLabel"];
            return itemLabel.length > 0 ? itemLabel : item[@"title"];
        }
    }
    return nil;
}

#pragma mark - Minimum touch target

/** Bounds grown to at least 44pt on each axis, centred. Never affects layout. */
- (CGRect)minimumTouchTargetRect
{
    CGRect bounds = self.bounds;
    CGFloat widthDeficit = MAX(0.0, kMinimumTouchTargetSize - CGRectGetWidth(bounds));
    CGFloat heightDeficit = MAX(0.0, kMinimumTouchTargetSize - CGRectGetHeight(bounds));
    if (widthDeficit == 0.0 && heightDeficit == 0.0) {
        return bounds;
    }
    return CGRectInset(bounds, -widthDeficit / 2.0, -heightDeficit / 2.0);
}

- (BOOL)shouldExpandTouchTarget
{
    return _enforceMinimumTouchTarget && !_disabled && _menuButton != nil;
}

/**
 * Computed on demand rather than assigned, so it can never go stale against a frame that
 * Fabric applies after our prop/layout passes.
 */
- (CGRect)accessibilityFrame
{
    if (![self shouldExpandTouchTarget]) {
        return [super accessibilityFrame];
    }
    return UIAccessibilityConvertFrameToScreenCoordinates([self minimumTouchTargetRect], self);
}

- (void)layoutSubviews
{
    [super layoutSubviews];
    // Re-resolve rather than only re-framing: the child-content fallback depends on text
    // that React Native lays out asynchronously, so at updateProps time it is often still
    // empty and the trigger would keep a nil label.
    [self updateAccessibility];
}

- (BOOL)pointInside:(CGPoint)point withEvent:(UIEvent *)event
{
    if ([super pointInside:point withEvent:event]) {
        return YES;
    }
    if (![self shouldExpandTouchTarget]) {
        return NO;
    }
    return CGRectContainsPoint([self minimumTouchTargetRect], point);
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event
{
    UIView *hit = [super hitTest:point withEvent:event];
    if (hit != nil && hit != self) {
        return hit;
    }
    // Inside the expanded margin: route to the button so the menu still opens.
    if ([self shouldExpandTouchTarget] && CGRectContainsPoint([self minimumTouchTargetRect], point)) {
        return _menuButton;
    }
    return hit;
}

#pragma mark -

- (void)updateDisabledState
{
    if (_menuButton) {
        _menuButton.enabled = !_disabled;
        _menuButton.userInteractionEnabled = !_disabled;

        // If disabled, remove the menu
        if (_disabled) {
            _menuButton.menu = nil;
        } else {
            // Re-enable menu if not disabled
            [self updateMenuItems:_menuItems selectedIdentifier:_selectedIdentifier];
        }
        [self updateAccessibility];
    }
}

- (void)selectMenuItem:(NSString *)identifier title:(NSString *)title
{
    if (!_disabled) {
        [self sendMenuSelection:identifier title:title];
    }
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

#pragma mark - Commands

- (void)handleCommand:(const NSString *)commandName args:(const NSArray *)args
{
    RCTMenuViewHandleCommand(self, commandName, args);
}

// The button's menu is presented by UIKit's own context menu interaction (via
// `showsMenuAsPrimaryAction`), not by a target/action we control. Sending control
// events therefore does nothing — `performPrimaryAction` is the only public API
// that triggers the presentation, and it is iOS 17.4+.
- (void)open
{
    if (_disabled || !_menuButton || !_menuButton.menu) {
        return;
    }

    if (@available(iOS 17.4, *)) {
        [_menuButton performPrimaryAction];
    } else {
        RCTLogWarn(@"[MenuView] open() requires iOS 17.4 or newer. On older versions the "
                   @"menu can only be opened by tapping it.");
    }
}

- (void)close
{
    [[self activeMenuInteraction] dismissMenu];
}

// UIButton installs a UIContextMenuInteraction of its own once a non-nil `menu`
// is set; that interaction is what owns the presented menu.
- (nullable UIContextMenuInteraction *)activeMenuInteraction
{
    for (id<UIInteraction> interaction in _menuButton.interactions) {
        if ([interaction isKindOfClass:[UIContextMenuInteraction class]]) {
            return (UIContextMenuInteraction *)interaction;
        }
    }
    return nil;
}

@end

Class<RCTComponentViewProtocol> MenuViewCls(void)
{
    return MenuView.class;
}
