package actualProjectPackage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ServerMain {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		while(true){
			BufferedReader buffRead = null;
			String measureId[] = new String[100];
			String tempValue[] = new String[100];
			//String foundVlaue;
			String serverMessage;
			String clntMeasIdStr = null;
			String servActMsg=null;
			String clntId = null;
			//long clntMeasId =0;
			long servMsgChkSum;
			byte[] actServMsgByt = null;
			int k =0;int j= 0;
			String foundValue = null ;
			String line ;
			String formattedServMessage;
			byte[] servMsgByt;
			byte[] serverBuffer = new byte[1000];//initializing buffer with some large size value 
			DatagramPacket toRecPacket = new DatagramPacket(serverBuffer,serverBuffer.length);//creating receiving packet
			DatagramSocket serverSocket = new DatagramSocket(9999);//creating server socket to receive 

			DatagramSocket toSend = new DatagramSocket(52225);//creating separate socket to send

			serverSocket.receive(toRecPacket);//receiving packets
			int clientPort = toRecPacket.getPort();//extracting client's port number
			InetAddress clientAddress = toRecPacket.getAddress();//extracting client's address
			String origClntMsg = null;
			byte[] clientDataArray = new byte[toRecPacket.getLength()];//creating a byte[] of received data length
			clientDataArray = Arrays.copyOf(toRecPacket.getData(),toRecPacket.getLength());
			int i =0;
			int index = 0;
			int[] ind = new int[6];//creating array to store repetitions of char > to segregate checksum value
			char search = '>';//character to search 
			byte toSearch = (byte)search;//getting ASCII value of > to search for

			for(int g=0;g<clientDataArray.length;g++)
				{if(clientDataArray[g] == toSearch)//creating an array of indices occurrences of char > to pick maximum
				{ind[i] = g;
				i++;
				}	
				}
			for (int t=0;t<ind.length;t++)
				{if (ind[t] > index){index = ind[t];} //getting the highest index for occurrence of >
				}

			byte[] clientChkSumByte = Arrays.copyOfRange(clientDataArray,index+1,clientDataArray.length);//extracting byte[] of check sum

			String chkSumStr = new String(clientChkSumByte,StandardCharsets.UTF_8);//converting byte[] of checksum to string
			long chkSumVal = Integer.parseInt(chkSumStr);//extracting checksum value sent by client from string as unsigned integer

			byte[] clientMessageByte = Arrays.copyOfRange(clientDataArray,0,index+1);//extracting byte[] of message excluding checksum
			ServerClass aObject = new ServerClass(clientMessageByte);//creating object
			long msgChkSum = aObject.calculateCheckSum(clientMessageByte);//calculating checksum of message from byte[]


			if (msgChkSum == chkSumVal)// if condition to check for integrity checksum
				{origClntMsg = new String(clientDataArray,StandardCharsets.UTF_8);//extracting entire message string if it passes integrity checksum
				clntId = ServerClass.getPatternMatch(1,origClntMsg,"<id>(.*?)</id>");
				clntMeasIdStr = ServerClass.getPatternMatch(1,origClntMsg,"<measurement>(.*?)</measurement>");//extracting measurement id 
				long clntMeasdIdInt = Integer.parseInt(clntMeasIdStr);
				//System.out.print(origClntMsg);
				if (ServerClass.checkSyntax(origClntMsg,clntMeasdIdInt)) // if condition to check for syntax i.e. all tags,placement and to check for 16 bit unsigned integer i.e. measurement id according to step 3
					{
					try{
						buffRead = new BufferedReader(new FileReader("C:\\Users\\comp\\Downloads\\data.txt"));} //reading value corresponding to measurement id
					catch (FileNotFoundException e) {System.out.println("File not found");}


					while ((line = buffRead.readLine()) != null)
						{String info[] = line.split("\\s+");
						measureId[k] = info[0];
						tempValue[j] = info[1];
						k++;
						j++;}
						//System.out.println("\n"+clntMeasIdStr);

					if (Arrays.asList(measureId).contains(clntMeasIdStr))// third if condition to check for valid measurement id
						{for (int y=0;y<measureId.length;y++)
							{if (measureId[y].equals(clntMeasIdStr))//
							{foundValue = tempValue[y];}}


						serverMessage = ServerClass.createMessage(clntId ,0,clntMeasIdStr,foundValue);//creating message when code is 0

						servMsgByt = ServerClass.convertStringtoByteArray(serverMessage.replaceAll("\\s+",""));//converting message to byte array

						ServerClass aObj = new ServerClass(servMsgByt);
						servMsgChkSum = aObj.calculateCheckSum(servMsgByt);//calculating checksum for created response message

						servActMsg = serverMessage + servMsgChkSum;//appending check sum to response message
						formattedServMessage = servActMsg.replaceAll("\\s+","");//removing all white spaces before sending out
						System.out.print("\n"+servActMsg+"\n");
						actServMsgByt = ServerClass.convertStringtoByteArray(formattedServMessage);//converting entire response message including checksum to byte[]
						DatagramPacket toSendPacket = new DatagramPacket(actServMsgByt,actServMsgByt.length,clientAddress,clientPort);//creating datagrampacket to send
						toSend.send(toSendPacket);serverSocket.close();toSend.close();
						}
					else //sending message when measurement id does not match i.e. code 3
						{serverMessage = ServerClass.createMessage(clntId,3,null,null);
						servMsgByt = ServerClass.convertStringtoByteArray(serverMessage.replaceAll("\\s+",""));//converting message to byte array
						ServerClass aObj = new ServerClass(servMsgByt);
						servMsgChkSum = aObj.calculateCheckSum(servMsgByt);//calculating checksum for created response message
						servActMsg = serverMessage + servMsgChkSum;//appending check sum to response message
						formattedServMessage = servActMsg.replaceAll("\\s+","");//removing all white spaces before sending out
						System.out.print("\n"+servActMsg + "\n");
						actServMsgByt = ServerClass.convertStringtoByteArray(formattedServMessage);//converting entire response message including checksum to byte[]
						DatagramPacket toSendPacket = new DatagramPacket(actServMsgByt,actServMsgByt.length,clientAddress,clientPort);//creating datagrampacket to send
						toSend.send(toSendPacket);serverSocket.close();toSend.close();
						}

					}
				else //sending message when syntax does not match i.e. code 2
					{clntId = ServerClass.getPatternMatch(1,origClntMsg,"<id>(.*?)</id>");
					serverMessage = ServerClass.createMessage(clntId,2,null,null);
					servMsgByt = ServerClass.convertStringtoByteArray(serverMessage.replaceAll("\\s",""));//converting message to byte array
					ServerClass aObj = new ServerClass(servMsgByt);
					servMsgChkSum = aObj.calculateCheckSum(servMsgByt);//calculating checksum for created response message
					servActMsg = serverMessage + servMsgChkSum;//appending check sum to response message
					formattedServMessage = servActMsg.replaceAll("\\s+","");//removing all white spaces before sending out
					System.out.print("\n"+servActMsg + "\n");
					actServMsgByt = ServerClass.convertStringtoByteArray(formattedServMessage);//converting entire response message including checksum to byte[]
					DatagramPacket toSendPacket = new DatagramPacket(actServMsgByt,actServMsgByt.length,clientAddress,clientPort);//creating datagrampacket to send
					toSend.send(toSendPacket);serverSocket.close();toSend.close();}
					}

			else //sending message when integrity checksum fails i.e. code 1
				{serverMessage = ServerClass.createMessage(null,1,null,null);
				servMsgByt = ServerClass.convertStringtoByteArray(serverMessage.replaceAll("\\s",""));//converting message to byte array
				ServerClass aObj = new ServerClass(servMsgByt);
				servMsgChkSum = aObj.calculateCheckSum(servMsgByt);//calculating checksum for created response message
				servActMsg = serverMessage + servMsgChkSum;//appending check sum to response message
				//System.out.println(servMsgChkSum);
				formattedServMessage = servActMsg.replaceAll("\\s+","");//removing all white spaces before sending out
				System.out.print("\n"+servActMsg+"\n");
				actServMsgByt = ServerClass.convertStringtoByteArray(formattedServMessage);//converting entire response message including checksum to byte[]
				DatagramPacket toSendPacket = new DatagramPacket(actServMsgByt,actServMsgByt.length,clientAddress,clientPort);//creating datagrampacket to send
				toSend.send(toSendPacket);serverSocket.close();toSend.close();
				}//else condition
			}//while condition
		}//main method
	}//class ServerMain
	
	
	
	