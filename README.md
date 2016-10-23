# Tracing using OSGi Weaving Hook and ASM

This is a template project that shows how to implement method level tracing in an OSGi environment
using ASM for byte code instrumentation.

This works around <http://forge.ow2.org/tracker/?func=detail&aid=317583&group_id=23&atid=100023> (which is totally unclear to me if it is a bug in ASM or the Java verifier) by starting the catch block in a constructor after the inserted code. So Java code like

```java
    Test() {
        super();
        ...more code...
    }
```

becomes

```java
    Test() {
        super();
        Tracer.enter(...);
        try {
           ...more code...
        } catch(Throwable t) {
           Tracer.exitException(...)
           throw t;   
       }
    }
```

instead of 

```java
    Test() {
        try {
           super();
           Tracer.enter(...);
           ...more code...
        } catch(Throwable t) {
           Tracer.exitException(...)
           throw t;   
       }
    }
```

