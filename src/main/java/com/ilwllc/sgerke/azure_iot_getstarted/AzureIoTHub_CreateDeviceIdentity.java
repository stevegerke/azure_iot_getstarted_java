package com.ilwllc.sgerke.azure_iot_getstarted;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;

import java.io.IOException;
import java.net.URISyntaxException;
 
public class AzureIoTHub_CreateDeviceIdentity 
{
	//private static final String connectionString = "HostName=BME280.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=YiOqj+HUB192bbS4Rb814ZeG9ndmIgoJhHAYPnwnuus=";
	private static String connectionString;
	//private static final String deviceId = "bme280";
	private static String deviceId;

	public static void main( String[] args ) throws IOException, URISyntaxException, Exception
    {
		System.out.println(args[0]);
		System.out.println(args[1]);
		System.out.println(args[2]);
		System.out.println(args[3]);
		
		 //Arguments must be passed for Azure HostName, Azure SharedAcessKeyName, and Azure SharedAcessKey 
		 if(args.length != 4){
			 System.out.println("***** Missing arguments *****");
			 return;
		 }
		  
		 connectionString = "HostName=" + args[0] + ";SharedAccessKeyName=" + args[1] + ";SharedAccessKey=" + args[2];
		 RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

		 deviceId = args[3];
		 Device device = Device.createFromId(deviceId, null, null);
		 try {
		   device = registryManager.addDevice(device);
		 } catch (IotHubException iote) {
		   try {
		     device = registryManager.getDevice(deviceId);
		   } catch (IotHubException iotf) {
		     iotf.printStackTrace();
		   }
		 }
		 System.out.println("Device ID: " + device.getDeviceId());
		 System.out.println("Device key: " + device.getPrimaryKey());
    }
}
