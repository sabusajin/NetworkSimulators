import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

public class Receiver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int portNo = 0;
		String abc = null;
		DatagramSocket socket = null;
		if (args.length==1)
			portNo = Integer.parseInt(args[0]);
		else
			System.out.println("WRONG PARAMETERS!!!");
		
		
		
		try{
			
			socket = new DatagramSocket(portNo);
			byte [] buffer = new byte [64];
		
			DatagramPacket initial = new DatagramPacket(buffer, buffer.length);
			socket.receive(initial);
			
			buffer = initial.getData();
			
			abc = new String (buffer,0,3);			
			System.out.println(abc.substring(0, 2));
			
			if (abc.equalsIgnoreCase("GBN")||abc.substring(0, 2).equalsIgnoreCase("SR"))
			{
				byte [] x = "200".getBytes();
				InetAddress receiverAddress = InetAddress.getByName("localhost");
				DatagramPacket dp = new DatagramPacket(x, x.length, receiverAddress, initial.getPort());
				
							
				socket.send(dp);	
	
			}
			else
			{
				byte [] x = "404".getBytes();
				InetAddress receiverAddress = InetAddress.getByName("localhost");
				DatagramPacket dp = new DatagramPacket(x, x.length, receiverAddress, initial.getPort());
				
							
				socket.send(dp);				
			}
			
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		try{
			if (abc.equalsIgnoreCase("GBN"))
			{
			gbn(socket);
			}
			else if (abc.substring(0,2).equalsIgnoreCase("SR"))
			{
				sr(socket);
			}
			

		}catch (Exception e)
		{
			e.printStackTrace();
		}

		


	}

	

	private static void gbn(DatagramSocket socket) throws IOException, ClassNotFoundException{
		// TODO Auto-generated method stub
		
		byte[] incoming = new byte[2048];
		int i, j;
		boolean finalPacket = false;
		int Rn = 0;
		System.out.println("!!!!!!!!!!!!!!!!! Receiver set to receive data !!!!!!!!!!!");
		System.out.println("----------------------------------------------------------");
		System.out.println();
		
		while (finalPacket == false) {
			
			
			//Receive incoming packet from gbn sender
			DatagramPacket rcvPacket = new DatagramPacket(incoming, incoming.length);

			socket.receive(rcvPacket);
			
			//buffer to store the data received
			byte[] data = rcvPacket.getData();
			
			//flag to check for checksum errors
			int flag = 0, wrngflag = 0;
			
			//convert incoming data to object			
			Packet dataPacket = (Packet) Serializer.toObject(data);
			
			
			System.out.println(" ================ Packet received - Sequence number ============= " + dataPacket.getSeqNum());
			
			
			//Check for checksum errors
			//data[i]+chksum[i] == -1 (1111 1111) binary
			
			for (i =0; i<dataPacket.getData().length;i++)
			{
				
				if (dataPacket.getData()[i] + dataPacket.getChksum()[i] != -1)
					flag = 1;
			}
			
			// Received in order with no data errors

			if (dataPacket.getSeqNum() == Rn && flag == 0 && !dataPacket.isLast()) {
				Rn++;
				wrngflag = 0;
			}
			
			//Checksum error

			else if (flag == 1) {
				System.out.println("----------------CHecksum Error-----------------");
				System.out.println();
				wrngflag = 1;
			}
			
			//Last packet
			else if (dataPacket.getSeqNum() == Rn && flag == 0 && dataPacket.isLast()) {

				Rn++;
				System.out.println("==============Final Packet==================");
				System.out.println();
				finalPacket = true;
				wrngflag = 0;
				

			}
			//out of order packet


			else {
				System.out.println(" |||||   Out of order Packet Discarded   ||||||");
				System.out.println();
				wrngflag = 1;
			}
			
			//Set up Acknowledgement packet

			InetAddress receiverAddress = InetAddress.getByName("localhost");
			int port = rcvPacket.getPort();
			AckPacket ackData = new AckPacket();
			ackData.setPacketno(Rn);	
			ackData.setLastOne(finalPacket);		
			byte [] sentData = Serializer.toBytes(ackData);
			DatagramPacket packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
			
			// Ack for In order packet			

			if (Math.random() > 0.1 && wrngflag == 0) {
				System.out.println("====================Ack Sent - expecting seq no ===============" + ackData.getPacketno());
				System.out.println();
				socket.send(packet);
			} // duplicate Ack for lost packet
			
			
			else if (Math.random() <= 0.1 && wrngflag == 0) {
				System.out.println("~~~~~~~~~~~~~ Packet Lost ~~~~~~~~~~~~~~~~~");
				System.out.println();
				Rn--;
				ackData.setPacketno(Rn);
			
				sentData = Serializer.toBytes(ackData);
				packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
				socket.send(packet);
				finalPacket = false;				

			}
			
			// Out of order / wrong checksum packet
			else {
				ackData.setPacketno(Rn);
			
				sentData = Serializer.toBytes(ackData);
				packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
				socket.send(packet);

			}

		}

	}
	
	private static void sr (DatagramSocket socket) throws IOException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub

		//System.out.println("Reached SR!!");
		
		byte[] incoming = new byte[2048];
		int i, j;
		boolean finalPacket = false;
		int Rn = 0;
		ArrayList <Integer> buf= new ArrayList<>();
		System.out.println("!!!!!!!!!!!!!!!!! Receiver set to receive data !!!!!!");
		System.out.println("----------------------------------------------------------");
		System.out.println();
		
		while (finalPacket == false) {
			
			
			//Receive incoming packet from gbn sender
			DatagramPacket rcvPacket = new DatagramPacket(incoming, incoming.length);
			socket.receive(rcvPacket);
			
			//buffer to store the data received
			byte[] data = rcvPacket.getData();
			
			//flag to check for checksum errors
			int flag = 0, wrngflag = 0;
			
			//convert incoming data to object			
			Packet dataPacket = (Packet) Serializer.toObject(data);
			
			System.out.println(" ============== Packet received - Sequence number =========== " + dataPacket.getSeqNum());
			
			
			//Check for checksum errors
			//data[i]+chksum[i] == -1 (1111 1111) binary
			
			

			
			
			for (i =0; i<dataPacket.getData().length;i++)
			{
				
				if (dataPacket.getData()[i] + dataPacket.getChksum()[i] != -1)
					flag = 1;
			}
			
			//Set up Acknowledgement packet
			
			InetAddress receiverAddress = InetAddress.getByName("localhost");
			int port = rcvPacket.getPort();
			AckPacket ackData = new AckPacket();
			ackData.setPacketno(Rn);			
			byte [] sentData = Serializer.toBytes(ackData);
			DatagramPacket packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
			
			if (flag == 1)
			{
				System.out.println("!!!!!!!!!!!!!Checksum Error!!!!!!!!!!! for packet"+dataPacket.getSeqNum());
				System.out.println();
				wrngflag = 1;
				ackData = new AckPacket();
				ackData.setPacketno(Rn);			
				sentData = Serializer.toBytes(ackData);
				packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
				socket.send(packet);
			}
			else if (dataPacket.getSeqNum() == Rn && flag == 0 && !dataPacket.isLast())
			{
				if (buf.isEmpty())
				{
					Rn++;
					ackData = new AckPacket();
					ackData.setPacketno(Rn);			
					sentData = Serializer.toBytes(ackData);
					packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
					System.out.println("ACK sent ============== "+Rn);
					System.out.println();
					socket.send(packet);
										
				}
				else
				{
					for (j = Rn; j < Rn+dataPacket.window; j++)
					{
						if (buf.contains(j)){
							Rn++;
							buf.removeAll(Collections.singleton(new Integer(j)));
							ackData = new AckPacket();
							ackData.setPacketno(Rn);			
							sentData = Serializer.toBytes(ackData);
							packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
							socket.send(packet);
							
														
						}
						else {
							ackData = new AckPacket();
							ackData.setPacketno(Rn);			
							sentData = Serializer.toBytes(ackData);
							packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
							socket.send(packet);
							break;
						}
					}
				}
				
				
			}
			else if (dataPacket.getSeqNum()>Rn && flag ==0 && !dataPacket.isLast())
			{
				
				System.out.println("Out of Order===========Stored in buffer!!!!!!!!!");
				System.out.println();
				
				if (!buf.contains(dataPacket.getSeqNum()))
					buf.add(dataPacket.getSeqNum());
				ackData = new AckPacket();
				ackData.setPacketno(dataPacket.getSeqNum());			
				sentData = Serializer.toBytes(ackData);
				packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
				socket.send(packet);
				
			}
			
			else if (dataPacket.getSeqNum()==Rn && flag ==0 && dataPacket.isLast())
			{
				finalPacket =true;
				System.out.println("=====================Last packet received===============");
				ackData = new AckPacket();
				ackData.setPacketno(dataPacket.getSeqNum());	
				ackData.setLastOne(true);	
				sentData = Serializer.toBytes(ackData);
				packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
				socket.send(packet);


			}
			else if (dataPacket.getSeqNum()<Rn && flag == 0)
			{
				
				System.out.println();
				
				ackData = new AckPacket();
				ackData.setPacketno(dataPacket.getSeqNum());			
				sentData = Serializer.toBytes(ackData);
				packet = new DatagramPacket(sentData, sentData.length, receiverAddress, port );
				socket.send(packet);
				
			}
			
			
			
		}
		
		
		
		
		
	}
		
		
		
	

}

