package com.dugbel.glass.battery;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import com.google.android.glass.app.Card;

/**
 * Activity that checks the battery status outputs it to a card then reads it aloud
 * 
 * @author dbell
 *
 */
public class BatteryStatusActivity extends Activity {

	/** TextToSpeech instance */
	private TextToSpeech speech;

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)  {
		super.onCreate(savedInstanceState);

		final IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		final Intent batteryStatus = this.registerReceiver(null, ifilter);

		// Are we charging / charged?
		final int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		final boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
		final boolean isCharged = status == BatteryManager.BATTERY_STATUS_FULL;

		// How are we charging?
		final int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		final boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		final boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

		// Battery Level
		final int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		final int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		final float batteryPct = level / (float)scale;

		final String chargeText;
		if (Math.round(batteryPct * 100) >= 99) {
			chargeText = "charged";
		} else if (Math.round(batteryPct * 100) <= 2) {
			chargeText = "empty";
		} else {
			chargeText = Math.round(batteryPct * 100) + "% charged";
		}

		// TODO Calculation of drain to hours

		final StringBuilder footnote = new StringBuilder();
		if (isCharging) {
			footnote.append("charging");
			if (usbCharge) {
				footnote.append(" via USB");
			}
			if (acCharge) {
				footnote.append(" via AC");
			}
		} else if (isCharged) {
			footnote.append("fully charged");
		} else if (!isCharging) {
			footnote.append("not charging");
		}

		// Set the Card text and image
		final Card card = new Card(this);
		card.setText("The battery is " + chargeText);
	
		card.setFootnote(footnote.toString());
		card.setImageLayout(Card.ImageLayout.LEFT);

		if (batteryPct == 1.0) {
			card.addImage(R.drawable.ic_battery_100);
		} else if (batteryPct > .5) {
			card.addImage(R.drawable.ic_battery_75);
		} else if (batteryPct > .2) {
			card.addImage(R.drawable.ic_battery_50);
		} else {
			card.addImage(R.drawable.ic_battery_20);
		}

		setContentView(card.toView());

		final String text = "The battery is " + chargeText;
		speech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			/*
			 * (non-Javadoc)
			 * @see android.speech.tts.TextToSpeech.OnInitListener#onInit(int)
			 */
			@Override
			public void onInit(int status) {
				speech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(speech != null) { 
			speech.stop();
			speech.shutdown();
		}
	}

}
