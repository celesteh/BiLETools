SharedSynthDef : SynthDef {

	var <>owner, <>time;

	*newFrom{|aSynthDef|

		aSynthDef.isKindOf(SynthDef).if ({
			^this.new(aSynthDef.name, aSynthDef.func).initShared();
		});
		aSynthDef.isKindOf(UntrustedSynthDef).if({
			^aSynthDef.build().initShared(aSynthDef.time);
		});
	}

	*new {arg ...args;
		^super.new(*args).initShared();
	}

	initShared{|date|
		time = time ? date ? Date.getDate;
	}


	earlier {|aSharedSynthDef|

		^(aSharedSynthDef.time.rawSeconds > time.rawSeconds)
	}

	later {|aSharedSynthDef|

		^(aSharedSynthDef.time.rawSeconds < time.rawSeconds)
	}


	/*
	*new { arg name, ugenGraphFunc, rates, prependArgs, variants, metadata;
		^super.newCopyArgs(name.asSymbol).variants_(variants).metadata_(metadata ?? {()}).children_(Array.new(64))
			.build(ugenGraphFunc, rates, prependArgs)
	}
	*/

}

UntrustedSynthDef {

	// this just holds data
	var <user, <data, name, <time;

	*new {|user, data, name|
		^super.newCopyArgs(user, data, name).init();
	}

	init {

		time = Date.getDate;
	}

	name {

		var str, index, arr;

		data.isKindOf(SynthDef).if({
			name = data.name;
			^name;
		});

		name.notNil.if({ ^name });

		// Try to parse the String
		// Which probably looks like SynthDef(\foo, {|bar| });
		data.isKindOf(String).if({
			str = data.stripWhiteSpace;
			index = str.find("SynthDef"); //find this and ditch it
			index.notNil.if({
				str = str.copyRange(index + 8, str.size-1);
				arr = str.split($\(); // get rid of the (
				(arr.size > 1).if({
					str = arr[1];
					arr = str.split($\,); //then the name, then a comma
					str = arr[0];
					str = str.stripWhiteSpace;
					[$\\, $\", $\'].includes(str[0]).if({ // is it single quotes, double quotes a \ ?
						str = str.copyRange(1, str.size-1);
					});
					[$\", $\'].includes(str.last).if({
						str = str.copyRange(0, str.size-2);
					});

					name = str.asSymbol;
					^name;
				})
			})
		});

		//no luck
		^nil;
	}

	earlier {|aUntrustedSynthDef|

		^(aUntrustedSynthDef.time.rawSeconds > time.rawSeconds)
	}

	later {|aUntrustedSynthDef|

		^(aUntrustedSynthDef.time.rawSeconds < time.rawSeconds)
	}


	build {

		var synthDef = data;

		synthDef.isKindOf(Function).if({
			//synthDef = synthDef.value;
		//	synthDef = synthDef.asCompileString;
		});


		synthDef.isKindOf(String).if({
			//synthDef.replace("SynthDef", "SharedSynthDef");
			synthDef = synthDef.interpret;
		});

		synthDef.isKindOf(Function).if({
			synthDef = synthDef.value;
			//synthDef = synthDef.asCompileString;
		});

		synthDef.isKindOf(SharedSynthDef).not.if({
			synthDef.isKindOf(SynthDef).if({
				synthDef = synthDef.as(SharedSynthDef);
			})
		});

		synthDef.isKindOf(SharedSynthDef).if({
			synthDef.owner = user;
			synthDef.time = time;
		})

		^synthDef
	}

	add {

		var sd;

		sd = this.build;
		sd.add;
		^sd;
	}

	asCompileString{

		data.isKindOf(String).if({
			^data;
		});
		^data.asCompileString;
	}
}

SharedSynthDefs {

	classvar <>default;
	var api, subscribeRemote, >trustRemote, local, approved, pending, semaphore;


	*new {|netAPI, subscribeRemote=false, trustRemote=false, threadsafe = false|
		^super.newCopyArgs(netAPI, subscribeRemote, trustRemote).init(threadsafe);
	}

	init {|threadsafe|

		this.class.default.isNil.if({
			this.class.default = this;
		});

		local = [];
		approved = [];
		pending = [];

		this.subscribeRemote = subscribeRemote;
		this.threadsafe = threadsafe;

	}

	subscribeRemote_ {|bool|

		bool.if({

			api.add('synthDef', { arg synthDef, user;

				var userobj = api.getUser(user) ? user;

				//synthDef = this.prTweak(synthDef);
				synthDef = UntrustedSynthDef(userobj, synthDef);
				//synthDef.owner = userobj;

				this.trustRemote.if({
					this.approve(synthDef);
				} , {
					// we don't trust it, so add it to a pending queue
					//semaphore.notNil.if({
					//	semaphore.wait;
					//});

					//pending = pending ++ synthDef;
					this.enqueue(synthDef);

					//semaphore.notNil.if({
					//	semaphore.signal;
					//});

				});

			}, "Share your SynthDefs. See the SharedsynthDefs class");
		} , {

			api.remove('synthDef');
		});

	}


	threadsafe_{|bool, shouldWait = false|

		bool.if({
			semaphore.notNil.if({
				semaphore = Semaphore(1);
			});
		} , {
			semaphore.notNil.if({
				shouldWait.if({
					semaphore.wait;
				});
				semaphore = nil;
			});
		});
	}

	threadsafe { ^semaphore.notNil }


	at {|key, searchOrder|

		var result;
		searchOrder = searchOrder ? [local, approved, pending];
		key = key.asSymbol;

		searchOrder.do({|arr|

			arr.notNil.if({
				result.isNil.if({
					result = arr.detect({|item| item.name.asSymbol == key });
				})
			})
		});

		^result;
	}

	remove {|key, threadsafe = true|

		var synthDef, obj;

		(threadsafe && semaphore.notNil).if({
			semaphore.wait;
		});

		key.isKindOf(SynthDef).not.if({
			synthDef = this.at(key);
			synthDef.respondsTo(\name).if({
				synthDef = this.at(synthDef.name);
			});
			synthDef.isNil.if({
				synthDef = this.prTweak(key);
				synthDef.respondsTo(\name).if({
					synthDef = this.at(synthDef.name);
				});
			});
		} , {
			//synthDef = key;
			synthDef = this.at(key.name);
		});


		synthDef.notNil.if({
			[local, approved, pending].do({|arr|
				obj = arr.remove(synthDef);
				obj.notNil.if({ this.remove(key, false) }); // get all of them!!
			});
		});

		(threadsafe && semaphore.notNil).if({
			semaphore.signal;
		});


	}

	enqueue {|synthDef, threadsafe=true|

		var obj, name;

		//get rid of older items with the same name in the pending queue
		// -> find them
		// stick this at the end


		//prepare the data

		synthDef.isKindOf(SynthDef).not.if({

			synthDef.isKindOf(UntrustedSynthDef).not.if({
				synthDef = UntrustedSynthDef(synthDef);

			});
		});

		name = synthDef.name;


		(threadsafe && semaphore.notNil).if({
			semaphore.wait;
		});

		name.notNil.if({
			pending = pending.reject({|item| item.name == name }); //only objects that don't match
		});

		pending = pending ++ synthDef;

		(threadsafe && semaphore.notNil).if({
			semaphore.wait;
		});
	}



	approve {|synthDef, threadsafe=true|

		// make sure it's fine
		// add it to the server
		// get rid of others with the same name
		// add it to the list

		synthDef = this.prTweak(synthDef);

		synthDef.add;


		(threadsafe && semaphore.notNil).if({
			semaphore.wait;
		});


		// remove other synths with the same name
		this.remove(synthDef.name, false); // don't wait, we're already in a semaphore

		approved = approved.add(synthDef);

		(threadsafe && semaphore.notNil).if({
			semaphore.signal;
		});

	}


	add {|synthDef, advertise=true, threadsafe=true|

		synthDef = this.prTweak(synthDef);

		synthDef.add;


		(threadsafe && semaphore.notNil).if({
			semaphore.wait;
		});


		// remove other synths with the same name
		this.remove(synthDef.name, false); // don't wait, we're already in a semaphore

		local = local.add(synthDef);

		(threadsafe && semaphore.notNil).if({
			semaphore.signal;
		});


		advertise.if({
			this.advertise(synthDef)
		});

	}

	advertise {|synthDef|

		//api.sendMsg('msg', api.nick, blah)
		api.sendMsq('synthDef', synthDef.asCompileString, api.nick);

	}

	prTweak {|synthDef, trust=false|

		synthDef.isKindOf(SynthDef).not.if({
			synthDef.isKindOf(UnTrustedSynthDef).not.if({
				synthDef = UntrustedSynthDef(nil, synthDef);
			});

			trust.if({
				synthDef = synthDef.build();
			});
		} , {

			synthDef.isKindOf(SharedSynthDef).not.if({
				synthDef = synthDef.as(SharedSynthDef);
			});
		});



					/*
		synthDef.isKindOf(Function).if({
			//synthDef = synthDef.value;
		//	synthDef = synthDef.asCompileString;
		});


		synthDef.isKindOf(String).if({
			//synthDef.replace("SynthDef", "SharedSynthDef");
			synthDef = synthDef.interpret;
		});

		synthDef.isKindOf(Function).if({
			synthDef = synthDef.value;
			//synthDef = synthDef.asCompileString;
		});

		synthDef.isKindOf(SharedSynthDef).not.if({
			synthDef.isKindOf(SynthDef).if({
				synthDef = synthDef.as(SharedSynthDef);
			})
		});
					*/

		^synthDef
	}

}