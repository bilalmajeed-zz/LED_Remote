# Smart Switch
A bluetooth connection used to controll an realy connected to an Arduino board

---

Using the power an Arduino paired with a HCI-05 Bluetooth Module, I built a modular Bluetooth controlled switch. The number of unique applications for this stretches as far as your imagination. As of right now I am using it as a remote light switch, primarily to turn on and off the lights when I enter or exit the room.

###How it works
Serial data consisting of ones and zeros are transferred over Bluetooth to the Arduino. From here the microcontroller interprets the data and either switches the relay on or off depending on the data. Now, the intended goal of this project was to allow people to use there own methods of Bluetooth data transfer but I believed that it would be beneficial and convenient if a companion app was developed to allow easy control over the switch. So I developed an app , with simplicity in mind, that only has three buttons (Connect, ON, OFF).

![alt text](https://raw.githubusercontent.com/bilalmajeed/LED_Remote/master/switch.jpg "Bluetooth Switch")

[Demo Video](https://youtu.be/hMrsDu7hRd0)
