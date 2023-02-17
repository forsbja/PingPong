all:
	kotlinc PingPong.kt -include-runtime -d PingPong.jar

clean:
	rm *.jar