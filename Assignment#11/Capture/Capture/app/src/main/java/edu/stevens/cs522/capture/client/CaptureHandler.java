/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.stevens.cs522.capture.client;

import edu.stevens.cs522.capture.Result;
import edu.stevens.cs522.capture.client.camera.CameraManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureHandler extends Handler {

	@SuppressWarnings("unused")
	private static final String TAG = CaptureHandler.class.getSimpleName();
	
	public interface ICaptureClient {
		public void drawViewfinder();
		public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor);
		public ViewfinderView getViewfinderView();
		public CameraManager getCameraManager();
		public Handler getCaptureHandler();
	}

	// Events for the capture state machine.
	public static final int DECODE = 1;
	public static final int RESTART_PREVIEW = 2;
	public static final int DECODE_SUCCEEDED = 3;
	public static final int DECODE_FAILED = 4;
	
	// Used to stop the decode thread.
	public static final int QUIT = 5;

	private final ICaptureClient client;
	private final DecodeThread decodeThread;
	private State state;
	private final CameraManager cameraManager;

	private enum State {
		PREVIEW, SUCCESS, DONE
	}

	CaptureHandler(ICaptureClient client, CameraManager cameraManager) {
		this.client = client;
		decodeThread = new DecodeThread(client);
		decodeThread.start();
		state = State.SUCCESS;

		// Start ourselves capturing previews and decoding.
		this.cameraManager = cameraManager;
		cameraManager.startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case RESTART_PREVIEW:
			restartPreviewAndDecode();
			break;
		case DECODE_SUCCEEDED:
			state = State.SUCCESS;
			Bundle bundle = message.getData();
			Bitmap barcode = null;
			float scaleFactor = 1.0f;
			if (bundle != null) {
				byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
				if (compressedBitmap != null) {
					barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
					// Mutable copy:
					barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
				}
				scaleFactor = bundle.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
			}
			client.handleDecode((Result) message.obj, barcode, scaleFactor);
			break;
		case DECODE_FAILED:
			// We're decoding as fast as possible, so when one decode fails, start another.
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(decodeThread.getHandler(), DECODE);
			break;
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		cameraManager.stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), QUIT);
		quit.sendToTarget();
		try {
			// Wait at most half a second; should be enough time, and onPause() will timeout quickly
			decodeThread.join(500L);
		} catch (InterruptedException e) {
			// continue
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(DECODE_SUCCEEDED);
		removeMessages(DECODE_FAILED);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(decodeThread.getHandler(), DECODE);
			client.drawViewfinder();
		}
	}

}
