const int redLED = 8;
const int solid = 0;
const int flash = 1;

const int buttonPIN = 2;
int currentState = 0;       //current state of OUTPUT pin
int reading;                //current reading from INPUT pin
//int previousState = LOW;    //previous reading from INPUT pin

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  pinMode(redLED, OUTPUT);
  pinMode(buttonPIN, INPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  reading = digitalRead(buttonPIN);

  if(reading == HIGH && currentState == 0) {
    Serial.print("Testing: Switch to flashing\n");
    currentState = 1;
    delay(500);
  } else if (reading == HIGH && currentState == 1) {
    Serial.print("Testing: Switch to SOLID\n");
    currentState = 0;   
    delay(500);
  }
  
  if(flashRED((int)currentState) ) {
    //digitalWrite(redLED, LOW);
  }
  
}


bool flashRED(int blink) {
  // flashes at 0.5Hz in the correct mode
  
  if (blink == flash) {
    digitalWrite(redLED, HIGH);
    delay(500);
    digitalWrite(redLED, LOW);
    delay(2000);
    return true;
  } else if (blink == solid) {
    digitalWrite(redLED, HIGH);
    return true;
  } else {
    return false;
  }
}
