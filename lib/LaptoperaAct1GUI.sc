// version 0.02-alpha

LaptoperaAct1GUI {

	var <win, api, view, matrix;

	* max { |net_api, port = 8000, show = true, win|
			^super.new.maxinit(net_api, port, show, win)
	}

	maxinit {|net_api, port = 8000, show, window|

		api = net_api;
		//win = window ? Window("Act1");
		
		matrix = NetworkGui.make(api, [\amp], {|gui, amp|

			var net, param1, param2, param3, param4, param5, antisocial;

			net = NetAddr("127.0.0.1", port);

			amp.action = {|p| net.sendMsg("/amp", p.value)};
			
			param1 = gui.addRemote('artificial');
			param1.sp(0, 0, 1, 0, \lin, gui);
			param1.guitype_(\voter);
			param1.spec.step = 0.01;
			param1.action = {|p| net.sendMsg("/artificial", p.value)};

			param2 = gui.addRemote('resonant');
			param2.sp(0, 0, 1, 0, \lin, gui);
			param2.guitype_(\voter);
			param2.spec.step = 0.01;
			param2.action = {|p| net.sendMsg("/resonant", p.value)};

			param3 = gui.addRemote('abstract');
			param3.sp(0, 0, 1, 0, \lin, gui);
			param3.spec.step = 0.01;
			param3.guitype_(\voter);
			param3.action = {|p| net.sendMsg("/abstract", p.value)};

			param4 = gui.addRemote('density');
			param4.sp(0, 0, 1, 0, \lin, gui);
			param4.spec.step = 0.01;
			param4.guitype_(\voter);
			param4.action = {|p| net.sendMsg("/density", p.value)};

			param5 = gui.addRemote('grain');
			param5.sp(0, 0, 1, 0, \lin, gui);
			param5.spec.step = 0.01;
			param5.guitype_(\voter);
			param5.action = {|p| net.sendMsg("/grain", p.value)};

			antisocial = gui.addLocal('antisocial');
			antisocial.sp(0, 0, 1, 0, \lin, gui);
			antisocial.action = {|p| 
				net.sendMsg("/antisocial", p.value);
				api.sendMsg('antisocial', p.value);};
		}).show(win);
	}
}
/*
	(


var api, chat, matrix, win;


api = NetAPI.broadcast("Nick");

chat = BileChat(api);

matrix = NetworkGui.make(api, [\amp], {|gui, amp, freq|

var dur;


dur = gui.addRemote(\dur);

dur.sp(1, 0.1, 5, 0, \lin, gui); //default, min, max, step size, warp, changer

gui.pattern_(

Pbind(

\freq, freq,

\amp, amp,

\dur, dur

)

);


}).show(chat.win);

)
*/
