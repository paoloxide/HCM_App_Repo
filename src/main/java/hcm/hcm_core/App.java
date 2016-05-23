package hcm.hcm_core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openqa.selenium.TakesScreenshot;

import hcm.seldriver.SeleniumDriver;
import hcm.utilities.ExcelReader;
import hcm.utilities.TextUtility;


/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) throws Exception{
    	
    	Map <String,String> srMap = new HashMap<String, String>();
    	Options options = new Options();
    	options.addOption("r", true, "service request number");
    	options.addOption("w", true, "current workspace");
    	options.addOption("e", true, "path to excel file");
    	String dataObject = null;
    	String workspace = null;
		String excel = null;
		srMap.put("sumMsg", "");
    	
    	CommandLineParser parser = new DefaultParser();
    	try {
			CommandLine cmd = parser.parse(options, args);
			
			if(cmd.hasOption("r")) {
				dataObject = cmd.getOptionValue("r");
			}
			
			if(cmd.hasOption("w")) workspace = cmd.getOptionValue("w");
			if(cmd.hasOption("e")) excel = cmd.getOptionValue("e");
			
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	
    	//String businessObject = "Manage Data Role and Security Profiles";
    	
    	SeleniumDriver sel = new SeleniumDriver();
    	
    	System.out.println("Initializing drivers...");
    	sel.initializeDriver("http://selenium-hub:4444/wd/hub", "firefox", workspace, excel);
    	System.out.print("Validating argument before startup");
    	sel.validateArgument(dataObject, workspace, excel);
		System.out.println("Running Test in FireFox");
		
    	//sel.initializeDriver("http://10.251.120.22:4444/wd/hub", "firefox", workspace, excel);
    	//sel.initializeDriver("http://192.168.1.11:4444/wd/hub", "firefox", workspace, excel);
    	System.out.println("Drivers Initialized.");
    	
    	sel.login(sel.getLoginCredentials("URL"), sel.getLoginCredentials("USERID"), sel.getLoginCredentials("PASSWORD"));
    	
    	sel.setupAndMaintenance();
    	    	  	
    	try{
	    		srMap = sel.runServiceRequest(dataObject, srMap);    	
	    		Thread.sleep(10000);
	    		System.out.println(srMap.get("sumMsg"));
	        	sel.dispose();
	        	System.out.println("Service Request has been managed successfully.");
	    	} catch(Exception e){
	    		e.printStackTrace();
	    		sel.takeScreenShot("App");
	    		sel.dispose();
	    		System.out.println("Error Encountered. Aborted Service Request.");
	    	}
    }
}