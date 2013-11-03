// All the SynthDefs for MYOB

(
// a test sound
(
SynthDef(\testsound, { |freq=500, sustain=1, amp=0.1, out=0|
	var son, env, dur;
	dur = sustain;
	env = EnvGen.kr(Env.new([amp,amp,0],[dur,dur*0.1]), doneAction: 2);
	son = SinOsc.ar(freq*[1,1.3]);
	Out.ar(out, son*env*amp)
}).share;
);

// The Control SynthDefs
// B Bus control via SynthDef
(
(
SynthDef('setBuses', { |out=11, value1, value2, value3, dur=0.1|
	var newvals = [value1, value2, value3];
	EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
	ReplaceOut.kr(out, newvals);
}).share;
);

(
SynthDef('setAmp', { |out=10, ampval=0.3, dur = 1|
	EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
	ReplaceOut.kr(out, ampval);
}).share;
);

(
SynthDef('setAmpRand', { |out=10, dur = 1|
	EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
	ReplaceOut.kr(out, LFNoise2.kr(rrand(0.1,10)).abs);
}).share;
);

(
SynthDef('setBus', { |out=11, value=0, dur = 1|
	EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
	ReplaceOut.kr(out, value);
}).share;
);

SynthDef(\envBusPerc, {|out=10, value=1, dur = 1, legato = 3, offset = 0.2|
	var env;
	env = EnvGen.kr(Env.perc(releaseTime:dur*legato, curve: -2), doneAction:2);
	ReplaceOut.kr(out, (value * env) + offset);
}).share;

SynthDef(\envBusRev, {|out=10, value=1, dur = 1, legato = 3, offset = 0.2|
	var env;
	env = EnvGen.kr(Env.new([0,1], [dur*legato]), doneAction:2);
	ReplaceOut.kr(out, (value * env) + offset);
}).share;

(
SynthDef('setBusesFX', { |out=0, value1=0.0, value2=0.0, value3=0.0, dur = 1|
	value1 = LFSaw.kr(LFNoise0.kr(0.5).range(1, 2)).range(value1, 1.0);
	value2 = value2.linlin(0,1,0,0.3) + LFSaw.kr(5).range(0.6, 0.76718139648438);
	value3 = SinOsc.kr(1).range(0.2, value3);
	EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
	ReplaceOut.kr(11, value1);
	ReplaceOut.kr(12, value2);
	ReplaceOut.kr(13, value3);
}).share;
);


SynthDef(\busNoise, {|out = 11, dur = 0.1|
	EnvGen.kr(Env.perc(releaseTime:dur), doneAction:2);
	ReplaceOut.kr(out, BrownNoise.kr);
}).share;




(
// a blip sound
SynthDef(\sd1, {arg out=0, sustain=0.5;
	var son, env,lfo,harm, amp,x,y,z,freq,dist;
	amp = In.kr(10, 1);
	x = In.kr(11, 1);
	y = In.kr(12, 1);
	z = In.kr(13, 1);
	env = EnvGen.kr(Env.new([0,1,0.8,0],[0.15,sustain,0.1],-3),doneAction:2);
	// do remapping here?
	freq = x.linexp(0,1,50,2000);
	lfo = y.linlin(0,1,0.01,100);
	harm = z.linexp(0,1,1,15);
	dist = z.linexp(0,1,10,1);

	lfo = LFSaw.kr(lfo).range(1,10);
	son = RHPF.ar(Blip.ar(freq*[1,1.02], harm, 0.1), freq*0.5*lfo,0.2, dist);
	son = LeakDC.ar(son);
	Out.ar(out, son.tanh * env * amp);
}).share;
);

(
SynthDef(\sd2, {|out=0, sustain=0.5|
	var son, env, mod, trig, trig2,freq, rate, rate2, rel, x,y,z, amp;
	amp = In.kr(10, 1);
	x = In.kr(11, 1);
	y = In.kr(12, 1);
	z = In.kr(13, 1);
	// do remapping here?
	freq = x.linexp(0,1,150,4000);
	rate = y.linlin(0,1,0.1,15);
	rate2 = rate* Rand(0.8,1.4);
	rel = z.linlin(0,1,0.1,2.0);

	trig = Impulse.kr(rate);
	trig = Trig.kr(trig, Rand(0.5,1.0) * sustain);
	mod = Stepper.kr(trig, 0, 1, x.linexp(0,1,1,10) ); //
	son = Mix.new(SinOsc.ar((freq*1.01.rand)/mod, mul:amp)!3).dup;
	son = Ringz.ar(son, freq/mod, 0.1);
	son = LeakDC.ar(son).tanh;

	env = EnvGen.kr(Env.new([0,0.5,0],[0.03,rel],[-7,-2]), doneAction:2);

	Out.ar(out, son*env*amp*0.6)
}).share;
);

(
SynthDef('sd3', { |out=0,sustain=0.5|
	var snd, env, env2, freq, mul, noises, atk, select, amp, x,y,z,a;
	amp = In.kr(10, 1);
	x = In.kr(11, 1);
	y = Latch.kr(In.kr(12, 1),Impulse.kr(4));
	z = In.kr(13, 1);
	a = In.kr(14, 1);
	env2 = Line.kr(1,0.5,sustain);
	// do remapping
	freq = x.linexp(0,1,1000,12000 * env2);
	mul = a.linexp(0,1,100,1500);
	atk = LFNoise0.kr(x*5).range(0.01,0.3);
	freq = freq *[1,1.01];
	noises = [ LFDClipNoise.ar(freq, mul),
			LFClipNoise.ar(freq, mul),
			LFDNoise0.ar(freq, mul*2),
			LFDNoise1.ar(freq, mul*4),
			LFDNoise3.ar(freq, mul*4),
			LFNoise0.ar(freq, mul*4),
			LFNoise1.ar(freq, mul*4),
			LFNoise2.ar(freq, mul*4)];

	select = z.linlin(0,1,0,noises.size);

	snd = SelectX.ar(select, BPF.ar(noises), freq*LFSaw.kr(z).range(0.5,1.5)) * 0.0001*(y*5);
	env = EnvGen.ar(Env.perc(atk,sustain),doneAction:2);
	Out.ar(out, snd.tanh * env * amp);
}).share;
);

// sd4,5,6 are Holgers Originals
(
SynthDef('sd4', { |out=0,sustain=0.5|
	var snd, env;
	var amp = In.kr(10, 1);
	var x = Latch.kr(In.kr(11, 1),Impulse.kr(2));
	var y = Latch.kr(In.kr(12, 1),Impulse.kr(4));
	var z = In.kr(13, 1);
	snd = SinOscFB.ar(x.linexp(0,1,430,1808) * [1,1.01], y.min(0.9999))
	* SinOsc.ar(Saw.ar(z.linlin(0,1,300,6000),
		SinOsc.ar(y.linexp(0, 1, 1, 110)), x.linexp(0, 1, 220, 880)));
	env = EnvGen.ar(Env.linen(0.02,sustain/2,sustain/2),doneAction:2);
	snd = RHPF.ar(snd,
		z.lag(0.1).linexp(0, 1, 80, 111),
		y.linexp(0, 1, 0.01, 0.1),0.1);
	Out.ar(out, snd * env * amp * 0.2);
}).share;
);

(
SynthDef('sd5', { |out=0,sustain=0.5|
	var snd, env;
	var amp = Latch.kr(In.kr(10, 1),Impulse.kr(1)).lag(0.1);
	var x = In.kr(11, 1);
	var y = Latch.kr(In.kr(12, 1), Impulse.kr(Rand(4,0.1)));
	var z = In.kr(13, 1);
	snd = SinOsc.ar(x.linexp(0,1,100,1000)).ring3(
		GVerb.ar(Impulse.ar(y.linexp(0,1,10,1000)),
			revtime: z.linlin(0, 1.0, 0.1, 1.4)));
	snd = RLPF.ar(snd, x.linexp(0,1,1000,5000), 0.2).tanh;
	env = EnvGen.ar(Env.perc(sustain, z, curve: 2),doneAction:2);
	Out.ar(out, snd * env * amp);
}).share;
);

(
SynthDef('sd6', { |out=0,sustain=0.5|
	var snd, env;
	var amp = In.kr(10, 1);
	var x = In.kr(11, 1);
	var y = Latch.kr(In.kr(12, 1),Impulse.kr(4));
	var z = In.kr(13, 1);
	var a = In.kr(14, 1);
	var env2 = Line.kr(1,0.5,sustain);
	snd = BPF.ar(
		LFClipNoise.ar(x.linexp(0,1,1000,12000 * env2) * [1,1.01],
		a.linexp(0,1,100,1500))) * 0.0001;
	env = EnvGen.ar(Env.perc(z,sustain),doneAction:2);
	Out.ar(out, snd.tanh * env * amp);
}).share;

);

);

/*
// alternatives

(
// a bassy sound
SynthDef(\sd1, { |out=0, sustain=1|
	var son, sin, env, filt, freq, lfo, bpm, rate, rise, dist, amp, env2, x,y,z;
	amp = In.kr(10, 1);
	x = In.kr(11, 1);
	y = In.kr(12, 1);
	z = In.kr(13, 1);
	env2 = EnvGen.ar(Env.perc(0.01,sustain, amp, curve: 2), doneAction:2);
	// do remapping here?
	freq = x.linexp(0,1,40,1000);
	rate = y.linlin(0,1,0.1,100);
	rise = z.linexp(0,1,0.1,50);
	dist = y.linexp(0,1,0.1,50);

	bpm = LFNoise1.kr(1,1,1) * (freq/4.4);
	lfo = SinOsc.kr(rate);
	env = SinOsc.kr((bpm/60), pi/2, freq, 40+freq);
	son = Mix.fill(4, {|i| LFSaw.ar((i+1)/2*freq,lfo,1/(i+1));});
	sin = Mix.fill(4, {|i| SinOsc.ar((i+1)/2.04*freq,lfo,1/(i+1))});
	son = (son+sin)!2;
	filt = RLPF.ar(son+sin, env*rise, 0.1, 0.1*dist).tanh;
	Out.ar(out, filt*env2*0.5);
	}).share;
);

(
SynthDef(\sd1, { |out=0, sustain=1|
	var son, sin, env, filt, trig, lfo, mod, bpm,rand, env2,freq, chop, rise, dist, dnoise, x,y,z,amp;
	amp = In.kr(10, 1);
	x = In.kr(11, 1);
	y = In.kr(12, 1);
	z = In.kr(13, 1);
	// do remapping here?
	freq = x.linexp(0,1,50,2000);
	chop = y.linlin(0,1,0.1,15);
	rise = z.linexp(0,1,0.1,50);
	dist = y.linexp(0,1,0.1,50);
	dnoise = x.linexp(0,1,10,0.1);

	env = freq * XLine.kr([rise,1/rise], 1, log(freq)*0.05);
	env2 = EnvGen.kr(Env.new([0,1,0.8,0],[0.15,sustain,0.1],-3),doneAction:2);
	rand = LFNoise0.kr(log(env**0.5), 0.25,0.25);
	chop = chop*rand*log(env);
	lfo = LFSaw.kr(chop, 0,0.3,0.5);
	son = LFTri.ar(env)!2 +
		 Mix.fill(2, {|i| LFCub.ar(env*i, 0, 3/(i+1))}) +
		 Mix.fill(6, {|i| LFSaw.ar(env*2*i, 0, 2/(i+1))})!2;
	filt = RLPF.ar(son*lfo, freq, 0.15, 0.1*dist).tanh;
	son = RHPF.ar(son*lfo,lfo*env+10, 0.15, 0.1*dist).fold2(dnoise);
	Out.ar(out,Limiter.ar((son+filt)*amp*env2*0.2,0.96));
	}).share;
);
*/

// the FX Synths
(

SynthDef(\dropTape, { |dt=2, inbus=0|
	var in = In.ar(inbus, 2);
	var del = DelayL.ar(in, dt, Line.kr(0,  dt.sqrt, dt, doneAction: 2).squared);
	ReplaceOut.ar(inbus, del);
}).share;

SynthDef(\MUTE, { |sustain=8|
	ReplaceOut.ar(0, In.ar(0, 2) * (1 - Line.kr(1, 1, sustain, doneAction: 2)));
}).share;

SynthDef(\wah, { |rate = 2, depth = 0.5, sustain=8, gain = 3|
	ReplaceOut.ar(0,
		(Ringz.ar(
			In.ar(0, 2) * Line.kr(1, 1, sustain, doneAction: 2),
			VarSaw.kr(2, 0, 0.1, depth).lag(0.1).exprange(200, 5000),
			0.003
		) * gain).tanh.lag(0.0003) / gain.sqrt
	);
}).share;

SynthDef(\rev, { |revTime = 3, drift = 0.5, sustain = 10, rate = 0.5, wet = 0.5|
	var env = Line.kr(1, 1, sustain, doneAction: 2);
	var in = In.ar(0, 2);
	var rev = AdCVerb.ar(in * (env * 0.1 * wet),
		revTime,
		combScale: LFDNoise3.kr(rate, drift.clip2(0.9), 1)
	);
	var fade = Line.kr(1,0,sustain);
	ReplaceOut.ar(0, in * (1-(wet*fade)) + (rev*fade));
}).share;

SynthDef(\krush, { |bits = 8, downsamp = 8, sustain = 10|
	var down;
	var env = Line.kr(1, 1, sustain, doneAction: 2);
	var in = In.ar(0, 2);
	in = in.round(0.5 ** bits);
	down = Latch.ar(in, Impulse.ar(SampleRate.ir / downsamp.max(2)));

	ReplaceOut.ar(0, down);
}).share;

SynthDef(\noizy, { |noizy = 0.5, sustain = 10|
	var down;
	var env = Line.kr(1, 1, sustain, doneAction: 2);
	var in = In.ar(0, 2);
	in = in * GrayNoise.ar(noizy, 1 - noizy);
	ReplaceOut.ar(0, in);
}).share;

SynthDef(\noizy2, { |rate = 0.5, sustain = 10|
	var noizy = LFNoise0.kr(rate).range(0, 1).squared;
	var env = Line.kr(1, 1, sustain, doneAction: 2);
	var in = In.ar(0, 2);
	in = in * GrayNoise.ar(noizy, 1 - noizy);
	ReplaceOut.ar(0, in);
}).share;


SynthDef(\tanh, { |noizy = 0.5, sustain = 10|
	var down;
	var env = Line.kr(1, 1, sustain, doneAction: 2);
	var in = In.ar(0, 2);
	in = OnePole.ar((in * 10).tanh * 0.3, 0.5);
	ReplaceOut.ar(0, in);
}).share;


SynthDef(\hpf, { |freq = 500, sustain = 10|
	var env = Line.kr(1, 1, sustain, doneAction: 2);
	var in = In.ar(0, 2);
	in = HPF.ar(in * (freq / 500).sqrt, freq);
	ReplaceOut.ar(0, in);
}).share;

SynthDef(\strobeAr, { |sustain=8, freq=8, width=0.5|
	var in = In.ar(0, 2);
	var strobe = LFPulse.kr(freq, 0, width);
	var env = Line.kr(1, 1, sustain, doneAction: 2);
	ReplaceOut.ar(0, in * strobe * env);
}).share;

SynthDef(\fade, { |sustain=8, freq=8, width=0.5|
	var in = In.ar(0, 2);
	var lfo = LFTri.kr(freq).range(0.01,1);
	var env = Line.kr(1, 1, sustain, doneAction: 2);
	ReplaceOut.ar(0, in * lfo * env);
}).share;

);


);



