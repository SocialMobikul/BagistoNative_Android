package com.masilotti.demo.utils

import com.masilotti.demo.components.AlertComponent
import com.masilotti.demo.components.BarcodeScannerComponent
import com.masilotti.demo.components.ButtonComponent
import com.masilotti.demo.components.DynamicButtonComponent
import com.masilotti.demo.components.FormComponent
import com.masilotti.demo.components.HapticComponent
import com.masilotti.demo.components.ImageSearchComponent
import com.masilotti.demo.components.LocationComponent
import com.masilotti.demo.components.MenuComponent
import com.masilotti.demo.components.NavBarButtonComponent
import com.masilotti.demo.components.ReviewPromptComponent
import com.masilotti.demo.components.SearchComponent
import com.masilotti.demo.components.ShareComponent
import com.masilotti.demo.components.ThemeModeComponent
import com.masilotti.demo.components.ToastComponent
import dev.hotwire.core.bridge.BridgeComponentFactory

object CustomBridgeComponents {

    val all = arrayOf(
        BridgeComponentFactory("barcode", ::BarcodeScannerComponent),
        BridgeComponentFactory("button", ::ButtonComponent),
        BridgeComponentFactory("dynamicbutton", ::DynamicButtonComponent),
        BridgeComponentFactory("share", ::ShareComponent),
        BridgeComponentFactory("form", ::FormComponent),
        BridgeComponentFactory("haptic", ::HapticComponent),
        BridgeComponentFactory("imagesearch", ::ImageSearchComponent),
        BridgeComponentFactory("location", ::LocationComponent),
        BridgeComponentFactory("menu", ::MenuComponent),
        BridgeComponentFactory("nav-buttono", ::NavBarButtonComponent),
        BridgeComponentFactory("review-prompt", ::ReviewPromptComponent),
        BridgeComponentFactory("search", ::SearchComponent),
        BridgeComponentFactory("thememode", ::ThemeModeComponent),
        BridgeComponentFactory("toast", ::ToastComponent),
        BridgeComponentFactory("alert", ::AlertComponent)
    )
}
