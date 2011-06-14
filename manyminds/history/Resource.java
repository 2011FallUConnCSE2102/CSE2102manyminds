package manyminds.history;

public interface Resource {
	public void spawnViewer();

	public byte[] getData();

	public void setData(byte[] data);
}