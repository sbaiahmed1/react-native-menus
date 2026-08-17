package com.menu

import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

@ReactModule(name = MenuViewManager.NAME)
class MenuViewManager : ViewGroupManager<MenuView>() {
    override fun getName(): String {
        return NAME
    }

    override fun createViewInstance(reactContext: ThemedReactContext): MenuView {
        return MenuView(reactContext)
    }

    @ReactProp(name = "title")
    fun setTitle(view: MenuView, title: String?) {
        view.setTitle(title)
    }

    @ReactProp(name = "themeVariant")
    fun setThemeVariant(view: MenuView, themeVariant: String?) {
        view.setThemeVariant(themeVariant)
    }

    @ReactProp(name = "color")
    fun setColor(view: MenuView, color: String?) {
        view.setColor(color)
    }

    @ReactProp(name = "checkedColor")
    fun setCheckedColor(view: MenuView, color: String?) {
        view.setCheckedColor(color)
    }

    @ReactProp(name = "uncheckedColor")
    fun setUncheckedColor(view: MenuView, color: String?) {
        view.setUncheckedColor(color)
    }

    @ReactProp(name = "menuItems")
    fun setMenuItems(view: MenuView, menuItems: com.facebook.react.bridge.ReadableArray?) {
        view.setMenuItems(menuItems)
    }

    @ReactProp(name = "selectedIdentifier")
    fun setSelectedIdentifier(view: MenuView, selectedIdentifier: String?) {
        view.setSelectedIdentifier(selectedIdentifier)
    }

    @ReactProp(name = "disabled")
    fun setDisabled(view: MenuView, disabled: Boolean) {
        view.setDisabled(disabled)
    }

    @ReactProp(name = "androidDisplayMode")
    fun setAndroidDisplayMode(view: MenuView, androidDisplayMode: String?) {
        view.setAndroidDisplayMode(androidDisplayMode)
    }

    // `accessibilityLabel` is a base View prop that BaseViewManager already handles (it
    // stores a tag and recomputes contentDescription). Override rather than redeclare, so
    // RN's own handling still runs, and mirror the value into the view — the trigger builds
    // its announcement from it plus the selected item, which the base handling can't know.
    override fun setAccessibilityLabel(view: MenuView, accessibilityLabel: String?) {
        super.setAccessibilityLabel(view, accessibilityLabel)
        view.setAccessibilityLabelProp(accessibilityLabel)
    }

    @ReactProp(name = "menuAccessibilityHint")
    fun setMenuAccessibilityHint(view: MenuView, menuAccessibilityHint: String?) {
        view.setMenuAccessibilityHint(menuAccessibilityHint)
    }

    @ReactProp(name = "enforceMinimumTouchTarget", defaultBoolean = true)
    fun setEnforceMinimumTouchTarget(view: MenuView, enforceMinimumTouchTarget: Boolean) {
        view.setEnforceMinimumTouchTarget(enforceMinimumTouchTarget)
    }

    override fun receiveCommand(root: MenuView, commandId: String, args: com.facebook.react.bridge.ReadableArray?) {
        when (commandId) {
            "open" -> root.open()
            "close" -> root.close()
            else -> super.receiveCommand(root, commandId, args)
        }
    }

    override fun getExportedCustomBubblingEventTypeConstants(): Map<String, Any> {
        return mapOf(
            "onMenuSelect" to mapOf(
                "phasedRegistrationNames" to mapOf(
                    "bubbled" to "onMenuSelect"
                )
            )
        )
    }

    companion object {
        const val NAME = "MenuView"
    }
}
