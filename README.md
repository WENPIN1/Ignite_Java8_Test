# Ignite_Java8_Test

[09:54:52] OS: Windows 10 10.0 amd64
[09:54:52] VM information: Java(TM) SE Runtime Environment 1.8.0_101-b13 Oracle Corporation Java HotSpot(TM) 64-Bit Server VM 25.101-b13

This test will show Java8 inline fault w/o copying the right argument to the callee method.


Exception in thread "main" java.lang.AssertionError: place <=0 0.0
	at my.own.AskRecord.setPlace(AskRecord.java:225)
	at my.trade.TM1.case6(TM1.java:333)
	at my.trade.TM1.mainTest(TM1.java:486)
	at my.trade.TM2.main(TM2.java:12)



VM_OPTIONS="-ea:my... -XX:CompileCommand=exclude,my/own/AskRecord.setPlace  -Xms2g -Xmx2g -XX:+UseG1GC -XX:+PrintGCDateStamps -verbose:gc  -DIGNITE_UPDATE_NOTIFIER=false -DmetricsLogFrequency=0 -XX:MaxGCPauseMillis=100"

Use "-XX:-inline" to pass the test.

