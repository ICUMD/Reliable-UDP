package actualProjectPackage;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ClientMain {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		while(true){//do this forever until application is terminated
			String serverMessage;
			Scanner inp = new Scanner(System.in);
			BufferedReader bufread = null;
			final int INIT_TIMEOUT_VAL = 1000;
			DatagramSocket clientSocket = null;
			int counter=1;
			//reading data file to randomly pick measurement id
			try {
				bufread = new BufferedReader(new FileReader("C:\\Users\\comp\\Downloads\\data.txt"));//reading data file
				} catch (FileNotFoundException e) {System.out.println("File not found");}
			String measureid[] = new String[100];//initializing an array to store measurement values
			int i =0;
			String line ;
			while ((line = bufread.readLine()) != null)
				{String info[] = line.split("\\s+");
				measureid[i] = info[0];        //creating array to randomly pick measurement values from data file
				i++;
				}

			Random rand = new Random();
			int MeasId = Integer.parseInt(measureid[rand.nextInt(measureid.length)]);//picking random measure id and converting it to integer
			//MeasId =1234;
			outerloop:
				//loop for step 5, do this i.e. re-send same measurement value as long as error codes are being received
				while(true){Thread.sleep(500); 
				//loop for step 4, do this i.e. re-send same measurement value if integrity checksum of response message fail
					while(true) 
						{Thread.sleep(500);
						int randRequest = rand.nextInt(1 + Short.MAX_VALUE - Short.MIN_VALUE);//generating random 16 bit unsigned request id within unsigned short data limit
						ClientClass aObj = new ClientClass(randRequest,MeasId);
							
						String message = aObj.toString();//creating request message
							
						String formattedMessage = message.replaceAll("\\s+","");//removing all white spaces/tabs before transmission 

						byte[] forCheckSum = ClientClass.convertStringtoByteArray(formattedMessage); //converting request message to byte[] for checksum
						long checkSum = ClientClass.calculateCheckSum(forCheckSum) ; //calculating checksum over byte[] which in turn contain the ASCII code for each character

						String actualMessage = formattedMessage + "" + checkSum;//appending check sum to request message
						System.out.println(aObj.toString() + "\n"+checkSum+"\n");
						byte [] toSendBytes = ClientClass.convertStringtoByteArray(actualMessage);//converting to byte[] from transmission of entire request message including checksum

						InetAddress serverAddress = InetAddress.getLocalHost();//assuming server to be same as client

						DatagramPacket toSendPacket = new DatagramPacket(toSendBytes,toSendBytes.length,serverAddress,9999);//packet to send request
						try {
							clientSocket = new DatagramSocket();//choosing arbitrary socket to send request
							} catch (SocketException e1) {
								System.out.println("No socket available");
								}

						
						//In the following lines, application attempts to send & receive packets,on failure the timeout is doubled till 4th timeout
						int varTimeoutVal = INIT_TIMEOUT_VAL;
						byte[] buffer = new byte[1000];//defining large enough buffer byte[] to receive response message from server
						DatagramPacket toReceivePacket = new DatagramPacket(buffer,buffer.length);
						do
							{try 
							{ 
								try {
									clientSocket.send(toSendPacket);
								} catch (IOException e) {
								System.out.println("Unable to send the packets" + e);
								}//sending message request
								clientSocket.setSoTimeout(varTimeoutVal);//setting timeout interval
								System.out.println("Receiving response from server: Attempt " +"#"+counter);
								clientSocket.receive(toReceivePacket);//trying to receive packets

								break;//breaking off after receiving packet successfully
							}
							catch(InterruptedIOException e)//catching timeout exception
								{counter = counter + 1;varTimeoutVal = 2*varTimeoutVal; //increasing counter & doubling timeout value
								if (counter == 5)
									{System.err.println("ERROR: Communication failure");System.exit(0);} //error message after 4th timeout
									}
							}while(counter <=4);//repeat sending packet until 4th timeout or if a packet is received

						
						//in the following lines , application processes response message upon successful reception
						byte [] serverData = new byte[toReceivePacket.getLength()];//retrieving actual length of received data
						serverData = Arrays.copyOf(toReceivePacket.getData(),toReceivePacket.getLength());//copying byte[] response data from buffer of required length
						serverMessage = new String(serverData,StandardCharsets.UTF_8);//converting byte[] to string message to retrieve checksum

						String serverCheckSumString = ClientClass.getPatternMatch(2,serverMessage,"^(.+?)(\\d*)$");//extracting checksum value

						long serverCheckSum1 = Integer.parseInt(serverCheckSumString);//converting it to long

						String srvrMsgStrChkSu = ClientClass.getPatternMatch(1,serverMessage,"^(.+?)(\\d*)$");//extracting entire response message except checksum

						byte[] srvMsgStrChkSuArBy = ClientClass.convertStringtoByteArray(srvrMsgStrChkSu);//converting to byte[] for checksum calculation
						long serverCheckSum2 = ClientClass.calculateCheckSum(srvMsgStrChkSuArBy);//calculating checksum for response message(16 bit unsigned integer)
						//System.out.println(serverCheckSum2);
						//comparing checksum values of response message
						if (serverCheckSum1 == serverCheckSum2)//comparing calculated and received checksum ,if equality then break & move ahead ,else go to step 2 and repeat sending the request with same measurement id
							{break;}
						else {continue;}
						}//third while loop

						//processing as per code value in response
						String srvrmsgcodestr = ClientClass.getPatternMatch(1,serverMessage,"<code>(.*?)</code>");//extracting response code
						int srvrmsgcode = Integer.parseInt(srvrmsgcodestr);//extracting it in integer
						switch(srvrmsgcode){//processing based on response message codes
							case 0: String srvrmsgmeas = ClientClass.getPatternMatch(1,serverMessage,"<value>(.*?)</value>");System.out.println("The measurement value of "+MeasId+" is :" + srvrmsgmeas + "\n");clientSocket.close();break outerloop;//extract value if code is 1,break and send a new measurement value again
							case 1:System.err.println("Error: integrity check failure . The request has one or more bit errors");
							System.out.println("Do you want to send the same measurement request again ?? (y/n)");
							switch(inp.next())
							{case "y": continue;	// deciding course of action based on user input when response code is 1
							case "n" :System.exit(0);} // upon entering "n" program will terminate & upon entering "y", same measurement request is resent
							case 2: System.err.println("Error: malformed request. The syntax of the request message is not correct");System.exit(0);
							case 3: System.err.println("Error: non-existant measurement. The measurement with the requested measurement ID does not exist.");System.exit(0);


										}//switch

						}//second while loop

			}//first main while loop

	}//main method
}//class ClientMain