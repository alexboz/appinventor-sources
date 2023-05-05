package com.google.appinventor.components.runtime;

import android.provider.Settings;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

@SimpleObject(external = true)
@DesignerComponent(
		version = 1,
		category = ComponentCategory.EXTENSION,
		description = "Connects to an ESP32 over MQTT to control its GPIO pins",
		iconName = "images/extension.png",
		nonVisible = true
)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "org.eclipse.paho.client.mqttv3-1.0.2.jar, org.eclipse.paho.android.service-1.0.2.jar")
public class ESP32IoT extends AndroidNonvisibleComponent implements Component {

	private static final String MQTT_TOPIC = "mit-appinventor/esp32/internal/";
	private String mqttTopic = MQTT_TOPIC;

	public ESP32IoT(ComponentContainer componentContainer) {
		super(componentContainer.$form());
	}

	private MqttClient mqttClient;
	private MemoryPersistence mqttMemoryPersistence = new MemoryPersistence();

	@SimpleFunction(description = "Establish connection to an MQTT broker")
	public void Connect(String host, int port, String identifier) {

		mqttTopic += identifier;

		try {
			mqttClient = new MqttClient(String.format("tcp://%s:%d", host, port), Settings.Secure.ANDROID_ID, mqttMemoryPersistence);
			mqttClient.connect();
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}

	}

	@SimpleFunction(description = "Set the state of a digital output pin")
	public void DigitalWrite(int pin, boolean state) {
		publishMessage(buildJSONMessage("digitalWrite", pin, state));
	}

	private String buildJSONMessage(String operation, int pin, boolean state){
		return String.format("{\"operation\":\"%s\",\"pin\":%d,\"state\":%b}", operation, pin, state);
	}

	private void publishMessage(String message) {
		if(mqttClient.isConnected()){
			try {
				mqttClient.publish(mqttTopic, message.getBytes(), 2, false);
			} catch (MqttException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
