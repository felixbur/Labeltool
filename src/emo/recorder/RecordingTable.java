package emo.recorder;

public interface RecordingTable {

	public void fireTableDataChanged();
	
	public void insertRecording(Recording r);
	public void deleteRecording(int row);
	public void clear();
	public Recording getRecordingAtRow(int row);
}
