
EZVoter : EZGui {
	
	
	var <plusView, <minusView, <sliderView, <numberView, <unitView, <>controlSpec,
		  popUp=false, numSize,  buttonWidth, numberWidth, unitWidth, gap, >indexSize;
	var <>round = 0.001;


	*new { arg parent, bounds, label, controlSpec, action, initVal,
			initAction=false, labelWidth=60, buttonWidth=20,
			numberWidth=45, unitWidth=0, labelHeight=20,  
			layout=\horz, gap = 2@2, margin;

		^super.new.init(parent, bounds, label, controlSpec, action,
			initVal, initAction, labelWidth, buttonWidth, numberWidth,
				unitWidth, labelHeight, layout, gap, margin)
	}

	init { arg parentView, bounds, label, argControlSpec, argAction, initVal,
			initAction, labelWidth, argButtonWidth, argNumberWidth,argUnitWidth,
			labelHeight, argLayout, argGap, argMargin;


		var numBounds,labelBounds, sliderBounds, plusBounds, minusBounds, unitBounds;
		var numberStep;


		// Set Margin and Gap
		this.prMakeMarginGap(parentView, argMargin, argGap);

		action = argAction;
		layout=argLayout;
		bounds.isNil.if{bounds = 350@20};
		buttonWidth = argButtonWidth;
		unitWidth = argUnitWidth;
		numberWidth = argNumberWidth;

		// if no parent, then pop up window
		# view,bounds = this.prMakeView( parentView,bounds);
		//view.decorator = HLayoutView(view, bounds);

		labelSize=labelWidth@labelHeight;
		numSize = numberWidth@labelHeight;
		
		// calculate bounds of all subviews
		# labelBounds, minusBounds, plusBounds, sliderBounds, numBounds, unitBounds 
			= this.prSubViewBounds(innerBounds, label.notNil);
		
				// instert the views
		label.notNil.if{ //only add a label if desired
			labelView = GUI.staticText.new(view, labelBounds);
			labelView.string = label;
		};


		plusView = GUI.button.new(view, plusBounds).states_([["+"]]);
		minusView = GUI.button.new(view, minusBounds).states_([["-"]]);
		
		//sliderView = EZSlider(view, sliderBounds, "", argControlSpec, 
		//					argAction, initVal, initAction, 0, 
		//					argNumberWidth,argUnitWidth, labelHeight, 
		//					argLayout, argGap, 0@0);


		//sliderView.action = { this.valueAction_(sliderView.value) };

		numberView = GUI.numberBox.new(view, numBounds);
		sliderView = GUI.slider.new(view, sliderBounds);

		controlSpec = argControlSpec.asSpec;
		controlSpec.addDependant(this);
		this.onClose = { controlSpec.removeDependant(this) };
		(unitWidth>0).if{
			unitView = GUI.staticText.new(view, unitBounds);
			unitView.string = " "++controlSpec.units.asString
		};

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
			// controlSpec wants a step, so zooming in with alt is disabled.
			numberView.alt_scale = 1.0;
			sliderView.alt_scale = 1.0;
		};

		numberView.step = numberStep;
		numberView.scroll_step = numberStep;
		//numberView.scroll=true;
		// set view parameters and actions

		
		plusView.action = {
			this.incrementAction;
		};
		
		minusView.action = {
			this.decrementAction;
		};

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};


		labelView.notNil.if({
			labelView.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
				if(clickCount == 2, {this.editSpec});
			};
		});		
		
	}
	
	
	step {
		var step;
		step = controlSpec.step;
		(step == 0).if ({ step = round});
		^step;
	}
	
	step_ { |step|
		controlSpec.step = step;
	}	
	
	indexSize {
		var result;
		
		indexSize.notNil.if ({
			(indexSize > 0).if ({
				result = indexSize;
			})
		});
		result = result ? this.step;
		
		^result
	}
	
			
	//value {
	//	^sliderView.value;
	//}
	
	value_ { |val|
		//sliderView.value_(val);
		value = controlSpec.constrain(val);
		numberView.value = value.round(round);
		sliderView.value = controlSpec.unmap(value);

	}
	
	valueAction_{|val|
		this.value_(val);
		this.doAction;
	}
	
	doAction { 
		(action.notNil).if({
			action.value(this)
		})
	}
	
	increment {
		//sliderView.value_(sliderView.value + this.indexSize);
		this.value_(value + this.indexSize);
	}
	
	decrement {
		//sliderView.value_(sliderView.value - this.indexSize);
		this.value_(value - this.indexSize);
	}
	
	incrementAction {
		this.increment;
		this.doAction;
	}
	
	decrementAction {
		this.decrement;
		this.doAction;
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

	font_ { arg font;

		labelView.notNil.if{labelView.font=font};
		unitView.notNil.if{unitView.font=font};
		numberView.font=font;
	}

	


	prSubViewBounds{arg rect, hasLabel;
		var labelBounds, minusBounds, plusBounds, sliderBounds, numBounds, unitBounds;
		var gap1, gap2, gap3, voterWidth;
		gap1 = gap.copy;
		gap2 = gap.copy;
		gap3 = gap.copy;


		hasLabel.not.if{ gap1 = 0@0; labelSize.x = 0 ;};
		labelSize.y = rect.height;
		labelBounds = (labelSize.x@labelSize.y).asRect; //to left
		voterWidth = labelSize.x + gap1.x;
		minusBounds = Rect(voterWidth, 0, buttonWidth, labelSize.y);
		voterWidth = voterWidth + buttonWidth + gap2.x;
		plusBounds = Rect(voterWidth, 0, buttonWidth, labelSize.y);
		voterWidth = voterWidth + buttonWidth + gap3.x;
		unitBounds = (unitWidth@labelSize.y).asRect.left_(rect.width-unitWidth); // to right
		//voterWidth = voterWidth + unitWidth + gap.x;
		numBounds = (numSize.x@labelSize.y).asRect
			.left_(rect.width-unitBounds.width-numSize.x-gap3.x);// to right
				sliderBounds  =  Rect( // adjust to fit
					voterWidth,
					0,
					//rect.width - labelBounds.width - unitBounds.width
					//		- numBounds.width - gap1.x - gap2.x - gap3.x,
					rect.width - voterWidth - gap.x - unitBounds.width
							- numBounds.width - gap.x,
					labelBounds.height
					);
		//sliderBounds = Rect( //adjust to fit
		//	voterWidth,
		//	gap.y,
		//	rect.width - voterWidth - gap.x,
		//	labelBounds.height
		//);
		
		^[labelBounds, minusBounds, plusBounds, sliderBounds, numBounds, unitBounds].collect{arg v; v.moveBy(margin.x,margin.y)}
	}
	
	setColors {arg stringBackground,stringColor,sliderBackground,numBackground,
		numStringColor,numNormalColor,numTypingColor,knobColor,background, 
		buttonColor, buttonTextColor;
		
			stringBackground.notNil.if({
				labelView.notNil.if{labelView.background_(stringBackground)};
				unitView.notNil.if{unitView.background_(stringBackground)};});
			stringColor.notNil.if({
				labelView.notNil.if{labelView.stringColor_(stringColor)};
				unitView.notNil.if{unitView.stringColor_(stringColor)};});
				
			(buttonColor.notNil && buttonTextColor.notNil).if({
				plusView.states = [[plusView.states[0][0], buttonTextColor, buttonColor]];
				minusView.states = [[minusView.states[0][0], buttonTextColor, buttonColor]];
			});

			//sliderView.setColors(stringBackground,stringColor,sliderBackground,numBackground,
			//	numStringColor,numNormalColor,numTypingColor,knobColor,background);

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
				sliderView.knobColor_(knobColor);};
			background.notNil.if{
				view.background=background;};
			numberView.refresh;

		
		 }

	editSpec {
		var ezspec;
		[labelView, sliderView, numberView, unitView].do({|view|
			view.notNil.if({ view.enabled_(false).visible_(false)});
		});
		ezspec = EZControlSpecEditor(view, view.bounds.moveTo(0,0), controlSpec: controlSpec, layout: layout);
		ezspec.labelView.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
			if(clickCount == 2, {
				ezspec.remove;
				[labelView, sliderView, numberView, unitView].do({|view|
					view.notNil.if({ view.enabled_(true).visible_(true)});
				});
			});
		};			
	}

}
