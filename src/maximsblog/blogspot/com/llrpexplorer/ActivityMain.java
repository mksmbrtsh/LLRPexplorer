package maximsblog.blogspot.com.llrpexplorer;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.enumerations.AISpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.AirProtocols;
import org.llrp.ltk.generated.enumerations.GetReaderCapabilitiesRequestedData;
import org.llrp.ltk.generated.enumerations.ROSpecStartTriggerType;
import org.llrp.ltk.generated.enumerations.ROSpecState;
import org.llrp.ltk.generated.enumerations.ROSpecStopTriggerType;
import org.llrp.ltk.generated.messages.ADD_ROSPEC;
import org.llrp.ltk.generated.messages.DELETE_ROSPEC;
import org.llrp.ltk.generated.messages.DISABLE_ROSPEC;
import org.llrp.ltk.generated.messages.ENABLE_EVENTS_AND_REPORTS;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES;
import org.llrp.ltk.generated.messages.GET_READER_CAPABILITIES_RESPONSE;
import org.llrp.ltk.generated.messages.GET_READER_CONFIG;
import org.llrp.ltk.generated.messages.READER_EVENT_NOTIFICATION;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.generated.messages.START_ROSPEC;
import org.llrp.ltk.generated.messages.START_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.STOP_ROSPEC;
import org.llrp.ltk.generated.parameters.AISpec;
import org.llrp.ltk.generated.parameters.AISpecStopTrigger;
import org.llrp.ltk.generated.parameters.InventoryParameterSpec;
import org.llrp.ltk.generated.parameters.ROBoundarySpec;
import org.llrp.ltk.generated.parameters.ROSpec;
import org.llrp.ltk.generated.parameters.ROSpecStartTrigger;
import org.llrp.ltk.generated.parameters.ROSpecStopTrigger;
import org.llrp.ltk.generated.parameters.ReaderEventNotificationData;
import org.llrp.ltk.generated.parameters.TagReportData;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.llrp.ltk.net.LLRPConnector;
import org.llrp.ltk.net.LLRPEndpoint;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.UnsignedByte;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.types.UnsignedShortArray;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ActivityMain extends Activity {

	private static final int ROSPEC_ID = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final TextView t = (TextView) findViewById(R.id.t);
		t.setMovementMethod(new ScrollingMovementMethod());

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();

		StrictMode.setThreadPolicy(policy);

		final LLRPConnector c = new LLRPConnector(new LLRPEndpoint() {

			@Override
			public void messageReceived(final LLRPMessage message) {
				runOnUiThread(new Runnable() {
					public void run() {
						if (message instanceof READER_EVENT_NOTIFICATION) {
							READER_EVENT_NOTIFICATION readerEventNotification = ((READER_EVENT_NOTIFICATION) message);
							ReaderEventNotificationData red = readerEventNotification
									.getReaderEventNotificationData();
							if (red.getConnectionAttemptEvent() != null) {
								t.setText(t.getText().toString() + "\n"
										+ "Connection attempt was successful");
							} else {
								t.setText(t.getText().toString() + "\n"
										+ "Connection attempt was unsucessful");
							}
							
						} else if(message instanceof GET_READER_CAPABILITIES_RESPONSE) {
							GET_READER_CAPABILITIES_RESPONSE getReaderCap = (GET_READER_CAPABILITIES_RESPONSE) message;
							UnsignedShort maxNumberOfAntennaSupported = getReaderCap.getGeneralDeviceCapabilities().getMaxNumberOfAntennaSupported();
							t.setText(t.getText().toString() + "\nmaxNumberOfAntennaSupported: " + maxNumberOfAntennaSupported);
						} else if(message instanceof START_ROSPEC_RESPONSE) {
							START_ROSPEC_RESPONSE startRospecResponse = (START_ROSPEC_RESPONSE) message;
							t.setText(t.getText().toString() + "\n" + startRospecResponse.getName());
						} else if(message instanceof RO_ACCESS_REPORT) {
							RO_ACCESS_REPORT roAccessReport = (RO_ACCESS_REPORT) message;
							List<TagReportData> l = roAccessReport.getTagReportDataList();
							for(TagReportData trd : l)
								t.setText(t.getText().toString() + "\n" + trd.getEPCParameter().toString());
						} else {
							t.setText(t.getText().toString() + "\n" + message.getName());
						}
					}
				});

			}

			@Override
			public void errorOccured(final String message) {
				runOnUiThread(new Runnable() {
					public void run() {
						t.setText(t.getText().toString() + "\n"
								+ message.toString());
					}
				});
			}
		}, "192.168.6.190");
		try {
			c.connect();
			c.
			GET_READER_CAPABILITIES getReaderCap = new GET_READER_CAPABILITIES();
			getReaderCap.setRequestedData(new GetReaderCapabilitiesRequestedData(
					GetReaderCapabilitiesRequestedData.All));
			c.send(getReaderCap);
			ENABLE_EVENTS_AND_REPORTS report = new ENABLE_EVENTS_AND_REPORTS();
			c.send(report);
			//CREATE an ADD_ROSPEC Message and send it to the reader
			ADD_ROSPEC addROSpec = new ADD_ROSPEC();
			addROSpec.setROSpec(createROSpec());
			c.send(addROSpec);
			ENABLE_ROSPEC enableROSpec = new ENABLE_ROSPEC();
			enableROSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
			c.send(enableROSpec);
			START_ROSPEC startROSpec = new START_ROSPEC();
			startROSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
			c.send(startROSpec);
			
			t.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					//Create a STOP_ROSPEC message and send it to the reader
					STOP_ROSPEC stopROSpec = new STOP_ROSPEC();
					stopROSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
					c.send(stopROSpec);

					//Create a DISABLE_ROSPEC message and send it to the reader
					DISABLE_ROSPEC disableROSpec = new DISABLE_ROSPEC();
					disableROSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
					c.send(disableROSpec);

					//Create a DELTE_ROSPEC message and send it to the reader
					DELETE_ROSPEC deleteROSpec = new DELETE_ROSPEC();
					deleteROSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));
					c.send(deleteROSpec);
					c.disconnect();
					t.setText(t.getText().toString() + "\n"
							+ "Disconnected");
				}
			}, 10000);
		} catch (LLRPConnectionAttemptFailedException e) {
			t.setText(t.getText().toString() + "\n"
					+ e.toString());
		}
	}

	private ROSpec createROSpec() {
		//create a new rospec
				ROSpec roSpec = new ROSpec();
				roSpec.setPriority(new UnsignedByte(0));
				roSpec.setCurrentState(new ROSpecState(ROSpecState.Disabled));
				roSpec.setROSpecID(new UnsignedInteger(ROSPEC_ID));

				//set up ROBoundary (start and stop triggers)
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

				//Add an AISpec
				AISpec aispec = new AISpec();
				
				//set AI Stop trigger to null
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
