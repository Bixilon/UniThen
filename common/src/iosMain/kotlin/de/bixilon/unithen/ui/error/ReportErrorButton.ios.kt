package de.bixilon.unithen.ui.error

import androidx.compose.runtime.Composable
import de.bixilon.unithen.BuildInfo
import platform.Foundation.NSError
import platform.MessageUI.MFMailComposeResult
import platform.MessageUI.MFMailComposeViewController
import platform.MessageUI.MFMailComposeViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.darwin.NSObject

class MailDelegate : NSObject(), MFMailComposeViewControllerDelegateProtocol {

    override fun mailComposeController(controller: MFMailComposeViewController, didFinishWithResult: MFMailComposeResult, error: NSError?) {
        controller.dismissViewControllerAnimated(true, completion = null)
    }
}

@Composable
actual fun useSendCrashMail(): (stack: String) -> Unit {
    return send@{
        if (!MFMailComposeViewController.canSendMail()) {
            return@send
        }

        val controller = MFMailComposeViewController().apply {
            setToRecipients(listOf(CRASH_ADDRESS))
            setSubject("UniThen Crash")
            setMessageBody("Hi there,\nApp version: ${BuildInfo.VERSION} on iOS ${UIDevice.currentDevice.systemVersion}\nPlease see the exception below:\n\n${it}\n\n\nCan you please fix this issue?\nThanks!", false)
        }

        controller.mailComposeDelegate = MailDelegate()

        val view = UIApplication.sharedApplication.keyWindow?.rootViewController

        view?.presentViewController(controller, animated = true, completion = null)
    }
}
