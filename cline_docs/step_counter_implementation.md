# Step Counter Implementation Details and Troubleshooting

This document outlines the implementation details of the step counter feature in the PlannerActivity and provides troubleshooting guidance for common issues.

## 1. Sensor Usage

The application utilizes the `Sensor.TYPE_STEP_DETECTOR` to count individual steps. This sensor triggers an event each time a step is detected.

## 2. Sensor Registration and Lifecycle

- The `SensorManager` is initialized in `initStepCounter()`.
- The `SensorEventListener` is registered in `onResume()` and unregistered in `onPause()` to conserve battery and ensure proper lifecycle management.
- Robust checks are in place to verify sensor availability and handle cases where the `SensorManager` might be null.

## 3. Data Persistence

- Step count data (current steps, last reset day, step goal) is stored locally using `SharedPreferences` for quick access.
- This data is also synchronized with Firebase Firestore under the `users` collection to ensure data persistence across sessions and devices.

## 4. Daily Reset Logic

- The step count is reset to zero at the beginning of each new day. This is determined by comparing the current day of the year with the `lastResetDay` stored in `SharedPreferences` and Firestore.

## 5. Troubleshooting Sensor Issues

The "Failed to register step detector listener in onResume()" error can occur due to several reasons, even with correct code implementation. This often points to device-specific limitations or conflicts with other applications.

### Common Causes:

1.  **Sensor Temporarily Unavailable or Busy:**
    *   Some Android devices or emulators may have limitations on concurrent sensor access.
    *   Other fitness tracking applications running in the background might be holding exclusive access to the step detector sensor.

2.  **Device Lacks Step Detector Sensor:**
    *   While the app checks for sensor presence, some older or low-end devices may not have a dedicated hardware step detector.

### User Guidance for Sensor Issues:

-   **Close Other Fitness Apps:** If you experience issues with the step counter, ensure that no other applications that use step tracking (e.g., Google Fit, Samsung Health, third-party pedometers) are running in the background.
-   **Restart the Application:** Sometimes, simply restarting the PlannerActivity or the entire application can resolve temporary sensor conflicts.
-   **Check Device Compatibility:** Verify that your device has a hardware step detector sensor. This can often be found in your device's specifications or by checking sensor testing apps.
-   **Monitor Sensor Status:** The application provides a "Sensor Status" indicator on the Planner screen.
    *   **"Sensor Status: Active"**: The step detector is working correctly.
    *   **"Sensor Status: Busy - Close other fitness apps"**: Another application might be using the sensor.
    *   **"Sensor Unavailable: Device lacks step detector"**: Your device does not have the necessary hardware.

## 6. Future Enhancements

-   Consider implementing a retry mechanism for sensor registration with a back-off strategy.
-   Explore using `Sensor.TYPE_STEP_COUNTER` as an alternative or fallback if `TYPE_STEP_DETECTOR` continues to be problematic on certain devices.
