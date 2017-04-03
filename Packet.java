
import java.io.Serializable;

public class Packet implements Serializable{
	
	
	public byte[] data;
	public byte[] chksum;
	public int seqNum;
	public boolean last;
	public int window;
	
	
	public boolean isLast() {
		return last;
	}
	public void setLast(boolean last) {
		this.last = last;
	}
	public int getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public byte[] getChksum() {
		return chksum;
	}
	public void setChksum(byte[] chksum) {
		this.chksum = chksum;
	}
	
	

}
