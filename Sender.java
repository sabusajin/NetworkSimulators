
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;

public class Sender {
	
	

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		String fileName = null;
		int port = 0;
		int numpackets = 0;
		
		String protocol = null;
		int m = 0, N = 0, TIMEOUT = 0, segSize = 0, PROBABILITY = 0;
		int flag = 0;
		
		
		if (args.length == 3)
		{
			fileName = args[0];
			port = Integer.parseInt(args[1]);
			numpackets = Integer.parseInt(args[2]);
		}
		else
		{
			System.out.println("++++++++++++++Invalid parameters+++++++++++++++++++++");
		}
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String fileRead = br.readLine();
			
			while (fileRead != null)
            {                
                String[] filecontent = fileRead.split(" ");
                
                if (flag == 0)
                	protocol = filecontent[0];
                else if (flag ==1)
                {
                	m = Integer.parseInt(filecontent[0]);
                	N = Integer.parseInt(filecontent[1]);
                	
                }
                else if (flag ==2)
                	
                	TIMEOUT = Integer.parseInt(filecontent[0]);
                else if (flag == 3)
                	segSize = Integer.parseInt(filecontent[0]);
                
                
                flag++;
                fileRead = br.readLine();
            }

            // close file stream
            br.close();
            
        }catch(FileNotFoundException e)
		{
        	e.printStackTrace();        	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (flag!=4)
			System.out.println("Wrong inputs in input file");
		
		
		
		System.out.println("Seqnum bits "+m+" "+" Window Size "+N+ " " + " Protocol " + protocol + " SegmentSize " +segSize + " Timeout "+ 
		TIMEOUT +" port "+port+ " numpackets "+numpackets);
		
		InetAddress receiverAddress = InetAddress.getByName("localhost");
		
		byte [] ptcl = protocol.getBytes();
		byte [] resp = new byte[1024];
		
		DatagramSocket socket = new DatagramSocket();		
		
		DatagramPacket initial = new DatagramPacket(ptcl, ptcl.length, receiverAddress, port);
		DatagramPacket initialResp = new DatagramPacket(resp, resp.length);
		socket.send(initial);


		socket.receive(initialResp);
		
		resp = initialResp.getData();
		
		String servResp = new String (resp,0,3);
		
		
		
		if (servResp.equals("200"))
		{
			try{
				
				System.out.println("Trying to send first packet");
				
				Thread.sleep(1500);
				
				sendData (protocol, m, N, segSize,TIMEOUT, port, numpackets);
				}catch(Exception e)
				{
					e.printStackTrace();
					System.out.println("Exception");
					
				}
		}
		else
		{
			System.out.println("Wrong protocol input!!!!!!!");
		}
		socket.close();	
		
		
				
	}

	public static void sendData(String protocol, int m, int n, int segSize, int tIMEOUT, int port, int numpackets) throws Exception {
		// TODO Auto-generated method stub
		
		
		
		int Sn = 0, Sf = 0, i;
		InetAddress receiverAddress = InetAddress.getByName("localhost");
		byte[] fileBytes = "AbcDEFGHiJKLmNOPqrstuVWXYZ".getBytes();
		Packet sentPacket = new Packet();
		sentPacket.data = fileBytes;
		sentPacket.window = n;
		byte[] cksum = new byte[fileBytes.length];
		byte x = -1;		
		
		
		for (i=0;i<fileBytes.length;i++)
		{
			cksum [i] = (byte)(fileBytes[i] ^ x);

			
		}
		sentPacket.chksum = cksum;	
			
		
		DatagramSocket toReceiver = null;
		
		if (protocol.equalsIgnoreCase("GBN"))
		{
			while (true)
			{
				while (Sn-Sf <n && Sn<numpackets)
				{
					
					
					byte[] wrngData = "xyz".getBytes();
					sentPacket.setData(fileBytes);	
					sentPacket.setSeqNum(Sn);
					
					
					if (Math.random() <= 0.1)
						sentPacket.setData(wrngData);
					toReceiver = new DatagramSocket();
					
					byte [] sentData = Serializer.toBytes(sentPacket);
					DatagramPacket packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
					System.out.println("===============Sending packet with seqno===================== "+ sentPacket.getSeqNum());
					if (Sn==0 && Sf==0)
						System.out.println("+++++++++Timer started for packet+++++++"+Sf);
					toReceiver.send(packet);
					Sn++;
					Thread.sleep(2500);
					
				}
				
				System.out.println();
				byte[] ackBytes = new byte[1024];
				DatagramPacket ack = new DatagramPacket(ackBytes,ackBytes.length);
				try{
					toReceiver.setSoTimeout(tIMEOUT);
					
					toReceiver.receive(ack);
					byte [] incoming = ack.getData();
					AckPacket ackPacket = (AckPacket) Serializer.toObject(incoming);
					if (Math.random() > 0.05)
					{
						System.out.println("============ACK Received Seq no====================== "+ (ackPacket.getPacketno()));
						Sf = Math.max(Sf, ackPacket.getPacketno());
						System.out.println("======Timer started for packet============"+Sf);
						
						
					}
					else
					{
						System.out.println("=======================ACK Lost seq no ========================"+ (ackPacket.getPacketno()));
					}
					
					if (ackPacket.getPacketno() == numpackets || ackPacket.isLastOne())
					{
						System.out.println("=================All packets sent and recieved==================");
						break;
					}
						
						
						
					
					
				} catch (SocketTimeoutException e){
					
					System.out.println("^^^^^^^^^^^^^^^^^^^^^Timeout for packet^^^^^^^^^^^^^^^^^^^^^^^^ "+Sf);
					
					int j = Sf;
					while (j<Sn)
					{
						byte[] wrngData = "xyz".getBytes();
						sentPacket.setData(fileBytes);	
						sentPacket.setSeqNum(j);
						
						
						
						if (Sn == numpackets-1)
							sentPacket.setLast(true);
						if (Math.random() <= 0.1)
							sentPacket.setData(wrngData);
						toReceiver = new DatagramSocket();
						
						byte [] sentData = Serializer.toBytes(sentPacket);
						DatagramPacket packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
						System.out.println("^^^^^^^^^^^^^^^^ReSending packet with seqno^^^^^^^^^^^^^^^^^^^^^^^^ "+ j);
						if (j==Sf)
						{
							System.out.println("!!!!! Timer Started for packet ======== "+j);
						}
						toReceiver.send(packet);
						j++;
						Thread.sleep(3000);
						
					}
					
					
				}
				
				
			}
		}
		else
		
		if (protocol.equalsIgnoreCase("SR"))
		{
			while (true)
			{
				while (Sn-Sf <n && Sn<numpackets)
				{
					
					byte[] wrngData = "xyz".getBytes();
					sentPacket.setData(fileBytes);	
					sentPacket.setSeqNum(Sn);
					
					if (Sn == numpackets-1)
						sentPacket.setLast(true);
					if (Math.random() <= 0.1)
						sentPacket.setData(wrngData);
					toReceiver = new DatagramSocket();
					
					byte [] sentData = Serializer.toBytes(sentPacket);
					DatagramPacket packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
					System.out.println("===============Sending packet with seqno===================== "+ sentPacket.getSeqNum());
					if (Sn==0 && Sf==0)
						System.out.println("+++++++++Timer started for packet+++++++"+Sf);
					toReceiver.send(packet);
					Sn++;
					Thread.sleep(2500);					
					
				}
				System.out.println();
				byte[] ackBytes = new byte[1024];
				DatagramPacket ack = new DatagramPacket(ackBytes,ackBytes.length);
				ArrayList<Integer> sendBuf= new ArrayList<Integer>();
				try{
					toReceiver.setSoTimeout(tIMEOUT);
					
					toReceiver.receive(ack);
					byte [] incoming = ack.getData();
					AckPacket ackPacket = (AckPacket) Serializer.toObject(incoming);
					
					//last packet received
					
					if (ackPacket.getPacketno() == numpackets || ackPacket.isLastOne())
					{
						System.out.println("!!!!    All Packets Received at Receiver   !!!!!!!!");
						break;
					}
					
					if (Math.random() <= 0.05)
					{
						System.out.println("=======================ACK Lost  ==========================="+ (ackPacket.getPacketno()));						
					}
					else
					{

						System.out.println("==========================    ACK Received ===================="+(ackPacket.getPacketno()));
						
						//In Order receive
						if (Sf == ackPacket.getPacketno())
						{
							Sf++;
							if (!sendBuf.isEmpty())
							{
								int j = Sf;
								while(j<=Sn)
								{
									if (sendBuf.contains(i))
									{
										Sf++;
										sendBuf.removeAll(Collections.singleton(new Integer(i)));
										j++;										
									}
									else
									{
										break;
									}
								}
								
							}
							
						}
						else 
							sendBuf.add(ackPacket.getPacketno()-1);
												
					}
					
											
					
					
				} catch (SocketTimeoutException e){
					
					System.out.println("^^^^^^^^^^^^^^^^^^^^^   Timeout for packet   ^^^^^^^^^^^^^^^^^^^^^^^^ "+Sf);
					
					int j = Sf;
					
					byte[] wrngData = "xyz".getBytes();
					sentPacket.setData(fileBytes);	
					sentPacket.setSeqNum(j);
					
					
					
					if (Sn == numpackets-1)
						sentPacket.setLast(true);
					if (Math.random() <= 0.1)
						sentPacket.setData(wrngData);
					toReceiver = new DatagramSocket();
					
					byte [] sentData = Serializer.toBytes(sentPacket);
					DatagramPacket packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
					System.out.println("^^^^^^^^^^^^^^^^ReSending packet with seqno^^^^^^^^^^^^^^^^^^^^^^^^ "+ j);
					if (j==Sf)
					{
						System.out.println("================= Timer Started for packet ================ "+j);
					}
					toReceiver.send(packet);
					
					
				
			}
		}
		
		
		
		
		
	}	
	
	
	

}
}
