package actualProjectPackage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerClass {

	private byte[] bytArray;
	private static String msgString;
	private static String id;
	private static int code;
	private static String measurement;
	private static String value;
	private final long C = 7919;
	private final long D = 65536;
	private static int groupNo;
	private static long measurId;
	private static String toMatchStr;
	private static String patternTo;
	private static String responseMessage;
	//constructor method
	public ServerClass(byte[] abytArray)
		{bytArray = abytArray;
		msgString = null;
		id = null;
		code= 0;
		measurement = null;
		responseMessage = null;
		groupNo = 0;
		toMatchStr = null;
		patternTo = null;

		}
	
	//method to calculate checksum
	public long calculateCheckSum(byte[] abytArray)
		{bytArray = abytArray;
		long S =0;
		long index;
		if (bytArray.length % 2 == 0)
			{long[] unsignedWords = new long[bytArray.length/2];
			int k = 0;
			for(int i=0;i<bytArray.length;i = i +2)
				{long x = (long)(((bytArray[i] & 0xFF) << 8) | (bytArray[i+1] & 0xFF));
				unsignedWords[k] = x; k = k +1;}
			for (int j = 0; j<unsignedWords.length;j++)//calculating checksum
				{index =  S ^ unsignedWords[j];
				S = ((C*index) % D);}
				return S;}

		else //if character sequence is odd in length
			{long [] unsignedWords = new long [(bytArray.length+1)/2];
			int o = 0;

			for (int m=0;m<bytArray.length-1;m = m + 2)//iterating over except last odd element
				{long x = (long)(((bytArray[m] & 0xFF) << 8) | (bytArray[m+1] & 0xFF));
				unsignedWords[o] = x;o = o + 1;}
				unsignedWords[(bytArray.length+1)/2 -1 ] = (long)(((bytArray[bytArray.length-1] & 0xFF) << 8) | (0 & 0xFF));//calculating unsigned word for last odd element
			for (int n = 0; n<unsignedWords.length;n++)
				{index =  S ^ unsignedWords[n];
				S = ((C*index) % D);}
				return S;}
	
		}
	
	//method to check syntax of received message which includes checking all opening,closing tags, spelling,index of tags, measurement id & to ensure that it does not contain any invalid character
	public static boolean checkSyntax(String aMsgString,long aMeasurId)
		{msgString = aMsgString;//add to remove white space here
		measurId = aMeasurId;
		if (msgString.contains("<request>") && msgString.contains("</request>") && msgString.contains("<id>") && msgString.contains("</id>") && msgString.contains("<measurement>") && msgString.contains("</measurement>") && measurId < (Short.MAX_VALUE-Short.MIN_VALUE) && measurId > 0 && measurId == (int)measurId)
			{if ((msgString.indexOf("<request>") < msgString.indexOf("<id>")) && (msgString.indexOf("<id>") < msgString.indexOf("</id>")) && (msgString.indexOf("</id>") < msgString.indexOf("<measurement>")) &&  (msgString.indexOf("<measurement>") <  msgString.indexOf("</measurement>")) &&  (msgString.indexOf("</measurement>") <  msgString.indexOf("</request>")))
				{if (msgString.matches("^[a-zA-Z0-9<>/]+$"))// to ensure it does not contain any invalid character other than required to create message
						{return true;}
				else {return false;}
			}
			else {return false;}	
		}
		else {return false;}
		}
	//method to create response message
	public static String createMessage(String aId,int aCode,String aMeasurement,String aValue)
		{id = aId;
		code = aCode;
		measurement = aMeasurement;
		value = aValue;
		String toReturn = null;
		if (code == 0)
			{toReturn = "<response>" + "\n" +"\t"+"<id>" + id +"</id>" + "\n" + "\t" + "<code>" + code +"</code>" +"\n" +"\t"+ "<measurement>" + measurement + "</measurement>" +"\n" + "\t"+"<value>"+ value + "</value>" + "\n" + "</response>";
			}
		else if (code == 1)
			{toReturn =  "<response>" + "\n"+"\t" +"<id>" + id +"</id>" + "\n" +"\t"+ "<code>" + code +"</code>" + "\n" + "</response>";}
		else if (code ==2){ toReturn =  "<response>" + "\n" +"<id>" + id +"</id>" + "\n" + "<code>" + code +"</code>" + "\n" + "</response>";}
		else if (code == 3){toReturn = "<response>" + "\n" +"<id>" + id +"</id>" + "\n" + "<code>" + code +"</code>" + "\n" + "</response>";}
		return toReturn;

		}

	//method to convert string object to byte[] for transmission
	public static byte[] convertStringtoByteArray(String aResponseMessage)//remove white spaces in main before getting byte[]
		{responseMessage = aResponseMessage;
		return responseMessage.getBytes();
		}

	//method to match patterns and extract desired strings using regular expressions
	public static String getPatternMatch(int aGroupNo,String aToMatchStr,String aPatternTo)
		{
		groupNo = aGroupNo;
		toMatchStr = aToMatchStr;
		patternTo = aPatternTo;
		Pattern pat = Pattern.compile(patternTo);
		Matcher mat = pat.matcher(toMatchStr);
		if (mat.find())
		{return mat.group(groupNo);}
		
		else {return "0";}
		
		}

	}//class ServerClass