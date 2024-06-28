NetworkGui : Environment {

	var api;
	var local, shared, remote;
	var <players;
	var <copyRemote, <mountRemote;
	var changed, major_change;
	var <win, view, <name, <>color;
	var <>redrawRate, clock;
	var <>gui_items;
	var keys;

	classvar guitypes;

	*initClass {

		guitypes = IdentityDictionary.new;

		guitypes.putPairs([

			\slider, {|cv, view, key, netgui, sub = 10| view.decorator.nextLine;
									EZSlider(
										view,
										(view.bounds.width-sub) @ 20,
										key.asString,
										cv.spec ? key.asSymbol.asSpec,
										{|ez| cv.value_(ez.value, netgui)},
										cv.value,
										unitWidth:30
									).setColors(Color.grey,Color.white, Color.grey
									(0.7),Color.grey, Color.white, Color.yellow);
						},
			/*
			\vslider, {|cv, view, key, netgui| EZSlider(
										view,
										//20 @ (view.bounds.width-10),
										label: key.asString,
										controlSpec: cv.spec ? key.asSymbol.asSpec,
										action: {|ez| cv.value_(ez.value, netgui)},
										initVal: cv.value,
										unitWidth:30,
										layout: \vert
									).setColors(Color.grey,Color.white, Color.grey
									(0.7),Color.grey, Color.white, Color.yellow);
						},
			*/
			\knob,	{|cv, view, key, netgui| EZKnob(
										view,
										50 @ 90,
										label: key.asString,
										controlSpec: cv.spec ? key.asSymbol.asSpec,
										action: {|ez| cv.value_(ez.value, netgui)},
										initVal: cv.value,
										unitWidth:20
									).setColors(Color.grey,Color.white, Color.grey
									(0.7),Color.grey, Color.white, Color.yellow);
						},
			\voter, 	{|cv, view, key, netgui, sub = 10| EZVoter(
										view,
										(view.bounds.width-sub) @ 20,
										key.asString,
										cv.spec ? key.asSymbol.asSpec,
										{|ez| /*"ez change".postln;*/ cv.value_(ez.value, netgui)},
										cv.value,
										unitWidth:30
									).setColors(Color.grey,Color.white, Color.grey
									(0.7),Color.grey, Color.white, Color.yellow);
						},

				/*,
			\slider2d, {|cvs, view, keys, netgui|
					var labelSize, numsize, labelView, numberView, slider, spec;

					labelSize = 30 @ 20;
					numsize = 40 @ 20;

					labelView = [
						GUI.staticText.new(view, labelSize),
						GUI.staticText.new(view, labelSize)];

					labelView[0].string = keys[0];
					labelView[1].string = keys[1];

					numberView = [
						GUI.numberBox.new(view, numsize),
						GUI.numberBox.new(view, numsize)
					];

					spec  = [
						cvs[0].spec ? keys[0].asSymbol.asSpec,
						cvs[1].spec ? keys[1].asSymbol.asSpec
					];

					slider = Slider2D(view, 60 @ 60)
						.action_({ |sl|
							cvs[0].input_(sl.x, netgui);
							cvs[1].input_(sl.y, netgui);
							numberView[0] = cvs[0].value;
							numberView[1] = cvs[1].value;
						});

					cvs[0].action_(slider,
						{ |val|
							AppClock.sched(0.0, {
								slider.x = cvs[0].input;
							})
						});

					cvs[0].action_(slider,
						{ |val|
							AppClock.sched(0.0, {
								slider.y = cvs[1].input;
							})
						});

					slider;
			}*/
		])
	}

	*new { |api, local, func|

		"You're really meant to use make".warn;
		^super.new.make_init(api, local, func);
	}


	*make { |api, local, func|
		"output".postln;
		^super.new.make_init(api, local, func);
	}


	makeArgs { arg func, local;
		var argList, size, names, argNames;

		size = func.def.argNames.size;
		argList = Array(size);
		//argNames = Array(size);
		names = func.def.argNames;
		argList = argList.add(this);
		if (size > 1, {
			// first arg is this, next args are local variables, then shared ones
			1.forBy(size - 1, 1, { arg i;
				name = names[i];
				local.notNil.if({
					local.includes(name).if({
					//argNames = argNames.add(name);
					argList = argList.add(this.addLocal(name))
					} , {
						argList = argList.add(this.addShared(name));
					});
				}, {
					argList = argList.add(this.addShared(name));
				});
				gui_items = gui_items.add(name);
			});
		});

		^argList;
	}


	make_init {|net_api, local_sym, func|

		var res, scv, arg_list, shared_sym;

		"foo".postln;

		api = net_api;
		local = Dictionary.new;
		shared = Dictionary.new;
		remote = Dictionary.new;
		players = [];
		keys = [];
		gui_items = [];

		arg_list = this.makeArgs(func, local_sym);

		mountRemote = false;
		copyRemote = false;

		func.value(*arg_list);

		//this.pr_make_gui;
	}


	pr_makegui {|w|

		var startButton, cv;

		color.isNil.if({ color = Color(0.6.rand + 0.1, 0.6.rand, 0.6.rand+ 0.1);});

		win = w;

		win.isNil.if({
			win = Window(name);
			win.view.background_(color);
			view = win.view;
			view.isNil.if({ view = win});
		}, {
			win.view.decorator.isNil.if({
				 win.view.decorator = FlowLayout(win.view.bounds);
			});
			view = CompositeView(win, (win.view.bounds.width - 2) @ 30);
		});
		//view.postln;

		view.decorator = FlowLayout(view.bounds);
		view.decorator.gap=2@2;

		// add a button to start and stop the sound.
		startButton = Button(view, 75 @ 20);
		startButton.states = [
			["Start", Color.black, Color.green(0.7)],
			["Stop", Color.white, Color.red(0.7)]
		];
		startButton.action = {|view|
			if (view.value == 1, {
				this.play;
			} , {
				this.stop;
			});
		};

		view.decorator.nextLine;
		/*
		keys = keys.flatten;

		keys.do({|key|
			"making gui".postln;
			//view.decorator.nextLine;
			cv = this.at(key);

			/*
			cv.widget = EZSlider(
				view,
				(view.bounds.width) @ 20,
				key.asString,
				cv.spec ? key.asSymbol.asSpec,
				{|ez| cv.value_(ez.value, this);/* "%\t%\t%\n".postf(key, cv.tag, cv.value)*/},
				cv.value,
				unitWidth:30
			)
				.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
					Color.white, Color.yellow);
			*/
			cv.guitype.isNil.if({
				cv.guitype = \slider;
			});

			guitypes[cv.guitype].isNil.if({
				cv.guitype = \slider;
			});

			cv.widget = guitypes[cv.guitype].value(cv, view, key, this);
		});
		*/

		major_change = true;
		this.pr_major_gui_update;

		//win.bounds= win.view.decorator.bounds;
	}

	pr_major_gui_update {

		var bounds, cv, gui_builder;

		win.isNil.if({

			this.pr_makegui; // we don't have a gui
		} , {
			// we do have a gui and need to add new sliders or change the name

			"major update".postln;

			major_change = false;

			name.notNil.if({
				win.name = name.asString;
			});


			keys = keys.flatten;

			keys.do({|item|



				item.isKindOf(Collection).if ({
					// thing goes on a line by itself
					win.view.decorator.nextLine;
				});

				item.do({ |key|

					cv = this[key];

					cv.widget.isNil.if({

						cv.guitype.isNil.if({
							cv.guitype = \slider;
						});

						cv.guitype.isKindOf(Function).if({
							gui_builder = cv.guitype;
						} , {
							gui_builder = guitypes[cv.guitype];
							gui_builder.isNil.if({
								cv.guitype = \slider;
								gui_builder = guitypes[cv.guitype];
							});
						});

						"new".postln;
						cv.widget = gui_builder.value(cv, view, key, this, 10);
					});
				});

				item.isKindOf(Collection).if ({
					// thing goes on a line by itself
					win.view.decorator.nextLine;
				});

			});

			bounds = win.bounds;
			bounds.height = win.view.decorator.top + 35;
			//bounds.width = 442;
			win.bounds_(bounds);
			//win.front;

		});
	}

	pr_gui_update {


		// this should only be called from the gui update loop
		// we're just going to assume the window isn't nil

		//"pr_gui_update".postln;

		win.isClosed.not.if ({
			//"the win is open".postln;

			major_change.if({
				"major_change is true".postln;
				this.pr_major_gui_update;
			});

			changed.if({

				changed = false;
				local.keysValuesDo({|key, cv|
					cv.value.notNil.if({
						cv.widget.notNil.if({
							cv.widget.value = cv.value;
						}, {
							("" ++ cv.tag ++ "is nil").warn;
						});
					});
				});
				shared.keysValuesDo({|key, cv|
					cv.value.notNil.if({
						cv.widget.notNil.if({
							cv.widget.value = cv.value;
						}, {
							("" ++ cv.tag ++ "is nil").warn;
						});
					});
				});
				remote.keysValuesDo({|key, cv|
					cv.value.notNil.if({
						cv.widget.notNil.if({
							cv.widget.value = cv.value;
						}, {
							("" ++ cv.tag ++ "is nil").warn;
						});
					});
				});
			});


			win.refresh;

			^redrawRate;
		}, {
			//"we think the win is closed".postln;
			^nil; // stop redrawing
		});

		^redrawRate;
	}

	addLocal {|key, item|
		^this.pr_add(key, item, true);
	}


	addShared {|key, item|
		^this.pr_add(key, item, false);
	}


	addRemote {|key|

		var tag, res, rcv, split, spec;

		tag = key.asSymbol;

		remote[tag].isNil.if({

			"adding remote".postln;

			res = api.subscribe(tag);
			res.action_(this, {this.update});
			res.action_(api, {|val| "to api".postln; api.sendMsg(tag, val.value)}); // les just added update network
			//res.action_({"fuck yeah".postln}); // does the res action ever actually get called?   YES!!
			rcv = SharedCV(this, res);
			//if( cv.copy_spec, {rcv.spec = cv.spec});
			split = tag.asString.split($/); // strip the user name from the front
			spec = (split[1] ? split[0]).asSymbol.asSpec;
			rcv.spec = spec;
			remote.put(tag, rcv);
			this.put(tag, rcv);
			keys = keys ++ tag;
			major_change = true;
			//rcv.action_(this, {"changed".postln; this.update});
			//this.remote_update_action;

		});

		^rcv;
	}


	pr_add {|key, item, is_local|
		var spec;

		item.notNil.if({

			//ok what sort of item is this?
			item.isKindOf(SharedCV).if({

				//local.put(key, item);
			} , {

			item.isKindOf(SharedResource).if({
				is_local.if({
					item = SharedCV.local(this, item, api);
				}, {
					item = SharedCV.shared(this, item, api);
				});
				item.action_(this, {this.update});

			} , {
				is_local.if({
					item = SharedCV.local(this, SharedResource(item), api);
				}, {
					item = SharedCV.shared(this, SharedResource(item), api);
				});
				item.action_(this, {this.update});
			})});

		} , {
			is_local.if({
				item = SharedCV.local(this, SharedResource(0), api);
			} , {
				item = SharedCV.shared(this, SharedResource(0), api);
			});

			item.action_(this, {this.update});
		});

		item.spec.isNil.if({
			spec = ControlSpec.specs[key.asSymbol];
			if (spec.isNil) {
				spec = ControlSpec.specs[key.asString.select{ | c | c.isAlpha}.asSymbol]
			};

			item.spec = spec;
		});

		is_local.if({
			local.put(key, item);
		} , {
			shared.put(key,item);
		});
		this.put(key, item);
		keys = keys ++ key;
		major_change = true;

		item.value.postln;

		^item;
	}

	synth_{ |evt, dict|
		var synth;
		//dict.notNil.if({
		//	dict.keysValuesDo({|key, val|
		//		val.isKindOf(SharedCV).if({
		//			dict[key] = val.shared;
		//		});
		//	});
		//});
		synth = SharedResourceEvent.synth(evt, dict);
		players = players.add(synth);
	}

	pattern_ { |pbind|
		var pat;

		pat = BilePatternPlayer(pbind);
		players = players.add(pat);
	}

	user_update_action {

		var user_names, tag, rcv, res;

		user_names = api.colleagues.keys.collect({|k| k});

		if (copyRemote, {
			"copyRemote".postln;
			user_names.do({ |name|
				name.postln;
				shared.keysValuesDo({|key, cv|

					(name.asSymbol != api.nick.asSymbol).if ({
						name.postln;
						name.isNil.if({"wtf".warn});
						tag = ("" ++ name ++ "/" ++ key).asSymbol;

						tag.postln;

						remote[tag.asSymbol].isNil.if ({
							res = api.subscribe(tag);
							res.action_(this, {this.update});
							rcv = SharedCV(this, res);
							cv.notNil.if({if( cv.copy_spec, {
									rcv.spec = cv.spec;
									rcv.guitype = cv.guitype;
							});});
							remote.put(tag, rcv);
							this.put(tag, rcv);
							keys = keys ++ tag;
							major_change = true;
						})
					})
				})
			})
		});

	}

	remote_update_action {

		var user_names, tag, rcv, res, spec, split;


		"remote update action".postln;

		if (mountRemote, {
			"mountRemote".postln;
			api.remote_shared.keys.do({|key|
				tag = key.asSymbol;
				tag.postln;
				remote[tag].isNil.if({
					res = api.subscribe(tag);
					res.action_(this, {this.update});
					rcv = SharedCV(this, res);
					//if( cv.copy_spec, {rcv.spec = cv.spec});
					split = tag.asString.split($/); // strip the user name from the front
					spec = split[1].asSymbol.asSpec;
					rcv.spec = spec;
					remote.put(tag, rcv);
					this.put(tag, rcv);
					keys = keys ++ tag;
					major_change = true;
					//api.add(tag, {|input| shared.value_(input, this)});

				})
			})
		});
	}

	copyRemote_ { |bool|

		copyRemote = bool;

		if (copyRemote, {

			this.user_update_action;
			api.add_user_update_listener(this, {this.user_update_action});
		});
	}

	mountRemote_ { |bool|

		mountRemote = bool;

		if (mountRemote, {

			this.remote_update_action;
			api.add_remote_update_listener(this, {this.remote_update_action});
		});
	}

	update {

		changed = true;
	}


	play { |clock, event, quant|
		players.do ({|pl|
			pl.play(clock, event.value, quant);
		})
	}

	stop {
		players.do ({|pl|
			pl.stop;
		})
	}

	pause{
		players.do ({|pl|
			pl.pause;
		})
	}

	resume{
		players.do ({|pl|
			pl.resume;
		})
	}



	mapHID { |key, slot|
		var scv;

		scv = local[key];
		if (scv.isNil, {
			scv = shared[key];
		});
		if(scv.isNil, {
			scv = remote[key];
		});

		scv.notNil.if({
			scv.attachHID(slot);
		});
	}

	n_ {|n|

		//local.do({|scv|
		//	scv.n = n;
		//});
		shared.do({|scv|
			scv.n = n;
		});
		remote.do({|scv|
			scv.n = n
		})
	}

	name_ {|na|

		name = na;
		major_change = true;
	}

	show { |win|
		var bounds;

		changed = false;
		major_change = false;

		this.pr_makegui(win);

		bounds = this.win.bounds;
		bounds.height = this.win.view.decorator.top + 35;
		//bounds.width = 442;
		this.win.bounds_(bounds);
		this.win.front;

		redrawRate.isNil.if({ redrawRate = 0.1});

		clock = AppClock.sched(redrawRate, { this.pr_gui_update; });

	}

}


SharedCV {

	var <shared, <network, <spec, <widget, <container, <copy_spec, <tag, <>guitype, bus;

	*new {|container, shared, network, key|
		^super.new.init(container, shared, network, key);
	}


	*local { |container, shared|
		^super.new.init(container, shared)
	}

	*shared { |container, shared, api, key|
		^super.new.init(container, shared, shared.mountAPI(api, key, "slider input range is 0-1"))
	}

	init { |gui, sr, net, key|

		container = gui;
		shared = sr;

		net.isKindOf(NetAPI).if ({
			network = shared.mountAPI(net, key)
		} , {
			network = net;
		});
		tag = key;
		copy_spec = true;
		//shared.action_(container, {|val| container.value_(val.value)}); // Les
	}

	spec_{|s, default_value, changer|
		spec = s.asSpec;
		widget.notNil.if({
			widget.controlSpec = spec;
		});
		shared.init_value(default_value ? spec.default, changer);
	}


	sp	{ | default= 0, lo = 0, hi=0, step = 0, warp = 'lin', changer |
		this.spec_(ControlSpec(lo,hi, warp, step, default), changer:changer);
	}

	local_spec_{|s, default_value, changer|
		copy_spec = false;
		this.spec_(s, default_value, changer);
	}

	local_sp {| default= 0, lo = 0, hi=0, step = 0, warp = 'lin', changer |
		copy_spec = false;
		this.sp(default, lo, hi, step, warp, changer);
	}

	input_ {|val, changer ... moreArgs|

		var mapped;

		mapped = spec.map(val);
		shared.value_(mapped, changer, moreArgs);
		widget.notNil.if({
			// save it for the redraw loop!
			//widget.value = mapped
		});
	}

	input {

		^spec.unmap(shared.value)
	}


	value_{|val, changer ... moreArgs|

		//"cv changed".postln;
		shared.value_(val, changer, moreArgs);
		widget.notNil.if({
			// save it for the redraw loop!
			//widget.value = val
		});

	}

	value {

		^shared.value
	}

	action_{ |... args|

		shared.action_(*args);
	}

	// network throttling

	n_ { |n|
		network.notNil.if({
			network.n = n;
		})
	}

	widget_ { |gui|

		gui.notNil.if ({
			//shared.action_(gui, { |val|
			//	gui.value_(spec.unmap(val.value))
			//});


				spec.notNil.if({
					gui.controlSpec = spec;
				});
		});
		widget = gui;
	}

	attachHID{ |slot|

		slot.action_({ |val|
			shared.value_(spec.map(val.value), slot);
		})
	}

	// Strem and Pattern Support
	asStream {}
	next { ^shared.value }
	reset { }
	embedInStream { ^shared.value.yield }


	// this is a bit outside of the normal CV stuff, but if you want coninuous control
	// of something, this is probably easier than sending messages to every event

	bus { |s|

		s = s ? Server.default;
		if ( bus.isNil, {
			bus = Bus.control( s, 1 );
		},{
			if ( bus.index.isNil, {
				bus = Bus.control( s, 1 );
			});
		});

		shared.action_(bus, { |v| bus.set( v.value ); });

		^bus
	}

	freeBus {
		shared.removeAction(bus);
		bus.free;
		bus = nil;
	}

// JITLib support
	kr{
		/*@
		desc: JitLib support
		@*/
		^In.kr( this.bus );
	}


}

// This class nicked from the Conductor classes by Ron Kuivila

BilePatternPlayer {

	var <>pattern, <>clock, <>event, <>quant, eventStreamPlayer;

	*new { |pattern, clock, event, quant|
		^super.newCopyArgs(pattern, clock ? TempoClock.default, event ? Event.default, quant ? 0)
	}

	play { |argClock, argEvent, argQuant|
		clock = argClock ? clock;
		event = argEvent ? event;
		quant = argQuant ? quant;

		eventStreamPlayer = pattern.play(clock, event.value, quant)
	}

	pause { eventStreamPlayer.pause }
	resume { eventStreamPlayer.resume }
	stop { eventStreamPlayer.stop; eventStreamPlayer = nil }
}



/*
(

	var api, matrix;

	api = NetAPI.broadcast("Les");

	matrix = NetworkGui.make(api, [\amp], {|gui, amp, x, y, z, freq, freq1|

		amp.spec_(\amp);
		x.spec_(\unipolar);
		y.spec_(\unipolar);
		z.spec_(\bipolar);
		freq1.spec_(\freq);

		gui.copyRemote = true; // will create x, y, z sliders for all other users

		gui.name = "XYZ";

		gui.synth_(
			(
				instrument: \default
			),
			(
				amp: amp,
				x:	x,
				y:	y,
				z:	[z, {|z| z * 2}],
				freq:freq
			)
		);

		//x.attachHID(wii[\ax]);
		//y.attachHID(wii[\ay]);
		//z.attachHID(wii[\az]);

	});

	matrix.addLocal(\db);
	matrix.addLocal(\bipolar);

	matrix.show;

	m = matrix;
)

*/
