Mount an HC-05 to your arduino, connect one or two loads.

**Considering your arduino programming**
for the first load: The buttons are programmed to send 0 --> to turn it off // 1 --> turn it on
for the second load: The buttons are programmed to send 3 --> to turn it off // 4 --> turn it on
- You may adjust those at main activity class

Features (for each power load):
* Real-time monitoring of AMPS, W, W.H, and cost in JD. (You should send data all in one "Serial.print" statement for each second from your arduino).
* User-input countdown timers to turn off loads
* User-input cost limit value (turn off loads if the real-time value exceeds the input value)
* Manual turn on/off buttons

- This android app is built on top of devld's raw hc-05 send/receive android app.
Enjoy!

