(

w = WiiMote.discover;

)

(


	// stockhausen! Act 2



	var api;
	var chat;
	var stick, wii;
	var clock;
	var bar;
	var qbuf, bufs, active_buf;
	var button_act, stick_act;
	var timer;
	var stop, start;

	stop = ~stopRecording;
	start = ~startRecording;

	stick = false; // change to true if you have a stick
	wii = true; //change to true if you have a wii


	api = NetAPI.default;


	s.options.memSize = s.options.memSize * 256;

	bar = 1;

	s.waitForBoot({

		var joystick, spec, document_gui, wiimote;
		var synth_group, post_group;
		var show;
		var cv;
		var stop_all_loops, play_once, play_loop, granulate, granulate_rm, record;
		var mAX_LEN;
		var active_instrument;


		mAX_LEN = 16;



		show = true;
		timer = TapTimer(mAX_LEN, 1, initClock: false);

		stick.if ({

			"yay stick".postln;



			GeneralHID.buildDeviceList.do({ arg dev, num;

				var name;

				name = dev[1].name;
				(name.contains("Joystick")).if ({

					joystick = GeneralHID.open( dev);
				})

			});

			joystick.isNil.if({
				stick = false;
			} , {
				joystick.caps;



				spec = GeneralHIDSpec.new(joystick);
				spec.add(\b1,	[1,1]);
				spec.add(\b2,	[1,2]);
				spec.add(\b3,	[1,3]);
				spec.add(\b4,	[1,4]);
				spec.add(\b5,	[1,5]);
				spec.add(\b6,	[1,6]);
				spec.add(\b7,	[1,7]);
				spec.add(\b8,	[1,8]);
				spec.add(\b9,	[1,9]);
				spec.add(\b10,	[1,10]);
				spec.add(\hat,	[3,57]);
				spec.add(\lx,	[3,48]);
				spec.add(\ly,	[3,49]);
				spec.add(\rx,	[3,50]);
				spec.add(\ry,	[3,53]);



				GeneralHID.startEventLoop(0.005);
			});


		});

		wii.if({

			wiimote = w; //WiiOSCClient.new;
			//wiimote.calibrate = false;
		wiimote.battery;
		w.enableMotionSensor( 1 );
		w.enableExpansion( 1 );
		w.enableButtons(1);
	});



		Task({


			s.sync;



			"making groups".postln;

			// create the groups

			synth_group = Group.new;
			post_group = Group.new(synth_group, \addAfter);

			// add some synthdefs


			SynthDef( "MultiGrainBuf", {arg out = 0, bufnum = 0, midFrameStart, midFrameEnd,
						grainDur, synthDur, pan = -1, amp = 0.2, interp = 2, rate = 1, percent;

				var env, speed, player, absPercent, trate, trig1, trig2, trig3,
					trig4, trig5, trig6, center;

				env = EnvGen.kr(Env.linen(0.01, (synthDur - 0.011), 0.001, amp*3), doneAction:2);

				speed = rate *  BufRateScale.kr(bufnum);
				trate = 1 / grainDur;
				trig1 = Impulse.ar(trate, 0);
				trig2 = Impulse.ar(trate, 0.2);
				trig3 = Impulse.ar(trate, 0.4);
				trig4 = Impulse.ar(trate, 0.6);
				trig5 = Impulse.ar(trate, 0.8);
				trig6 = Impulse.ar(trate, 1);

				center = Line.kr(midFrameStart, midFrameEnd, synthDur - grainDur);

				player = TGrains.ar(2, trig1, bufnum, rate, center, grainDur, pan, env, interp);
				player = player + TGrains.ar(2, trig2, bufnum, rate, center, grainDur,
										pan, env, interp);
				player = player + TGrains.ar(2, trig3, bufnum, rate, center, grainDur,
										pan, env, interp);
				player = player + TGrains.ar(2, trig4, bufnum, rate, center, grainDur,
										pan, env, interp);
				player = player + TGrains.ar(2, trig5, bufnum, rate, center, grainDur,
										pan, env, interp);
				player = player + TGrains.ar(2, trig6, bufnum, rate, center, grainDur,
										pan, env, interp);
				//player = player / 2;

				absPercent = 1.0 - percent;

				player = (player * percent) + ((player.abs) * absPercent);
				//player = Mix.ar([player, player.abs]);
				player = player[0] + player [1];
				player = LeakDC.ar(player).tanh;

				Out.ar(out, player);

			}).add;





			SynthDef(\playshift, {arg out = 0, bufnum = 0, pan = -1,amp =1,startFrame = 0,
						dur = 1, rate = 1, shift_amp, signal_amp, interval, mask, gate =1;



 				var env, speed, player, panner, shifted, mix, gated;

 				//env = EnvGen.kr(Env.linen(0.001, (dur - 0.002), 0.001, amp*2), doneAction:2);
				env = EnvGen.kr(Env.sine(dur, amp * 3), doneAction: 2); // was dur * 2
				gated = EnvGen.kr(Env.adsr(0.001, 0.001, 1, 1, 1, -4), gate);

				env = env * gated;

				speed = rate *  BufRateScale.kr(bufnum);
				player = PlayBuf.ar(1, bufnum, speed, loop:1, startPos: startFrame);
				shifted = PitchShift.ar(player, 0.1, interval) * shift_amp;
				player = player * signal_amp;
				mix = player;// * env;// + shifted;  //tsting removing shifted part
				//mix = MantissaMask.ar(mix, mask);
				panner = Pan2.ar(mix, pan, env);
				panner = panner * 5;
				panner = panner.tanh;
				Out.ar(out, panner);
			}).add;


			SynthDef(\granulate, {|out = 0, bufnum, pan = -1, amp = 1, dur, rate = 1, density,
								pos, gate =1|

				var env, player, line, gated;

				//env = EnvGen.kr(Env.linen(0.001, (dur - 0.002), 0.001, amp*2), doneAction:2);
				env = EnvGen.kr(Env.sine(dur * 2, amp * 3), doneAction: 2);
				gated = EnvGen.kr(Env.adsr(0.001, 0.001, 1, 1, 1, -4), gate);

				env = env * gated;

				line = (pos + Line.kr(0.0, 1, dur)) % 1;

				player = GrainBuf.ar(1, Impulse.kr(density), LFNoise1.kr.range(0.05, 0.2),
									bufnum, rate * LFNoise1.kr.range(0.9, 1.1),
									line, 4, pan, -1, mul:env);

				player = player * 5;
				player = player.tanh;

				Out.ar(out, player)
			}).add;


			SynthDef(\granulate_rm, {|out = 0, bufnum, pan = -1, amp = 1, dur, rate = 1,
									density, freq, pos|

				var env, player, line, sin;

				//env = EnvGen.kr(Env.linen(0.001, (dur - 0.002), 0.001, amp*2), doneAction:2);
				env = EnvGen.kr(Env.sine(dur * 2, amp * 3), doneAction: 2);

				line = (pos + Line.kr(0.0, 1, dur)) % 1;

				player = GrainBuf.ar(1, Impulse.kr(density), LFNoise1.kr.range(0.05, 0.2),
									bufnum, rate * LFNoise1.kr.range(0.9, 1.1),
									line, 4, pan, -1, mul:env);

				sin = SinOsc.ar(freq, 0, env);
				player = player * sin;
				player = player * 5;
				player = player.tanh;
				Out.ar(out, player)
			}).add;

			SynthDef(\monitor, { arg pan = 0, beatTrack = 0, pitchTrack = 0, lock=0, mask = 8;

				var trackb,trackh,trackq,tempo;
				var source, freq, hasFreq, filter, panner;

				source= AudioIn.ar([1,2]);

				#trackb,trackh,trackq,tempo=AutoTrack.kr(source, lock);
				SendTrig.kr(trackb * beatTrack, 0, 0.9);

				#freq, hasFreq = Pitch.kr(source, ampThreshold: 0.02, median: 7);
				SendTrig.kr(hasFreq * pitchTrack, 1, freq);


				filter = MantissaMask.ar(source, mask);


				//panner = Pan2.ar(filter, pan, 0.2);





				Out.ar(0, /*panner*/ filter);
			}).store;


			// communication

			//AppClock.sched(0, { chat = BileChat(api); clock = BileClock(api); clock.show; nil });

			// get samples ready

			s.sync;

				//qbuf = SingleSample.alloc(s, "Q Buf", 1, mAX_LEN, show:show, num_grains:64);
				//qbuf = SingleSample.open(s, "1.1",
				//"/Users/celesteh/Library/Application Support/SuperCollider/sounds/field/belgian-night-mn.aiff",
				//File.openDialog.pathname,
				//27);

				~bufferAction = { |ba|


					stop_all_loops.value;

					qbuf = SingleSample.grains("1.1", BufferTool.grain(s,  ba), 27);
					bufs = [qbuf];

					qbuf.pan.value = -1;  qbuf.amp.value = 0.2;  qbuf.ratio.value = 1;


					active_buf = qbuf;

					active_buf.isNil.if({"things are fucked".postln});

				//s.sync;

					bufs = [qbuf];


					bufs.do({|b|

						b.amp.value = 0.2;
						b.ratio.value = 1;
						b.pan.value = -1;
						cv = b.addCV(\density);
						cv.sp(20, 10, 512, 1, 'linear');
						cv = b.addCV(\dur);
						cv.sp(4, 0.2, 10, 0, 'linear');
						cv = b.addCV(\freq);
						cv.spec_(\freq);
						cv = b.addCV(\pos);
						cv.spec_(\unipolar);
						//cv = b.at(\rate);
						//cv.spec_(1, -10, 10, 0, 'linear');
						AppClock.sched(0, {b.show; nil;});
					});
				};

			"before applclock".postln;

				"motherfucker".postln;
				//fucking_buffer = Buffer.loadDialog(s, action: {|b|
				//	qbuf = SingleSample.grains("1.1", BufferTool.grain(s, b), 27);

				"action".postln;
				//s.sync;




			play_once  = {|buf|
				var ttime;

				"play once".postln;

				(buf.isNil).if ({ buf = active_buf }, {active_buf = buf});

				ttime = bar * buf.dur_val;
					//~active.play_once_pan(\player, ~timer);
				Synth.new(\MultiGrainBuf, [\out, 0, \interp, 0, \rate, 1 + 1.0.rand,
							\percent, 0.9, \pan, 1.0.rand2, \amp, active_buf.amp.value,
							\grainDur,
							1 / 3.0.rand, \freq, 0.3, \bufnum, active_buf.bufnum,
							\midFrameStart, active_buf.buf.startFrame, \midFrameEnd,
							s.sampleRate * ttime, \synthDur, (2.rrand(8)) * ttime, \dur,
							ttime * 0.8], synth_group);
			};


			play_loop = { |buf, instr = \playshift ... args|

				"loop".postln;
				(buf.isNil).if ({ buf = active_buf }, {active_buf = buf});


				(buf.isPlaying).if ({
					"stop it".postln;
					buf.stop;
				} , {
					"we should play".postln;
					buf.pbind(instr, group: synth_group, pairs:args);
				});


			};


			granulate = { |buf|

				Synth(\granulate, [\out, 0, \bufnum, buf.bufnum, \dur, bar * buf.dur_val,
								\density, buf.density, \rate, buf.rate])

			};

			granulate_rm = { |buf|

				Synth(\granulate_rm, [\out, 0, \bufnum, buf.bufnum, \dur, bar * buf.dur_val,
								\density, buf.density, \rate, buf.rate,
								\freq, 660 + 330.0.rand2])
			};

			record = { |buf, state = 0|

				//(buf.isNil).if ({ buf = active_buf }, {active_buf = buf});

				timer.start_tap;

				(state == 1).if ({
					/*
					timer.isTicking.if ({
						timer.tempoclock.playNextBar({
							~start.value;
						});
					} , {
						"not ticking, ".post;
						~start.value;
						"but started recoding".postln;
					});
					*/
					~startRecording.value;
				} , { (state == 0).if ({

					~stopRecording.value;
				})});
			};


			stop_all_loops = {

				"stop all loops".postln;
				bufs.do({ |buf|

					(buf.isPlaying).if ({
						buf.stop;
					})
				})
			};




			/*
			AppClock.sched( 0, {

			document_gui = Document.new("Player", "a record, qwer play, zxcv loop, t timing,"
					+ "p status,  +- double/halve loop, 4 loop * 4, 8 loop * 8\n\n");


			document_gui.background_(Color.yellow(alpha:0.8)); //ooooh semi-translucent yellow!
			document_gui.keyDownAction_({arg doc, key;

				var time;

				("key down:" + key).postln;
				key.switch(

					$q,	{play_once.(active_buf)},
					$w,	{play_loop.(active_buf)},
					$e,	{play_loop.(active_buf,\granulate)},
					$r,	{play_loop.(active_buf,\granulate_rm, \freq, 660 + 330.0.rand2)},

					$a,	{record.(active_buf)},

					$t,	{timer.tap},

					$., { stop_all_loops.value; },

					$p, { bufs.do({|b| b.show_status}); }

				);
				nil;
			});


			});
			*/





			(stick || wii).if({ // joystick stuff



				button_act = { |val, action|
					var value;

					value = val.value;
					(value != 0).if({
						"button".postln;
						action.value;
					})
				};

				stick_act = { |val, sym, inverse = true|
					var value;
					//val.value.post; " ".post;
					value = val.value;
					inverse.if({ value = 1 - value; });

					active_buf.notNil.if({
						active_buf.conductor.notNil.if({
							active_buf.conductor.[sym].input = value;
						});
					});
				};
			});

			stick.if({

				spec[\b5].action_({|val| button_act.(val, { play_once.(active_buf)})});
				spec[\b6].action_({|val| button_act.(val, {play_loop.(active_buf,
										\granulate)})});
				spec[\b7].action_({|val| button_act.(val, {play_loop.(active_buf)})});
				spec[\b8].action_({|val| button_act.(val, {play_loop.(active_buf,
										\granulate_rm)})});


				spec[\b1].action_({|val| record.(active_buf, val.value) });

				spec[\hat].action_({|val|
					var adjusted;
					adjusted = (val.value * 10).floor;

					(adjusted ==1).if ({ stop_all_loops.value;});
				});


				spec[\rx].action_({|val| val.value.postln; stick_act.(val, \density)});
				spec[\ry].action_({|val| stick_act.(val, \dur, false)});
				spec[\lx].action_({|val| stick_act.(val, \rate)});
				spec[\ly].action_({|val| stick_act.(val, \freq, false)});

			});

			wii.if({

			"wii!".postln;

				//wiimote.calibrate = false;


				active_instrument = \playshift;

				wiimote.setAction(\bA, ({|val| record.(active_buf, val.value) }));
				wiimote.setAction(\bB, {|val| play_loop.(active_buf, active_instrument); });
				wiimote.setAction(\bLeft, {|val| button_act.(val, {stop_all_loops.value; })});
				wiimote.setAction(\bRight, {|val| button_act.(val, ~increment; )});
				wiimote.setAction(\bUp, {|val| button_act.(val, ~mostRecent; )});
				wiimote.setAction(\bOne, {|val| button_act.(val, {
						active_instrument = \playshift })});
				wiimote.setAction(\bTwo, {|val| button_act.(val, {
						active_instrument = \granulate })});

				wiimote.setAction(\ax, {|val| stick_act.(val, \density, false)});
				wiimote.setAction(\ay, {|val| stick_act.(val, \rate, false)});

			});

				//})

		}).play;
	});
)
