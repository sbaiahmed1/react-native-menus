package com.menu

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.MenuViewManagerInterface
import com.facebook.react.viewmanagers.MenuViewManagerDelegate

@ReactModule(name = MenuViewManager.NAME)
class MenuViewManager : SimpleViewManager<MenuView>(),
  MenuViewManagerInterface<MenuView> {
  private val mDelegate: ViewManagerDelegate<MenuView>

  init {
    mDelegate = MenuViewManagerDelegate(this)
  }

  override fun getDelegate(): ViewManagerDelegate<MenuView>? {
    return mDelegate
  }

  override fun getName(): String {
    return NAME
  }

  public override fun createViewInstance(context: ThemedReactContext): MenuView {
    return MenuView(context)
  }

  @ReactProp(name = "color")
  override fun setColor(view: MenuView?, color: String?) {
    view?.setBackgroundColor(Color.parseColor(color))
  }

  companion object {
    const val NAME = "MenuView"
  }
}
