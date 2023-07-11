# DocuWare Connector
Axon Ivy’s [DocuWare](https://start.docuware.com/) connector helps you accelerate your process automation initiatives by integrating DocuWare with your process application in the shortest possible time.

This connector:

-	Is based on REST webservice technologies.
-	Gives you access to sample DocuWare functionality
-	reduces the integration effort to a minimum: Use the demo implementation, there you will find examples for the calls

## Demo

1. Upload a document to a DocuWare file cabinet.
2. Provide a GUI to test some basic DocuWare calls.

## Setup

Before any interactions between the Axon Ivy Engine and DocuWare services can be run, they have to be introducted to each other. This can be done as follows:

1. Get a DocuWare account and the DocuWare cloud `host-name`, `user-name`, `password` and `host-id` to use.

1. Override the global variables for `host-name`, `user-name`, `password` and `host-id` in the demo project as shown in the example below.

```
Variables:
  
  docuware-connector:
  
    host: <myhost>.docuware.cloud

    username: <myuser>
  
    # [password]
    password: <mypass>
    
    hostid: <mhostid>
```

If your REST URL does not follow the predefined REST URL pattern of this connector, you can change the URL in the Engine Cockpit. To change the URL in the Designer, you have to unpack the connector project and change it there.

Run `start.ivp` of the DocuWareDemo demo process to test your setup.

