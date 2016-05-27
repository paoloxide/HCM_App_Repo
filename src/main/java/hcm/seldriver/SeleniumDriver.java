package hcm.seldriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.text.NumberFormat.Field;
import java.util.Date;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.thoughtworks.selenium.Wait;

import hcm.common.InputErrorException;
import hcm.common.ReporterManager;
import hcm.common.ExtendedFirefoxDriver;
import hcm.common.ArgumentExecutor;
import hcm.common.ArgumentHandler;
import hcm.common.CustomRunnable;
import hcm.common.DuplicateEntryException;
import hcm.common.InputErrorHandler;
import hcm.common.TaskUtilities;
import hcm.utilities.ExcelReader;
import hcm.utilities.TextUtility;

public class SeleniumDriver {

	private static final long ELEMENT_APPEAR = 60L;
	private static final int MAX_TIMEOUT = 60;
	public static final int navRow = 1;
	public static final int defaultLabelRow = 2;
	public static int rowGroup = defaultLabelRow - 1;// 2 : Oracle 1 : Exelon
	public static final int defaultInputRow = defaultLabelRow + 1;
	public static final int defaulColNum = 0;
	private static String sumMsg = "";
	private static String errMsg = "";
	private WebDriverWait wait = null;
	private TextUtility textReader = null;
	private ExcelReader excelReader = null;
	public static WebDriver driver;
	private static WebDriver augmentedDriver;
	private String caseName;
	private String workspace_path;
	private String excel_path;
	// Case-dependent variables
	private int afrrkInt = 0;
	// Dependency files
	//private String excelPath = "hcm-configurations_bak.xlsx";
	private String excelPath = "HCM_Core-Configurations.xlsx";
	//private String configPath = "lib/config_file_hcm_set4.txt"; //config_file_hcm_set4
	private String configPath = "lib/config_file.txt;
	private String screenShotPath = "target/screenshots/";
	
	public void initializeDriver(String hubURL, String browser,	String workspace, String excel) throws Exception{
		try {
			workspace_path = workspace + "/";
			excel_path = excel + "/";
			driver = new RemoteWebDriver(new URL(hubURL),getCapability(browser));
			// driver = new ExtendedFirefoxDriver(getCapability(browser));
			// driver.manage().window().maximize();

			// Set window size base on the remote server
			driver.manage().window().setSize(new Dimension(1020, 737));
			System.out.println("Browser size: "	+ driver.manage().window().getSize());
			augmentedDriver = new Augmenter().augment(driver);

			// Read text file
			textReader = new TextUtility();
			textReader.read(workspace_path + configPath);

			// Read Excel file
			excelReader = new ExcelReader();
			System.out.println(excel_path + excelPath);
			excelReader.loadExcelFile(excel_path + excelPath);

			wait = new WebDriverWait(driver, ELEMENT_APPEAR);

		} catch (Exception e) {
			//e.printStackTrace();
			dispose();
			throw e;
		}
	}

	public void validateArgument(String sr, String workspace, String excel)throws InputErrorException{
		getSheetName(sr);
		Vector<String> fields = textReader.getCollection(sr, "Fields");
		
		try{
			if(fields.size() < 1) 
				throw new InputErrorException("The Fields sections cannot be "
					+ "empty.\n\tVerify in the "+workspace
					+ configPath.substring(4)+"\n\tif ["+sr+"] really exists.\n");
			System.out.print(".");
			if(excelReader.getCellData(defaultInputRow, defaulColNum).isEmpty()) 
				throw new InputErrorException("Target data in the excel "
					+ "cannot be empty.\n\tPlease check in "+excel+excelPath+"\n\t"
					+ "if sheet: \""+getSheetName(sr)+"\" really exists.");
			System.out.print(".");
			System.out.println(".VALID");
		} catch(InputErrorException ie){
			dispose();
			throw ie;
		}
	}

	protected DesiredCapabilities getCapability(String browser) {
		try {
			if (browser.contentEquals("firefox"))
				return DesiredCapabilities.firefox();
			else
				return DesiredCapabilities.firefox();

		} catch (Exception e) {

		}
		return null;
	}

	public String getLoginCredentials(String input) {
		excelReader.setActiveSheet("Configurations");
		int rowNum = 0;
		int colNum = 0;
		String data = null;

		if (input.contentEquals("URL")) {
			rowNum = 0;
			colNum = 1;
		} else if (input.contentEquals("USERID")) {
			rowNum = 1;
			colNum = 1;
		} else if (input.contentEquals("PASSWORD")) {
			rowNum = 2;
			colNum = 1;
		}
		data = excelReader.getCellData(rowNum, colNum);

		return data;
	}

	public void login(String siteURL, String username, String password) {
		driver.get(siteURL);
		System.out.println(siteURL);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("userid")));
		System.out.println("Loading URL..");
		// takeScreenShot(caseName);
		driver.findElement(By.id("userid")).clear();
		System.out.println("Waiting for User Id...");
		driver.findElement(By.id("userid")).sendKeys(username);
		System.out.println("User Id " + username + " entered.");

		driver.findElement(By.name("password")).clear();
		System.out.println("Waiting for Password...");
		driver.findElement(By.name("password")).sendKeys(password);
		System.out.println("Password ******* entered.");

		driver.findElement(By.xpath("//tbody/tr[4]/td/button")).click();
		System.out.println("Logging in...");
	}

	public void setupAndMaintenance() throws Exception{
		System.out.println("Loading page..");
		//TaskUtilities.customWaitForElementVisibility("xpath", "//img[@alt='Navigator]", 120);
		//TaskUtilities.customWaitForElementVisibility("xpath", "//a[text()='Setup and Maintenance']", 120);
		try{
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='Navigator']"))).click();
			// driver.findElement(By.xpath("//img[@alt='Navigator']")).click();
			System.out.println("Navigating to Setup and Maintenance...");
			//wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Setup and Maintenance"))).click();
			TaskUtilities.jsFindThenClick("xpath", "//a[text()='Setup and Maintenance']");
		} catch(WebDriverException we){
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='Navigator']"))).click();
			wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Setup and Maintenance"))).click();
		}
		// driver.findElement(By.linkText("Setup and Maintenance")).click();
	}

	public void searchTask(String key) throws Exception{
		// go to Setup and Maintenance
		// driver.get("https://fs-aufsn4x0cba.oracleoutsourcing.com/setup/faces/TaskListManagerTop?fnd=%3B%3B%3B%3Bfalse%3B256%3B%3B%3B&_adf.no-new-window-redirect=true&_adf.ctrl-state=s6qt8bhoo_5&_afrLoop=1042465641683653&_afrWindowMode=2&_afrWindowId=1zd8d2b3t");
		try{
			TaskUtilities.customWaitForElementVisibility("xpath", "//span/label[text()='Search']/../input", MAX_TIMEOUT);
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span/label[text()='Search']/../input")));
			// Search
			driver.findElement(By.xpath("//span/label[text()='Search']/../input")).clear();
			driver.findElement(By.xpath("//span/label[text()='Search']/../input")).sendKeys(key);
			driver.findElement(By.xpath("//span/label[text()='Search']/../input")).sendKeys(Keys.ENTER);
		} catch(Exception e){
			TaskUtilities.customWaitForElementVisibility("xpath", "//span/label[text()='Search']/../input", MAX_TIMEOUT);
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span/label[text()='Search']/../input")));
			driver.findElement(By.xpath("//span/label[text()='Search']/../input")).clear();
			driver.findElement(By.xpath("//span/label[text()='Search']/../input")).sendKeys(key);
			driver.findElement(By.xpath("//span/label[text()='Search']/../input")).sendKeys(Keys.ENTER);
		}
		// takeScreenShot(caseName); 
		System.out.println("Searching for task " + key + ".");
	}

	public void goToTask(String key) {
		//wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='" + key+ "']/../../../td/a/img[@title='Go to Task']"))).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[text()='" + key+ "']"))).click();
		driver.findElement(By.xpath("//a[text()='" + key+ "']")).sendKeys(Keys.ENTER);
		System.out.println("Go to task " + key + ".");
	}

	public void navigateToTask(String name) throws Exception {
		System.out.println("Task navigation started...");
		String divPath;
		int intLoc = 1;
		String currentLoc = "", searchData = "", labelLocator = "", labelLocatorPath = "";

		getSheetName(name);
		String fullNavPath = excelReader.getCellData(navRow, defaulColNum);
		System.out.println("Nav Path is..." + fullNavPath);
		String[] navStep = fullNavPath.split(" > ");

		// Span Manage Implementation Project -- > Implementation Projects
		// TaskUtilities.customWaitForElementVisibility("xpath", "//a[text()='"+ navStep[intLoc] + "']", MAX_TIMEOUT);
		// TaskUtilities.jsFindThenClick("xpath", "//a[text()='" + navStep[intLoc] + "']");
		TaskUtilities.customWaitForElementVisibility("xpath", "//span[text()='"+ navStep[intLoc] + "']/..", MAX_TIMEOUT);
		Thread.sleep(3500);
		TaskUtilities.jsFindThenClick("xpath", "//span[text()='"+ navStep[intLoc] + "']/..");
		TaskUtilities.customWaitForElementVisibility("xpath", "//h1[text()='"+ navStep[intLoc] + "']", MAX_TIMEOUT);
		// Setting project name...
		if (!name.contentEquals("Manage Implementation Project")) {
			excelReader.setActiveSheet("Create Implementation Project");
			String projectName = excelReader.getCellData(defaultInputRow,defaulColNum);
			System.out.println("Project Name is " + projectName);

			searchData = projectName;
			labelLocator = "Name";
			labelLocatorPath = TaskUtilities.retryingSearchfromDupInput(labelLocator, "");

			action(labelLocator, "textbox", "xpath", labelLocatorPath,searchData);
			TaskUtilities.jsFindThenClick("xpath", "//button[text()='Search']");
			Thread.sleep(3500);
			TaskUtilities.customWaitForElementVisibility("xpath","//a[text()='" + searchData + "']", MAX_TIMEOUT);
			TaskUtilities.jsFindThenClick("xpath", "//a[text()='" + searchData+ "']");

			TaskUtilities.customWaitForElementVisibility("xpath","//h1[contains(text(),'" + searchData + "')]", MAX_TIMEOUT);
		}

		intLoc += 1;
		TaskUtilities.scrollDownToElement(false, "");
		while (intLoc < navStep.length) {
			currentLoc = navStep[intLoc].replace("HCM","Human Capital Management");
			System.out.println("We are now at: " + currentLoc);
			divPath = "//div[text()='" + currentLoc + "']";

			TaskUtilities.customWaitForElementVisibility("xpath", divPath, MAX_TIMEOUT);
			TaskUtilities.jsScrollIntoView("xpath", divPath);

			if (TaskUtilities.is_element_visible("xpath", divPath+ "//a[@title='Expand']")) {
				// TaskUtilities.retryingFindClick(By.xpath(divPath+
				// "//a[@title='Expand']"));
				driver.findElement(By.xpath(divPath + "//a[@title='Expand']")).sendKeys(Keys.ENTER);
				TaskUtilities.customWaitForElementVisibility("xpath", divPath + "//a[@title='Collapse']", MAX_TIMEOUT);
			}

			if (TaskUtilities.is_element_visible("xpath", divPath + "/../..//a[@title='Go to Task']")) {
				TaskUtilities.jsFindThenClick("xpath", divPath);

				// Open for improvement...
				String href = driver.findElement(By.xpath(divPath + "/../..//a[@title='Go to Task']")).getAttribute("href");
				System.out.println("Obtained href is: " + href);
				String isRedirecting = getPropertiesRedirect(name);

				// if (href.contentEquals("#")) {
				if (isRedirecting.toLowerCase().contentEquals("false")) {
					TaskUtilities.jsFindThenClick("xpath", divPath + "/../..//a[@title='Go to Task']");
					// } else if (href.contains("http")) {
				} else if (isRedirecting.toLowerCase().contentEquals("true")) {
					openAdminLink(href);
				}
			}

			intLoc += 1;
		}
	}

	public void openAdminLink(String href) throws Exception {

		WebElement body = driver.findElement(By.cssSelector("body"));
		// String newTabAction = Keys.chord(Keys.COMMAND, "t");
		String newTabAction = Keys.chord(Keys.CONTROL, "t");
		body.sendKeys(newTabAction);

		String chooseTab = Keys.chord(Keys.COMMAND, "2");// on my pc 3 others should be 2;
		// String switchTab = Keys.chord(Keys.CONTROL, Keys.TAB);
		body.sendKeys(chooseTab);

		driver.get(href);
	}

	public void search(String data, String locatorType, String locator) {
		driver.findElement(getLocator(locatorType, locator)).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(),'Search Results')]/../../../../../../..//tbody/tr/td[text()='" + data + "']"))).click();
	}

	public Map<String,String> runServiceRequest(String sr, Map<String, String> srMap) throws Exception {
		caseName = sr;
		
		//Verifying All Data
		//showConfigurations(sr);
		
		// Detect the Action to perform
		try{
			if (getPropertiesAction(sr).contentEquals("Search Task")) {
				searchTask(sr);

			} else if (getPropertiesAction(sr).contentEquals("Navigate")) {
				
			} /*else if (getPropertiesAction(sr).contentEquals(null)) {
				System.out.print("No Action indicated in Properties. Please set the Action in the customization.txt");
				System.out.print("Terminating transaction..");
				return srMap;
			} else {
				System.out.print(" Action not available. Please set Search Task or Navigate only");
				System.out.print("Terminating transaction..");
				return srMap;
			}*/
			
		} catch(NullPointerException e){
			throw new InputErrorException("No Action indicated in Properties. "
					+ "\n\tPlease set the Action in the "+configPath.substring(4));
		}
		
		srMap.put("sumMsg", srMap.get("sumMsg")+
				"\n====================== R E P O R T   S U M M A R Y =====================\n");
		srMap = runServiceRequestSearchTask(sr, srMap);
		srMap.put("sumMsg", srMap.get("sumMsg")+
				"====================== E N D   O F   R E P O R T =======================\n");	
		// Screenshot
		takeScreenShot(sr);
		return srMap;
	}

	public Map<String,String> runServiceRequestSearchTask(String sr, Map<String,String> srMap) throws Exception {

		getSheetName(sr);
		int rowNum = 0;
		boolean hasRunGoToTask = false, hasPrepared = false, hasCheckedFields= false,
				hasArray = false, isAnArrayAction = false, isNonIdenticalArray = false;

		if (getPropertiesAction(sr).contentEquals("Search Task"))
			rowNum = defaultInputRow;
		if (getPropertiesAction(sr).contentEquals("Navigate"))
			rowNum = defaultInputRow;
		// int pivotRow = rowNum;
		int pivotIndex = 1;
		System.out.println("First Excel Entry: " + excelReader.getCellData(rowNum, 0));
		
		readerloop: while (!excelReader.getCellData(rowNum, 0).isEmpty()) {
			//Preparing all properties....
			if(getPropertiesSMReturnee(sr).toLowerCase().contentEquals("true")){
				setupAndMaintenance();
				searchTask(sr);
			}
			if (getPropertiesRecursive(sr).toLowerCase().contentEquals("true")) {
				if (getPropertiesAction(sr).contentEquals("Search Task")) {
					goToTask(sr);
				} else if (getPropertiesAction(sr).contentEquals("Navigate")) {
					navigateToTask(sr);
					getSheetName(sr);
				}
			} else {
				if (getPropertiesAction(sr).contentEquals("Search Task") && !hasRunGoToTask) {
					goToTask(sr);
					hasRunGoToTask = true;
				} else if (getPropertiesAction(sr).contentEquals("Navigate") && !hasRunGoToTask) {
					navigateToTask(sr);
					getSheetName(sr);
					hasRunGoToTask = true;
				}
			}
			if (!hasPrepared) {
				runPrePrep(sr);
				hasPrepared = true;
			}
			
			String dataName = getPropertiesDataName(sr, rowNum);
			String dataType = sr;
			if(dataType.startsWith("Manage "))
				dataType = dataType.replace("Manage ", "");
			if(dataType.endsWith("ies"))
				dataType = dataType.replace("ies", "y");
			takeScreenShot(caseName);

			runSteps(sr, "Pre-Steps", rowNum);

			Vector<String> fields = textReader.getCollection(sr, "Fields");
			Map<String, String> savedEntry = new HashMap<String, String>();
			Map<String, String> fieldVariables = new HashMap<String, String>();

			int fieldSize = fields.size();
			int colNum = 0;
			// int rowGroup = defaultLabelRow-1;
			int iteration = 0;
			int checkpoint = 0;
			int rowInputs = 0, nextPivotIndex = 0;
			String arrayCol = null;
			int arrayRow = 0;
			System.out.println("Field Size: " + fieldSize);
			System.out.println("Pivot Index: " + pivotIndex);
			
			//Array verifier...
			System.out.print("Array Detection: ");
			if(!hasCheckedFields){
				for(String statement : fields){
					if(statement.startsWith("array")){
						hasArray = true;
					}
				}
				hasCheckedFields = true;
			}
			System.out.println((""+hasArray).toUpperCase());
			
			fieldloop: while (iteration < fieldSize) {
				System.out.println("Now Processing Field Line: "+ iteration);
				String current = fields.elementAt(iteration);
				String[] step = current.split(" \\| ");
				int sleepTime = 0;
				String caseId = "";
				boolean isFound = false, waitImmunity = false;
				// boolean array = false;

				if (current.startsWith("//")){
					iteration += 1;
					continue fieldloop;
				}
				// Case Handler
				if (current.contains("esac")) {
					iteration += 1;
					continue fieldloop;
				}
				if (current.startsWith("case:")) {
					while (!current.contains("esac")) {
						iteration += 1;
						current = fields.elementAt(iteration);
						if ((iteration + 1) < fieldSize && fields.elementAt(iteration + 1).startsWith("case:")) {
							iteration += 1;
							current = fields.elementAt(iteration);
						}
					}
					iteration += 1;
					continue fieldloop;
				}
				// Case Handler

				//array handler
				if (step[0].contains("array") && !step[0].contains("col")) {
					arrayCol = step[1];
					arrayRow = rowNum;
					isAnArrayAction = true;
					if(step[0].contains("non-identical")) isNonIdenticalArray = true;
					checkpoint = iteration + 1;
					iteration++;
					continue fieldloop;
				} else if (step[0].contains("stop") && !step[0].contains("col")) {
					int nextRow = rowNum + 1;

					if (excelReader.getCellData(nextRow, Integer.valueOf(arrayCol)).length()>0){
						if (excelReader.getCellData(rowNum, 0).contentEquals(excelReader.getCellData(nextRow, 0))) {
							rowNum++;
							colNum = Integer.valueOf(arrayCol);
							rowInputs += 1; //dynamic movement of row after the array..
							iteration = checkpoint;
							continue fieldloop;
						}
						if (excelReader.getCellData(nextRow, 0).length() > 0 && step[0].contains("non-identical")) {
							rowNum++;
							colNum = Integer.valueOf(arrayCol);
							rowInputs += 1; //dynamic movement of row after the array..
							iteration = checkpoint;
							continue fieldloop;
						}
					}

					//Refresh data...
					if(nextPivotIndex < rowInputs) nextPivotIndex = rowInputs;
					rowInputs = 0;
					isAnArrayAction = false;
					isNonIdenticalArray = false;
					rowNum = arrayRow; //Reverts the row back..
					iteration++;
					continue fieldloop;
				}
				//array handler: Column version
				if (step[0].contains("colArray")) {
					//arrayRow = step[1];
					isAnArrayAction = true;
					checkpoint = iteration + 1;
					iteration++;
					continue fieldloop;
				} else if (step[0].contains("colStop")) {
					int nextCol = colNum+1;

					if ((excelReader.getCellData(rowNum, nextCol)).length()>0) {
						colNum++;
						//rowNum = Integer.valueOf(arrayRow);
						iteration = checkpoint;
						continue fieldloop;
					}

					isAnArrayAction = false;
					iteration += 1;
					continue fieldloop;
				}
				
				// Action Reader...
				if (step[0].contains("setExcelRow:")) {
					colNum = 0;
					Map<String, String> cellProp = ArgumentExecutor.executeSetExcelRow(current, excelReader, pivotIndex);
					rowNum = Integer.parseInt(cellProp.get("rowNum"));
					rowGroup = Integer.parseInt(cellProp.get("rowGroup"));
					iteration += 1;
					continue fieldloop;
				}
				if (step[0].contains("setExcelCol:")) {
					colNum = ArgumentExecutor.executeSetExcelColumn(current, excelReader, rowGroup);
					iteration += 1;
					continue fieldloop;
				}
				if (step[0].contains("takeScreenshot:")) {
					String sName = ArgumentExecutor.executeTakeScreenshot(step[0], caseName);
					takeScreenShot(sName);
					iteration += 1;
					continue fieldloop;
				}
				// Table Inputs...
				if (step[3].contains("$afrrkInt")) {
					System.out.print("Formerly " + step[3]);
					step[3] = step[3].replace("$afrrkInt", "" + afrrkInt);
					System.out.println(" is now: " + step[3]);
				}

				// Data conditions...
				String data = "";
				if (step[0].toLowerCase().contains("time") && !step[0].toLowerCase().contains("zone")
						&& !step[0].toLowerCase().contains("overtime")) {
					data = excelReader.getCellData(rowNum, colNum, "time");
				} else if (step.length > 4 && step[4].toLowerCase().contains("parse number")) {
					data = ArgumentExecutor.executeCellParser(step[4], excelReader, rowNum, colNum, data);
				} else {
					data = excelReader.getCellData(rowNum, colNum);
				}

				if (step[0].toUpperCase().contains("WAIT TO APPEAR")) {
					System.out.println("Waiting for " + step[1]+ " to appear...");
					wait.until(ExpectedConditions.visibilityOfElementLocated(getLocator(step[2],step[3])));
					colNum--;
				} else if (step[0].toUpperCase().contains("WAIT TO DISAPPEAR")) {
					System.out.println("Waiting for " + step[1]+ " to dissappear...");
					wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(step[2],step[3])));
					colNum--;
				} else if(step[0].toUpperCase().contains("WAIT UNTIL CLICKABLE")){
					System.out.println("Waiting for " + step[1]+ " to be clickable...");
					wait.until(ExpectedConditions.elementToBeClickable((getLocator(step[2],step[3]))));
					colNum--;
				} else if (step[0].toUpperCase().contains("DYNAMIC")) {

					colNum--;
				} else {

					if ((data.isEmpty() || data.contains("blank"))&& !step[1].contains("button") 
							&& !step[1].contains("skippable") && !current.contains("trigger:")) {
						System.out.print(step[0] + " is empty.\n");
					} else {
						current = ArgumentHandler.executeArgumentConverter(current, excelReader, rowNum, rowGroup, colNum);
						step = current.split(" \\| ");

						try {

							if (step.length > 4) {// Fourth step handler...

								Map<String, String> retrievingMap = ArgumentHandler.executeFourthArgument(current, excelReader, rowNum, rowGroup,colNum, data);
								sleepTime = Integer.parseInt(retrievingMap.get("sleepTime"));
								waitImmunity = Boolean.parseBoolean(retrievingMap.get("waitImmunity"));
								data = retrievingMap.get("data");
								isFound = Boolean.parseBoolean(retrievingMap.get("isFound"));
								caseId = retrievingMap.get("caseId");
								iteration += Integer.parseInt(retrievingMap.get("iteration"));
								colNum = Integer.parseInt(retrievingMap.get("colNum"));
								// Case Handler...
								if (!caseId.isEmpty() && !caseId.contentEquals("")) {
									System.out.println("Case Scenario: "+ caseId);
									String caseHolder = ArgumentExecutor.getCaseStatement(fields, caseId);
									if (!step[1].contains("button"))
										colNum += 1;
									if (!waitImmunity) action(step[0], step[1], step[2],step[3], data);
									//while (!current.contentEquals("case: "+ caseId)) {
									while(!current.contentEquals(caseHolder)){
										iteration += 1;
										current = fields.elementAt(iteration);
									}
									iteration += 1;
									continue fieldloop;
								}
								if (waitImmunity && step[4].contains(":") && step[4].contains("wait")) {// Recent change...
									iteration += 1;
									continue fieldloop;
								}
								if (!retrievingMap.get("savedEntry").isEmpty()) {
									String[] entry = retrievingMap.get("savedEntry").split(",");
									savedEntry.put(entry[0], entry[1]);
								}
								if (retrievingMap.get("endingArg").contains("continue")) {
									if (retrievingMap.get("endingArg").contains("fieldloop")) {
										continue fieldloop;
									}
								}
							}

							if (isFound) {
								throw new TimeoutException();
							} else {
								TaskUtilities.jsCheckMessageContainer();
							}

							Thread.sleep(sleepTime);
							action(step[0], step[1], step[2], step[3], data);
						} /*catch (UnhandledAlertException ue) {
							System.out.println("Error has class: "+ue.getClass());
							driver.findElement(By.cssSelector("body")).sendKeys(Keys.ENTER);
							//if(step[4].contains("throws")) throw 
							iteration = 0;
							colNum = 0;
							//iteration -= 1;
							continue fieldloop;
						} */catch (Exception e) {
							e.printStackTrace();
							boolean isUAEHandled = false;
							while(!isUAEHandled){
								try{
									uaeif: if(e.getClass().toString().contains("UnhandledAlertException")){
										driver.findElement(By.cssSelector("body")).sendKeys(Keys.ENTER);
										if(step.length > 4 && step[4].contains("throws")){
											break uaeif;
										}
										iteration = 0;
										colNum = 0;
										continue fieldloop;
									}
									isUAEHandled = true;
									
								} catch(Exception e2){
									//Ignores error and try to 
								}
								
							}
							takeScreenShot(caseName);
							String errKey = InputErrorHandler.identifyInputErrors(textReader,excelReader, sr, rowNum);
							String caseType = ":Undo-Steps";
							fieldVariables.put("isAnArrayAction", ""+isAnArrayAction);
							fieldVariables.put("sr", sr);
							fieldVariables.put("caseType", caseType);
							fieldVariables.put("rowNum", ""+rowNum);
							fieldVariables.put("rowInputs",""+rowInputs);
							fieldVariables.put("nextPivotIndex", ""+nextPivotIndex);
							fieldVariables.put("hasArray", ""+hasArray);
							fieldVariables.put("isNonIdenticalArray", ""+isNonIdenticalArray);
							System.out.println("Input has errors on: " + errKey);

							if (step.length > 4) {
								if (step[4].contains("throws")) {//Recently changed...
									String caseItem = ArgumentExecutor.executeThrower(e, step[4]);
									Map<String, String> exceptionVariables = runUndoCaseSteps(fieldVariables, caseItem, savedEntry.get(caseItem), rowNum);
									boolean isResuming = Boolean.parseBoolean(exceptionVariables.get("isResuming"));
									int trueNextPivotIndex = Integer.parseInt(exceptionVariables.get("nextPivotIndex"));
									nextPivotIndex = 0;
									if (!isResuming) {
										//rowNum += 1 + nextPivotIndex;
										rowNum = Integer.parseInt(exceptionVariables.get("rowNum")) + 1;
										pivotIndex += 1 + trueNextPivotIndex;
										sumMsg = ReporterManager.processFailedSR(excelReader, e, sumMsg, errMsg, dataType, dataName, rowNum);
										continue readerloop;
									} else {
										colNum += 1;
										iteration += 1;
										continue fieldloop;
									}
								}
							}

							for (String key : savedEntry.keySet()) {
								String newKey = key.toLowerCase().replaceAll("\\*", "");
								System.out.println("newKey is now " + newKey);
								// if((""+e).toLowerCase().contains(newKey)){
								if (errKey.toLowerCase().contentEquals(newKey)) {
									System.out.println("Exception Found.. running case: "+ key);
									Map<String, String> exceptionVariables = runUndoCaseSteps(fieldVariables, key, savedEntry.get(key), rowNum);
									boolean isResuming = Boolean.parseBoolean(exceptionVariables.get("isResuming"));
									int trueNextPivotIndex = Integer.parseInt(exceptionVariables.get("nextPivotIndex"));
									nextPivotIndex = 0;
									if (!isResuming) {
										//rowNum += 1 + nextPivotIndex;
										rowNum = Integer.parseInt(exceptionVariables.get("rowNum")) + 1;
										pivotIndex += 1 + trueNextPivotIndex;
										sumMsg = ReporterManager.processFailedSR(excelReader, e, sumMsg, errMsg, dataType, dataName, rowNum);
										continue readerloop;
									} else {
										colNum += 1;
										iteration += 1;
										continue fieldloop;
									}
								}
							}

							System.out.println("No Available Error Handler found: Enforcing default Undo-Steps...");
							takeScreenShot(caseName);
							runUndoSteps(sr);
							int trueNextPivotIndex = nextPivotIndex;
							Map<String, String> FailedArrayAttr = ArgumentHandler.executeFailedArrayRow(excelReader, hasArray, 
									isNonIdenticalArray, rowInputs, rowNum, nextPivotIndex, trueNextPivotIndex);
							rowNum = Integer.parseInt(FailedArrayAttr.get("rowNum"));
							nextPivotIndex = Integer.parseInt(FailedArrayAttr.get("nextPivotIndex"));
							trueNextPivotIndex = Integer.parseInt(FailedArrayAttr.get("trueNextPivotIndex"));
							
							rowNum += 1 + nextPivotIndex;
							pivotIndex += 1 + trueNextPivotIndex;
							sumMsg = ReporterManager.processFailedSR(excelReader, e, sumMsg, errMsg, dataType, dataName, rowNum);
							continue readerloop;
						}

					}

				}

				if (step[1].contains("button") || step[1].contains("nullable"))
					colNum--;

				colNum++;
				iteration++;
				System.out.println("Field Line Processing: SUCCESSFUL");
			}

			takeScreenShot(caseName);
			int trueNextPivotIndex = nextPivotIndex;
			try {
				runSteps(sr, "Post-Steps", rowNum);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ERROR HAS BEEN DETECTED...");
				takeScreenShot(caseName);
				runUndoSteps(sr);
				Map<String, String> FailedArrayAttr = ArgumentHandler.executeFailedArrayRow(excelReader, hasArray, 
						isNonIdenticalArray, rowInputs, rowNum, nextPivotIndex, trueNextPivotIndex);
				rowNum = Integer.parseInt(FailedArrayAttr.get("rowNum"));
				nextPivotIndex = Integer.parseInt(FailedArrayAttr.get("nextPivotIndex"));
				trueNextPivotIndex = Integer.parseInt(FailedArrayAttr.get("trueNextPivotIndex"));
				
				rowNum += 1 + nextPivotIndex;
				pivotIndex += 1 + trueNextPivotIndex;
				nextPivotIndex = 0;
				sumMsg = ReporterManager.processFailedSR(excelReader, e, sumMsg, errMsg, dataType, dataName, rowNum);
				continue readerloop;
			}

			rowNum += 1 + nextPivotIndex;
			pivotIndex += 1 + trueNextPivotIndex;
			nextPivotIndex = 0;
			sumMsg = ReporterManager.processSuccessSR(excelReader, sumMsg, dataType, dataName, rowNum);
		}
		
		srMap.put("sumMsg", srMap.get("sumMsg")+sumMsg);
		return srMap;
	}

	// Get the Excel Sheet Name in the config_file.txt
	private String getSheetName(String name) {
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();

		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("Sheetname")) {
				String[] sheetname = current.split(" \\| ");
				excelReader.setActiveSheet(sheetname[1]);
				//break;
				return sheetname[1];
			}
		}
		
		return "No sheet found.";
	}

	private String getPropertiesAction(String name) {
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();
		String action = null;
		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("Action") && !current.contains("Sheetname")) {
				String[] Action = current.split(" \\| ");
				action = Action[1];
				break;
			}
		}
		return action;
	}

	private String getPropertiesRecursive(String name) {// returns false by default
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();
		String recursive = "";
		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("Recursive")) {
				String[] Recursive = current.split(" \\| ");
				recursive = Recursive[1];
				break;
			}
		}
		if (recursive.isEmpty())
			recursive = "false";
		return recursive;
	}

	private String getPropertiesRedirect(String name) {// returns false by default
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();
		String redirect = null;
		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("Redirect-Enabled")) {
				String[] Redirect = current.split(" \\| ");
				redirect = Redirect[1];
				break;
			}
		}
		if (redirect.isEmpty())
			redirect = "false";
		return redirect;
	}
	
	private String getPropertiesSMReturnee(String name) {// returns false by default
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();
		String smr = "";
		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("SetupAndMaintenanceReturnee")) {
				String[] SMR = current.split(" \\| ");
				smr = SMR[1];
				break;
			}
		}
		if (smr.isEmpty())
			smr = "false";
		return smr;
	}

	private String getPropertiesDataName(String name, int rowNum){
		Vector<String> properties = textReader.getCollection(name, "Properties");
		Enumeration<String> elements = properties.elements();
		String data = "", retrievedData = "";
		int colNum = 0;
		while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			if (current.contains("Data Name")) {
				String[] dataName = current.split(" \\| ");
				data = dataName[1];
				break;
			}
		}
		if (data.isEmpty())
			data = "*Name";
		
		while(excelReader.getCellData(defaultLabelRow, colNum).length()>0){
			if(excelReader.getCellData(defaultLabelRow, colNum).contentEquals(data)){
				retrievedData = excelReader.getCellData(rowNum, colNum);
			}
			colNum += 1;
		}
		if(retrievedData.isEmpty())
			retrievedData = "Data Name";
		
		return retrievedData;
	}
	// Runs the Pre-Steps or Post-Steps set in the config_file.text
	private void runSteps(String name, String steps, int curRowNum) throws Exception {
		Vector<String> Steps = textReader.getCollection(name, steps);
		Enumeration<String> elements = Steps.elements();
		int STEP_TIMEOUT = MAX_TIMEOUT;
		System.out.println("Executing " + steps + ".");

		stepsloop: while (elements.hasMoreElements()) {

			String current = elements.nextElement();
			String[] step = current.split(" \\| ");
			int colNum = 0;

			if (current.isEmpty())
				break;
			if (current.contains("skip:")) {
				break stepsloop;
			}
			if (current.contains("execute:")) {
				String rs = ArgumentExecutor.executeArithmetic(current, afrrkInt);
				afrrkInt = Integer.parseInt(rs);
				System.out.println("Arithmetic has been executed successfully...");
				continue stepsloop;
			}

			current = ArgumentHandler.executeArgumentConverter(current, excelReader, curRowNum, rowGroup, colNum);
			step = current.split(" \\| ");
			// Third step checker..
			if (step[3].contains("$afrrkInt")) {
				step[3] = step[3].replace("$afrrkInt", "" + afrrkInt);
			}
			// Waiting variations...
			TaskUtilities.jsCheckMessageContainer();
			if (step[0].equalsIgnoreCase("row")) {
				TaskUtilities.customWaitForElementVisibility(step[2], step[3],15, new CustomRunnable() {

							public void customRun() throws Exception {
								// TODO Auto-generated method stub
								TaskUtilities.scrollDownToElement(false, "");
							}
						});
			}

			if (step[0].toUpperCase().contains("WAIT TO DISAPPEAR")) {
				System.out.println("Waiting for " + step[1] + " to dissappear...");
				wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(step[2],step[3])));
				continue stepsloop;
			}
			else if(step[0].toUpperCase().contains("WAIT UNTIL CLICKABLE")){
				System.out.println("Waiting for " + step[1]+ " to be clickable...");
				wait.until(ExpectedConditions.elementToBeClickable((getLocator(step[2],step[3]))));
				continue stepsloop;
			}
			if (step.length > 4) {
				if (step[4].contains("wait")) {
					Map<String, String> waitParams = ArgumentExecutor.executeWait(current);
					if (Integer.parseInt(waitParams.get("waitTime")) / 1000 > STEP_TIMEOUT) {
						STEP_TIMEOUT = Integer.parseInt(waitParams.get("waitTime")) / 1000;
					}
				}
			}
			TaskUtilities.customWaitForElementVisibility(step[2], step[3], STEP_TIMEOUT, new CustomRunnable() {

						public void customRun() throws Exception {
							// TODO Auto-generated method stub
							TaskUtilities.jsCheckMessageContainer();
							TaskUtilities.jsCheckInputErrors();
						}
					});

			WebElement we = getElement(step[2], step[3]);
			TaskUtilities.jsScrollIntoView(step[2], step[3]);

			// Wait for element to be present
			// wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(step[2],step[3])));

			/*
			 * //Auto scroll to element Coordinates coordinate =
			 * ((Locatable)getLocator(step[2],step[3])).getCoordinates();
			 * coordinate.onPage(); coordinate.inViewPort();
			 */
			if (step[1].contains("js")) {
				TaskUtilities.jsFindThenClick(step[2], step[3]);
			} else {
				TaskUtilities.retryingFindClick(getLocator(step[2], step[3]));
			}
			if (step[1].contains("enter")) {
				we.sendKeys(Keys.ENTER);
			}
			// Wait for element to be clickable
			// wait.until(ExpectedConditions.elementToBeClickable(getLocator(step[2],step[3])));

			// Click Element
			// WebElement elmnt =
			// driver.findElement(getLocator(step[2],step[3]));
			// JavascriptExecutor executor = (JavascriptExecutor)driver;
			// executor.executeScript("arguments[0].click();", elmnt);

			// Display console
			System.out.println(step[0] + " " + step[1] + " is clicked using " + step[2] + " = " + step[3]);

			// Take screenshot
			takeScreenShot(caseName);
		}
	}

	// Runs Pre-prep steps in the config_file.text
	private void runPrePrep(String name) throws Exception {
		System.out.println("Preparation Step Status: IN PROGRESS");
		Vector<String> prePeps = textReader.getCollection(name, "Pre-Prep");
		Enumeration<String> elements = prePeps.elements();

		preploop: while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			String[] step = current.split(" \\| ");
			if (current.isEmpty())
				break;

			System.out.println("Executing Pre-Prep Statement: " + current);
			if (current.contains("execute:")) {
				String rs = ArgumentExecutor.executeArithmetic(current,afrrkInt);
				afrrkInt = Integer.parseInt(rs);
				System.out.println("Arithmetic has been executed successfully...");
				continue preploop;
			} else if (current.contains("skip:")) {
				break preploop;
			}
			// First step checker...
			if (step[0].equalsIgnoreCase("table")) {
				String dummy = step[3].substring(0, step[3].indexOf("//tr"))+ "/..";
				// step[3] = step[3].substring(0,
				// step[3].indexOf("//tr"))+"/..";
				System.out.println("locator path is now: " + step[3]);
				// wait.until(ExpectedConditions.elementToBeClickable(getLocator(step[2],step[3])));
				TaskUtilities.customWaitForElementVisibility(step[2], dummy,MAX_TIMEOUT);
				TaskUtilities.retryingFindClick(getLocator(step[2], dummy));
				afrrkInt = TaskUtilities.surveyCurrentTableInputs(step[3]);
			}

			if (!step[0].contentEquals("table")) {
				TaskUtilities.customWaitForElementVisibility(step[2], step[3], MAX_TIMEOUT);
				// wait.until(ExpectedConditions.elementToBeClickable(getLocator(step[2],step[3]))).click();
				if(step[1].contains("js")){
					TaskUtilities.jsFindThenClick(step[2], step[3]);
				} else{
					TaskUtilities.retryingFindClick(getLocator(step[2], step[3]));
				}
				if(step[1].contains("enter")){
					driver.findElement(getLocator(step[2], step[3]));
				}
			}
		}
		System.out.println("Preparation Step Status: DONE");
	}

	// Determine the appropriate actions based on the type of element set in the config_file.txt
	public void action(String name, String type, String locatorType, String locator, String data) throws Exception {
		System.out.println("Performing actions...");
		int TIME_OUT = MAX_TIMEOUT;
		int attempts = 0;

		if (type.contentEquals("skippable")) {
			if (data.isEmpty() || data.contains("blank")) {
				return;
			} else {
				// Skips
			}
		}
		actionloop: while (attempts < 3) {
			try {

				if (name.contentEquals("Cancel"))
					TIME_OUT = 10;
				System.out.println("Waiting for element to be found...");
				TaskUtilities.customWaitForElementVisibility(locatorType,locator, TIME_OUT, new CustomRunnable() {

							public void customRun() throws Exception {
								// TODO Auto-generated method stub
								TaskUtilities.jsCheckMessageContainer();
								TaskUtilities.jsCheckInputErrors();
							}
						});
				TaskUtilities.jsScrollIntoView(locatorType, locator);
				// boolean isDisabled =
				// TaskUtilities.jsCheckEnablementStatus(locatorType ,locator);
				// if(!isDisabled)
				// wait.until(ExpectedConditions.elementToBeClickable(getLocator(locatorType,locator)));
				WebElement element = getElement(locatorType, locator);

				// Coordinates coordinate =
				// ((Locatable)element).getCoordinates();
				// coordinate.onPage();
				// coordinate.inViewPort();
				
				if (type.contains("textbox") || type.contains("dropdown")) {
					// wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(locatorType,locator)));

					element.click();
					element.clear();
					element.click();
					System.out.println(name + " " + type + " "+ " is clicked using " + locatorType + " = " + locator);
					if (data.toLowerCase().contains("yes,")) {
						data = data.substring(data.indexOf(",") + 1);
						data = data.trim();
					}else if(data.contains("blank")){
						data = "";
					}
					
					if(type.contains("serial")){
						String[] bits = data.split("");
						for(String bit: bits){
							element.sendKeys(bit);
							//Thread.sleep(5);
						}
					}else{
						element.sendKeys(data);
						if (type.contentEquals("dropdown")) element.sendKeys(Keys.ENTER);
						element.sendKeys(Keys.TAB);
					}
					
					System.out.println(name + " = " + data);

				} else if (type.contains("select")) {
					// wait.until(ExpectedConditions.elementToBeClickable(getLocator(locatorType,locator)));
					if (type.contentEquals("select")) {
						new Select(element).selectByVisibleText(data);
						element.sendKeys(Keys.ENTER);
						System.out.println(name + " " + type + " "+ " is clicked using " + locatorType + " = " + locator);
					} else {
						int curOps = 0;
						WebElement option = getElement(locatorType, locator + "/option[@title='"+data+"']");
						int opsValue = Integer.parseInt(option.getAttribute("value"));
						
						element.sendKeys(Keys.PAGE_UP);
						while (curOps < opsValue) {
							element.sendKeys(Keys.ARROW_DOWN);
							curOps += 1;
						}
						element.sendKeys(Keys.TAB);
						System.out.println(name + " " + type + " "+ " is picked using " + locatorType + " = " + locator);
					}
					System.out.println(name + " = " + data);
					Thread.sleep(1000);

				} else if (type.contains("button")) {
					// wait.until(ExpectedConditions.elementToBeClickable(getLocator(locatorType,locator)));
					if (type.contains("js")) {
						TaskUtilities.jsFindThenClick(locatorType, locator);
					} else {
						element.click();
					}
					System.out.println(name + " " + type + " "+ " is clicked using " + locatorType + " = " + locator);
					Thread.sleep(1000);

				} else if (type.contentEquals("radio")) {
					element.click();
					System.out.println(name + " " + type +" is clicked using " + locatorType + " = " + locator);
					System.out.println(name + " = " + data);

				} else if (type.contentEquals("checkbox")) {
					// wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(locatorType,locator)));
					boolean isChecked = TaskUtilities.jsGetCheckboxTickStatus(locatorType, locator);
					// if(element.isSelected()) {
					if ((data.toUpperCase().contentEquals("FALSE") || data.toUpperCase().contains("NO") || data.toUpperCase().contains("BLANK")) && isChecked) {
						element.click();
						System.out.println(name + " " + type + " "+ " is clicked using " + locatorType + " = "+ locator);
						System.out.println(name + " = " + data);
					}
					else if ((data.toUpperCase().contentEquals("TRUE") || data.toUpperCase().contains("YES") || !data.isEmpty()) && !isChecked) {
						if(!data.isEmpty() && (data.toUpperCase().contentEquals("FALSE") || data.toUpperCase().contentEquals("NO")  || data.toUpperCase().contains("BLANK"))){
							//Skips since data is No or false...
						}else{
							element.click();
							System.out.println(name + " " + type + " "+ " is clicked using " + locatorType + " = "+ locator);
							System.out.println(name + " = " + data);
						}
					}
				} else if (type.contentEquals("combobox")) {
					List<String> actions = ArgumentExecutor.parseCombobox(name,locator, data);
					for (String act : actions) {
						System.out.println("Will now process..." + act);
						String[] step = act.split(" \\| ");
						action(step[0], step[1], step[2], step[3], data);
					}
				} else if (type.contentEquals("nullable")) {
					// Skips for now...
				}

				if (type.contains("enter")) {
					element.sendKeys(Keys.ENTER);
				}

				break actionloop;
			} catch (StaleElementReferenceException e) {
				e.printStackTrace();
				attempts += 1;
				if (attempts == 3)
					throw e;
			} catch (TimeoutException te) {
				if (attempts > 0) {
					wait = new WebDriverWait(driver, ELEMENT_APPEAR);
					throw te;
				}
				attempts += 1;
				wait = new WebDriverWait(driver, ELEMENT_APPEAR / 2);
			} catch (UnhandledAlertException ue) {
				throw new UnhandledAlertException("Unexpected modal dialog appeared. Reverting steps.");
			} catch (WebDriverException we) {
				we.printStackTrace();
				TaskUtilities.jsCheckMessageContainer();
				TaskUtilities.jsCheckInputErrors();
				attempts += 1;
				if (attempts == 3)
					throw we;
			}
		}
	}

	// Runs the Undo-steps set in the config_file.text
	private void runUndoSteps(String name) throws Exception {
		Vector<String> undoSteps = textReader.getCollection(name, "Undo-Steps");
		Enumeration<String> elements = undoSteps.elements();
		System.out.println("Default Fallback Steps Execution: IN PROGRESS");

		unsloop: while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			String[] step = current.split(" \\| ");

			if (current.isEmpty() || current.contains("case:"))
				break;
			if (current.startsWith("//")){
				continue unsloop;
			}
			// Execute entry...
			if (current.contains("execute:")) {
				String rs = ArgumentExecutor.executeArithmetic(current,afrrkInt);
				afrrkInt = Integer.parseInt(rs);
				System.out.println("Arithmetic has been executed successfully...");
				continue unsloop;
			}

			if (step[0].toUpperCase().contains("WAIT TO DISAPPEAR")) {
				System.out.println("Waiting for " + step[1]	+ " to dissappear...");
				wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(step[2],step[3])));
				continue unsloop;
			}

			TaskUtilities.customWaitForElementVisibility(step[2], step[3],MAX_TIMEOUT);
			
			if (step[1].contentEquals("clear")) {
				driver.findElement(getLocator(step[2], step[3])).clear();
			}
			
			TaskUtilities.jsScrollIntoView(step[2], step[3]);
			if(step[1].contains("retry")){
				TaskUtilities.retryingFindClick(getLocator(step[2], step[3]));
			}else{
				TaskUtilities.jsFindThenClick(step[2], step[3]);
			}

			System.out.println(step[0] + " " + step[1] + " is clicked using " + step[2] + " = " + step[3]);
		}

		System.out.println("Default Fallback Steps Execution: DONE");
	}

	// Supports Fallback methods for Exception Cases
	private Map<String, String> runUndoCaseSteps(Map<String,String> fieldVariables, String caseItem, String data, int curRowNum) throws Exception {
		int colNum = 0;
		String name = fieldVariables.get("sr");
		String caseType = fieldVariables.get("caseType");
		int rowInputs = Integer.parseInt(fieldVariables.get("rowInputs"));
		int nextPivotIndex = Integer.parseInt(fieldVariables.get("nextPivotIndex"));
		boolean isAnArrayAction = Boolean.parseBoolean(fieldVariables.get("isAnArrayAction"));
		boolean hasArray = Boolean.parseBoolean(fieldVariables.get("hasArray"));
		boolean isNonIdenticalArray = Boolean.parseBoolean("isNonIdenticalArray");
		Map<String, String> exceptionArray = new HashMap<String,String>();
		
		System.out.print("Setting Case elements");
		Vector<String> undoCaseSteps = textReader.getCaseCollection(name, caseItem, caseType);
		Enumeration<String> elements = undoCaseSteps.elements();
		System.out.println(" ...DONE.");
		exceptionArray.put("rowNum", ""+curRowNum);
		exceptionArray.put("nextPivotIndex", ""+nextPivotIndex);
		
		System.out.println("Undo-Step - Case: "+caseItem+" execution: IN PROGRESS");
		
		uncaseloop: while (elements.hasMoreElements()) {
			String current = elements.nextElement();
			System.out.println("Executing Undo-Case statement: " + current);
			// current = current.replace("\t", "");
			String[] step = current.split(" \\| ");
			
			if((isAnArrayAction || hasArray)){
				if(!isNonIdenticalArray){
					while(excelReader.getCellData(curRowNum, 0).contentEquals(excelReader.getCellData(curRowNum+1, 0))){
						curRowNum += 1;
						if(rowInputs > 0) rowInputs += 1;
						//nextPivotIndex += 1;
					}
				}else{
					while(excelReader.getCellData(curRowNum, 0).length()>0){
						curRowNum += 1;
						if(rowInputs > 0) rowInputs += 1;
					}
				}
				
				exceptionArray.put("rowNum", ""+curRowNum);
				if(rowInputs > 0){
					exceptionArray.put("nextPivotIndex", ""+rowInputs);
				}else{
					exceptionArray.put("nextPivotIndex", ""+nextPivotIndex);
				}
			}

			if (current.isEmpty()){
				exceptionArray.put("isResuming", ""+true);
				return exceptionArray;
				//return true;
			}
			if (current.startsWith("//")){
				continue uncaseloop;
			}
			// Decision here...
			if (step[0].contentEquals("resume esac")) {
				System.out.println("Undo-Step - Case: "+caseItem+" execution: DONE");
				exceptionArray.put("isResuming", ""+true);
				return exceptionArray;
				//return true;
			} else if (step[0].contentEquals("end esac")) {
				System.out.println("Undo-Step - Case: "+caseItem+" execution: DONE");
				exceptionArray.put("isResuming", ""+false);
				return exceptionArray;
				//return false;
			}

			if (step[0].toUpperCase().contains("WAIT TO DISAPPEAR")) {
				System.out.println("Waiting for " + step[1] + " to dissappear...");
				wait.until(ExpectedConditions.invisibilityOfElementLocated(getLocator(step[2],step[3])));
				continue uncaseloop;
			}

			// Validation actions...
			current = ArgumentHandler.executeArgumentConverter(current, excelReader, curRowNum, rowGroup, colNum);
			step = current.split(" \\| ");

			if (step.length > 4) {
				if (step[4].contains("encode")) {
					String[] read = step[4].split(":");
					data = read[1];
				}
			}
			// pseudo perform actions...
			if (step[1].contentEquals("textbox")) {
				action(step[0], step[1], step[2], step[3], data);
			} else if (step[1].contentEquals("clear")) {
				driver.findElement(getLocator(step[2], step[3])).clear();
			}

			// Execute entry...
			if (current.contains("execute:")) {
				String rs = ArgumentExecutor.executeArithmetic(current,afrrkInt);
				afrrkInt = Integer.parseInt(rs);
				System.out.println("Arithmetic has been executed successfully...");
				continue uncaseloop;
			}

			TaskUtilities.customWaitForElementVisibility(step[2], step[3], MAX_TIMEOUT);
			TaskUtilities.jsScrollIntoView(step[2], step[3]);

			if(step[1].contains("js")){
				TaskUtilities.jsFindThenClick(step[2], step[3]);
			}else{
				TaskUtilities.retryingFindClick(getLocator(step[2], step[3]));
			}
			// wait.until(ExpectedConditions.elementToBeClickable(getLocator(step[2],step[3]))).click();

			System.out.println(step[0] + " " + step[1] + " is clicked using " + step[2] + " = " + step[3]);
		}

		exceptionArray.put("isResuming", ""+false);
		return exceptionArray;
		//return true;
	}

	//DEV ONLY: show customization done on the text file...
	private void showConfigurations(String sr){
		String msg = "";
		
		msg += "\n======================  C O N F I G U R A T I O N S  =====================\n";
		msg += "Service Request Name: "+ sr;
		msg += "\nTarget Customization File: "+configPath;
		msg += "\nTarget Excel File: "+excelPath;
		msg += "\nTarget Excel Sheet: "+getSheetName(sr);
		msg += "\nPreferred Action: "+getPropertiesAction(sr);
		msg += "\nRecursive Action: "+getPropertiesRecursive(sr);
		msg += "\nSetup and Maintenance Fallback: "+getPropertiesSMReturnee(sr);
		msg += "\nTarget Data Name: "+getPropertiesDataName(sr, defaultLabelRow);
		msg += "\n==========================================================================\n";
		
		System.out.println(msg);
	}

	public WebElement getElement(String type, String value) {
		return wait.until(ExpectedConditions.presenceOfElementLocated(getLocator(type, value)));
	}

	public void clearAndType(WebElement element, String data) {
		element.click();
		element.clear();
		element.click();
		element.sendKeys(data.trim());
		element.sendKeys(Keys.TAB);

	}

	public By getLocator(String type, String value) {
		if (type.contentEquals("id"))return By.id(value);
		else if (type.contentEquals("xpath"))return By.xpath(value);
		else if (type.contentEquals("tagname"))return By.xpath(value);
		else if (type.contentEquals("classname"))return By.className(value);
		else if (type.contentEquals("cssselector"))return By.cssSelector(value);
		else if (type.contentEquals("name"))return By.name(value);
		else if (type.contentEquals("linktext"))return By.linkText(value);
		else return By.partialLinkText(value);
	}

	// Screenshot
	public void takeScreenShot(String caseName) {
		String datePrefix = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
		String path = workspace_path + screenShotPath+ caseName.replace(" ", "_") + "";
		String ssPath = workspace_path + screenShotPath;
		try {
			File ssDir = new File(ssPath);
			if (!ssDir.exists())
				ssDir.mkdir();
			File dir = new File(path);
			if (!dir.exists()) {
				System.out.println("The location " + path + " does not exist.");
				dir.mkdir();
				System.out.println("A directory " + path + " is created.");
			}

			byte[] screenshot;

			screenshot = ((org.openqa.selenium.TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.BYTES);

			File screenshotFile = new File(MessageFormat.format("{0}/{1}-{2}",path, datePrefix, caseName.replace(" ", "_") + ".png"));

			FileOutputStream outputStream = new FileOutputStream(screenshotFile);
			try {
				outputStream.write(screenshot);
				System.out.println("Screen shot "+ screenshotFile.toString().substring(path.length() + 1) + " saved in "+ path);
			} finally {
				outputStream.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		driver.close();
		driver.quit();
	}

}
