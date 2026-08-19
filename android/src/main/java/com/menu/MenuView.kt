package com.menu

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.RCTEventEmitter

class MenuView(context: Context) : FrameLayout(context) {
    private var themeVariant: String = "system"
    private var title: String = ""
    private var menuItems: List<Map<String, Any>> = emptyList()
    private var selectedItemIdentifier: String? = null
    private var checkedColor: String = "#007AFF" // Default iOS blue
    private var uncheckedColor: String = "#8E8E93" // Default iOS gray
    private var textColor: String? = null
    private var disabled: Boolean = false
    private var androidDisplayMode: String? = "dialog"
    private var accessibilityLabelProp: String? = null
    private var menuAccessibilityHint: String? = null
    private var enforceMinimumTouchTarget: Boolean = true

    init {
        setupMenuTrigger()
    }

    private fun setupMenuTrigger() {
        // Set click listener on the container itself
        // Children will be added by React Native
        isClickable = true
        isFocusable = true
        // Screen readers need to land on the trigger as a single control, not on the
        // decorative child text, so it is announced as an actionable button.
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        accessibilityDelegate = createAccessibilityDelegate()
    }

    /**
     * Deliberately a function, not a `val`. `init {}` calls `setupMenuTrigger()` before a
     * property declared further down has been initialised, so assigning from a `val` here
     * silently installed `null` — the delegate never ran and the trigger kept reporting as
     * a bare FrameLayout with no role, state, or expand/collapse actions.
     */
    private fun createAccessibilityDelegate() = object : View.AccessibilityDelegate() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.className = android.widget.Button::class.java.name
            info.isEnabled = !disabled
            info.isClickable = !disabled
            info.contentDescription = resolveAccessibilityLabel()

            val hint = menuAccessibilityHint ?: DEFAULT_MENU_HINT
            info.addAction(
                AccessibilityNodeInfo.AccessibilityAction(
                    AccessibilityNodeInfo.ACTION_CLICK,
                    hint
                )
            )

            // Expose the open/closed state so TalkBack can announce and drive it.
            if (isMenuShowing()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_COLLAPSE)
            } else if (!disabled) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND)
            }
        }

        override fun performAccessibilityAction(host: View, action: Int, args: Bundle?): Boolean {
            return when (action) {
                AccessibilityNodeInfo.ACTION_EXPAND -> {
                    open()
                    true
                }
                AccessibilityNodeInfo.ACTION_COLLAPSE -> {
                    close()
                    true
                }
                else -> super.performAccessibilityAction(host, action, args)
            }
        }
    }

    /**
     * Label precedence mirrors iOS: explicit prop, then the selected item, then `title`.
     * Falls back to the child content so the trigger is never announced nameless.
     */
    private fun resolveAccessibilityLabel(): CharSequence? {
        accessibilityLabelProp?.takeIf { it.isNotEmpty() }?.let { return it }

        selectedItemIdentifier?.let { selected ->
            menuItems.firstOrNull { it["identifier"] == selected }?.let { item ->
                // Fall through rather than return an empty name: setMenuItems stores an
                // absent title as "", so a selected item with neither a label nor a title
                // would otherwise leave the trigger announced with no name at all.
                val resolved = (item["accessibilityLabel"] as? String)?.takeIf { it.isNotEmpty() }
                    ?: (item["title"] as? String)?.takeIf { it.isNotEmpty() }
                if (resolved != null) {
                    return resolved
                }
            }
        }

        if (title.isNotEmpty()) {
            return title
        }

        return flattenChildText().takeIf { it.isNotEmpty() }
    }

    /**
     * Starts at the content views, never at this view: reading our own contentDescription
     * here would feed the value we just derived back into the derivation.
     *
     * On Android the JS wrapper renders MenuView as an absolutely-positioned overlay with
     * the app's content as a SIBLING rather than a child (see `src/index.tsx`), so this
     * view usually has no children at all. Fall back to the siblings in that case.
     */
    private fun flattenChildText(): String {
        val sources: List<View> = if (childCount > 0) {
            (0 until childCount).map { getChildAt(it) }
        } else {
            (parent as? ViewGroup)
                ?.let { p -> (0 until p.childCount).map { p.getChildAt(it) } }
                // Never another MenuView: their descriptions are themselves derived, so
                // including them makes each pass swallow and re-swallow the others.
                ?.filter { it !== this && it !is MenuView }
                ?: emptyList()
        }

        return sources
            .map { textOf(it) }
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .take(MAX_DERIVED_LABEL_CHARS)
    }

    private fun textOf(view: View): String {
        if (view is TextView && !view.text.isNullOrEmpty()) {
            return view.text.toString()
        }
        view.contentDescription?.takeIf { it.isNotEmpty() }?.let { return it.toString() }
        if (view is ViewGroup) {
            return (0 until view.childCount)
                .map { textOf(view.getChildAt(it)) }
                .filter { it.isNotEmpty() }
                .joinToString(" ")
        }
        return ""
    }

    /**
     * Mirrors the resolved label onto the View itself. The AccessibilityDelegate already sets
     * it on the node, but TalkBack and inspection tooling also read View.contentDescription,
     * and RN leaves it null whenever the app supplied no explicit accessibilityLabel.
     */
    private fun syncContentDescription() {
        val resolved = resolveAccessibilityLabel()
        if (contentDescription?.toString() != resolved?.toString()) {
            contentDescription = resolved
        }
    }

    /**
     * Collapses the trigger to a single screen-reader element by hiding the content views,
     * which this view already announces.
     *
     * A native backstop for the `importantForAccessibility` prop the JS wrapper already sets,
     * so the behaviour does not depend on that wrapper surviving view flattening.
     *
     * Safe to walk siblings because the JS wrapper is marked `collapsable={false}`, so this
     * view's parent is always that wrapper and never the surrounding screen.
     *
     * To verify this, dump the COMPRESSED hierarchy — `uiautomator dump --compressed`, the
     * tree a screen reader actually walks. A plain `uiautomator dump` deliberately includes
     * views that are not important for accessibility, so the hidden content still shows up
     * there and looks like a failure.
     */
    private fun hideContentFromAccessibility() {
        val targets: List<View> = if (childCount > 0) {
            (0 until childCount).map { getChildAt(it) }
        } else {
            (parent as? ViewGroup)
                ?.let { p -> (0 until p.childCount).map { p.getChildAt(it) } }
                ?.filter { it !== this && it !is MenuView }
                ?: emptyList()
        }

        targets.forEach { view ->
            if (view.importantForAccessibility != IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS) {
                view.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            }
        }
    }

    fun setAccessibilityLabelProp(label: String?) {
        this.accessibilityLabelProp = label
        syncContentDescription()
    }

    fun setMenuAccessibilityHint(hint: String?) {
        this.menuAccessibilityHint = hint
    }

    fun setEnforceMinimumTouchTarget(enforce: Boolean) {
        this.enforceMinimumTouchTarget = enforce
        requestLayout()
    }

    /**
     * Grows the *touch* area to at least 48dp via a TouchDelegate on the parent. Layout is
     * untouched, so nothing moves on screen — only hit-testing changes (WCAG 2.2 target size).
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        applyMinimumTouchTarget()
        hideContentFromAccessibility()
        // Resolved here, not at prop time: the child-text fallback depends on text React
        // Native lays out asynchronously.
        syncContentDescription()
    }

    private fun applyMinimumTouchTarget() {
        val parentView = parent as? View ?: return
        if (!enforceMinimumTouchTarget) {
            parentView.touchDelegate = null
            return
        }

        val minPx = (MIN_TOUCH_TARGET_DP * resources.displayMetrics.density).toInt()
        val widthDeficit = maxOf(0, minPx - width)
        val heightDeficit = maxOf(0, minPx - height)
        if (widthDeficit == 0 && heightDeficit == 0) {
            // Clear, don't just skip: a delegate installed while the view was smaller would
            // otherwise keep its stale oversized rect once the view grows past the minimum.
            parentView.touchDelegate = null
            return
        }

        val rect = android.graphics.Rect()
        getHitRect(rect)
        rect.inset(-widthDeficit / 2, -heightDeficit / 2)
        parentView.touchDelegate = android.view.TouchDelegate(rect, this)
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        // This view already intercepts every touch, so children are never interactive and
        // are decorative to a screen reader — the trigger itself carries the announcement.
        // `labelFromChildren` still reads their text directly for the label fallback.
        child.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
    }

    override fun onInterceptTouchEvent(ev: android.view.MotionEvent?): Boolean {
        // Intercept all touch events to handle them at the parent level
        return true
    }

    override fun onTouchEvent(event: android.view.MotionEvent?): Boolean {
        // Handle touch events - show menu on tap only if not disabled
        if (disabled) {
            return false
        }
        if (event?.action == android.view.MotionEvent.ACTION_UP) {
            performClick()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        showMenu()
        return true
    }

    fun setAndroidDisplayMode(mode: String?) {
        this.androidDisplayMode = mode
    }
    
    fun setColor(color: String?) {
        // Store text color for potential future use with child views
        textColor = color
    }

    fun setCheckedColor(color: String?) {
        color?.let {
            try {
                Color.parseColor(it) // Validate color format
                checkedColor = it
            } catch (e: IllegalArgumentException) {
                // Handle invalid color format, keep default
            }
        }
    }

    fun setUncheckedColor(color: String?) {
        color?.let {
            try {
                Color.parseColor(it) // Validate color format
                uncheckedColor = it
            } catch (e: IllegalArgumentException) {
                // Handle invalid color format, keep default
            }
        }
    }

    fun setSelectedIdentifier(selectedIdentifier: String?) {
        this.selectedItemIdentifier = selectedIdentifier
        // The announced label is derived from the selection, so it must be recomputed here:
        // a prop change alone does not trigger a layout pass.
        syncContentDescription()
    }

    fun setDisabled(disabled: Boolean) {
        this.disabled = disabled
        updateDisabledState()
    }

    private fun updateDisabledState() {
        isClickable = !disabled
        isFocusable = !disabled
    }

    fun setTitle(title: String?) {
        this.title = title ?: ""
        syncContentDescription()
    }

    fun setThemeVariant(themeVariant: String?) {
        this.themeVariant = themeVariant ?: "system"
    }

    private fun isDarkMode(): Boolean {
        return when (themeVariant) {
            "dark" -> true
            "light" -> false
            else -> {
                val currentNightMode = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    private fun getBackgroundColor(): Int {
        return if (isDarkMode()) {
            Color.parseColor("#1C1C1E") // iOS Dark Gray
        } else {
            Color.WHITE
        }
    }

    private fun getTextColor(): Int {
        return if (isDarkMode()) {
            Color.WHITE
        } else {
            Color.BLACK
        }
    }

    fun setMenuItems(menuItems: ReadableArray?) {
        val items = mutableListOf<Map<String, Any>>()
        
        menuItems?.let { array ->
            for (i in 0 until array.size()) {
                val item = array.getMap(i)
                if (item != null) {
                    val menuItem = mutableMapOf<String, Any>(
                        "identifier" to (item.getString("identifier") ?: ""),
                        "title" to (item.getString("title") ?: "")
                    )
                    
                    if (item.hasKey("subtitle")) {
                        menuItem["subtitle"] = item.getString("subtitle") ?: ""
                    }
                    
                    if (item.hasKey("destructive")) {
                        menuItem["destructive"] = item.getBoolean("destructive")
                    }

                    if (item.hasKey("accessibilityLabel")) {
                        menuItem["accessibilityLabel"] = item.getString("accessibilityLabel") ?: ""
                    }

                    if (item.hasKey("accessibilityHint")) {
                        menuItem["accessibilityHint"] = item.getString("accessibilityHint") ?: ""
                    }

                    if (item.hasKey("testID")) {
                        menuItem["testID"] = item.getString("testID") ?: ""
                    }

                    items.add(menuItem)
                }
            }
        }
        
        this.menuItems = items
        syncContentDescription()
    }

    private var currentDialog: Dialog? = null
    private var currentPopup: android.widget.PopupMenu? = null
    
    fun open() {
        if (!disabled) {
            showMenu()
        }
    }

    fun close() {
        currentDialog?.dismiss()
        currentPopup?.dismiss()
    }

    private fun isMenuShowing(): Boolean = currentDialog?.isShowing == true || currentPopup != null

    override fun onDetachedFromWindow() {
        // Dismiss before the host window goes away, otherwise Android leaks the window
        close()
        super.onDetachedFromWindow()
    }

    private fun showMenu() {
        // A second menu would orphan the first one's reference, leaving it undismissable
        if (isMenuShowing()) {
            return
        }

        // Check if tooltip mode is requested
        val useTooltip =  androidDisplayMode == "tooltip"

        if (useTooltip) {
            showTooltipMenu()
        } else {
            showDialogMenu()
        }
    }
    
    private fun showTooltipMenu() {
        // Apply theme wrapper for PopupMenu
        val contextThemeWrapper = android.view.ContextThemeWrapper(
            context, 
            if (themeVariant == "dark") android.R.style.Theme_DeviceDefault_NoActionBar 
            else android.R.style.Theme_DeviceDefault_Light_NoActionBar
        )
        
        val popup = android.widget.PopupMenu(contextThemeWrapper, this)
        currentPopup = popup
        
        // Add items to the menu
        menuItems.forEachIndexed { index, item ->
            val title = item["title"] as String
            val identifier = item["identifier"] as String
            val destructive = item["destructive"] as? Boolean == true
            
            val menuItem = popup.menu.add(0, index, index, title)
            
            // Handle destructive items (Red text)
            if (destructive) {
                val spannableTitle = android.text.SpannableString(title)
                spannableTitle.setSpan(
                    android.text.style.ForegroundColorSpan(Color.RED),
                    0,
                    spannableTitle.length,
                    android.text.Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                menuItem.title = spannableTitle
            }
            
            // Handle selection state
            if (identifier == selectedItemIdentifier) {
                menuItem.isCheckable = true
                menuItem.isChecked = true
            }

            // Let a per-item accessibilityLabel override what TalkBack reads (API 26+).
            val itemLabel = (item["accessibilityLabel"] as? String)?.takeIf { it.isNotEmpty() }
            if (itemLabel != null &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
            ) {
                menuItem.contentDescription = itemLabel
            }
        }
        
        // Force show icons/checkmarks if possible (requires internal API or Android Q+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            popup.setForceShowIcon(true)
        }
        
        popup.setOnMenuItemClickListener { menuItem ->
            val index = menuItem.itemId
            if (index >= 0 && index < menuItems.size) {
                val item = menuItems[index]
                selectMenuItem(item["identifier"] as String, item["title"] as String)
                true
            } else {
                false
            }
        }
        
        popup.setOnDismissListener {
            currentPopup = null
        }
        
        popup.show()
    }

    private fun showDialogMenu() {
        val dialogView = createModalMenuView()
        
        currentDialog = Dialog(context).apply {
            setContentView(dialogView)
            setCancelable(true)
            
            // Set dialog to appear at bottom with side margins
            window?.let { window ->
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window.setGravity(Gravity.BOTTOM)
                
                // Add horizontal margins using layout params
                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels
                val marginPixels = (12 * displayMetrics.density).toInt()
                val dialogWidth = screenWidth - (marginPixels * 2)
                val maxDialogHeight = (screenHeight * 0.7).toInt() // Maximum 70% of screen height
                
                window.setLayout(
                    dialogWidth,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                
                // Set maximum height constraint
                window.attributes = window.attributes.apply {
                    height = WindowManager.LayoutParams.WRAP_CONTENT
                    // This will be handled by the ScrollView's maxHeight
                }
                
                // Add slide up animation, unless the user asked for reduced motion
                if (!isReduceMotionEnabled()) {
                    window.attributes?.windowAnimations = android.R.style.Animation_Dialog
                }
            }

            // Name the window so TalkBack announces what just opened.
            val menuName = title.takeIf { it.isNotEmpty() } ?: resolveAccessibilityLabel()
            setTitle(menuName)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                dialogView.accessibilityPaneTitle = menuName
            }

            // Clear reference when dialog is dismissed
            setOnDismissListener {
                currentDialog = null
                returnAccessibilityFocusToTrigger()
            }
        }

        currentDialog?.show()

        // Move screen-reader focus into the menu once it is on screen.
        dialogView.post {
            dialogView.performAccessibilityAction(
                AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS,
                null
            )
        }
    }

    /**
     * TalkBack focus would otherwise be stranded on the dismissed window, so hand it back to
     * the trigger the user activated.
     */
    private fun returnAccessibilityFocusToTrigger() {
        post {
            performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
        }
    }

    /** Honours the system "remove animations" accessibility setting. */
    private fun isReduceMotionEnabled(): Boolean {
        return try {
            android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                1f
            ) == 0f
        } catch (e: Exception) {
            false
        }
    }
    
    private fun createModalMenuView(): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            
            // Set background with rounded corners - based on theme
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                setColor(getBackgroundColor())
                cornerRadius = 24f
            }
            background = drawable
        }
        
        // Header
        if (title.isNotEmpty()) {
            val headerText = TextView(context).apply {
                text = title
                setTextColor(getTextColor())
                textSize = 18f
                setPadding(60, 40, 60, 40)
                gravity = android.view.Gravity.CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            container.addView(headerText)
            
            // Add separator after title
            val separator = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (0.5 * context.resources.displayMetrics.density).toInt()
                )
                setBackgroundColor(if (isDarkMode()) Color.parseColor("#38383A") else Color.parseColor("#E0E0E0"))
            }
            container.addView(separator)
        }
        
        // Create a ScrollView to contain the items with dynamic height
        val displayMetrics = context.resources.displayMetrics
        // Max 90% of screen height - allows near full screen, only scrolls when content exceeds this
        val maxScrollHeight = (displayMetrics.heightPixels * 0.9).toInt()
        
        val scrollView = object : ScrollView(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                // Measure child first to get actual content height
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                
                val contentHeight = measuredHeight
                
                // If content fits within max height, use content height (wrap_content behavior)
                // Otherwise, limit to max height and enable scrolling
                if (contentHeight <= maxScrollHeight) {
                    // Content fits - use actual content height
                    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY))
                } else {
                    // Content overflows - limit to max height and scroll
                    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maxScrollHeight, MeasureSpec.EXACTLY))
                }
            }
        }.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isVerticalScrollBarEnabled = true
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            isFillViewport = false
        }
        
        // Radio group for menu items - will grow with content
        val radioGroup = RadioGroup(context).apply {
            setPadding(60, 20, 60, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        menuItems.forEachIndexed { index, item ->
            // We'll create a custom view that mimics a RadioButton but allows rich content
            // However, since the user specifically requested "RadioButton", we will use a RadioButton
            // but we'll need to customize it heavily or wrap it to support subtitle/icon.
            // A standard RadioButton in Android is a TextView with a button drawable. 
            // It's hard to add a subtitle/icon *inside* the RadioButton text easily without Spannables or custom compound drawables.
            
            // To respect "restore RadioButton" but also "rich features", we can use a RadioButton 
            // but set its text to empty and put it inside a container with our rich views, 
            // OR we can just use RadioButton and try to use SpannableString for title/subtitle 
            // and compound drawables for icons.
            
            // Let's try the container approach where the RadioButton is the "checkmark" 
            // and the whole row is clickable.

            // RadioButton as the selection indicator - created first to be referenced in itemContainer listener
            val radioButton = RadioButton(context).apply {
                isChecked = item["identifier"] == selectedItemIdentifier
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                
                // Custom tinting
                val colorStateList = android.content.res.ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(
                        Color.parseColor(checkedColor),
                        Color.parseColor(uncheckedColor)
                    )
                )
                buttonTintList = colorStateList

                // Handle click directly on radio button (though hidden from accessibility, it might still be clickable)
                setOnClickListener {
                    selectMenuItem(item["identifier"] as String, item["title"] as String)
                    currentDialog?.dismiss()
                }

                // Hide from accessibility as the container will represent the item
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            
            val itemContainer = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16)
                }
                gravity = Gravity.CENTER_VERTICAL
                isClickable = true
                isFocusable = true
                // WCAG 2.2 target size: rows are the primary controls inside the menu.
                minimumHeight = (MIN_TOUCH_TARGET_DP * resources.displayMetrics.density).toInt()

                setOnClickListener {
                    radioButton.performClick()
                }

                // The row is the accessible item, so announce it as one node rather than
                // letting TalkBack read the title and subtitle as separate stray text.
                val itemLabel = (item["accessibilityLabel"] as? String)?.takeIf { it.isNotEmpty() }
                val itemHint = (item["accessibilityHint"] as? String)?.takeIf { it.isNotEmpty() }
                val itemSubtitle = (item["subtitle"] as? String)?.takeIf { it.isNotEmpty() }
                val destructive = item["destructive"] as? Boolean == true

                contentDescription = buildString {
                    append(itemLabel ?: (item["title"] as String))
                    if (itemLabel == null && itemSubtitle != null) {
                        append(", ")
                        append(itemSubtitle)
                    }
                    if (destructive) {
                        append(", ")
                        append(DESTRUCTIVE_ANNOUNCEMENT)
                    }
                }

                // Accessibility delegate to make the row act like a radio button
                accessibilityDelegate = object : View.AccessibilityDelegate() {
                    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        info.className = RadioButton::class.java.name
                        info.isCheckable = true
                        info.isChecked = radioButton.isChecked
                        info.isSelected = radioButton.isChecked
                        // Surface the test id the way React Native does for `testID`
                        // (ReactAccessibilityDelegate sets viewIdResourceName), so black-box
                        // E2E tools see it as resource-id instead of it polluting
                        // contentDescription. Defaults to the item's own identifier.
                        val itemTestID = (item["testID"] as? String)?.takeIf { it.isNotEmpty() }
                            ?: (item["identifier"] as? String)?.takeIf { it.isNotEmpty() }
                        if (itemTestID != null) {
                            info.viewIdResourceName = itemTestID
                        }
                        if (itemHint != null) {
                            info.addAction(
                                AccessibilityNodeInfo.AccessibilityAction(
                                    AccessibilityNodeInfo.ACTION_CLICK,
                                    itemHint
                                )
                            )
                        }
                    }
                }
            }

            // Text Container (Title + Subtitle)
            val textContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                // The row already carries the full announcement; without this TalkBack
                // would read the title and subtitle a second time as loose text.
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            }

            val titleView = TextView(context).apply {
                text = item["title"] as String
                textSize = 16f
                
                val isDestructive = item["destructive"] as? Boolean == true
                if (isDestructive) {
                    setTextColor(Color.parseColor("#FF3B30"))
                } else {
                    setTextColor(getTextColor())
                }
            }
            textContainer.addView(titleView)

            val subtitle = item["subtitle"] as? String
            if (!subtitle.isNullOrEmpty()) {
                val subtitleView = TextView(context).apply {
                    text = subtitle
                    textSize = 14f
                    setTextColor(Color.parseColor("#8E8E93")) // Gray
                    setPadding(0, 4, 0, 0)
                }
                textContainer.addView(subtitleView)
            }

            itemContainer.addView(textContainer)
            itemContainer.addView(radioButton)

            radioGroup.addView(itemContainer)
            
            // Add divider between items (except after the last item)
            if (index < menuItems.size - 1) {
                val divider = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (0.5 * context.resources.displayMetrics.density).toInt()
                    ).apply {
                        setMargins(0, 8, 0, 8)
                    }
                    setBackgroundColor(if (isDarkMode()) Color.parseColor("#38383A") else Color.parseColor("#E0E0E0"))
                }
                radioGroup.addView(divider)
            }
        }
        
        scrollView.addView(radioGroup)
        container.addView(scrollView)
        
        // Add some bottom padding for better visual spacing
        val bottomSpacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                20
            )
        }
        container.addView(bottomSpacer)
        
        return container
    }

    private fun selectMenuItem(identifier: String, title: String) {
        // No longer store selectedIdentifier internally - it's controlled by props
        sendMenuSelection(identifier, title)
    }

    private fun sendMenuSelection(identifier: String, title: String) {
        val event: WritableMap = Arguments.createMap().apply {
            putString("identifier", identifier)
            putString("title", title)
        }
        
        val reactContext = context as ReactContext
        reactContext
            .getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(id, "onMenuSelect", event)
    }

    companion object {
        /** WCAG 2.2 minimum target size, in dp. Applied to hit-testing only. */
        private const val MIN_TOUCH_TARGET_DP = 48f
        private const val DEFAULT_MENU_HINT = "Opens a menu"
        private const val DESTRUCTIVE_ANNOUNCEMENT = "destructive"

        /** Backstop so a derived label can never become an unbounded wall of text. */
        private const val MAX_DERIVED_LABEL_CHARS = 200
    }
}
