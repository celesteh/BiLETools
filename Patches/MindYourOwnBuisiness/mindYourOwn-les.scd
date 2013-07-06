

(

var index;

r = Republic.new.makeDefault;
r.gui(12); // add a display if you like.

	AllGui();

r.join(\les);

r.shareHistory;

index = Routine({var index = 10;
	inf.do({
		index.yield;
		index = index +1;
	})
}).asStream;



~newBus = {
	Bus.new('control', index.next, 1, s);
};

a = ~newBus.value;

~syns =  {r.synthDescs.asArray.sort({ |a, b| a.name < b.name }).collect { |desc| ("(\n" ++ desc.metadata.sourceCode ++ ".share;\n);\n")}};


)






(
SynthDef(\mindyourownles, {|out|
	var env, son;


	Out.ar(out, son * env);
}).share;
)


(
Pdef(\x,
	Pbind(
		\instrument, \mindyourownles,
		\server, r.s,
		\where, Prand(r.nameList, 8), //
		\dur, 1,
		\legato, 0.3,



	).trace
).play;
)

