package com.ilwllc.sgerke.azure_iot_getstarted;

import java.io.IOException;
import com.microsoft.azure.eventhubs.*;
import com.microsoft.azure.servicebus.*;

import java.nio.charset.Charset;
import java.time.*;
import java.util.function.*;

public class AzureIoTHub_ReadD2CMessages 
{
	//private static String connStr = "Endpoint=sb://ihsuprodblres035dednamespace.servicebus.windows.net/;EntityPath=iothub-ehub-bme280-144904-d5af800c99;SharedAccessKeyName=iothubowner;SharedAccessKey=YiOqj+HUB192bbS4Rb814ZeG9ndmIgoJhHAYPnwnuus=";
	private static String connStr;
	
	private static EventHubClient receiveMessages(final String partitionId)
	 {
	   EventHubClient client = null;
	   try {
	     client = EventHubClient.createFromConnectionStringSync(connStr);
	   }
	   catch(Exception e) {
	     System.out.println("Failed to create client: " + e.getMessage());
	     System.exit(1);
	   }
	   try {
	     client.createReceiver( 
	       EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,  
	       partitionId,  
	       Instant.now()).thenAccept(new Consumer<PartitionReceiver>()
	     {
	       public void accept(PartitionReceiver receiver)
	       {
	         System.out.println("** Created receiver on partition " + partitionId);
	         try {
	           while (true) {
	             Iterable<EventData> receivedEvents = receiver.receive(100).get();
	             int batchSize = 0;
	             if (receivedEvents != null)
	             {
	               for(EventData receivedEvent: receivedEvents)
	               {
	                 System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s", 
	                   receivedEvent.getSystemProperties().getOffset(), 
	                   receivedEvent.getSystemProperties().getSequenceNumber(), 
	                   receivedEvent.getSystemProperties().getEnqueuedTime()));
	                 System.out.println(String.format("| Device ID: %s", receivedEvent.getSystemProperties().get("iothub-connection-device-id")));
	                 System.out.println(String.format("| Message Payload: %s", new String(receivedEvent.getBody(),
	                   Charset.defaultCharset())));
	                 batchSize++;
	               }
	             }
	             System.out.println(String.format("Partition: %s, ReceivedBatch Size: %s", partitionId,batchSize));
	           }
	         }
	         catch (Exception e)
	         {
	           System.out.println("Failed to receive messages: " + e.getMessage());
	         }
	       }
	     });
	   }
	   catch (Exception e)
	   {
	     System.out.println("Failed to create receiver: " + e.getMessage());
	   }
	   return client;
	 }
	
	public static void main( String[] args ) throws IOException
    {
		//Arguments must be passed for Azure EventHubCompatibleEndPoint, EventHubCompatibleName, SharedAccessKeyName, and SharedAcessKey 
		if(args.length != 4){
			System.out.println("***** Missing arguments *****");
			return;
		}
		
		connStr = "Endpoint=" + args[0] + ";EntityPath=" + args[1] + ";SharedAccessKeyName=" + args[2] + ";SharedAccessKey=" + args[3];
		EventHubClient client0 = receiveMessages("0");
		EventHubClient client1 = receiveMessages("1");
		System.out.println("Press ENTER to exit.");
		System.in.read();
		try
		{
		  client0.closeSync();
		  client1.closeSync();
		  System.exit(0);
		}
		catch (ServiceBusException sbe)
		{
		  System.exit(1);
		}
    }
}
