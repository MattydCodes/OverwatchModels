import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import peasy.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class sketch_3d_scan_model_ow extends PApplet {


PeasyCam cam;

PShape model;
PShape[] models;
String[] names = {"ana.txt","ashe.txt","ball.txt","bap.txt","bastion.txt","bdva.txt","brig.txt","doom.txt","dva.txt","genji.txt","hanzo.txt","hog.txt","junk.txt","lucio.txt","mccree.txt","mei.txt","mercy.txt","moira.txt","orisa.txt","pharah.txt","reaper.txt","rein.txt","sigma.txt","soldier.txt","sombra.txt","sym.txt","torb.txt","tracer.txt","widow.txt","winston.txt","zarya.txt","zen.txt"};
int index = 0;
int rx = 360;
int ry = 100;

public void setup(){
  
  loadnew();
  cam = new PeasyCam(this,300);
  frameRate(60);
}

float lx = 0;
float ly = 0;

public void draw(){
  background(0);
  directionalLight(255, 245, 225,lx,-0.75f,ly);
  if(keyPressed){
    lx = cos(millis()/1000.0f);
    ly = cos(millis()/1000.0f);
  }
  ambientLight(50,50,50);
  for(int i = 0; i < 32; i++){
    translate(i*180,0,0);
    shape(models[i]);
    translate(-i*180,0,0);
  }
}

public String RemoveInvalidData(String data){
  data.replaceAll("\\);","\\)");
  data.replaceAll("\\}","");
  data.replaceAll("\\{","");
  data.replaceAll("\\,","");
  data.replaceAll(" ","");
  return data;
}

public PVector[] ParseDataToVector(String data){
  PVector[] temp = new PVector[36500];
  boolean parsing = true;
  while(parsing){
    if(data.length() <= 40){
      parsing = false;
    }
    
    int si = data.indexOf('(');
    int ei = data.indexOf(')',si);
    
    int yi = data.indexOf(';',si+1);
    int zi = data.indexOf(';',yi+1);
    float x = PApplet.parseFloat(data.substring(si+1,yi).trim());
    float y = PApplet.parseFloat(data.substring(yi+1,zi).trim());
    float z = PApplet.parseFloat(data.substring(zi+1,ei).trim());
    temp[index] = new PVector(x,y,z);
    index++;
    data = data.substring(ei+1);
  }
  return temp;
}

public PShape createModel(PVector[] points){
  PShape temp = createShape();
  temp.beginShape(POINTS);
  temp.fill(255);
  temp.stroke(255);
  temp.strokeWeight(0.001f);
  for(int i = 0; i < index-1; i++){
    temp.vertex(points[i].x,points[i].y,points[i].z);
  }
  temp.endShape();
  return temp;
}

public void highres(){
  
}

public void loadAll(){
  models = new PShape[32];
  for(int i = 0; i < 32; i++){
    println("Loading : " + names[i]);
    models[i] = createModel(ParseDataToVector(RemoveInvalidData(loadStrings("data/newscans/"+names[i])[0])));
    models[i].scale(50);
    models[i].rotateX(PI/4);
    index = 0;
  }
}

public void loadnew(){
  models = new PShape[32];
  for(int i = 0; i < 32; i++){
    println("Loading : " + names[i]);
    models[i] = loadQuadStripModel("data/models/m"+names[i]);
    models[i].scale(50.0f);
    models[i].rotateY(-PI/4);
  }
}

public void savePointModel(PShape m, String address){
  String[] file = new String[m.getVertexCount()];
  for(int i = 0; i < m.getVertexCount(); i++){
    PVector p = m.getVertex(i);
    file[i] = str(p.x)+","+str(p.y)+","+str(p.z);
  }
  saveStrings(address,file);
}

public PShape loadPointModel(String address){
  String[] file = loadStrings(address);
  PShape m = createShape();
  m.beginShape(POINTS);
  m.fill(255);
  m.stroke(255);
  m.strokeWeight(0.001f);
  for(int i = 0; i < file.length; i++){
    int fc = file[i].indexOf(',');
    int sc = file[i].indexOf(',',fc+1);
    m.vertex(PApplet.parseFloat(file[i].substring(0,fc)),PApplet.parseFloat(file[i].substring(fc+1,sc)),PApplet.parseFloat(file[i].substring(sc+1,file[i].length())));
  }
  m.endShape();
  
  
  return m;
}

public PVector[] sortArray(PVector[] unsorted){
  boolean sorting = true;
  while(sorting){
    sorting = false;
    for(int i = 0; i < unsorted.length-3; i++){
      if(dist(unsorted[i].x,unsorted[i].y,unsorted[i].z,unsorted[i+2].x,unsorted[i+2].y,unsorted[i+2].z) < dist(unsorted[i+1].x,unsorted[i+1].y,unsorted[i+1].z,unsorted[i+2].x,unsorted[i+2].y,unsorted[i+2].z)){
        PVector c = unsorted[i];
        unsorted[i] = unsorted[i+1];
        unsorted[i+1] = c;
        sorting = true;
      }
    }
  }
  return unsorted;
}

public PShape loadQuadStripModel(String address){
  float dm = 0.4f;
  String[] file = loadStrings(address);
  PVector[][] points = new PVector[rx][ry];
  PShape m = createShape();
  m.beginShape(TRIANGLES);
  m.fill(random(100,255),random(100,255),random(100,255));
  m.noStroke();
  for(int x = 0; x < rx; x++){
    for(int y = 0; y < ry; y++){
      try{
      int i = x + y * rx;
      int fc = file[i].indexOf(',');
      int sc = file[i].indexOf(',',fc+1);
      points[x][y] = new PVector(PApplet.parseFloat(file[i].substring(0,fc)),PApplet.parseFloat(file[i].substring(fc+1,sc)),PApplet.parseFloat(file[i].substring(sc+1,file[i].length())));      
      }catch(Exception e){
        println(e);
      }
    }
  }
  for(int y = 0; y < ry-1; y++){
    for(int x = 0; x < rx-1; x++){
      if(dist(points[x][y].x,points[x][y].z,0,0) <= 2 && dist(points[x+1][y].x,points[x+1][y].z,0,0) <= 2 && dist(points[x][y+1].x,points[x][y+1].z,0,0) <= 2 && dist(points[x+1][y+1].x,points[x+1][y+1].z,0,0) <= 2){
          try{
            if(dist(points[x][y].x,points[x][y].y,points[x][y].z,points[x+1][y].x,points[x+1][y].y,points[x+1][y].z) <= dm && dist(points[x][y].x,points[x][y].y,points[x][y].z,points[x][y+1].x,points[x][y+1].y,points[x][y+1].z) <= dm && dist(points[x][y].x,points[x][y].y,points[x][y].z,points[x+1][y+1].x,points[x+1][y+1].y,points[x+1][y+1].z) <= dm*1.5f){
              m.vertex(points[x][y].x,points[x][y].y,points[x][y].z);
              m.vertex(points[x][y+1].x,points[x][y+1].y,points[x][y+1].z);
              m.vertex(points[x+1][y].x,points[x+1][y].y,points[x+1][y].z);
              m.vertex(points[x+1][y].x,points[x+1][y].y,points[x+1][y].z);
              m.vertex(points[x+1][y+1].x,points[x+1][y+1].y,points[x+1][y+1].z);
              m.vertex(points[x][y+1].x,points[x][y+1].y,points[x][y+1].z);
            }
          }catch(Exception e){
          }
        try{
          if(dist(points[rx-1][y].x,points[rx-1][y].z,0,0) <= 2 && dist(points[0][y].x,points[0][y].z,0,0) <= 2 && dist(points[rx-1][y+1].x,points[rx-1][y+1].z,0,0) <= 2 && dist(points[0][y+1].x,points[0][y+1].z,0,0) <= 2){
              m.vertex(points[rx-1][y].x,points[rx-1][y].y,points[rx-1][y].z);
              m.vertex(points[rx-1][y+1].x,points[rx-1][y+1].y,points[rx-1][y+1].z);
              m.vertex(points[0][y].x,points[0][y].y,points[0][y].z);
              m.vertex(points[0][y].x,points[0][y].y,points[0][y].z);
              m.vertex(points[0][y+1].x,points[0][y+1].y,points[0][y+1].z);
              m.vertex(points[rx-1][y+1].x,points[rx-1][y+1].y,points[rx-1][y+1].z);
          }
        }catch(Exception e){
        }
      }
    }
  }
  m.endShape();
  
  
  return m;  
}
  public void settings() {  fullScreen(P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "sketch_3d_scan_model_ow" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
