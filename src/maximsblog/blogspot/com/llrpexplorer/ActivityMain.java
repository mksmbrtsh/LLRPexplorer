package maximsblog.blogspot.com.llrpexplorer;

import java.util.List;
import java.util.concurrent.TimeoutException;

import maximsblog.blogspot.com.llrpexplorer.ReaderService.IntentDo;

import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.generated.enumerations.AISpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.AccessReportTriggerType;
import org.llrp.ltk.generated.enumerations.AirProtocols;
import org.llrp.ltk.generated.enumerations.GetReaderCapabilitiesRequestedData;
import org.llrp.ltk.generated.enumerations.NotificationEventType;
import org.llrp.ltk.generated.enumerations.ROReportTriggerType;
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
import org.llrp.ltk.generated.messages.SET_READER_CONFIG;
import org.llrp.ltk.generated.messages.START_ROSPEC;
import org.llrp.ltk.generated.messages.START_ROSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.STOP_ROSPEC;
import org.llrp.ltk.generated.messages.STOP_ROSPEC_RESPONSE;
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
import org.llrp.ltk.generated.parameters.TagReportData;
import org.llrp.ltk.net.LLRPConnection;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.llrp.ltk.net.LLRPConnector;
import org.llrp.ltk.net.LLRPEndpoint;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.LLRPBitList;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.UnsignedByte;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.types.UnsignedShortArray;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

public class ActivityMain extends Activity implements OnClickListener {

	private IntentFilter mIntentFilter;
	private TextView t;
	private Switch mToggleConnectSwitch;
	private Button mButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(ReaderService.IntentDo.connect.toString());
		mIntentFilter.addAction(ReaderService.IntentDo.message.toString());
		mIntentFilter.addAction(ReaderService.IntentDo.error.toString());
		mIntentFilter.addAction(ReaderService.IntentDo.connectionError
				.toString());
		mIntentFilter.addAction(ReaderService.IntentDo.disconnect.toString());

		t = (TextView) findViewById(R.id.t);
		t.setMovementMethod(new ScrollingMovementMethod());
		mButton = (Button) findViewById(R.id.exec);
		mButton.setOnClickListener(this);
		mButton.setEnabled(false);
		mToggleConnectSwitch = (Switch) findViewById(R.id.toggleButton1);
		mToggleConnectSwitch.setOnClickListener(this);
		TextView textAbout = (TextView)findViewById(R.id.textView1);
		textAbout.setText(getResources().getText(R.string.about_text));
		Linkify.addLinks(textAbout, Linkify.ALL);
		textAbout.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void onResume() {
		super.onStart();
		registerReceiver(mIntentReceiver, mIntentFilter);
	}

	@Override
	public void onPause() {
		super.onStop();
		if (mIntentReceiver != null)
			unregisterReceiver(mIntentReceiver);
	};

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				if (intent.getAction().equals(
						ReaderService.IntentDo.message.toString())) {
					try {
						LLRPMessage message = LLRPMessageFactory
								.createLLRPMessage(new LLRPBitList(intent
										.getStringExtra("msg")));
						updateUI(message);
					} catch (InvalidLLRPMessageException e) {
						e.printStackTrace();
					}
				} else if (intent.getAction().equals(
						ReaderService.IntentDo.error.toString())) {
					String message = intent.getStringExtra("error");
					t.setText(t.getText().toString() + "\n" + message);
				} else if (intent.getAction().equals(
						ReaderService.IntentDo.connectionError.toString())) {
					t.setText(t.getText().toString() + "\n"
							+ "connection error");
					mButton.setEnabled(false);
					mToggleConnectSwitch.setOnClickListener(null);
					mToggleConnectSwitch.setChecked(false);
					mToggleConnectSwitch.setEnabled(true);
					mToggleConnectSwitch.setOnClickListener(ActivityMain.this);
				}
			}
		}
	};

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.toggleButton1) {
			Intent service;
			service = new Intent(this, ReaderService.class);
			if (!mToggleConnectSwitch.isChecked()) {
				service.putExtra("IntentDo", IntentDo.disconnect);
			} else {
				service.putExtra("IntentDo", IntentDo.connect);
			}
			mToggleConnectSwitch.setEnabled(true);
			startService(service);
		} else {
			Intent service = new Intent(this, ReaderService.class);
			service.putExtra("IntentDo", IntentDo.read);
			startService(service);
		}
	}

	private void updateUI(LLRPMessage message) {
		if (message instanceof READER_EVENT_NOTIFICATION) {
			READER_EVENT_NOTIFICATION readerEventNotification = ((READER_EVENT_NOTIFICATION) message);
			ReaderEventNotificationData red = readerEventNotification
					.getReaderEventNotificationData();
			if (red.getAISpecEvent() == null && red.getAntennaEvent() == null
					&& red.getConnectionCloseEvent() == null
					&& red.getGPIEvent() == null
					&& red.getHoppingEvent() == null
					&& red.getReaderExceptionEvent() == null
					&& red.getReportBufferLevelWarningEvent() == null
					&& red.getReportBufferOverflowErrorEvent() == null
					&& red.getRFSurveyEvent() == null
					&& red.getROSpecEvent() == null) {
				if (red.getConnectionAttemptEvent() != null) {
					t.setText(t.getText().toString() + "\n"
							+ "Connection attempt was successful");
					mButton.setEnabled(true);
				} else {
					t.setText(t.getText().toString() + "\n"
							+ "Connection attempt was unsucessful");
					mButton.setEnabled(false);
				}
				mToggleConnectSwitch.setEnabled(true);
			} else {
				//t.setText(t.getText().toString() + "\n"
				//		+ readerEventNotification.toString());
			}
		} else if (message instanceof GET_READER_CAPABILITIES_RESPONSE) {
			GET_READER_CAPABILITIES_RESPONSE getReaderCap = (GET_READER_CAPABILITIES_RESPONSE) message;
			UnsignedShort maxNumberOfAntennaSupported = getReaderCap
					.getGeneralDeviceCapabilities()
					.getMaxNumberOfAntennaSupported();
			t.setText(t.getText().toString()
					+ "\nmaxNumberOfAntennaSupported: "
					+ maxNumberOfAntennaSupported);
		} else if (message instanceof START_ROSPEC_RESPONSE) {
		} else if (message instanceof RO_ACCESS_REPORT) {
			RO_ACCESS_REPORT roAccessReport = (RO_ACCESS_REPORT) message;
			List<TagReportData> l = roAccessReport.getTagReportDataList();
			if(l == null || l.isEmpty()) {
				t.setText(t.getText().toString() + "\n"
						+ "Tag not found");
			}
			for (TagReportData trd : l)
				t.setText(t.getText().toString() + "\n"
						+ trd.getEPCParameter().toString());
		}
		if (message instanceof STOP_ROSPEC_RESPONSE) {

		} else {

		}
	}

}
