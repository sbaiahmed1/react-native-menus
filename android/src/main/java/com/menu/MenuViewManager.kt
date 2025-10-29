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
