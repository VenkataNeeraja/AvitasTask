package com.avitas.core.util;

 
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;



public class ReadData {
	
    public static Workbook workbook;
    public static HSSFSheet worksheet;
    public static DataFormatter formatter= new DataFormatter();
    public static String file_location = System.getProperty("user.dir") + "//data//data.xls";
    //public static String file_location = "data/data.xls";
    static String SheetName= "Login";
    public  String Res;
    
    public int DataSet=-1;
	public static Object[][] ReadExcel() throws IOException, InvalidFormatException
    {
	
	FileInputStream stream = new FileInputStream(file_location); //Excel sheet file location get mentioned here
	Workbook workbook = WorkbookFactory.create(stream); //get my workbook
	Sheet s = workbook.getSheet(SheetName);// get my sheet from workbook
	
    Row Row=s.getRow(0);    //get my Row which start from 0   
 
    int RowNum = s.getPhysicalNumberOfRows();// count my number of Rows
    int ColNum= Row.getLastCellNum(); // get last ColNum 
     
    Object Data[][]= new Object[RowNum-1][ColNum]; // pass my  count data in array
     
        for(int i=0; i<RowNum-1; i++) //Loop work for Rows
        {  
            Row row= s.getRow(i+1);
             
            for (int j=0; j<ColNum; j++) //Loop work for colNum
            {
                if(row==null)
                    Data[i][j]= "";
                else
                {
                    Cell cell= row.getCell(j);
                    if(cell==null)
                        Data[i][j]= ""; //if it get Null value it pass no data 
                    else
                    {
                        String value=formatter.formatCellValue(cell);
                        Data[i][j]=value; //This formatter get my all values as string i.e integer, float all type data value
                    }
                }
            }
        }

    return Data;
}
}
