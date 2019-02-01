#include <Servo.h>

#define BAUDRATE 19200                //Baudrate for serial port connection. Used as a console input

#define ESC_ZERO 0                   //Set lower limit for esc speed
#define SPEED_MAX 130                 //Set upper limit for esc speed

#define redLED 8                      //only being used as a solid colour when button is pressed
#define buttonPIN 2                   //constant pin numbers for button switch and esc signal
#define escPIN 9

int reading;                          //reading from switch at any given loop of intructions
//int escOutputValue = ESC_ZERO;        
//float currentOutputValue = ESC_ZERO;

Servo ESC;


void setup() {
  // put your setup code here, to run once:

  Serial.begin(BAUDRATE);
  pinMode(redLED, OUTPUT);
  ESC.attach(escPIN);
  ESC.write(ESC_ZERO);  //Set rotation of motor to zero
  pinMode(buttonPIN, INPUT);
  Serial.println("Serial initialized.\nESC initialized as a servo. No motion.\nLED output and Button input initialized.\n");
  delay(100);
}

void loop() {
  // put your main code here, to run repeatedly:
  
  reading = digitalRead(buttonPIN);
  if (reading == HIGH) {
    Serial.println("Circuit made");
    flashRED(0);
    Serial.println("LED lit");
    ESC.write(SPEED_MAX);
    Serial.println("Motor in motion\n\n");
  }
  if (reading == LOW) {
    Serial.println("Circuit broken");
    digitalWrite(redLED, LOW);
    Serial.println("LED off");
    ESC.write(ESC_ZERO);
    Serial.println("Motor stopping.\n\n");
  }
  
}

bool flashRED(int blink) {
  // flashes at 0.5Hz in the correct mode
  
  if (blink == 1) {
    digitalWrite(redLED, HIGH);
    delay(500);
    digitalWrite(redLED, LOW);
    delay(2000);
    return true;
  } else if (blink == 0) {
    digitalWrite(redLED, HIGH);
    return true;
  } else {
    return false;
  }
}
