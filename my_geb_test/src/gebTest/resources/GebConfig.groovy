import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.openqa.selenium.Dimension
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxDriverLogLevel
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.Network

import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL
import static org.testcontainers.shaded.org.apache.commons.io.FileUtils.ONE_GB

// see https://www.testcontainers.org/modules/webdriver_containers/
def webDriverExec = System.getProperty('webdriverExec')
def containerNetwork = System.getProperty('container.network')
def reportsDir = System.getProperty('geb.build.reportsDir')

def videoDir = new File("${reportsDir}/videos")
videoDir.mkdirs()

/*
 Following properties are set in the build gradle via system.properties:
    - baseUrl
    - reportsDir
 */

waiting {
    // max request time in seconds
    timeout = 360

    // http://gebish.org/manual/current/#failure-causes
    includeCauseInMessage = true
}

environments {
    /* Only start the web driver as container. This container is added to the network. */
    firefoxContainer {
        driver = {
            def networkId = findNetworkIdForName(containerNetwork)
            BrowserWebDriverContainer webdriverContainer = addFirefoxContainerToNetwork(networkId)
            webdriverContainer.withRecordingMode(RECORD_ALL, videoDir)
            webdriverContainer.withSharedMemorySize(2 * ONE_GB)
            webdriverContainer.start()
            driver = webdriverContainer.getWebDriver()
            driver
        }
    }

    chromeContainer {
        driver = {
            def networkId = findNetworkIdForName(containerNetwork)
            BrowserWebDriverContainer webdriverContainer = addFirefoxContainerToNetwork(networkId)
            webdriverContainer.withRecordingMode(RECORD_FAILING, videoDir)
            webdriverContainer.withSharedMemorySize(2 * ONE_GB)
            webdriverContainer.start()
            driver = webdriverContainer.getWebDriver()
            driver
        }
    }
    /* Only start the web driver as container. This container is added to the network. */

    chromeTablet {
        driver = {
            def driver = createChromeDriverInstance(webDriverExec)
            driver.manage().window().setSize(new Dimension(1024, 768))
            driver
        }
    }

    chromePC {
        driver = {
            def driver = createChromeDriverInstance(webDriverExec)
            driver.manage().window().setSize(new Dimension(1920, 1200))
            driver
        }
    }

    geckoPC {
        driver = {
            def driver = createGeckoDriverInstance(webDriverExec)
            driver.manage().window().setSize(new Dimension(1920, 1200))
            driver
        }
    }

    geckoTablet {
        driver = {
            def driver = createGeckoDriverInstance(webDriverExec)
            driver.manage().window().setSize(new Dimension(1024, 768))
            driver
        }
    }
}

private def createGeckoDriverInstance(String webDriverExec) {
    System.setProperty("webdriver.gecko.driver", webDriverExec)
    FirefoxOptions options = new FirefoxOptions()
    //options.setHeadless(true)
    options.setLogLevel(FirefoxDriverLogLevel.ERROR)
    driverInstance = new FirefoxDriver(options)
    driverInstance
}

private def createChromeDriverInstance(String webDriverExec) {
    System.setProperty("webdriver.chrome.driver", webDriverExec)
    driverInstance = new ChromeDriver()
    driverInstance
}

private BrowserWebDriverContainer addFirefoxContainerToNetwork(String networkId){
    Network tcNet = createNetwork(networkId)

    FirefoxOptions options = new FirefoxOptions()
    options.setHeadless(true)
    BrowserWebDriverContainer webdriverContainer = new BrowserWebDriverContainer<>()
            .withCapabilities(options)
            .withNetwork(tcNet)

    return webdriverContainer
}

private BrowserWebDriverContainer addChromeContainerToNetwork(String networkId){
    Network tcNet = createNetwork(networkId)

    ChromeOptions options = new ChromeOptions()
    options.setHeadless(true)
    BrowserWebDriverContainer webdriverContainer = new BrowserWebDriverContainer<>()
            .withCapabilities(options)
            .withNetwork(tcNet)

    return webdriverContainer
}

private String findNetworkIdForName(String name) {
    def client = DockerClientFactory.instance().client()
    def rv = client.inspectNetworkCmd().withNetworkId(name).exec().id
    return rv
}

private createNetwork(networkId) {
    Network tcNet = new Network() {
        @Override
        String getId() {
            return networkId
        }

        @Override
        void close() throws Exception {}

        @Override
        Statement apply(Statement base, Description description) {
            return null
        }
    }
    return tcNet
}

