All: PlotLogic.jar

PlotLogic.jar: PlotLogic.java makefile
	javac PlotLogic.java
	jar -cfe PlotLogic.jar PlotLogic PlotLogic.java *.class
	rm -f *.class

clean:
	rm -f *.jar
	rm -f *.class


