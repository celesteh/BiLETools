OscGroupClientResponder {

	classvar client;
	var responders;
	var <echo;
	var <canChangeName;

	*new {|path, serveraddress, username, userpass, groupname, grouppass|

		^super.new.init(path, serveraddress, username, userpass, groupname, grouppass);

	}

	init{|path, serveraddress, username, userpass, groupname, grouppass="bacon"|

		client.isNil.if ({

			//"killall OscGroupClient".unixCmd;
			OscGroupClient.program_(path);

			client = OscGroupClient(serveraddress, username, userpass, groupname.toLower, grouppass);
		});

		responders = Dictionary.new;
		echo = false;
		canChangeName = false;

	}


	join { |action|
		"join - OscGroupClientResponder".postln;
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

	recvPort {
		^NetAddr.langPort;
	}

	newColleage{
	}

	// this needs more thought
	myAddr{
		^[client.netAddr.ip, client.netAddr.port]; // this is who you message to reach me
	} // but you have to be logged in, so this doesn't help

}

BroadcastResponder {

	//classvar client;
	//classvar <ip;
	var port;
	var <recvPort;
	var responders;
	var netAddr;
	var <echo;
	var <canChangeName;

	*pr_getPort {|requestedPort, maxTries=5|
		var success, keepTrying = true, i=0;

		{keepTrying}.while({

			requestedPort = requestedPort + i;
			success = thisProcess.openUDPPort(requestedPort);
			keepTrying = success.not; //If we failed, try again
			i = i +1;
			// Don't loop this forever
			keepTrying = keepTrying && ( i <= maxTries);
		});

		success.if({
			^requestedPort;
		}, {
			"failed to get port".warn;
			^nil;
		})
	}


	*new {|port|

		^super.new.init(port);

	}

	init{|sharedPort, ip|

		var maxTries = 5;

		ip = ip ? "255.255.255.255";
		port = sharedPort ? NetAddr.langPort;
		responders = [];//Dictionary.new;
		NetAddr.broadcastFlag = true;

		// Allw some port drift in case of crashes
		netAddr =  [ NetAddr(ip, port) ];

		// Now ask for the port, with the allowable drift
		recvPort = this.class.pr_getPort(port, maxTries);

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
			key.postln;
			func.value(time, this, msg);
		};

		OSCdef(key, function, key);

		key.postln;

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

		netAddr.do({|n| n.sendMsg(*msg)});
	}

	newColleage {|user|

		var ports, userPort;

		userPort = user.netAddr.port;

		ports = netAddr.collect({|n| n.port });
		ports.includes(userPort).not.if ({ // we are not messaging this port

			netAddr = netAddr ++ NetAddr("255.255.255.255", userPort);
		});
	}


	myAddr{
		^[NetAPI.ip, recvPort];

	}



}

IndividualResponder : BroadcastResponder {

	// Exactly the same, but we don't broadcast

	*new {|port|
		^super.new.init(port);
	}

	init {|port|

		super.init(port);
		netAddr = [];
		echo = false;

	}

	// Add every user
	newColleage {|user|
		this.addAddress(user.netAddr);
	}

	// Add any address, including the one from an OscGroupClient
	addAddress{|addr|

		var ips, userIp, shouldAdd = true;

		userIp = addr.ip;

		ips = netAddr.collect({|n| n.ip });
		ips.includes(userIp).if ({ // we are not messaging this port
			netAddr.do({|n|
				((n.ip == addr.ip) && (n.port == addr.port)).if({
					// we already have them
					shouldAdd = false;
				})
			});
		});

		shouldAdd.if({

			netAddr = netAddr ++ addr;
		});
	}


}


