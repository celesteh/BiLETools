OSCHID {

/*@
shortDesc: Deals with OSC-based Human Interface Devices
longDesc: Treat your touchOSC or OSCeleteon as you would any other HID
seeAlso: OscSlot
spec: A dictionary mapping keys to OscSlots
@*/

  	classvar <>dEBUG;
  	var <calibrate;

	var <>spec, <actionSpec;
	var <>remote;
	var resp;

	*new { arg netAddr;
		
		/*
		desc: Creates a new OSCHID. There should be one of these per device.
		netAddr: An optional argument for the address of the device.
		ex:
					
		k = OSCHID.new.spec_((
			ax: OscSlot(\realtive, '/hand/x'),
			ay: OscSlot(\realtive, '/hand/y'),
			az: OscSlot(\realtive, '/hand/z')
		));
		
		k.callibrate = false;
		

		*/
		
		^super.new.init(netAddr);
	}

	init { arg netAddr;
		
		var action;
		
		calibrate = true;
		
		("Set DarWIIn part to" + NetAddr.langPort).postln;
		("In calibration mode").postln;
		
		dEBUG = true;

		//roll = 0;
		//pitch = 0;

		remote = netAddr;
		remote.isNil.if({remote = NetAddr("127.0.0.1", 5601)});
		resp = [];
	}
	
	
	at { | controlName |
		^this.spec.atFail(controlName, {
			Error("invalid control name").throw
		});
	}
	
	setAction{ |key,keyAction|
		
		/*@
		desc: Set the action for a particular slot by key
		key: The key for the slot.
		keyAction: The Action, which takes the slot as an argument.
		ex:
		k.setAction(ax, {|val|
			val.value.postln;
		});
		@*/
		
		this.at( key).action_( keyAction );
	}
	
	removeAction{ |key|
		this.at( key).action_( {} );
	}
	
	calibrate_ { |bool|
		/*@
		desc: Turn callibration on or off
		bool: true for on, false for off
		ex:
		When callibration is true, relative slots do not call their actions
		
		k.calibrate = true;

		k.setAction(ax, {|val|
			val.value.postln;
		});

		k.callibrate = false;
		@*/		
		
		calibrate = bool;
		this.spec.do({ |slot|
			slot.calibrate = calibrate;
		});
	}

}

WiiOSCClient : OSCHID{
	
	
	/*@
	shortDesc: A class that communicates with DarWiinRemote OSC
	longDesc: This class has all the same methods as WiiMote, so if that class ever gets fixed, it will be possible to switch back to it with minimal effort.  To use this class, start DarWiinRemote OSC and set its preferences so its OSC out port is the language port of SuperCollider
	seeAlso: WiiMote
	@*/
	
	// For Usage, see the documentation at the bottom of this file
	
 // 	classvar <>dEBUG;
 // 	var <calibrate;


	

	var <>id;
	var <battery;
	var <ext_type;
	var <>closeAction, <>connectAction, <>disconnectAction;
	var <calibration;
	var <remote_led, <>remote_buttons, <>remote_motion, <>remote_ir;
	var <>nunchuk_buttons, <>nunchuk_motion, <>nunchuk_stick;
	var <>classic_buttons, <>classic_stick1, <>classic_stick2, <>classic_analog;
	var <>dumpEvents = false;

  	var batterylevel;
	var ir;
	
	


	deviceSpec {
		^(
			ax: OscSlot(\relative, '/wii/acc', nil, 1),
			ay: OscSlot(\relative, '/wii/acc', nil, 2),
			az: OscSlot(\relative, '/wii/acc', nil, 3),

			apitch: OscSlot(\relative, '/wii/orientation', nil, 1),
			aroll: OscSlot(\relative, '/wii/orientation', nil, 2),

			bA: OscSlot(\button, '/wii/button/a'),
			bB: OscSlot(\button, '/wii/button/b'),
			bOne: OscSlot(\button, '/wii/button/one'),
			bTwo: OscSlot(\button, '/wii/button/two'),
			bMinus: OscSlot(\button, '/wii/button/minus'),
			bHome: OscSlot(\button, '/wii/button/home'),
			bPlus: OscSlot(\button, '/wii/button/plus'),
			bUp: OscSlot(\button, '/wii/button/up'),
			bDown: OscSlot(\button, '/wii/button/down'),
			bLeft: OscSlot(\button, '/wii/button/left'),
			bRight: OscSlot(\button, '/wii/button/right')

/*			px: { remote_ir[0] },
			py: { remote_ir[1] },
			angle: { remote_ir[2] },
			tracking: { remote_ir[3] },
*/
// when i'm less lazy
/*
			nax: { nunchuk_motion[0] },
			nay: { nunchuk_motion[1] },
			naz: { nunchuk_motion[2] },
			nao: { nunchuk_motion[3] },

			nsx: { nunchuk_stick[0] },
			nsy: { nunchuk_stick[1] },

			nbZ: { nunchuk_buttons[0] },
			nbC: { nunchuk_buttons[1] },

			cbX: { classic_buttons[0] },
			cbY: { classic_buttons[1] },
			cbA: { classic_buttons[2] },
			cbB: { classic_buttons[3] },
			cbL: { classic_buttons[4] },
			cbR: { classic_buttons[5] },
			cbZL: { classic_buttons[6] },
			cbZR: { classic_buttons[7] },
			cbUp: { classic_buttons[8] },
			cbDown: { classic_buttons[9] },
			cbLeft: { classic_buttons[10] },
			cbRight: { classic_buttons[11] },
			cbMinus: { classic_buttons[12] },
			cbHome: { classic_buttons[13] },
			cbPlus: { classic_buttons[14] },

			csx1: { classic_stick1[0] },
			csy1: { classic_stick1[1] },

			csx2: { classic_stick2[0] },
			csy2: { classic_stick2[1] },

			caleft: { classic_analog[0] },
			caright: { classic_analog[1] }
*/		)
	}

	
	
	*new { arg netAddr;
		
		^super.new.init(netAddr);
	}
	
	init { arg netAddr;
		
		/*
		var action;
		
		calibrate = true;
		
		("Set DarWIIn part to" + NetAddr.langPort).postln;
		("In calibration mode").postln;
		
		dEBUG = true;

		//roll = 0;
		//pitch = 0;

		remote = netAddr;
		remote.isNil.if({remote = NetAddr("127.0.0.1", 5601)});
		resp = [];
		*/
		super.init(netAddr);
		spec = this.deviceSpec;
	}
	
	


}

/*
Acceleration {
	var <>x, <>y, <>z;
	//var <>speedX, <>speedY, <>speedZ;
	
	*new{ arg x, y, z;
		^super.new.copyArgs(x,y,z);
	}
}
*/

OscSlot {
	
	/*@
	shortDesc: A wrapper class for OSCresponderNodes
	longDesc: Sets up an OSCresponderNode for a given key and optionally scales it based on minimum and maximum input received so far. This class is designed to mimmic GeneralHIDSlot.
	seeAlso: OSCHID GeneralHIDSlot OSCresponderNode
	action: Sets the action for the slot. This will not get called for \relative slots until the calibrations is set to false
	bus: Returns the bus to which this slot is mapped, if a bus has been created.
	@*/
	
	var <osckey, <>action, <>value, <>rawValue, <responder, <bus, busAction, index, type, scale;
	var <max, <min, >calibrate;
	
	*new{ arg type, osckey, action, index = 1, scale = 256;
		/*@
		desc: Create a new Slot
		type: \relative for inputs that should be scaled. \button for inputs that should not be
		osckey: The OSC message to listen for
		action: The action to call upon receiving a new OSC message
		index: The index of the message to listen for.  In most cases this will be one, but if your device bundles x, y z coordinates together and you want to listen for y, this would be 2. Or for z, it would be 3.
		ex:
		
		w = OSCHID.new.spec_((
			ax: OscSlot(\relative, '/wii/acc', nil, 1),
			ay: OscSlot(\relative, '/wii/acc', nil, 2),
			az: OscSlot(\relative, '/wii/acc', nil, 3)
		));

		@*/
		^super.new.init(type, osckey, action, index, nil, scale);
	}

	*match { arg type, osckey, action, index, equals, scale = 256;

		^super.new.init(type, osckey, action, index, equals, scale);
	}

	
	init { arg typ, key, act, ind = 1, eq, scl = 256;
		
		var resp_func;
		osckey = key;
		action = act;
		//busAction = {};
		index = ind.asInt;
		type = typ;
		scale = scl;
		calibrate = true;
		
		responder = OSCresponderNode(nil, osckey, { |t, r, msg|


			var test;
			
				//WiiOSCClient.dEBUG.if ({
					//msg.post; " ".post; msg[index].postln;
			test = true;
			eq.notNil.if ({
				test = false;
				(eq.size ==2).if ({
					(msg[eq.first] == eq.last).if ({
						test = true;
					});
				});
			});

			if (test, {
					rawValue = msg[index];
					
					min.isNil.if({ min = rawValue});
					max.isNil.if({ max = rawValue});
					
					if( rawValue < min, { 
						min = rawValue;
						"% % min % \n".postf(osckey, index, min);
					});
					if (rawValue > max, {
						max = rawValue;
						"% % max % \n".postf(osckey, index, max);
					});
				//});
				
				if (type == \relative, {
					//value = rawValue / scale;
					value = rawValue - min;
					(max != min).if({
						value = value / (max - min);
					});
				}, { value = rawValue});
				calibrate.not.if({
					this.action.notNil.if({this.action.value(this);});
					busAction.notNil.if({busAction.value(this);});
				});
			})
		}).add;
			
	}


	createBus{ |s|
		/*@
		desc: Create a control bus on the specified server, and map the incoming values to this bus.
		@*/
		s = s ? Server.default;
		if ( bus.isNil, {
			bus = Bus.control( s, 1 );
		},{
			if ( bus.index.isNil, {
				bus = Bus.control( s, 1 );
			});
		});
		/*		if ( s.serverRunning.not and: s.isLocal, {
			"Server seems not running, so bus will be invalid".warn;
			});*/
		busAction = { |v| bus.set( v.value ); };
	}
	
	
	freeBus{
		/*@
		desc: Free the bus on the server
		@*/
		busAction = {};
		bus.free;
	}

// JITLib support
	kr{
		/*@
		desc: JitLib support
		@*/
		this.createBus;
		^In.kr( bus );
	}

	
}

WiiRamp {
	
	var <>upslope, <>downslope, <>hold;
	var rout, max, min, direction, level, slope;
	
	*new { |upslope =15, downslope = 150, hold = 10|
		
		^super.new.init(upslope, downslope, hold);
	}
	
	init { arg up, down, top;


		upslope = up;
		downslope = down;
		hold = top;

		
		rout = Routine { arg inVal;
			
			var counter;
					
			level = inVal;
			slope = 0;
			max = inVal;
			min = inVal;
			
			counter = hold;
			direction = 0;
			//switch = false;
			
			inVal = inVal.yield;
			
			true.while({
				
				if ((inVal > level) , { // current position is below input
					direction = 1; // so go up
					counter = hold; // reset counter
					if ((inVal > max), { // this is a new target
						max = inVal;
						slope = (max - level) / upslope; // compute slope
					})
				});
				
				if (level < max, { direction = 1; });
				
				if ((direction == 1), {  // go up
					
					level = level + slope;
					
					if (((level >= max) || 
						(slope < 0.0000000000001)), { 
							
							direction = 0; 
							max = 0;
					});
					
				}, {
					if (direction == 0, { // hold at the top
						//"hold".postln;
						counter = counter -1;
						if ( counter == 0, {  // until the counter runs out
							direction = -1;
							counter = hold;
						})
					} , {
						if (( inVal < level), {  // if not holding or going up, check if
											// we need to go down
							min = inVal;
							slope = (level - min) / downslope;
							level = level - slope;	
						});
					});
				});
				
				inVal = level.yield;
			});
		};

		
	}
	
	next { |inval|
		
		^rout.next(inval);
	}
}


// Ok, here is the documentation!

/*

	// First, you create a new instance of WiiOSCClient, 
	// which starts in calibration mode
	
	
	w = WiiOSCClient.new;

	// If you have not already done so, open up DarwiinRemote OSC and get it talking to your wii.
	// Then go to preferences of that application and set the OSC port to the language port
	// Of SuperCollider.  You will see a message in the post window telling you what port
	// that is .... or you will see a lot of min and max messages, which lets you know it's
	// already callibrating
	
	// move your wiimote about as if you were playing it.  It will scale it's output accordingly
	
	
	// now that you're done callibrating, turn callibration mode off
	
	w.calibrate = false;
	
	// The WiiOSCClient is set up to behave very much like a HID client and is furthermore
	// designed for drop-in-place compatibility if anybody ever sorts out the WiiMote code
	// that SuperCollider pretends to support.
	
	// To get at a particular aspect of the data, you set an action per slot:
	
	w.setAction(\ax, {|val|
		
		val.value; // is the scaled data from \ax - the X axis of the accelerometre.
		// It should be between 0-1, scaled according to how you waved your arms during
		// the callibration period
	});
	
	
	
	// You can use a WiiRamp to provide some lag
	(
		r = WiiRamp (20, 200, 15);
	
		w.setAction(\ax, {|val|
			var scaled, lagged;
		
			scaled = ((val.value * 2) - 1).abs;
			lagged = r.next(scaled);
		
			// now do somehting with lagged
		});
	)
*/
