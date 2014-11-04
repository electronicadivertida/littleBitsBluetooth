#include <SoftwareSerial.h>

#define RST 5
#define TxD 6
#define RxD 7
#define KEY 8

#define SALIDA 2
#define ENTRADA 3
#define MAESTRO 4

#define INTR1 9
#define INTR2 10
#define INTR3 11
#define INTR4 12

#define secAT 0
#define secNAME 1
#define secPASSW 2
#define secRESET 3
#define secUSAR 4



SoftwareSerial BTSerial(TxD, RxD);
int mlSecuencia=secAT;
int mlIntr=0;
int mlMaestro = LOW;
int mlEntrada = LOW;
boolean mbPrimeraVez=true;

void setup()
{
pinMode(RST, OUTPUT);
pinMode(KEY, OUTPUT);
pinMode(SALIDA, OUTPUT);
pinMode(MAESTRO, INPUT);
pinMode(INTR1, INPUT);
pinMode(INTR2, INPUT);
pinMode(INTR3, INPUT);
pinMode(INTR4, INPUT);
pinMode(ENTRADA, INPUT);
digitalWrite(SALIDA, LOW);
Serial.begin(9600);
delay(100);
}
void loop()
{
/*  
  int lintr2=digitalRead(INTR2);
  int lintr3=digitalRead(INTR3);
  int lintr4=digitalRead(INTR4);
*/
  int lintr1=digitalRead(INTR1);
  int lintr2=0;
  int lintr3=0;
  int lintr4=0;

  int lintr=lintr1 + (lintr2*2) + (lintr3*2*2) + (lintr4*2*2*2);
  int lMaestro=digitalRead(MAESTRO);
  if(mlIntr!=lintr || lMaestro!=mlMaestro || mbPrimeraVez){
    mbPrimeraVez=false;
    mlSecuencia = secAT;
    mlIntr=lintr;
    mlMaestro=lMaestro;
    reset(HIGH);
    enviarHC05("AT");
    enviarHC05("AT+ADDR?");
    if(mlMaestro==LOW){
      /*Esclavo*/
      enviarHC05("AT+ROLE=0");
    }else{
      /*Maestro*/
      enviarHC05("AT+ROLE=1");
    }  
    mlSecuencia =secNAME;    
    
    String lsName = "LITTLE";
    lsName=lsName+String(mlIntr);
    enviarHC05("AT+NAME="+lsName);
    mlSecuencia =secPASSW;    
    /*El password que nunca supere lso 4 digitos*/
    enviarHC05("AT+PSWD="+String(mlIntr));
    mlSecuencia =secRESET;    
  }
  if(mlSecuencia == secRESET && !BTSerial.available()){
    reset(LOW);
    mlSecuencia =secUSAR;    
  }
  /*Si hay datos disponibles*/
  if (BTSerial.available()){
    /*los leemos*/
    byte lbBT = BTSerial.read();
    
    if(mlSecuencia == secUSAR){
      Serial.println("Lectura Bluetooth: " + String(lbBT));
      /*activamos salida si la secuencia es secUSAR*/
      if(lbBT == 0){
        digitalWrite(SALIDA, LOW);
      }else{
        digitalWrite(SALIDA, HIGH);
      }
    }else{
      Serial.write(lbBT);      
    }
  }
  /*Si cambia la entrada la enviamos*/
  int lentrada=digitalRead(ENTRADA);
  if(lentrada!=mlEntrada){
    mlEntrada=lentrada;
    Serial.println("Entrada LittleBits: " +String(mlEntrada));
    BTSerial.write(mlEntrada);
  }
  
}
void enviarHC05(String psCommand){
    Serial.println(psCommand);
    BTSerial.print(psCommand+"\r\n");
    delay(100);
}
void reset(int plKEY){
    Serial.println("RESET");
    digitalWrite(RST, LOW);
    delay(500);
    digitalWrite(KEY, plKEY);
    digitalWrite(RST, HIGH);
    delay(500);
    if(plKEY==HIGH){
      Serial.println("BAUDIOS 38400");
      BTSerial.begin(38400);
    }else{
      Serial.println("BAUDIOS 9600");
      BTSerial.begin(9600);
    }
    BTSerial.flush();
    delay(500);
    
}



