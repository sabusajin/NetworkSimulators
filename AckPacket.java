

import java.io.Serializable;

public class AckPacket implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int packetno;
	boolean lastOne = false;
	

	public boolean isLastOne() {
		return lastOne;
	}

	public void setLastOne(boolean lastOne) {
		this.lastOne = lastOne;
	}

	public int getPacketno() {
		return packetno;
	}

	public void setPacketno(int packetno) {
		this.packetno = packetno;
	}
	

}
