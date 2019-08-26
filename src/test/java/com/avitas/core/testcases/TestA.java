package com.avitas.core.testcases;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.avitas.core.base.BaseTest;
import com.avitas.core.util.*;
import com.relevantcodes.extentreports.LogStatus;

public class TestA extends BaseTest {
	String testCaseName = "TestA";
	SoftAssert softAssert;

	@Test(testName = "testA", dataProvider = "Login")
	public void testB(String username, String password) throws Exception {

		test = rep.startTest("TestA");
		test.log(LogStatus.INFO, "Starting the test test A");

		openBrowser("Chrome");
		navigate("appurl");

		assertPageTitle("Swag Labs", driver);

		// check if User Name field is present
		if (!isElementPresent("username_css"))
			reportFailure("User Name field not present");// critical

		// check if Password field is present
		if (!isElementPresent("password_css"))
			reportFailure("Password field not present");// critical

		softAssert.assertTrue(verifyText("signinbutton_css", "signin_text"), "Text did not match");
		
		// logs in
		signin(username, password);

		addProductToTheCart("Sauce Labs Onesie");
		addProductToTheCart("Sauce Labs Bike Light");

		click("shoppingcart_css");

		assertProductInTheCart("Sauce Labs Onesie");
		assertProductInTheCart("Sauce Labs Bike Light");

		test.log(LogStatus.PASS, "Test A Passed");
		// screenshots
		takeScreenShot();

	}

	@BeforeMethod
	public void init() {
		super.init();
		softAssert = new SoftAssert();
	}

	@AfterMethod
	public void quit() {
		try {
			softAssert.assertAll();
		} catch (Error e) {
			test.log(LogStatus.FAIL, e.getMessage());
		}

		rep.endTest(test);
		rep.flush();
		driver.quit();
	}

	@DataProvider
	public Object[][] Login() throws Exception {
		
		Object[][] testObjArray = ReadData.ReadExcel();

		return (testObjArray);

	}
}
