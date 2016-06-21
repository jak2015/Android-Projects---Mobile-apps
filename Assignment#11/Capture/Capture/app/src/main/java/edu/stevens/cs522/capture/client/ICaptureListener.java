package edu.stevens.cs522.capture.client;

public interface ICaptureListener {
	
	public void onCapture(String code, int dialogId);

	public void onCameraUnavailable(int dialogId);
	
	public void onDismiss(int dialogId);
	
}