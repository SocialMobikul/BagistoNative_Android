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

/**
 * Central registry for all Bagisto Native bridge components.
 * 
 * This object provides a unified way to register all available bridge components
 * with Hotwire Native. Each component enables specific native functionality
 * accessible from the web layer.
 * 
 * @property all Array of BridgeComponentFactory instances for all components
 * 
 * @see BridgeComponentFactory
 * @see AlertComponent
 * @see LocationComponent
 * @see BarcodeScannerComponent
 * 
 * Usage:
 * ```kotlin
 * Hotwire.registerBridgeComponents(*CustomBridgeComponents.all)
 * ```
 * 
 * Available Components:
 * - alert: Native alert dialogs
 * - barcode: QR/barcode scanning
 * - button: Navigation buttons
 * - dynamicbutton: Dynamic cart buttons
 * - form: Form submission
 * - haptic: Haptic feedback
 * - imagesearch: ML image search
 * - location: GPS location
 * - menu: Context menu
 * - nav-button: Navigation bar buttons
 * - review-prompt: App review prompts
 * - search: Search functionality
 * - share: Native share
 * - thememode: Theme switching
 * - toast: Toast messages
 * - historysync: Navigation history
 */
object CustomBridgeComponents {

    /**
     * Array of all available bridge component factories.
     * 
     * Each factory registers a component with Hotwire Native,
     * making it accessible from the web layer via window.BagistoNative.{name}
     * 
     * @return Array of BridgeComponentFactory instances
     */
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
        BridgeComponentFactory("nav-button", ::NavBarButtonComponent),
        BridgeComponentFactory("review-prompt", ::ReviewPromptComponent),
        BridgeComponentFactory("search", ::SearchComponent),
        BridgeComponentFactory("thememode", ::ThemeModeComponent),
        BridgeComponentFactory("toast", ::ToastComponent),
        BridgeComponentFactory("alert", ::AlertComponent),
        BridgeComponentFactory("historysync", ::NavigationHistoryComponent)
    )
}
