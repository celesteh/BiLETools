BtSlider :BtNumGui {

	// This object is based on a reimplementation of EZSlider by Sam Pluta
	// https://github.com/spluta/LiveModularInstrument/blob/e0ee5eeee36dcbd9613f8c9539702bea7ab45b96/GUI/QtEZSlider.sc

	//var orientation;


	*new { arg label, controlSpec, action, initVal,
		initAction=false, orientation=\horz, viewUnits=false;

		^super.new.init(label, controlSpec, action, initVal,
			initAction, orientation, viewUnits);
	}

	init { arg argLabel, argControlSpec, argAction, initVal, initAction, orientation, viewUnits;
		var numberStep, unitText;

		//orientation = argOrientation;

		//viewArray = List.newClear;
		/*
		controlSpec = argControlSpec.asSpec;
		controlSpec.addDependant(this);

		argLabel = argLabel ? "";

		if(argLabel!=nil,{
			labelView = StaticText();
			labelView.string = argLabel;
			labelView.maxHeight_(15).maxWidth_(60).font_(Font("Arial", 14));
			labelView.bounds = Rect(0,0, 15, 30);
			BileTools.hintSize(labelView);
			//viewArray.add([labelView, stretch:4]);
			this.name = argLabel;
		},{
			labelView=nil
		});

		sliderView = Slider();
		sliderView.value = controlSpec.unmap(controlSpec.default);
		//sliderView.maxHeight_(150);
		//viewArray.add(sliderView);

		numberView = NumberBox();
		//numberView.maxWidth_(60);
		//numberView.maxHeight_(15);
		numberView.string = controlSpec.default.asString;
		numberView.font_(Font("Arial", 13));
		BileTools.hintSize(numberView);
		//if(viewNumberBox, {viewArray.add(numberView)});

		zAction = {}; //the default zAction is to do nothing


		// set view parameters and actions

		//(controlSpec.units.notNil && viewUnits).if({
		unitText = "";
		viewUnits.if({
			"view units".postln;
			unitText = controlSpec.units ? unitText;
			unitText.postln;
			controlSpec.units.postln;
		});
			units = StaticText();
			units.string = unitText;
			units.maxHeight_(15).maxWidth_(60).font_(Font("Arial", 14));
			BileTools.hintSize(units);
		//});

		this.pr_orient(orientation);

		initVal = initVal ? controlSpec.default;
		action = argAction;

		sliderView.action = {
			this.valueAction_(controlSpec.map(sliderView.value));
		};

		sliderView.receiveDragHandler = { arg slider;
			slider.valueAction = controlSpec.unmap(GUI.view.currentDrag);
		};

		sliderView.beginDragAction = { arg slider;
			controlSpec.map(slider.value)
		};

		numberView.action = { this.valueAction_(numberView.value) };

		numberStep = controlSpec.step;
		if (numberStep == 0) {
			numberStep = controlSpec.guessNumberStep
		}{
			numberView.alt_scale = 1.0;
			sliderView.alt_scale = 1.0;
		};

		numberView.step = numberStep;
		numberView.scroll_step = numberStep;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};

		if (labelView.notNil) {
			labelView.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
				if(clickCount == 2, {this.editSpec});
			}
		};

		//view = View().layout = layout;
		//view.layout.gap_(0.0 @ 0.0)
		this.layout = slLayout;

		*/


		super.init(argLabel, argControlSpec, argAction, initVal, initAction, viewUnits);
		initVal = initVal ? controlSpec.default;
		sliderView = Slider();
		sliderView.value = controlSpec.unmap(controlSpec.default);
		this.pr_orient(orientation);
		this.pr_sliderInit(initVal);


		this.layout = slLayout;

	}

	/*
	labelWidth {
		^labelView.bounds.width;
	}

	labelWidth_ {|width|
		var bounds;

		// get the bounds, change only the width, re-set the bounds
		bounds = labelView.bounds;
		width = width ? bounds.width; // Unlikely to be nil, but whatevs
		//bounds.width = width;
		//labelView.bounds = bounds;
		labelView.fixedWidth = width;
	}

	unitWidth {
		^units.bounds.width;
	}

	unitWidth_ {|width|
		var bounds;

		// get the bounds, change only the width, re-set the bounds
		bounds = units.bounds;
		width = width ? bounds.width; // Unlikely to be nil, but whatevs
		//bounds.width = width;
		//labelView.bounds = bounds;
		units.fixedWidth = width;
	}

	numberWidth {
		^numberView.bounds.width;
	}


	numberWidth_ {|width|
		var bounds;

		// get the bounds, change only the width, re-set the bounds
		bounds = numberView.bounds;
		width = width ? bounds.width; // Unlikely to be nil, but whatevs
		//bounds.width = width;
		//labelView.bounds = bounds;
		//numberView.maxWidth = width;
		numberView.fixedWidth = width;
		//(sliderView.orientation == \vertical).if({
		//	super.maxWidth = width + sliderView.bounds.width;
		//});
	}
*/

	pr_orient{|orientation|

		var vert, horz, long = 130, short, nboxWidth, maxWidth;

		short = numberView.sizeHint.height;

		vert = {


			sliderView.orientation= orientation = \vertical;
			//sliderView.with = short;
			sliderView.fixedWidth = short;
			//sliderView.height = long;

			numberView.font.postln;
			controlSpec.maxval.asString.postln;


			/*
			maxWidth.postln;

			//numberView.fixedWidth = maxWidth.ceil.max(nboxWidth);

			super.maxWidth = short + maxWidth.ceil;

			labelView.resize = 1;
			sliderView.resize = 4;
			numberView.resize = 7;
			units.resize = 7;
			*/

			slLayout = VLayout(
				[labelView, align:\center, stretch:1],
				[sliderView, stretch:20],
				[numberView, align:\center, stretch:1],
				[units, align:\center, stretch:1]
				//[labelView, s:1],
				//[sliderView, s:4],
				//[numberView, s:7],
				//[units, s:7]
			);
			this.resize_(4);
		};

		horz = {

			sliderView.orientation = orientation = \horizontal;
			//sliderView.height = short;
			sliderView.fixedHeight = short;
			//sliderView.width = long;
			//sliderView.bounds = Rect(0,0, long, short);

			//numberView.maxWidth = numberView.sizeHint.width;
			//numberView.minWidth = nboxWidth;
			//numberView.bounds = numberView.bounds.size_(Size(numberView.sizeHint.width/2, numberView.sizeHint.height));
			//super.maxHeight = numberView.sizeHint.height * 1.5;

			/*
			labelView.resize = 1;
			sliderView.resize = 2;
			numberView.resize_(3);
			//numberView.asView.resize = 3;
			units.resize = 3;
*/
			slLayout = HLayout(labelView,sliderView, numberView, units
				//[labelView, s:1],
				//[sliderView, s:2],
				//[numberView, s:3],
				//[units, s:3]
			);

			this.resize_(2);
		};


		switch(orientation,
			\vert, vert,
			\vertical, vert,
			\horz, horz,
			\horizontal, horz);

	}

	//asView {/*^layout*/ ^this}
	//view {}
	/*
	maxHeight_ {arg val;
		val.notNil.if({
			sliderView.maxHeight_(val * 1.5.reciprocal);
			super.maxHeight = val;
		});
	}
	*/

	//maxWidth_ {arg val;
	//	viewArray.do{arg item;
	//		item.maxWidth_(val);
	//	}
	//	sliderView.maxWidth = val;
	//}
	/*
	onClose{controlSpec.removeDependant(this)}

	value_ { arg val;
		value = controlSpec.constrain(val);
		{
			numberView.value = value.round(round);
			sliderView.value = controlSpec.unmap(value);
		}.defer;
	}

	map {arg val;
		^controlSpec.unmap(val);
	}

	valueAction_ { arg val;
		this.value_(val);
		this.doAction;
	}

	doAction { action.value(this) }

	set { arg label, spec, argAction, initVal, initAction = false;
		labelView.notNil.if { labelView.string = label.asString };
		spec.notNil.if { controlSpec = spec.asSpec };
		argAction.notNil.if { action = argAction };

		initVal = initVal ? value ? controlSpec.default;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};
	}

	font_{ arg font;

		labelView.notNil.if{labelView.font=font};
		numberView.font=font;
		units.font = font;
	}

	pr_set_if_visible{|gui, background|

		background.notNil.if({
			gui.notNil.if({
				("^[[:space:]]*$".matchRegexp(gui.string)
						|| (gui.string != "")).if({
					gui.background = background;
				}, {
					gui.background = background.alpha_(0);
				})
			})
		})
	}
	*/

	greatestWidth {

		(sliderView.orientation == \vertical).if ({
			^super.greatestWidth;
		}, {
			^inf
		})
	}

	greatestHeight {

		(sliderView.orientation == \horizontal).if ({
			^super.greatestHeight;
		}, {
			^inf
		})
	}


	setColors{arg stringBackground,stringColor,sliderBackground,numBackground,
		numStringColor,numNormalColor,numTypingColor,knobColor,background;

		stringBackground.notNil.if{
			this.pr_set_if_visible(labelView, stringBackground);
			this.pr_set_if_visible(units, stringBackground);
		};
		stringColor.notNil.if{
			labelView.notNil.if{labelView.stringColor_(stringColor)};
			units.notNil.if{units.stringColor_(stringColor)};};
		numBackground.notNil.if{
			numberView.background_(numBackground);};
		numNormalColor.notNil.if{
			numberView.normalColor_(numNormalColor);};
		numTypingColor.notNil.if{
			numberView.typingColor_(numTypingColor);};
		numStringColor.notNil.if{
			numberView.stringColor_(numStringColor);};
		sliderBackground.notNil.if{
			sliderView.background_(sliderBackground);};
		//knobColor.notNil.if{
		//	sliderView.knobColor_(knobColor);}; */
		background.notNil.if{
			this.background=background;};
		//numberView.refresh;

	}

	//background_{|bgcolor|
	//	this.background = background;
	//}


}

BtKnob : BtNumGui {

		*new { arg label, controlSpec, action, initVal,
		initAction=false, mode=\vert, viewUnits=false;

		^super.new.init(label, controlSpec, action, initVal,
			initAction, mode, viewUnits);
	}

	init { arg argLabel, argControlSpec, argAction, initVal, initAction, mode, viewUnits;
		var numberStep, unitText;

		super.init(argLabel, argControlSpec, argAction, initVal, initAction, viewUnits);
		initVal = initVal ? controlSpec.default;
		sliderView = Knob();
		sliderView.mode = mode;
		sliderView.value = controlSpec.unmap(controlSpec.default);
		//this.pr_orient(orientation);
		//this.pr_sliderInit(initVal);

		labelView.resize = 0;
		numberView.resize = 0;
		units.resize = 0;

		slLayout = VLayout(
			[labelView, stretch:2, align:\center],
			[sliderView, stretch:5],
			[numberView, stretch:2, align:\center],
			[units, stretch:2, align:\center]
		);

		this.pr_sliderInit(initVal);

		this.layout = slLayout;

	}

	greatestWidth {

		//^super.greatestWidth.max(sliderView.bounds.width);
		^inf;
	}

	greatestHeight {

		//^super.greatestHeight.max(sliderView.bounds.height);
		^inf;
	}



	setColors{arg stringBackground,stringColor,sliderBackground,numBackground,
		numStringColor,numNormalColor,numTypingColor,knobColor,background;

		stringBackground.notNil.if{
			this.pr_set_if_visible(labelView, stringBackground);
			this.pr_set_if_visible(units, stringBackground);
		};
		stringColor.notNil.if{
			labelView.notNil.if{labelView.stringColor_(stringColor)};
			units.notNil.if{units.stringColor_(stringColor)};};
		numBackground.notNil.if{
			numberView.background_(numBackground);};
		numNormalColor.notNil.if{
			numberView.normalColor_(numNormalColor);};
		numTypingColor.notNil.if{
			numberView.typingColor_(numTypingColor);};
		numStringColor.notNil.if{
			numberView.stringColor_(numStringColor);};
		sliderBackground.notNil.if{
			sliderView.background_(sliderBackground);};
		knobColor.notNil.if{
			knobColor.isKinfOf(SequenceableCollection).not.if({
				knobColor = [knobColor];
			});
			sliderView.color_(knobColor);};
		background.notNil.if{
			this.background=background;};
		//numberView.refresh;

	}


}

BtNumGui : BtGui{

	var <>sliderView, <>numberView, /*<>layout,*/ <>units;
	var <>round = 0.001;
	//var <view;
	var slider_alt_scale;

	init { arg label, argControlSpec, argAction, initVal, initAction, viewUnits;
		var numberStep, unitText;

		controlSpec = argControlSpec.asSpec;
		controlSpec.addDependant(this);

		labelView = this.pr_text(label);

		if(label!=nil,{
			this.name = label;
		});

		numberView = this.pr_numberBox();

		zAction = {}; //the default zAction is to do nothing


		// set view parameters and actions

		//(controlSpec.units.notNil && viewUnits).if({
		viewUnits.if({
			unitText = controlSpec.units;
		});

		units = this.pr_text(unitText);


		initVal = initVal ? controlSpec.default;
		action = argAction;


		numberView.action = { this.valueAction_(numberView.value) };

		numberStep = controlSpec.step;
		if (numberStep == 0) {
			numberStep = controlSpec.guessNumberStep
		}{
			numberView.alt_scale = 1.0;
			slider_alt_scale = 1.0;
		};

		numberView.step = numberStep;
		numberView.scroll_step = numberStep;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};

		if (labelView.notNil) {
			labelView.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
				if(clickCount == 2, {this.editSpec});
			}
		};

		//view = View().layout = layout;
		//view.layout.gap_(0.0 @ 0.0)
		//this.layout = slLayout;



	}

	pr_sliderInit {|value|

		sliderView.action = {
			this.valueAction_(controlSpec.map(sliderView.value));
		};

		sliderView.receiveDragHandler = { arg slider;
			slider.valueAction = controlSpec.unmap(GUI.view.currentDrag);
		};

		sliderView.beginDragAction = { arg slider;
			controlSpec.map(slider.value)
		};

		slider_alt_scale.notNil.if({
			sliderView.alt_scale = slider_alt_scale;
		});

		value.notNil.if({
			sliderView.value = controlSpec.unmap(value);
		});

	}

	asView {/*^layout*/ ^this}
	view {}


	pr_numberBox{|spec, font|
		var numberBox, width;

		spec = spec ? controlSpec;
		font = font ? Font("Arial", 13);

		numberBox = NumberBox();
		numberBox.string = spec.default.round(0.01).asString;
		numberBox.font_(font);

		width = "20000".bounds(font).width .max(
			spec.maxval.round(0.01).asString.bounds(font).width. max (
				spec.minval.round(0.01).asString.bounds(font).width
		)) * 1.1;

		numberBox.fixedWidth = width;
		^numberBox;
	}

	greatestWidth {

		^numberView.bounds.width.max(
			labelView.bounds.width.max(
				units.bounds.width));
	}

	greatestHeight {
		^numberView.bounds.height;
	}


	resizeToGreatest {
		var width, height;
		width = this.greatestWidth;
		height = this.greatestHeight;

		(width.notNil && (width != inf)).if ({

			this.maxWidth = width + (numberView.bounds.height * 1.1);
		});

		(height.notNil && (height != inf)).if ({

			this.maxHeight = height + (numberView.bounds.height * 1.5);
		});

	}

	unitWidth {
		units.notNil.if({
			^units.bounds.width;
		});

		^0
	}

	unitWidth_ {|width|
		var bounds;

		units.notNil.if({
		//pr_isBlank(units).not.if({
			//"unitWidth_".postln;
			//this.bounds.width.postln;
			//width.postln;
			// get the bounds, change only the width, re-set the bounds
			bounds = units.bounds;
			width = width ? bounds.width; // Unlikely to be nil, but whatevs
			//bounds.width = width;
			//labelView.bounds = bounds;
			//bounds.width.postln;
			//width.postln;
			units.fixedWidth = width;
		//units.resizeTo(width, bounds.height);
		//})
		});
		this.resizeToGreatest;
	}

	numberWidth {
		^numberView.bounds.width;
	}


	numberWidth_ {|width|
		var bounds;

		// get the bounds, change only the width, re-set the bounds
		bounds = numberView.bounds;
		width = width ? bounds.width; // Unlikely to be nil, but whatevs
		//bounds.width = width;
		//labelView.bounds = bounds;
		//numberView.maxWidth = width;
		numberView.fixedWidth = width;
		//(sliderView.orientation == \vertical).if({
		//	super.maxWidth = width + sliderView.bounds.width;
		//});
		//numberView.resizeTo(width, bounds.height);
		//"numberWidth_( % ) % % hint %".format(width, numberView.bounds.width, this.bounds.width,
		//	this.sizeHint.width
		//).postln;
		this.resizeToGreatest;
		//this.refresh;
	}




	width{|w|

		//"width!!--------------------------------------------------".postln;
		^super.width_(w)
	}

	onClose{controlSpec.removeDependant(this)}

	value_ { arg val;
		value = controlSpec.constrain(val);
		{
			numberView.value = value.round(round);
			sliderView.notNil.if({
				sliderView.value = controlSpec.unmap(value);
			});
		}.defer;
	}

	map {arg val;
		^controlSpec.unmap(val);
	}

	set { arg label, spec, argAction, initVal, initAction = false;
		labelView.notNil.if { labelView.string = label.asString };
		spec.notNil.if { controlSpec = spec.asSpec };
		argAction.notNil.if { action = argAction };

		initVal = initVal ? value ? controlSpec.default;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};
	}

	font_{ arg font;

		super.font_(font);

		labelView.notNil.if{labelView.font=font};
		numberView.font=font;
		units.font = font;
	}




}

BTNumber : BtNumGui {
	//var <numberView,<>units, <>round = 0.001;

	*new { arg label, controlSpec, action, initVal,
		initAction=false, viewUnits=false;

		^super.new.init(label, controlSpec, action, initVal,
			initAction, viewUnits);
	}



	init { arg argLabel, argControlSpec, argAction, initVal, initAction, viewUnits;
		var numberStep, unitText;

		var layoutArr;

		//"init".debug(this);

		super.init(argLabel, argControlSpec, argAction, initVal, initAction, viewUnits);
		initVal = initVal ? controlSpec.default;
		sliderView = nil; //View();//Knob();

		labelView.resize = 0;
		numberView.resize = 2;
		numberView.maxWidth = inf;
		units.resize = 0;
		layoutArr = [
			[labelView, align:\center],
			View().fixedWidth_(1),
			[numberView, s:20, align:\center]
		];

		viewUnits.if({
			layoutArr = layoutArr.add([units, align:\center]);
		});

		slLayout = HLayout(*layoutArr);

		//this.pr_sliderInit(initVal);

		this.layout = slLayout;



	}




	pr_sliderInit {|value|
	    ^nil;//View();
	}

	numberWidth_{}

	greatestWidth {

		//^super.greatestWidth.max(numberView.bounds.width);
		^inf;
	}

	greatestHeight {

		^super.greatestHeight.max(numberView.bounds.height);
		//^inf;
	}

	doAction { this.action.value(this) }





}

BtText : BtGui {

	var <textField;

	*new { arg label, action, initVal, initAction=false, orientation=\horz;

		^super.new.init(label, action, initVal, initAction, orientation);
	}

	init { arg label, argAction, initVal, initAction, orientation;

		labelView = this.pr_text(label);
		labelView.resizeToHint;

		if(label!=nil,{
			this.name = label;
		});

		textField = TextField();
		textField.font = Font("Arial", 14);
		textField.string = initVal;


		zAction = {};
		action = argAction;


		textField.action = {
			this.valueActionIfChanged_(textField.value);
		};
		//.action = { this.valueAction_(textField.value) };

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};

		slLayout = HLayout(
			[labelView, stretch:1],
			[textField, stretch:10]
		);

		this.layout = slLayout;

	}

	value_{|val|
		value = val;
		textField.value = val;
	}



	greatestWidth {

		^inf
	}

	greatestHeight {
		^labelView.bounds.height;
	}


	resizeToGreatest {
		var width, height;

		height = this.greatestHeight;

		(height.notNil && (height != inf)).if ({

			this.maxHeight = height + (labelView.bounds.height * 1.5);
		});

	}

	numberWidth_{}
	numberWidth { ^0 }

	font_ {|font|
		super.font_(font);
		textField.font = font

	}


}

BtListView : BtLists {

	initViews{ arg label,argOrientation;
		var labelBounds, listBounds, orientation;

		orientation=argOrientation ? \vert;

		labelView.maxHeight = this.labelMaxHeight;

		widget = ListView.new;

		slLayout  = VLayout(
			[labelView, stretch:1],
			[widget, stretch:4]
		);

		this.layout = slLayout;


	}
	menu {^ widget}

	setColors{arg stringBackground, stringColor, menuBackground,  menuStringColor,background ;

			stringBackground.notNil.if{
				labelView.notNil.if{labelView.background_(stringBackground)};};
			stringColor.notNil.if{
				labelView.notNil.if{labelView.stringColor_(stringColor)};};
			menuBackground.notNil.if{
				this.menu.background_(menuBackground);};
			menuStringColor.notNil.if{
				this.menu.stringColor_(menuStringColor);};
			background.notNil.if{
				this.background=background;};
	}


}

BtPopUpMenu : BtLists {

	initViews{ |argOrientation|
		var labelBounds, listBounds, orientation;

		orientation=argOrientation ? \horz;

		widget = PopUpMenu.new();

		slLayout = HLayout(
			[labelView, stretch:1],
			[widget, stretch:3]
		);

		this.layout = slLayout;
	}

	menu {^ widget}

	setColors{arg stringBackground, stringColor, menuBackground,  menuStringColor,background ;

			stringBackground.notNil.if{
				labelView.notNil.if{labelView.background_(stringBackground)};};
			stringColor.notNil.if{
				labelView.notNil.if{labelView.stringColor_(stringColor)};};
			menuBackground.notNil.if{
				this.menu.background_(menuBackground);};
			menuStringColor.notNil.if{
				this.menu.stringColor_(menuStringColor);};
			background.notNil.if{
				this.background=background;};
	}

		greatestWidth {

		^inf
	}

	greatestHeight {
		^labelView.bounds.height;
	}


	resizeToGreatest {
		var width, height;

		height = this.greatestHeight;

		(height.notNil && (height != inf)).if ({

			this.maxHeight = height + (labelView.bounds.height * 1.5);
		});

	}

}

BtLists : BtGui {

	// VListef from the EZLists

	var <items, <>globalAction;

	*new { |label,items, globalAction, initVal, initAction = false, orientation =\horz|

		^super.new.init(label, items, globalAction, initVal,
			initAction, orientation);
	}


	init {arg label, argItems, argGlobalAction, initVal, initAction, orientation;

		super.init(label);

		this.initViews( orientation );
		this.items = argItems ? [];

		globalAction = argGlobalAction;

		widget.action = { |obj|
			items.at(obj.value).value.value(this);
			globalAction.value(this);
		};

		this.value_(initVal);

		items.notNil.if {
			if(initAction) {
				items.at(initVal).value.value(this); // You must do this like this
				globalAction.value(this);	// since listView's array is not accessible yet
			};
			this.value_(initVal);
		};

	}

	initViews { }  // override this for your subclass views

	value { ^widget.value }
	value_ {|val| widget.value = val }

	valueAction_ { |val| widget.value_(val); this.doAction }

	doAction { widget.doAction }

	items_{ |assocArray|
		assocArray = assocArray.collect { |it| if (it.isKindOf(Association), { it }, { it -> nil }) };
		items = assocArray;
		widget.items = assocArray.collect { |item| item.key };
	}

	item { ^items.at(this.value).key }
	itemFunc { ^items.at(this.value).value }

	addItem { |name, action|
		this.insertItem(nil, name, action);
	}

	insertItem { |index, name, action|
		var temp;
		index = index ? items.size;
		this.items = items.insert(index, name.asSymbol -> action);
	}

	removeItemAt { |index|
		var temp;
		items.removeAt(index);
		this.items_(items)

	}

	replaceItemAt { |index, name, action|
		var temp;
		name = name ? items.at(index).key;
		action = action ? items.at(index).value;
		this.removeItemAt(index);
		this.insertItem(index, name, action);

	}

}

BtGui : View {

	var <>labelView;
	var <>action, <value, <>zAction, viewArray;
	var <widget;
	//var <view;
	var slLayout;
	var <>controlSpec;


	init {arg label, argAction;
		labelView = this.pr_text(label);

		if(label!=nil,{
			this.name = label;
		});
		zAction = {};
		action = argAction;

	}

	labelWidth {
		var width = 0;
		labelView.notNil.if({
			width = labelView.bounds.width;
		});
		^width
	}

	labelWidth_ {|width|
		var bounds;

		labelView.notNil.if({

			// get the bounds, change only the width, re-set the bounds
			bounds = labelView.bounds;
			width = width ? bounds.width; // Unlikely to be nil, but whatevs
			//bounds.width = width;
			//labelView.bounds = bounds;
			//labelView.fixedWidth = width;
			labelView.resizeTo(width, bounds.height);
			labelView.fixedWidth = width;
			//labelView.background = Color.white;
			//"labelWidth_( % ) % %".format(width, labelView.bounds.width, this.bounds.width).postln;
			//labelView.refresh; this.refresh;

		});
		this.resizeToGreatest;
	}

	labelMaxHeight {
		var bounds = Rect(0,0,0,0);

		labelView.notNil.if({
			bounds = labelView.string.bounds(labelView.font)
		});
		^(bounds.height * 1.5);
	}

	valueAction_ { arg val;
		this.value_(val);
		this.doAction;
	}

	map {arg val;
		controlSpec.notNil.if({
			^controlSpec.unmap(val);
		});
		^val;
	}


	doAction { action.value(this) }


	pr_set_if_visible{|gui, background|

		background.notNil.if({
			//gui.notNil.if({
			//	("^[[:space:]]*$".matchRegexp(gui.string)
			//			|| (gui.string != "")).if({
			//		gui.background = background;
			//	}, {
			//		gui.background = background.alpha_(0);
			//	})
			//})
			this.pr_isBlank(gui).if({
				// yes
				gui.background = background.alpha_(0);
			} , {
				// not blank
				gui.background = background;
			})
		})
	}

	pr_text {|text = "", font|
		var staticText;

		font = font ? Font("Arial", 14);

		staticText = StaticText();
		staticText.font = font;
		staticText.string = text.asString;
		//staticText.bounds = text.bounds(font);
		^staticText
	}

	font_{|font| labelView.notNil.if{labelView.font=font}; }

	greatestWidth {

		^labelView.bounds.width.max;
	}

	greatestHeight {
		^labelView.bounds.height;
	}


	resizeToGreatest {
		var width, height;
		width = this.greatestWidth;
		height = this.greatestHeight;

		(width.notNil && (width != inf)).if ({

			this.maxWidth = width + (labelView.bounds.height * 1.1);
		});

		(height.notNil && (height != inf)).if ({

			this.maxHeight = height + (labelView.bounds.height * 1.5);
		});

	}


	pr_isBlank {|gui|
		var blank = true;
		gui.notNil.if({
			("^[[:space:]]*$".matchRegexp(gui.string)
						|| (gui.string != "")).if({
					blank = false;
				})
			})
		^blank
	}

	setColors{arg stringBackground,stringColor,sliderBackground,numBackground,
		numStringColor,numNormalColor,numTypingColor,knobColor,background;

		stringBackground.notNil.if{
			this.pr_set_if_visible(labelView, stringBackground);
		};
		background.notNil.if{
			this.background=background;};
		//numberView.refresh;

	}
}

BtRanger {

	// This object was written by Sam Pluta
	// https://github.com/spluta/LiveModularInstrument/blob/e0ee5eeee36dcbd9613f8c9539702bea7ab45b96/GUI/QtEZSlider.sc

	var <>controlSpec, <>rangeSlider, <>hiBox, <>loBox, <hi, <lo, <>labelView, <>layout;
	var <>round = 0.001;
	var <>action, value, <>zAction;

	*new { arg labelView, controlSpec, action, initVal,
		initAction=false, orientation=\vert;

		^super.new.init(labelView, controlSpec, action, initVal,
			initAction, orientation);
	}

	init { arg arglabelView, argControlSpec, argAction, initVal, initAction, orientation;
		var numberStep;

		rangeSlider = RangeSlider();
		rangeSlider.orientation=orientation;
		hiBox = NumberBox().font_(Font("Helvetica", 10));
		hiBox.maxWidth_(60).maxHeight_(15);
		loBox = NumberBox().font_(Font("Helvetica", 10));
		loBox.maxWidth_(60).maxHeight_(15);
		labelView = StaticText().font_(Font("Helvetica", 10));
		labelView.string = arglabelView;

		switch(orientation,
			\vert, {rangeSlider.orientation = \vertical; layout = VLayout(labelView, hiBox, rangeSlider, loBox)},
			\vertical, {rangeSlider.orientation = \vertical; layout = VLayout(labelView, hiBox, rangeSlider, loBox)},
			\horz, {rangeSlider.orientation = \horizontal; layout = HLayout(labelView, loBox, rangeSlider.maxHeight_(15), hiBox)},
			\horizontal, {rangeSlider.orientation = \horizontal; layout = HLayout(labelView, loBox, rangeSlider.maxHeight_(15), hiBox)});

		// set view parameters and actions

		controlSpec = argControlSpec.asSpec;
		controlSpec.addDependant(this);

		initVal = initVal ? [controlSpec.minval, controlSpec.maxval];
		action = argAction;

		zAction = {}; //the default zAction is to do nothing

		rangeSlider.receiveDragHandler = { arg rangeSlider;
			rangeSlider.valueAction = controlSpec.unmap(GUI.view.currentDrag);
		};

		rangeSlider.beginDragAction = { arg rangeSlider;
			controlSpec.map(rangeSlider.value)
		};

		loBox.action_({ |box| this.lo_(box.value).doAction; });
		rangeSlider.action_({ |sl|
			this.lo_(controlSpec.map(sl.lo));
			this.hi_(controlSpec.map(sl.hi));
			this.doAction;
		});
		hiBox.action_({ |box| this.hi_(box.value).doAction; });

		numberStep = controlSpec.step;
		if (numberStep == 0) {
			numberStep = controlSpec.guessNumberStep
		}{
			hiBox.alt_scale = 1.0;
			loBox.alt_scale = 1.0;
			rangeSlider.alt_scale = 1.0;
		};

		hiBox.step = numberStep;
		hiBox.scroll_step = numberStep;
		loBox.step = numberStep;
		loBox.scroll_step = numberStep;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};

		if (labelView.notNil) {
			labelView.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
				if(clickCount == 2, {this.editSpec});
			}
		};

	}

	asView {^layout}

	map {arg val;
		^controlSpec.unmap(value);
	}

	value { ^[lo, hi] }
	value_ { |vals|
		this.lo_(vals[0]).hi_(vals[1]) }
	valueAction_ { |vals| this.value_(vals).doAction }

	lo_ { |val|
		lo = controlSpec.constrain(val);
		{
			loBox.value_(lo.round(round));
			rangeSlider.lo_(controlSpec.unmap(lo));
		}.defer
	}

	hi_ { |val|
		hi = controlSpec.constrain(val);
		{
			hiBox.value_(hi.round(round));
			rangeSlider.hi_(controlSpec.unmap(hi));
		}.defer
	}

	onClose{controlSpec.removeDependant(this)}

	doAction { action.value(this) }

}

BtSlider2D {

	// This object was written by Sam Pluta
	// https://github.com/spluta/LiveModularInstrument/blob/e0ee5eeee36dcbd9613f8c9539702bea7ab45b96/GUI/QtEZSlider.sc

	var <>controlSpecX, <>controlSpecY, <>slider, <x, <y, <>layout;
	var <>round = 0.001;
	var <>action, value, <>zAction;

	*new { arg controlSpecX, controlSpecY, action, initVal,
		initAction=false;

		^super.new.init(controlSpecX, controlSpecY, action, initVal,
			initAction);
	}

	init { arg argcontrolSpecX, argcontrolSpecY, argAction, initVal, initAction;
		var numberStep;

		slider = Slider2D();

		x = 0; y = 0;

		// set view parameters and actions

		controlSpecX = argcontrolSpecX.asSpec;
		controlSpecX.addDependant(this);

		controlSpecY = argcontrolSpecY.asSpec;
		controlSpecY.addDependant(this);

		initVal = initVal ? [controlSpecX.default,controlSpecY.default];
		action = argAction;

		zAction = {}; //the default zAction is to do nothing

		slider.action_({ |sl|
			action.value([controlSpecX.map(sl.x), controlSpecY.map(sl.y)]);

		});

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};

		layout = slider;
	}

	asView {^layout}

	map {arg val;
		^[controlSpecX.unmap(val[0]),controlSpecY.unmap(val[1])];
	}

	value { ^[x, y] }
	value_ { |vals| slider.setXY(vals[0], vals[1]) }
	valueAction_ { |vals|
		this.value_(vals);
		action.value([controlSpecX.map(vals[0]), controlSpecY.map(vals[1])]);
	}

	activex_ { |val|
		{slider.x_(val)}.defer;
		x = val;
		action.value([controlSpecX.map(x), controlSpecY.map(y)]);
	}

	activey_ { |val|
		{slider.y_(val)}.defer;
		y = val;
		action.value([controlSpecX.map(x), controlSpecY.map(y)]);
	}


}

// BtLists : EZGui{  // an abstract class
//
//	// This object was written by Sam Pluta
//	// https://github.com/spluta/LiveModularInstrument/blob/e0ee5eeee36dcbd9613f8c9539702bea7ab45b96/GUI/QtEZSlider.sc
//
// 	var <items, <>globalAction;
//
// 	*new { arg parentView, bounds, labelView,items, globalAction, initVal=0,
// 		initAction=false, labelWidth,labelHeight=20, layout, gap, margin;
//
// 		^super.new.init(parentView, bounds, label, items, globalAction, initVal,
// 		initAction, labelWidth,labelHeight,layout, gap, margin);
// 	}
//
// 	init { arg parentView, bounds, label, argItems, argGlobalAction, initVal,
// 		initAction, labelWidth, labelHeight, layout,  argGap, argMargin;
//
// 		// try to use the parent decorator gap
// 		this.prMakeMarginGap(parentView, argMargin, argGap);
//
// 		// init the views (handled by subclasses)
// 		this.initViews(  parentView, bounds, label, labelWidth,labelHeight,layout );
//
// 		this.items=argItems ? [];
//
// 		globalAction=argGlobalAction;
//
// 		widget.action={arg obj;
// 			items.at(obj.value).value.value(this);
// 			globalAction.value(this);
// 		};
//
// 		this.value_(initVal);
//
// 		items.notNil.if{
// 			if(initAction){
// 				items.at(initVal).value.value(this); // You must do this like this
// 				globalAction.value(this);	// since listView's array is not accessible yet
// 			};
// 			this.value_(initVal);
// 		};
//
// 	}
//
// 	initViews{}  // override this for your subclass views
//
// 	value{ ^widget.value}
// 	value_{|val| widget.value=val}
//
// 	valueAction_{|val| widget.value_(val); this.doAction}
//
// 	doAction {widget.doAction;}
//
// 	items_{ arg assocArray;
// 		assocArray = assocArray.collect({ |it| if (it.isKindOf(Association), { it }, { it -> nil }) });
// 		items=assocArray;
// 		widget.items=assocArray.collect({|item| item.key});
// 	}
//
// 	item {^items.at(this.value).key}
// 	itemFunc {^items.at(this.value).value}
//
// 	addItem{arg name, action;
// 		this.insertItem(nil, name, action);
// 	}
//
// 	insertItem{ arg index, name, action;
// 		var temp;
// 		index = index ? items.size;
// 		this.items=items.insert(index, name.asSymbol -> action);
// 	}
//
// 	removeItemAt{ arg index;
// 		var temp;
// 		items.removeAt(index);
// 		this.items_(items)
//
// 	}
//
// 	replaceItemAt{ arg index, name, action;
// 		var temp;
// 		name = name ? items.at(index).key;
// 		action = action ? items.at(index).value;
// 		this.removeItemAt(index);
// 		this.insertItem(index, name, action);
//
// 	}
//
// }
// BtPopUpMenu : EZLists{
//
//	// This object was written by Sam Pluta
//	// https://github.com/spluta/LiveModularInstrument/blob/e0ee5eeee36dcbd9613f8c9539702bea7ab45b96/GUI/QtEZSlider.sc
//
// 	initViews{ arg parentView, bounds, label, labelWidth,labelHeight,arglayout;
// 		var labelBounds, listBounds;
//
// 		labelWidth = labelWidth ? 80;
// 		layout=arglayout ? \horz;
// 		labelSize=labelWidth@labelHeight;
//
// 		bounds.isNil.if{bounds= 160@20};
//
// 		// if no parent, then pop up window
// 		# view,bounds = this.prMakeView( parentView,bounds);
//
// 		// calcualate bounds
// 		# labelBounds,listBounds = this.prSubViewBounds(innerBounds, label.notNil);
//
// 		// insert the views
//
// 		/*		label.notNil.if{ //only add a label if desired
// 		if ((layout==\vert)(layout==\vertical)||{
// 		labelView = StaticText.new(view, labelBounds).resize_(2);
// 		labelView.align = \left;
// 		}{
// 		labelView = StaticText.new(view, labelBounds);
// 		labelView.align = \right;
// 		};
// 		labelView.string = label;
// 		};*/
//
// 		widget = PopUpMenu.new(view, listBounds).resize_(5);
// 	}
//
// 	menu {^ widget}
//
// 	setColors{arg stringBackground, stringColor, menuBackground,  menuStringColor,background ;
//
// 		stringBackground.notNil.if{
// 		labelView.notNil.if{labelView.background_(stringBackground)};};
// 		stringColor.notNil.if{
// 		labelView.notNil.if{labelView.stringColor_(stringColor)};};
// 		menuBackground.notNil.if{
// 		this.menu.background_(menuBackground);};
// 		menuStringColor.notNil.if{
// 		this.menu.stringColor_(menuStringColor);};
// 		background.notNil.if{
// 		view.background=background;};
// 	}
//
// }
