package hcm.common;

import hcm.seldriver.SeleniumDriver;
import hcm.utilities.ExcelReader;

public class ReporterManager {
	
	public static String trimErrorMessage(String errMsg) {
		int enterIndex = errMsg.indexOf("\n");
		if(enterIndex != -1) errMsg = errMsg.substring(enterIndex);
		
		int tabIndex = errMsg.indexOf("\t");
		if(tabIndex != -1) errMsg = errMsg.substring(tabIndex);
		
		enterIndex = errMsg.indexOf("\n");
		if(enterIndex != -1) errMsg = errMsg.substring(enterIndex);
		
		int lastEnterIndex = errMsg.indexOf("\n");
		if(lastEnterIndex != -1 && lastEnterIndex-1 >= errMsg.length()){
			errMsg.substring(0, lastEnterIndex-1);
		}
		
		errMsg = errMsg.replaceAll("\\n", "\n[DIAGNOSTIC] ")
				.replaceAll("null", "")
				.replaceAll("\\t", "")
				.replaceAll("  ", "")
				.replaceAll("    ", "");
		
		return errMsg;
		
	}
	public static String formatErrorMessage(String format, String errMsg){
			if(format.contentEquals("logger")){
				errMsg = errMsg.replaceAll("\\n", " ")
				.replaceAll("null", "")
				.replaceAll("\\t", "")
				.replaceAll("  ", "")
				.replaceAll("    ", "");
			}else{
				return errMsg;
			}
		
		return errMsg;
	}
	public static String processFailedSR(ExcelReader excelReader, Exception e, String sumMsg, String errMsg, String dataType, String dataName, int rowNum){
		//errMsg = formatErrorMessage("logger",""+e);
		errMsg += trimErrorMessage(e+errMsg);
		errMsg = errMsg+"\n";
		sumMsg += "[FAILED] Unable to create "+dataType+": "+dataName+"..."+errMsg;
		errMsg = "";
		if(excelReader.getCellData(rowNum, 0).length()>0)
			sumMsg += "------------------------------------------------------------------------\n";
		return sumMsg;
	}
	public static String processSuccessSR(ExcelReader excelReader, String sumMsg, String dataType, String dataName, int rowNum){
		sumMsg += "[SUCCESS] "+dataType+": "+dataName+" has been created successfully.\n";
		if(excelReader.getCellData(rowNum, 0).length()>0)
			sumMsg += "------------------------------------------------------------------------\n";
		return sumMsg;
	}
	
}
