
BileChat {

	var <win, api, <string, disp, >growl, exists, view, <>color;

	* new { |net_api, show = true|
			^super.new.init(net_api, show)
	}

	init { |net_api, show = true|

		var api_methods, update_action, talk, user_list, user_update_action, side, flag;


		exists = true;

		api = net_api;
		api.isNil.if({
			api = NetAPI.default;
			api.isNil.if({
				Error("You must open a NetAPI first").throw;
			})
		});

		// do NOT initQuerying here
		// disable remoe query too in case of loop
		//api.remote_query;

		color.isNil.if({
			/*
			flag = true;
			{flag}.while({
				color = Color.rand;
				if (((color.red < 0.5)
					|| (color.green < 0.5)
					|| (color.blue < 0.5)), {
						flag = false;
				});
			});
			*/
			//color = Color(0.6.rand + 0.1, 0.6.rand, 0.6.rand + 0.1);
			color = BileTools.colour;
		});

		win = Window.new("Communication", Rect(128, 64, 500, 360));
		win.view.background_(color);
		win.view.decorator = FlowLayout(win.view.bounds, 10@10);
		win.view.decorator.gap=10@5;
		win.view.minWidth_(180);
		win.view.minHeight_(130);
		

		view = CompositeView(win, 480 @ 300);
		view.resize_(5);

		disp = TextView(view,Rect(0,0, 380,300))
			.editable = false;
		disp.resize_(5);
			//.focus(true);
		disp.hasVerticalScroller = true;
		disp.autohidesScrollers_(true);
		//disp.autoScrolls = true;

		user_list = PopUpMenu(view,Rect(390,0,90, 30));
		user_list.resize_(3);

		user_update_action = {
			var user_names;
			(win.isClosed.not && exists).if({
				AppClock.sched(0, {
					user_names = api.colleagues.keys.collect({|k| k});

					user_list.items = (["All APIs", "Remote Data" , api.nick] ++ user_names)
										.collect({|a| a.asString});
					api_methods.refresh;
					nil;
				});
			}, {
				api.remove_user_update_listener(this);
			});
		};
		user_update_action.value;
		api.add_user_update_listener(this, user_update_action );

		user_list.action = { arg menu;

			var key, user, list;

			list = [];

			(menu.value == 0 ).if({  // all
				//api.init_querying; // just in case // NO!!
				api.remote_query;
				list = /*api.functionNames.asArray ++*/ api.remote_functions.keys.asArray;
			}, {
				(menu.value == 1).if({  //remote shared data
					api.remote_query;
					list = api.remote_shared.keys.asArray;
				} , {
					(menu.value == 2).if ({ // me
						list = api.functionNames;
					} , {
						// anybody else
						//("somebody else" + api.colleagues.keys.asArray).postln;
						key = menu.item.asSymbol;
						//api.collagues.keys.asArray.postln;
						user = api.colleagues[key];
						user.notNil.if ({
							//"not nil".postln;
							list = user.callableFunctions;
							//"key % user % list %\n".postf(key, user.nick, list);
						});
					})
				})
			});

			list = list.collect({|a| a.asString});
			api_methods.items = list.asArray;
			win.isClosed.not.if({
				AppClock.sched(0, {
					api_methods.refresh;
					nil;
				});
			})
		};

		api_methods = ListView(view ,Rect(390,40,90,260));
		api_methods.items = (["API Methods"] ++ api.functionNames
							++ api.remote_functions.keys).
								collect({|a| a.asString});
		api_methods.resize_(6);
		/*
		update_action = {
			//"remote_action_update_listener_fucked_your_mom".postln;
			win.isClosed.not.if({
				AppClock.sched(0, {
					api_methods.items = ["API Methods"]
						++ api.functionNames++ api.remote_functions.keys;
					api_methods.refresh;
					nil;
				});
			}, {
				api.remove_remote_update_listener(this);
			});
		};
		*/
		api_methods.action = { |v|
			var index, dialog, desc_text, text, selector;

			index = v.value;
			//(index ==0). if ({
			//	api.init_querying; // just in case
			//	api.remote_query;
				//update_action.value
			//} , {
				// open a new window with the description
				selector = api_methods.items[index];
				dialog =  Window.new(selector, Rect (118,64, 200, 80));
				text = api.help(selector) ?? "No description available";
				(text.size == 0).if ({ text = "No description available"});

				desc_text = TextView(dialog.asView,Rect(10, 10, 170, 60))
					.string_(text)
					.hasVerticalScroller_(true)
					.autohidesScrollers_(true)
					.editable = false;
				dialog.front;
			//})
		};

		//api.add_remote_update_listener(this, update_action);
		growl = File.exists("/usr/local/bin/growlnotify");
		growl.not.if({ 
			\MandelHub.asClass.notNil.if({
				var pseudohub = (classPath:  { |that,filename| 
					(MandelHub.filenameSymbol.asString.dirname ++ "/" ++ filename);
				});
				Platform.case(
					\osx, {
						growl = MandelPlatformOSX(pseudohub);
					},
					\linux, {
						growl = MandelPlatformLinux(pseudohub);
					},
					{ // default
						this.post("Platform specific functions for your system aren't available.");
						growl = false;
					}
				);
			});
		});


		api.add('msg', { arg user, blah;

			AppClock.sched(0, {
				(win.isClosed.not && exists).if ({
					//disp.string = disp.string ++ "\n buh?";
					//disp.string = disp.string ++ "\n" + user ++">"+ blah;
					//string = disp.string;
					this.add(""++ user ++ ">" + blah);
					this.growlnotify(user, blah);
					//[user, blah].postln;
				}, {
					api.remove('msg');
				});
				nil;
			});
		}, "For chatting. Usage: msg, nick, text");

		win.view.decorator.nextLine;
		talk = TextView(win.view,480@30)
		.focus(true)
		.autohidesScrollers_(true);
		talk.resize_(8);

		talk.keyDownAction_({ arg view, char, modifiers, unicode, keycode;

			var blah;

			(char == 13.asAscii).if({
				blah = talk.string;
				blah = blah.stripRTF.tr(13.asAscii, $ ).tr(10.asAscii, $ ).replace("  ", " ");
				talk.string = "";
				this.say(blah);
			});


			talk.keyDownAction(view, char, modifiers, unicode, keycode);
		});

		show.if ({
			win.front;
		});

		win.onClose_({
			api.remove('msg');
			api.remove_user_update_listener(this);
			disp = nil;
			win = nil;
			this.release;
			"should be gone".postln;
			exists = false;
		});



	}


	show { |doit = true|

		if (doit, {
			win.front;
		});
	}

	add { |notification|

		var str, bounds, scrolled;

		str ="";

		notification.as(Array).do({|c|
			(c.ascii >= 0).if ({

				str = str++ c;
			})
		});
		AppClock.sched(0, {
			//disp.string = disp.string ++ "\n" ++ str;
			disp.respondsTo('setString').if ({
				disp.setString("\n"++str, disp.string.size, 0);
			} , {
				disp.string = disp.string ++ "\n" ++ str;
			});

			string = disp.string;

			scrolled = false;

			disp.respondsTo('innerBounds').if({
				bounds = disp.innerBounds;
				//"bottom is at %\n".postf(bounds.bottom);
 				disp.respondsTo('visibleOrigin').if ({
					disp.visibleOrigin = Point(0,bounds.bottom);
					//"Set point at %\n".postf(bounds.bottom);
					scrolled = true;
				});
			});

			scrolled.not.if({
				disp.respondsTo('select').if ({
					str = disp.selectedString;
					str.notNil.if({
						(str.size < 1).if ({ // don't wipe out the user's selection
							disp.select(disp.string.size+1, 0);
						});
					});
				})
			});


			nil
		});

	}

	growlnotify { |user, blah|
		//var fuckyousc;
		//"this is getting called".postln;
		(growl == true).if({
			//"shoudl growl".postln;
			//fuckyousc = blah;
			blah = blah.asString.replace("\\", "\\\\");
			blah = blah.asString.replace("\"", "\\\"");
			//"wtf".postln;
			("/usr/local/bin/growlnotify \"" ++ user ++ "\" -m \"" ++ blah ++
				"\" -a SuperCollider").unixCmd;
		},{
			(growl != false).if({
				growl.displayNotification(user.asString, blah.asString);
			});
		});
	}

	say { |blah|

		//if (api.echo.not, {
		//	api.msg(api.nick, blah);
		//});

		api.sendMsg('msg', api.nick, blah);
	}

	name_{|name|
		name.notNil.if({
			win.name = name.asString;
		});
	}


}


BileClock {

	var api, <clock, <master;
	var <win, <view;
	var <startingtime, <>tempo, <>inc, <cursecs, isPlaying = false, timeString;
	var remFun, <mod, startTime, <>onMod, <>onBeat, startedAt, startButton;

	/* This class is a fork of the ClockFace quark */


	* new { |net_api, starttime = 0, tempo = 1, inc = 0.1, window, is_master = false|

		^super.new.init(net_api, starttime, tempo, inc, window, is_master);
	}

	init { |net_api, starttime = 0, tempo = 1, inc = 0.1, window, is_master = false|

		var text, startButton, resetButton;

		this.tempo = tempo;
		this.inc = inc;
		startingtime = starttime;

		api = net_api;
		api.isNil.if({
			api = NetAPI.default;
			api.isNil.if({
				Error("You must open a NetAPI first").throw;
			})
		});

		api.add('clock/clock', { |command|

			//command.postln;
			this.master = false;

			command.asSymbol.switch(

				'start', { this.pr_start },
				'stop', {this.pr_stop }
			)
		}, "Start or stop the clock. Usage: clock/clock start or clock/clock stop");

		api.add('clock/reset', { this.reset}, "Reset the clock");

		api.add('clock/set', {|minutes, seconds| this.set(minutes, seconds);}, "Set the clock time."
				+ "Usage: clock/set minutes, seconds");

		cursecs = starttime;


		master = is_master;

		window.notNil.if({this.show(window)});

	}

	show { |window|
		/*
		clock.notNil.if({
			clock.window.front;
		} , {
			clock = ClockFace.new;
		});
		*/

		var text, resetButton;

		win = window;

		win.isNil.if({
			win = Window("Clock", Rect(0, 0, 450, 80));
			win.view.background_(BileTools.colour);
			view = win.view;
			view.isNil.if({ view = win});
		}, {
			win.view.decorator.isNil.if({
				 win.view.decorator = FlowLayout(win.view.bounds);
			});
			view = CompositeView(win, (win.view.bounds.width - 2) @ 30);
		});

		view.decorator = FlowLayout(view.bounds);
		view.decorator.gap=10@5;


		timeString = StaticText.new(view, 240@80)
			.string_(cursecs.asTimeString)
			.font_(Font("Arial", 40));

		// add a button to start and stop the clock.
		startButton = Button(view, 85 @ 20);
		startButton.states = [
			["Start Clock", Color.black, Color.green(0.7)],
			["Stop Clock", Color.white, Color.red(0.7)]
		];
		startButton.action = {|view|
			if (view.value == 1, {

				this.startAll;
			} , {
				this.master = true;
				this.stop;
			});
		};

		resetButton = Button(view, 85 @ 20);
		resetButton.states = [
			["Reset Clock", Color.white, Color.blue(0.7)]];
		resetButton.action = {|view|
			this.resetAll;
		};


		win.front;
		win.onClose_({this.pr_stop});

	}

	pr_start {

		var cur, last, floor;

		master.if({
			this.master_(master);
		});

		last = 0.0;
		isPlaying = true;
		clock = TempoClock.new(tempo);
		startedAt = clock.elapsedBeats;
		remFun = {this.pr_stop};
		CmdPeriod.add(remFun);

		("starting tempo" + tempo + "inc" + inc).postln;

		clock.sched(inc, {
			//"tick".postln;
			isPlaying.if({
				cur = clock.elapsedBeats - startedAt + startingtime;
				mod.notNil.if({
					cur = cur%mod;
					(cur < last).if({
						{onMod.value;
							this.onBeat.value(cur.floor.asInt);
						}.defer;
					});
				});
				this.cursecs_(cur, false);
				//cur.postln;
				onBeat.notNil.if({
					(((floor = cur.floor) - last.floor) == 1).if({
						{this.onBeat.value(floor.asInt)}.defer;
					});
				});
				last = cur;
				inc;
			}, { nil});
		});

		AppClock.sched(0, {
			startButton.value = 1;
			nil;
		});
	}

	pr_stop {

		master = false;
		startingtime = cursecs;
		isPlaying = false;
		clock.clear;
		CmdPeriod.remove(remFun);
		clock.stop;

		AppClock.sched(0, {
			startButton.value = 0;
			nil;
		});

		//clock.isNil.if({ AppClock.sched(0, {clock = ClockFace.new; clock.stop; nil}) },
		//	{clock.stop; clock.onBeat = nil;});
	}

	play { this.start }

	start { |time|

		time.notNil.if ({ this.set(0, time)});
		master.if({
			this.startAll;
		} , {
			this.pr_start;
		});
	}

	stop {
		master.if({
			this.stopAll;
		} , {
			this.pr_stop;
		});
		this.master = false;
		this.onBeat = nil;
	}

	master_ { |is_master|

		master = is_master;
		clock.notNil.if({

			master.if ({
				this.onBeat_({ |time|

					((time %1) == 0).if({
						this.echoTime(time);
					});
				})
			} , {
				this.onBeat = nil;
			});
		});
	}


	set { |minutes = 0, seconds = 0|
		var time;

		master.not.if({
			time  = (minutes * 60) + seconds;
			this.cursecs_(time, isPlaying.not);
		});
	}

	time {
		clock.notNil.if({
			^clock.elapsedBeats - startedAt + startingtime;
		} , {^0});
	}

	cursecs_ {arg curtime, updateStart = true;
		var curdisp;
		cursecs = curtime;
		curdisp = curtime.asTimeString;
		curdisp = curdisp[0 .. (curdisp.size-3)];
		updateStart.if({startingtime = cursecs;
		});
		timeString.notNil.if({
			//curdisp.postln;
			AppClock.sched(0, {timeString.string_(curdisp); nil})});
	}


	reset {
		/*
		clock.notNil.if({
			AppClock.sched(0, {
				clock.stop;
				clock.window.close;
			nil })});

		AppClock.sched(0.1, {
			clock = ClockFace.new; nil
		});
		*/
		//this.set(0, 0);
		this.cursecs_(0);
	}

	startAll{
		this.master = true;
		api.sendMsg('clock/clock', 'start');
		this.pr_start;
		clock.sched(0.5,{
			isPlaying.if({
				api.sendMsg('clock/set', (cursecs / 60).floor, cursecs % 60);
				0.5
			},{
				nil
			});
		});
		Task({
			0.001.wait;
			api.sendMsg('clock/clock', 'start');
			0.001.wait;
			api.sendMsg('clock/clock', 'start');
		}).play;
	}

	stopAll {
		this.master = false;
		api.sendMsg('clock/clock', 'stop');
		this.pr_stop;
		Task({
			0.01.wait;
			api.sendMsg('clock/clock', 'stop');
			0.01.wait;
			api.sendMsg('clock/clock', 'stop');
		}).play;
	}

	setAll{ |minutes, seconds|
		this.master = true;
		api.sendMsg('clock/set', minutes, seconds);
		this.set(minutes, seconds);
	}

	resetAll {
		this.master = true;
		api.sendMsg('clock/reset');
		this.reset;
	}

	echoTime { |time|

		var minutes, seconds;

		minutes = (time / 60).floor;
		seconds = time % 60;

		api.sendMsg('clock/set', minutes, seconds);
	}

}

ClockPanel : BileClock {
	/*
	var <win, <view, api, <clock;

	*new { |api, win, clock|
		^super.new.init(api, win, clock);
	}

	init  {|a, w, c|

		var text, startButton, resetButton;

		api = a;
		clock = c;

		clock.isNil.if({
			clock = BileClock(api);
		});


		win = w;

		win.isNil.if({
			win = Window("Clock Control");
			win.view.background_(Color.rand);
			view = win.view;
			view.isNil.if({ view = win});
		}, {
			win.view.decorator.isNil.if({
				 win.view.decorator = FlowLayout(win.view.bounds);
			});
			view = CompositeView(win, (win.view.bounds.width - 2) @ 30);
		});

		view.decorator = FlowLayout(view.bounds);
		view.decorator.gap=5@5;

		// add a button to start and stop the sound.
		startButton = Button(view, 85 @ 20);
		startButton.states = [
			["Start Clock", Color.black, Color.green(0.7)],
			["Stop Clock", Color.white, Color.red(0.7)]
		];
		startButton.action = {|view|
			if (view.value == 1, {
				clock.show;
				clock.startAll;
			} , {
				clock.stopAll;
			});
		};

		resetButton = Button(view, 85 @ 20);
		resetButton.states = [
			["Reset Clock", Color.white, Color.blue(0.7)]];
		resetButton.action = {|view|
			clock.resetAll;
		};
	}

	show {
		win.front;
	}
	*/
}

DeviceDialog {

	*new {|s, action|

		super.new.init(s, action)
	}

	*show{|s, action|

		super.new.init(s, action)
	}

	init{ |s, action|

		var options;
		var indevices, outdevices;
		var win, view, in, out, button;

		Platform.case(\osx, {
			s.isNil.if({ s = Server.default});

			options = s.options;
			indevices = ServerOptions.inDevices;
			outdevices = ServerOptions.outDevices;

			AppClock.sched(0, {
				win =  Window("Audio Devices", 450@80);
				win.view.background_(BileTools.colour);
				view = win.view;
				view.decorator = FlowLayout(view.bounds);
				view.decorator.gap=10@5;

				in = EZPopUpMenu(view, 230@22, "Input Device",
					[\Default -> 	{ options.inDevice = nil; }] );

				indevices.do({ |device|
					in.addItem(device.asSymbol, { options.inDevice = device}) });

				out = EZPopUpMenu(view, 230@22, "Output Device",
					[\Default -> 	{ options.inDevice = nil; }] );

				outdevices.do({ |device|
					out.addItem(device.asSymbol, { options.outDevice = device}) });

				button = Button(view, 115@22).states_([["Start"]]).action_({win.close});

				win.onClose_({
					s.waitForBoot(action);
				});

				win.front;

				nil;
			});
		},
		\linux,		{"Not Supported. Use Jack instead.".warn; s.waitForBoot(action)});
	}
}


BileTools {

	*show { |api|

		var chat, clock;

		chat = BileChat(api);
		clock = BileClock(api);

		AppClock.sched(0, {
			chat.show;
			clock.show;
			nil;
		});

		^[chat, clock]
	}

	*colour {
		^ Color(0.6.rand + 0.1, 0.6.rand, 0.6.rand + 0.1);
	}

}
