(

/*

10 secs everybody solos
20 secs of chorus: all play
30 sec: verses: then pairs me & jonas
*/

var nano, joystick;

nano = NanoKontrol2.new;

try
{
	GeneralHID.buildDeviceList;
	joystick = GeneralHID.findBy(1133, 49684);
	joystick = GeneralHID.open(joystick);
	GeneralHID.startEventLoop;
} {|error| \caught.postln; error.dump };


s.waitForBoot({

	var single, bells, irreplaceable, fftbuf, syn, assign, con, instr, buf, bright3, bright4,
	pmono;

	fftbuf = Buffer.alloc(s,1024);
	//	SynthDef(\concat, {|var out, })
	single = Buffer.readChannel(s, "/home/celesteh/Documents/inProgress/bile/shandy/SingleLadies.wav",
		channels: [0]);
	bells = Buffer.readChannel(s, "/home/celesteh/Documents/les-tmp/bells/bell2.wav", channels: [0]);
	irreplaceable = Buffer.readChannel(s, "/home/celesteh/Desktop/Beyonce/1-02\ Irreplaceable.wav",
		channels: [0]);
	bright3 = Buffer.readChannel(s, "/home/celesteh/Desktop/Beyonce/brighton-3.aiff",
		channels: [0]);
	bright4 = Buffer.readChannel(s, "/home/celesteh/Desktop/Beyonce/brighton-4.aiff",
		channels: [0]);

	s.sync;

	SynthDef(\token1, { |freq =440, amp, amps = 0.05, out =0, grainDur = 0.016, matchLength = 0.1, gate = 1, bufnum = 0|

		var concat, control, input, detectedamp, env;

		input= SinOsc.ar(2)*
		Mix(
			Gendy3.ar(3,5,1.0,1.0,(
				Array.fill(5,{
					LFNoise0.kr(Rand(0,1.3),1,2)})
				* freq), amps, grainDur,5,mul:0.1));

		control=PlayBuf.ar(1, bufnum, loop:1);//SoundIn.ar;

		detectedamp=Amplitude.ar(control);

		env = EnvGen.kr(Env.asr, gate, doneAction:2) * amp * detectedamp;

		concat= Concat2.ar(control,input,1.0,1.0,1.0,matchLength,0,1.0,0.0,1.0,1.0);

		concat = (concat * 10).tanh;

		Out.ar(out,concat * env)
	}).add;


	SynthDef(\token2, { |freq =440, amp, amps = 0.05, out =0, grainDur = 0.016, matchLength = 0.1, gate = 1, bufnum = 0, trigRate = 2|

		var concat, control, input, detectedamp, env, imp, startFrame;

		input= SinOsc.ar(2)*
		Mix(
			Gendy3.ar(3,5,1.0,1.0,(
				Array.fill(5,{
					LFNoise0.kr(Rand(0,1.3),1,2)})
				* freq), amps, grainDur,5,mul:0.1));

		imp = Dust.kr(trigRate.reciprocal);
		startFrame = Diwhite(0, BufFrames.kr(bufnum));

		control=PlayBuf.ar(1, bufnum, startPos:Demand.kr(imp+ gate, 0, startFrame), trigger:imp, loop:1);//SoundIn.ar;

		//detectedamp=Amplitude.ar(control);

		env = EnvGen.kr(Env.asr, gate, doneAction:2) * amp;

		concat= Concat2.ar(control,input,1.0,1.0,1.0,matchLength,0,1.0,0.0,1.0,1.0);

		concat = (concat * 10).tanh;

		Out.ar(out,concat * env)
	}).add;


	SynthDef(\freeze, {|out, bufnum, amp, gate=1, trigRate, freeze|

		var player, imp, fft, chain, env, startFrame;

		imp = Dust.kr(trigRate.reciprocal);
		startFrame = Diwhite(0, BufFrames.kr(bufnum));

		player=PlayBuf.ar(1, bufnum, startPos:Demand.kr(imp+ gate, 0, startFrame), trigger:imp, loop:1);//SoundIn.ar;

		chain = FFT(LocalBuf(2048), player);
		chain = PV_Freeze(chain, freeze);
		env = EnvGen.kr(Env.asr, gate, doneAction:2) * amp;

		fft = IFFT(chain);

		Out.ar(out, fft * env );
	}).add;



	s.sync;
	instr = \freeze;
	buf = irreplaceable;

	//syn = Synth(\token2, [\bufnum, bells.bufnum, \trigRate, 5 ]);


	con = Conductor.make({|cond, amp, freq, amps, grainDur, matchLength, trigRate, freeze|

		var setsyn;

		amps.sp(0.05, 0.01, 0.05, 0);
		grainDur.sp(0.016, 0.001, 0.016, 0);
		matchLength.sp(0.1, 0.0, 0.1, 0);
		trigRate.sp(0.1, 0.1, 20, 0);
		freeze.sp(0, 0, 1, 1);
		amp.value = 0.2;



		assign = { |index, param, sym|
			(param.respondsTo(\action_) && param.respondsTo(\input_)).if({
			sym.postln;
				(index <=8).if ({
					nano.at((\fader ++ index).asSymbol).action_({|val|
						//Bus('control', busIndex, 1).set(val.value / 127);

						param.input_(val.value/127);
					});
				});

				param.action_({|val|
					pmono.notNil.if({
						Pset(sym.asSymbol, val.value, pmono)
					})
				})
			})

		};

		setsyn = {

			pmono.notNil.if({
				Pset(\bufnum, buf.bufnum, pmono);
				Pset(\instrument, instr.asSymbol, pmono);
			});
		};

		//[freq, amp, amps, grainDur, matchLength, trigRate, freeze]

		cond.valueKeys.do({|sym, index|
			index = index+1;
			assign.(index, cond[sym], sym);
		});

		//\playBt   -> NKButton.new(45),
		//\stopBt   -> NKButton.new(46),

		nano.at(\playBt).action_({ var player; \play.postln; player = cond.player; player.play});
		nano.at(\stopBt).action_({ var player; \stop.postln; player = cond.player; player.stop});


		joystick.slots[3].at(1).action_({|val|
			freq.input_(val.value);
			amps.input_(val.value);
			grainDur.input_(val.value);
		});

		joystick.slots[3].at(0).action_({|val|
			matchLength.input_(val.value);
		});



		//setsyn = {
		//	\stop.postln;
		//	instr.postln;
		//	cond.player.stop;


		//	cond.pattern_(
		//		(
		//			instrument: instr.asSymbol
		//		),
		//		[
		//			//freq =440, amps = 0.05, out =0, grainDur = 0.016, matchLength = 0.1, gate = 1, bufnum = 0, trigRate = 2|
		//			amps: amps,
		//			graindDur: grainDur,
		//			bufnum:  [buf, buf.bufnum],
		//			mathcLength: matchLength,
		//			trigRate: trigRate,
		//			amp: amp,
		//			freeze: freeze
		//		]
		//	)



		//};


		cond.action_({
			pmono =
			Pmono(
				instr.asSymbol,
				\freq, Pfunc({freq.value}),
				\amp,  Pfunc({amp.value}),
				\grainDur,  Pfunc({grainDur.value}),
				\mathLength,  Pfunc({matchLength.value}),
				\trigRate,  Pfunc({trigRate.value}),
				\freeze,  Pfunc({freeze.value}),
				\bufnum, Pfunc({buf.bufnum})
			).play;
			"playing".postln;
			},
			{ pmono.stop },
			{ pmono.pause},
			{ pmono.resume}
		);


		//setsyn.value;




		joystick.slots[1].at(290).action_({|val|

			val = val.value;
			(val ==1).if({
				buf = bells;
				"bells".postln;
				setsyn.value;
			})
		});

		joystick.slots[1].at(289).action_({|val|

			val = val.value;
			(val ==1).if({
				buf = single;
				"single".postln;
				setsyn.value;
			})
		});

		joystick.slots[1].at(291).action_({|val|

			val = val.value;
			(val ==1).if({
				instr = \token1;
				"token1".postln;
				setsyn.value;
			})
		});

		joystick.slots[1].at(292).action_({|val|

			val = val.value;
			(val ==1).if({
				instr = \token2;
				"toekn2".postln;
				setsyn.value;
			})
		});

		joystick.slots[1].at(298).action_({|val|

			val = val.value;
			(val ==1).if({
				instr = \freeze;
				"freeze".postln;
				setsyn.value;
			})
		});


		joystick.slots[1].at(288).action_({|val|

			val = val.value;
			(val ==1).if({
				cond.player.play;
				"play".postln;
			})
		});

		joystick.slots[1].at(294).action_({|val|

			val = val.value;
			(val ==1).if({
				cond.player.stop;
				"stop".postln;
			})
		});


	}).show


})

)

(

(note:1).play;
(note:5).play;
(note:8).play;
)











/*(
s.waitForBoot({
//d=Buffer.read(s,"/home/celesteh/Documents/inProgress/MU800/MU800 SC/Spring Term/01 Bad Romance.aiff");
//d = Buffer.read(s, "/home/celesteh/Documents/inProgress/bile/shandy/dion.aiff");
d = Buffer.read(s, "/home/celesteh/Documents/inProgress/bile/shandy/SingleLadies.wav");

b = Buffer.alloc(s,4096,1); //for sampling rates 44100 and 48000

q = Buffer.read(s, "/usr/local/share/SuperCollider/Extensions/SC3plugins/PitchDetection/extraqitchfiles/QspeckernN4096SR44100.wav");
})
)

(
{
var in, fft, resample, chord, rootnote, sympath;
var key, transientdetection;
	var trackb,trackh,trackq,tempo;
var freq, hasFreq, sin, env;
var bsound,hsound,qsound;


in = PlayBuf.ar(1, d, BufRateScale.kr(d), 1, 0, 1, 2);

fft = FFT(b, in);

key = KeyTrack.kr(fft, 2.0, 0.5);
key.poll;
key = Median.kr(101, key); // Remove outlier wibbles

chord = if(key<12, #[0, 4, 7], #[0, 3, 7]);
rootnote = if(key<12, key, key-12) + 48;
	//key = if(key > 16, 0, key);

sympath = 0;//SinOsc.ar((rootnote + chord).midicps, 0, 0.4).mean / 2;

	sin = 0;
	# freq, hasFreq = Pitch.kr(in);
	sin = SinOsc.ar(freq, 0, 0.2);

	env = EnvGen.kr(Env.asr(releaseTime: 0.2), hasFreq* if(freq > 330, 0, 1));
	sin = sin * env;

	# freq, hasFreq = Tartini.kr(in);
	env = EnvGen.kr(Env.asr(releaseTime: 0.2), hasFreq* if(freq > 330, 0, 1));

	sin = sin + (SinOsc.ar(freq, 0, 0.2) * env);

	# freq, hasFreq = Qitch.kr(in, q.bufnum,0.01,1);
	env = EnvGen.kr(Env.asr(releaseTime: 0.2), hasFreq * if(freq > 330, 0, 1));

	sin = sin + (SinOsc.ar(freq, 0, 0.2) * env);



#trackb,trackh,trackq,tempo=BeatTrack.kr(FFT(b, in),
		MouseX.kr(0, 1));

bsound= Pan2.ar(LPF.ar(WhiteNoise.ar*(Decay.kr(trackb,0.05)),1000),0.0);

hsound= Pan2.ar(BPF.ar(WhiteNoise.ar*(Decay.kr(trackh,0.05)),3000,0.66),-0.5);

qsound= Pan2.ar(HPF.ar(WhiteNoise.ar*(Decay.kr(trackq,0.05)),5000),0.5);

	Out.ar(0, (bsound+hsound+qsound + sympath +sin) * 0.2 /*+ (in * 0.2)*/);
}.play;
)


//Out.ar(0,/*Pan2.ar(in, -0.5) + */Pan2.ar(sympath, 0.5));
//}.play
//)


(
{
    var trate, dur, clk, pos, pan;
    trate = MouseY.kr(8,120,1);
    dur = 12 / trate;
    clk = Impulse.kr(trate);
    pos = MouseX.kr(0,BufDur.kr(d)) + TRand.kr(0, 0.01, clk);
    pan = WhiteNoise.kr(0.6);
    TGrains.ar(2, clk, d, 1, pos, dur, pan, 0.1);
}.scope(zoom: 4);
)

({
	Tgrains.ar(2, trig, d.bufnum

(
{
    var trate, dur, rate;
    trate = MouseY.kr(2,200,1);
    dur = 4 / trate;
    rate = Dseq([10, 1, 1, 0.5, 0.5, 0.2, 0.1], inf);
    TGrains.ar(2, Impulse.ar(trate), b, rate, MouseX.kr(0,BufDur.kr(d)), dur, Dseq([-1, 1], inf), 0.1, 2) * 0.1;
}.scope(zoom: 4);
)
*/