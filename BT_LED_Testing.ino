//Testing using code from a website. Have to test with his specified app.
// Phone pairs with HC-06 but doesn't stay connected? Normal? 
// Check if LED blinks as expected with app

char data = 0;                //Variable for storing received data

void setup()
{
  Serial.begin(9600);         //Sets the data rate in bits per second (baud) for serial data transmission
  pinMode(8, OUTPUT);        //Sets digital pin 8 as output pin
}

void loop()
{
  if (Serial.available() > 0) // Send data only when you receive data:
  {
    data = Serial.read();      //Read the incoming data and store it into variable data
    Serial.print(data);        //Print Value inside data in Serial monitor
    Serial.print("\n");        //New line
    if (data == '1')           //Checks whether value of data is equal to 1
      digitalWrite(8, HIGH);  //If value is 1 then LED turns ON
    else if (data == '0')      //Checks whether value of data is equal to 0
      digitalWrite(8, LOW);   //If value is 0 then LED turns OFF
  }

}
