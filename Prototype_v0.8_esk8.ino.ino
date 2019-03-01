// Testing using code from a website. Have to test with his specified app.
// Phone must pair with HC-06 and then Connect from app.
// Check if LED blinks as expected with app?

#include <SoftwareSerial.h>
#include <Servo.h>

#define BAUDRATE 9600
#define BT_ZERO 48
#define BT_MAX 57
#define ESC_ZERO 90
#define ESC_MAX 130

#define LED_PIN 12
#define PWM_PIN 9
#define BT_RX 4
#define BT_TX 2

// mySerial ( RX (input pin), TX ( output transmission pin))
SoftwareSerial mySerial(BT_RX, BT_TX);
Servo ESC;
char data;
int val;

void setup() {
  Serial.begin(BAUDRATE); //open the serial port
  delay(200);
  mySerial.begin(BAUDRATE); // open the bluetooth serial port
  delay(200);
  pinMode(LED_PIN, OUTPUT);
  delay(200);

  // Servo setup
  ESC.attach(PWM_PIN);
  delay(1000);
}


void loop()
{
  if (mySerial.available() > 0) // Send data only when you receive data:
  {
    data = mySerial.read(); //read data from bluetooth over serial

    //    if (isDigit(data)) {
    //      Serial.println(data);
    //      if ((int)data == 49)           //Checks whether value of data is equal to 1
    //        digitalWrite(12, HIGH);  //If value is 1 then LED turns ON
    //      else if ((int)data == 48)      //Checks whether value of data is equal to 0
    //        digitalWrite(12, LOW);   //If value is 0 then LED turns OFF
    //    }
    //  }

    if (isDigit(data)) {
      Serial.print("Data received from Bluetooth: ");
      Serial.println(data);
      mySerial.print("Data received from Bluetooth: ");
      mySerial.println(data);

      val = map((int)data, BT_ZERO, BT_MAX, ESC_ZERO, ESC_MAX);
      if ((int)data <= BT_ZERO) {
        digitalWrite(LED_PIN, LOW);
        val = ESC_ZERO;
        ESC.write(ESC_ZERO);

        Serial.println("BT_ZERO");
      } else {
        digitalWrite(LED_PIN, HIGH);
        if ((int)data >= BT_MAX) {
          data = BT_MAX;
          val = ESC_MAX;
          Serial.println("MAX");
        }
        ESC.write(val);
      }
    } // Comment out this section for troubleshooting

  }
}



