import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL11.glVertex2i;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class GLLoader implements Runnable{

	private class colorquad{
        float r;
        float g;
        float b;
	}
	private class point{
		int x;
		int y;
	}
	private class quaddata{
        point bl; //bottom-left vertex
        point br; //bottom-right vertex
        point tr; //top-right vertex
        point tl; //top-left vertex
        quaddata(){
        	bl = new point();
        	br = new point();
        	tr = new point();
        	tl = new point();
        }
	}
	private static final int[] xmove = {0,1,0,-1};
	private static final int[] ymove = {-1,0,1,0};
	
	// The window handle
	private long window;
	private int windowW;
	private int windowH;
	private int xArrayVal =64;
	private int yArrayVal =32;
	public int width = 0, height = 0, algo = 0, mode = 0;
	public boolean fullscreen = false, takeScreenshot = false, staticEnded = false;
	public String format;
	private volatile boolean running = true;
	private quaddata storage[][];
	private quaddata wiggle[][];
	private quaddata jump[][];
	private float alphaChannels[][] = new float[xArrayVal][yArrayVal];
	private colorquad colorChannels[][] = new colorquad[xArrayVal][yArrayVal];
	private colorquad colorChannelsActive[][] = new colorquad[xArrayVal][yArrayVal];
	
	private boolean errorChecking[][] = new boolean[xArrayVal][yArrayVal];

	private int[] AudioData = new int[512];
	
	private point pointMapper[];
	
	private long variableYieldTime, lastTime;
	
	private void activeColors (float r,float g,float b){
		for(int i=0;i<xArrayVal;i++){
			for(int k=0;k<yArrayVal;k++){			
					colorChannelsActive[i][k].r=r;				
					colorChannelsActive[i][k].g=g;		
					colorChannelsActive[i][k].b=b;			
			}
		}
	}

	private void alphaFiller(){
		float alpha;
		Random Random = new Random() ;
		Noise noise = new Noise();
		switch (algo){
		case 0:
			for(int i =0; i<xArrayVal; i++){
				for(int k=0; k<yArrayVal;k++){
					int seed = Random.nextInt(1000);			
					alpha = alphaChannels[i][k]  + noise.noise(i+seed,k+seed)/8;
					if(alpha >= 1.0f || alpha <= 0.0f ){
						alphaChannels[i][k] = 1.0f;
					}
					else{
						alphaChannels[i][k]=alpha;
					}		
				}
			}
			break;
		case 1:
			for(int i =0; i<xArrayVal; i++){
				for(int k=0; k<yArrayVal;k++){
					int seed = Random.nextInt(1000);
					alpha = alphaChannels[i][k]  + noise.noise(i+seed,k+seed)/8;
					if(alpha >= 1.0f || alpha <= 0.0f ){
						alphaChannels[i][k] = 1.0f;
					}
					else{
						alphaChannels[i][k]=alpha;
					}		
				}
			}
			break;
		case 2:
			for(int i =0; i<xArrayVal; i++){
				for(int k=0; k<yArrayVal;k++){
					int seed = Random.nextInt(1000);
					alpha = alphaChannels[i][k]  + noise.noise(i+seed,k+seed)/32;
					if(alpha >= 1.0f || alpha <= 0.0f ){
						alphaChannels[i][k] = 1.0f;
					}
					else{
						alphaChannels[i][k]=alpha;
					}		
				}
			}
			for(int i =0; i<xArrayVal; i++){
				for(int k=0; k<yArrayVal;k++){
					int seed = Random.nextInt(2000);
					alpha = alphaChannels[i][k]  + noise.noise(i+seed,k+seed)/50;
					if(alpha >= 1.0f || alpha <= 0.0f ){
						alphaChannels[i][k] = 1.0f;
					}
					else{
						alphaChannels[i][k]=alpha;
					}		
				}
			}
			break;
		case 3:
			for(int i =0; i<xArrayVal; i++){
				for(int k=0; k<yArrayVal;k++){
					int seed = Random.nextInt(1000);
					alpha = alphaChannels[i][k]  + noise.noise(i+seed,k+seed)/8;
					if(alpha >= 1.0f || alpha <= 0.0f ){
						alphaChannels[i][k] = 1.0f;
					}
					else{
						alphaChannels[i][k]=alpha;
					}		
				}
			}
			break;
		}

		
	}
	
	private void arrayFiller(){
		storage = new quaddata[xArrayVal][yArrayVal];
		wiggle = new quaddata[xArrayVal][yArrayVal];
		jump = new quaddata[xArrayVal][yArrayVal];
		pointMapper = new point[xArrayVal*yArrayVal];
		int SIZE=16; 
		int PADDING_HALF=2;	
		int x=0;
		int y=0;
		int tracker=0;
		for(int i=0;i<xArrayVal;i++){
			x++;
			y=0;
			for(int k=0;k<yArrayVal;k++){
				y++;
				point cur = new point();
				cur.x=i;
				cur.y=k;
				pointMapper[tracker]=cur;
				tracker++;
				storage[i][k] = new quaddata();
				wiggle [i][k] = new quaddata();
				jump [i][k] = new quaddata();
		        storage[i][k].bl.x = SIZE*(x-1) + PADDING_HALF;
		        storage[i][k].bl.y = SIZE*(y-1) + PADDING_HALF;
		       
		        storage[i][k].br.x = SIZE*x - PADDING_HALF;
		        storage[i][k].br.y = SIZE*(y-1) + PADDING_HALF;
		        
		        storage[i][k].tr.x = SIZE*x - PADDING_HALF;
		        storage[i][k].tr.y = SIZE*y - PADDING_HALF;
		        
		        storage[i][k].tl.x = SIZE*(x-1) + PADDING_HALF;
		        storage[i][k].tl.y = SIZE*y - PADDING_HALF;
		      
		        wiggle[i][k].bl.x = SIZE*(x-1) + PADDING_HALF;
		        wiggle[i][k].bl.y = SIZE*(y-1) + PADDING_HALF;
		       
		        wiggle[i][k].br.x = SIZE*x - PADDING_HALF;
		        wiggle[i][k].br.y = SIZE*(y-1) + PADDING_HALF;
		        
		        wiggle[i][k].tr.x = SIZE*x - PADDING_HALF;
		        wiggle[i][k].tr.y = SIZE*y - PADDING_HALF;
		        
		        wiggle[i][k].tl.x = SIZE*(x-1) + PADDING_HALF;
		        wiggle[i][k].tl.y = SIZE*y - PADDING_HALF;
		        
		        jump[i][k].bl.x = SIZE*(x-1) + PADDING_HALF;
		        jump[i][k].bl.y = SIZE*(y-1) + PADDING_HALF;
		       
		        jump[i][k].br.x = SIZE*x - PADDING_HALF;
		        jump[i][k].br.y = SIZE*(y-1) + PADDING_HALF;
		        
		        jump[i][k].tr.x = SIZE*x - PADDING_HALF;
		        jump[i][k].tr.y = SIZE*y - PADDING_HALF;
		        
		        jump[i][k].tl.x = SIZE*(x-1) + PADDING_HALF;
		        jump[i][k].tl.y = SIZE*y - PADDING_HALF;
			}
		}
	}
	
	private void arrayFillerJump(int i, int k, int jumper){
		int SIZE=16*jumper; 
		int PADDING_HALF=2;	
		jump[i][k].bl.x = SIZE*(i-1) + PADDING_HALF + jumper;
		jump[i][k].bl.y = SIZE*(k-1) + PADDING_HALF + jumper;
       
		jump[i][k].br.x = SIZE*i - PADDING_HALF + jumper;
		jump[i][k].br.y = SIZE*(k-1) + PADDING_HALF + jumper;
        
		jump[i][k].tr.x = SIZE*i - PADDING_HALF + jumper;
		jump[i][k].tr.y = SIZE*k - PADDING_HALF + jumper;
        
		jump[i][k].tl.x = SIZE*(i-1) + PADDING_HALF + jumper;
		jump[i][k].tl.y = SIZE*k - PADDING_HALF + jumper;		
		
	}

	private void arrayFillerWiggle(){
		Random Random = new Random() ;
		int SIZE=16; 
		int PADDING_HALF=2;	
		int x=0;
		int y=0;
		for(int i=0;i<xArrayVal;i++){
			x++;
			y=0;
			for(int k=0;k<yArrayVal;k++){
				y++;
				wiggle[i][k].bl.x = SIZE*(x-1) + PADDING_HALF + Random.nextInt(4);
				wiggle[i][k].bl.y = SIZE*(y-1) + PADDING_HALF + Random.nextInt(4);
		       
				wiggle[i][k].br.x = SIZE*x - PADDING_HALF + Random.nextInt(4);
				wiggle[i][k].br.y = SIZE*(y-1) + PADDING_HALF + Random.nextInt(4);
		        
				wiggle[i][k].tr.x = SIZE*x - PADDING_HALF + Random.nextInt(4);
				wiggle[i][k].tr.y = SIZE*y - PADDING_HALF + Random.nextInt(4);
		        
				wiggle[i][k].tl.x = SIZE*(x-1) + PADDING_HALF + Random.nextInt(4);
				wiggle[i][k].tl.y = SIZE*y - PADDING_HALF + Random.nextInt(4);
		      
			}
		}
	}
	
	private void arrayFillerWiggleReset(){
		int SIZE=16; 
		int PADDING_HALF=2;	
		int x=0;
		int y=0;
		for(int i=0;i<xArrayVal;i++){
			x++;
			y=0;
			for(int k=0;k<yArrayVal;k++){
				y++;
				wiggle[i][k].bl.x = SIZE*(x-1) + PADDING_HALF;
				wiggle[i][k].bl.y = SIZE*(y-1) + PADDING_HALF;
		       
				wiggle[i][k].br.x = SIZE*x - PADDING_HALF;
				wiggle[i][k].br.y = SIZE*(y-1) + PADDING_HALF;
		        
				wiggle[i][k].tr.x = SIZE*x - PADDING_HALF;
				wiggle[i][k].tr.y = SIZE*y - PADDING_HALF;
		        
				wiggle[i][k].tl.x = SIZE*(x-1) + PADDING_HALF;
				wiggle[i][k].tl.y = SIZE*y - PADDING_HALF;
		      
			}
		}
	}
	
	private void arrayJumpReset(){
		int SIZE=16; 
		int PADDING_HALF=2;	
		int x=0;
		int y=0;
		for(int i=0;i<xArrayVal;i++){
			x++;
			y=0;
			for(int k=0;k<yArrayVal;k++){
				y++;
				jump[i][k].bl.x = (SIZE*(x-1) + PADDING_HALF + jump[i][k].bl.x)/2;
				jump[i][k].bl.y = (SIZE*(y-1) + PADDING_HALF + jump[i][k].bl.y)/2;
		       
				jump[i][k].br.x = (SIZE*x - PADDING_HALF + jump[i][k].br.x)/2;
				jump[i][k].br.y = (SIZE*(y-1) + PADDING_HALF + jump[i][k].br.y)/2;
		        
				jump[i][k].tr.x = (SIZE*x - PADDING_HALF + jump[i][k].tr.x)/2;
				jump[i][k].tr.y = (SIZE*y - PADDING_HALF + jump[i][k].tr.y)/2;
		        
				jump[i][k].tl.x = (SIZE*(x-1) + PADDING_HALF + jump[i][k].tl.x)/2;
				jump[i][k].tl.y =(SIZE*y - PADDING_HALF + jump[i][k].tl.y)/2;
		      
			}
		}
	}
	
	private void audioProc(){
		if(mode==1){
				double[] tmp;
					tmp = AudioModule.staticData;
				for(int i=0; i<512 ; i++){
					if(AudioModule.staticData[i] <= 0.25d){
						AudioData[i] =0;	
					}
					else{
						if(AudioModule.staticData[i] > 0.25d){
							AudioData[i] =1;	
						}
						if(AudioModule.staticData[i] >= 0.4d){
							AudioData[i] =2;	
						}
						if(AudioModule.staticData[i] >= 0.6d){
							AudioData[i] =3;	
						}	
	
						if(AudioModule.staticData[i] >= 0.9d){
							AudioData[i] =4;	
						}
						
						
					}
			//	AudioData[i] = (float) AudioModule.relData[i];
			}	
		}
		else
		{
			switch (algo){
			case 0:
				for(int i=0; i<512 ; i++){
						if(AudioModule.relData[i] <= 0.25d){
							AudioData[i] =0;	
						}
						else{
							if(AudioModule.relData[i] > 0.25d){
								AudioData[i] =1;	
							}
							if(AudioModule.relData[i] >= 0.4d){
								AudioData[i] =2;	
							}
							if(AudioModule.relData[i] >= 0.6d){
								AudioData[i] =3;	
							}	
		
							if(AudioModule.relData[i] >= 0.9d){
								AudioData[i] =4;	
							}
							
							
						}
				//	AudioData[i] = (float) AudioModule.relData[i];
				}	
				break;
			case 1:
				for(int i=0; i<512 ; i++){
						if(AudioModule.relData[i] <= 0.25d){
							AudioData[i] =0;	
						}
						else{
							if(AudioModule.relData[i] > 0.25d){
								AudioData[i] =1;	
							}
							if(AudioModule.relData[i] >= 0.4d){
								AudioData[i] =2;	
							}
							if(AudioModule.relData[i] >= 0.6d){
								AudioData[i] =3;	
							}	
		
							if(AudioModule.relData[i] >= 0.9d){
								AudioData[i] =4;	
							}
							
							
						}
				//	AudioData[i] = (float) AudioModule.relData[i];
				}	
				break;
			case 2:
				for(int i=0; i<512 ; i++){
						if(AudioModule.relData[i] <= 0.3d){
							AudioData[i] =0;	
						}
						else{
							if(AudioModule.relData[i] > 0.3d){
								AudioData[i] =1;	
							}
							if(AudioModule.relData[i] >= 0.4d){
								AudioData[i] =2;	
							}
							if(AudioModule.relData[i] >= 0.8d){
								AudioData[i] =3;	
							}	
							
							
						}
				//	AudioData[i] = (float) AudioModule.relData[i];
				}	
				break;
			case 3:
				for(int i=0; i<512 ; i++){
						if(AudioModule.relData[i] <= 0.3d){
							AudioData[i] =0;	
						}
						else{
							if(AudioModule.relData[i] > 0.3d){
								AudioData[i] =1;	
							}
							if(AudioModule.relData[i] >= 0.4d){
								AudioData[i] =2;	
							}
							if(AudioModule.relData[i] >= 0.8d){
								AudioData[i] =3;	
							}	
							
							
						}
				//	AudioData[i] = (float) AudioModule.relData[i];
				}	
				break;
			}
			
		}
		
	}
	
	
	private void boolreset(){
		for(int i=0;i<xArrayVal;i++){		
			for(int k=0;k<yArrayVal;k++){		
		      errorChecking[i][k]=false;
			}
		}
	}
	
	private void colorCrawler (int x, int y, int Magnitude, colorquad Color){
		if(Magnitude <=0){
			return;
		}
		//System.out.printf("%d ", Magnitude);
		int ytemp;
		int xtemp;
		for(int i=0; i<4; i++)
		{	
			xtemp = x+xmove[i];
			ytemp = y+ymove[i];
			if (!((xtemp <0) || (xtemp>(xArrayVal-1)) || (ytemp <0) || (ytemp>(yArrayVal-1))))
			{		
				colorChannelsActive[x][y].r = (colorChannelsActive[x][y].r + Color.r)/2;
				colorChannelsActive[x][y].g = (colorChannelsActive[x][y].g + Color.g)/2;
				colorChannelsActive[x][y].b = (colorChannelsActive[x][y].b + Color.b)/2;
				Magnitude--;
				colorCrawler(xtemp, ytemp, Magnitude, Color);
			}
		}
		return;
	}
	
	private void colorCrawler2 (int x, int y, int Magnitude, colorquad Color){
		if(Magnitude <=0 || errorChecking[x][y]==true){
			return;
		}
		//System.out.printf("%d ", Magnitude);
		int ytemp;
		int xtemp;
		if(colorChannelsActive[x][y].r <= 0.05f && colorChannelsActive[x][y].g <= 0.05f && colorChannelsActive[x][y].b <= 0.05f){
			colorChannelsActive[x][y].r = Color.r;
			colorChannelsActive[x][y].g = Color.g;
			colorChannelsActive[x][y].b = Color.b;
		}
		else{
			colorChannelsActive[x][y].r = (colorChannelsActive[x][y].r + Color.r)/2;
			colorChannelsActive[x][y].g = (colorChannelsActive[x][y].g + Color.g)/2;
			colorChannelsActive[x][y].b = (colorChannelsActive[x][y].b + Color.b)/2;		
		}
		errorChecking[x][y]=true;
		Magnitude--;
		for(int i=0; i<4; i++)
		{	
			xtemp = x+xmove[i];
			ytemp = y+ymove[i];
			
			if (!((xtemp <0) || (xtemp>(xArrayVal-1)) || (ytemp <0) || (ytemp>(yArrayVal-1))))
			{						
				colorCrawler2(xtemp, ytemp, Magnitude, Color);
			}
		}
		return;
	}
	
	private void colorCrawler3 (int x, int y, int Magnitude, colorquad Color){
		if(Magnitude <=0 || errorChecking[x][y]==true){
			return;
		}
		//System.out.printf("%d ", Magnitude);
		int ytemp;
		int xtemp;
		if(staticEnded==false){
			arrayFillerJump(x,y,Magnitude);
		}	
		colorChannelsActive[x][y].r = (colorChannelsActive[x][y].r + Color.r)/2;
		colorChannelsActive[x][y].g = (colorChannelsActive[x][y].g + Color.g)/2;
		colorChannelsActive[x][y].b = (colorChannelsActive[x][y].b + Color.b)/2;		
		errorChecking[x][y]=true;
		Magnitude--;
		for(int i=0; i<4; i++)
		{	
			xtemp = x+xmove[i];
			ytemp = y+ymove[i];
			
			if (!((xtemp <0) || (xtemp>(xArrayVal-1)) || (ytemp <0) || (ytemp>(yArrayVal-1))))
			{						
				colorCrawler3(xtemp, ytemp, Magnitude, Color);
			}
		}
		return;
	}
	
	public void destroyWindow(){
		try {
			Robot robot = new Robot();

			// Simulate a mouse click
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);

			robot.delay(50);

			// Simulate a key press
			robot.keyPress(KeyEvent.VK_ALT);
			robot.keyPress(KeyEvent.VK_TAB);
			robot.delay(20);
			robot.keyRelease(KeyEvent.VK_ALT);
			robot.keyRelease(KeyEvent.VK_TAB);

			robot.delay(50);

			robot.keyPress(KeyEvent.VK_ESCAPE);
			robot.keyRelease(KeyEvent.VK_ESCAPE);

		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	private void draw() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
		//setColor(material);	
		glDisable(GL_LIGHTING);
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_LIGHTING);
		glDisable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		double[] tmp;
		if (mode == 1) {
			tmp = AudioModule.staticData;
		}
		else {
			tmp = AudioModule.relData;
		}
		int pointerA;
		int rand;
		switch (algo){
			case 0:
			glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			for(int i=0;i<xArrayVal;i++){
				for(int k=0;k<yArrayVal;k++){
					float rn = (float) tmp[(i + k*64) % 512];
					float rn1 = (float) tmp[(i + k*64) % 512];
					float rn2 = (float) tmp[(i + k*64) % 512];
					glBegin(GL_QUADS);
					glColor3f(rn,rn1,rn2);
			        glVertex2i(storage[i][k].bl.x,storage[i][k].bl.y); //bottom-left vertex
			      //  glColor3f(rn,rn1,rn2);
			        glVertex2i(storage[i][k].br.x, storage[i][k].br.y); //bottom-right vertex
			      //  glColor3f(rn,rn1,rn2);
			        glVertex2i(storage[i][k].tr.x, storage[i][k].tr.y); //top-right vertex
			        //glColor3f(rn,rn1,rn2);
			        glVertex2i(storage[i][k].tl.x, storage[i][k].tl.y); //top-left vertex
			        glEnd();
				}
			}
			break;
			case 1:
			glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			for(int a=0;a<512;a++){
				if(AudioData[a]!=0){
					rand = (int) Math.floor(Math.random() * 2) -1;
					pointerA = a*4 + rand;
					if(pointerA>=xArrayVal*yArrayVal){
						pointerA=xArrayVal*yArrayVal-1;
					}
					if(pointerA<=0){
						pointerA=0;
					}
					//System.out.printf("%d %d %d ",pointerA,pointMapper[pointerA].x, pointMapper[pointerA].y);
					colorCrawler(pointMapper[pointerA].x,pointMapper[pointerA].y,AudioData[a],colorChannels[pointMapper[pointerA].x][pointMapper[pointerA].y]);
				}
			}
			for(int i=0;i<xArrayVal;i++){
				for(int k=0;k<yArrayVal;k++){
					glBegin(GL_QUADS);
					glColor4f(colorChannelsActive[i][k].r,colorChannelsActive[i][k].g,colorChannelsActive[i][k].b,alphaChannels[i][k]);
			        glVertex2i(storage[i][k].bl.x,storage[i][k].bl.y); //bottom-left vertex
			        glVertex2i(storage[i][k].br.x, storage[i][k].br.y); //bottom-right vertex
			        glVertex2i(storage[i][k].tr.x, storage[i][k].tr.y); //top-right vertex
			        glVertex2i(storage[i][k].tl.x, storage[i][k].tl.y); //top-left vertex
			        glEnd();
				}
			}
			break;
			case 2:
			glClearColor(0.0f, 0.5f, 0.5f, 1.0f);
			for(int a=0;a<512;a++){
				if(AudioData[a]!=0){
					rand = (int) Math.floor(Math.random() * 2) -1;
					pointerA = a*4 + rand;
					if(pointerA>=xArrayVal*yArrayVal){
						pointerA=xArrayVal*yArrayVal-1;
					}
					if(pointerA<=0){
						pointerA=0;
					}
					//System.out.printf("%d %d %d ",pointerA,pointMapper[pointerA].x, pointMapper[pointerA].y);
					colorCrawler2(pointMapper[pointerA].x,pointMapper[pointerA].y,AudioData[a],colorChannels[pointMapper[pointerA].x][pointMapper[pointerA].y]);
					boolreset();
				}
			}		
			for(int i=0;i<xArrayVal;i++){
				for(int k=0;k<yArrayVal;k++){
					glBegin(GL_QUADS);
					glColor4f(colorChannelsActive[i][k].r,colorChannelsActive[i][k].g,colorChannelsActive[i][k].b,alphaChannels[i][k]);
			        glVertex2i(storage[i][k].bl.x,storage[i][k].bl.y); //bottom-left vertex
			        glVertex2i(storage[i][k].br.x, storage[i][k].br.y); //bottom-right vertex
			        glVertex2i(storage[i][k].tr.x, storage[i][k].tr.y); //top-right vertex
			        glVertex2i(storage[i][k].tl.x, storage[i][k].tl.y); //top-left vertex
			        glEnd();
				}
			}
			break;
			case 3:
			glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
			if(mode==1){
				activeColors(1.0f,1.0f,1.0f);
			}
			for(int a=0;a<512;a++){
				if(AudioData[a]!=0){
					rand = (int) Math.floor(Math.random() * 2) -1;
					pointerA = a*4 + rand;
					if(pointerA>=xArrayVal*yArrayVal){
						pointerA=xArrayVal*yArrayVal-1;
					}
					if(pointerA<=0){
						pointerA=0;
					}
					//System.out.printf("%d %d %d ",pointerA,pointMapper[pointerA].x, pointMapper[pointerA].y);
					colorCrawler3(pointMapper[pointerA].x,pointMapper[pointerA].y,AudioData[a],colorChannels[pointMapper[pointerA].x][pointMapper[pointerA].y]);
					boolreset();			
				}
			}		
			for(int i=0;i<xArrayVal;i++){
				for(int k=0;k<yArrayVal;k++){
					glBegin(GL_QUADS);
					if(mode!=1){
						glColor4f(colorChannelsActive[i][k].r,colorChannelsActive[i][k].g,colorChannelsActive[i][k].b,alphaChannels[i][k]);
						glVertex2i((storage[i][k].bl.x+wiggle[i][k].bl.x+jump[i][k].bl.x)/3,(storage[i][k].bl.y+wiggle[i][k].bl.y+jump[i][k].bl.y)/3); //bottom-left vertex
				        glVertex2i((storage[i][k].br.x+wiggle[i][k].br.x+jump[i][k].br.x)/3,(storage[i][k].br.y+wiggle[i][k].br.y+jump[i][k].br.y)/3); //bottom-right vertex
				        glVertex2i((storage[i][k].tr.x+wiggle[i][k].tr.x+jump[i][k].tr.x)/3,(storage[i][k].tr.y+wiggle[i][k].tr.y+jump[i][k].tr.y)/3); //top-right vertex
				        glVertex2i((storage[i][k].tl.x+wiggle[i][k].tl.x+jump[i][k].tl.x)/3,(storage[i][k].tl.y+wiggle[i][k].tl.y+jump[i][k].tl.y)/3); //top-left vertex
				        glEnd();
					}
					else{
						glColor4f(colorChannelsActive[i][k].r,colorChannelsActive[i][k].g,colorChannelsActive[i][k].b,alphaChannels[i][k]);
				        glVertex2i(storage[i][k].bl.x,storage[i][k].bl.y); //bottom-left vertex
				        glVertex2i(storage[i][k].br.x, storage[i][k].br.y); //bottom-right vertex
				        glVertex2i(storage[i][k].tr.x, storage[i][k].tr.y); //top-right vertex
				        glVertex2i(storage[i][k].tl.x, storage[i][k].tl.y); //top-left vertex
				        glEnd();
					}
				}
			}
			if(staticEnded==false){
				arrayJumpReset();
			}
			break;
			
			
		}

		glEnable(GL_LIGHTING);
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		glfwSwapBuffers(window);
		glfwPollEvents();	
	}
	
	private void fadeout(){
		switch (algo){
		case 0:
			for(int i=0;i<xArrayVal;i++){
				for(int k=0;k<yArrayVal;k++){
					colorChannelsActive[i][k].r = colorChannelsActive[i][k].r - 0.1f;
					if(colorChannelsActive[i][k].r<0.0f){
						colorChannelsActive[i][k].r=0.0f;
					}
					colorChannelsActive[i][k].g = colorChannelsActive[i][k].g - 0.1f;
					if(colorChannelsActive[i][k].g<0.0f){
						colorChannelsActive[i][k].g=0.0f;
					}
					colorChannelsActive[i][k].b = colorChannelsActive[i][k].b - 0.1f;
					if(colorChannelsActive[i][k].b<0.0f){
						colorChannelsActive[i][k].b=0.0f;
					}
				}
			}
			break;
		case 1:
			for(int i=0;i<xArrayVal;i++){
				for(int k=0;k<yArrayVal;k++){
					colorChannelsActive[i][k].r = colorChannelsActive[i][k].r - 0.1f;
					if(colorChannelsActive[i][k].r<0.0f){
						colorChannelsActive[i][k].r=0.0f;
					}
					colorChannelsActive[i][k].g = colorChannelsActive[i][k].g - 0.1f;
					if(colorChannelsActive[i][k].g<0.0f){
						colorChannelsActive[i][k].g=0.0f;
					}
					colorChannelsActive[i][k].b = colorChannelsActive[i][k].b - 0.1f;
					if(colorChannelsActive[i][k].b<0.0f){
						colorChannelsActive[i][k].b=0.0f;
					}
				}
			}
			break;
		case 2:
			for(int i=0;i<xArrayVal;i++){
				for(int k=0;k<yArrayVal;k++){
					colorChannelsActive[i][k].r = colorChannelsActive[i][k].r - 0.05f;
					if(colorChannelsActive[i][k].r<0.0f){
						colorChannelsActive[i][k].r=0.0f;
					}
					colorChannelsActive[i][k].g = colorChannelsActive[i][k].g - 0.05f;
					if(colorChannelsActive[i][k].g<0.0f){
						colorChannelsActive[i][k].g=0.0f;
					}
					colorChannelsActive[i][k].b = colorChannelsActive[i][k].b - 0.05f;
					if(colorChannelsActive[i][k].b<0.0f){
						colorChannelsActive[i][k].b=0.0f;
					}
				}
			}
			break;	
		case 3:
			if(staticEnded==false){
				for(int i=0;i<xArrayVal;i++){
					for(int k=0;k<yArrayVal;k++){
						colorChannelsActive[i][k].r = colorChannelsActive[i][k].r + 0.05f;
						if(colorChannelsActive[i][k].r>1.0f){
							colorChannelsActive[i][k].r=1.0f;
						}
						colorChannelsActive[i][k].g = colorChannelsActive[i][k].g + 0.05f;
						if(colorChannelsActive[i][k].g>1.0f){
							colorChannelsActive[i][k].g=1.0f;
						}
						colorChannelsActive[i][k].b = colorChannelsActive[i][k].b + 0.05f;
						if(colorChannelsActive[i][k].b>1.0f){
							colorChannelsActive[i][k].b=1.0f;
						}
					}
				}	
			}
			break;
		}
	}
	
	private void init(int WIDTH, int HEIGHT, boolean FULLSCREEN) {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure our window
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
		// Get the resolution of the primary monitor
		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		// Create the window
		if(FULLSCREEN==false){
		window = glfwCreateWindow(WIDTH, HEIGHT, "Visualizer Output", NULL, NULL);
		}
		else{
			WIDTH= vidmode.width();
			HEIGHT= vidmode.height();
		window = glfwCreateWindow(WIDTH, HEIGHT, "Visualizer Output", glfwGetPrimaryMonitor(), NULL);
		}
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
		});


		// Center our window
		glfwSetWindowPos(
			window,
			(vidmode.width() - WIDTH) / 2,
			(vidmode.height() - HEIGHT) / 2
		);
		windowW=vidmode.width();
		windowH=vidmode.height();
		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}
	
	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, xArrayVal*10, 0, xArrayVal*10*windowH/windowW, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		//glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );   wireframemode
		glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
		// the window or has pressed the ESCAPE key.
		int frame =0;
		int ticktock=0;
		boolean trigger = false;
		while ( !glfwWindowShouldClose(window) ) {
			audioProc();
			draw();	
			frame++;
			if(frame%2==0 && mode !=1){
				alphaFiller();
			}
			if(frame%3==0){
				if(algo!=0 && mode!=1){
					fadeout();
				}
			}
			if(algo==3 && mode != 1){
				if(frame % 15 == 0){
					trigger =true;
					arrayFillerWiggle();
				}
				if(trigger == true){
					ticktock++;
					if(ticktock>=4){
						ticktock=0;
						trigger =false;
						arrayFillerWiggleReset();
					}
				}
			}
			if(frame==30){
				frame=0;
			}
			sync(30);
			if (takeScreenshot) {
				saveImage();
			}
		}
	}
	
	public void run() {
		running = true;
		runLoader(width, height, fullscreen, algo);
	}
	
	public void runLoader(int width, int height, boolean fullscreen, int algoC) {
		System.out.println("Audio Visualizer Gen0.5 V: " + Version.getVersion() + " Proto");
		arrayFiller();
		algo = algoC;
		arrayFiller();
		vertexArrayFillerInit();
		try {
			init(width, height, fullscreen);
			loop();
			// Free the window callbacks and destroy the window
			glfwFreeCallbacks(window);
			glfwDestroyWindow(window);
		} finally {
			// Terminate GLFW and free the error callback
			glfwTerminate();
			glfwSetErrorCallback(null).free();
		}
	}
	
	private void saveImage() {
		glReadBuffer(GL_FRONT);
		int bpp = 4;
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
		GL11.glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		String dir = System.getProperty("user.home");
		dir += "\\Pictures\\SoundVisualizer";
		//System.out.printf(dir);
		File directory = new File(dir);
		
		if (!directory.exists()) {
			try {
				directory.mkdir();
			}
			catch (SecurityException se) {
				se.printStackTrace();
			}
		}

		dir += "\\ScreenCapture-" + System.currentTimeMillis() + "." + format;
		File file = new File(dir);
		format = format.toUpperCase();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x=0; x<width; x++) {
			for (int y=0; y<height; y++) {
				int i = (x + (width * y)) * bpp;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i+1) & 0xFF;
				int b = buffer.get(i+2) & 0xFF;
				image.setRGB(x, height - (y+1), (0xFF << 24) | (r << 16) | (g << 8) | b);
			}
		}

		try {
			ImageIO.write(image, format, file);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		takeScreenshot = false;
	}
	
	
    /**
     * An accurate sync method that adapts automatically
     * to the system it runs on to provide reliable results.
     * 
     * @param fps The desired frame rate, in frames per second
     * @author kappa (On the LWJGL Forums)
     */
    
    private void sync(int fps) {
        if (fps <= 0) return;
          
        long sleepTime = 1000000000 / fps; // nanoseconds to sleep this frame
        // yieldTime + remainder micro & nano seconds if smaller than sleepTime
        long yieldTime = Math.min(sleepTime, variableYieldTime + sleepTime % (1000*1000));
        long overSleep = 0; // time the sync goes over by
          
        try {
            while (true) {
                long t = System.nanoTime() - lastTime;
                  
                if (t < sleepTime - yieldTime) {
                    Thread.sleep(1);
                }else if (t < sleepTime) {
                    // burn the last few CPU cycles to ensure accuracy
                    Thread.yield();
                }else {
                    overSleep = t - sleepTime;
                    break; // exit while loop
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            lastTime = System.nanoTime() - Math.min(overSleep, sleepTime);
             
            // auto tune the time sync should yield
            if (overSleep > variableYieldTime) {
                // increase by 200 microseconds (1/5 a ms)
                variableYieldTime = Math.min(variableYieldTime + 200*1000, sleepTime);
            }
            else if (overSleep < variableYieldTime - 200*1000) {
                // decrease by 2 microseconds
                variableYieldTime = Math.max(variableYieldTime - 2*1000, 0);
            }
        }
    }
    
    public void terminate() {
		running = false;
	}

	private void vertexArrayFillerInit(){
		float theta;
		switch (algo){
		case 0:
			for(int i =0; i<xArrayVal; i++){
				for(int k=0; k<yArrayVal;k++){
					alphaChannels[i][k]=1.0f;
					colorquad colors = new colorquad();
					colorquad colorsActive = new colorquad();
					theta = (float) i * k;
				    while (theta < 0){
				    	theta += 360;
				    }	        			 
				    while (theta >= 360){
				    	theta -= 360;
				    }			   	 
				    if (theta < 120) {
				    	colors.g = theta / 120;
				        colors.r = 1 - colors.g;
				        colors.b = 0;
				    } else if (theta < 240) {    
				    	colors.b = (theta - 120) / 120;
				        colors.g = 1 - colors.b;
				        colors.r = 0;
				    } else {
				    	colors.r = (theta - 240) / 120;
				    	colors.b = 1 - colors.r;
				        colors.g = 0;
				    }
				    colorsActive.r = 0.0f;
			    	colorsActive.b = 0.0f;
			        colorsActive.g = 0.0f;
					colorChannels[i][k]=colors;
					colorChannelsActive[i][k]=colorsActive;
				}
			}
			break;
			
		case 1:
			for(int i =0; i<xArrayVal; i++){
				for(int k=0; k<yArrayVal;k++){
					alphaChannels[i][k]=1.0f;
					colorquad colors = new colorquad();
					colorquad colorsActive = new colorquad();
					theta = (float) i * k;
				    while (theta < 0){
				    	theta += 360;
				    }	        			 
				    while (theta >= 360){
				    	theta -= 360;
				    }			   	 
				    if (theta < 120) {
				    	colors.g = theta / 120;
				        colors.r = 1 - colors.g;
				        colors.b = 0;
				    } else if (theta < 240) {    
				    	colors.b = (theta - 120) / 120;
				        colors.g = 1 - colors.b;
				        colors.r = 0;
				    } else {
				    	colors.r = (theta - 240) / 120;
				    	colors.b = 1 - colors.r;
				        colors.g = 0;
				    }
				    colorsActive.r = 0.0f;
			    	colorsActive.b = 0.0f;
			        colorsActive.g = 0.0f;
					colorChannels[i][k]=colors;
					colorChannelsActive[i][k]=colorsActive;
				}
			}
			break;
		
		
		case 2:
			int numRed = 256;
			int numGreen = 256;
			int numBlue = 256;
			int index=0;
			float palette[][] = new float[1280][3];
			for (int grn=0; grn<numGreen; grn++)
			{
			palette[index][0]=1.0f;
			palette[index][1]=grn/255.0f;
			palette[index][2]=0.0f;
			index=index+1;
			}
			index=1*256;
			for (int red=0; red<numRed; red++)
			{
			palette[index][0]=1.0f-red/255.0f;
			palette[index][1]=1.0f;
			palette[index][2]=0.0f;
			index=index+1;
			}
			index=2*256;
			for (int blu=0; blu<numBlue; blu++)
			{
			palette[index][0]=0.0f;
			palette[index][1]=1.0f;
			palette[index][2]=blu/255.0f;
			index=index+1;
			}
			index=3*256;
			for (int grn=0; grn<numGreen; grn++)
			{
			palette[index][0]=0.0f;
			palette[index][1]=1.0f-grn/255.0f;
			palette[index][2]=1.0f;
			index=index+1;
			}
			index=4*256;
			for (int red=0; red<numRed; red++)
			{
			palette[index][0]=red/255.0f;
			palette[index][1]=0.0f;
			palette[index][2]=1.0f;
			index=index+1;
			}
			int curIndex=0;
			float storeIndex=0;
			int counter=0;
			for(int i =0; i<xArrayVal; i++){
				for(int k=0; k<yArrayVal;k++){
					counter++;
					alphaChannels[i][k]=1.0f;
					colorquad colors = new colorquad();
					colorquad colorsActive = new colorquad();
					curIndex = ( (counter) * (1079) / (xArrayVal*yArrayVal));			
					storeIndex = palette[curIndex][0];
					//System.out.printf("%d ", curIndex);
					colors.r = (float) ( ((storeIndex - 0) * 1280) / (xArrayVal*yArrayVal - 0) + 0 );
					//System.out.printf("%f ", storeIndex);
					storeIndex = palette[curIndex][1];
					colors.g = (float) ( ((storeIndex - 0) * 1280) / (xArrayVal*yArrayVal - 0) + 0 );
					//System.out.printf("%f ", storeIndex);
					storeIndex = palette[curIndex][2];
					colors.b = (float) ( ((storeIndex - 0) * 1280) / (xArrayVal*yArrayVal - 0) + 0 );
					//System.out.printf("%f \n", storeIndex);
				    colorsActive.r = 0.0f;
			    	colorsActive.b = 0.5f;
			        colorsActive.g = 0.5f;
					colorChannels[i][k]=colors;
					colorChannelsActive[i][k]=colorsActive;
				}
			}
			break;	
		
		case 3:
			int counterC=0;
			float thetay;
			for(int i =0; i<xArrayVal; i++){
				for(int k=0; k<yArrayVal;k++){
					alphaChannels[i][k]=1.0f;
					colorquad colors = new colorquad();
					colorquad colorsActive = new colorquad();
					theta = (float) counterC;
					thetay = (float) k * 10;
					counterC++;
				    while (theta < 0){
				    	theta += 360;
				    }	        			 
				    while (theta >= 360){
				    	theta -= 360;
				    }			   	 
				    if (theta < 120) {
				    	colors.g = theta / 120;
				        colors.r = 1 - colors.g;
				        colors.b = 0;
				    } else if (theta < 240) {    
				    	colors.b = (theta - 120) / 120;
				        colors.g = 1 - colors.b;
				        colors.r = 0;
				    } else {
				    	colors.r = (theta - 240) / 120;
				    	colors.b = 1 - colors.r;
				        colors.g = 0;
				    }
				    while (thetay < 0){
				    	theta += 360;
				    }	        			 
				    while (thetay >= 360){
				    	theta -= 360;
				    }			   	 
				    if (thetay < 120) {
				    	colors.g = (float) (thetay / 120 + colors.g /2.0);
				        colors.r = (float) (1 - colors.g + colors.r /2.0);
				        colors.b = (float) (0 + colors.b /2.0);
				    } else if (thetay < 240) {    
				    	colors.b = (float) ((thetay - 120) / 120 + colors.b /2.0);
				        colors.g = (float) (1 - colors.b + colors.g /2.0);
				        colors.r = (float) (0 + colors.r /2.0);
				    } else {
				    	colors.r = (float) ((thetay - 240) / 120 + colors.r /2.0);
				    	colors.b = (float) (1 - colors.r + colors.b /2.0);
				        colors.g = (float) (0 + colors.g /2.0);
				    }
				    colorsActive.r = 0.0f;
			    	colorsActive.b = 0.0f;
			        colorsActive.g = 0.0f;
					colorChannels[i][k]=colors;
					colorChannelsActive[i][k]=colorsActive;
				}
			}
			break;
			
		}
	}

    public long windowReturn(){
		return window;
	}
	
	/*public static void main(String[] args) {
		new GLLoader().run(1280,720,true);
	}*/
	
}