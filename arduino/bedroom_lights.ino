
#include <FastLED.h>

#define EQ7_AUDIO_IN A5
#define EQ7_STROBE 5
#define EQ7_RESET 9
#define LED_DATA 11
#define NUM_LEDS 77 // HARD MAX 80 (DRAWS ALMOST 5A)

CRGB leds[NUM_LEDS];

enum Mode{MUSIC, CHANNEL, AMBIENT, DYNAMIC, RANDOM};
Mode currentMode = 2;

// GENERAL
int spectrumValue[7]; // INTERNAL
const int filterValue = 200; // INTERNAL

bool on = false; // whether program is running or not
double brightness = 1;
bool white = false;



// MUSIC MODE
int MUSIC_hue_start = 0;
int MUSIC_hue_step = 3;

// CHANNEL MODE
int CHANNEL_channel = 0; 
int CHANNEL_hue = 0;

// AMBIENT MODE
int AMBIENT_hue = 0;

// DYNAMIC MODE
int DYNAMIC_currentColor = 0; // INTERNAL
int DYNAMIC_flowDirection = 1; // INTERNAL

int DYNAMIC_flow_speed = 5; 

// RANDOM MODE
int RANDOM_nextColor[NUM_LEDS][3]; // INTERNAL

int RANDOM_speed = 40; 

void setup() 
{ 
  pinMode(EQ7_AUDIO_IN, INPUT);
  pinMode(EQ7_STROBE, OUTPUT);
  pinMode(EQ7_RESET, OUTPUT);

  Serial.begin(9600);
  Serial.setTimeout(40); // for readString() so that it doesnt pause lighting effects while reading
  while(!Serial){}

  Serial.println("yuh"); // arduino is reset when serial port is opened by computer. to establish connection, the computer listens after opening

  FastLED.addLeds<WS2811, LED_DATA>(leds, NUM_LEDS); // REMEMBER TO CHANGE TO WS2811 FOR 12v STRIP
  for(int i = 0; i < NUM_LEDS; i++)
  {
    leds[i] = CRGB(0,0,0);
  }

  digitalWrite(EQ7_RESET, LOW);
  digitalWrite(EQ7_STROBE, HIGH);
}

void loop() 
{
  if(!on)
  {
    for(int i = 0; i < NUM_LEDS; i++)
    {
      leds[i] = CRGB(0,0,0);
    }
    FastLED.show();
    return;
  }

  if(currentMode == MUSIC)
  {
    digitalWrite(EQ7_RESET, HIGH);
    digitalWrite(EQ7_RESET, LOW);

    for(int i = 0; i < 7; i++)
    {
      digitalWrite(EQ7_STROBE, LOW);
      delayMicroseconds(80);
      spectrumValue[i] = analogRead(EQ7_AUDIO_IN);
      spectrumValue[i] = constrain(spectrumValue[i], filterValue, 1023);
      spectrumValue[i] = map(spectrumValue[i], filterValue, 1023, 0, 255);
      digitalWrite(EQ7_STROBE, HIGH);
    }

    int ledIndex = 0;
    for(int i = 0; i < 7; i++)
    {
      
      for(int j = 0; j < (NUM_LEDS/7); j++)
      {
        leds[ledIndex] = CHSV(MUSIC_hue_start + (MUSIC_hue_step * ledIndex), white ? 0 : 255, spectrumValue[i] * brightness);
        ledIndex++;
      }
    }
  }
  else if(currentMode == CHANNEL)
  {
    digitalWrite(EQ7_RESET, HIGH);
    digitalWrite(EQ7_RESET, LOW);
  
    for(int i = 0; i < 7; i++)
    {
      digitalWrite(EQ7_STROBE, LOW);
      delayMicroseconds(80);
      spectrumValue[i] = analogRead(EQ7_AUDIO_IN);
      digitalWrite(EQ7_STROBE, HIGH);
    }

    int litLeds = map(spectrumValue[CHANNEL_channel], filterValue, 1023, 0, NUM_LEDS - 1);
    for(int i = 0; i < NUM_LEDS; i++)
    {
      if(i < litLeds)
      {
        leds[i] = CHSV(CHANNEL_hue, white ? 0 : 255, 255 * brightness);
      }
      else
      {
        leds[i] = CHSV(0,0,0);
      }
    }
  }
  else if(currentMode == AMBIENT)
  {
    for(int i = 0; i < NUM_LEDS; i++)
    {
      leds[i] = CHSV(AMBIENT_hue, white ? 0 : 255, 255 * brightness);
    }
  }
  else if(currentMode == DYNAMIC)
  {
    DYNAMIC_currentColor += normalize(DYNAMIC_flowDirection);
    if(DYNAMIC_currentColor >= 360 || DYNAMIC_currentColor <= 0)
    {
      DYNAMIC_flowDirection *= -1;
    }

    for(int i = 0; i < NUM_LEDS; i++)
    {
      leds[i] = CHSV(DYNAMIC_currentColor, 255, 255 * brightness);
//      delay(100/DYNAMIC_flow_speed);
    }
    delay(1000/DYNAMIC_flow_speed);
  }
  else if(currentMode == RANDOM)
  {
    for(int i = 0; i < NUM_LEDS; i++)
    {
      if(leds[i].r > RANDOM_nextColor[i][0])
      {
        leds[i].r++;
      }
      else if(leds[i].r < RANDOM_nextColor[i][0])
      {
        leds[i].r--;
      }

      if(leds[i].g > RANDOM_nextColor[i][1])
      {
        leds[i].g++;
      }
      else if(leds[i].g < RANDOM_nextColor[i][1])
      {
        leds[i].g--;
      }

      if(leds[i].b > RANDOM_nextColor[i][2])
      {
        leds[i].b++;
      }
      else if(leds[i].b < RANDOM_nextColor[i][2])
      {
        leds[i].b--;
      }

      if(leds[i] == CRGB(RANDOM_nextColor[i][0] * brightness, RANDOM_nextColor[i][1] * brightness, RANDOM_nextColor[i][2] * brightness))
      {
        for(int j = 0; j < 3; j++)
        {
          leds[i][j] = random(0,255);
        }
      }

//      delay(100/RANDOM_speed);
    }
  }

  FastLED.show();
}

void serialEvent()
{
  String message = Serial.readString();
  message.trim();
  Serial.println(message + " in return");
  decodeCommand(message);
}

void decodeCommand(String command)
{
  String base = command.substring(0, command.indexOf(' '));
  command = command.substring(command.indexOf(' ') + 1);
  if(base == "sta") // change state (on/off)
  {
    on = constrain(command.substring(0,2).toInt(), 0, 1) != 0;
  }
  else if(base == "mod") // change mode
  {
//    currentMode = constrain(command.substring(0,2).toInt(), 0, 4);
    currentMode = command.substring(0,2).toInt();
  }
  else if(base == "bri")
  {
    brightness = constrain(command.substring(0,3).toDouble(), 0, 1.0);
  }
  else if(base == "whi")
  {
    white = constrain(command.substring(0,2).toInt(), 0, 1) != 0;
  }
  else if(base == "set")
  {
    Serial.println("set");
//    Mode mode = constrain(command.substring(0,2).toInt(), 0, 4);
    Mode mode = command.substring(0,2).toInt();
    command = command.substring(2);
    Serial.println(command);
    String key = command.substring(0, command.indexOf(' '));
    command = command.substring(command.indexOf(' ') + 1);
    Serial.println(command);
    int value = command.substring(0, command.indexOf(' ')).toInt();
    setSetting(mode, key, value);
  }
//  Serial.println(base);
//  Serial.println(command);
}

int normalize(int d)
{
  return d/abs(d);
}

int getSetting(Mode mode, String key)
{
  if(mode == MUSIC)
  {
    if(key == "hue_start")
    {
      return MUSIC_hue_start;
    }
    else if(key == "hue_step")
    {
      return MUSIC_hue_step;
    }
  }
  else if(mode == CHANNEL)
  {
    if(key == "channel")
    {
      return CHANNEL_channel;
    }
    else if(key == "hue")
    {
      return CHANNEL_hue;
    }
  }
  else if(mode == AMBIENT)
  {
    if(key == "hue")
    {
      return AMBIENT_hue;
    }
  }
  else if(mode == DYNAMIC)
  {
    return DYNAMIC_flow_speed;
  }
  
  else if(mode == RANDOM)
  {
    return RANDOM_speed;
  }
}

void setSetting(Mode mode, String key, int value)
{
  if(mode == MUSIC)
  {
    if(key == "hue_start")
    {
      MUSIC_hue_start = constrain(value, 0, value);
    }
    else if(key == "hue_step")
    {
      MUSIC_hue_step = value;
    }
  }
  else if(mode == CHANNEL)
  {
    if(key == "channel")
    {
      CHANNEL_channel = constrain(value, 0, 6);
    }
    else if(key == "hue")
    {
      CHANNEL_hue = constrain(value, 0, value);
    }
  }
  else if(mode == AMBIENT)
  {
    if(key == "hue")
    {
      AMBIENT_hue = constrain(value, 0, value);
    }
  }
  else if(mode == DYNAMIC)
  {
    DYNAMIC_flow_speed = constrain(value, 0, 100);
  }
  else if(mode == RANDOM)
  {
    RANDOM_speed = constrain(value, 0, 100);
  }
}
