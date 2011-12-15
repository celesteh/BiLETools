
BileChat {
	
	var <win, api, <string, disp, >growl, exists, view, <>color;
	
	* new { |net_api, show = true|
			^super.new.init(net_api, show)
	}
	
	init { |net_api, show = true|
		
		var api_methods, update_action, talk, user_list, user_update_action, side;
		
		
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
		
		color.isNil.if({ color = Color.rand});

		win = Window.new("Communication", Rect(128, 64, 510, 370));
		win.view.background_(color);
		win.view.decorator = FlowLayout(win.view.bounds);
		
		view = CompositeView(win, 510 @ 370);
		//view.decorator.gap=2@2;
		
		disp = TextView(view,Rect(10,10, 380,300))
			.editable = false;
			//.focus(true);
		disp.hasVerticalScroller = true;
		disp.autohidesScrollers_(true);
		//disp.autoScrolls = true;
		
		user_list = PopUpMenu(view,Rect(400,10,90, 30));
		
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
		
		api_methods = ListView(view ,Rect(400,50,90,250));
		api_methods.items = (["API Methods"] ++ api.functionNames 
							++ api.remote_functions.keys).
								collect({|a| a.asString});
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
		
		api.add('msg', { arg user, blah;
			(win.isClosed.not && exists).if ({
				AppClock.sched(0, {
					//disp.string = disp.string ++ "\n buh?";
					//disp.string = disp.string ++ "\n" + user ++">"+ blah;
					//string = disp.string;
					this.add(""++ user ++ ">" + blah);
					this.growlnotify(user, blah);
					nil;
				});
				[user, blah].postln;
			}, {
				api.remove('msg');
			})
		}, "For chatting. Usage: msg, nick, text");
		
		talk = TextView(view,Rect(10,330, 480,15))
			.focus(true);

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
		
		var str;
		
		str ="";
		
		notification.as(Array).do({|c|
			(c.ascii >= 0).if ({
				
				str = str++ c;
			})
		});
		
		disp.string = disp.string ++ "\n" ++ str;
		string = disp.string;
					
	}
	
	growlnotify { |user, blah|
		//var fuckyousc;
		//"this is getting called".postln;
		growl.if({
			//"shoudl growl".postln;
			//fuckyousc = blah;
			blah = blah.asString.replace("\\", "\\\\");
			blah = blah.asString.replace("\"", "\\\"");
			//"wtf".postln;
			("/usr/local/bin/growlnotify \"" ++ user ++ "\" -m \"" ++ blah ++ 
				"\" -a SuperCollider").unixCmd;
		});
	}	
	
	say { |blah|
		
		if (api.echo.not, {
			api.msg(api.nick, blah);
		});
		
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
	
	* new { |net_api, is_master = false|
		
		^super.new.init(net_api);
	}
	
	init { |net_api, is_master = false|
		
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

		master = is_master;

	}
	
	show {
		
		clock.notNil.if({
			clock.window.front;
		} , {
			clock = ClockFace.new;
		});
	}
	
	pr_start {
		clock.isNil.if({ AppClock.sched(0, {
			clock = ClockFace.new;
			master.if({
				this.master_(master); 
			});
			clock.play;
			
			nil
		}) }, {
			master.if({
				this.master_(master); 
			});
			clock.play;
		});
	}
	
	pr_stop {
		
		master = false;
		clock.isNil.if({ AppClock.sched(0, {clock = ClockFace.new; clock.stop; nil}) },
			{clock.stop; clock.onBeat = nil;});
	}
	
	start {
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
	}
	
	master_ { |is_master|
		
		master = is_master;
		clock.notNil.if({
		
			master.if ({	
				clock.onBeat_({ |time|
				
					((time %1) == 0).if({
						this.echoTime(time);
					});
				})
			} , {
				clock.onBeat = nil;
			});
		});
	}
	
	
	set { |minutes = 0, seconds = 0|
		var time;
		
		master.not.if({
			time  = (minutes * 60) + seconds;
			clock.isNil.if({ AppClock.sched(0, {clock = ClockFace.new(time); nil}) },
				{clock.cursecs_(time)});
		});
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
		clock.notNil.if({ clock.cursecs_(0) });
	}
	
	startAll{
		this.master = true;
		api.sendMsg('clock/clock', 'start');
		this.pr_start;
	}
	
	stopAll {
		this.master = false;
		api.sendMsg('clock/clock', 'stop');
		this.pr_stop;
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

ClockPanel {
	
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
}

