
EZVoter : EZGui {
	
	
	var <plusView, <minusView, <sliderView, <>controlSpec,
		  popUp=false, buttonWidth, gap, >indexSize;
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


		var labelBounds, sliderBounds, plusBounds, minusBounds;
		var numberStep;


		// Set Margin and Gap
		this.prMakeMarginGap(parentView, argMargin, argGap);

		action = argAction;
		layout=argLayout;
		bounds.isNil.if{bounds = 350@20};
		buttonWidth = argButtonWidth;


		// if no parent, then pop up window
		# view,bounds = this.prMakeView( parentView,bounds);
		//view.decorator = HLayoutView(view, bounds);

		labelSize=labelWidth@labelHeight;
		
		// calculate bounds of all subviews
		# labelBounds, minusBounds, plusBounds, sliderBounds 
			= this.prSubViewBounds(innerBounds, label.notNil);
		
				// instert the views
		label.notNil.if{ //only add a label if desired
			labelView = GUI.staticText.new(view, labelBounds);
			labelView.string = label;
		};


		plusView = GUI.button.new(view, plusBounds).states_([["+"]]);
		minusView = GUI.button.new(view, minusBounds).states_([["-"]]);
		
		sliderView = EZSlider(view, sliderBounds, nil, argControlSpec, 
							argAction, initVal, initAction, 0, 
							argNumberWidth,argUnitWidth, labelHeight, 
							argLayout, argGap, 0@0);

		sliderView.action = { this.valueAction_(sliderView.value) };
		
		plusView.action = {
			this.incrementAction;
		};
		
		minusView.action = {
			this.decrementAction;
		};

		
		
	}
	
	
	step {
		var step;
		step = sliderView.controlSpec.step;
		(step == 0).if ({ step = round});
		^step;
	}
	
	step_ { |step|
		sliderView.controlSpec.step = step;
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
	
			
	value {
		^sliderView.value;
	}
	
	value_ { |val|
		sliderView.value_(val);
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
		sliderView.value_(sliderView.value + this.indexSize);
	}
	
	decrement {
		sliderView.value_(sliderView.value - this.indexSize);
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
		spec.notNil.if { sliderView.controlSpec = spec.asSpec };
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
		sliderView.notNil.if{sliderView.font = font};
	}
	


	prSubViewBounds{arg rect, hasLabel;
		var labelBounds, minusBounds, plusBounds, sliderBounds;
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
		sliderBounds = Rect( //adjust to fit
			voterWidth,
			gap.y,
			rect.width - voterWidth - gap.x,
			labelBounds.height
		);
		
		^[labelBounds, minusBounds, plusBounds, sliderBounds].collect{arg v; v.moveBy(margin.x,margin.y)}
	}
	
	setColors {arg stringBackground,stringColor,sliderBackground,numBackground,
		numStringColor,numNormalColor,numTypingColor,knobColor,background, 
		buttonColor, buttonTextColor;
		
			stringBackground.notNil.if({
				labelView.notNil.if{labelView.background_(stringBackground)};});
			stringColor.notNil.if({
				labelView.notNil.if{labelView.stringColor_(stringColor)};});
				
			(buttonColor.notNil && buttonTextColor.notNil).if({
				plusView.states = [[plusView.states[0][0], buttonTextColor, buttonColor]];
				minusView.states = [[minusView.states[0][0], buttonTextColor, buttonColor]];
			});

			sliderView.setColors(stringBackground,stringColor,sliderBackground,numBackground,
				numStringColor,numNormalColor,numTypingColor,knobColor,background);
		
		 }

}