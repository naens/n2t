all:
	javac -Xmaxerrs 1 -Xmaxwarns 1 *.java

proj:
	echo java > lang.txt
	zip project11.zip *.java lang.txt
	mv project11.zip /tmp
	rm lang.txt

clean:
	rm -f *.class *~
