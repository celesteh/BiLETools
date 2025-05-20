WebSocketResponder {

	//classvar client;
	//classvar <ip;
	var port;
	var <recvPort;
	var responders;
	var netAddr;
	var <>echo;
	var <canChangeName;
	var <jsResponder;
	var webview;
	var browser;


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



	*new {|username, userpass, groupname, grouppass, url, port|

		^super.new.init(username, userpass, groupname, grouppass, url, port);

	}

	init{|username, userpass, groupname, grouppass="bacon", url, port|


		var reloadStop, prev, next, urlBox, go;

		// This code is from the WebView helpfile. It's overkill, unless your webshost includes a login requirement.

		webview = WebView()
		.minSize_(300@200);
		reloadStop = Button()
		.states_([["※"], ["◙"]])
		.fixedSize_(36@28);
		prev = Button()
		.states_([["⇦"]])
		.fixedSize_(36@28);
		next = Button()
		.states_([["⇨"]])
		.fixedSize_(36@28);
		urlBox = TextField()
		.minWidth_(100);
		go = Button()
		.states_([["⌘"]])
		.fixedSize_(36@28);

		reloadStop.action = {
			|v|
			if (v.value == 1) {
				webview.reload(true);
			} {
				webview.stop;
			}
		};

		prev.action = { webview.back };
		next.action = { webview.forward };
		urlBox.action = { webview.url = urlBox.string };
		go.action = { webview.url = urlBox.string };

		webview.onUrlChanged = {
			|view, url|
			urlBox.string = url;
		};

		webview.onLoadStarted = {
			reloadStop.value = 1;
			urlBox.background = Color.grey(0.4);
		};

		webview.onLoadFinished = {
			reloadStop.value = 0;
			urlBox.background = Color.grey(0.2);
		};

		webview.onLoadFailed = {
			reloadStop.value = 0;
			urlBox.background = Color.red(1, 0.2);
		};

		browser = View(bounds:900@700).layout_(VLayout(
			HLayout(
				prev, reloadStop, next,
				urlBox,
				go
			),
			webview
		));

		//browser.front;

		//urlBox.valueAction = "http://supercollider.github.io/"
		urlBox.valueAction = url;



		responders = IdentityDictionary.new;
		echo = false;
		canChangeName = false;

		recvPort = this.class.pr_getPort(port ? NetAddr.langPort, 5) ? NetAddr.langPort;

		jsResponder = {

			AppClock.sched(0, {
				//"getMesg".debug(WebSocketResponder);
				webview.runJavaScript("getMesg()", {|res|
					res.notNil.if({
						//res.postln;
						this.pr_dispatch(res, Process.elapsedTime);
						jsResponder.value; // recurse until the queue is empty
					});
				});
				nil;
			});
		};

	}


	join { |action|

		webview.runJavaScript("join()", {});
		webview.onJavaScriptMsg = { this.jsResponder.value };
		action.value;
	}


	pr_dispatch {|input, time|

		var key, func;

		input.debug(this);

		input.isKindOf(Collection).if({
			key = input[0];//input.removeAt(0);
		} , {
			key = input;
		});

		key = key.asSymbol;

		func = responders.at(key);
		func.notNil.if({
			func.value(time, this, input);
		});
	}

	addResp{ |key, func|

		var resp, function;

		// this is our primary way of doing this
		responders.put(key.asSymbol, func);


		//But let's add an OSC Responder also for shits and giggles.
		function = { |msg, time, addr, recvPort|
			//key.postln;
			func.value(time, this, msg);
		};

		OSCdef(key, function, key);
	}

	removeResp{ |key|

		responders.remove(key);
		OSCdef(key).clear;
		OSCdef(key).free;
	}

	sendMsg{  arg ... msg;

		var str;

		msg = msg.collect({|item|
			(item.isKindOf(SimpleNumber).not && item.isKindOf(Boolean).not).if({
				item = item.asString;
				item = item.replace("\"", "\\\"");
				item = item.quote;
			});
			item;
		});

		AppClock.sched(0, {
			webview.runJavaScript("sendMesg(%)".format(msg), {});
		});
	}

	newColleage{
	}


	myAddr{
		^[NetAPI.ip, recvPort];

	}

	show{
		browser.front;
	}


}