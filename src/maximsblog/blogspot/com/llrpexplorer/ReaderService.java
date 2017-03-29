package maximsblog.blogspot.com.llrpexplorer;

import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.generated.enumerations.AISpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.AccessReportTriggerType;
import org.llrp.ltk.generated.enumerations.AirProtocols;
import org.llrp.ltk.generated.enumerations.ConnectionAttemptStatusType;
import org.llrp.ltk.generated.enumerations.GetReaderCapabilitiesRequestedData;
import org.llrp.ltk.generated.enumerations.NotificationEventType;
import org.llrp.ltk.generated.enumerations.ROReportTriggerType;
import org.llrp.ltk.generated.enumerations.ROSpecStartTriggerType;
import org.llrp.ltk.generated.enumerations.ROSpecState;
import org.llrp.ltk.generated.enumerations.ROSpecStopTriggerType;
import org.llrp.ltk.generated.interfaces.Timestamp;
import org.llrp.ltk.generated.messages.ADD_ROSPEC;
import org.llrp.ltk.generated.messages.CLOSE_CONNECTION;
import org.llrp.ltk.generated.messages.DELETE_ROSPEC;
import org.llrp.ltk.generated.messages.DISABLE_ROSPEC;
import org.llrp.ltk.generated.messages.ENABLE_EVENTS_AND_REPORTS;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES;
import org.llrp.ltk.generated.messages.READER_EVENT_NOTIFICATION;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.generated.messages.SET_READER_CONFIG;
import org.llrp.ltk.generated.messages.START_ROSPEC;
import org.llrp.ltk.generated.messages.STOP_ROSPEC;
import org.llrp.ltk.generated.parameters.AISpec;
import org.llrp.ltk.generated.parameters.AISpecStopTrigger;
import org.llrp.ltk.generated.parameters.AccessReportSpec;
import org.llrp.ltk.generated.parameters.C1G2EPCMemorySelector;
import org.llrp.ltk.generated.parameters.ConnectionAttemptEvent;
import org.llrp.ltk.generated.parameters.EventNotificationState;
import org.llrp.ltk.generated.parameters.InventoryParameterSpec;
import org.llrp.ltk.generated.parameters.ROBoundarySpec;
import org.llrp.ltk.generated.parameters.ROReportSpec;
import org.llrp.ltk.generated.parameters.ROSpec;
import org.llrp.ltk.generated.parameters.ROSpecStartTrigger;
import org.llrp.ltk.generated.parameters.ROSpecStopTrigger;
import org.llrp.ltk.generated.parameters.ReaderEventNotificationData;
import org.llrp.ltk.generated.parameters.ReaderEventNotificationSpec;
import org.llrp.ltk.generated.parameters.TagReportContentSelector;
import org.llrp.ltk.generated.parameters.UTCTimestamp;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.llrp.ltk.net.LLRPConnector;
import org.llrp.ltk.net.LLRPEndpoint;
import org.llrp.ltk.net.LLRPIoHandlerAdapter;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.LLRPBitList;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.UnsignedByte;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedLong_DATETIME;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.types.UnsignedShortArray;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class ReaderService extends Service {

	private LLRPConnector mLLRPConnector;
	private static final int ROSPEC_ID = 1;
	
	public enum IntentDo {
		status, connect, disconnect, message, error, read, connectionError
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		if (intent == null)
			return START_STICKY;
		if (mLLRPConnector == null) {
			SharedPreferences prefs = getSharedPreferences("ReaderConfig",
					MODE_PRIVATE);
			String host = prefs.getString("host", "192.168.6.190");
			int port = prefs.getInt("port", 5084);
			mLLRPConnector = new LLRPConnector(mLLRPEndpoint, host, port);
		}
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				if (intent.getSerializableExtra("IntentDo") == IntentDo.connect) {
					try {
						LLRPIoHandlerAdapter handler = mLLRPConnector.getHandler();
						BlockingQueue<ConnectionAttemptEvent> connectionAttemptEventQueue = handler.getConnectionAttemptEventQueue();
						ConnectionAttemptEvent connectionAttemptEvent = connectionAttemptEventQueue.poll(500l, TimeUnit.MILLISECONDS);
						if(connectionAttemptEvent != null){
							ConnectionAttemptStatusType status = connectionAttemptEvent.getStatus();
							if(status.intValue() == ConnectionAttemptStatusType.Success){
								mLLRPConnector.reconnect();
							}else{
								mLLRPConnector.connect();
							}
						} else {
							mLLRPConnector.connect();
						}
					} catch (LLRPConnectionAttemptFailedException e) {
						Intent intent = new Intent(IntentDo.connectionError.toString());
						ReaderService.this.sendBroadcast(intent);
					} catch (InterruptedException e) {
						Intent intent = new Intent(IntentDo.connectionError.toString());
						ReaderService.this.sendBroadcast(intent);
					}
				} else if (intent.getSerializableExtra("IntentDo") == IntentDo.disconnect) {
					try {
						mLLRPConnector.transact(new CLOSE_CONNECTION());
						mLLRPConnector.disconnect();

					} catch (TimeoutException e1) {
						Intent intent = new Intent(IntentDo.connectionError.toString());
						ReaderService.this.sendBroadcast(intent);
						return;
					}
					try {
						Intent intent2 = new Intent(IntentDo.message.toString());
						READER_EVENT_NOTIFICATION ren = new READER_EVENT_NOTIFICATION();
						ReaderEventNotificationData rend = new ReaderEventNotificationData();
						UTCTimestamp utc = new UTCTimestamp();
						utc.setMicroseconds(new UnsignedLong_DATETIME(Calendar
								.getInstance().getTimeInMillis()));
						rend.setTimestamp(utc);
						ren.setReaderEventNotificationData(rend);
						intent2.putExtra("msg", ren.toBinaryString());
						ReaderService.this.sendBroadcast(intent2);
					} catch (InvalidLLRPMessageException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (intent.getSerializableExtra("IntentDo") == IntentDo.message) {
					String m = intent.getStringExtra("msg");
					LLRPMessage message;
					try {
						message = LLRPMessageFactory
								.createLLRPMessage(new LLRPBitList(m));
						mLLRPConnector.send(message);
					} catch (InvalidLLRPMessageException e) {
						e.printStackTrace();
					}
				} else if(intent.getSerializableExtra("IntentDo") == IntentDo.read) {
					// Create a SET_READER_CONFIG Message and send it to the reader
					SET_READER_CONFIG setReaderConfig = createSetReaderConfig();
					mLLRPConnector.send(setReaderConfig);
					// CREATE an ADD_ROSPEC Message and send it to the reader
					ADD_ROSPEC addROSpec = new ADD_ROSPEC();
					addROSpec.setROSpec(createROSpec());
					mLLRPConnector.send(addROSpec);
					ENABLE_ROSPEC enableROSpec = new ENABLE_ROSPEC();
					enableROSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
					mLLRPConnector.send(enableROSpec);
					START_ROSPEC startROSpec = new START_ROSPEC();
					startROSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
					mLLRPConnector.send(startROSpec);
					ENABLE_EVENTS_AND_REPORTS report = new ENABLE_EVENTS_AND_REPORTS();
					mLLRPConnector.send(report);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
							// Create a STOP_ROSPEC message and send it to the reader
							STOP_ROSPEC stopROSpec = new STOP_ROSPEC();
							stopROSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
							mLLRPConnector.send(stopROSpec);

							// Create a DISABLE_ROSPEC message and send it to the reader
							DISABLE_ROSPEC disableROSpec = new DISABLE_ROSPEC();
							disableROSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
							mLLRPConnector.send(disableROSpec);

							// Create a DELETE_ROSPEC message and send it to the reader
							DELETE_ROSPEC deleteROSpec = new DELETE_ROSPEC();
							deleteROSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
							mLLRPConnector.send(deleteROSpec);
				}
			}
		});
		t.start();

		return START_STICKY;
	};

	private LLRPEndpoint mLLRPEndpoint = new LLRPEndpoint() {

		@Override
		public void messageReceived(LLRPMessage message) {
			Intent intent = new Intent(IntentDo.message.toString());
			try {
				intent.putExtra("msg", message.toBinaryString());
				ReaderService.this.sendBroadcast(intent);
			} catch (InvalidLLRPMessageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void errorOccured(String message) {
			Intent intent = new Intent(IntentDo.error.toString());
			intent.putExtra("error", message);
			ReaderService.this.sendBroadcast(intent);
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	private SET_READER_CONFIG createSetReaderConfig() {
		SET_READER_CONFIG setReaderConfig = new SET_READER_CONFIG();

		// Create a default RoReportSpec so that reports are sent at the end of ROSpecs
		 
		ROReportSpec roReportSpec = new ROReportSpec();
		roReportSpec.setN(new UnsignedShort(0));
		roReportSpec.setROReportTrigger(new ROReportTriggerType(
				ROReportTriggerType.Upon_N_Tags_Or_End_Of_ROSpec));
		TagReportContentSelector tagReportContentSelector = new TagReportContentSelector();
		tagReportContentSelector.setEnableAccessSpecID(new Bit(0));
		tagReportContentSelector.setEnableChannelIndex(new Bit(0));
		tagReportContentSelector.setEnableFirstSeenTimestamp(new Bit(0));
		tagReportContentSelector.setEnableInventoryParameterSpecID(new Bit(0));
		tagReportContentSelector.setEnableLastSeenTimestamp(new Bit(0));
		tagReportContentSelector.setEnablePeakRSSI(new Bit(0));
		tagReportContentSelector.setEnableSpecIndex(new Bit(0));
		tagReportContentSelector.setEnableTagSeenCount(new Bit(0));
		
		tagReportContentSelector.setEnableAntennaID(new Bit(1));
		tagReportContentSelector.setEnableROSpecID(new Bit(1));
		
		C1G2EPCMemorySelector epcMemSel = new C1G2EPCMemorySelector();
		epcMemSel.setEnableCRC(new Bit(0));
		epcMemSel.setEnablePCBits(new Bit(0));
		tagReportContentSelector
				.addToAirProtocolEPCMemorySelectorList(epcMemSel);
		roReportSpec.setTagReportContentSelector(tagReportContentSelector);
		setReaderConfig.setROReportSpec(roReportSpec);

		//  Set default AccessReportSpec
		 
		AccessReportSpec accessReportSpec = new AccessReportSpec();
		accessReportSpec.setAccessReportTrigger(new AccessReportTriggerType(
				AccessReportTriggerType.Whenever_ROReport_Is_Generated));
		setReaderConfig.setAccessReportSpec(accessReportSpec);

		// Set up reporting for AISpec events, ROSpec events, and GPI Events
		 
		ReaderEventNotificationSpec eventNoteSpec = new ReaderEventNotificationSpec();
		EventNotificationState noteState = new EventNotificationState();
		noteState.setEventType(new NotificationEventType(
				NotificationEventType.AISpec_Event));
		noteState.setNotificationState(new Bit(1));
		eventNoteSpec.addToEventNotificationStateList(noteState);
		noteState = new EventNotificationState();
		noteState.setEventType(new NotificationEventType(
				NotificationEventType.ROSpec_Event));
		noteState.setNotificationState(new Bit(1));
		eventNoteSpec.addToEventNotificationStateList(noteState);
		
		setReaderConfig.setReaderEventNotificationSpec(eventNoteSpec);

		setReaderConfig.setResetToFactoryDefault(new Bit(0));

		return setReaderConfig;
	}
	
	private ROSpec createROSpec() {
		// create a new rospec
		ROSpec roSpec = new ROSpec();
		roSpec.setPriority(new UnsignedByte(0));
		roSpec.setCurrentState(new ROSpecState(ROSpecState.Disabled));
		roSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
		// set up ROBoundary (start and stop triggers)
		ROBoundarySpec roBoundarySpec = new ROBoundarySpec();

		ROSpecStartTrigger startTrig = new ROSpecStartTrigger();
		startTrig.setROSpecStartTriggerType(new ROSpecStartTriggerType(
				ROSpecStartTriggerType.Null));
		roBoundarySpec.setROSpecStartTrigger(startTrig);

		ROSpecStopTrigger stopTrig = new ROSpecStopTrigger();
		stopTrig.setDurationTriggerValue(new UnsignedInteger(0));
		stopTrig.setROSpecStopTriggerType(new ROSpecStopTriggerType(
				ROSpecStopTriggerType.Null));
		roBoundarySpec.setROSpecStopTrigger(stopTrig);

		roSpec.setROBoundarySpec(roBoundarySpec);

		// Add an AISpec
		AISpec aispec = new AISpec();

		// set AI Stop trigger to null
		AISpecStopTrigger aiStopTrigger = new AISpecStopTrigger();
		aiStopTrigger.setAISpecStopTriggerType(new AISpecStopTriggerType(
				AISpecStopTriggerType.Null));
		aiStopTrigger.setDurationTrigger(new UnsignedInteger(0));
		aispec.setAISpecStopTrigger(aiStopTrigger);

		UnsignedShortArray antennaIDs = new UnsignedShortArray();
		antennaIDs.add(new UnsignedShort(4));
		aispec.setAntennaIDs(antennaIDs);

		InventoryParameterSpec inventoryParam = new InventoryParameterSpec();
		inventoryParam.setProtocolID(new AirProtocols(
				AirProtocols.EPCGlobalClass1Gen2));
		inventoryParam.setInventoryParameterSpecID(new UnsignedShort(1));
		aispec.addToInventoryParameterSpecList(inventoryParam);

		roSpec.addToSpecParameterList(aispec);

		return roSpec;
	}
	
}
