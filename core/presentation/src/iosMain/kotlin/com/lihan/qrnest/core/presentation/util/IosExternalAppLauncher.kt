@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.lihan.qrnest.core.presentation.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.useContents
import platform.Contacts.CNContact
import platform.Contacts.CNLabeledValue
import platform.Contacts.CNMutableContact
import platform.Contacts.CNPhoneNumber
import platform.ContactsUI.CNContactViewController
import platform.ContactsUI.CNContactViewControllerDelegateProtocol
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.darwin.NSObject

class IosExternalAppLauncher : ExternalAppLauncher {

    private var contactDelegateRef: ContactDelegate? = null

    override fun openUrl(url: String) {
        val normalized = if (url.startsWith("http://", ignoreCase = true) ||
            url.startsWith("https://", ignoreCase = true)
        ) {
            url
        } else {
            "https://$url"
        }
        val nsUrl = NSURL.URLWithString(normalized) ?: return
        UIApplication.sharedApplication.openURL(
            url = nsUrl,
            options = emptyMap<Any?, Any>(),
            completionHandler = null
        )
    }

    override fun dialPhone(phone: String) {
        val cleanNumber = phone.removePrefix("tel:").trim()
        val nsUrl = NSURL.URLWithString("tel:$cleanNumber") ?: return
        UIApplication.sharedApplication.openURL(
            url = nsUrl,
            options = emptyMap<Any?, Any>(),
            completionHandler = null
        )
    }

    override fun openGeo(latLng: String) {
        val parts = latLng.removePrefix("geo:").split(",")
        val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull() ?: 0.0
        val lng = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0
        val nsUrl = NSURL.URLWithString("http://maps.apple.com/?ll=$lat,$lng&q=$lat,$lng") ?: return
        UIApplication.sharedApplication.openURL(
            url = nsUrl,
            options = emptyMap<Any?, Any>(),
            completionHandler = null
        )
    }

    @OptIn(BetaInteropApi::class)
    override fun insertContact(name: String, email: String, phone: String) {
        val topController = topViewController() ?: return
        val contact = CNMutableContact().apply {
            setGivenName(name)
            if (phone.isNotBlank()) {
                setPhoneNumbers(
                    listOf(
                        CNLabeledValue.labeledValueWithLabel(
                            label = "Mobile",
                            value = CNPhoneNumber.phoneNumberWithStringValue(phone)
                        )
                    )
                )
            }
            if (email.isNotBlank()) {
                setEmailAddresses(
                    listOf(
                        CNLabeledValue.labeledValueWithLabel(
                            label = "Home",
                            value = NSString.create(string = email)
                        )
                    )
                )
            }
        }
        val contactController = CNContactViewController.viewControllerForNewContact(contact)
        val delegate = ContactDelegate {
            topController.dismissViewControllerAnimated(true, completion = null)
            contactDelegateRef = null
        }
        contactDelegateRef = delegate
        contactController.delegate = delegate
        val navigationController = UINavigationController(rootViewController = contactController)
        topController.presentViewController(navigationController, animated = true, completion = null)
    }

    override fun shareText(text: String) {
        val topController = topViewController() ?: return
        val activityController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )
        activityController.popoverPresentationController?.apply {
            sourceView = topController.view
            sourceRect = topController.view.bounds.useContents {
                CGRectMake(size.width / 2.0, size.height / 2.0, 0.0, 0.0)
            }
            permittedArrowDirections = 0uL
        }
        topController.presentViewController(activityController, animated = true, completion = null)
    }

    override fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(
            url = settingsUrl,
            options = emptyMap<Any?, Any>(),
            completionHandler = null
        )
    }

    override fun openWifiSettings() {
        openAppSettings()
    }

    private fun topViewController(): UIViewController? {
        val app = UIApplication.sharedApplication
        val keyWindow = app.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { it.windows.filterIsInstance<UIWindow>() }
            .firstOrNull { it.isKeyWindow() }
            ?: app.keyWindow
        var controller = keyWindow?.rootViewController
        while (controller?.presentedViewController != null) {
            controller = controller.presentedViewController
        }
        return controller
    }

    private class ContactDelegate(
        private val onComplete: () -> Unit
    ) : NSObject(), CNContactViewControllerDelegateProtocol {
        override fun contactViewController(
            viewController: CNContactViewController,
            didCompleteWithContact: CNContact?
        ) {
            onComplete()
        }
    }
}
