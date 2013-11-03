// Mind Your Own Busines, v3, 3.11.2013
// SETUP


///////////////////////////////////
//// FIRST: Start Republic, IF NOT DONE by standalone
(
r = Republic.new.makeDefault;
r.gui(12); // add a display if you like.

// personalize here
~myPathToOscGroupClient="/Users/monodread/Documents/OscGroup/OscGroupClient"; // <- your path to OSCGroupClient
~myIP= "192.168.1.69"; //	<-- put the IP for OSCGroups here

// NICKNAME
~myNick= "whoami".unixCmdGetStdOut; // use computers name or personalize OR
~myNick = "jonas"; 	// <-- your nickname here

// join via code or in standalone use window to enter nickname
// takes a few seconds to start
r.join(~myNick.asSymbol);

)



///////////////////////////////////
// SECOND: start OSCGroups with BiLe Tools:
// cleanup before:
"killall OscGroupClient".unixCmd;

(
var nano, assign;
(

// BILE Tools via OSCGroups
n = NetAPI.default ? NetAPI.oscgroup(~myPathToOscGroupClient, ~myIP, ~myNick, "password");
AppClock.sched(0.5, {
	BileClock(n).show;
	BileChat(n);
}).play;
);

// create some dict to store stuff
q = q ? ();

r.s.meter; // some meters
// scope your bus values, works only in SC IDE
Stethoscope.new(r.s, 4, 10, rate:\control);

// create MANDELCLOCK
try {
MandelHub.join(~myNick);
m = MandelHub.instance;
m.gui; // make a Gui with the Metro & bpm
};

// create a 6-chan bus
~bus = Bus.new(\control, 10, 6, r.s);
// 10 is volume, 11 -> 'where'-bus, 12..16 are sound params

// create a TempoClock, synced to MandelClock
t = TempoClock.default.tempo_(1);

(
// assign bus control to Nano if you have Les' NanoKontrol2 class installed

'NanoKontrol2'.asClass.notNil.if({
    nano = NanoKontrol2.new;

    assign = { |fader, busIndex|
        nano.at(fader.asSymbol).action_({|val|
            Bus('control', busIndex, 1).set(val.value / 127);
        });
    };

	assign.(\fader8, 10);

    4.do({|index|
        index = index+1;
        assign.(\fader ++ index, 10+index);
    });

})
);

// load some example patterns and sounds to start with:
loadRelative("MYOB_Synths.scd");
loadRelative("MYOB_Patterns.scd");


);

// Not used when using MandelClock...
/*
(
// make a sub dict for our responders
q.resp = ();

// prepare listening for SC Standalone...
(
q.resp[\setclock] = OSCresponder(nil, '/setclock', { |t, resp, msg|
		var clock;
		clock = msg[1]; // second item in 'msg' is ndefname
		"//New Tempo is:  % bpm.\n".postf(clock);
		~clock.tempo = clock/60;
	}).add;
);

/*
// alternative Code for SC3.5 ++
q.resp[\setclock] = OSCFunc....


// use like this
r.send(\all, '/setclock', 60);
*/

);



// play test sound
( 'instrument': 'testsound', 'freq': 500, 'sustain': 1, 'amp': 1, 'where': 0).play;

// some example code:

// SET THE BUSES, directly on local buses...
~bus.set(0.5); //sets first bus only
~bus.get(_.postcs); // gets one value
~bus.getn(6,{ arg vals; vals.postcs;}); // gets all chans
~bus.setn([0.5,1,0.5,0.2,0.9]); // sets all channels
~bus.setn([0,0,0,0]); // all quiet

~bus.setPairs(0, 0.4, 2, 0.67); //set a specific channel: index, value
~bus.setAt(0, 1.0); // does the same

// get the latest SynthDef? if not done through BiLE Chat
// post SynthDefs if button doesnt work... problem: get truncated!
r.synthDescs.asArray.sort({ |a, b| a.name < b.name }).collect { |desc| ("(\n" ++ desc.metadata.sourceCode ++ ".share;\n);\n")}.asString;

// If you need Guis, do
TdefAllGui.new(10);
PdefAllGui.new(10);

*/

///////////////////////////////////////////////////////////////
//// THIRD: THE DIFFERENT BUSINESSES

//// 1. Timbre (x,y,z) -> control the buses
//// 2. Sound sources -> write the SynthDefs on the fly
//// 3. Event Patterns -> play Pdef/Tdef to trigger Sounds/Events, control sustain
//// 4. Volume/Spatialisation
//// 5. Add FX to Sounds -> use MasterFX Synths to add Effects

// GENERAL RULES
//// Every parameter must be between 0 and 1.
//// Bus 10 is Volume
//// Bus 11+ are parameters that can be used in the synth defs
//// Bus 16 is Targetdestination.


///////////////////////////////////////////////////////////////
//// FOURTH: SCORE & Example Tdef/Pdef

// We start with only 1 Sound, 1 Rhythm, 1 Parameter
// We take turns in the different Businesses every five minutes
// Every time we swap, the person gives his/her code to the follower to continue smoothly

// What happens inbetween with each 'business' is up to the person who is in charge for that.



// TO DO / DEVELOP
// Does Republic work for all, also Linux???

// Separate Sound params from Spatiality & Volume? Job for 2 People?
// Allow multiple Rhythms in parallel for more intersting layers?

// Start with different sets of sound for different performances.
// instead of blank slate?

// integrate Tempo / Sync. Read Tempo from Bus?


////// EXAMPLE CODE / EVENTs TO USE:

// FOR BUS CONTROL

//
// single event, zero values except amp
( 'instrument': 'setBuses', 'value1': 0, 'value2': 0, 'value3': 0, 'dur': 1, 'where': \all ).play;
// single event, all rand vals
( 'instrument': 'setBuses', 'value1': 1.0.rand, 'value2': 1.0.rand, 'value3': 1.0.rand, 'dur': 1, 'where': \all ).play;

// PROBLEM: Event or rather: Synth defaults set non specified vals back to zero!
( 'instrument': 'setBuses', 'ampval': 0.1, 'value1': 0.5, 'dur': 1, 'where': \all ).play;
( 'instrument': 'setBuses', 'ampval': 0.5, 'value2': 0.5.rand, 'dur': 1, 'where': \all ).play;

// WORK AROUND FOR NOW: Use specific SynthDefs for each single bus!
( 'instrument': 'setBus', 'out': 11, 'value': 0.2.rand, 'dur': 1, 'where': \all ).play; // set bus number through out arg
( 'instrument': 'setBus', 'out': 12, 'value': 0.2.rand, 'dur': 1, 'where': \all ).play;
( 'instrument': 'setBus', 'out': 13, 'value': 1.0.rand, 'dur': 1, 'where': \all ).play;

// from a Pattern
Pdef(\env,
	Pbind(
		\instrument, \envBusPerc,
		\server, r.s,
		\where, Pseq(r.nameList, inf), //
		\dur, Pwhite(0.1, 1),
		\legato, 0.5,
	     \value, Ptuple([Pseq([0,1],inf),1,Pseq([0,1],inf),1]),
		\offset, 0.5,
		\out, [11,12,13,14]
	).trace
).play


///////////////////////////////////////////////////////////////
// FOR PATTERNCONTROL

/*
// a test rhythm
Pbindef( 'rhythm_test' , 'instrument', 'sd3', 'sustain', Pseq([ 0.1, Pwhite(0.1, 0.6, 1) ], inf, 0), 'dur', (2 * Pseq([ 0.15, 0.1 ], inf, 0))).play;
*/

// change single params on the fly:
Pbindef(\rhythm1, \instrument, \sd1);
Pbindef(\rhythm1, \dur, 0.4);
Pbindef(\rhythm1, \dur, 0.4 * Prand([1, 1, Pn(0.3,3), Pn(0.5,2)],inf));

Pbindef(\rhythm1, \sustain, Pwhite(1, 3.0) * 0.01);

///////////////////////////////////////////////////////////////
// FOR AMP

( 'instrument': 'setAmp', 'ampval': 1.0.rand, 'dur': 1, 'where': \all ).play;

// from tdef
(
Tdef(\ampset, {
	[10,33].choose.do{
	( 'instrument': 'setAmp', 'ampval': 1.0.rand, 'dur': 1, 'where': r.nameList.choose ).play;
	[1,3,5].choose.rand.wait;
	}}).play;
);

///////////////////////////////////////////////////////////////
// FX
(instrument: "MUTE", addAction: 1, dur: 2, where: \all).play;

(instrument: \dropTape, addAction: 1, sustain: 15, where: \all).play;
(instrument: \dropTape, addAction: 1, dt: [2, 4, 6, 8].scramble * 0.5).play;

(instrument: \wah, addAction: 1, dur: 10, where: \all).play;
(instrument: \wah, addAction: 1, dur: 1, rate: [2, 3, 4, 6], where: \all).play;

(instrument: \strobeAr, addAction: 1, freq: 3, dur:4, where: \all).play;

(instrument: \fade, addAction: 1, freq: 0.1, dur:10, where: \all).play;


(instrument: \rev, addAction: 1, dur: 10, drift: 0.1, rate: 0.3, revTime: 10, wet: 0.6, where: \all).play;

(instrument: \krush, addAction: 1, bits: 6, \downsamp: 20, dur: 10, where: \all).play;

(instrument: \noizy, addAction: 1, noizy: 0.1, dur: 2, where: \all).play;

(instrument: \noizy2, addAction: 1, rate: 5, dur: 10, where: \all).play;

(instrument: \tanh, addAction: 1, dur: 10, where: \all).play;

(instrument: \hpf, addAction: 1, freq: 5000, dur: [3, 5, 7, 9].choose, where: \all).play;
(instrument: \hpf, addAction: 1, freq: 2500, dur: 2, where: \all).play;
(instrument: \hpf, addAction: 1, freq: 1500, dur: 10, where: \all).play;



