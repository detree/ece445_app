# ece445_app
app component and test ino files
You should able to auto-import it into Android Studio. The build is build toward Android6, API23. Using Java7/8

#Organization of code
1. `BTActions.java`
Communication functions for bluetooth
2. `BTCommActivity.java`
The `Activity` (GUI page) for interfacing with bluetooth and GPS
2. `DeviceList.java`
The `Activity` (GUI page) for select devices (__no chage__ from adaption)
3. `GPSComm.java`
The interfaces towards the GPS. GPS is using the interface provided by the `Google Play Service`. There is minimum things we could do.
4. `MsgFormatter.java`
Encodeing/decoding the bluetooth output/input messages. A custom protocol. __No Synchronization__ promised. However, the bluetooth interface seems a interface like TCP which provide predictable and stable messages
5. `Parameter.java`
The parameters used in the `handlers`. Recommend __no change__.
