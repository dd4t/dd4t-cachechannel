# dd4t-cachechannel

This project contains a custom CacheConnector, which is used by the Tridion microservices to send and receive messages to / from Apache ActiveMQ. The difference with the out of the box CacheConnector is that the message is sent in text format instead of a binary format. As a result, the messages can also be read by a .NET Web Application, for example when running DD4T.


## Installation notes

See https://github.com/dd4t/DD4T.Caching.ApacheMQ for installation notes.

