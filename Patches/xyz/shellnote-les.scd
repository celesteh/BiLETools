(

w = WiiMote.discover;

)


(


//// first - adjust tilt


// don't forget to press play!!!!!



	var act, adder, api, clock, chat, con, should_call_action, twiddle;
	var win, active_player, challenger, xyz_constructor, controlling, lists, buttons;
	var semaphores, cv;
	var user_names, user_update_action;
	var kinect, assign_gestural_control, wiimote, controller;

	//"`/usr/bin/dirname \"$0\"`/kinect/run.sh".unixCmd;

	// shelly update this
/*
	kinect = OSCHID.new.spec_((
		ax: OscSlot(\relative, '/hand/x'),
		ay: OscSlot(\relative, '/hand/y'),
		az: OscSlot(\relative, '/hand/z')
		));
*/


wiimote = w; //WiiOSCClient.new;
			//wiimote.calibrate = false;
		wiimote.battery;
		w.enableMotionSensor( 1 );
		w.enableExpansion( 1 );
		w.enableButtons(1);

controller = wiimote;


	//api = NetAPI("/Applications/Media/pro/audio/SuperCollider/OscGroup/OscGroupClient",
	//			//"localhost",
	//			"169.254.239.230",
	//			"les", "lespass");

//	api = NetAPI.broadcast("Les"); // shelly update this
api = NetAPI.oscgroup("~/Documents/code/osc/oscgroups/bin/OscGroupClient", "192.168.1.76", "les", "password");


	should_call_action = true;
	controlling = Dictionary.new;
	lists = Dictionary.new;
	challenger = Dictionary.new;
	active_player = Dictionary.new;
	semaphores = Dictionary.new;
	buttons = Dictionary.new;





	//s.options.outDevice = "JackRouter";
	s.options.memSize = s.options.memSize * 16;




	s.waitForBoot({

		// shelly, put your own synthdef here
		SynthDef(\shellnote, {|out = 0, freq1, freq2, x, amp|

			var impulses, vosim, fof, verb, moog, trig, q, biquad, freq, comb, gverb,
			shift, inputs, change, env, thresh, shape;

			trig = Trig.kr(x - 0.25, 0.01);// + Trig.kr((x - 0.25) * -1, 0.01);
			trig = trig + Trig.kr(x - 0.5, 0.01); // + Trig.kr((x - 0.5) * -1, 0.01);
			trig = trig + Trig.kr(x - 0.75, 0.01);// + Trig.kr((x - 0.75) * -1, 0.01);

			thresh = 0.000000001;
			inputs = freq1 + freq2 + x;
			shape = HPZ1.kr(inputs).abs;
			change = shape > thresh;
			//shape = shape / RunningMax(shape).max(thresh);

			//amp =  amp * shape;

			env = EnvGen.kr(Env.linen(0.0009, 0.00001, 2, amp - 0.01, 'sine'), change) + 0.01.min(amp);


			q = Demand.kr(trig, 0, Dwhite(0.1, 0.9, inf)).lag(10);
			//ffreq = x.linexp(0, 1, 600.1, 800.3);
			//Demand.kr(trig, 0, Dwhite(600, 800, inf));
			freq = Demand.kr(trig, 0,
							Drand([110.5, 220.5, 330.5, 440.5, 550.5, 660.6], inf)
						).lag(2);
			//freq = LFNoise2.kr(x).linexp(0,1, 110.5, 660.5).lag(10);
			//freq = x.linexp(0, 1, 110.6, 660.6);//.lag(2);
			impulses = Blip.ar(freq/*200.15*/).tanh;
			vosim = VOSIM.ar(impulses, freq1, (3 * x) + 1, 0.99).tanh;
			fof = Formlet.ar(vosim, freq2,  0.01, x/*0.1*/).tanh;
			//verb = FreeVerb.ar(fof.tanh,
			//					Lag.kr(LFNoise1.kr.range(0.1, 0.9),5),
			//					Lag.kr(LFNoise1.kr.range(0.3, 0.7),5),
			//					Lag.kr(LFNoise1.kr.range(0.3, 0.7), 5));
			//biquad = SOS.ar(verb, x, 1-x, 0, (1-x)/5, x/5).tanh;
			//biquad = SOS.ar(fof, x, 1-x, 0, (1-x)/5, x/5).tanh;
			//moog = BMoog.ar(biquad, /*Lag.kr(LFNoise2.kr.range(8600, 800),10)*/
			moog = BMoog.ar(fof, /*Lag.kr(LFNoise2.kr.range(8600, 800),10)*/
							/*x.linexp(0, 1, 600.1, 800.3).lag(10)*/
							freq1.expexp(200,900,  600.1, 800.3).lag(10), q, 0);
			comb = CombC.ar(moog, 0.2, /*x.linexp(0, 1, 101.1, 900).reciprocal*/
						freq2.reciprocal, /*-0.2*/ -1 * x).tanh;

			//gverb = GVerb.ar(comb, x.linlin(0, 0, 5, 100), 5, maxroomsize:100).tanh;
			//gverb = gverb[0] + gverb[1];

			//shift = PitchShift.ar(gverb, 0.02, x).tanh * amp;

			Out.ar(out, comb * env);
		}).add;

		// don't change this until....

		s.sync;

		clock = BileClock(api).show;
		chat = BileChat(api);


		con = Conductor.make({|con, amp|

			var x, y, z;

			amp.spec_(\amp);
			x = con.addCV((api.nick ++ "/x").asSymbol);
			x.spec_(\unipolar);
			y = con.addCV((api.nick ++ "/y").asSymbol);
			y.spec_(\unipolar);
			z = con.addCV((api.nick ++ "/z").asSymbol);
			z.spec_(\unipolar);


			// shelly change this part to map your variables
			con.synth_(
				(instrument: \shellnote ),
				[
					amp: amp,
					freq1: [z, z.linexp(0.0, 1, 220.0, 880)],
					freq2: [y, ( 1- y).linexp(0.0, 1, 111.1, 880)],
					x: x
				]
			);

		});


		assign_gestural_control  = { |what, action|
			// shelly update this to be the variable for your touchosc
			controller.setAction(("a" ++ what).asSymbol, action);
		};

		// dont' change below this


		twiddle = { |tag|

			var conv, shared, controller;

			shared = SharedResource(0);
			conv = con[tag];

			shared.changeAction_(api, { |val|
				api.sendMsg(tag, val.value);
				//"reporting to api".postln;
			});

			api.add(tag, { |input|
				shared.value_(input, api);
			});

			shared.changeAction_(con, { |val|
				//was a start comment here

				//var cv;
				//cv = con[contag];
				//cv.value = val.value;
				conv.value = val.value;

				// was an end comment here
				//"that's reporting to cv".postln;
			});

			conv.action_({ |cv|
				shared.value_(cv.value, con);
			});

		};


		//con.addCV((api.nick ++ "/x").asSymbol);
		twiddle. ((api.nick ++ "/x").asSymbol);
		//con.addCV((api.nick ++ "/y").asSymbol);
		twiddle. ((api.nick ++ "/y").asSymbol);
		//con.addCV((api.nick ++ "/z").asSymbol);
		twiddle. ((api.nick ++ "/z").asSymbol);



		con.show;

		win = Window.new("XYZ", Rect (rrand(20,300),rrand(20,300), 310, 95));
		win.view.decorator=FlowLayout(win.view.bounds);

		user_update_action = {
			var tag, cv;

			user_names = api.colleagues.keys.collect({|k| k});




			(win.isClosed.not).if({
				AppClock.sched(0, {


					lists.do({ |popup|
						popup.items = ['none'] ++ api.nick.asSymbol ++  user_names;
					});
					user_names.do({ | name|
						["x", "y", "z"].do({|let|
							tag = ("" ++ name ++ "/" ++ let).asSymbol;
							con[tag].isNil.if({
								cv = con.addCV(tag);
								cv.spec_(\unipolar);
								twiddle.(tag);
							});
						})
					});
				nil;
				});
			});
		};




		user_update_action.value;
		api.add_user_update_listener(this, user_update_action );





		xyz_constructor = { |letter|
			var list, button, conv, composite, label;


			letter = letter.asSymbol; // just checking

			// make the GUI

			composite = CompositeView(win, Point(95, 100));
			composite.decorator=FlowLayout(composite.bounds);

			//label = StaticText(composite, Rect(45, 45, 15, 15));
			//label.string = letter.asString.toUpper;

			list = PopUpMenu(composite, Point(90, 30));
			list.items = ['none'] ++ api.nick.asSymbol ++  user_names;

			button = Button(composite, Point(90, 30))
				.states_([
				["Nada", Color.grey, Color.clear],
				["FIGHT!!!", Color.black, Color.yellow],
				["Win", Color.black, Color.green],
				["Lose", Color.black, Color.red]
				]);


			semaphores[letter] = Semaphore(1);


			// STEAL!!

			list.action = { |menu|
				var cv;

				controlling.isNil.if({"controlling is nil".warn});
				// first resign previous association
				//"previous %\n".postf(controlling[letter]);
				controlling[letter].notNil.if({
					// are we resigning from ourselves?
					if( controlling[letter].asSymbol == api.nick.asSymbol, {
						// no more self-abuse!
						//"--------me".postln;
						api.call('resign', api.nick.asSymbol, letter, api.nick.asSymbol)
					});
					//"--------tell all".postln;
					api.sendMsg('resign', controlling[letter].asSymbol,
							letter, api.nick.asSymbol);

				});

				(menu.value != 0).if ({
					//"steal".postln;
					api.sendMsg('steal', menu.item, letter, api.nick.asSymbol);

					// are we stealing ourselves?
					if (((menu.item.asSymbol == api.nick.asSymbol)
						/* && api.echo.not */), {
						// yes, we are playing with ourselves.
						api.call('steal', api.nick.asSymbol, letter, api.nick.asSymbol);
					});

					controlling[letter] = menu.item.asSymbol;

					cv = con[(controlling[letter] ++ "/" ++ letter).asSymbol];
					assign_gestural_control.(letter, {|val|
						cv.notNil.if({
							cv.value = val.value;
						})
					});

					// the fight is on!
					button.value = 1;

				}, {  // we switched to none
					//"switched to none".postln;
					assign_gestural_control.value(letter, {});
					//wii.setAction(("a" ++ letter).asSymbol, {});
					controlling[letter] = nil;

					// nothing happening
					button.value = 0;
				});

				//"leaving list action %\n".postf(letter);
				//".....".postln;
			};

			lists[letter] = list;
			buttons[letter] = button;

		};

		xyz_constructor.('x');
		xyz_constructor.('y');
		xyz_constructor.('z');

		win.front;

		//kinect.calibrate = false;


		// configure the API


		// listen for challengers
		api.add('steal', { arg from, what, who;

			// first just make sure everything is a symbol:
			from = from.asSymbol;
			what = what.asSymbol;
			who = who.asSymbol;

			//"in steal".postln;

			// This is in a task because I was going to use semaphores
			// because there could be race conditions in which multiple OSC
			// responders are trying to access the same variables at the same time,
			// but semaphores make my head hurt.
			// And there might be strange states where we're confused about what's
			// going on, but it probably won't crash.  Anyway, this piece is sort of
			// about relationships and relationships are like this.
			// ... at least mine are....
			Task({
				if (from == api.nick.asSymbol, { // it's ME!

					//"stealing from me! - wait".postln; // quits here
					//semaphores[what].wait;
					//"done waiting".postln;
					// are they fighting anybody for it?
					active_player.isNil.if({"active_player is nil".warn});

					active_player[what].isNil.if({ //no
						//"no fight".postln;
						// challenger wins immediately
						active_player[what] = who;
						api.sendMsg('winner', api.nick.asSymbol, what, who);
						//if( api.echo.not, {
							// alert ourselves also:
							//api.call('winner', api.nick.asSymbol, what, who);
						//});
						//"challenger wins".postln;

					} , { // there's a fight
						//"fight".postln;
						//add them to the challenger list, if there is one
						challenger[what].notNil.if({
							(challenger[what].includes(who)
							|| (active_player[what] == who)).not.if({
								challenger[what] = challenger[what] ++ [who];
							}, {
							//"duplication".postln;
							});
						} , {
							// or create the list if there isn't
							(active_player[what] != who).if({
								challenger[what] = [who];
							});
						});

						//      //semaphores[what].signal;

						// wait and pick a winner
						Task ({
							var winner;

							//"now, we wait".postln;

							9.5.wait; // SC users are impatient
							//semaphores[what].wait;
							// are there still challengers?
							challenger[what].isNil.if({ // no
								// is there an active player
								active_player[what].notNil.if({ // yes
									// active player wins by default
									api.sendMsg('winner', api.nick.asSymbol, what,
												active_player[what]);
									//api.echo.not.if({
										// alert ourselves also:
										api.call('winner', api.nick.asSymbol, what,
												active_player[what]);
									//});
								}) // nobody cares, no winner


							} , { // challengers remain!
								// ok, is there still a defender?
								active_player[what].isNil.if({ // no
									// challenger wins by default
									winner = challenger[what].choose;
									active_player[what] = winner;
								}, { // yes, the fight is ongoing
									// PROBABILITY
									0.75.coin.if({ // change probability here
										// challenger wins
										winner = challenger[what].choose;
										active_player[what] = winner;
									} , { // challenger looses
										winner = active_player[what];
									})
								});
								// clear the challnger list
								challenger[what] = nil;
								// announce a winner
								api.sendMsg('winner', api.nick.asSymbol, what, winner);
								//api.echo.not.if({
									// to ourselves also
									api.call('winner', api.nick.asSymbol, what, winner);
								//});
							});
							//semaphores[what].signal;

							0.5.wait;
							// renotify to make up for dropped packets

							// are there new challengers?
							challenger[what].isNil.if({
								// we wouldn't want to prematurely decide a new fight

								// is the winner still the active player?
								(active_player[what] == winner).if({
									// ok, re-alert the winner
									api.sendMsg('winner', api.nick.asSymbol, what, winner);
								});
							});

						}).play;
					}); // end of the is there a fight if
					//semaphores[what].signal;
				}); // else it's not me
				// now, lets' check if they're fighting us for something
				// this requires looking at the GUI, so it has to be in
				// one of those stupid AppClock things, which is one of the more
				// annoying facest of GUI programming in SuperCollider
				// I'm so sick of worrying about stupid thread handling issues in GUIs
				AppClock.sched(0.0, {
					//"from is ".post; from.postln;
					//"lists[what is".post; lists[what].item.asSymbol.postln;
					if (( from == lists[what].item.asSymbol), {
						// much like Jolene in Dolly Parton's best song
						// somebody is trying to steal our man
						// or our x, y or z as the case may be

						//"fighting with me".postln;
						// the line to update the gui can go here
						//because where already in an AppClock
						buttons[what].value = 1;
					});
					nil
				});
			}).play;
		}); // end api('steal)


		// did we win??
		api.add('winner', { arg from, what, who;

			var my_slave;

			// first just make sure everything is a symbol:
			from = from.asSymbol;
			what = what.asSymbol;
			who = who.asSymbol;

			//"a winner! = in winner % % %\n".postf(from, what, who);

			AppClock.sched(0.0, {

				//"in appclock".postln;

				my_slave = lists[what].item.asSymbol;
				//my_slave.postln;
				// does this effect us?

				if(my_slave == from, { // yes
					//"did i win?".postln;
					// did we lose?
					//api.nick.asSymbol.postln;
					//who.postln;
					//(who != api.nick.asSymbol).postln;
					if (who != api.nick.asSymbol, { //oh no!
						//"no".postln;
						assign_gestural_control.(what, {});
						//wii.setAction(("a" ++ what).asSymbol, {}); // kill the wii action
						lists[what].value = 0; // change the list

						controlling[what] = nil;

						// also, update the gui widget I haven't written yet
						buttons[what].value = 3;
					}, {


						//"we won".postln;
						// change the gui thingee that i wan't written yet to announce our
						// triumphalism
						buttons[what].value = 2;
					});

				}); // doesn't effect us, so we don't care

				nil;
			})

		}); // end api('winner')

		// Rick Astley is never gonna give us up
		// but some other bastard might
		api.add('resign', { arg from, what, who;

			// first just make sure everything is a symbol:
			from = from.asSymbol;
			what = what.asSymbol;
			who = who.asSymbol;

			//"..resign".postln;

			Task ({
				//semaphores[what].wait;
				//"..done waiting".postln;
				if (from == api.nick.asSymbol, { // it's me; you fucking bastard!
					//"..% resigns from my %\n".postf(who, what);
					// is this asshole currently a controller?
					active_player.notNil.if({
						active_player[what].notNil.if ({
							//"..we have an active_player %\n".postf(active_player[what]);
							(active_player[what] == who).if({ //yes
								//how dare you break up with us!!
								active_player[what] = nil; // boil their bunny
								//"..was the active player".postln;
							}, {/*"..not them".postln;*/})
						}, { /*"..no active player".postln*/}); // nobody was controlling us
					}, {
						//"..active_player is not initialised".warn;
					});
					//"..check challenger".postln;
					// were they just a challenger
					challenger.notNil.if({ //"..not nil".postln;
						//"..challenger is %\n".postf(challenger[what]);
						challenger[what].notNil.if({ //"..challenger[what] not nil".postln;
							challenger[what].includes(who).if({
								challenger[what].remove(who);
								//"..was a challenger".postln;
							}, {/*"..was not a challenger".postln;*/});
						}, { /*"..no challengers".postln*/});
					}, { /*"..challenger is not initialised".warn*/});
				}, {/*"..not me".postln;*/}); // it's not me, i'm self-centred, i don't care
				//"..ok, signal".postln;
				//semaphores[what].signal;
				//"..finishing resign".postln;
			}).play;
		}); // an end to the heartbreak

	});

	Task({
		5.wait;
		api.identify;
		inf.do ({
			10.wait;
			api.identify;
		});
		//user_update_action.value;
	}).play;

)



// don't forget to press play!!!!!






/*


* For the actual params:

/bile/player_name/[x|y|z] data

(the square brackets and | means pick one of those)


the player_names are:

Les
Shelly
Julien
Chris

(In all cases, player_name refers to the person who is actually
producing sound.)

capitalisation counts

So anybody controlling Juju's x would send:

/bile/Julien/x 0.5


* To take control of a param:

/bile/steal player_name [x|y|z] my_name

So when Chris want's to steal Juju's x, Chris sends:

/bile/steal/ Julien x Chris

After no more than 10 seconds, Julien's program should announce a winner:



* To announce a winner

/bile/winner player_name [x|y|z] winner

So, if Chris wins:

/bile/winner Julien x Chris

It is the responsibility of losers to STOP sending data once they've lost.



* To give up control

/bile/resign player_name [x|y|z] quitter

After Chris gets bored of controlling Juju or decides to drop out of a fight:

/bile/resign Julien x Chris


*/
	