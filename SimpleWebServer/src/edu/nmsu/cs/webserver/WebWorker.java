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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.text.html.HTML.Tag;

public class WebWorker implements Runnable
{

	private Socket socket;

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
			
			String s =readHTTPRequest(is);                    //Here I read the request
			if(s!="Not Found") {                              // If the request does not send back "not found"
				writeHTTPHeader(os, "text/html");             // then procede as usual
				writeContent(os);
				os.flush();
			}
			else {                                           // if it was not found then pass the "not found" string as content type
			writeHTTPHeader(os, s);                          // to write http header
			writeContent2(os);                               // and go to write content 2 where I have the text of "Error 404, not found"
			os.flush();
			}
			    
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
	private String readHTTPRequest(InputStream is) 
	{
		String line="";
		String ret;
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		//System.out.println("InputStream is : "+ is.toString());
		
		while (true)
		{
			try
			{
				while (!r.ready())
					Thread.sleep(1);
				line = r.readLine();
				if(line.contains("GET")&& !line.matches(   "GET /res/acc/test.html HTTP/1.1")) {
					System.out.println("404 not found  ***********\n");
					System.out.println("Line was : "+line);
					ret = "Not Found";
					return ret;
				}
				else if(line.contains("GET")&& line.matches(   "GET /res/acc/test.html HTTP/1.1")) {
					ret = line;
					
					
				}
				System.err.println("Request line: (" + line + ")");
				if (line.length() == 0)
					break;
				
			}
			catch (Exception e)
			{
				System.err.println("Request error: " + e);
				break;
			}
		}
		return line;
	}

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
		if(contentType != "Not Found") {                 // If the content type is not "Not Found" which it means it was found
		os.write("HTTP/1.1 200 OK\\n".getBytes());       // then it is a 200 status code 
		os.write("Date: ".getBytes());
		os.write((df.format(d)).getBytes());
		os.write("\n".getBytes());
		os.write("Server: Jon's very own server\n".getBytes());
		// os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
		// os.write("Content-Length: 438\n".getBytes());
		os.write("Connection: close\n".getBytes());
		os.write("Content-Type: ".getBytes());
		
		os.write(contentType.getBytes());
		os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
		return;
		}
		else {                                            // If it was not found then it is a status code 404.
			os.write("HTTP/1.1 404 Not Found\\n".getBytes());
			os.write("Date: ".getBytes());
			os.write((df.format(d)).getBytes());
			os.write("\n".getBytes());
			os.write("Server: Jon's very own server\n".getBytes());
			// os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
			// os.write("Content-Length: 438\n".getBytes());
			os.write("Connection: close\n".getBytes());
			os.write("Content-Type: ".getBytes());
			
			os.write(contentType.getBytes());
			os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
			return;	
		}
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
		
		Date d = new Date();
		DateFormat df = DateFormat.getDateTimeInstance();
		
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		os.write("<html><head></head><body>\n".getBytes());
		
		os.write("<p>hello world, the current date is :  <cs371date> . The server is <cs371server>.</p>".getBytes());
		os.write("</body></html>\n".getBytes());
	}
	private void writeContent2(OutputStream os) throws Exception
	{
		
		
		os.write("<html><head></head><body>\n".getBytes());
		os.write("<h3>Error 404, Page Not Found!</h3>\n".getBytes());
		os.write("</body></html>\n".getBytes());
	}

} // end class
