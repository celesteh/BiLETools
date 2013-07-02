/*

Possible strategies:

Draw a 'mission out of a hat!
(Each person first puts two missions into the hat

Note playing paradigm of sounds, so something triggers on changes


Freezenote: click a button to temporarily halt others from being able to make changes

*/

(


// don't forget to press play!!!!!



	var act, adder, api, clock, chat, con, semaphore, should_call_action, twiddle;
	var user_names, user_update_action;


	//api = NetAPI("/Applications/Media/pro/audio/SuperCollider/OscGroup/OscGroupClient",
	//			//"localhost",
	//			"169.254.239.230",
	//			"les", "lespass");

api = NetAPI.default ? NetAPI.oscgroup("~/Documents/code/osc/oscgroups/bin/OscGroupClient", "realizedsound.mooo.com", "les", "password");

	semaphore = Semaphore(1);
	should_call_action = true;

	s.waitForBoot({



		// put your own synthdef here
	    /*
		SynthDef(\stuck, {|out = 0, freq1, freq2, x, amp|

			var impulses, vosim, fof, verb, moog, trig, q, biquad, freq, comb, gverb,
			shift;

			trig = Trig.kr(x - 0.25, 0.01) + Trig.kr((x - 0.25) * -1, 0.01);
			trig = trig + Trig.kr(x - 0.5, 0.01) + Trig.kr((x - 0.5) * -1, 0.01);
			trig = trig + Trig.kr(x - 0.75, 0.01) + Trig.kr((x - 0.75) * -1, 0.01);

			q = Demand.kr(trig, 0, Dwhite(0.1, 0.9, inf)).lag(10);
			//ffreq = x.linexp(0, 1, 600.1, 800.3);
			//Demand.kr(trig, 0, Dwhite(600, 800, inf));
			freq = Demand.kr(trig, 0,
							Drand([110.5, 220.5, 330.5, 440.5, 550.5, 660.6], inf)
						).lag(2);
			//freq = LFNoise2.kr(x).linexp(0,1, 110.5, 660.5).lag(10);

			impulses = Blip.ar(freq/*200.15*/).tanh;
			vosim = VOSIM.ar(impulses, freq1, 3, 0.99).tanh;
			fof = Formlet.ar(vosim, freq2,  0.01, 0.1.tanh);
			verb = FreeVerb.ar(fof.tanh,
								Lag.kr(LFNoise1.kr.range(0.1, 0.9),5),
								Lag.kr(LFNoise1.kr.range(0.3, 0.7),5),
								Lag.kr(LFNoise1.kr.range(0.3, 0.7), 5));
			biquad = SOS.ar(verb, x, 1-x, 0, (1-x)/5, x/5).tanh;
			moog = BMoog.ar(biquad, /*Lag.kr(LFNoise2.kr.range(600, 800),10)*/
							x.linexp(0, 1, 600.1, 800.3).lag(10), q, 0);
			comb = CombC.ar(moog, 0.2, x.linexp(0, 1, 101.1, 900).reciprocal, -0.2).tanh;

			gverb = GVerb.ar(comb, (x+1) * 100, 5).tanh;
			gverb = gverb[0] + gverb[1];

			//shift = PitchShift.ar(gverb, 0.02, x).tanh * amp;

			Out.ar(out, gverb * amp);
		}).add;
	    */
		// don't change this until....


	b = Bus.control(s, 2);

		s.sync;

	~bus = b;

		clock = BileClock(api).show;
		chat = BileChat(api);

		act = {|name, attribute, value|

			//value.postln;
			api.sendMsg((name ++ "/" ++ attribute).asSymbol, value);
		};



		con = Conductor.make({|con|

			var x, amp;

			x = con.addCV((api.nick ++ "/x").asSymbol);
			x.spec_(\unipolar);
			amp = con.addCV((api.nick ++ "/amp").asSymbol);
			amp.spec_(\amp);


			// change this part to map your variables
			//con.synth_(
			//	(instrument: \stuck ),
			//	[
			//		amp: amp,
			//		//freq1: [x, x.linexp(0.0, 1, 220.0, 880)],
			//		//freq2: [x, ( 1- x).linexp(0.0, 1, 111.1, 880)],
			//		x: x
			//	]
			//)

		con.controlBus_((index:b.index), [x, amp]);

		});

		// dont' change below this

		twiddle = { |tag|

			var conv, shared, controller, n, count;

			shared = SharedResource(0, true);
			conv = con[tag];

			n = 2; // throttle
			// this makes sense for kinect, but may be higher for other controllers
			count = 1;

			shared.changeAction_(api, { |val|
				//try {
					(count >= n).if({
						api.sendMsg(tag, val.value);
						//"reporting to api".postln;
						count = 1;
					} , {
						count = count +1
					});
				//} {}
			});

			api.add(tag, { |input|
				Task({
					//"tasking api".postln;
					try {
						shared.value_(input, api);
					} {}
				}).play;
			});

			shared.changeAction_(con, { |val|
				try {
					conv.value = val.value;
				} {}
			});

			conv.action_({ |cv|
				Task({
					//"tasking conv".postln;
					try {
						//"wiggle".postln;
						shared.value_(cv.value, con);
					} {}
				}).play;
			});

		};


		//con.addCV((api.nick ++ "/x").asSymbol);
		twiddle. ((api.nick ++ "/x").asSymbol);
		twiddle. ((api.nick ++ "/amp").asSymbol);

		con.show;
		user_update_action = {
			var tag, cv;

			user_names = api.colleagues.keys.collect({|k| k});




			AppClock.sched(0.0, {


				user_names.do({ | name|
					tag = ("" ++ name ++ "/x").asSymbol;
					con[tag].isNil.if({
						("adding" + name).postln;
						cv = con.addCV(tag);
						cv.spec_(\unipolar);
						twiddle.(tag);
					});
					tag = ("" ++ name ++ "/amp").asSymbol;
					con[tag].isNil.if({
						cv = con.addCV(tag);
						cv.spec_(\amp);
						twiddle.(tag);
					});
				});
				nil;
			});
		};




		user_update_action.value;
		api.add_user_update_listener(this, {user_update_action.value} );





	})
)



// don't foreget to press play!!!!!


	