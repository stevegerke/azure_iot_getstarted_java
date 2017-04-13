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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class BME280_Stream_AzureIoTHub 
{
	//private static String connString = "HostName=BME280.azure-devices.net;DeviceId=bme280;SharedAccessKey=lmNRvnW1ce00cvhP6PBzJA==";
	private static String connString;
	private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
	private static String deviceId;
	private static DeviceClient client;
	private static Integer sleepSeconds;
	 
	private static class TelemetryDataPoint {
		   public String deviceId;
		   public double fahrenheit;

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

		      while (true) {
		    	  BME280_I2CBus bme280 = new BME280_I2CBus();
		    	  String[] bme280_value = bme280.value().split(", ");
		    	  TelemetryDataPoint telemetryDataPoint = new TelemetryDataPoint();
		    	  telemetryDataPoint.deviceId = deviceId;
		    	  telemetryDataPoint.fahrenheit = Double.parseDouble(bme280_value[5]);
		    	  
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
		        Thread.sleep(sleepSeconds);
		      }
		    } catch (InterruptedException e) {
		      System.out.println("Finished.");
		    } catch (Exception e) {
		    	System.out.println("bme280 exception");
				e.printStackTrace();
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
