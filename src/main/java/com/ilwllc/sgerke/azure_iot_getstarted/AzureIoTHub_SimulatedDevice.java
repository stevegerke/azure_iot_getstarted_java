package com.ilwllc.sgerke.azure_iot_getstarted;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;

public class AzureIoTHub_SimulatedDevice 
{
	//private static String connString = "HostName={youriothubname}.azure-devices.net;DeviceId=myFirstJavaDevice;SharedAccessKey={yourdevicekey}";
	private static String connString;
	private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
	private static String deviceId = "myFirstJavaDevice";
	private static DeviceClient client;
	private static Integer sleepSeconds;
	 
	private static class TelemetryDataPoint {
		   public String deviceId;
		   public double windSpeed;

		   public String serialize() {
		     Gson gson = new Gson();
		     return gson.toJson(this);
		   }
		 }
	
	private static class EventCallback implements IotHubEventCallback
	 {
	   public void execute(IotHubStatusCode status, Object context) {
	     System.out.println("IoT Hub responded to message with status: " + status.name());

	     if (context != null) {
	       synchronized (context) {
	         context.notify();
	       }
	     }
	   }
	 }
	
	private static class MessageSender implements Runnable {

		  public void run()  {
		    try {
		      double avgWindSpeed = 10; // m/s
		      Random rand = new Random();

		      while (true) {
		        double currentWindSpeed = avgWindSpeed + rand.nextDouble() * 4 - 2;
		        TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
		        telemetryDataPoint.deviceId = deviceId;
		        telemetryDataPoint.windSpeed = currentWindSpeed;

		        String msgStr = telemetryDataPoint.serialize();
		        Message msg = new Message(msgStr);
		        System.out.println("Sending: " + msgStr);

		        Object lockobj = new Object();
		        EventCallback callback = new EventCallback();
		        client.sendEventAsync(msg, callback, lockobj);

		        synchronized (lockobj) {
		          lockobj.wait();
		          
		        TimeUnit.SECONDS.sleep(sleepSeconds);
		        }
		        Thread.sleep(1000);
		      }
		    } catch (InterruptedException e) {
		      System.out.println("Finished.");
		    }
		  }
		}

	public static void main( String[] args ) throws IOException, URISyntaxException {
		  
		  //Arguments must be passed for Azure HostName, Azure DeviceId, Azure SharedAcessKey. and sleepSeconds (between messages) 
		  if(args.length != 4){
			  System.out.println("***** Missing arguments *****");
			  return;
		  }
		  
		  connString = "HostName=" + args[0] + ";DeviceId=" + args[1] + ";SharedAccessKey=" + args[2];
		  deviceId = args[1];
		  sleepSeconds = Integer.parseInt(args[3]);
		  
		  client = new DeviceClient(connString, protocol);
		  client.open();

		  MessageSender sender = new MessageSender();

		  ExecutorService executor = Executors.newFixedThreadPool(1);
		  executor.execute(sender);

		  System.out.println("Press ENTER to exit.");
		  System.in.read();
		  executor.shutdownNow();
		  client.close();
		}
}
