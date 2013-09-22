(
s.waitForBoot({
//d=Buffer.read(s,"/home/celesteh/Documents/inProgress/MU800/MU800 SC/Spring Term/01 Bad Romance.aiff");
d = Buffer.read(s, "/home/celesteh/Documents/inProgress/bile/shandy/dion.aiff");

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

sympath = SinOsc.ar((rootnote + chord).midicps, 0, 0.4).mean / 2;

	# freq, hasFreq = Pitch.kr(in);
	sin = SinOsc.ar(freq, 0, 0.2);

	env = EnvGen.kr(Env.asr(releaseTime: 0.2), hasFreq);
	sin = sin * env;

	# freq, hasFreq = Tartini.kr(in);
	env = EnvGen.kr(Env.asr(releaseTime: 0.2), hasFreq);

	sin = sin + (SinOsc.ar(freq, 0, 0.2) * env);

	# freq, hasFreq = Qitch.kr(in, q.bufnum,0.01,1);
	env = EnvGen.kr(Env.asr(releaseTime: 0.2), hasFreq);

	sin = sin + (SinOsc.ar(freq, 0, 0.2) * env);



#trackb,trackh,trackq,tempo=BeatTrack.kr(FFT(b, in),
		MouseX.kr(0, 1));

bsound= Pan2.ar(LPF.ar(WhiteNoise.ar*(Decay.kr(trackb,0.05)),1000),0.0);

hsound= Pan2.ar(BPF.ar(WhiteNoise.ar*(Decay.kr(trackh,0.05)),3000,0.66),-0.5);

qsound= Pan2.ar(HPF.ar(WhiteNoise.ar*(Decay.kr(trackq,0.05)),5000),0.5);

	Out.ar(0, bsound+hsound+qsound + sympath +sin /*+ (in * 0.2)*/);
}.play;
)


//Out.ar(0,/*Pan2.ar(in, -0.5) + */Pan2.ar(sympath, 0.5));
//}.play
//)

