User {
	
	var <>netAddr, <>nick, <>requests, <>offers, api, shared;
	
	
	*new { arg addr, port, nick;
		
		^super.new.init(addr, port, nick);
	}
	
	
	init { arg addr, port, nick;
		
		netAddr = NetAddr(addr.asString, port.asString.asInt);
		nick.notNil.if ({
			this.nick = nick.asSymbol;
		});
		offers = [];
		requests = [];
		api = Dictionary.new;
	}
	
	updateAddress{ arg addr, port;
		netAddr = NetAddr(addr.asString, port.asString);
	}		
	
	addService { arg offer;
		
		this.offers = this.offers.add(offer.asSymbol);
	}
	
	removeService { arg rescinded;
		
		this.offers.remove(rescinded.asSymbol);
	}
	
	addListener { arg request;
		
		this.requests = this.requests.add(request.asSymbol);
	}
	
	removeListener { arg nevermind;
		
		this.requests.remove(nevermind.asSymbol);
	}
	
	sendMsg { arg ... msg;
		
		netAddr.sendMsg(*msg);
	}
	
	addFunctionKey { |key, desc|
		
		//"key % desc %\n".postf(key.asSymbol, desc);
		api.put(key.asSymbol, desc);
		api.keys.postln;
	}
	
	callableFunctions{
		var arr;
		arr = api.keys;
		arr = arr.asArray;
		//"arr %\n".postf(arr);
		^arr;
	}
		
}
		