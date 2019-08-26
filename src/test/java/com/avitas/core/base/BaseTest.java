package com.avitas.core.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.avitas.core.util.ExtentManager;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public abstract class BaseTest {
	public WebDriver driver;
	public Properties prop;
	public ExtentReports rep = ExtentManager.getInstance();
	public ExtentTest test;
	// Creating the JavascriptExecutor interface object by Type casting
	JavascriptExecutor js = (JavascriptExecutor) driver;

	public void init() {
		// init the prop file
		if (prop == null) {
			prop = new Properties();
			try {
				FileInputStream fs = new FileInputStream(
						System.getProperty("user.dir") + "//src//test//resources//projectconfig.properties");
				prop.load(fs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void openBrowser(String bType) {

		if (bType.equals("Mozilla"))
			driver = new FirefoxDriver();
		else if (bType.equals("Chrome")) {

			System.setProperty("webdriver.chrome.driver", prop.getProperty("chromedriver_exe"));
			driver = new ChromeDriver();
		} else if (bType.equals("IE")) {
			System.setProperty("webdriver.chrome.driver", prop.getProperty("iedriver_exe"));
			driver = new InternetExplorerDriver();
		}
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		driver.manage().window().maximize();

	}

	public void navigate(String urlKey) {
		driver.get(prop.getProperty(urlKey));
	}

	public void signin(String username, String password) {

		enterUsername(username);
		enterPassword(password);
		clickLoginButton();

	}

	public void enterUsername(String username) {
		waitUntilVisible(prop.getProperty("username_css"));
		type("username_css", username);

	}

	public void enterPassword(String password) {
		type("password_css", password);

	}

	public void clickLoginButton() {
		click("signinbutton_css");
	}

	public void click(String locatorKey) {
		getElement(locatorKey).click();
	}

	public void type(String locatorKey, String data) {
		getElement(locatorKey).sendKeys(data);
	}

	public void waitUntilVisible(String locator) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(locator)));
	}

	public void addProductToTheCart(String productName) {
		String productCart_xpath = prop.getProperty("add_cart_begin_xpath") + productName
				+ prop.getProperty("add_cart_end_xpath");
		clickButton("xpath", productCart_xpath, driver);
	}

	public void assertProductInTheCart(String productName) {
		String productInTheCart_xpath = prop.getProperty("inside_cart_begin_xpath") + productName
				+ prop.getProperty("inside_cart_end_xpath");
		assertElementPresent("xpath", productInTheCart_xpath, driver);

	}

	// finding element and returning it
	public WebElement getElement(String locatorKey) {
		WebElement e = null;
		try {
			if (locatorKey.endsWith("_id"))
				e = driver.findElement(By.id(prop.getProperty(locatorKey)));
			else if (locatorKey.endsWith("_name"))
				e = driver.findElement(By.name(prop.getProperty(locatorKey)));
			else if (locatorKey.endsWith("_xpath"))
				e = driver.findElement(By.xpath(prop.getProperty(locatorKey)));
			else if (locatorKey.endsWith("_css"))
				e = driver.findElement(By.cssSelector(prop.getProperty(locatorKey)));
			else {
				reportFailure("Locator not correct - " + locatorKey);
				Assert.fail("Locator not correct - " + locatorKey);
			}

		} catch (Exception ex) {
			// fail the test and report the error
			reportFailure(ex.getMessage());
			ex.printStackTrace();
			Assert.fail("Failed the test - " + ex.getMessage());
		}
		return e;
	}

	/*********************** Validations ***************************/
	public boolean verifyTitle() {
		return false;
	}

	public boolean isElementPresent(String locatorKey) {
		List<WebElement> elementList = null;
		if (locatorKey.endsWith("_id"))
			elementList = driver.findElements(By.id(prop.getProperty(locatorKey)));
		else if (locatorKey.endsWith("_name"))
			elementList = driver.findElements(By.name(prop.getProperty(locatorKey)));
		else if (locatorKey.endsWith("_xpath"))
			elementList = driver.findElements(By.xpath(prop.getProperty(locatorKey)));
		else if (locatorKey.endsWith("_css"))
			elementList = driver.findElements(By.cssSelector(prop.getProperty(locatorKey)));
		else {
			reportFailure("Locator not correct - " + locatorKey);
			Assert.fail("Locator not correct - " + locatorKey);
		}

		if (elementList.size() == 0)
			return false;
		else
			return true;
	}

	public boolean verifyText(String locatorKey, String expectedTextKey) {
		String actualText = getElement(locatorKey).getText().trim();
		String expectedText = prop.getProperty(expectedTextKey);
		if (actualText.equals(expectedText))
			return true;
		else
			return false;

	}

	/***************************** Reporting ********************************/

	public void reportPass(String msg) {
		test.log(LogStatus.PASS, msg);
	}

	public void reportFailure(String msg) {
		test.log(LogStatus.FAIL, msg);
		takeScreenShot();
		Assert.fail(msg);
	}

	public void takeScreenShot() {
		// fileName of the screenshot
		Date d = new Date();
		String screenshotFile = d.toString().replace(":", "_").replace(" ", "_") + ".png";
		// store screenshot in that file
		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File(System.getProperty("user.dir") + "//screenshots//" + screenshotFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// put screenshot file in reports
		test.log(LogStatus.INFO, "Screenshot-> "
				+ test.addScreenCapture(System.getProperty("user.dir") + "//screenshots//" + screenshotFile));

	}
	// ****************************FUNCTIONS*******************************************

	public static void closeJscriptPopup(Alert alert, WebDriver driver) {
		alert = driver.switchTo().alert();
		alert.accept();
	}

	public boolean checkIsElementPresent(String identifyBy, String locator, WebDriver driver) {
		print("Verify the Element " + identifyBy + "=" + locator + " is  Present or not", "Steps");
		boolean isElemPresent = false;
		if (identifyBy.equalsIgnoreCase("xpath")) {
			isElemPresent = driver.findElement(By.xpath(locator)).isDisplayed();
		} else if (identifyBy.equalsIgnoreCase("id")) {
			isElemPresent = driver.findElement(By.id(locator)).isDisplayed();
		} else if (identifyBy.equalsIgnoreCase("name")) {
			isElemPresent = driver.findElement(By.name(locator)).isDisplayed();
		} else if (identifyBy.equalsIgnoreCase("css")) {
			isElemPresent = driver.findElement(By.cssSelector(locator)).isDisplayed();
		} else if (identifyBy.equalsIgnoreCase("link")) {
			isElemPresent = driver.findElement(By.linkText(locator)).isDisplayed();
		}

		print("Element Verified Successfully", "pass");
		return isElemPresent;
	}

	public void clickButton(String identifyBy, String locator, WebDriver driver) {
		print("Click the Button " + identifyBy + "=" + locator + "", "steps");
		if (identifyBy.equalsIgnoreCase("xpath")) {
			driver.findElement(By.xpath(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("id")) {
			driver.findElement(By.id(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("name")) {
			driver.findElement(By.name(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("css")) {
			driver.findElement(By.cssSelector(locator)).click();
		}
		print("Button clicked successfully", "pass");
	}

	public void clickLink(String identifyBy, String locator, WebDriver driver) {
		print("Click the Link " + identifyBy + "=" + locator + "", "steps");
		if (identifyBy.equalsIgnoreCase("xpath")) {
			driver.findElement(By.xpath(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("id")) {
			driver.findElement(By.id(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("name")) {
			driver.findElement(By.name(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("link")) {
			driver.findElement(By.linkText(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("css")) {
			driver.findElement(By.cssSelector(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("name")) {
			driver.findElement(By.partialLinkText(locator)).click();
		}
		print("Link clicked successfully", "pass");

	}

	public void typeinEditbox(String identifyBy, String locator, String valuetoType, WebDriver driver) {
		print("Enter the value " + valuetoType + " in the text field " + identifyBy + "=" + locator + "", "Steps");
		WebElement element = null;
		if (identifyBy.equalsIgnoreCase("xpath")) {
			element = driver.findElement(By.xpath(locator));
		} else if (identifyBy.equalsIgnoreCase("id")) {
			element = driver.findElement(By.id(locator));
		} else if (identifyBy.equalsIgnoreCase("name")) {
			element = driver.findElement(By.name(locator));
		} else if (identifyBy.equalsIgnoreCase("css")) {
			element = driver.findElement(By.cssSelector(locator));
		}

		if (driver instanceof RemoteWebDriver) {
			// executeScript() is a work-around for Chrome/Ubuntu, sendKeys()
			// causes digits "5" & "6" to be
			// interpreted as Backspace and Carriage Return and data displays
			// incorrectly in the browser.
			((JavascriptExecutor) driver).executeScript("arguments[0].value = '" + valuetoType + "'", element);
		} else {
			element.sendKeys(valuetoType);
		}
		print("Value entered in the textfield", "pass");
	}

	public void selectRadiobutton(String identifyBy, String locator, WebDriver driver) {
		print("Select the radio button " + identifyBy + "=" + locator + "", "steps");
		if (identifyBy.equalsIgnoreCase("xpath")) {
			driver.findElement(By.xpath(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("id")) {
			driver.findElement(By.id(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("name")) {
			driver.findElement(By.name(locator)).click();
		} else if (identifyBy.equalsIgnoreCase("css")) {
			driver.findElement(By.cssSelector(locator)).click();
		}
		print("Radio button selected successfully ", "pass");
	}

	public void selectCheckbox(String identifyBy, String locator, String checkFlag, WebDriver driver) {
		print("Select the checkbox " + identifyBy + "=" + locator + "", "Steps");
		if (identifyBy.equalsIgnoreCase("xpath")) {
			if ((checkFlag).equalsIgnoreCase("ON")) {
				if (!(driver.findElement(By.xpath(locator)).isSelected())) {
					driver.findElement(By.xpath(locator)).click();
				}
			}
		} else if (identifyBy.equalsIgnoreCase("id")) {
			if ((checkFlag).equalsIgnoreCase("ON")) {
				if (!(driver.findElement(By.id(locator)).isSelected())) {
					driver.findElement(By.id(locator)).click();
				}
			}
		} else if (identifyBy.equalsIgnoreCase("name")) {
			if ((checkFlag).equalsIgnoreCase("ON")) {
				if (!(driver.findElement(By.name(locator)).isSelected())) {
					driver.findElement(By.name(locator)).click();
				}
			}
		} else if (identifyBy.equalsIgnoreCase("css")) {
			if ((checkFlag).equalsIgnoreCase("ON")) {
				if (!(driver.findElement(By.cssSelector(locator)).isSelected())) {
					driver.findElement(By.cssSelector(locator)).click();
				}
			}
		}

		print("Checkbox selected successfully ", "pass");
	}

	public void selectValue(String valToBeSelected, WebDriver driver) {
		print("Select the value " + valToBeSelected + "", "Steps");
		List<WebElement> options = driver.findElements(By.tagName("option"));
		for (WebElement option : options) {
			if (valToBeSelected.equalsIgnoreCase(option.getText())) {
				option.click();
				break;
			}
		}

		print("Value selected successfully ", "pass");
	}

	public void selectbyindex(String identifyBy, String locator, int index, WebDriver driver) {

		print("Select the Index " + index + " from the dropdown " + identifyBy + "=" + locator + "", "Steps");
		WebElement element = null;
		if (identifyBy.equalsIgnoreCase("xpath")) {
			element = driver.findElement(By.xpath(locator));
		} else if (identifyBy.equalsIgnoreCase("id")) {
			element = driver.findElement(By.id(locator));
		} else if (identifyBy.equalsIgnoreCase("name")) {
			element = driver.findElement(By.name(locator));
		} else if (identifyBy.equalsIgnoreCase("css")) {
			element = driver.findElement(By.cssSelector(locator));
		}

		Select sel = new Select(element);
		sel.selectByIndex(index);
		print("Index selected successfully ", "pass");
	}

	public String getMyText(String identifyBy, String locator, WebDriver driver) {
		print("Get the text from " + identifyBy + "=" + locator + "", "steps");
		if (identifyBy.equalsIgnoreCase("xpath")) {
			return driver.findElement(By.xpath(locator)).getText();
		} else if (identifyBy.equalsIgnoreCase("id")) {
			return driver.findElement(By.id(locator)).getText();
		} else if (identifyBy.equalsIgnoreCase("name")) {
			return driver.findElement(By.name(locator)).getText();
		} else if (identifyBy.equalsIgnoreCase("link")) {
			return driver.findElement(By.linkText(locator)).getText();
		} else if (identifyBy.equalsIgnoreCase("css")) {
			return driver.findElement(By.cssSelector(locator)).getText();
		} else if (identifyBy.equalsIgnoreCase("name")) {
			return driver.findElement(By.partialLinkText(locator)).getText();
		} else
			return null;

	}

	public String getMyAttributeValue(String identifyBy, String locator, String tagName, WebDriver driver) {
		print("Get the attribute value for the tag " + tagName + " from the element " + identifyBy + "=" + locator + "",
				"steps");
		if (identifyBy.equalsIgnoreCase("css")) {
			return driver.findElement(By.cssSelector(locator)).getAttribute(tagName);
		} else {
			print("Problem in getting attribute value", "Fail");
			return null;
		}
	}

	public Boolean isTextDisplayed(String identifyBy, String locator, WebDriver driver) {
		print("verify the text displayed in " + identifyBy + "=" + locator + "", "steps");
		if (identifyBy.equalsIgnoreCase("xpath")) {
			return driver.findElement(By.xpath(locator)).isDisplayed();
		} else if (identifyBy.equalsIgnoreCase("id")) {
			return driver.findElement(By.id(locator)).isDisplayed();
		} else if (identifyBy.equalsIgnoreCase("name")) {
			return driver.findElement(By.name(locator)).isDisplayed();
		} else if (identifyBy.equalsIgnoreCase("link")) {
			return driver.findElement(By.linkText(locator)).isDisplayed();
		} else if (identifyBy.equalsIgnoreCase("css")) {
			return driver.findElement(By.cssSelector(locator)).isDisplayed();
		} else if (identifyBy.equalsIgnoreCase("name")) {
			return driver.findElement(By.partialLinkText(locator)).isDisplayed();
		} else {
			print("Text is not displaying", "fail");
			return false;
		}
	}

	public void assertElementPresent(String identifyBy, String locator, WebDriver driver) {
		print("verify the Element Present " + identifyBy + "=" + locator + "", "steps");
		if (identifyBy.equalsIgnoreCase("xpath")) {
			Assert.assertTrue(driver.findElement(By.xpath(locator)).isDisplayed(), locator + " is not found");
		} else if (identifyBy.equalsIgnoreCase("id")) {
			Assert.assertTrue(driver.findElement(By.id(locator)).isDisplayed(), locator + " is not found");
		} else if (identifyBy.equalsIgnoreCase("name")) {
			Assert.assertTrue(driver.findElement(By.name(locator)).isDisplayed(), locator + " is not found");
		} else if (identifyBy.equalsIgnoreCase("css")) {
			Assert.assertTrue(driver.findElement(By.cssSelector(locator)).isDisplayed(), locator + " is not found");
		} else if (identifyBy.equalsIgnoreCase("link")) {
			Assert.assertTrue(driver.findElement(By.linkText(locator)).isDisplayed(), locator + " is not found");
		}
		print("Element Verified Successfully", "Pass");
	}

	public void assertElementNotPresent(String identifyBy, String locator, WebDriver driver) throws Exception {
		print("verify the Element Not Present " + identifyBy + "=" + locator + "", "steps");
		if (identifyBy.equalsIgnoreCase("xpath")) {

			List<WebElement> elementlist = driver.findElements(By.xpath(locator));

			if (elementlist.isEmpty() == false) {
				throw new Exception("Element is Present");
			}
			print("Element is not present", "pass");
		} else if (identifyBy.equalsIgnoreCase("id")) {

			List<WebElement> elementlist = driver.findElements(By.id(locator));
			if (elementlist.isEmpty() == false) {
				throw new Exception("Element is Present");
			}
			print("Element is not present", "pass");
		} else if (identifyBy.equalsIgnoreCase("name")) {
			List<WebElement> elementlist = driver.findElements(By.name(locator));
			if (elementlist.isEmpty() == false) {
				throw new Exception("Element is Present");
			}
			print("Element is not present", "pass");
		} else if (identifyBy.equalsIgnoreCase("css")) {

			List<WebElement> elementlist = driver.findElements(By.cssSelector(locator));
			if (elementlist.isEmpty() == false) {
				throw new Exception("Element is Present");
			}
			print("Element is not present", "pass");
		} else if (identifyBy.equalsIgnoreCase("link")) {

			List<WebElement> elementlist = driver.findElements(By.linkText(locator));
			if (elementlist.isEmpty() == false) {
				throw new Exception("Element is Present");
			}
			print("Element is not present", "pass");
		}

	}

	public void assertPageTitle(String text, WebDriver driver) {
		print("Verify the Page title. Title is " + text, "Steps");
		Assert.assertTrue(driver.getTitle().equalsIgnoreCase(text), text + " is not matched");
		print("Page Title verified successfully", "pass");
	}

	public void print(String msg, String status) {

		{
			if (status.equalsIgnoreCase("Steps"))
				test.log(LogStatus.INFO, msg);
			else if (status.equalsIgnoreCase("Pass"))
				test.log(LogStatus.PASS, msg);
			else if (status.equalsIgnoreCase("fail"))
				test.log(LogStatus.FAIL, msg);
		}
	}

}
