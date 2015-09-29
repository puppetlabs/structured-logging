# puppetlabs/structured-logging

`structured-logging` is a library that helps you to:
 - write arbitrary JSON to your logs, making it easier to interface with log analysis tools.
 - write messages to arbitrarily named loggers, instead of just the one named
   after the namespace in which the log statement appears.

It is built on `clojure.tools.logging`, but it only works with logback. 

## Usage

### maplog
The `maplog` macro is very similar in convention to the clojure.tools.logging
`logp` and `logf` macros. It is typically invoked like this:

    (maplog :warn {:user-service "https://...", :status 503, :elapsed 27}
            "Failed to query user-service {user-service}. Response: status {status}")

The api should be generally familiar. The second parameter is the structured
data you want to log; any clojure map is ok, as long as
[Cheshire](https://github.com/dakrone/cheshire) can deal with it. The final
parameter is the log message, which is formatted using string interpolation
against the keys of the map.

### Named loggers

When you would pass a log-level parameter, you may instead supply a vector of
`[:custom-logger :level]`. This will log a message to the logger named
"custom-logger". Then you can specifically address that logger in your logback
configuration (to redirect it to a different log file, for example).

We also supply new versions of `logp` and `logf` to support this style of invocation.

## Common fields

If you use the recommended logger configuration, as described below, you will
see the following fields in each JSON log message:

* @timestamp
* message
* logger_name
* thread_name
* level
* level_value (numeric, suitable for sorting)
* stack_trace

Additional relevant fields may be added in any given message, but this base set
will always be present.

## Configuration
### JSON text

If you want to log JSON data where you would otherwise log regular text, replace the `encoder` element in
your `logback.xml` with this one:

    <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
      <providers>
        <timestamp/>
        <message/>
        <loggerName/>
        <threadName/>
        <logLevel/>
        <logLevelValue/>
        <stackTrace/>
        <logstashMarkers/>
      </providers>
    </encoder>

Even though this says 'logstash' on it, it works completely independently from
any log aggregation system. The final `<logstashMarkers/>` element is
important - that inserts our custom properties into each json message.

## Logstash integration

You can also log directly to logstash with an appender configured like this:

    <appender name="stash" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
      <remoteHost>my.great.logstash.server.com</remoteHost>
      <port>4560</port>

      <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
        <providers>
          <timestamp/>
          <message/>
          <loggerName/>
          <threadName/>
          <logLevel/>
          <logLevelValue/>
          <stackTrace/>
          <logstashMarkers/>
        </providers>
      </encoder>
    </appender>

You will also need to add a reference to the appender from the `<root>` element:

    <root>
      ... 
      <appender-ref ref="stash" />
    </root>



## Log Output

We are using a json encoder that comes with the `logstash-logback` integration
library. (https://github.com/logstash/logstash-logback-encoder) This can be used
in conjunction with the logstash tcp appender, or in conjunction with a typical
file appender. In either case, the json for the above log message looks
something like this:

    {
      "@timestamp": "...",
      "level": "WARN",
      "remote": "https://...",
      "status": 503,
      "elapsed": 27
    }

The exact fields that show up depend on how you've configured the encoder;
'@timestamp' is a logstash convention.

## Answers to expected questions

*Q*: Can I embed arbitrary structures in there? 
*A*: You can log anything that cheshire can serialize, but you may wish to stick
 to simple key-value formats to keep your logs easy to analyze.

*Q*: What happens when I log a map with :level as the key?
*A*: Don't do that. It's not yet clear how this case should be handled. For now,
avoid such keys. Namespacing your map keys may be prudent if you're worried
about collisions.

*Q*: Does this library require logstash?
*A*: No. It does depend on the json encoder that comes with the logback-logstash
integration library, but you can point the json it spits out at any old logback
appender. Critically, it has a feature that allows us to edit the json before it
gets written, which is where we add our custom information. We *could* write
such an encoder ourselves, but they already wrote it.

*Q*: Why didn't you use one of the other logback-json encoders?
*A*: Because those just encode the normal log event fields as json; the useful
feature here is the ability to include arbitrary data as part of the json.

## Tips

Structured logging is a slippery slope and should be applied where it has
clear benefits. While we've streamlined it a lot, this is heavier than regular
logging, and it's still non-application code that you have to scatter throughout
your program.

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
