// Ver 0.5.0,  11 May 2011


// WTF. why is it not finding everyone

NetAPI {

	classvar <all, <listeners,<>defaultResponse ='/response', <ip;
	var <client, <colleagues, user_update_listeners;
	var <name, functions, <nick;
	var <remote_functions, remote_update_listeners;
	var shared, <remote_shared;


	*initClass {
		var getIP;



		all = IdentityDictionary.new;
		listeners = Dictionary.new;

		getIP = {arg action;
			var before = NetAddr.broadcastFlag;
			NetAddr.broadcastFlag = true;
			OSCresponder(nil, '/getMyIP', { arg t,r,msg,addr;
				action.(addr);
				NetAddr.broadcastFlag = before;
				r.remove;
			}).add;

			NetAddr("255.255.255.255", NetAddr.langPort).sendMsg('/getMyIP');
			nil;
		};

		getIP.value({arg addr;
			ip =  addr.ip;
		});
	}

	*new { |path = "broadcast", serveraddress, username, userpass|

		^(all.at('bile') ?? {

			^super.new.init(path, serveraddress, username, userpass)});
	}

	*oscgroup { |path = "broadcast", serveraddress, username, userpass|

		^(all.at('bile') ?? {

			^super.new.init(path, serveraddress, username, userpass)});
	}

	*multicast { |username|

		"Depricated: please switch to broadcast".warn;

	}

	*broadcast { |username|

		if (all.at('bile').notNil, {
			"Already Connected".warn;
			^all.at('bile').nick_(username);
		} , {
			^super.new.init(\broadcast, nil, username);
		});
	}


	*default {
		^(all.at('bile'))
	}

	init { |path = "broadcast", serveraddress, username, userpass|


		name = 'bile';
		remote_functions = Dictionary.new;
		remote_update_listeners = Dictionary.new;
		user_update_listeners = Dictionary.new;
		functions = Dictionary.new;
		colleagues = Dictionary.new;
		//shared = Dictionary.new;
		remote_shared = Dictionary.new;

		//ip = MulticastResponder.ip;

		nick = username;

		path.isKindOf(String).if ({
			path = path.standardizePath;
			File.exists(path).if({

				/*
				OscGroupClient.program_(path);

				client = OscGroupClient(serveraddress, username, userpass, "bile", "bacon");
				*/
				//"APIResponder".postln;
				client = APIResponder(path, serveraddress, username, userpass);
			} , {

				"No such path\nDefaulting to broadcast".warn;
				client = BroadcastResponder.new;
			});
		} , {

			(path != \broadcast). if ({

				"Invalid first argument.\nDefaulting to broadcast".warn;

			});

			client = BroadcastResponder.new;
		});

		client.join({

			Task ({
				0.2.rand.wait;
				this.sendMsg('API/removeAll', nick, ip);
				this.init_sharing;
				this.init_querying;
				(0.1 + 0.1.rand).wait;
				this.identify;
				(1 + 1.0.rand).wait;
				this.remote_query;

			}).play;

		});
		all.put(name, this);


		//api = API('bile');
		//api.mountClient;

	}


	local { arg ... args;
		this.call(*args);
	}

	remote { arg ... args;
		this.sendMsg(*args)
	}

	nick_ { |username|
		("You cannot change your name while you are already connected.").warn;
		/*
		if (username != nick, {
			client.canChangeName.if({
				colleagues[username].isNil.if({
					nick = username;
				} , {
					(username.asString + " is already in use.").warn;
				});
			} , {
				("You cannot change your name while you are already connected.").warn;
			})
		})
		*/
	}

	init_querying {

						// set up API Querying
		//"init querying".postln;
		client.addResp(("/" ++ name ++ "/API/Query").asSymbol,  { arg time, resp, msg;
			var desc;
						//"query".postln;
			Task({ // pause between sending keys
				functions.keysValuesDo({ |key, dat|

					desc = "";

					(dat.isKindOf(SequenceableCollection)).if({
						(dat.size > 1).if({
							desc = dat[1];
					})});
					this.advertise(key, desc, nick);
					//("key" + key).postln;
					0.1.rand.wait;
				})
			}).play;

			Task({ // pause between sending keys
				shared.notNil.if({
					shared.keysValuesDo({ |key, dat|
						/*
						desc = "";

						(dat.isKindOf(SequenceableCollection)).if({
							(dat.size > 1).if({
								desc = dat[1];
						})});
						*/
						this.advertiseShared(key, dat.desc);
						//("key" + key).postln;
						0.1.rand.wait;
					})
				})
			}).play;
		});

		client.addResp(("/" ++ name ++ "/API/Key").asSymbol, { arg time, resp, msg;
			var key, desc, user, username;

			//msg.postln;

			key = msg[1];
			desc = msg[2];
			username = msg[3];

			(key.asString.containsStringAt(0, "/"++name++"/")).if ({
				key = key.asString[6..];
				key = key.asSymbol;
			});


			username.notNil.if({

				user = colleagues[username.asSymbol];
				user.notNil.if({
					user.addFunctionKey(key, desc);
					colleagues[username.asSymbol] = user;
				});
			});

						//[key.asSymbol, desc, msg].postln;
						//remote_functions.keys.postln;
						//remote_functions.includes(key.asSymbol).not.if ({
							remote_functions.put(key.asSymbol, desc ?? "");
							remote_update_listeners.do({|action|
								action.value(key.asSymbol, desc);
							})
						//})
		});

		client.addResp(("/" ++ name ++ "/API/IDQuery").asSymbol,  { arg time, resp, msg;
						"IDquery".postln;
						//client.sendMsg('/bile/API/ID', nick, this.my_ip, NetAddr.langPort);
						this.identify;
							//("key" + key).postln;
		});

		client.addResp(("/" ++ name ++ "/API/ID").asSymbol,  { arg time, resp, msg;
						var new_user, username, my_nick;
						//msg.postln;

						my_nick = nick.asString.replace(" ", "");
						username = msg[1].asString.replace(" ", "");

						(username != nick.asString).if ({
							username = username.asSymbol;
							colleagues[username].isNil.if({
								new_user = User.new(msg[2], msg[3], msg[1]);
								this.addUser(new_user);
								//("adding" + username + "to" + nick + [msg[2], msg[3], msg[1]]).postln;
							})
						});
		});


		client.addResp(("/" ++ name ++ "/API/Shared").asSymbol, { arg time, resp, msg;

			var key, desc;

			key = msg[1];
			desc = msg[2];

			(key.asString.containsStringAt(0, "/"++name++"/")).if ({
				key = key.asString[6..];
				key = key.asSymbol;
			});
			// remove leading /
			(key.asString.containsStringAt(0, "/")).if({
				key = key.asString[1..];
				key = key.asSymbol;
			});
			// don't count your own stuff
			(key.asString.containsStringAt(0, nick.asString ++ "/")).not.if({

				desc = desc ? "";

				//"%\t%\n".postf(key, desc);
				remote_shared = remote_shared.put(key, desc);

				//and let listeners know
				remote_update_listeners.do({|action|
					action.value(key.asSymbol, desc);
				})
			});
		});

	}

	addUser { |user|

		var username;


		username = user.nick;

		["username", username].postln;

		(username != nick).if ({
			colleagues[username].isNil.if({

				colleagues.put(username, user);
				user_update_listeners.do({|action|
					action.value(user);
				});
			})
		})
	}


	init_sharing {

		"init sharing".postln;

		shared.isNil.if({
			shared = Dictionary.new;
		});

		client.addResp(("/" ++ name ++ "/API/registerListener").asSymbol, { arg  time, resp, msg;

			var symbol, username, ip, port;
			var data, user, sym;

			symbol = msg[1];
			username = msg[2];
			ip = msg[3];
			port = msg[4];

			symbol = symbol.asString;
			sym = symbol;
			//symbol.postln;

			(symbol.containsStringAt(0, "/"++name++"/")).if ({
				symbol = symbol[6..];
				//symbol.postln;
				(symbol.containsStringAt(0, nick.asString++"/")).if ({

					//"it's me!".postln;

					symbol = symbol[nick.asString.size..];
					symbol = symbol.asSymbol;
					data = shared[symbol];

					(data.isNil && symbol.asString.containsStringAt(0, "/")).if({
						symbol = symbol.asString[1..].asSymbol;
						data = shared[symbol];
						data.isNil.if({
							data = shared[sym];
							data.isNil.if({
								data = shared[this.pr_sharedFormat(symbol)]
							});
						});
					});

					//symbol.postln;

					user = colleagues[username];
					user.isNil.if({
						user = User(ip, port, username);
						this.addUser(user);
						//"new user".postln;
					});


					data.notNil.if({


						data.addListener(user, this);
						// send them the current value
						user.sendMsg(this.pr_sharedFormat(sym), data.value);
					} , {
						//"error".postln;
						user.sendMsg(("/" ++ name ++ "/API/Error/noSuchSymbol").asSymbol,
								("/"++name++"/" ++ nick ++ "/" ++ symbol).asSymbol);
					});
				})
			});

		});

		client.addResp(("/" ++ name ++ "/API/removeListener").asSymbol, { arg  time, resp, msg;
			var symbol, username, ip;
			var data, user;

			symbol = msg[1];
			username = msg[2];
			ip = msg[3];

			symbol = symbol.asString;

			(symbol.containsStringAt(0, "/"++name++"/")).if ({
				symbol = symbol[6..];
				(symbol.containsStringAt(0, nick.asString)).if ({
					symbol = symbol[nick.asString.size..];
					symbol = symbol.asSymbol;
					data = shared[symbol];

					user = colleagues[username];
					user.isNil.if({
						data.notNil.if({
							data.removeListener(user);
						} , {
							user.sendMsg(("/" ++ name ++ "API/Error/noSuchSymbol").asSymbol,
								("/"++name++"/" ++ nick ++ "/" ++ symbol).asSymbol);
						})
					})
				})
			})
		});

		client.addResp(("/" ++ name ++ "/API/removeAll").asSymbol, { arg time, resp, msg;

			var username, ip;
			var user;

			username = msg[1];
			ip = msg[2];

			user = colleagues[username];
			user.isNil.if({
				shared.do({ |item|
					item.removeListener(user);
				})
			})
		});

		client.addResp(("/" ++ name ++ "/API/Error/noSuchSymbol").asSymbol, { arg time, resp, msg;

			var symbol;
			symbol = msg[1];

			("No such symbol" + symbol).warn;
		});

	}

		// defining
	add { arg selector,func, desc;
		functions.put(selector.asSymbol, [func, desc]);
		client.addResp(this.pr_formatTag(selector).asSymbol, { arg time, resp, msg;
			var result,returnAddr,returnPath;
			result = NetAPI.prFormatResult( this.call(selector,*msg[1..])
										/*func.value(*msg[1..])*/ );
			//# returnAddr,returnPath = API.prResponsePath(addr);
			//returnAddr.sendMsg(*([returnPath] ++ result));
			//msg.postln;
		});
		this.advertise(selector, desc);
	}
	addAll { arg dict;
		//functions.putAll(dict)
		dict.do({|item, index|
			add(index. item)
		});
	}
	make { arg func;
		this.addAll(Environment.make(func))
	}

	remove{ arg selector;
		functions.removeAt(selector).isNil.if({
			remote_functions.removeAt(selector).notNil.if({
				remote_update_listeners.do({|action|
					action.value(selector, "");
				})
			})
		}, {
			// get rid of it everywhere
			this.remove(selector);
			client.removeResp(selector);
		})
	}

	exposeMethods { arg obj, selectors;
		selectors.do({ arg m;
			this.add(m,{ arg ... args; obj.performList(m,args) })
		})
	}
	exposeAllExcept { arg obj,selectors=#[];
		obj.class.methods.do({ arg meth;
			if(selectors.includes(meth.name).not,{
				this.add(meth.name,{ arg ... args; obj.performList(meth.name,args) })
			})
		})
	}

	// calling
	call { arg selector ... args;
		var m, ret;

		//try {
			//[selector, args].postln;

		m = functions[selector.asSymbol];

		m.notNil.if({
			m = m.first;
			//args.postln;
			^m.valueArray(args);
			//ret = true;
			}, {
				//ret = false;
				^DoesNotUnderstandError(this, selector, args);
			});
			// , {
			//	(remote_functions.includes(selector)). if ({
			//		this.sendMsg(selector, args);
			//		"Sending to the network"
			//	} , {
			//		(selector.asString + "not found in NetAPI" + name).warn;
			//	})
			//})
		//} {|exception| exception.throw; }
		^ret;
	}
	// create a function
	func { arg selector ... args;
		^{ arg ... ags; this.call(selector,*(args ++ ags)) }
	}
	// respond as though declared functions were native methods to this object
	doesNotUnderstand { arg selector ... args;
		var ret;
		ret = this.call(selector,*args);
		ret.notNil.if({
			ret.isKindOf(Exception).if({
				ret.throw;
			});
		});
	}
	// '/app/cmdName', arg1, arg2
	*call { arg selector ... args;
		var blank,app,cmd;
		# blank,app ... cmd = selector.asString.split($/);
		^this(app).call(cmd.join($/).asSymbol,*args)
	}
	// maybe better separated by API
	*registerListener { arg callsFromNetAddr,sendResponseToNetAddr,responseCmdName;
		listeners[callsFromNetAddr] = [sendResponseToNetAddr,responseCmdName];
	}

	// interrogating
	functionNames {
		^functions.keys
	}

	help { |selector|
		var the_thing;

		selector = selector.asSymbol;

		the_thing = functions[selector];
		the_thing.notNil.if({ // is it local
			^the_thing.last;
		}, {
			the_thing = remote_functions[selector];
			the_thing.notNil.if({ // is it remote
				^"Remote:" + the_thing;
			} , {
				the_thing = remote_shared[selector];
				the_thing.notNil.if({ // is it a shared item
					^"Shared data:" + the_thing;
				}, {
					the_thing = "Not Found";
					the_thing.warn;
					^"WARNING:" + the_thing;
				});
			})
		})
	}

	*prResponsePath { arg addr;
		var l;
		l =listeners[addr];
		if(l.notNil,{
			^[l[0],l[1] ? defaultResponse]
		});
		^[addr,defaultResponse]
	}
	*prFormatResult { arg result;
		^if(result.isString,{
			result = [result];
		},{
			result = result.asArray;
		});
	}

	printOn { arg stream;
		stream << this.class.asString << "('" << name << "')"
	}


	pr_formatMsg{  arg ... msg;
		var sym;
		//msg.dump;

		//(msg[0].asString.containsStringAt(0, "/bile/")).not.if ({
		//	sym = ("/bile/" ++ (msg[0].asString ?? "")).asSymbol;

		//	msg[0] = sym;
		//});
		//[sym, msg, msg[0]].postln;
		msg[0] = this.pr_formatTag(msg[0]);

		^msg;
	}

	pr_formatTag { arg symbol;

		(symbol.asString.containsStringAt(0, "/"++name++"/")).not.if ({
			symbol.asString.containsStringAt(0, "/").not.if({
				symbol = ("/"++name++"/" ++ (symbol.asString ?? "")).asSymbol;
			}, {
				symbol = ("/" ++ name ++ (symbol.asString ?? "")).asSymbol;
			})
		});

		^symbol;
	}

	pr_sharedFormat { arg symbol;

		var prefix;

		prefix = "/"++name++"/" ++ nick;

		(symbol.asString.containsStringAt(0, prefix)).not.if ({
			symbol.asString.containsStringAt(0, "/").not.if({
				symbol = (prefix ++ "/" ++ (symbol.asString ?? "")).asSymbol;
			}, {
				symbol = (prefix ++ (symbol.asString ?? "")).asSymbol;
			})
		});

		^symbol;
	}


	sendMsg{  arg ... msg;

		var sym;
		/*
		sym = ("/bile/" ++ (msg[0].asString ?? "")).asSymbol;
		(msg.size > 1).if ({
			msg[0] = sym;
			client.sendMsg(*msg);
			//[[sym] ++ msg[1..]].postln;
			msg.postln;
		} , {
			client.sendMsg(sym);
			sym.postln;
		});
		*/
		sym = this.pr_formatMsg(*msg);
		client.sendMsg(*sym);
		//sym.postln;
		client.echo.not.if({
			this.call(*msg)
		});


	}


	msgUser { arg user ... msg;

		var sym;

		sym = this.pr_formatMsg(*msg);

		//user.isK
	}

	remote_query {
		Task({
			this.sendMsg('API/IDQuery');
			"done IDQuery".postln;
			0.2.wait;
			this.sendMsg('API/Query');
		}).play;
	}

	advertise { | selector, desc = ""|

		desc.isNil.if({ desc =" ";});

		this.sendMsg('API/Key', this.pr_formatTag(selector), desc, nick)
	}


	share { |selector, data, desc|

		shared.isNil.if({
			this.init_sharing
		});

		selector = this.pr_sharedFormat(selector);
		//data.symbol = selector; // make sure they're the same
		//data.desc = desc; // ditto
		data.key = selector;
		shared.put(selector, data);
		// make sure we can update from a network
		//this.add(selector, {|input| data.value_(input.value, this)});
		this.advertiseShared(selector, desc);
		^selector;
	}

	advertiseShared {| selector, desc = ""|

		"advertising".postln;

		this.sendMsg('API/Shared', this.pr_sharedFormat(selector), desc)
	}

	subscribe { | selector|
		var resource;
		selector = selector.asSymbol;
		resource = SharedResource.new;
		//resource.mountAPI(this, selector);
		//SharedRemoteListener(selector, this, resource);
		selector = this.pr_formatTag(selector);
		this.sendMsg('API/registerListener', selector, nick, this.my_ip, NetAddr.langPort);
		//("subscribing to" + selector).postln;
		this.add(selector, { |input|
			resource.value_(input, this);
			//("" + selector ++ ":" + input).postln;
		});
		^resource;
	}

	map { |selector, key|

		var resource;

		resource = this.subscribe(selector);
		resource.action_(key, {|val| this.call(key, val.value)});
	}


	shareData { |selector, value|

		//selector.postln;
		selector = this.pr_sharedFormat(selector);
		this.sendMsg(selector, value);
	}


	identify {
		"identifying".postln;
		this.sendMsg('API/ID', nick, this.my_ip, NetAddr.langPort);
	}

	add_remote_update_listener { |owner, action|

		remote_update_listeners = remote_update_listeners.put(owner, action);
	}

	remove_remote_update_listener { |owner|
		remote_update_listeners.removeAt(owner);
	}

	add_user_update_listener { |owner, action|

		user_update_listeners = user_update_listeners.put(owner, action);
	}

	remove_user_update_listener { |owner|
		user_update_listeners.removeAt(owner);
	}

	my_ip {
		^NetAPI.ip
	}

	hostname {
		^"hostname".unixCmdGetStdOut.replace("\n", "");
	}

	echo {

		^client.echo;
	}

	chat {

		"Depricated. Use BileChat(NetAPI) instead.".warn;
		^BileChat(this)
	}

	clock {

		"Depricated. Use BileClock(NetAPI) instead.".warn;
		^BileClock(this)
	}
}

APIResponder {

	classvar client;
	var responders;
	var <echo;
	var <canChangeName;

	*new {|path, serveraddress, username, userpass|

			^super.new.init(path, serveraddress, username, userpass);

		}

	init{|path, serveraddress, username, userpass|

		client.isNil.if ({

				//"killall OscGroupClient".unixCmd;
			OscGroupClient.program_(path);

			client = OscGroupClient(serveraddress, username, userpass, "bile", "bacon");
		});

		responders = Dictionary.new;
		echo = false;
		canChangeName = false;

	}


	join { |action|
		"join - APIResponder".postln;
		client.join;//(action);
		action.value;
	}


	addResp{ |key, func|
		//("adding" + key).postln;

		client.addResp(key, func);

		//responders.put(key,

		//	OSCresponderNode(nil, key, func));
	}


	removeResp{ |key|
		var resp;

		//resp = responders.at(key);
		//resp.notNil.if({ resp.remove});
		client.removeResp(key);
	}


	sendMsg{  arg ... msg;
		//"send".postln;
		//("sending" + msg).postln;
		client.sendMsg(*msg);

	}



}

BroadcastResponder {

	//classvar client;
	//classvar <ip;
	var responders;
	var netAddr;
	var <echo;
	var <canChangeName;


	*new {

			^super.new.init;

		}

	init{

		responders = [];//Dictionary.new;
		NetAddr.broadcastFlag = true;
		netAddr = NetAddr("255.255.255.255", 57120);
		echo = true;
		canChangeName = true;
	}


	join { |action|

		action.value;
	}


	addResp{ |key, func|

		var resp, function;
		//client.addResp(key, func);

		resp = //OSCresponderNode(nil, key, func);
		//resp = resp.add;
		function = { |msg, time, addr, recvPort|
			func.value(time, this, msg);
		};

		OSCdef(key, function);

		responders = responders ++ key;
	}


	removeResp{ |key|
		var resp;

		//resp = responders.at(key);
		//resp.notNil.if({ resp.remove});
		responders.remove(key);
		OSCdef(key).clear;
		OSCdef(key).free;
	}

	sendMsg{  arg ... msg;

		netAddr.sendMsg(*msg);
	}


}


