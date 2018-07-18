package com.rolamix.plugins.audioplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class Proximity implements SensorEventListener {
  private AudioManager audioManager;
  private SensorManager sensorManager;
  private Sensor proximitySensor;
  private PowerManager powerManager;
  private WakeLock wakeLock;
  
  private boolean isPlaying = false;
  
  public static String TAG = "Proximity";
  
  public Proximity(Context context) {
    powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
    wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
    
    sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
    proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    
    audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    AudioDeviceCallback mAudioDeviceCallback = createAudioDeviceCallback();
    mAudioDeviceCallback.onAudioDevicesAdded(audioManager.getDevices(audioManager.GET_DEVICES_OUTPUTS));
    audioManager.registerAudioDeviceCallback(mAudioDeviceCallback, null);
  }
  
  public void setPlaying(boolean _isPlaying) {
    isPlaying = _isPlaying;
    changeSensorState();
  }
  
  private void changeSensorState() {
    if (isHeadsetPluggedIn()) {
      inactiveSensor();
    } else if (isPlaying == true) {
      activeSensor();
    } else {
      inactiveSensor();
    }
  }
  
  private void activeSensor() {
    sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
  }
  
  private void inactiveSensor() {
    sensorManager.unregisterListener(this, proximitySensor);
  }
  
  private boolean isContainsHeadphone(AudioDeviceInfo[] devices) {
    for (int i = 0; i < devices.length; i++) {
      AudioDeviceInfo device = devices[i];
      if (!device.isSink())
      continue;
      if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
      || device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
      || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
      || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
      || device.getType() == AudioDeviceInfo.TYPE_USB_HEADSET) {
        return true;
      }
    }
    return false;
  }
  
  private boolean isHeadsetPluggedIn() {
    AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
    return isContainsHeadphone(devices);
  }
  
  public void updateDevices(boolean isNewDevices, AudioDeviceInfo[] devices) {
    changeSensorState();
  }
  
  private AudioDeviceCallback createAudioDeviceCallback() {
    return new AudioDeviceCallback() {
      @Override
      public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
        updateDevices(true, addedDevices);
      }
      
      @Override
      public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
        updateDevices(false, removedDevices);
      }
    };
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    float distance = event.values[0];
    if (distance < proximitySensor.getMaximumRange()) {
      //near
      wakeLock.acquire();
    } else if (wakeLock.isHeld()) {
      //far
      wakeLock.release();
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    
  }
}
