
SharedResource {
	
	var <value, signed_actions, unsigned_actions, semaphore, spec;
	//var <>symbol, remote_listeners, api, <>desc;
	var <>changeFunc, lastAdvertised;
	
	*new {arg value, threadSafe = false, changeFunc;
		^super.new.init(value, threadSafe, changeFunc);
	}
	
	init { arg item, threadSafe = false, changed;
		
		value = item;
		
		threadSafe.if({
			semaphore = Semaphore(1);
		});
		
		changeFunc = changed;
		lastAdvertised = value;
		
		changeFunc.isNil.if({
			changeFunc = {|old, new| old != new}
		});
		
	}
	
	spec_ {| s, v, changer|
			spec = s.asSpec;
			this.value_(v ? spec.default, changer);
			this.addUniqueMethod(\input_, { arg in, theChanger ... moreArgs;
				this.value_(spec.map(in), theChanger, *moreArgs);
			});
	}
	
	sp	{ | default= 0, lo = 0, hi=0, step = 0, warp = 'lin', changer |
		this.spec_(ControlSpec(lo,hi, warp, step, default), changer:changer);
	}

	pr_doValue { arg newValue, theChanger ... moreArgs;
		
		var changed, result;
		
		changed = false;
		
		result = newValue.value(value);// what that's doing there is letting people
			// write a mathematic function like
			// { |old| old + 1 }
				
		changeFunc.value(lastAdvertised, result).if({ // here we're letting people
			// set their own thresholds and metrics, so they can write a boolean like
			// {|old, new| (old - new).abs > (old * 0.01)}
			
			// we have a change!
			lastAdvertised = result;
			changed = true;
		});
		
		value = result;

		^changed;	
	}
		
		
	value_ { arg newValue, theChanger ... moreArgs;
		
		var changed, result;
		
		changed = false;
		
		semaphore.notNil.if({
			//"ready to wait".postln;
			semaphore.wait;
			
			changed = this.pr_doValue(newValue, theChanger, *moreArgs);
			
			//"notify".postln;
			semaphore.signal;
		}, {
			
			changed = this.pr_doValue(newValue, theChanger, *moreArgs);
		});
			
		
		changed.if({
			// notify others
			dependantsDictionary.at(this).copy.do({ arg dep;
				(dep === theChanger).not.if({
					dep.update(this, theChanger, *moreArgs);
				})
			});
			signed_actions.notNil.if({
				signed_actions.keysDo({ |key|
					(key === theChanger).not.if({
						signed_actions[key].value(this, theChanger, *moreArgs);
					});
				});
			});
			unsigned_actions.notNil.if({
				unsigned_actions.do({|action|
					action.value(this, theChanger, *moreArgs);
				});
			});
		}, { /*"no change".postln;*/});
	}
		
	changeAction_ { |arg1, arg2|
		this.action_(arg1, arg2);
	}
	
	action_ { |arg1, arg2|
		
		arg2.notNil.if({
			// arg1 is the adder and arg2 is the action
			if (signed_actions.isNil, {
				signed_actions = IdentityDictionary.new(4);
			});
		
			signed_actions.put(arg1, arg2);
		} , {
			// arg1 is the action
			if (unsigned_actions.isNil, {
				unsigned_actions = [arg1];
			} , {
				unsigned_actions = unsigned_actions ++ arg1;
			});
		});
	}
	
	removeDependant { arg dependant;
		signed_actions.notNil.if({
			signed_actions.remove(dependant);
		});
		super.removeDependant;
	}
	
	removeAction { arg toBeRemoved;

		removeDependant(toBeRemoved);
		unsigned_actions.notNil.if({
			unsigned_actions.remove(toBeRemoved);
		})
	}
	
	/*
	remoteListener { |user, api|
		
		remote_listeners.isNil.if({
			remote_listeners = Dictionary.new;
		});
		
		"New remote listener %\n".postf(user.nick);
		
		remote_listeners.put(user.nick, user);
		this.action_(symbol, {|val| api.shareData(symbol, val.value)});
	}
	
	removeRemoteListener { |user|
		
		remote_listeners.notNil.if({
			remote_listeners.removeAt(user.nick).notNil.if({
				(remote_listeners.size < 1).if({
					removeAction(symbol);
				})
			})
		})
	}
	*/
	
	mountAPI { |api, key, desc, broadcast=true, symbol|
		
		var remote;
		//symbol = oSCsymbol;
		//desc = description;
		remote = SharedRemoteListeners(key, api, this, desc, broadcast, symbol);
		//api.share(symbol, remote, desc);
		^remote;
	}
	

}

SharedRemoteListeners {
	
	var listeners, <shared, broadcast, api, <>key, <>desc, <n, count, tag;
	
	*new{|key, api, shared, desc, broadcast = true, symbol|
	
		^super.new.init(key, api, shared, broadcast, symbol);
	}
	
	init { |symbol, netapi, sharedResource, description, tellAll = true, osc_tag|
		
		broadcast = tellAll;  if((broadcast.isNil), {broadcast = true});
		tag = osc_tag;
		api = netapi;
		key = symbol;
		shared = sharedResource;
		listeners = Dictionary.new;
		desc = description;
		count = 0;
		n = 1;
		shared.action_(this, {|val| this.action(val) });
		api.share(key, this, desc);
	}
	
	
	action { |value|
		
		count = count + 1;
		//("count is" + count).postln;
		//"value is %\n".postf(value.value);
		if (count == n, { // only broadcast every n updates
			
			count = 0;
			
			if (tag.notNil && broadcast, {  // use the tag we passed in
				api.sendMsg(tag, value.value);
			} , {
				if((broadcast && (listeners.size > 0)),{
					api.shareData(key, value.value);
				}, {
					//notify individual listeners
					listeners.do({ |punter|
						punter.sendMsg(tag ?? key, value.value);  // if there's a defined tag
					})
				})
			});
		})
	}
	
	addListener { |punter|
		punter.notNil.if({
			listeners = listeners.put(punter.nick, punter);
		})
	}
	
	removeListener { |punter|
		punter.notNil.if({
			listeners.removeAt(punter.nick)
		});
	}
	
	value {
		shared.value
	}
	
	n_ { |new|
		
		new = new.floor;
		if ((new < 1), { new = 1 });
		n = new;
	}
	
	/*
	slider { |parent, bounds, label, controlSpec, action, initVal, initAction, labelWidth,
				numberWidth, unitWidth, labelHeight, layout, gap|
		
		var ez, action;
		
		
		
		ez = EZSlider(parent, bounds, label?? tag ?? key)
		
	}	
	*/
	
	
}

SharedResourceEvent : Event {
	
	//var <event;
	//var isPlaying;
	var activeEvent;
	
	*synth{|evt, dict|
		^super.new.init_synth(evt, dict);
	}
	
	init_synth { |evt, dict|
		
		this.isPlaying = false;
		this.parent = this[\synthEvent];
		//var keys, objects;
		this.pr_keyvalues(evt, dict);
		//event = event.synth;
		//this = event.copy;
		//event.play;
		this.synth;
	}
	
	
	pr_keyvalues { |evt, dict|
		
		var shared_R;
		
		evt.isNil.if({ evt = Event.new });
		
		evt.keysValuesDo({ |key, val|
			this.put(key, val);
			//this.set(key, val);
		});
		
		dict.keysValuesDo ({ |key, val|
			
			//keys = keys.add(key);
			//"dict.keysValuesDo".postln;
			//"key % value %\n".postf(key, val);
			
			//(key.notNil && val.notNil).if({
			
			(val.isKindOf(Collection) || val.isKindOf(List)).if ({
				// is this a list with a function in it?
				//"list".postln;
				shared_R = val.first;
				//this.put(key, shared_R);
				shared_R.action_({ |shared|
					var result;
					
					result = val[1].value(shared.value); // evaluate the function
					this[\isPlaying].if({
						this.set(key, result);
					});
					this.put(key, result);
				});
				//this.set(key, val[1].value(shared_R.value));
				//this.put(key, shared_R);
				this.put(key, val[1].value(shared_R.value));
			} , { // or is it a . . . ?
				
				(val.isKindOf(SharedCV)).if({
					val = val.shared;
				});
				
				(val.isKindOf(SharedRemoteListeners)).if({
					val = val.shared;
				});
				
				(val.isKindOf(SharedResource)).if ({
					//"sharedResource".postln;
					shared_R = val;
					//this.put(key, shared_R);
					shared_R.action_({ |shared|
						//shared.value.postln;
						this[\isPlaying].if({
							this.set(key, shared.value)
						});
						this.put(key, shared.value)
					});
					//"key % value %\n".postf(key, shared_R.value);
					//this.set(key, shared_R.value);
					this.put(key, shared_R.value);
					
				}, {
					
					// just some normal value;
					this.put(key, val);
					//this.set(key, val);
				});
			});
				
			//objects = objects.add(shared_R);
			//this.put(key, shared_R);
			//});
		});
		
		//^evt;
	}
	
	play { |...args|
		
		var evt;
		
		
		//this[\isPlaying].not.if({
			this.isPlaying = true;
			super.play(*args);
		//} , {
		//	this.resume;
		//})
		
		
		//evt = this.copy;
		
		//activeEvent = evt;
		//activeEvent.play(*args);
	}
	/*
	stop {|...args|
		activeEvent.stop(*args);
		activeEvent = nil;
	}
	*/	
	
}