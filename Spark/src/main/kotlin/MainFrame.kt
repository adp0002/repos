package org.adprasad

import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.CefInitializationException
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import me.friwi.jcefmaven.UnsupportedPlatformException
import org.cef.CefApp
import org.cef.CefApp.CefAppState
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.handler.CefDisplayHandlerAdapter
import org.cef.handler.CefFocusHandlerAdapter
import java.awt.BorderLayout
import java.awt.Component
import java.awt.KeyboardFocusManager
import java.awt.event.*
import java.io.IOException
import javax.swing.JFrame
import javax.swing.JTextField
import kotlin.collections.isNotEmpty
import kotlin.jvm.Throws
import kotlin.system.exitProcess


/**
 * This is a simple example application using JCEF.
 * It displays a JFrame with a JTextField at its top and a CefBrowser in its
 * center. The JTextField is used to enter and assign an URL to the browser UI.
 * No additional handlers or callbacks are used in this example.
 *
 * The number of used JCEF classes is reduced (nearly) to its minimum and should
 * assist you to get familiar with JCEF.
 *
 * For a more feature complete example have also a look onto the example code
 * within the package "tests.detailed".
 */
class MainFrame private constructor(startURL: String?, useOSR: Boolean, isTransparent: Boolean, args: Array<String?>) :
    JFrame() {
    private val address_: JTextField
    private val cefApp_: CefApp
    private val client_: CefClient
    private val browser_: CefBrowser
    private val browerUI_: Component
    private var browserFocus_ = true

    /**
     * To display a simple browser window, it suffices completely to create an
     * instance of the class CefBrowser and to assign its UI component to your
     * application (e.g. to your content pane).
     * But to be more verbose, this CTOR keeps an instance of each object on the
     * way to the browser UI.
     */
    init {
        // (0) Initialize CEF using the maven loader
        val builder = CefAppBuilder()
        // windowless_rendering_enabled must be set to false if not wanted.
        builder.cefSettings.windowless_rendering_enabled = useOSR
        // USE builder.setAppHandler INSTEAD OF CefApp.addAppHandler!
        // Fixes compatibility issues with MacOSX
        builder.setAppHandler(object : MavenCefAppHandlerAdapter() {
            override fun stateHasChanged(state: CefAppState?) {
                // Shutdown the app if the native CEF part is terminated
                if (state == CefAppState.TERMINATED) exitProcess(0)
            }
        })

        if (args.isNotEmpty()) {
            builder.addJcefArgs(*args)
        }

        // (1) The entry point to JCEF is always the class CefApp. There is only one
        //     instance per application and therefore you have to call the method
        //     "getInstance()" instead of a CTOR.
        //
        //     CefApp is responsible for the global CEF context. It loads all
        //     required native libraries, initializes CEF accordingly, starts a
        //     background task to handle CEF's message loop and takes care of
        //     shutting down CEF after disposing it.
        //
        //     WHEN WORKING WITH MAVEN: Use the builder.build() method to
        //     build the CefApp on first run and fetch the instance on all consecutive
        //     runs. This method is thread-safe and will always return a valid app
        //     instance.
        cefApp_ = builder.build()

        // (2) JCEF can handle one to many browser instances simultaneous. These
        //     browser instances are logically grouped together by an instance of
        //     the class CefClient. In your application you can create one to many
        //     instances of CefClient with one to many CefBrowser instances per
        //     client. To get an instance of CefClient you have to use the method
        //     "createClient()" of your CefApp instance. Calling an CTOR of
        //     CefClient is not supported.
        //
        //     CefClient is a connector to all possible events which come from the
        //     CefBrowser instances. Those events could be simple things like the
        //     change of the browser title or more complex ones like context menu
        //     events. By assigning handlers to CefClient you can control the
        //     behavior of the browser. See tests.detailed.MainFrame for an example
        //     of how to use these handlers.
        client_ = cefApp_.createClient()

        // (3) Create a simple message router to receive messages from CEF.
        val msgRouter = CefMessageRouter.create()
        client_.addMessageRouter(msgRouter)

        // (4) One CefBrowser instance is responsible to control what you'll see on
        //     the UI component of the instance. It can be displayed off-screen
        //     rendered or windowed rendered. To get an instance of CefBrowser you
        //     have to call the method "createBrowser()" of your CefClient
        //     instances.
        //
        //     CefBrowser has methods like "goBack()", "goForward()", "loadURL()",
        //     and many more which are used to control the behavior of the displayed
        //     content. The UI is held within a UI-Compontent which can be accessed
        //     by calling the method "getUIComponent()" on the instance of CefBrowser.
        //     The UI component is inherited from a java.awt.Component and therefore
        //     it can be embedded into any AWT UI.
        browser_ = client_.createBrowser(startURL, useOSR, isTransparent)
        browerUI_ = browser_.getUIComponent()

        // (5) For this minimal browser, we need only a text field to enter an URL
        //     we want to navigate to and a CefBrowser window to display the content
        //     of the URL. To respond to the input of the user, we're registering an
        //     anonymous ActionListener. This listener is performed each time the
        //     user presses the "ENTER" key within the address field.
        //     If this happens, the entered value is passed to the CefBrowser
        //     instance to be loaded as URL.
        address_ = JTextField(startURL, 100)
        address_.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent?) {
                browser_.loadURL(address_.getText())
            }
        })

        // Update the address field when the browser URL changes.
        client_.addDisplayHandler(object : CefDisplayHandlerAdapter() {
            override fun onAddressChange(browser: CefBrowser?, frame: CefFrame?, url: String?) {
                address_.setText(url)
            }
        })

        // Clear focus from the browser when the address field gains focus.
        address_.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                if (!browserFocus_) return
                browserFocus_ = false
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
                address_.requestFocus()
            }
        })

        // Clear focus from the address field when the browser gains focus.
        client_.addFocusHandler(object : CefFocusHandlerAdapter() {
            override fun onGotFocus(browser: CefBrowser) {
                if (browserFocus_) return
                browserFocus_ = true
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
                browser.setFocus(true)
            }

            override fun onTakeFocus(browser: CefBrowser?, next: Boolean) {
                browserFocus_ = false
            }
        })

        // (6) All UI components are assigned to the default content pane of this
        //     JFrame and afterwards the frame is made visible to the user.
        contentPane.add(address_, BorderLayout.NORTH)
        contentPane.add(browerUI_, BorderLayout.CENTER)
        pack()
        setSize(800, 600)
        isVisible = true

        // (7) To take care of shutting down CEF accordingly, it's important to call
        //     the method "dispose()" of the CefApp instance if the Java
        //     application will be closed. Otherwise you'll get asserts from CEF.
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                CefApp.getInstance().dispose()
                dispose()
            }
        })
    }

    companion object {
        private val serialVersionUID = -5570653778104813836L

        @Throws(
            UnsupportedPlatformException::class,
            CefInitializationException::class,
            IOException::class,
            InterruptedException::class
        )
        @JvmStatic
        fun main(args: Array<String>) {
            //Print some info for the test reports. You can ignore this.
//            TestReportGenerator.print(args)


            // The simple example application is created as anonymous class and points
            // to Google as the very first loaded page. Windowed rendering mode is used by
            // default. If you want to test OSR mode set |useOsr| to true and recompile.
            val useOsr = false
            MainFrame("http://www.google.com", useOsr, false, args as Array<String?>)
        }
    }
}