
// artificial / natural, resonant/dry, abstract/concrete, density/sparseness, grain/sustained
// possibilities: diversity / homogenity, pitched / unpitched




(

var api, win, faders, matrix;

api = NetAPI.oscgroup("~/Documents/code/osc/oscgroups/bin/OscGroupClient",
	"192.168.1.65",
	"Les", "password");
//api = NetAPI.broadcast("Les");  //Change your name
BileChat(api);
BileClock(api).show;

//faders = NanoKontrol2.new;

s.waitForBoot({

	var artificial, resonant, abstract, density, grain, scaled, antisocial, setup;

	// synth(s) go here

	// you may wish to include a gate argument to your synth


	scaled = { |param, fader|

		var fade, result, scale;

		//fade = fader.value / 127;
		//scale = fade * antisocial.value;
		result = (param.value * (1 - antisocial.value));// + scale;
	};



	matrix = NetworkGui.make(api, [\amp], {|gui, amp|

		var param1, param2, param3, param4, param5;

		//faders.fader1.action = {|f| amp.value_(f.value / 127, f) };

		param1 = gui.addRemote('artificial');
		param1.sp(0, 0, 1, 0.01, \lin, gui);
		param1.guitype_(\voter);
		param1.action = {|p| artificial = scaled.(p, /*faders.fader2*/)};
		//faders.fader2.action = {|f| artificial = scaled.(param1, f)};

		param2 = gui.addRemote('resonant');
		param2.sp(0, 0, 1, 0.01, \lin, gui);
		param2.guitype_(\voter);
		param2.action = {|p| resonant = scaled.(p, /*faders.fader3*/)};
		//faders.fader3.action = {|f| resonant = scaled.(param2, f)};

		param3 = gui.addRemote('abstract');
		param3.sp(0, 0, 1, 0.01, \lin, gui);
		param3.guitype_(\voter);
		param3.action = {|p| abstract = scaled.(p, /*faders.fader4*/)};
		//faders.fader4.action = {|f| abstract = scaled.(param3, f)};

		param4 = gui.addRemote('density');
		param4.sp(0, 0, 1, 0.01, \lin, gui);
		param4.guitype_(\voter);
		param4.action = {|p| density = scaled.(p, /*faders.fader5*/)};
		//faders.fader5.action = {|f| density = scaled.(param4, f)};

		param5 = gui.addRemote('grain');
		param5.sp(0, 0, 1, 0.01, \lin, gui);
		param5.guitype_(\voter);
		param5.action = {|p| grain = scaled.(p, /*faders.fader6*/)};
		//faders.fader6.action = {|f| grain = scaled.(param5, f)};

		antisocial = gui.addLocal('antisocial');
		antisocial.sp(0, 0, 1, 0.01, \lin, gui);
		antisocial.action = {|p| api.sendMsg('antisocial', p.value);};
		//faders.fader7.action = {|f| antisocial.value_(f.value / 127, f)};



		// synth / pattern control goes here

		// note that it's possible for values to be <0 or >1, so plan accordingly

	}).show();

})
)

