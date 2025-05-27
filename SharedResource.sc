
SharedResource {

	var value, <signed_actions, unsigned_actions, semaphore, spec;
	//var <>symbol, remote_listeners, api, <>desc;
	var <>changeFunc, lastAdvertised, has_ever_changed;
	var <>silent;

	*new {arg value, threadSafe = false, changeFunc, silent=true;
		^super.new.init(value, threadSafe, changeFunc, silent);
	}

	init { arg item, threadSafe = false, changed, isSilent=true;

		threadSafe = threadSafe ? false;
		value = item;
		silent = isSilent;

		threadSafe.if({
			semaphore = Semaphore(1);
		});

		changeFunc = changed;
		lastAdvertised = value;

		changeFunc.isNil.if({
			changeFunc = {|old, new| old != new}
		});

		has_ever_changed = item.notNil;//false;

		//"SharedResource %".format(value).postln;

	}

	init_value {|value, changer|

		has_ever_changed.not.if({
			this.value_(value, changer)
		})
	}

	spec_ {| s, v, changer|
			spec = s.value.asSpec;
			this.init_value(v ? spec.default, changer);

			this.addUniqueMethod(\input_, { arg in, theChanger ... moreArgs;
				this.value_(spec.map(in), theChanger, *moreArgs);
			});
	}

	/*
	sp	{ | default= 0, lo = 0, hi=0, step = 0, warp = 'lin', changer |
		this.spec_(ControlSpec(lo,hi, warp, step, default), changer:changer);
	}
	*/

	sp {
		this.deprecated(thisMethod, BileChat.class.findMethod(\spec_));
	}


	silentValue_ { arg newValue, theChanger ... moreArgs;

		"silent value".debug(this);

		semaphore.notNil.if({
			//"ready to wait".postln;
			semaphore.wait;

			value = newValue.value(value);

			//"notify".postln;
			semaphore.signal;
		}, {

			value = newValue.value(value);
		});
	}

	pr_doValue { arg newValue, theChanger ... moreArgs;

		var changed, result;

		"pr_doValue".debug(this);

		changed = false;

		result = newValue.value(value);// what that's doing there is letting people
			// write a mathematic function like
			// { |old| old + 1 }

		"result %".format(result).debug(this);

		changeFunc.value(lastAdvertised, result).if({ // here we're letting people
			// set their own thresholds and metrics, so they can write a boolean like
			// {|old, new| (old - new).abs > (old * 0.01)}

			// we have a change!
			lastAdvertised = result;
			changed = true;
		});

		value = result;

		"changed %".format(changed).debug(this);

		^changed;
	}

	value{

		var return_val;
		// If we have semaphore control, it should also be for reading the value, not just for writing it.
		semaphore.notNil.if({
			//"ready to wait".postln;
			semaphore.wait;
			return_val = value;
			semaphore.signal;
		} , {
			return_val = value;
		});

		^return_val;
	}

	value_ { arg newValue, theChanger ... moreArgs;

		var changed, result;


		silent.if({
			this.silentValue_(newValue, theChanger, *moreArgs);
		} , {

			"in value_".debug(this);

			changed = false;

			semaphore.notNil.if({
				"ready to wait".debug(this);
				semaphore.wait;

				changed = this.pr_doValue(newValue, theChanger, *moreArgs);

				"notify".debug(this);
				semaphore.signal;
			}, {

				changed = this.pr_doValue(newValue, theChanger, *moreArgs);
			});


			changed.if({

				"changed".debug(this);
				has_ever_changed = true;

				// notify others
				dependantsDictionary.at(this).copy.do({ arg dep;
					(dep === theChanger).not.if({
						"notify".debug(this);
						dep.update(this, theChanger, *moreArgs);
					}, {
						"don't notify %".format(dep).debug(this);
					});
				});
				signed_actions.notNil.if({
					signed_actions.keysDo({ |key|
						(key === theChanger).not.if({
							signed_actions[key].value(this, theChanger, *moreArgs);
							(""++ theChanger + "is notifying"+ key).debug(this);
						}, {
							(""++ theChanger + "is not notifying"+ key).debug(this);
						});
					});
				});
				unsigned_actions.notNil.if({
					unsigned_actions.do({|action|
						action.value(this, theChanger, *moreArgs);
					});
				});
			}, { /*"no change".postln;*/});
		});
	}

	changeAction_ { |arg1, arg2|
		this.action_(arg1, arg2);
	}

	action_ { |arg1, arg2|

		var owner, act;

		//also allow the user to pass in nil, action
		arg1.isNil.if({
			act = arg2;  //this.action_(nil, {})
		}, {
			arg2.isNil.if({
				act = arg1; // this.action_({})
			}, {
				owner = arg1;  //this.action_(action_owner, {})
				act = arg2;
			});
		});

		act.notNil.if({
			owner.notNil.if({
				if (signed_actions.isNil, {
					signed_actions = Dictionary.new();//IdentityDictionary.new(4);
				});

				signed_actions.put(owner, act);
				//"let's look at the dictionary".debug(this);
				//signed_actions.dump;
				//signed_actions.keys.debug(this);
			} , {
				// unknown owner, add the action to an array
				if (unsigned_actions.isNil, {
					unsigned_actions = [act];
				} , {
					unsigned_actions = unsigned_actions ++ act;
				});
			});
		});
		/*
		arg2.notNil.if({
		// arg1 is the adder and arg2 is the action
		if (signed_actions.isNil, {
		signed_actions = IdentityDictionary.new(4);
		});

		signed_actions.put(arg1, arg2);
		signed_actions.keys.postln;
		} , {
		// arg1 is the action
		if (unsigned_actions.isNil, {
		unsigned_actions = [arg1];
		} , {
		unsigned_actions = unsigned_actions ++ arg1;
		});
		});
		*/
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

	mountAPI { |api, key, desc, broadcast=true, symbol, owned|

		var remote;
		//symbol = oSCsymbol;
		//desc = description;
		remote = SharedRemoteListeners(key, api, this, desc, broadcast, symbol, owned);
		//api.share(symbol, remote, desc);
		"api mounted % % %".format(key, symbol, owned).debug(this);
		key.isNil.if({ Error("nil key").throw });
		^remote;
	}


}

SharedRemoteListeners {

	var listeners, <shared, broadcast, api, <>key, <>desc, <n, count, tag;

	*new{|key, api, shared, desc, broadcast = true, symbol, owned|

		^super.new.init(key, api, shared, broadcast, symbol, owned:owned);
	}

	init { |symbol, netapi, sharedResource, description, tellAll = true, osc_tag, owned|

		broadcast = tellAll;  if((broadcast.isNil), {broadcast = true});
		tag = osc_tag;
		api = netapi;
		key = symbol;
		shared = sharedResource; //shared.isNil.if({ shared = SharedResource.new});
		listeners = Dictionary.new;
		desc = description;
		count = 0;
		n = 1;
		shared.action_(this, {|val| this.action(val) });
		api.share(key, this, desc, owned);
		//api.add(key, {|input| shared.value_(input, this)});
	}


	action { |value|

		count = count + 1;
		//("count is" + count).postln;
		//"value is %\n".postf(value.value);
		if (count == n, { // only broadcast every n updates

			count = 0;

			if (tag.notNil && broadcast, {  // use the tag we passed in
				api.sendMsg(tag, value.value);
				//"sent to api".postln;
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


	action_ { |...args|
		shared.action_(*args);
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

	value_ {|...args|
		shared.value_(*args);
	}

	silentValue_ {|...args|
		shared.silentValue_(*args);
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

		this[\isPlaying] = false;
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
			//this.isPlaying = true;
		this[\isPlaying] = true;
			super.play(*args);
		//} , {
		//	this.resume;
		//})

		//this.isPlaying.debug(this);

		//evt = this.copy;

		//activeEvent = evt;
		//activeEvent.play(*args);
	}

	stop {|...args|
		//activeEvent.stop(*args);
		//activeEvent = nil;
		this.isPlaying = false;
		super.stop(*args);
	}

	isPlaying {
		var ret = true;

		this[\gate].notNil.if({
			ret = (this[\gate] != 0);
		});
		ret = ret && this[\isPlaying];
		^ret
	}

}
