### Setup

1/ Cloning the repo and build 

<div class="highlighter-rouge"><div class="highlight"><pre class="highlight" style="background-color: greydark;"><code style="font-size: 12.0px;">COMP10619:$ <span style="font-weight: bold">git clone https://github.com/ptabasso2/springTchibo.git</span>
COMP10619:$ <span style="font-weight: bold">cd springTchibo</span>
COMP10619:$ <span style="font-weight: bold">./gradlew build</span>
</code></pre></div></div>


2/ Make sure the DD agent is running and listening on port **8126** 

3/ Start the server with agent instrumentation
<div class="highlighter-rouge"><div class="highlight"><pre class="highlight" style="background-color: greydark;"><code style="font-size: 12.0px;">COMP10619:$ <span style="font-weight: bold">java -javaagent:./dd-java-agent-0.17.0.jar -jar build/libs/springtchibo-1.0.jar
-Ddd.agent.host=localhost -Ddd.agent.port=8126 -Ddd.service.name=springTchibo
-jar build/libs/springtchibo-1.0.jar --server.port=9393</span>
</code></pre></div></div>

4/ Testing the application
In another termnial window lauch the following command:

<div class="highlight"><pre class="highlight" style="background-color: greydark;"><code style="font-size: 12.0px;">COMP10619:$ <span style="font-weight: bold">curl localhost:9393/ServiceC</span>
</code></pre></div>

This should normally display this type of lines:

<figure>
<img src="https://github.com/ptabasso2/springTchibo/blob/master/Result.png" style="height: 200px; width: auto; margin-left: 150px">
</figure>
