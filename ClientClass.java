package actualProjectPackage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientClass {

	private int requestId;//variable for request id
	private int measurementId;//variable for measurement id
	private static String requestMessage;//string object for request message
	private static byte[] messageByte; 
	private final static long C = 7919;//C value for checksum
	private final static long D = 65536;//D value for checksum
	private static int groupNo;//variable for group number used in regular expressions
	private static String toMatchStr;
	private static String patternTo;
	public ClientClass(int aRequestId,int aMeasurementId)
		{requestId = aRequestId;
		measurementId = aMeasurementId;
		requestMessage = "";
		messageByte = null;
		groupNo = 0;
		toMatchStr = null;
		patternTo = null;
		}//initializer

//method to create request message
	public String toString()
		{return "<request>" + "\n" + "\t"+ "<id>" + requestId + "</id>" + "\n" + "\t" + "<measurement>" + measurementId +"</measurement>" +"\n" + "</request>";
		}//toString() class

//method to convert string to byte[]
	public static byte[] convertStringtoByteArray(String aRequestMessage)
		{requestMessage = aRequestMessage;
		return requestMessage.getBytes();}//convertStringtoByteArray class


//method to calculate length of a string
	public int calculateLength(String aRequestMessage)
		{requestMessage = aRequestMessage;
		return requestMessage.length();}//calculateLength class

//method to calculate CheckSum over byte[]
	public static long calculateCheckSum(byte[] aMessageByte) 
		{messageByte = aMessageByte;
		long S =0;
		long index;
		if (messageByte.length % 2 == 0)//if character sequence/byte[] (using UTF-8 encoding) is even
			{long[] unsignedWords = new long[messageByte.length/2];//create array of 16 bit unsigned words
			int k = 0;
			for(int i=0;i<messageByte.length;i = i+2)
				{ long x = (long)(((messageByte[i] & 0xFF) << 8) | (messageByte[i+1] & 0xFF));//bit wise shift and or to form unsigned word.lower index arrives first
				unsignedWords[k] = x; k = k +1;}
			for (int j = 0; j<unsignedWords.length;j++)//calculating checksum over array of 16 bit words
			{index =  S ^ unsignedWords[j];
			S = ((C*index) % D);}
			}
	
	//else if character sequence is odd in length
		else if (messageByte.length % 2 !=0)
			{long [] unsignedWords = new long [(messageByte.length+1)/2];
			int o = 0;

			for (int m=0;m<messageByte.length-1;m = m + 2)//iterating over except last odd element
				{long x = (long)(((messageByte[m] & 0xFF) << 8) | (messageByte[m+1] & 0xFF));
				unsignedWords[o] = x;o = o + 1;}
			unsignedWords[(messageByte.length+1)/2 -1 ] = (long)(((messageByte[messageByte.length-1] & 0xFF) << 8) | (0 & 0xFF));//calculating unsigned word for last odd element
			for (int n = 0; n<unsignedWords.length;n++)
				{index =  S ^ unsignedWords[n];
				S = ((C*index) % D);}
			}
		return S;

		}//calculateCheckSum class


//method to extract & return string objects using regular expressions using group number of required string
//, string in which the matching is to take place, pattern to be matched/looked for
	public static String getPatternMatch(int aGroupNo,String aToMatchStr,String aPatternTo)
		{
		groupNo = aGroupNo;
		toMatchStr = aToMatchStr;
		patternTo = aPatternTo;
		Pattern pat = Pattern.compile(patternTo);
		Matcher mat = pat.matcher(toMatchStr);
		mat.find();
		return mat.group(groupNo);//return required string
		}//class getPatternMatch
}//class ClientClass
