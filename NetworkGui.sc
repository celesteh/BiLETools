NetworkGui : Environment {

	var api;
	var local, shared, remote;
	var <players, synthDefs;
	var <copyRemote, <mountRemote;
	var changed, major_change;
	var win, view, <name, <color, <>light_color;
	var <>redrawRate, clock;
	var <>gui_items; // not used
	var keys;
	var layout, user_layout;
	var <>manyPlayers;
	var <sharePlayers;
	var <sharedfunc;

	classvar guitypes;

	*initClass {

		guitypes = IdentityDictionary.new;
		guitypes.putPairs([


			\number, {|cv, view, key, netgui, sub = 10, bgcolor|
				var spec;


				spec = cv.spec ? key.asSymbol.asSpec;
				bgcolor = bgcolor ? BileTools.light_colour;

				BTNumber(
					key.asString,
					spec,
					{|ez| cv.value_(ez.value, netgui)},
					cv.value,
					false,
					true
				).background_(bgcolor);
			},

			\slider, {|cv, view, key, netgui, sub = 10, bgcolor|
				var spec;


				spec = cv.spec ? key.asSymbol.asSpec;
				bgcolor = bgcolor ? BileTools.light_colour;

				//view.decorator.nextLine;
				BtSlider(
					key.asString,
					spec,
					{|ez| cv.value_(ez.value, netgui)},
					cv.value,
					false,
					\horizontal,
					true
				).background_(bgcolor); //.setColors(Color.grey.(0.2, 0.8),Color.white, Color.grey
				//(0.7),Color.grey, Color.white, Color.yellow).background_(Color.yellow);//.layout_(view.layout);

				/*
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
				*/
			},

			\text, {|cv, view, key, netgui, sub = 10, bgcolor|
				//var spec;

				bgcolor = bgcolor ? BileTools.light_colour;

				////arg label, action, initVal, initAction=false, orientation=\horz;
				BtText(
					key.asString,
					{|ez| cv.value_(ez.value, netgui)},
					cv.value,
					false,
					\horizontal
				).background_(bgcolor);
			},


			\vslider, {|cv, view, key, netgui, sub = 10, bgcolor|
				var spec;


				spec = cv.spec ? key.asSymbol.asSpec;
				bgcolor = bgcolor ? BileTools.light_colour;

				//view.decorator.nextLine;
				BtSlider(
					key.asString,
					spec,
					{|ez| cv.value_(ez.value, netgui)},
					cv.value,
					false,
					\vertical,
					true
				).background_(bgcolor);
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
			// arg label, controlSpec, action, initVal, initAction=false, mode=\vert, viewUnits=false;
			\knob,	{|cv, view, key, netgui, sub=10, bgcolor|
				var spec;

				spec = cv.spec ? key.asSymbol.asSpec;
				bgcolor = bgcolor ? BileTools.light_colour;

				BtKnob(
					key.asString,
					spec,
					{|ez| cv.value_(ez.value, netgui)},
					cv.value,
					false,
					\vert,
					true
				).background_(bgcolor);
			},
			\none, {|cv, view, key, netgui, sub=10, bgcolor| nil; }   //,
			/*
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
			},*/
			/*
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
			*/
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

		^super.new.make_init(api, local, func);
	}


	*addGui{ |tag, func|
		//{|cv, view, key, netgui ... args| returns an gui object of some kind
		guitypes.put(tag, func);
	}


	makeArgs { arg func, local;
		var argList, size, names, argNames;

		// Set this deinfitely before we make any widgets
		light_color = light_color ? BileTools.light_colour;


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

	assignArgs { arg func;
		var argList, size, names, argNames;

		// Set this deinfitely before we make any widgets
		light_color = light_color ? BileTools.light_colour;


		size = func.def.argNames.size;
		argList = Array(size);
		//argNames = Array(size);
		names = func.def.argNames;
		argList = argList.add(this);
		if (size > 1, {
			// first arg is this, next args are local variables, then shared ones
			1.forBy(size - 1, 1, { arg i;
				name = names[i];
				argList = argList.add(this.at(name));
				//local.notNil.if({
				//	local.includes(name).if({
						//argNames = argNames.add(name);
				//		argList = argList.add(this.addLocal(name))
				//	} , {
				//		argList = argList.add(this.addShared(name));
				//	});
				//}, {
				//	argList = argList.add(this.addShared(name));
				//});
				//gui_items = gui_items.add(name);
			});
		});

		^argList;
	}


	code {|func, changer|
		var arg_list, funcstr;

		changer = changer ? this;

		func.notNil.if({
			arg_list = this.assignArgs(func);
			funcstr = func.asCompileString;
			sharePlayers.notNil.if({
				sharePlayers.if({
					(changer == this).if({
						// send this out
						sharedfunc.isNil.if({
							sharedfunc = SharedResource(funcstr);
							sharedfunc.mountAPI(api, "code/%".format(name).asSymbol, "% code".format(name));
							api.add("code/%".format(name).asSymbol, {|newfuncstr, nick|
								newfuncstr.debug(nick);
								(nick != api.nick).if({
									sharedfunc.value_(newfuncstr, nick);
								});
							});
						} , {
							sharedfunc.value_(funcstr, changer);
							api.sendMsg("code/%".format(name).asSymbol, funcstr, api.nick);
						});
					});
				});
			});
			func.value(*arg_list)
		});
	}

	sharePlayers_ {|bool|

		sharePlayers = bool;
		sharePlayers.if({
			// start listening
			sharedfunc = SharedResource("");
			sharedfunc.mountAPI(api, "code/%".format(name).asSymbol, "% code".format(name));
			sharedfunc.action = { "% code changed".format(name).postln; }
		});
	}


	make_init {|net_api, local_sym, func|

		var res, scv, arg_list, shared_sym;


		manyPlayers = true;
		api = net_api;

		local_sym = local_sym ? [];

		local = Dictionary.new;
		shared = Dictionary.new;
		remote = Dictionary.new;
		players = [];
		keys = [];
		gui_items = [];

		func.notNil.if({ arg_list = this.makeArgs(func, local_sym)});

		mountRemote = false;
		copyRemote = false;

		func.notNil.if({ func.value(*arg_list)});

		//this.pr_make_gui;
	}


	pr_makegui {|w|

		var startButton, cv;

		//color.isNil.if({ color = Color(0.6.rand + 0.1, 0.6.rand, 0.6.rand+ 0.1);});
		color = color ? BileTools.colour;
		light_color = light_color ? BileTools.light_colour;

		//win = w;

		//win.isNil.if({
		//	win = Window(name);
		//	win.background_(color);
			//view = win.view;
			//view.isNil.if({ view = win});
			//}, {
			//	win.view.decorator.isNil.if({
			//		 win.view.decorator = FlowLayout(win.view.bounds);
			//	});
			//	view = CompositeView(win, (win.view.bounds.width - 2) @ 30);
		//});
		//view.postln;
		//view = view ? win;

		w.notNil.if({ view = w.asView });
		view.isNil.if({
			//"looping?".debug(this);
			view = View();
			view.background_(color);
			this.vTitle = name;
		});

		//view.decorator = FlowLayout(view.bounds);
		//view.decorator.gap=2@2;

		/*
		// add a button to start and stop the sound.
		startButton = Button(/*view, 75 @ 20*/);
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
		BileTools.hintSize(startButton);
		startButton.resizeToHint;
		startButton.resize = 1;
		*/
		//view.layout = VLayout( HLayout([startButton, stretch:0], nil)  );
		//view.layout.spacing = 2;

		layout.notNil.if({
			view.layout = layout;
		}, {
			view.layout = VLayout( this.pr_header );
		});
		//view.decorator.nextLine;
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

	pr_header {

		var startButton;
		//win.isNil.if({
		//	"no window!".warn;
		//});

		// add a button to start and stop the sound.
		startButton = Button(/*view, 75 @ 20*/);
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
		BileTools.hintSize(startButton);
		startButton.resizeToHint;
		startButton.resize = 1;

		^HLayout([startButton, stretch:0], StaticText().string_(name), nil);

	}

	pr_major_gui_update {

		var bounds, widgets, widget;

		view.isNil.if({

			this.pr_makegui; // we don't have a gui
		} , {
			// we do have a gui and need to add new sliders or change the name

			//"major update".debug(this);
			widgets = [];

			major_change = false;

			name.notNil.if({
				//win.name = name.asString;
				this.vTitle_(name.asString);
			});

			layout.isNil.if({ // if you set your own layout, this is your problem

				keys = keys.flatten;
				//keys.postln;

				widgets = keys.collect({|key|

					widget = this.getWidget(key);
					widget.notNil.if({
						view.layout.add(widget);
						view.layout.setStretch(widget, 0);
					});
					//widgets = widgets ++ widget;
					widget;

				});

				widgets = widgets.reject({|w| w.isNil }).asList;

				this.class.equalise(widgets);
				widgets.do( _.resizeToHint);

			}, { /*"own layout".postln;*/ });

			bounds = view.bounds;//win.bounds;
			//bounds.height = win.view.decorator.top + 35;
			//bounds.width = 442;
			//win.bounds_(bounds);
			BileTools.hintSize(view);
			//win.front;

		});
	}

	asView {
		view.isNil.if({ this.pr_makegui });
		^view
	}

	color_ {|c|
		color = c;
		this.asView.background_(color);
	}


	//show { |doit = true|

	//	if (doit, {
	//		//view.front;
	//		this.win.front;
	//	});
	//}

	win { // get the top level View
		var lineage = this.asView.parents;

		lineage.isNil.if({
			^view
		});
		^lineage.last;
	}

	vTitle_{|name|
		name.notNil.if({
			//win.name = name.asString;
			this.win.name = name.asString;
		});
	}



	keys {
		^keys.flatten;
	}

	layout_{|newlayout|

		var mylayout = VLayout(
			this.pr_header,
			newlayout
		);

		user_layout = newlayout;

		view.notNil.if({
			//"immediately live".postln;
			view.layout= mylayout;
		});

		layout = mylayout;
	}


	addWidget{|widget, regenerate=true|

		//"add a widget";

		user_layout.notNil.if({
			//"add to layout".postln;
			user_layout.add(widget);
			user_layout.setStretch(widget, 0);
		}, {
			regenerate.not.if({
				//"add to view".postln;
				view.notNil.if({
					widget.isKindOf(SharedCV).if({
						widget = widget.widget;
					});
					view.layout.add(widget);
					view.layout.setStretch(widget, 0);
				});
			}, {
				//"push it off".postln;
				major_change = true;
			});
		});
	}

	*equalise{ |widgets|

		var biggest;

		//"Equalise widths".postln;
		// make all the labels the same width
		biggest =widgets.maxItem({|w| w.respondsTo(\labelWidth).if ({w.labelWidth}, {0}) });

		//"biggest is %".format(biggest).debug(this);

		biggest.respondsTo(\labelWidth).if ({
			//biggest.controlSpec.postln;
			widgets.do({|w| w.respondsTo(\labelWidth_).if({ w.labelWidth = biggest.labelWidth })});
			//widgets.do({|w| w.respondsTo(\labelWidth_).if({ w.labelWidth = 50 })});
		});

		// make all units the same width
		biggest =widgets.maxItem({|w| w.respondsTo(\unitWidth).if  ({w.unitWidth}, {0}) });
		//biggest.controlSpec.postln;

		biggest .respondsTo(\unitWidth_).if({
			widgets.do({|w| w.respondsTo(\unitWidth_).if({w.unitWidth = biggest.unitWidth })});
		});
		//widgets.do({|w| w.respondsTo(\unitWidth_).if({w.unitWidth = 100 })});
		//widgets.do({|w| w.respondsTo(\unitWidth_).if({ w.unitWidth_(100) })});

		biggest =widgets.maxItem({|w| w.respondsTo(\numberWidth).if  ({w.numberWidth}, {0}) });
		//biggest.controlSpec.postln;
		//"numberWidth".postln;
		biggest.respondsTo(\numberWidth_).if({
			widgets.do({|w| w.respondsTo(\numberWidth_).if({w.numberWidth = biggest.numberWidth })});
		});
	}

	// this.class.getCVWidget(cv, key, view, this, bgcolor);
	*getCVWidget{|cv, key, viewToSpecify, mvc_agent, bgcolor|
		var gui_builder, guiWidget;

		cv.notNil.if({
			cv.widget.isNil.if({


				cv.guitype.isNil.if({
					cv.spec.notNil.if({
						cv.spec.isKindOf(EmptySpec).if({
							cv.guitype = \text;
						});
					});
				});
				cv.guitype = cv.guitype ? \slider;

				cv.guitype.isKindOf(Function).if({
					gui_builder = cv.guitype;
				} , {
					gui_builder = guitypes[cv.guitype];

					gui_builder.isNil.if({
						cv.guitype = \slider;
						gui_builder = guitypes[cv.guitype];
					});
				});

				//"new".postln;
				//"gui_build args %, %, %, %, %, %".format(cv, viewToSpecify, key, mvc_agent, 10, bgcolor).postln;
				cv.widget = gui_builder.value(cv, viewToSpecify, key, mvc_agent, 10, bgcolor);

			});

			guiWidget = cv.widget;
		});

		^guiWidget;

	}

	getWidget {|key, bgcolor, layoutClass, equalise_collections=true|

		var cv, gui_builder, guiWidget, collected_widgets = [];

		//"get widget key %".format(key).postln;

		layoutClass = layoutClass ? HLayout;
		bgcolor = bgcolor ? light_color;

		key.isKindOf(Collection).if({
			collected_widgets = key.collect( this.getWidget(_)); // see Partial Application helpfile
			equalise_collections.if({
				this.equalise(collected_widgets);
			});
			guiWidget = layoutClass.new(*collected_widgets);
		}, {

			key.isKindOf(SharedCV).if({
				cv = key;
				key = cv.tag;
			}, {
				cv = this[key];
			});

			//"getCVWidget args % % % % %".format(cv,key,view, this,bgcolor).postln;
			guiWidget = this.class.getCVWidget(cv, key, view, this, bgcolor);

			// We do nils on purpose now
			//guiWidget.isNil.if ({ "no such item %".format(key.asString).warn; });


		});

		^guiWidget;
	}

	getWidgets { |bgcolor, layoutClass, equalise_collections|

		^ this.keys.collect( this.getWidget(_, bgcolor, layoutClass, equalise_collections) );
	}

	pr_gui_update {


		// this should only be called from the gui update loop
		// we're just going to assume the window isn't nil


		//win.isClosed.not.if ({
		view.isClosed.not.if ({
			//"the win is open".postln;

			major_change.if({
				//"major_change is true".postln;
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


			//win.refresh;
			view.refresh;

			^redrawRate;
		}, {
			//"we think the win is closed".postln;
			^nil; // stop redrawing
		});

		^redrawRate;
	}

	addLocal {|key, item, redraw_all, silent = false|
		^this.pr_add(key, item, true, redraw_all, silent:silent);
	}


	addShared {|key, item, redraw_all, owned, silent = false|
		^this.pr_add(key, item, false, redraw_all, owned, silent:silent);
	}

	addUnsigned{|key, item, redraw_all|

	}

	addRemote {|key, redraw_all|

		var tag, res, rcv, split, spec;

		tag = key.asSymbol;

		remote[tag].isNil.if({

			//"adding remote".postln;

			res = api.subscribe(tag);
			res.action_(this, {this.update});
			res.action_(api, {|val|  api.sendMsg(tag, val.value)});
			//res.action_({"fuck yeah".postln}); // does the res action ever actually get called?   YES!!
			rcv = SharedCV(this, res);
			//if( cv.copy_spec, {rcv.spec = cv.spec});
			split = tag.asString.split($/); // strip the user name from the front
			spec = (split[1] ? split[0]).asSymbol.asSpec;
			rcv.spec = spec;
			remote.put(tag, rcv);
			this.put(tag, rcv);
			keys = keys ++ tag;

			redraw_all = redraw_all ? layout.notNil;

			redraw_all.if({
				major_change = redraw_all;
			}, {
				//getCVWidget(cv, key, view, this, bgcolor);
				this.addWidget(this.class.getCVWidget(rcv, tag, view, this, light_color));
			});
			//rcv.action_(this, {"changed".postln; this.update});
			//this.remote_update_action;

		});

		^rcv;
	}


	pr_add {|key, item, is_local, redraw_all, owned, silent=true|
		var spec;

		silent = silent ? true;

		"pr_add key %".format(key).debug(this);

		item.notNil.if({

			//ok what sort of item is this?
			item.isKindOf(SharedCV).if({

				//local.put(key, item);
			} , {

				item.isKindOf(SharedResource).if({
					is_local.if({
						item = SharedCV.local(this, item, api, key);
					}, {
						item = SharedCV.shared(this, item, api, key, owned:owned, silent:silent);
					});
					item.action_(this, {this.update});

				} , {
					is_local.if({
						item = SharedCV.local(this, SharedResource(item), api, key);
					}, {
						item = SharedCV.shared(this, SharedResource(item, silent:silent), api, key, owned:owned);
					});
					item.action_(this, {this.update});
			})});

		} , {
			is_local.if({
				item = SharedCV.local(this, SharedResource(0), api, key);
			} , {
				item = SharedCV.shared(this, SharedResource(0, silent:silent), api, key, owned:owned);
			});

			item.action_(this, {this.update});
		});

		item.spec.isNil.if({
			spec = ControlSpec.specs[key.asSymbol];
			if (spec.isNil) {
				spec = ControlSpec.specs[key.asString.select{ | c | c.isAlpha}.asSymbol]
			};
			if (spec.isNil) {

				if (item.value.isKindOf(String)) {
					spec = \string.asSpec;
					item.guitype = \text;
				};
				if (item.value.isKindOf(Symbol)) {
					spec = \symbol.asSpec;
					item.guitype = \text;
				};
				if (item.value.isKindOf(SimpleNumber)) {
					(item.value.abs > 1).if({
						item.guitype = \number
					})
				};
				if (item.value.isKindOf(Boolean)) {
					(item.value.abs > 1).if({
						item.guitype = \number
					})
				};
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

		redraw_all = redraw_all ? layout.isNil;

		redraw_all.if({
			major_change = redraw_all;
		}, {
			// getCVWidget(cv, key, view, this, bgcolor);
			this.addWidget(this.class.getCVWidget(item, key, view, this, light_color));
		});

		//item.value.postln;

		^item;
	}

	unsilence {
		shared.keys.do({|k|
			shared.at(k).silent= false;
		});
	}

	pr_getActivePlayers{
		var active = players.select({|pl| pl.isPlaying});
		^active;
	}

	synth_{ |evt, dict|
		var synth, old, isPlaying;

		isPlaying = (this.pr_getActivePlayers().size > 0);

		synth = SharedResourceEvent.synth(evt, dict);
		manyPlayers.if({
			players = players.add(synth);
			isPlaying.if({
				synth.play;
			});
		} , {
			isPlaying = (this.pr_getActivePlayers().size > 0);
			this.stop;
			players = [synth];
			isPlaying.if({
				this.play;
			});
		});
	}

	pattern_ { |pat|
		var patPlayer, old, active, isPlaying;

		active = this.pr_getActivePlayers;
		isPlaying = (active.size > 0);
		//isPlaying.debug(this);

		manyPlayers.if({
			patPlayer = BilePatternPlayer(pat);
			players = players.add(patPlayer);
		} , {
			players.do({|pl|
				pl.respondsTo(\set).if({
					pl.set(\gate, 0);
				});
			});
			patPlayer = BilePatternPlayer(Pdef(this, pat));
			//players = [patPlayer]
			// we don't want to lose past players in case we accidentally get two going at once
			players = active.add(patPlayer);
		});

		isPlaying.if({
			patPlayer.play;
		});

	}

	synthDef_{|def|

		synthDefs.notNil.if({
			synthDefs.includes(def.name).not.if({
				synthDefs = synthDefs.add(def.name);
			});
		} , {
			synthDefs = [def.name];
		});

		api.synthDefs.add(def);
	}

	user_update_action {

		var user_names, tag, rcv, res;

		user_names = api.colleagues.keys.collect({|k| k});

		if (copyRemote, {
			//"copyRemote".postln;
			user_names.do({ |name|
				//name.postln;
				shared.keysValuesDo({|key, cv|

					(name.asSymbol != api.nick.asSymbol).if ({
						//name.postln;
						name.isNil.if({"wtf".warn});
						tag = ("" ++ name ++ "/" ++ key).asSymbol;

						//tag.postln;

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


		//"remote update action".postln;

		if (mountRemote, {
			//"mountRemote".postln;
			api.remote_shared.keys.do({|key|
				tag = key.asSymbol;
				//tag.postln;
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
			pl.respondsTo(\set).if({
				pl.set(\gate, 0);
			});
			pl.stop;
		});
		// only keep the last one if we're not multiPlayer
		manyPlayers.not.if({
			(players.size > 0).if({
				players = [players.last]
			});
		});
	}

	pause{
		players.do ({|pl|
			pl.respondsTo(\pause).if({
				pl.pause;
			});
		})
	}

	resume{
		players.do ({|pl|
			pl.respondsTo(\resume).if({
				pl.resume;
			})
		})
	}



	mapHID { |key, element|
		var scv;

		scv = local[key];
		if (scv.isNil, {
			scv = shared[key];
		});
		if(scv.isNil, {
			scv = remote[key];
		});

		scv.notNil.if({
			scv.attachHID(element);
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

	show { |argwin|
		var bounds;

		//win = argwin ? win;

		changed = false;
		major_change = false;

		this.pr_makegui(/*win*/argwin); // dont' reset the view unless necessary

		(view.notNil && layout.notNil).if({
			view.layout_(layout);
		});

		//bounds = this.win.bounds;
		//bounds.height = this.win.view.decorator.top + 35;
		//bounds.width = 442;
		//this.win.bounds_(bounds);
		BileTools.hintSize(/*this.win*/view);
		this.win.front;

		//redrawRate.isNil.if({ redrawRate = 0.1});
		redrawRate = redrawRate ? 0.1;

		clock = AppClock.sched(redrawRate, { this.pr_gui_update; });

	}

}


SharedCV {

	var <shared, <network, <spec, <widget, <container, <copy_spec, <tag, <>guitype, bus;

	*new {|container, shared, network, key, owned|
		^super.new.init(container, shared, network, key);
	}


	*local { |container, shared|
		^super.new.init(container, shared)
	}

	*shared { |container, shared, api, key, owned|
		^super.new.init(container, shared, shared.mountAPI(api, key, "slider input range is 0-1", owned:owned))
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


	sp	{ | default= 0, minval = 0, maxval=0, step = 0, warp = 'lin', changer |
		this.spec_(ControlSpec(minval, maxval, warp, step, default), changer:changer);
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

		"cv changed".debug(this);
		shared.value_(val, changer, *moreArgs);
		widget.notNil.if({
			// save it for the redraw loop!
			//widget.value = val
		});

	}

	silent_{|bool|
		shared.silent_(bool);
	}

	slient { ^shared.bool }

	silentValue_{|val, changer ... moreArgs|

		//"cv changed".postln;
		shared.silentValue_(val, changer, moreArgs);
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
				gui.respondsTo(\controlSpec).if({
					gui.controlSpec = spec;
				});
			});
		});
		widget = gui;
	}

	attachHID{ |element|

		element.action_({ |val|
			shared.value_(spec.map(val.value), element);
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

	// debugging bullshit
	guiClass {

		^SharedCVDebugGUI; // thi is just for debugging
	}

}

SharedCVDebugGUI : ObjectGui {

	guiBody{  arg layout;

		model.shared.value.gui(layout);
	}
}



// ControlSpecs for non-numeric Items


EmptySpec : Spec {

	var <warp, <>default;

	*new {|warp, default|
		^super.newCopyArgs(warp, default).init;
	}


	*newFrom { arg similar;
		^this.new(similar.warp, similar.default);
	}

	init {
		warp = warp.asWarp(this);
	}

	units { ^"" }

	map { arg value;
		// maps a value from [0..1] to spec range
		^warp.map(value);
	}
	unmap { arg value;
		// maps a value from spec range to [0..1]
		^warp.unmap(value);
	}

	constrain { arg value;
		^value;
	}




	*initClass {
		Class.initClassTree(Warp);

		// This is dodgy, but I get a circular dependancy otherwise
		Warp.warps.put(\empty, Warp);
		Warp.warps.put(\symbol, SymbolWarp);
		Warp.warps.put(\bool, BooleanWarp);

		specs = specs.addAll([
			\string -> EmptySpec(\empty, ""),
			\symbol -> EmptySpec(\symbol, "".asSymbol),
			\bool -> EmptySpec(\bool, true)
		]);
	}

}

SymbolWarp : Warp {

	map {|value|
		value.isKindOf(Symbol).if({
			^value
		});
		^value.asString.asSymbol;
	}

	unmap {|value|
		value.isKindOf(Symbol).if({
			^value
		});
		^value.asString.asSymbol;
	}
}

BooleanWarp : Warp {

	map {|value|
		value.isKindOf(Boolean).if({
			^value
		});

		value.isKindOf(SimpleNumber).if({
			^(value != 0) ;   // 0 is false, everything else is true
		});

		value.isKindOf(Symbol).if({
			value = value.asSting;
		});

		value.isKindOf(String).if({
			^value.asBoolean;
		});

	}

	unmap {|value|
		^value
	}

}






// This class nicked from the Conductor classes by Ron Kuivila

BilePatternPlayer  {

	var <>clock, <>event, <>quant, <eventStreamPlayer, <>pattern;

	*new { |pattern, clock, event, quant|
		^super.newCopyArgs(clock ? TempoClock.default, event ? Event.default, quant ? 0).bppInit(pattern);
	}

	bppInit {|pat|
		pattern = Pdef(this, pat);
	}



	play { |argClock, argEvent, argQuant|
		clock = argClock ? clock;
		event = argEvent ? event;
		quant = argQuant ? quant;

		eventStreamPlayer = pattern.play(clock, event.value, quant)
	}

	fadeTime {
		pattern.respondsTo(\fadeTime).if({
			^pattern.fadeTime;
		});
		^nil;
	}
	fadeTime_ {arg ...args;
				pattern.respondsTo(\fadeTime_).if({
			^pattern.fadeTime_(*args);
		});
		^nil;
	}

	isPlaying {
		eventStreamPlayer.notNil.if({
			^eventStreamPlayer.isPlaying;
		}, {
			^false;
		})
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
