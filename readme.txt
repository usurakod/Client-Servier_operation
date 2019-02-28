# COEN 317: DistributedSystems
 
> Name - Uma Surakod
> Assignment name- Programming Assignment 1
> Date - 2/2/2019

# High-level description of the assignment and what your program(s) does
- In this program client will make HTTP request, server accepts the request and provide the requested page, 
  In this project by default we are providing the https://scu.edu page.
- Each request is handled using thread.
- Code check for request is GET otherwise throw an error.
- handled the file exists or not in the path which we are searching
- file types of htm,html,jpg,jpeg,png,css,gif are handled 

# Brief explanation about the program:
- Server socket has an object myServSocket which takes the port number as an argument from command line. It listens for incoming connections
- There is a while loop to listen infinitely 
  - A Socket object conSocket is created to that accepts the incoming client request 
  - An object myRequest is created to process HTTP request message
  - A thread is created to process each request and is started
- Each request is handled by a method processRequest() which is in HttpRequest class, this class implements runnable interface
- In processRequest() method, we get reference for server socket input and output streams
- Taking the initial request line of the HTTP request message and extract the filename, which is then parsed and type of content is determined
- the requested resource is written to a buffer and displayed and connection is closed

- Http 404,403,200 are handled
- for Http 400 error also 404 is returned

 




 •A list of submitted files •Instructions for running your program •Any otherinformation you want usto know 