This Java Applet helps to create SIPs appropriate for submittal to the Fedora DirIngest service.

Note that before the build will succeed, you'll have to use java's "keytool" 
to make a keystore so that the applet can be signed during the build.  

Instructions are in the comments of the build.xml.  After "ant dist" 
(java 1.5's compiler will give warnings which can be safely ignored), 
you can go into the dist/ directory and view Test.html with
your browser to see the interface.
