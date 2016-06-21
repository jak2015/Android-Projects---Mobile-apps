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

package edu.stevens.cs522.capture.client.camera;

import java.io.IOException;

import edu.stevens.cs522.capture.util.CameraUtils;
import edu.stevens.cs522.capture.util.IContinue;
import edu.stevens.cs522.capture.PlanarYUVLuminanceSource;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The implementation
 * encapsulates the steps needed to take preview-sized images, which are used for both preview and decoding.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

	private static final String TAG = CameraManager.class.getSimpleName();

	private static final int MIN_FRAME_WIDTH = 240;
	private static final int MIN_FRAME_HEIGHT = 240;
	private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
	private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

	private final Context context;
	private final CameraConfigurationManager configManager;
	private Camera camera;
	private AutoFocusManager autoFocusManager;
	private Rect framingRect;
	private Rect framingRectInPreview;
	private boolean initialized;
	private boolean previewing;
	// private int requestedFramingRectWidth;
	// private int requestedFramingRectHeight;
	/**
	 * Preview frames are delivered here, which we pass on to the registered handler. Make sure to clear the handler so
	 * it will only receive one message.
	 */
	private final PreviewCallback previewCallback;

	public CameraManager(Context context) {
		this.context = context;
		this.configManager = new CameraConfigurationManager(context);
		previewCallback = new PreviewCallback(configManager);
	}

	/**
	 * Opens the camera driver and initializes the hardware parameters.
	 * 
	 * @param holder
	 *            The surface object which the camera will draw preview frames into.
	 * @throws IOException
	 *             Indicates the camera driver failed to open.
	 */
	public void openDriver(final SurfaceHolder holder, final Activity activity, final int cameraId, final IContinue<Boolean> callback)
			throws IOException {
		AsyncTask<Void, Void, Camera> bindCameraTask = new AsyncTask<Void, Void, Camera>() {

			@Override
			protected Camera doInBackground(Void... params) {
				synchronized (CameraManager.this) {
					try {
						Camera theCamera = CameraUtils.bindCamera(cameraId);
						if (theCamera == null) {
							throw new IOException("Failed to open the camera driver.");
						}
						theCamera.setPreviewDisplay(holder);
						camera = theCamera;

						if (!initialized) {
							initialized = true;
							configManager.initFromCameraParameters(theCamera);
						}

						Camera.Parameters parameters = theCamera.getParameters();
						String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these,
																										// temporarily
						try {
							configManager.setDesiredCameraParameters(theCamera, activity, false);
						} catch (RuntimeException re) {
							// Driver failed
							Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
							Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
							// Reset:
							if (parametersFlattened != null) {
								parameters = theCamera.getParameters();
								parameters.unflatten(parametersFlattened);
								try {
									theCamera.setParameters(parameters);
									configManager.setDesiredCameraParameters(theCamera, activity, true);
								} catch (RuntimeException re2) {
									// Well, darn. Give up
									Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
								}
							}
						}
						return theCamera;
					} catch (IOException e) {
						Log.e(TAG, "IO Exception while initializing the camera.", e);
						return null;
					}
				}
			}

			@Override
			protected void onPostExecute(Camera theCamera) {
				callback.kontinue(theCamera != null);
			}
		};
		
		bindCameraTask.execute();
		
//		Camera theCamera = camera;
//		if (theCamera == null) {
//			theCamera = OpenCameraInterface.open();
//			if (theCamera == null) {
//				throw new IOException();
//			}
//			camera = theCamera;
//		}

	}

	public synchronized boolean isOpen() {
		return camera != null;
	}

	/**
	 * Closes the camera driver if still in use.
	 */
	public synchronized void closeDriver() {
		if (camera != null) {
			camera.release();
			camera = null;
			// Make sure to clear these each time we close the camera, so that any scanning rect
			// requested by intent is forgotten.
			framingRect = null;
			framingRectInPreview = null;
		}
	}

	/**
	 * Asks the camera hardware to begin drawing preview frames to the screen.
	 */
	public synchronized void startPreview() {
		Camera theCamera = camera;
		if (theCamera != null && !previewing) {
			theCamera.startPreview();
			previewing = true;
			autoFocusManager = new AutoFocusManager(context, camera);
		}
	}

	/**
	 * Tells the camera to stop drawing preview frames.
	 */
	public synchronized void stopPreview() {
		if (autoFocusManager != null) {
			autoFocusManager.stop();
			autoFocusManager = null;
		}
		if (camera != null && previewing) {
			camera.stopPreview();
			previewCallback.setHandler(null, 0);
			previewing = false;
		}
	}

	/**
	 */
	public synchronized void setTorch(boolean newSetting) {
		if (newSetting != configManager.getTorchState(camera)) {
			if (camera != null) {
				if (autoFocusManager != null) {
					autoFocusManager.stop();
				}
				configManager.setTorch(camera, newSetting);
				if (autoFocusManager != null) {
					autoFocusManager.start();
				}
			}
		}
	}

	/**
	 * A single preview frame will be returned to the handler supplied. The data will arrive as byte[] in the
	 * message.obj field, with width and height encoded as message.arg1 and message.arg2, respectively.
	 * 
	 * @param handler
	 *            The handler to send the message to.
	 * @param message
	 *            The what field of the message to be sent.
	 */
	public synchronized void requestPreviewFrame(Handler handler, int message) {
		Log.d(TAG, "Requesting preview frame.");
		Camera theCamera = camera;
		if (theCamera != null && previewing) {
			// This is where we install the callback for the next preview frame.
			previewCallback.setHandler(handler, message);
			theCamera.setOneShotPreviewCallback(previewCallback);
		}
	}
	
	public Point getPreviewSize() {
		return configManager.getPreviewSize();
	}

	/**
	 * Calculates the framing rect which the UI should draw to show the user where to place the barcode. This target
	 * helps with alignment as well as forces the user to hold the device far enough away to ensure the image will be in
	 * focus.
	 * 
	 * @return The rectangle to draw on screen in window coordinates.
	 */
	public synchronized Rect getFramingRect() {
		if (framingRect == null) {
			if (camera == null) {
				return null;
			}
			Point previewSize = configManager.getPreviewSize();
			if (previewSize == null) {
				// Called early, before init even finished
				return null;
			}

			int width = findDesiredDimensionInRange(previewSize.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
			int height = findDesiredDimensionInRange(previewSize.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

			int leftOffset = (previewSize.x - width) / 2;
			int topOffset = (previewSize.y - height) / 2;
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
			Log.d(TAG, "Calculated framing rect: " + framingRect);
		}
		return framingRect;
	}

	private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
		// int dim = 5 * resolution / 8; // Target 5/8 of each dimension
		int dim = (int)(resolution * CameraConfigurationManager.PREVIEW_SIZE_RATIO);
		if (dim < hardMin) {
			return hardMin;
		}
		if (dim > hardMax) {
			return hardMax;
		}
		return dim;
	}

	/**
	 * Like {@link #getFramingRect} but coordinates are in terms of the preview frame, not UI / screen.
	 */
	public synchronized Rect getFramingRectInPreview() {
		if (framingRectInPreview == null) {
			framingRectInPreview = getFramingRect();
			Log.d(TAG, "Calculated framing rect in preview: " + framingRectInPreview);
		}
		return framingRectInPreview;
	}

//	/**
//	 * Allows third party apps to specify the scanning rectangle dimensions, rather than determine them automatically
//	 * based on screen resolution.
//	 * 
//	 * @param width
//	 *            The width in pixels to scan.
//	 * @param height
//	 *            The height in pixels to scan.
//	 */
//	public synchronized void setManualFramingRect(int width, int height) {
//		if (initialized) {
//			Point screenResolution = configManager.getScreenResolution();
//			if (width > screenResolution.x) {
//				width = screenResolution.x;
//			}
//			if (height > screenResolution.y) {
//				height = screenResolution.y;
//			}
//			int leftOffset = (screenResolution.x - width) / 2;
//			int topOffset = (screenResolution.y - height) / 2;
//			framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
//			if (Config.DEBUG) Log.d(TAG, "Calculated manual framing rect: " + framingRect);
//			framingRectInPreview = null;
//		} else {
//			requestedFramingRectWidth = width;
//			requestedFramingRectHeight = height;
//		}
//	}

	/**
	 * A factory method to build the appropriate LuminanceSource object based on the format of the preview buffers, as
	 * described by Camera.Parameters.
	 * 
	 * @param data
	 *            A preview frame.
	 * @param width
	 *            The width of the image.
	 * @param height
	 *            The height of the image.
	 * @return A PlanarYUVLuminanceSource instance.
	 */
	public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
		
		Rect rect = getFramingRectInPreview();
		if (rect == null) {
			return null;
		}
		// Go ahead and assume it's YUV rather than die.
		return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(),
				false);
		
	}

}
