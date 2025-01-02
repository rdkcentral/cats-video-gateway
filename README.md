# Video-Gateway Microservice
The Video-Gateway Microservice supports interfacing to Video Encoders for fetching video stream url, snapshot url and supported resolutions for devices under test. To achieve this, the Video-Gateway Microservice interfaces with programable Video Encoders deployed to the rack. A device under test is directly connected an individual outlet on the Video Encoder, allowing for streaming the video output as well as capturing snapshots in desired resolutions for individual devices under test. Custom slot mapping is supported to ensure the microservice is aware of all devices under test connected to each Video Encoder deployed to the rack and the individual outlet assignments for each device under test.

<br><br>

## Development Setup

Build using `mvn clean install`

Run using `java -jar target/video-gateway.jar`

### Running locally
```
mvn spring-boot:run
```

Once running, application will be locally accessible at http://localhost:9025/

<br><br>

## Building

Build the project using `mvn clean install`.
Copy the built jar file into the corresponding directory structure as required by the Dockerfile.

    docker build -t="/video-gateway" .


<br><br>

## Deploying

Copy the `mappings.json` file to the `/opt/data/video-gateway/config` (or as required by the docker-compose file).
Video-gateway-ms offers the capability to customize any slot's device and outlet reference.
This allows for flexibility in slot capability for non traditional rack deployments.
For instance, say you have a single 4 outlet video encoder device and 5 slots on your rack.
If you want device 5 to have power capability but it is not neccessary for device 2,
you could create a slot mapping that allows for this with the following JSON:

 ```
 {
  "slots": {
    "1": "1:1",
    "2": "1:2",
    "3": "1:3",
    "4": "1:4",
    "5": "3:1",
    "6": "3:2"
  },
  "devices": [
    {
      "id": 1,
      "internalIp": "192.168.100.101",
      "natPort": "28101",
      "natSSLPort": "28401",
      "natRTSPPort": "25501",
      "type": "Axis.P7216",
      "maxPort": 4
    },
    {
      "id": 2,
      "internalIp": "192.168.100.102",
      "internalPort": "80",
      "natPort": "28102",
      "natSSLPort": "28402",
      "natRTSPPort": "25502",
      "type": "Axis.P7216",
      "maxPort": 4
    },
    {
      "id": 3,
      "internalIp": "192.168.100.103",
      "internalPort": "80",
      "natPort": "28103",
      "natSSLPort": "28403",
      "natRTSPPort": "25503",
      "type": "Axis.P7216",
      "maxPort": 4
    }
  ],
  "rackHost": "<variable detail>",
  "rackIp": "<variable detail>",
  "useProxy": "<variable detail>",
  "proxyBaseUrl" : "<variable detail>"
}
 ```

<br><br>

## Access the Swagger Documentation

The Swagger Documentation for the Video-Gateway Microservice can be accessed at https://localhost:9025/swagger-ui.html when running locally. Default swagger path is **/swagger-ui.html**.


<br><br>

## Accessing Video Streams

Video stream urls provided via API can be used in browser to interface with the video stream for Hanwha or Axis Video Encoders. Hanwha Video Encoders may require additional login to stream video via browser.

<br><br>

## NGINX Configuration

NGINX is used to support a unified path for communication to the rack microservices as well as communication between the rack microservices. NGINX configuration for video-gateway can be found at [video-gateway.conf](conf/video-gateway.conf). This configuration file is used to route requests to the video-gateway microservice.


<br><br>

### Video gateway Health Check

    http://localhost:9025/actuator/health
    
    
    

## Video Device Types

Each video encoder device specified in the mappings.json file must also include a type, the supported types are listed below:


| Hardware Type | Hardware Code Identifier | Connection Protocol |  Documentation |
| --- | --- | --- | --- | 
|Axis Video Encoder|Axis.P7216|HTTP|[Axis Encoder Docs](https://www.axis.com/dam/public/68/f1/ad/axis-p7216-video-encoder--user-manual-en-US-102065.pdf)|
|Axis Video Encoder|Axis.P7316|HTTP|[Axis Encoder Docs](https://help.axis.com/en-us/axis-p7316)|
|Axis Camera|Axis.FA54|HTTP|[Axis Camera Docs](https://help.axis.com/en-us/axis-fa54-main-unit)|
|Hanwha Video Encoder|Hanwha.SPE-1620|HTTP|[Hanwha Encoder Docs](https://www.hanwhavision.com/en/products/peripherals/encoder/A/spe-1620/)|

<br><br>
