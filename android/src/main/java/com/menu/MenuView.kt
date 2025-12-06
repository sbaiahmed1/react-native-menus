package com.menu

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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

    init {
        setupMenuTrigger()
    }

    private fun setupMenuTrigger() {
        // Set click listener on the container itself
        // Children will be added by React Native
        isClickable = true
        isFocusable = true
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

                    items.add(menuItem)
                }
            }
        }
        
        this.menuItems = items
    }

    private var currentDialog: Dialog? = null
    
    private fun showMenu() {
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
                
                // Add slide up animation
                window.attributes?.windowAnimations = android.R.style.Animation_Dialog
            }
            
            // Clear reference when dialog is dismissed
            setOnDismissListener {
                currentDialog = null
            }
        }
        
        currentDialog?.show()
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
                
                setOnClickListener {
                    radioButton.performClick()
                }

                // Accessibility delegate to make the row act like a radio button
                accessibilityDelegate = object : View.AccessibilityDelegate() {
                    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        info.className = RadioButton::class.java.name
                        info.isCheckable = true
                        info.isChecked = radioButton.isChecked
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
}
