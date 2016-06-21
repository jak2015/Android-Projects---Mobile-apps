/*
 * Copyright (C) 2010 ZXing authors
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to configure the camera
 * hardware.
 */
final class CameraConfigurationManager {

	private static final String TAG = "CameraConfiguration";

	// This is bigger than the size of a small screen, which is still supported. The routine
	// below will still select the default (presumably 320x240) size for these. This prevents
	// accidental selection of very low resolution on some devices.
	private static final int MIN_FPS = 5;
	
	private static final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen
	
	public static final double PREVIEW_SIZE_RATIO = 5.0 / 8;
	public static final double PREVIEW_SIZE_INVERSE_RATIO = 800 / 5;

	private final Context context;
	private Point screenResolution;
	private Point cameraResolution;
	private Point previewSize;
	
	CameraConfigurationManager(Context context) {
	    this.context = context.getApplicationContext();
	}

	/**
	 * Reads, one time, values from the camera that are needed by the app.
	 */
	void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		
		Display display = manager.getDefaultDisplay();
		Point theScreenResolution = new Point();
		display.getSize(theScreenResolution);
		screenResolution = theScreenResolution;
				
		Log.d(TAG, "Screen resolution: " + screenResolution);
		cameraResolution = findBestPreviewSizeValue(parameters, screenResolution);
		Log.d(TAG, "Camera resolution: " + cameraResolution);
	}

	void setDesiredCameraParameters(final Camera camera,Activity activity,
			boolean safeMode) {

		final Camera.Parameters parameters = camera.getParameters();

		if (parameters == null) {
			Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
			return;
		}

		Log.d(TAG, "Initial camera parameters: " + parameters.flatten());

		if (safeMode) {
			Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
		}

		setBestPreviewFPS(parameters);

		String focusMode = null;
		if (safeMode) {
			focusMode = findSettableValue(parameters.getSupportedFocusModes(), 
										  Camera.Parameters.FOCUS_MODE_AUTO);
		} else {
			focusMode = findSettableValue(parameters.getSupportedFocusModes(),
										  Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, 
										  Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
										  Camera.Parameters.FOCUS_MODE_AUTO);
		}
		// }
		// Maybe selected auto-focus but not available, so fall through here:
		if (!safeMode && focusMode == null) {
			focusMode = findSettableValue(parameters.getSupportedFocusModes(), 
										  Camera.Parameters.FOCUS_MODE_MACRO,
										  Camera.Parameters.FOCUS_MODE_EDOF);
		}
		if (focusMode != null) {
			parameters.setFocusMode(focusMode);
		}
		
		if (!safeMode) {
			MeteringInterface.setFocusArea(parameters);
			MeteringInterface.setMetering(parameters);
		}		   

		// setZoom(parameters);

		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);

		camera.setParameters(parameters);

		Camera.Parameters afterParameters = camera.getParameters();
		Camera.Size afterSize = afterParameters.getPreviewSize();
		if (afterSize != null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
			Log.w(TAG, "Camera said it supported preview size " + cameraResolution.x + 'x' + cameraResolution.y
					+ ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
			cameraResolution.x = afterSize.width;
			cameraResolution.y = afterSize.height;
		}


	}
	
	@SuppressWarnings("unused")
	private void setZoom(Camera.Parameters parameters) {
		if (parameters.isZoomSupported()) {
			List<Integer> zooms = parameters.getZoomRatios();
			for (int ix = 1; ix < zooms.size(); ix++) {
				if (zooms.get(ix-1) < PREVIEW_SIZE_INVERSE_RATIO && zooms.get(ix) >= PREVIEW_SIZE_INVERSE_RATIO) {
					parameters.setZoom(ix);
					Log.d(TAG, "Setting zoom level to "+zooms.get(ix));
					return;
				}
			}
			throw new IllegalStateException("Insufficient zoom level supported for this camera.");
		}
		throw new IllegalArgumentException("No zoom supported for this camera.");
	}

	Point getCameraResolution() {
		return cameraResolution;
	}

	Point getScreenResolution() {
		return screenResolution;
	}
	
	Point getPreviewSize() {
		return previewSize;
	}

	boolean getTorchState(Camera camera) {
		if (camera != null) {
			Camera.Parameters parameters = camera.getParameters();
			if (parameters != null) {
				String flashMode = camera.getParameters().getFlashMode();
				return flashMode != null
						&& (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) || 
							Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
			}
		}
		return false;
	}

	void setTorch(Camera camera, boolean newSetting) {
		Camera.Parameters parameters = camera.getParameters();
		doSetTorch(parameters, newSetting, false);
		camera.setParameters(parameters);
	}

	private void doSetTorch(Camera.Parameters parameters, boolean newSetting, boolean safeMode) {
		String flashMode;
		if (newSetting) {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(), 
										  Camera.Parameters.FLASH_MODE_TORCH,
										  Camera.Parameters.FLASH_MODE_ON);
		} else {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(), 
										  Camera.Parameters.FLASH_MODE_OFF);
		}
		if (flashMode != null) {
			parameters.setFlashMode(flashMode);
		}
	}

	private static void setBestPreviewFPS(Camera.Parameters parameters) {
		// Required for Glass compatibility; also improves battery/CPU performance a tad
		List<int[]> supportedPreviewFpsRanges = parameters.getSupportedPreviewFpsRange();
		Log.d(TAG, "Supported FPS ranges: " + toString(supportedPreviewFpsRanges));
		if (supportedPreviewFpsRanges != null && !supportedPreviewFpsRanges.isEmpty()) {
			int[] minimumSuitableFpsRange = null;
			for (int[] fpsRange : supportedPreviewFpsRanges) {
				int fpsMax = fpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
				if (fpsMax >= MIN_FPS * 1000
						&& (minimumSuitableFpsRange == null || fpsMax > minimumSuitableFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX])) {
					minimumSuitableFpsRange = fpsRange;
				}
			}
			if (minimumSuitableFpsRange == null) {
				Log.d(TAG, "No suitable FPS range?");
			} else {
				int[] currentFpsRange = new int[2];
				parameters.getPreviewFpsRange(currentFpsRange);
				if (!Arrays.equals(currentFpsRange, minimumSuitableFpsRange)) {
					Log.d(TAG, "Setting FPS range to " + Arrays.toString(minimumSuitableFpsRange));
					parameters.setPreviewFpsRange(minimumSuitableFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
							minimumSuitableFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
				}
			}
		}
	}

	// Actually prints the arrays properly:
	private static String toString(Collection<int[]> arrays) {
		if (arrays == null || arrays.isEmpty()) {
			return "[]";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append('[');
		Iterator<int[]> it = arrays.iterator();
		while (it.hasNext()) {
			buffer.append(Arrays.toString(it.next()));
			if (it.hasNext()) {
				buffer.append(", ");
			}
		}
		buffer.append(']');
		return buffer.toString();
	}

	private Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {

		List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
		if (rawSupportedSizes == null) {
			Log.w(TAG, "Device returned no supported preview sizes; using default");
			Camera.Size defaultSize = parameters.getPreviewSize();
			previewSize = new Point(defaultSize.width, defaultSize.height);
			return previewSize;
		}

		// Sort by size, descending
		List<Camera.Size> supportedPreviewSizes = new ArrayList<Size>(rawSupportedSizes);
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size a, Camera.Size b) {
				int aPixels = a.height * a.width;
				int bPixels = b.height * b.width;
				if (bPixels < aPixels) {
					return -1;
				}
				if (bPixels > aPixels) {
					return 1;
				}
				return 0;
			}
		});

		// Remove sizes that are unsuitable
		Iterator<Camera.Size> it = supportedPreviewSizes.iterator();
		while (it.hasNext()) {
			Camera.Size supportedPreviewSize = it.next();
			int realWidth = supportedPreviewSize.width;
			int realHeight = supportedPreviewSize.height;
			if (realWidth * realHeight < MIN_PREVIEW_PIXELS) {
				it.remove();
				continue;
			}

			boolean isCandidatePortrait = realWidth < realHeight;
			int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
			int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
			if (maybeFlippedWidth > screenResolution.x || maybeFlippedHeight > screenResolution.y) {
				it.remove();
			}
		}

	    if (Log.isLoggable(TAG, Log.INFO)) {
			StringBuilder previewSizesString = new StringBuilder();
			for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
				previewSizesString.append(supportedPreviewSize.width).append('x').append(supportedPreviewSize.height)
						.append(' ');
			}
			Log.d(TAG, "Supported preview sizes: " + previewSizesString);
		}

		// If more than one preview size, pick second largest.
		if (supportedPreviewSizes.size() > 1) {
			Camera.Size largestPreview = supportedPreviewSizes.get(1);
			previewSize = new Point(largestPreview.width, largestPreview.height);
			Log.d(TAG, "Using second largest suitable preview size: " + previewSize);
			return previewSize;
		}

		// Otherwise return current preview size
		Camera.Size defaultPreview = parameters.getPreviewSize();
		previewSize = new Point(defaultPreview.width, defaultPreview.height);
		Log.d(TAG, "No suitable preview sizes, using default: " + previewSize);
		return previewSize;
	}

	private static String findSettableValue(Collection<String> supportedValues, String... desiredValues) {
		Log.d(TAG, "Supported values: " + supportedValues);
		String result = null;
		if (supportedValues != null) {
			for (String desiredValue : desiredValues) {
				if (supportedValues.contains(desiredValue)) {
					result = desiredValue;
					break;
				}
			}
		}
		Log.d(TAG, "Settable value: " + result);
		return result;
	}
	
}
