package org.adprasad

//import javafx.application.Platform
//import javafx.beans.value.ObservableValue
//import javafx.concurrent.Worker
//import javafx.embed.swing.JFXPanel
//import javafx.scene.Scene
//import javafx.scene.control.ProgressBar
//import javafx.scene.layout.StackPane
//import javafx.scene.web.WebEngine
//import javafx.scene.web.WebHistory
//import javafx.scene.web.WebView
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import org.cef.CefApp
import org.cef.CefApp.CefAppState
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefMessageRouter
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import kotlin.system.exitProcess


fun main() {
    SwingUtilities.invokeLater {
        UserGuideApp().start()
    }
}

class UserGuideApp {

    private lateinit var currentPage: Page
    private lateinit var masterConfig: MasterConfig
    private lateinit var currentProfileConfig: ProfileConfig

    private lateinit var mainFrame: JFrame
    private lateinit var leftPane: JScrollPane
    private lateinit var rightPane: JScrollPane
    private lateinit var tree: JTree
//    private lateinit var webViewPanel: JFXPanel
//    private lateinit var webEngine: WebEngine
    private lateinit var htmlPane: JEditorPane
    private lateinit var root: DefaultMutableTreeNode
    private lateinit var browser: CefBrowser
    private lateinit var progressBar: JProgressBar

    // Jcef

    fun start() {
        try {
            setupMainWindow()
            reloadConfiguration()
//            setupCefBrowser()
            mainFrame.isVisible = true
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(mainFrame, "Config Error: Something went wrong with your config files or not found.")
        }

    }

    private fun setupCefBrowser() {
        // Initialize CEF app
        // (0) Initialize CEF using the maven loader
        val builder = CefAppBuilder()
//        builder.setInstallDir(Paths.get(System.getProperty("user.dir")).toFile())

        // windowless_rendering_enabled must be set to false if not wanted.
        builder.cefSettings.windowless_rendering_enabled = false
        // USE builder.setAppHandler INSTEAD OF CefApp.addAppHandler!
        // Fixes compatibility issues with MacOSX
        builder.setAppHandler(object : MavenCefAppHandlerAdapter() {
            override fun stateHasChanged(state: CefAppState?) {
                // Shutdown the app if the native CEF part is terminated
                if (state == CefAppState.TERMINATED) exitProcess(0)
            }
        })

        val cefApp = builder.build()
//        cefApp.addAppHandler(object : CefAppHandlerAdapter(null) {
//            override fun onBeforeCommandLineProcessing(processType: String?, commandLine: CefCommandLine) {
//                commandLine.appendSwitch("allow-file-access-from-files")
//                commandLine.appendSwitch("disable-web-security")
//            }
//        })
        // Create a client
        val client: CefClient = cefApp.createClient()
        val msgRouter = CefMessageRouter.create()
        client.addMessageRouter(msgRouter)

        // Create browser instance with initial URL
        val initialUrl = "https://www.google.com"
        val isOffscreenRenderingEnabled = false
        browser = client.createBrowser(initialUrl, isOffscreenRenderingEnabled, false)

        // Optional: Add a load handler to monitor loading events
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadingStateChange(
                browser: CefBrowser?,
                isLoading: Boolean,
                canGoBack: Boolean,
                canGoForward: Boolean
            ) {
                progressBar.isVisible = isLoading
            }

//            fun onLoadingProgressChange(browser: CefBrowser?, progress: Double) {
//                val value = (progress * 100).toInt()
//                progressBar.value = value
//                progressBar.isVisible = value < 100
//                if (value == 100) progressBar.isVisible = false
//            }

        })

        // Main window
//        mainFrame = JFrame("")
//        mainFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
//        mainFrame.setSize(1200, 700)
//        mainFrame.add(browser.uiComponent, BorderLayout.CENTER)
//        mainFrame.addWindowListener(object : WindowAdapter() {
//            override fun windowClosing(e: WindowEvent?) {
//                CefApp.getInstance().dispose()
//                mainFrame.dispose()
//            }
//        })
//        mainFrame.isVisible = true
    }

    private fun setupMainWindow() {
        // Main window
        mainFrame = JFrame("")
        mainFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainFrame.setSize(1200, 700)

        // Left container
        setupLeftContainer()

        // Right container
        setupRightContainer()

        mainFrame.add(progressBar, BorderLayout.NORTH)

        // Add left, right containers to the main window
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane)
        splitPane.dividerSize = 1
        splitPane.dividerLocation = 250
        mainFrame.contentPane.add(splitPane, BorderLayout.CENTER)
        mainFrame.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT && browser.canGoBack()) {
                    browser.goBack()
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && browser.canGoForward()) {
                    browser.goForward()
                }
            }
        })
        mainFrame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                CefApp.getInstance().dispose()
                mainFrame.dispose()
            }
        })
        mainFrame.setFocusable(true);  // Important, enable focus to get key events
        mainFrame.requestFocusInWindow();

        // Request focus on WebView Panel after visible
//        mainFrame.addWindowListener(object : java.awt.event.WindowAdapter() {
//            override fun windowActivated(e: java.awt.event.WindowEvent?) {
//                webViewPanel.requestFocusInWindow()
//            }
//        })

        setupMenuBar()
    }

    private fun setupLeftContainer() {
        root = DefaultMutableTreeNode("Index")
        tree = JTree(root)
        tree.addTreeSelectionListener {
            val selectedNode = tree.lastSelectedPathComponent
            if (selectedNode != null && selectedNode is DefaultMutableTreeNode) {
                onNodeSelected(selectedNode)
            }
        }
        leftPane = JScrollPane(tree)
        leftPane.setBorder(EmptyBorder(10, 20, 10, 20))
    }

    private fun setupRightContainer() {
        setupCefBrowser()
//        webViewPanel = JFXPanel()
//        Platform.runLater({
//            val webView = WebView()
//            webEngine = webView.engine
//            webEngine.isJavaScriptEnabled = true
//
//            val progressBar = ProgressBar(0.0).apply {
//                isVisible = true
//                prefWidth = 300.0
//            }
//
//            val webViewContainer = StackPane(webView, progressBar)
////            val webViewContainer = StackPane(browser.uiComponent, progressBar)
//            webViewPanel.scene = Scene(webViewContainer)
//
//            // Observe the loading progress of the web engine
//            webEngine.loadWorker.progressProperty()
//                .addListener { _: ObservableValue<out Number>, _: Number, newValue: Number ->
//                    progressBar.progress = newValue.toDouble()
//                    progressBar.isVisible = newValue.toDouble() < 1.0
//                }
//
//            // Optional: Hide progress bar when load completes
//            webEngine.loadWorker.stateProperty().addListener { _: ObservableValue<out Worker.State>, _, newState ->
//                if (newState == Worker.State.SUCCEEDED) {
//                    progressBar.isVisible = false
//                }
//            }
//        })
//
//        // Add KeyListener to the panel or frame
//        webViewPanel.setFocusable(true);  // Important, enable focus to get key events
//        webViewPanel.requestFocusInWindow();
//        webViewPanel.addKeyListener(object : KeyAdapter() {
//            override fun keyPressed(e: KeyEvent) {
//                Platform.runLater {
//                    val history: WebHistory = webEngine.history
//                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
//                        if (history.currentIndex > 0) history.go(-1)
//                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
//                        if (history.currentIndex < history.entries.size - 1) history.go(1)
//                    }
//                }
//            }
//        })
//        rightPane = JScrollPane(webViewPanel)

        progressBar = JProgressBar()
        progressBar.foreground = Color.blue
        progressBar.background = Color.white
        progressBar.setIndeterminate(true)
//        progressBar.setStringPainted(true)
//        progressBar.setString("Loading...")
        progressBar.repaint();
        progressBar.isVisible = false // Hide initially

        // Get current preferred width
        val currentWidth = progressBar.getPreferredSize().width
        progressBar.preferredSize = Dimension(currentWidth, 2)

        val panel = JPanel()
        browser.uiComponent.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT && browser.canGoBack()) {
                    browser.goBack()
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && browser.canGoForward()) {
                    browser.goForward()
                }
            }
        })
//        panel.add(browser.uiComponent, BorderLayout.CENTER)
        rightPane = JScrollPane(browser.uiComponent)
        rightPane.setBorder(null)
        rightPane.setViewportBorder(null)
        rightPane.getViewport().border = null
    }

    private fun openExternalLink(url: String) {
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun loadConfiguration() {
        masterConfig = ConfigManager.getMasterConfig()
        // Set current data profile
        setCurrentDataProfile()
    }

    private fun setCurrentDataProfile() {
        val defaultProfileName = masterConfig.defaultProfile
        if (defaultProfileName.isEmpty()) {
            currentProfileConfig = masterConfig.profileConfigs.first()
        }
        currentProfileConfig = masterConfig.profileConfigs.first { it.name == defaultProfileName }

//        print("Current Profile: $currentProfileConfig")
    }

    private lateinit var profilesMenu: JMenu

    /// Create window menus
    private fun setupMenuBar() {
        UIManager.put("Menu.font", Font("Arial", Font.PLAIN, 14));
        val menuBar = JMenuBar()
        // File menu
        val fileMenu = JMenu("File")
        profilesMenu = JMenu("Profiles")
        fileMenu.add(profilesMenu)

        // Reload menu
        val reloadMenu = JMenu("Reload")
        val reloadConfigMenu = JMenuItem("Config")
        reloadConfigMenu.addActionListener {
            reloadConfiguration()
        }
        val reloadContentMenu = JMenuItem("Current Page")
        reloadContentMenu.addActionListener {
            if (::currentPage.isInitialized) {
                setHtmlContent(currentPage.path)
            }
        }
        reloadMenu.add(reloadConfigMenu)
        reloadMenu.add(reloadContentMenu)

        // Add Main menus
        menuBar.add(fileMenu)
        menuBar.add(reloadMenu)
        mainFrame.jMenuBar = menuBar
    }

    private fun updateProfilesMenu() {
        profilesMenu.removeAll()
        masterConfig.profileConfigs.forEach {
            val menuItem = JMenuItem(it.name.capitalizeWords())
            menuItem.addActionListener {
                onProfileMenuSelected(menuItem.text)
            }
            profilesMenu.add(menuItem)
        }
    }

    private fun reloadConfiguration() {
        loadConfiguration()
        updateProfilesMenu()
        reloadSideBar()
        setHtmlContent(currentProfileConfig.defaultPage)
    }

    private fun onProfileMenuSelected(title: String) {
        currentProfileConfig = masterConfig.profileConfigs.first { it.name.lowercase() == title.lowercase() }
        reloadSideBar()
    }

    // Setup sidebar
    private fun reloadSideBar() {
        val treeModel = tree.model as DefaultTreeModel
        root.removeAllChildren()
        // Setup root node
        root.userObject = currentProfileConfig.name.capitalizeWords()

        // Setup group node
        currentProfileConfig.list.forEach { group ->
            // Group node
            val groupNode = DefaultMutableTreeNode(group)
            // Setup Group pages
            group.list.forEach { page ->
                val pageNode = DefaultMutableTreeNode(page)
                groupNode.add(pageNode)
            }
            treeModel.insertNodeInto(groupNode, root, root.childCount)
        }
        // Refresh sidebar
        treeModel.reload()
    }

    private fun loadDefaultPage() {
        setHtmlContent(currentProfileConfig.defaultPage)
    }

    private fun loadSelectedPage(page: Page) {
        currentPage = page
        setHtmlContent(page.path)
    }

    private fun onNodeSelected(node: DefaultMutableTreeNode) {
        if (node.isRoot) {
            loadDefaultPage()
        } else if (!node.isLeaf) {
            // Handle group node selection
            return
        } else {
            // Handle page node selection
            loadSelectedPage((node.userObject as? Page) as Page)
        }
    }

    private fun expandNodeByTitle(title: String) {
        val targetNode = findNodeByTitle(root, title)

        if (targetNode != null) {
            val path = TreePath(targetNode.path)
            tree.expandPath(path)
            tree.scrollPathToVisible(path) // Optional: scrolls the tree to make the node visible
        }
    }

    private fun findNodeByTitle(root: DefaultMutableTreeNode, title: String): DefaultMutableTreeNode? {
        val enumeration = root.breadthFirstEnumeration()
        while (enumeration.hasMoreElements()) {
            val node = enumeration.nextElement() as DefaultMutableTreeNode
            if (node.userObject.toString() == title) { // Assuming userObject is the title
                return node
            }
        }
        return null
    }

    private fun setHtmlContent(path: String) {
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("www.")) {
            loadWebpage(path)
        } else {
            if (!FileIO.isExists(path)) {
                JOptionPane.showMessageDialog(mainFrame, "File was not found.")
                return
            }
            loadHtmlContent(FileIO.getAbsoluteFilePath(path))
        }
    }

    private fun loadHtmlContent(path: String) {
        // Create browser loading an HTML string using data URL
        val htmlData = File(path).toURI().toString()
        val dataUrl = "text/html," + URLEncoder.encode(htmlData, StandardCharsets.UTF_8.toString())
        val localUrl = File(path).toURI().toString()
        browser.loadURL(localUrl)

//        Platform.runLater({ webEngine.load(File(path).toURI().toString()) })
    }

    private fun loadWebpage(url: String) {
        browser.loadURL(url)

//        Platform.runLater({ webEngine.load(url) })
    }
}

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }
}

// JCEF Usage examples
// https://github.com/jcefmaven/jcefmaven
// https://github.com/jcefmaven/jcefsampleapp/blob/master/src/main/java/me/friwi/jcefsampleapp/MainFrame.java
// https://www.youtube.com/watch?v=sr3u_bAmNNg