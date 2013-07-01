// version 0.014m-alpha

// The following environment variables are defined:
// ~startRecording -  a function to open a buffer and a new file and start recording

// ~stopRecording - a function to stop recording, close the file 
//				and send an OSC message announcing the new file

// ~increment - increase the line number 1

// ~dir - the directory in which files exist. Changing this variable does not change the
//				behavious of code in this file

// ~buffer - the currently selected buffer

// ~bufferAction - This is a function, definied by you, the user, that will get called when the
//				buffer is loaded
//
// ~nilAction - This is a function, defined by you, the user, that will get called when you
//				set your active buffer to undefined
//
// ~startAction - This is a function, definied by you, the user, that will get called when
//              you hit the button marked start. (Typically, it would start playing something.)
//
// ~stopAction - This is a function, defined by you, the user, that wil get called when
//              you hit the button marked stop. (Typically, it would stop playing something.)
//
// ~mostRecent - a function which sets ~buffer to the most recently recorded one. 
//				use: ~mostRecent.value  
//				YOU CANNOT DEPEND ON IT RETURNING THE RIGHT BUFFER. USE ~buffer AFTER CALLING IT

// something about shelly unselecting - bug // add nil action for buffer

(

	var api;
	
	api = NetAPI.default ? NetAPI.broadcast("Holger");
	b = BileChat(api);
	c = BileClock(api).show;
	
	s.options.memSize = s.options.memSize * 256;
	s.options.outDevice = nil;
//	s.options.inDevice = "H2 Audio";
	//s.options.outDevice = "MOTU 828mk2";
	//s.options.inDevice = "MOTU 828mk2";

	
	s.waitForBoot({
		
		var recBuf, win, colour, section_gui, section, line_gui, line, dir_gui, dir, path,
			filename, button, syn, recording, view, synthSampleDir, loadFile, start, stop,
			oldbuf, tmpfile, incButton, latest, act, play_button; 
			
		var menus, doMenu;
		
		recBuf = Buffer.alloc(s, 65536, 1);
		recording = false;

		
		SynthDef("LaptoperaRecorder",{ arg in = 0, bufnum, dur, running=1.0, trigger=0.0; 
			DiskOut.ar(bufnum, SoundIn.ar(in).tanh);
			//EnvGen.kr(Env.sine(dur), doneAction: 2);
		}).add;
		
		SynthDef(\LaptoperRecordingPlayer, {|out = 0, bufnum = 0, amp =0.2|
			var player;
			player = PlayBuf.ar(1, bufnum, loop:0, doneAction: 2);
			player = [player, player];
			Out.ar(out,  player * amp);
		}).add;


		colour = Color.new(0.9, 0.1, 0.3);
		win = Window("Recorder");
		win.view.background_(colour);
		win.view.decorator = FlowLayout(win.view.bounds);
		win.view.decorator.gap=10@10;
		view = win.view;
		
		view.decorator = FlowLayout(view.bounds);


		
		//win.view.decorator.gap=2@2;
		//dir = "~/tmp/audio/";
		//dir = "/Volumes/composer\'s\ Public\ Folder/Recordings";
		//dir = "/Volumes/Shared/Leptopera/";
		//dir = "/Volumes/Shelly\ Knotts\'s\ Public\ Folder/laptopera/";
		dir = "/Users/hb/Public/Laptopera/";
		
		synthSampleDir = 
		//"~/Dropbox/BiLE\\\ Sound\\\ Files/samples/leptopera/act2".standardizePath;
		("~/Dropbox/BiLE\\\ Sound\\\ Files".standardizePath ++ "/samples/leptopera/act2");

		~dir = dir.standardizePath;
		
		
		
		 dir_gui =  EZText (view,
		 	300@30,
		 	"Directory",
		 	{|ez| 
			 	dir = ez.value.asString; 
			 	~dir = dir.standardizePath;
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
		 	

		 line = 0;
		 
		 line_gui = EZText (view,
		 	300@30,
		 	"Line Number",
		 	{|ez|  
			 	recording.not.if({ // don't change while recording
				 	line = ez.value.asString.asInteger;
			 	}, { // in fact, change back
				 	ez.value = line;
			 	});
		 	},
		 	"0", labelWidth:100);
		 	
		 ~increment = {
			 recording.not.if({ // don't change while recording
				 line = line + 1;
				 ("line is" + line).postln;
				 AppClock.sched(0, { line_gui.value = line; nil; });
			 });
		 };
		 	
		 incButton = Button(view, 30 @ 30).states_([["+"]]). action_(~increment);
		 	
			 	
		
		
		start = {
			recording.not.if ({
				"record".postln;
				//syn = Synth("recorder", args:[\bufnum, recBuf.bufnum]);
				tmpfile = ("/tmp/" ++ api.nick ++ 
							Date.getDate.bootSeconds.floor ++ ".aiff")
						.standardizePath;
				recBuf.write(tmpfile, "aiff", "int16", 0, 0, true,
					{|b|
						syn = Synth.tail(s, "LaptoperaRecorder", args:[\bufnum, b.bufnum]);
						recording = true;
				});
			});
		};
		
		
		act = { |buf|
			"playing".postln;
			Synth(\LaptoperRecordingPlayer, args:[\out, 0, \bufnum, buf.bufnum, \amp, 0.4]);
		};
		
				
		stop = {
			
			var sf, newb, file, dur;
			
			"stop".postln;
			syn.notNil.if({syn.free;});
			recBuf.close;
			recBuf.free;		

			recording.if({
				recording = false;
				
				 filename = section.asString ++ line ++ "-" ++ api.nick ++
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
					api.sendMsg('msg', api.nick, section.asString ++ line); // alert the chat
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
					
					newb = Buffer.read(s, path, action: act);					
					(dur + 0.1).wait;
					newb.close;
					newb.free;
					newb = nil;
					//syn.free;

				}).play;
			});
					
			recBuf = Buffer.alloc(s, 65536, 1);
		};
			 
			 				
		button = Button(view, 300@30).states_([
				["Record", Color.red, Color.green],
				["Stop", Color.black, Color.red]
			])
			.action_({|but|
				(but.value==1).if (start, // pressed record
					stop);			
			});
				// ok, now do other stuff
		
		//view = CompositeView(win, 
		//	Rect((win.bounds.width / 2), 30, (win.bounds.width / 2), win.bounds.height));
		
		//view.decorator = FlowLayout(view.bounds);
		//view.decorator.gap=5@5;
		
		menus = IdentityDictionary(know: true);
		
		doMenu = { |tag|
			
			var sampleMenu, label, dedbuf;
		
			label = tag.asString + "files";
			
			sampleMenu = EZPopUpMenu(view, 300@30, label, labelWidth: 100);
			sampleMenu.addItem("unselected", {
				
				dedbuf = oldbuf;
				oldbuf = ~buffer;
				
				dedbuf.notNil.if({
					dedbuf.close;
					dedbuf.free;
				});
				~buffer = nil;
				
				~nilBuffer.notNil.if({ ~nilBuffer.value });
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
				oldbuf = ~buffer;
				~buffer = b;
				
				~bufferAction.notNil.if({
					~bufferAction.value(b);
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
			
			var file, index, menu;
			
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

				(menu.items.select({|item| item.key == filename }).size == 0).if({
					AppClock.sched(0, {
						"adding".postln;
						menu.addItem(filename.asString, {
							loadFile.(file, index);
						});
						nil;
					});
				},
					{
						("already added " ++ filename).postln;
					});
			});
		});
		
		("synthSampleDir is" + synthSampleDir).postln;
		
		synthSampleDir = (synthSampleDir ++ "/*.*").pathMatch;
		synthSampleDir.do({| sample|
			menus.at('synth').addItem(sample.asString, {
				loadFile.(sample, 'synth');
			});
		});
		
		
		~mostRecent = {
			
			var menuindex, men, index;
			
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
			
			~buffer;
		};

play_button = Button(view, 50@30).states_([
    ["Play", Color.red, Color.green],
    ["Stop Playing", Color.black, Color.red]
   ])
   .action_({|but|
    (but.value==1).if ({
     ~startAction.notNil.if({
      ~startAction.value; })
    }, {
     ~stopAction.notNil.if({
      ~stopAction.vaue ; })
    }) 
   });
    // ok, now do other stuff

		
		win.front;
		
		~startRecording = start;
		~stopRecording = stop;
	});
)

