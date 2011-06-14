package manyminds.history;

public class PictureResource implements Resource {

	private byte[] myData;

	public PictureResource() {
		setData(new byte[1]);
	}

	public PictureResource(byte[] data) {
		setData(data);
	}

	public byte[] getData() {
		byte[] retVal = new byte[myData.length];
		System.arraycopy(myData, 0, retVal, 0, myData.length);
		return retVal;
	}

	public void setData(byte[] data) {
		myData = new byte[data.length];
		System.arraycopy(data, 0, myData, 0, data.length);
	}

	public void spawnViewer() {
	}

}