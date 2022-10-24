# Mission Management Middleware for Underwater Autonomous Vehicles (AUV)
This software is the mission management middleware solution implemented as part of the SWARMs architecture for the [SWARMs]((http://swarms.eu/)) [European research project](https://cordis.europa.eu/project/id/662107/es).

It has three main components:
- The Missions and Tasks Reporter and Register and Reporter (MTRR), which is the main core for the mission management middleware.
- The Task Reporter, which is an auxiliary component for receiving and processing task progress information messages before delivering them to the MTRR.
- The Thrift Proxy, which is an auxiliary component to enable communications with the Mission Management Tool application via thrift.







# Draft

![image](https://user-images.githubusercontent.com/13553876/197519124-1f08fa96-c58c-4f2e-8235-92b76930ddbe.png)
![image](https://user-images.githubusercontent.com/13553876/197519156-b34c59a3-5c46-4d1d-a258-0c0e3336b7cb.png)
![image](https://user-images.githubusercontent.com/13553876/197519228-da180297-dfa5-4e34-9f02-65a7ec755c14.png)
![image](https://user-images.githubusercontent.com/13553876/197519269-962ffa0d-9e5a-49ef-b0ba-71ac98ce3563.png)
![image](https://user-images.githubusercontent.com/13553876/197519395-3bc4b74b-c8a7-4797-990a-053a1e10b815.png)

# Technology Readiness Level (TRL)
This software has reached a TRL 7: System prototype demonstration in operational environment ([EU definition](https://ec.europa.eu/research/participants/data/ref/h2020/wp/2014_2015/annexes/h2020-wp1415-annex-g-trl_en.pdf)).

# Usage
## Requirements
These components are part of the SWARMs architecture, and must be integrated with to be executed.

# References
- [Proposal of an Automated Mission Manager for Cooperative Autonomous Underwater Vehicles](https://doi.org/10.3390/app10030855)

# License
Parts copyrighted by Universidad Politécnica de Madrid (UPM) are distributed under a dual license scheme:
- For academic uses: Licensed under GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
- For any other use: Licensed under the Apache License, Version 2.0.
Parts copyrighted by DEMANES are distributed under the Apache License, Version 2.0.

For further details on the license, please refer to the [License](LICENSE.md) file.

# Intellectual Property
This software has been registered as an intellectual property by their authors under the registration M-007945/2018 at the Intellectual Property Registry of Madrid (Spain).
