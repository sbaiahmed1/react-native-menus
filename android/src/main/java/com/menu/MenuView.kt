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
    private var menuItems: List<Map<String, String>> = emptyList()
    private var selectedItemIdentifier: String? = null
    private var checkedColor: String = "#007AFF" // Default iOS blue
    private var uncheckedColor: String = "#8E8E93" // Default iOS gray
    private var textColor: String? = null

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
        // Handle touch events - show menu on tap
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

    fun setMenuItems(menuItems: ReadableArray?) {
        val items = mutableListOf<Map<String, String>>()
        
        menuItems?.let { array ->
            for (i in 0 until array.size()) {
                val item = array.getMap(i)
                if (item != null) {
                    val menuItem = mapOf(
                        "identifier" to (item.getString("identifier") ?: ""),
                        "title" to (item.getString("title") ?: "")
                    )
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
            
            // Set background with rounded corners - always white will add new prop soon
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 24f
            }
            background = drawable
        }
        
        // Header - removed as per user request
        // val headerText = TextView(context).apply {
        //     text = "--sous-thème--"
        //     setTextColor(Color.WHITE)
        //     textSize = 18f
        //     setPadding(60, 40, 60, 40)
        //     gravity = android.view.Gravity.CENTER
        //     setTypeface(null, android.graphics.Typeface.BOLD)
        // }
        // container.addView(headerText)
        
        // Create a ScrollView to contain the radio group with dynamic height
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
            val radioButton = RadioButton(context).apply {
                text = item["title"]
                setTextColor(Color.BLACK) // Changed to black for white background
                textSize = 16f
                setPadding(0, 30, 0, 30)
                isChecked = item["identifier"] == selectedItemIdentifier
                
                // Custom radio button styling with dynamic colors
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
                
                // Make sure text wraps properly for long content
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                
                setOnClickListener {
                    selectMenuItem(item["identifier"] ?: "", item["title"] ?: "")
                    // Close dialog immediately when item is selected
                    currentDialog?.dismiss()
                }
            }
            radioGroup.addView(radioButton)
            
            // Add divider between items (except after the last item)
            if (index < menuItems.size - 1) {
                val divider = View(context).apply {
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        (0.5 * context.resources.displayMetrics.density).toInt() // 0.5px converted to dp
                    ).apply {
                        setMargins(0, 8, 0, 8) // Small margins around divider
                    }
                    setBackgroundColor(Color.parseColor("#E0E0E0")) // Light gray color
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
