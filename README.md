BiLETools
=========

BiLE was a laptop band in the 2010s. Some members used MAX/MSP and others used SuperCollider.

The tools we developed will hopefully be useful to future laptop bands.

This version of our tool set is now a quark. The patches for some specific pieces have been removed from this version but are preserved at https://github.com/BiLEnsemble/BiLETools

This library was designed to share data such that a user could share data. This shared data
would be readable and writeable locally via code, via a GUI, and via the network. This was done in a
MVC model. This data could potentially be written by a passed function, which could make the
data change ina thread-safe manner. The updated data would then notify all listners of the change.

Users could also advertise OSC messages they wish to receive.

Below follows our original whitepaper.

----------------------------------------------------------------

This document describes the networking infrastructure in use by BiLE.

The goal of the infrastructure design has been flexibility for real time
changes in sharing network data and calling remote methods for users of
languages like supercollider. While this flexibility is somewhat lost to
users of inflexible languages like MAX, they, nevertheless, can benefit
from having a structure for data sharing.


## Network Models

If there is a good reason, for example, a remote user, we support
OSCGroups as a means of sharing data.
If all users are located together on the same subnet, then we use
broadcast on port 57120.

## OSC Prefix

By convention, all OSC messages start with '/bile/'

## Data Restrictions

Strings must all be ASCII. Non ASCII characters will be ignored.

## Establishing Communication
### Identity
#### ID

Upon joining the network, users should announce their identity:

`/bile/API/ID nickname ipaddress port`

Nicknames must be ASCII only. Every user must have a unique nickname.
“API” is reserved and cannot be used as a nickname.

##### Example:

`/bile/API/ID Nick 192.168.1.66 57120`

Note that because broadcast echoes back, users may see their own ID
arrive as an announcement.

#### IDQuery

Users should also send out their ID in response to an IDQuery:

`/bile/API/IDQuery`

Users can send this message at any time, in order to compile a list of
everyone on the network.

### API
#### Query

Users can enquire what methods they can remotely invoke and what data
they can request.

`/bile/API/Query`

In reply to this, users should send `/bile/API/Key` and `/bile/API/Shared`
(see below)

#### Key

Keys represent remote methods. The user should report their accessible
methods in response to a Query

`/bile/API/Key symbol desc nickname`

* The symbol is an OSC message that the user is listening for.
* The desc is a text based description of what this message does. It
should include a usage example.
* The nickname is the name of the user that accepts this message.

##### Example

`/bile/API/Key /bile/msg "For chatting. Usage: msg, nick, text" Nick`

#### Shared

Shared represents available data streams. Sources may include input
devices, control data sent to running audio processes or analysis. The
user should report their shared data response to a Query

`/bile/API/Shared symbol desc`

* The symbol is an OSC message that the user sends with. The format of
this should be `/bile/nickname/symbol`
* The desc is a text based description of the data. If the range is not
between 0-1, it should mention this.
* The nickname is the name of the user that accepts this message.

##### Example

`/bile/API/Shared /bile/Nick/freq "Frequency. Not scaled."`

### Listening
#### RegisterListener

Shared data will not be sent out if no one has requested it and it may
be sent either directly to interested users or to the entire group, at
the sender's discretion. In order to ensure receiving the data stream, a
user must register as a listener.

`/bile/API/registerListener symbol nickname ip port`

* The symbol is an OSC message that the user will be listening for. It
should correspond with a previously advertised shared item. If the
receiver of this message recognises their own nickname in the symbol
(which is formatted /bile/nickname/symbol), they should return an error:
`/bile/API/Error/noSuchSymbol`
* The nickname is the name of the user that will accept the symbol as a
message.
* The ip is the ip address of the user that will accept the symbol as a
message.
* The port is the port of the user that will accept the symbol as a
message.

##### Example

`/bile/API/registerListener /bile/Nick/freq Shelly 192.168.1.67 57120`

### Error
#### noSuchSymbol

In the case that a user receives a request to register a listener or to
remove a listener for data that they are not sharing, they can reply
with

`/bile/API/Error/noSuchSymbol OSCsymbol`

* The symbol is an OSC message that the user tried to start or stop
listening to. It is formatted `/bile/nickname/symbol`. Users should not
reply with an error unless they recognise their own nickname as the
middle element of the OSC message. This message may be sent directly to
the confused user.

##### Example

`/bile/API/Error/noSuchSymbol /bile/Nick/freq`

### De-listening
#### RemoveListener
To announce an intention to ignore subsequent data, a user can ask to be
removed.

`/bile/API/removeListener symbol nickname ip`

* The symbol is an OSC message that the user will no longer be listening
for. If the receiver of this message sees their nickname in the symbol
which is formatted `/bile/nickname/symbol`), they can reply with
`/bile/API/Error/noSuchSymbol symbol`
* The nickname is the name of the user that will no longer accept the
symbol as a message.
* The ip is the ip address of the user that will no longer accept the
symbol as a message.

##### Example

`/bile/API/removeListener /bile/Nick/freq Shelly 192.168.1.67`

#### RemoveAll
Users who are quitting the network can asked to be removed form
everything that they were listening to.

`/bile/API/removeAll nickname ip`

* The nickname is the name of the user that will no longer accept any
shared data.
* The ip is the ip address of the user that will no longer accept any
shared data.

##### Example

`/bile/API/removeAll Nick 192.168.1.66`

## Commonly Used Messages
### Chatting
#### Msg

This is used for chatting.

`/bile/msg nickname text`

* The nickname is the name of the user who is sending the message.
* The text is the text that the user wishes to send to the group.

### Clock

This is for a shared stopwatch and not for serious timing applications

#### Clock start or stop

`/bile/clock/clock symbol`

* The symbol is either start or stop.
#### Reset

Reset the clock to zero.

`/bile/clock/reset`

#### Set

Set the clock time

`/bile/clock/set minutes seconds`

* Minutes is the number of minutes past zero.
* Seconds is the number of seconds past zero.

## Proposed Additions

Because users can silently join, leave and re-join the network, it could
be a good idea to have users time out after a period of silence, maybe
around 30 seconds or so. To stay active, they would need to send I'm
still here messages.

There should possibly also be a way for a user to announce that they
have just arrived, so, for example, if a SuperCollider user recompiles,
her connection will think of itself as new and other users will need to
delete or recreate connections depending on that user.
