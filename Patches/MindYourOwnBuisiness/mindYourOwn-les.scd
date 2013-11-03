
// to start

// first start jack

/*
1. set bus - les
2. synthdefs - jonas
3. rhythm - shelly
4. set amp , fx - Holger
*/


(

var index, assign, nano;

s.waitForBoot({

	r = Republic.new.makeDefault;
	r.s.options.memSize = r.s.options.memSize * 2.pow(16);
	r.gui(12); // add a display if you like.

	AllGui();

	r.join(\les);

	r.shareHistory;

	index = Routine({var index = 10;
		inf.do({
			index.yield;
			index = index +1;
		})
	}).asStream;



	~newBus = {
		Bus.new('control', index.next, 1, s);
	};

	a = ~newBus.value;
	w = ~newBus.value;
	x = ~newBus.value;
	y = ~newBus.value;
	z = ~newBus.value;

	~syns =  {r.synthDescs.asArray.sort({ |a, b| a.name < b.name }).collect ({ |desc| desc.metadata.sourceCode.postln;});};

	s.sync;

	q = q ?? IdentityDictionary.new(know:true); // global dictionary
	try{
		s = r.myServer;
		Ndef.defaultServer=s;
	};
	//make sure you are only using republic server
	s.meter;

	~busVals = {
		r.nameList.do({|name|
			4.do({|i|
				i = i + 10;
				x = Bus.new(\control, i, 1, r.servers[name]);
				x.get({ |val| ("" ++ name + i + val).postln; })
			})
		})
	};

	Task({
		"y u no run?".warn;
		3.wait;
		//n = NetAPI.broadcast(\les);
		n = NetAPI.oscgroup("~/Documents/code/osc/oscgroups/bin/OscGroupClient", "192.168.0.101", "les", "password");
		1.wait;
		AppClock.sched(0.5, {
			BileClock(n).show;
			BileChat(n);
			// scope your bus values
			Stethoscope.new(r.s, 4, 10, rate:\control)
		}).play;
	}).play;


	// create some dict to store stuff
	q = q ? ();
	q.numChans = q.numChans ? 2;

	r.s.meter; // some meters
	// scope your bus values, works only in SC IDE
	Stethoscope.new(r.s, 4, 10, rate:\control);

	// create a 6-chan bus
	//~bus = Bus.new(\control, 10, 6, r.s);
	// 10 is volume, 11..15 are sound params

	// load some example patterns and sounds to start with:
	loadRelative("MYOB_Synths.scd");
	loadRelative("MYOB_Patterns.scd");

	// create a TempoClock;
	~clock = TempoClock.default.tempo_(1);

	~clock.tempo = 1; // original 60 bpm

	// make a sub dict for our responders
	q.resp = ();

	// prepare listening...
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


	s.sync;



	SynthDef('setBuses', { |out=10, ampval, val1, val2, val3, dur = 0.1|
		var newvals = [ampval, w, val1, val2, val3];
		EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
		ReplaceOut.kr(out, newvals);
	}).share;

	s.sync;

	SynthDef('setAmp', { |out=10, ampval=0.3, dur = 1|
		EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
		ReplaceOut.kr(out, ampval);
	}).share;

	s.sync;


	SynthDef('setBus', { |out=12, val1 = 0, dur = 1|
		EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
		ReplaceOut.kr(out, val1);
	}).share;

	s.sync;

	SynthDef(\envBusPerc, {|out=10, value =1, dur = 1, legato = 3, offset = 0.2|
		var env;
		env = EnvGen.kr(Env.perc(releaseTime:dur*legato, curve: -2), doneAction:2);
		ReplaceOut.kr(out, (value * env) + offset);
	}).share;

	s.sync;


	SynthDef(\envBusRev, {|out=10, value =1, dur = 1, legato = 3, offset = 0.2|
		var env;
		env = EnvGen.kr(Env.new([0,1], [dur*legato]), doneAction:2);
		ReplaceOut.kr(out, (value * env) + offset);
	}).share;

	s.sync;


	SynthDef('setBusesFX', { |out=0, ampval=0.3, val1=0.0, val2=0.0, val3=0.0, dur = 1|
		ampval = LFNoise1.kr(0.5).range(0.0, ampval);
		val1 = LFSaw.kr(LFNoise0.kr(0.5).range(1, 2)).range(val1, 1.0);
		val2 = val2.linlin(0,1,0,0.3) + LFSaw.kr(5).range(0.6, 0.76718139648438);
		val3 = SinOsc.kr(1).range(0.2, val3);
		EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
		ReplaceOut.kr(10, ampval);
		ReplaceOut.kr(11, val1);
		ReplaceOut.kr(12, val2);
		ReplaceOut.kr(13, val3);
	}).share;

	s.sync;


	SynthDef(\busNoise, {|out = 11, dur = 0.1|
		EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
		ReplaceOut.kr(out, BrownNoise.kr);
	}).share;

	s.sync;


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

	});


})



);

NetAPI.ip
n = NetAPI.oscgroup("~/Documents/code/osc/oscgroups/bin/OscGroupClient", "192.168.1.67", "les", "password");
BileClock(n).show;
BileChat(n);
// scope your bus values
Stethoscope.new(r.s, 4, 10, rate:\control)

PdefAllGui(5)


r.synthDescs.asArray.sort({ |a, b| a.name < b.name }).collect ({ |desc| desc.metadata.sourceCode.postln;});

~syns.value

//x = ~newBus.value
//y = ~newBus.value
//z = ~newBus.value



/*
(

SynthDef(\setBusles, { |out=0,value=0.0, dur = 1|
	EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
	ReplaceOut.kr(out, value);
}).share;


SynthDef(\envBusPercles, {|out=10, value =1, dur = 1, legato = 3, offset = 0.2|
	var env;
	env = EnvGen.kr(Env.perc(releaseTime:dur*legato, curve: -2), doneAction:2);
	ReplaceOut.kr(out, (value * env) + offset);
}).share;

SynthDef(\setBusesles, { |out=0,va=0.0, vx = 0, vy = 0, vz, gate = 1|
	EnvGen.kr(Env.adsr, gate:gate, doneAction:2);
	ReplaceOut.kr(out, [va, vx, vy, vz]);
}).share;

SynthDef(\initBusesles, {|out=10, dur = 0.1, value = 1|
	var env;
	env = EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
	ReplaceOut.kr(out, [value, value, value, value]);
}).share;


SynthDef(\busNoise, {|out = 11, dur = 0.1|
	EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
	ReplaceOut.kr(out, BrownNoise.kr);
}).share;
)
*/
(
Pdef(\init,
	Pbind(
		\instrument, \setBus,
		\server, r.s,
		\where, Pseq(r.nameList, 5), //
		\dur, 0.1,
		\legato, 2,
		\val1, 0.0001,
		\out, [11, 12, 13, 14]
	).trace
).play

)

Pdef(\init).stop

(

Pdef(\env,
	Pbind(
		\instrument, \envBusRev,
		\server, r.s,
		\where, Prand(r.nameList, inf), //
		\dur, Pwhite(0.1, 1),
		\legato, 50,
		\value, Pwhite(0, 0.5),
		\ofset, 0.5,
		\freq, Prand([\rest, \rest, 440], inf),
		\out, Pwhite(11,14)
	).trace
).play


)
Pdef(\env).stop

(
Pdef(\noise,
	Pbind(
		\instrument, \busNoise,
		\server, r.s,
		\where, Prand(r.nameList, inf), //
		\dur, Pwhite(0.01, 1),
		\legato, 50,
		\freq, Prand([\rest, \rest, 440], inf),
		\out, Pwhite(11,14)
	).trace
).play
)

Pdef(\noise).stop

/*
(
Pdef(\mindyouown,
	Pbind(
		\instrument, \mindyourownles,
		\server, r.s,
		\where, Prand(r.nameList, 8), //
		\dur, 1,
		\legato, 0.3,



	).trace
).play;
)


Pdef(\mindyouown).stop
*/

(
/*
Pdef(\gate,
	Pbind(
		\server, r.s,
		\where, Pfunc({r.nameList.choose}), //

		\instrument, \gate,
		\dur, 0.5
	).trace
).play;

Pdef(\freq,
	Pbind(
		\server, r.s,
		\where, Pfunc({r.nameList.choose}), //
		\instrument, \freq,
		\dur, 0.5,
		\freq, Pwhite(0.0, 1.0)
	).trace
).play;

Pdef(\freq2,
	Pbind(
		\server, r.s,
		\where, Pfunc({r.nameList.choose}), //
		\instrument, \freq2,
		\dur, 0.5,
		\freq, Pwhite(0.0, 1.0)
	).trace
).play;


Pdef(\amp,
	Pbind(
		\server, r.s,
		\instrument, \gate,
		\where, Pfunc({r.nameList.choose}), //
		\dur, 0.5,
		\amp, Pwhite(0.0, 1.0)
	).trace
).play;*/
Pdef(\bus,
	Pbind(
		\server, r.s,
		\instrument, \setBusles,
		\where, Pfunc({r.nameList.choose}), //
		\dur, 1,
		\value, Pwhite(0.2, 1),
		\out, Pwhite(10, 20)
	).trace
).play;
/*
Pdef(\gate,
	Pbind(
		\server, r.s,
		\instrument, \setBus,
		\where, Pfunc({r.nameList.choose}), //
		\dur, 0.2,
		\value, Pwhite(0, 1),
		\out, 13
	).trace
).play;
*/

)

(
Pdef(\gate).stop;
Pdef(\bus).stop;
)
(
Pdef(\gate).play;
Pdef(\bus).play;
)

(
SynthDef(\setBusesles, { |out=0,va=0.0, vx = 0, vy = 0, vz, gate = 1|
	EnvGen.kr(Env.adsr, gate:gate, doneAction:2);
	ReplaceOut.kr(out, [va, vx, vy, vz]);
}).share;
)

(



Conductor.make({|con, amp, x, y, z|


	con.pattern_(
		Pbind(
			\instrument, \setBusesles, // this Event is explicitly specifying all the default values

			\addAction, 1,
			\out, 10,
			\group, 1,
			\server, r.s,
			\where, Pseq(r.nameList), //


			\va, amp,
			\vx, x,
			\vy, y,
			\vz, z
	)
	)
}).show
)


n = NetAPI.broadcast(\les)
BileClock(n).show

SynthDef('sd1', { |out=0,sustain=0.5|
	var snd, env;
	var amp = In.kr(10, 1);
	var x = In.kr(11, 1);
	var y = In.kr(12, 1);
	var z = In.kr(13, 1);
	snd = SinOscFB.ar(x.linexp(0,1,220,1180) * [1,1.01], y.min(0.9999))
	* SinOsc.ar(SinOsc.ar(z.linlin(0,1,30,360), SinOsc.ar(y.linexp(0, 1, 100, 110)), x.linexp(0, 1, 220, 880)));
	env = EnvGen.ar(Env.perc(0.2,sustain),doneAction:2);
	snd = RLPF.ar(snd, z.linexp(0, 1, 800, 1100), y.linexp(0, 1, 0.01, 0.9));
	Out.ar(out, snd * env * amp);
}).share

SynthDef('sd2', { |out=0,sustain=0.5|
	var snd, env;
	var amp = In.kr(10, 1);
	var x = In.kr(11, 1);
	var y = In.kr(12, 1);
	var z = In.kr(13, 1);
	snd = RHPF.ar(LPF.ar(BrownNoise.ar(), y.linexp(0,1,10000,18000)),
		x.linexp(0,1,2440,8000) * [1,1.01], 0.1) *GVerb.ar(Dust.ar(y), revTime:z.linlin(0, 1, 1, 5));
	env = EnvGen.ar(Env.perc(z, sustain, curve: -2),doneAction:2);
	Out.ar(out, snd * env * amp);
}).share


SynthDef('sd3', { |out=0,sustain=0.5|
	var snd, env;
	var amp = In.kr(10, 1);
	var x = In.kr(11, 1);
	var y = In.kr(12, 1);
	var z = In.kr(13, 1);
	var a = In.kr(14, 1);
	snd = LPF.ar(LFSaw.ar(x.linexp(0,1,440,880) * [1,1.01],
		y.linlin(0,1.0, 0.1, 0.9)), a.linlin(0,1,3000,8000));
	env = EnvGen.ar(Env.perc(z,sustain),doneAction:2);
	Out.ar(out, snd * env * amp);
})



Array.geom(1, 5, 1)

w = 7
(
(
Pdef(\rhythm3,
	Pbind(\instrument, \sd3,
		\server, r.s,
		\where,  Pclump(2,Prand(r.nameList,inf)),
		\sustain, 0.5,
		\dur, Pseq((Array.fill(5, { arg i; i/w })).scramble.pyramid(1 +10.rand),inf),
		\freq, Pseq(Array.fill(w, { arg i; i*100 }).add(\rest), inf)
		//\dur, 0.2,
	)).play;
);
(
Pdef(\rhythm1,
		PbindProxy(\instrument, \sd1,
		\server, r.s,
		\where,  Pclump(2,Prand(r.nameList,inf)),
		\sustain, 0.5,
		\dur, Pseq((Array.fill(5, { arg i; i/w })).scramble.pyramid(1 +10.rand),inf),
		\freq, Pseq(Array.fill(w, { arg i; i+440 }).add(\rest), inf)
		//\dur, 0.2,
	)).play;
);
(
Pdef(\rhythm2,
		PbindProxy(\instrument, \sd2,
		\server, r.s,
		\where,  Pclump(2,Prand(r.nameList,inf)),
		\sustain, 0.3,
		\dur, Pseq((Array.fill(5, { arg i; i/w })).scramble.pyramid(1 +10.rand),inf),
		\freq, Pseq(Array.fill(w, { arg i; i+440 }).add(\rest), inf)
		//\dur, 0.2,
	)).play;
)
)


(

Pdef(\rhythm1).stop;
Pdef(\rhythm2).stop;
Pdef(\rhythm3).stop;
)




(
SynthDef(\mindyourownles, {|out|
	var env, son;
	son = SinOsc.ar(In.kr(11).linexp(0, 1, 330, 990), SinOsc.ar(In.kr(12).linexp(0, 1, 330, 990)));
		env = EnvGen.kr(Env.adsr(peakLevel: In.kr(10).linexp(0, 1, 0.1, 0.5)), gate:In.kr(13), doneAction:2 );

	Out.ar(out, son * env);
}).share;
)