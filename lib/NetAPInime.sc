// Ver 0.5.1,  28 May 2011

NetAPInime {
	
	classvar <all, <listeners,<>defaultResponse ='/response', <ip;
	var <client, <colleagues, user_update_listeners;
	var <name, functions, <nick;
	var <remote_functions, remote_update_listeners;
	var shared, <remote_shared;
	
	*initClass {

		all = IdentityDictionary.new;
		listeners = Dictionary.new;
	}
	

	*new { |path = "multicast", serveraddress, username, userpass|

		^(all.at('bile') ?? {
		
			^super.new.init(path, serveraddress, username, userpass)});
	}
	
	*oscgroup { |path = "multicast", serveraddress, username, userpass|

		^(all.at('bile') ?? {
		
			^super.new.init(path, serveraddress, username, userpass)});
	}

	*multicast { |username|
		
		if (all.at('bile').notNil, {
			"Already Connected".warn;
			^all.at('bile').nick_(username);
		} , {
			^super.new.init(\multicast, nil, username);
		});
	}
		
	
	*default {
		^(all.at('bile'))
	}
	
	init { |path = "multicast", serveraddress, username, userpass|
		
		ip = NetAPI.ip;
		
		name = 'bile';
		remote_functions = Dictionary.new;
		remote_update_listeners = Dictionary.new;
		user_update_listeners = Dictionary.new;
		functions = Dictionary.new;
		colleagues = Dictionary.new;
		shared = Dictionary.new;
		remote_shared = Dictionary.new;
		
		//ip = MulticastResponder.ip;
		
		nick = username;
		
		path.isKindOf(String).if ({
			File.exists(path).if({
			
				/*
				OscGroupClient.program_(path);
			
				client = OscGroupClient(serveraddress, username, userpass, "bile", "bacon");
				*/
				client = APIResponder(path, serveraddress, username, userpass);
			} , {
			
				"No such path\nDefaulting to multicast".warn;
				client = MulticastResponder.new;
			});
		} , {
				
			(path != \multicast). if ({
				
				"Invalid first argument.\nDefaulting to multicast".warn;
				
			});
			
			client = MulticastResponder.new;
		});		
			
		client.join({
				
			Task ({
				(0.01 + 0.2.rand).wait;
				this.sendMsg('API/removeAll', nick, ip);
				(0.1 + 0.1.rand).wait;
				//this.init_sharing;
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
		client.addResp(("/" ++ name ++ "/API/IDQuery").asSymbol,  { arg time, resp, msg;
						//"IDquery".postln;
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

		/*
		client.addResp(("/" ++ name ++ "/API/Shared").asSymbol, { arg time, resp, msg;
			
			var key, desc;

			key = msg[1];
			desc = msg[2];
			
			(key.asString.containsStringAt(0, "/"++name++"/")).if ({
				key = key.asString[6..];
				key = key.asSymbol;
			});
			
			remote_shared.put(key, desc);
		});
		*/	
	}
	
	addUser { |user|
		
		var username;
		
		username = user.nick;
		(username != nick).if ({
			colleagues[username].isNil.if({
		
				colleagues.put(username, user);
				user_update_listeners.do({|action|
					action.value(user);
				});
			})
		})
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
		var m;
		
		//try {
			//[selector, args].postln;
		
			m = functions[selector.asSymbol];
		
			m.notNil.if({
				m = m.first;
				//args.postln;
				^m.valueArray(args)
			}, {
				DoesNotUnderstandError(this, selector, args).throw;
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
	}
	// create a function
	func { arg selector ... args;
		^{ arg ... ags; this.call(selector,*(args ++ ags)) }
	}
	// respond as though declared functions were native methods to this object
	doesNotUnderstand { arg selector ... args;
		^this.call(selector,*args)
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
		
		
	}
	
	
	msgUser { arg user ... msg;
		
		var sym;
		
		sym = this.pr_formatMsg(*msg);
		
		//user.isK
	}
	
	remote_query {
		Task({
			this.sendMsg('API/IDQuery');
			0.2.wait;
			this.sendMsg('API/Query');
		}).play;
	}
	
	advertise { | selector, desc = ""|
		
		desc.isNil.if({ desc =" ";});
		
		this.sendMsg('API/Key', this.pr_formatTag(selector), desc, nick)
	}
	
	
	share { |selector, data, desc|
		
		
		selector = this.pr_sharedFormat(selector);
		//data.symbol = selector; // make sure they're the same
		//data.desc = desc; // ditto
		data.key = selector;			
		shared.put(selector, data);
		this.advertiseShared(selector, desc);	
	}
	
	advertiseShared {| selector, desc = ""|
		
		this.sendMsg('API/Shared', this.pr_sharedFormat(selector), desc)
	}
	
	subscribe { | selector|
		var resource;
		resource = SharedResource(symbol: selector);
		this.sendMsg('API/registerListener', this.pr_formatTag(selector), nick, this.my_ip, NetAddr.langPort);
		this.add(selector, { |input|
			resource.value_(input, this);
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

