// Testing using code from a website. Have to test with his specified app.
// Phone must pair with HC-06 and then Connect from app.
// Check if LED blinks as expected with app?

#include <SoftwareSerial.h>
#include <Servo.h>

#define BAUDRATE 9600

// defines for bluetooth input states
#define BT_ZERO 48
#define BT_BRAKE 49
#define BT_CENTER 50
#define BT_POWER 51
#define BT_MAX 53

// defines for esc power states
#define ESC_ZERO 90
#define ESC_BRAKE 98
#define ESC_CENTER 106
#define ESC_POWER 114
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

    if (isDigit(data)) {
      Serial.print("Data received from Bluetooth: ");
      Serial.println(data);
      mySerial.print("Data received from Bluetooth: ");
      mySerial.println(data);
      
      val = map((int)data, BT_ZERO, BT_MAX, ESC_ZERO, ESC_MAX);
      // Checking if input is coast or brake
      if ((int)data <= BT_CENTER && (int)data >= BT_ZERO) {
        digitalWrite(LED_PIN, LOW);
        // if mapped value to ESC is below what's possible, reset to ESC_ZERO (full brake)
        if (val < ESC_ZERO) {
          val = ESC_ZERO;
        }
        ESC.write(val);
        Serial.println(data);
      } // end if braking or coast

      // Checking if input is power (3 levels)
      else if ((int)data >= BT_POWER && (int)data <= BT_MAX) {
        digitalWrite(LED_PIN, HIGH);
        if (val > ESC_MAX) {
          val = ESC_MAX;
        }
        ESC.write(val);
      } // end else if power

      // Checking if output is out of range (else)
      else {
        digitalWrite(LED_PIN, LOW);
        val = ESC_ZERO;
        ESC.write(val);
      } // end else out of range
      
    } // Comment out this section for troubleshooting

  }
}



