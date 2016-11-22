//======message protocol constants=======
const char SentSep = ',';
const char PartSep = ':';
const char updateW = 'W';
const char updateV = 'V';
const char recalcW = 'R';
//======message protocol ends=======
//======global var for info storage======
double accumWater=0;
double totalWeight=0;
boolean needResendW = true;
//======global var for info storage end======

String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete
long lastT = 0;
#define led 13

void setup(){
  Serial.begin(9600);
  pinMode(led, OUTPUT);
  inputString.reserve(30);
}

void loop()
{
  // print the string when a newline arrives:
  if (stringComplete) {
    if(inputString[0]==recalcW){
      totalWeight++;
      String msg = updateW+PartSep+String((int)totalWeight)+SentSep;
      Serial.println(msg);
    }
    // clear the string:
    inputString = "";
    stringComplete = false;
  }
  
  long currT = millis();
  if(currT-lastT>1000){
    String msg = String(updateV)+PartSep+String((int)accumWater)+SentSep;
    accumWater++;
    if(needResendW){
      msg = msg+updateW+PartSep+String((int)totalWeight)+SentSep;
    }
    Serial.println(msg);
    lastT = currT;
  }
  serialEvent();
}

/*
  SerialEvent occurs whenever a new data comes in the
 hardware serial RX.  This routine is run between each
 time loop() runs, so using delay inside loop can delay
 response.  Multiple bytes of data may be available.
 */
void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      stringComplete = true;
    }
  }
}

void ledOn()
 {
    analogWrite(led, 255);
    delay(10);
  }

void ledOff()
{
    analogWrite(led, 0);
    delay(10);
}
 

    
