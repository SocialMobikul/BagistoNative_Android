package com.mobikul.bagisto.utils

import com.mobikul.bagisto.components.AlertComponent
import com.mobikul.bagisto.components.BarcodeScannerComponent
import com.mobikul.bagisto.components.ButtonComponent
import com.mobikul.bagisto.components.DynamicButtonComponent
import com.mobikul.bagisto.components.FormComponent
import com.mobikul.bagisto.components.HapticComponent
import com.mobikul.bagisto.components.ImageSearchComponent
import com.mobikul.bagisto.components.LocationComponent
import com.mobikul.bagisto.components.MenuComponent
import com.mobikul.bagisto.components.NavBarButtonComponent
import com.mobikul.bagisto.components.NavigationHistoryComponent
import com.mobikul.bagisto.components.ReviewPromptComponent
import com.mobikul.bagisto.components.SearchComponent
import com.mobikul.bagisto.components.ShareComponent
import com.mobikul.bagisto.components.ThemeModeComponent
import com.mobikul.bagisto.components.ToastComponent
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
        BridgeComponentFactory("alert", ::AlertComponent),
        BridgeComponentFactory("historysync", ::NavigationHistoryComponent)
    )
}
