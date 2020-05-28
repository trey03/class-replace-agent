# class-replace-agent

- mvn clean package assembly:single 
- set jvm options : 
~~~
  -javaagent:/Users/userhome/class-replace-agent-1.0-SNAPSHOT-jar-with-dependencies.jar
  -Dconfig_file=/Users/userhome/agent.properties
~~~
- agent.properties 配置
~~~
# 扫描路径
scan=com.itrey
# rep. 开头配置需要替换的类 比如下是用BTest替换ATest，要求BTest必须是继承ATest
rep.com.itrey.ATest=com.itrey.BTest
~~~
- run test : com.itrey.AgentTest