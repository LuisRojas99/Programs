package edu.nmsu.cs.webserver;

/*
 * 
 * 
 * CHANGE MADE
 * 
 * 
 * 
 */




/*
 * Web worker: an object of this class executes in its own new thread to receive and respond to a
 * single HTTP request. After the constructor the object executes on its "run" method, and leaves
 * when it is done.
 *
 * One WebWorker object is only responsible for one client connection. This code uses Java threads
 * to parallelize the handling of clients: each WebWorker runs in its own thread. This means that
 * you can essentially just think about what is happening on one client at a time, ignoring the fact
 * that the entirety of the webserver execution might be handling other clients, too.
 *
 * This WebWorker class (i.e., an object of this class) is where all the client interaction is done.
 * The "run()" method is the beginning -- think of it as the "main()" for a client interaction. It
 * does three things in a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it writes out some HTML
 * content for the response content. HTTP requests and responses are just lines of text (in a very
 * particular format).
 * 
 * @author Jon Cook, Ph.D.
 *
 **/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.text.html.HTML.Tag;

public class WebWorker implements Runnable
{

	private Socket socket;
    private String server="Luis's Kawaii Server :3";//name of the server
    private File page;// page/file being served
    
	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s)
	{
		socket = s;
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP request and then returns, which
	 * destroys the thread. This method assumes that whoever created the worker created it with a
	 * valid open socket object.
	 **/
	public void run()
	{
		System.err.println("Handling connection...");
		try
		{
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			readHTTPRequest(is);
			writeHTTPHeader(os, "text/html");
			writeContent(os);
			os.flush();
			socket.close();
		}
		catch (Exception e)
		{
			System.err.println("Output error: " + e);
		    
		}
		System.err.println("Done handling connection.");
		return;
	
	}

	/**
	 * Read the HTTP request header.
	 * I turned from void to String return, 
	 * if the URL is not the same exact as the one I wanted, return string "not found"
	 * if it was the same then return the string return the string.
	 * 
	 * 
	 **/
	private void readHTTPRequest(InputStream is) 
	{
		String line;
		
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		//System.out.println("InputStream is : "+ is.toString()); this was me trying to debug manualy lol.
		
		while (true)
		{
			try
			{
				while (!r.ready()) {
					Thread.sleep(1);
				}
				line = r.readLine();
				System.err.println("Request Line: ("+line+")");
				
				if(line.length()==0) {      // if line doesn't have anything break the whileloop there is no point on going foreward.
					break;
					
				}
				if(line.substring(0, 3).equals("GET")) {        //if the substring of line from index 0 to 3 is equal to "GET" then split the line from the blankspace on... 
		               
					   String[] splitLine = line.split(" ");// after GET we have a space, so make splitline = to the substring from the index of blank space to end.
					   String path = "."+ splitLine[1]; //makes path start   then add the line splitted from the blankspace
					   //
					   System.out.println("This is path :"+path);// debug to know what is the path is taking
		               
		               if (path.equals("./")) {
		                  
		            	  System.out.println("Success!");
		                  path = "./text.html"; 
		               }
		               page = new File(path);
				}//end of if
		    }
				 
				
			
			catch (Exception e)
			{
				System.err.println("Request error: " + e);
				break;//if it has an exception then break the loop
			}//end catch error
		}//end of whileloop
		return;
	}//end readhttp

	/**
	 * Write the HTTP header lines to the client network connection.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 * @param contentType
	 *          is the string MIME content type (e.g. "text/html")
	 **/
	private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
	{
		Date d = new Date();
		DateFormat df = DateFormat.getDateTimeInstance();
		
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		System.out.println("this is page :"+page);
		System.out.println("This page exist :"+page.exists());
		if(page.exists()) {// if the page exists (file is in the servers directory)
			os.write("HTTP/1.1 200 OK\n".getBytes());//status message should be 200
			
		}
		else{
			os.write("HTTP/1.1 404 Not Found\n".getBytes());// else then return not found 404 status message
			
		}
		os.write("Date is : ".getBytes());
		os.write(df.format(d).getBytes());
		os.write("\n".getBytes());
		os.write("Server : Luis's own Server\n".getBytes());
		os.write("Connection: close\n".getBytes());
		os.write("Content-Type: ".getBytes());
		os.write(contentType.getBytes());
		os.write("\n\n".getBytes());
		
		return;
		
		
	}

	/**
	 * cd C:\Users\gorel\Desktop\SchoolWork\Programs\SimpleWebServer\src
	 * 
	 * javac edu/nmsu/cs/webserver/*.java -d ../bin
	 * 
	 * cd C:\Users\gorel\Desktop\SchoolWork\Programs\SimpleWebServer
	 * 
	 * java -cp bin edu.nmsu.cs.webserver.WebServer
	 * 
	 * 
	 * Write the data content to the client network connection. This MUST be done after the HTTP
	 * header has been written out.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 **/
	private void writeContent(OutputStream os) throws Exception
	{
		if(page.exists()) {// if the page exists then continue with the process
			BufferedReader reader = new BufferedReader(new FileReader(page));//makes a reader for the page being served
			String string; //string is the line readed.
			Date d = new Date();
			DateFormat df =DateFormat.getDateTimeInstance();
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			while(reader.readLine()!=null) {//if the line readed is not empty, proceed to do whileloop when it becomes null break the while loop
				
				string=reader.readLine();   // set string to the line readed above.
				
				string= string.replaceAll("<cs371date>",df.format(d).toString());// replacing tags
				string= string.replaceAll("<cs371server>",server);    // replacing tags
			    os.write("<html><head></head>".getBytes());
			    os.write(string.getBytes());
			    //os.write("<body><h2>The server is : <cs371server></h2></body>".getBytes());
			    os.write("</html>".getBytes());
			    
			}//end while loop
		}//end if
		else { //if the page/file does not exist in directory, throw message 404 not found (this is what is seen at the web page, not the status message).
			
			os.write("<html><head></head>".getBytes());
			os.write("<body><h2>Error 404 Page Status: Not Found :(".getBytes());
			return;
		}//end else
		
	}//end write content

} // end class
