// Testing using code from a website. Have to test with his specified app.
// Phone must pair with HC-06 and then Connect from app.
// Check if LED blinks as expected with app?

#include <SoftwareSerial.h>
SoftwareSerial mySerial(4, 2);
char led = 0;

void setup() {
  //pinMode(3, INPUT);
  //pinMode(2, OUTPUT);

  Serial.begin(9600); //open the serial port
  delay(200);
  mySerial.begin(9600); // open the bluetooth serial port
  delay(200);
  pinMode(12, OUTPUT);
  //delay(2000);
}


void loop()
{
  if (mySerial.available() > 0) // Send data only when you receive data:
  {
    char data = mySerial.read();
    //led = isDigit(data) ? data : 'n';
    if (isDigit(data)) {
      Serial.println(data);
      if ((int)data == 49)           //Checks whether value of data is equal to 1
        digitalWrite(12, HIGH);  //If value is 1 then LED turns ON
      else if ((int)data == 48)      //Checks whether value of data is equal to 0
        digitalWrite(12, LOW);   //If value is 0 then LED turns OFF
    } 
  }

  if (Serial.available() > 0) {
    mySerial.println(Serial.readString());  // send from bluetooth to serial
  }
}



