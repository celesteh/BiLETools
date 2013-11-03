// MYOB, v2, some example Tdefs for Bus control

// CONTROL VIA TDEF
// Used with Event from a Tdef
(
(
Tdef(\resetParams, {
	1.do{
		(	'instrument': 'setBuses',
			'value1': 0,
			'value2': 0,
			'value3': 0,
			'dur': 1,
			'where': \all
		).play;
	}});
);
(
Tdef(\setAllRand,{
	inf.do { |i|
		(	'instrument': 'setBuses',
			'value1': 1.0.rand,
			'value2': 1.0.rand,
			'value3': 1.0.rand,
			'dur': 0.1,
			'where': \all
		).play;
		0.5.wait;
	};
});
);

(
Tdef(\setAmps,{
	inf.do { |i|
		i = i%100 * 0.01;
		('instrument': 'setAmp','ampval': i, 'where': \all).play;
		0.1.rand.wait;
	};
});
);

(
Tdef(\setParam1,{
	inf.do { |i, val1,val2,val3|
		i = i%100 * 0.01;
		(	'instrument': 'setBus',
			'out': 11,
			'value': i,
			'dur': 0.1,
			'where': \all
		).play;
		2.0.rand.wait;
	};
});
);


(
Tdef(\setParam2,{
	inf.do { |i, val1|
		i = i%100 * 0.01;
		(	'instrument': 'setBus',
			'out': 12,
			'value': 0.5.rand+i%0.5,
			'dur': 0.1,
			'where': \all
		).play;
		0.5.rand.wait;
	};
});
);

(
Tdef(\setParam3,{
	inf.do { |i|
		i = i%100 * 0.01;
		(	'instrument': 'setBus',
			'out': 13,
			'value': 0.3.rand+i%0.5,
			'dur': 0.1,
			'where': \all
		).play;
		0.05.wait;
	};
});
);

(
Tdef(\setAllFX,{
	inf.do { |i|
		(	'instrument': 'setBusesFX',
			'value1': 0.5.rand,
			'value2': 0.5.rand,
			'value3': 0.5.rand,
			'dur': 1.0,
			'where': \all
		).play;
		0.5.wait;
	};
});
);

(
Tdef(\sloRiseInSync,{
	inf.do { |i|
		i = i%100 * 0.01;
		(	'instrument': 'setBusesFX',
			'value1': i,
			'value2': i,
			'value3': i,
			'dur': 1.0,
			'where': \all
		).play;
		0.1.wait;
	};
});
);

// some Clock Control Tdefs
// slow down quick
Tdef(\clck_sldwn, {10.do {|i| r.send(\all, '/setclock', (~clock.tempo*60)-(i*3)); 0.5.wait;}});
// speed up
Tdef(\clck_speedup, {10.do {|i| r.send(\all, '/setclock', (~clock.tempo*60)+(i*3)); 0.5.wait;}});
// set spec bpms
Tdef(\clck_set60, {1.do{ r.send(\all, '/setclock', 60); 0.5.wait;}});
Tdef(\clck_set135, { 1.do{r.send(\all, '/setclock', 135); 0.5.wait;}});
Tdef(\clck_set112, { 1.do{r.send(\all, '/setclock', 112); 0.5.wait;}});
Tdef(\clck_set80, { 1.do{r.send(\all, '/setclock', 80); 0.5.wait;}});

Tdef(\clck_6Up, { r.send(\all, '/setclock', (~clock.tempo*60)+6); 0.5.wait;});
Tdef(\clck_6Dwn,{ r.send(\all, '/setclock', (~clock.tempo*60)-6); 0.5.wait;});



// SOME EXAMPLE PATTERNS


// if you need a gui:
// PdefAllGui(10);


(
w = 4;
Pdef(\rhythm1,
	Pbind(
   		\instrument, \sd1,
//      \where,  0, //specify target if you want
        \sustain, Pwhite(0.01, 0.12) * w,
        // choose some rhythm
//      \dur, Prand((Array.fill(w, { arg i; i/ w })).scramble.pyramid(1 + w.rand),inf)
//		\dur, 2/3 * Pseq([0.25, 0.25, 0.5],inf) * 1.0
		\dur, 0.5 * Pseq([2,1,2,1,2,1,1,1,1]/6,inf)
));
);

(
w = 4;
Pdef(\rhythm2,
	Pbind(
    	\instrument, \sd2,
        \sustain, Pseq([0.1, Pwhite(0.1,0.5,1)],inf),
        // \dur, Prand((Array.fill(5, { arg i; i/w })).scramble.pyramid(1 +10.rand),inf),
        \dur, 0.25 * Ppatlace([Pseq([2,3,2,2,3]/3,inf)-0.1,0.1],inf) //12/8ths pulse
));
);

(
Pdef(\rhythm3,
	Pbind(
   		\instrument, \sd3,
    		\i, Pn(Pseries(0.01,0.01,100),inf),
    		\sustain, Pkey(\i)*Pwhite(1,3.0),
//  		\dur, Prand((Array.fill(w, { arg i; i * w })).scramble.pyramid(1 +1.rand),inf)
		\dur, Pkey(\i)
));
);



/*
Here are some basic rumbapattern for dur param, try out
// clave son
	\dur,
		Ppatlace([Pseq([2,2,3,2,3]/3,inf)-0.1,0.1],inf) //12/8ths pulse
//		Ppatlace([Pseq([3,3,4,2,4]/4,inf)-0.1,0.1],inf)  //4/4s pulse
//clave rumba
	\dur,
		Ppatlace([Pseq([2,3,2,2,3]/3,inf)-0.1,0.1],inf) //12/8ths pulse
		// Ppatlace([Pseq([3,4,3,2,4]/4,inf)-0.05,0.05],inf) // 4/4s pulse
// clave bell patterns
	\dur,
		Ppatlace([Pseq([3,3,1,3,2,3,1]/3,inf)-0.1,0.1],inf) //12/8ths pulse
		// Ppatlace([Pseq([3,3,1,3,2,3,1]/4,inf),0],inf) // 4/4s pulse
// straight pulses
	\dur,
		1/3 // for 12/8ths
//		0.25 // for 4/4s
*/


);


