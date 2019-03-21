package hello;


import datadog.opentracing.DDSpan;
import datadog.opentracing.DDTracer;
import datadog.trace.api.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;



@RestController
public class GreetingController {


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    HttpServletRequest request;


    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);

    private Tracer tracer;
    private DDTracer ddtracer = new DDTracer("ServiceCorrelation");

    @RequestMapping("/ServiceC")
    public String serviceC() throws InterruptedException {

        //tracer = GlobalTracer.get();


//        MDC.put("ddTraceID", "ddTraceID:" + String.valueOf(CorrelationIdentifier.getTraceId()));
//        MDC.put("ddSpanID", "ddSpanID:" + String.valueOf(CorrelationIdentifier.getSpanId()));



//        Span span2 = tracer.buildSpan("ServiceC").start();
        DDSpan span2 = ddtracer.buildSpan("ServiceC").start();

        span2.setTag("Hola", "Hombre");
        span2.setTag("first", "second").setTag("third", 12);
        span2.setBaggageItem("one", "secretinfo");


        MDC.put("ddTraceID", "ddTraceID:" + ((DDSpan) span2).getTraceId() );
        MDC.put("ddSpanID", "ddSpanID:" +  ((DDSpan) span2).getSpanId() );



//        MDC.put("ddTraceID", "ddTraceID:" + String.valueOf(CorrelationIdentifier.getTraceId()));
//        MDC.put("ddSpanID", "ddSpanID:" +  String.valueOf(CorrelationIdentifier.getSpanId()));

        Map<String, String> map = new HashMap<>();

        ddtracer.inject(span2.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));
        //tracer.inject(span2.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));

        logger.info("In Service C ***************");

        //Span span = tracer.buildSpan("ServiceA").start();

        logger.info("Span2============= getTraceId() :" + ((DDSpan) span2).getTraceId());
        logger.info("Span2============= getSpanId() :" + ((DDSpan) span2).getSpanId());

//        logger.info("Span2============= getTraceId() :" + CorrelationIdentifier.getTraceId());
        //       logger.info("Span2============= getSpanId() :" + CorrelationIdentifier.getSpanId());


        //Creating a new span inside doSomeStuff()
        doSomeStuff((DDSpan) span2);

        //build HttpHeader
        HttpHeaders header = new HttpHeaders();
        header.setAll(map);

        //Sleep
        Thread.sleep(250L);

        //Post
        String rs = restTemplate.postForEntity("http://localhost:9393/ServiceD", new HttpEntity(header), String.class).getBody();

        //Removing MDC
        MDC.remove("ddTraceID");
        MDC.remove("ddSpanID");

        span2.finish();
        return "Test\n";
    }


    @RequestMapping("/ServiceD")
    public String serviceD() throws InterruptedException
    {

        Enumeration<String> e = request.getHeaderNames();
        Map<String, String> spanMap = new HashMap<>();

        while (e.hasMoreElements())
        {
            // add the names of the request headers into the spanMap
            String key = e.nextElement();
            String value = request.getHeader(key);
            spanMap.put(key, value);
        }

        /* 13 Mars
        Span span1 = ddtracer.buildSpan("ServiceD")
                .asChildOf(ddtracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(spanMap))).start();


        DDSpan span1 = ddtracer.buildSpan("ServiceD")
                .asChildOf(ddtracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(spanMap))).start();

        */

        DDSpan span1 = ddtracer.buildSpan("ServiceD")
                .asChildOf(ddtracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(spanMap))).start();

        /* 13 Mars
         */

//        MDC.put("ddTraceID", "ddTraceID : " + String.valueOf(CorrelationIdentifier.getTraceId()));
//        MDC.put("ddSpanID", "ddSpanID   : " + String.valueOf(CorrelationIdentifier.getSpanId()));


        MDC.put("ddTraceID", "ddTraceID : " + span1.getTraceId());
        MDC.put("ddSpanID", "ddSpanID   : " + span1.getSpanId());


        Thread.sleep(230L);

        logger.info("In Service D ***************");
        logger.info("Reading baggage item: " + span1.getBaggageItem("one"));


        MDC.remove("ddTraceID");
        MDC.remove("ddSpanID");


        span1.finish();

        return "test\n";
    }


    public void doSomeStuff(DDSpan span){
        DDSpan spanLoc = ddtracer.buildSpan("doSomeStuff").asChildOf(span).start();


        try {
            Thread.sleep(250L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("spanLoc============= getTraceId() : " +  spanLoc.getTraceId());
        logger.info("spanLoc============= getSpanId()  : " + spanLoc.getSpanId());

        spanLoc.finish();

    }



}



/* 1

//a. Code utilisant du MDC avec CorrelationIdentifier
//b. Utilisation mixte du java agent et de l'api de tracing.
//c. Si utilisé sans javaagent les correlation id ne sont pas corrects
//COMP10619:springTchibo pejman.tabassomi$ java -javaagent:/Users/pejman.tabassomi/DDproject/Docker/springbootlog/dd-java-agent-0.17.0.jar -Ddd.agent.host=localhost -Ddd.agent.port=8126 -Ddd.service.name=springTchibo -jar build/libs/springtchibo-1.0.jar --server.port=9393




package hello;


import datadog.trace.api.CorrelationIdentifier;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
import io.opentracing.util.GlobalTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@RestController
public class GreetingController {


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    HttpServletRequest request;


    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);

    private Tracer tracer;

    @RequestMapping("/ServiceC")
    public String serviceC() throws InterruptedException {

        tracer = GlobalTracer.get();

        Span span2 = tracer.buildSpan("ServiceC").start();
        span2.setTag("Key", "Value");
        span2.setBaggageItem("one", "secretinfo");


        MDC.put("ddTraceID", "ddTraceID:" + String.valueOf(CorrelationIdentifier.getTraceId()));
        MDC.put("ddSpanID", "ddSpanID:" +  String.valueOf(CorrelationIdentifier.getSpanId()));

        Map<String, String> map = new HashMap<>();

        tracer.inject(span2.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map));

        logger.info("In Service C ***************");
        logger.info("Span2============= getTraceId() :" + CorrelationIdentifier.getTraceId());
        logger.info("Span2============= getSpanId() :" + CorrelationIdentifier.getSpanId());


        //Creating a new span inside doSomeStuff()
        doSomeStuff(span2);

        //build HttpHeader
        HttpHeaders header = new HttpHeaders();
        header.setAll(map);

        //Sleep
        Thread.sleep(250L);

        //Post
        String rs = restTemplate.postForEntity("http://localhost:9393/ServiceD", new HttpEntity(header), String.class).getBody();

        //Removing MDC
        MDC.remove("ddTraceID");
        MDC.remove("ddSpanID");

        span2.finish();
        return rs;
    }


    @RequestMapping("/ServiceD")
    public String serviceD() throws InterruptedException
    {

        Enumeration<String> e = request.getHeaderNames();
        Map<String, String> spanMap = new HashMap<>();

        while (e.hasMoreElements())
        {
            // add the names of the request headers into the spanMap
            String key = e.nextElement();
            String value = request.getHeader(key);
            spanMap.put(key, value);
        }


        Span span1 = tracer.buildSpan("ServiceD")
                .asChildOf(tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(spanMap))).start();



        MDC.put("ddTraceID", "ddTraceID : " + String.valueOf(CorrelationIdentifier.getTraceId()));
        MDC.put("ddSpanID", "ddSpanID   : " + String.valueOf(CorrelationIdentifier.getSpanId()));


        Thread.sleep(230L);

        logger.info("In Service D ***************");
        logger.info("Reading baggage item: " + span1.getBaggageItem("one"));


        MDC.remove("ddTraceID");
        MDC.remove("ddSpanID");


        span1.finish();

        return "test\n";
    }


    public void doSomeStuff(Span span){
        Span spanLoc = tracer.buildSpan("doSomeStuff").asChildOf(span).start();


        try {
            Thread.sleep(250L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("spanLoc============= getTraceId() : " +  String.valueOf(CorrelationIdentifier.getTraceId()));
        logger.info("spanLoc============= getSpanId()  : " + String.valueOf(CorrelationIdentifier.getSpanId()));

        spanLoc.finish();

    }


}



 */