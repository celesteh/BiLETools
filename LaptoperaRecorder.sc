// version 0.017-alpha


LaptoperaRecorder {
	
	var <win, api, view;
	var <buffer, <>bufferAction, <>nilAction, <>startAction, <>stopAction;
	var oldbuf, menus, section, latest, s;
	var <dir, <>synthSampleDir;
	var line, recording, syn, recBuf, tmpfile;
	
	*initClass {
		
		StartUp.add({
			
					
			SynthDef("LaptoperaRecorder",{ arg in = 0, bufnum, dur, 
							running=1.0, trigger=0.0, amp = 0.5, gate; 
				var inner, env;
				inner = SoundIn.ar(in).tanh;
				DiskOut.ar(bufnum, inner);
				//EnvGen.kr(Env.sine(dur), doneAction: 2);
				env = EnvGen.kr(Env.linen(0.01, dur-0.02, 0.01, amp, \sin), doneAction: 2);
				Out.ar(1, inner * env);
			}).store;
	
			SynthDef(\LaptoperRecordingPlayer, {|out = 0, bufnum = 0, amp =0.5|
				var player;
				player = PlayBuf.ar(1, bufnum, loop:0, doneAction: 2);
				player = [player, player];
				Out.ar(out,  player * amp);
			}).store
		});	
	}
	
	* new { |net_api, s, dir, synthSampleDir, show = true, win|
			^super.new.init(net_api, s, dir, synthSampleDir, show, win)
	}
	
	* max { |net_api, s, dir, show = true, win|
		^super.new.maxinit(net_api, s, dir, show, win)
	}
	
	dir_ {|directory|
		(directory.last != $/). if({
			directory = directory ++ "/";
		});
		dir = directory.standardizePath;
	}
			
	maxinit {|net_api, serv, directory, show = true, window|
	
		var colour, dir_gui, section_gui, line_gui;
		var incButton, button;
		
		api = net_api;
		s = serv;

		DeviceDialog(s, { recBuf = Buffer.alloc(s, 65536, 1);});
		
		this.dir_(directory);
		
		recording = false;

		
		
		colour = Color(0.6.rand + 0.1, 0.6.rand, 0.6.rand+ 0.1); //Color.new(0.9, 0.1, 0.3);
		win = window ? Window("Recorder");
		win.view.background_(colour);
		win.view.decorator = FlowLayout(win.view.bounds);
		//view = CompositeView(win, (win.view.bounds.width / 2) @ 30);
		//view = win.view;
		view = CompositeView(win, (win.view.bounds.width @ win.view.bounds.height));

		view.decorator = FlowLayout(view.bounds);
		view.decorator.gap=10@10;

		 dir_gui =  EZText (view,
		 	300@30,
		 	"Directory",
		 	{|ez| 
			 	dir = ez.value.asString; 
			 	dir = dir.standardizePath;
			 },
		 	dir,labelWidth:100);
		section = "A";
		
		section_gui = EZPopUpMenu(view,
		 	300@30,
		 	"Section",
		 	[
		 		\s2a-> {|a| section = "A";},
		 		\s2b-> {|a| section = "B";},
		 		\s2c-> {|a| section = "C";}
		 	],labelWidth:100);
		 	
		 line = SharedResource(-1);
		 
		 line_gui = EZText (view,
		 	300@30,
		 	"Line Number", {}, line.value, labelWidth:100);
		 
		 line_gui.action_(
		 	{|ez|  
			 	recording.not.if({ // don't change while recording
				 	line.value_(ez.value.asString.asInteger, line_gui);
			 	}, { // in fact, change back
				 	ez.value = line.value;
			 	});
		 	});
		 
		 line.action_(line_gui, {|val| 
			 "line action".postln;
			 AppClock.sched(0, {line_gui.value_(val.value); nil; });
		 });

		line.value_(0, this);

		incButton = Button(view, 30 @ 30).states_([["+"]]). action_({this.increment});
		
		button = Button(view, 200@30).states_([
			["Record", Color.red, Color.green(0.7.rand + 0.3)],
			["Stop", Color.black, Color.red(0.7.rand + 0.3)]
		])
		.action_({|but|
			(but.value==1).if ({
				this.startRecording
			},{ // pressed record
				this.stopRecording;
			})
		});
		//.align_(\center);

		show.if({
			win.front;
		});	

		"max is done".postln;
	}	
	
	init { |net_api, serv, directory, syn_dir, show = true, window|
		
		var doMenu, loadFile, play_button;
		
		
		this.maxinit(net_api, serv, directory, false, window);
		
		(syn_dir.last != $/). if({
			syn_dir = syn_dir ++ "/";
		});
		synthSampleDir = syn_dir.standardizePath;

		menus = IdentityDictionary(know: true);
		
		doMenu = { |tag|
			
			var sampleMenu, label, dedbuf;
		
			label = tag.asString + "files";
			
			sampleMenu = EZPopUpMenu(view, 300@30, label, labelWidth: 100);
			sampleMenu.addItem("unselected", {
				
				dedbuf = oldbuf;
				oldbuf = buffer;
				
				dedbuf.notNil.if({
					dedbuf.close;
					dedbuf.free;
				});
				buffer = nil;
				
				nilAction.notNil.if({ nilAction.value });
			});
			
			sampleMenu;
		};
		
		menus.put('a', doMenu.("A"));
		menus.put('b', doMenu.("B"));
		menus.put('c', doMenu.("C"));
		menus.put('synth', doMenu.("Synth sample"));
	
		loadFile = { |file, index|
			
			Buffer.read(s, file, action: {|b|
						
				var dedbuf;
				
				// don't delete the old buffer right away, store it in oldbuf
				// so your synths using the old buffer won't perish as soon as you switch	
				dedbuf = oldbuf;
				oldbuf = buffer;
				buffer = b;
				
				bufferAction.notNil.if({
					bufferAction.value(b);
				});
				
				dedbuf.notNil.if({
					dedbuf.close;
					dedbuf.free;
				});
						
				// make sure the other menus don't have the wrong thing highlighted
				AppClock.sched(0, {
					menus.keys.do({|key|
						(key != index).if({
							menus.at(key).value_(0);
						});
					});
				nil;
				});
			});
		};
			
		api.add('newfile', {arg filename;
			
			var file, index, menu, men_i;
			
			"newfile".postln;
			
			index = filename.asString.at(0).toLower.asSymbol;
			menu = menus.at(index);
			
			("index is" + index).postln;
			
			file = (dir ++ filename).asString.standardizePath;
			("file is" + file).postln;
			("menu is found?" + menu.notNil).postln;

			(File.exists(file) && menu.notNil).if ({ // sanity check
				"we are sane".postln;
				latest = filename;
				
				//menu.items.includes(filename.asString).not.if({
					AppClock.sched(0, {
						
						menu.items.do({|assoc, i|
							(assoc.key.asString.compare(latest.asString) == 0).if({
								// found it
								men_i = i;
								//break;
							})
						});
			
						men_i.isNil.if({

							"adding".postln;
							menu.addItem(filename.asString, {
								loadFile.(file, index);
							});
						});
						nil;
					});
				//});
			});
		});
		
		("synthSampleDir is" + synthSampleDir).postln;
		
		synthSampleDir = (synthSampleDir ++ "/*.*").pathMatch;
		synthSampleDir.do({| sample|
			menus.at('synth').addItem(sample.asString, {
				loadFile.(sample, 'synth');
			});
		});

		play_button = Button(view, 100@30).states_([
    			["Play", Color.red, Color.green],
    			["Stop Playing", Color.black, Color.red]
   			])
   			.action_({|but|
    				(but.value==1).if ({
     				startAction.notNil.if({
      					startAction.value; })
    				}, {
     				stopAction.notNil.if({
      					stopAction.value; })
    				}) 
   		});
    // ok, now do other stuff

		show.if({
			win.front;
		});	
	}
	
	startRecording {
		//var tmpfile;
		
		recording.not.if ({
			"record".postln;
			//syn = Synth("recorder", args:[\bufnum, recBuf.bufnum]);
			tmpfile = ("/tmp/" ++ api.nick ++ 
							Date.getDate.bootSeconds.floor ++ ".aiff")
						.standardizePath;
			recBuf.write(tmpfile, "aiff", "int16", 0, 0, true, {|b|
				syn = Synth.tail(s, "LaptoperaRecorder", args:[\bufnum, b.bufnum]);
				recording = true;
			});
		});
	}
	
	stopRecording {
		var filename, path, file, dur, newb;
		
		"stop".postln;
		syn.notNil.if({syn.free;});
		recBuf.close;
		recBuf.free;		

		recording.if({
			recording = false;
				
			filename = section.asString ++ line.value ++ "-" ++ api.nick ++
			 	Date.getDate.bootSeconds.floor ++ ".aiff";
			path = (dir ++ filename).standardizePath;

			Task({
				// make sure the file is actually closed before trying to normalise it
				s.sync;
				0.001.wait;
				SoundFile.normalize(tmpfile, path);
					
				s.sync;
				0.001.wait;
				("path is" + path).postln;
					
				("rm" + tmpfile).postln;
				("rm" + tmpfile).unixCmd;
				
				api.sendMsg('newfile', filename); // send osc message
				api.sendMsg('msg', api.nick, section.asString ++ line.value); // alert the chat
				//api.call('newfile', filename);
				"alert your colleagues".postln;
					
				Task({
					0.1.wait;
					api.sendMsg('newfile', filename); // send osc message again
				}).play;

					
				//syn = Synth.basicNew(\LaptoperRecordingPlayer);
					
				s.sync;
				0.001.wait;
				("path is" + path).postln;

				file = SoundFile.new;
				file.openRead(path);
				dur = file.duration;
				file.close;
					
				newb = Buffer.read(s, path, action: {|buf|
					"playing".postln;
					Synth(\LaptoperRecordingPlayer, args:[\out, 0, \bufnum, buf.bufnum, 
						\amp, 0.5]);
				});
									
				(dur + 0.1).wait;
				newb.close;
				newb.free;
				newb = nil;
		

			}).play;
		});
					
		recBuf = Buffer.alloc(s, 65536, 1);
	}
	
	line_ { |num, caller| line.value_(num, caller ? this);}
	line { ^line.value }
	
	increment {
		"increment".postln;
		recording.not.if({ // don't change while recording
			line.value_(line.value + 1, this);
			("line is" + line.value).postln;
			//AppClock.sched(0, { line_gui.value = line; nil; });
		}, {
			"we're recording".postln;
		});
	}
	
	mostRecent {
		var menuindex, men, index;
		
		latest.notNil.if({
			
			menuindex = latest.asString.at(0).toLower.asSymbol;
			men = menus.at(menuindex);
			
			men.items.do({|assoc, i|
				(assoc.key.asString.compare(latest.asString) == 0).if({
					// found it
					index = i;
					//break;
				})
			});
			
			index.notNil.if({
				AppClock.sched(0, {men.valueAction = index; nil});
			});
		})
	}
}
