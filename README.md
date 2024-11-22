

# Alpha416: Application Layer Protocol Project

This project implements a multithreaded client-server application based on the **Alpha Vantage API**. It involves designing an application layer protocol, socket programming, and multithreading. The Alpha416 protocol is used to fetch currency exchange rates and natural gas price data from the Alpha Vantage server via a TCP connection.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Alpha416 Protocol](#alpha416-protocol)
   - [Request Methods](#request-methods)
   - [Response Codes](#response-codes)
3. [Features](#features)
4. [Setup and Usage](#setup-and-usage)
5. [Deliverables](#deliverables)
6. [Demonstration Requirements](#demonstration-requirements)

---

## Project Overview

Alpha416 is a custom application layer protocol that interacts with the **Alpha Vantage API** to fetch:
1. Exchange rates between two currencies.
2. Natural gas price data, including:
   - Price on a specific date.
   - Change between two dates.
   - Average price over a range of dates.

### Workflow
1. The **client** sends a request using the Alpha416 protocol.
2. The **server**:
   - Parses the request.
   - Queries the Alpha Vantage API.
   - Processes the response.
   - Sends the relevant data back to the client.
3. The **client** displays the server response to the user.

### API Endpoints Used:
- **Currency Exchange Rate**:  
  `https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=SOME_CURRENCY&to_currency=SOME_CURRENCY&apikey=YOUR_API_KEY`
  
- **Natural Gas Prices**:  
  `https://www.alphavantage.co/query?function=NATURAL_GAS&interval=monthly&apikey=YOUR_API_KEY`

---

## Alpha416 Protocol

### Request Methods
The Alpha416 protocol supports the following methods:
1. **EXC**: Fetch exchange rate data.  
   Example:  
   `Alpha416 EXC -from USD -to EUR -refresh`

2. **GAS**: Fetch natural gas price data.  
   Example:  
   `Alpha416 GAS -date 2023-08-01 -average 2023-12-01`

3. **QUIT**: Disconnect the client from the server.  
   Example:  
   `Alpha416 QUIT`

### Response Codes
1. **ALPHA_200 Success**: Request was successful, and data is included in the response body.
2. **ALPHA_400 Invalid Request**: Request is incorrectly formatted or missing required parameters.
3. **ALPHA_404 Not Found**: Requested data is unavailable.
4. **ALPHA_500 Server Fault**: An unexpected error occurred (e.g., API call limit exceeded).

---

## Features

- **Multithreading**: The server can handle multiple client connections simultaneously.
- **Timeouts**: Server sockets are configured with timeouts to ensure efficient resource utilization.
- **Error Handling**: Comprehensive error responses for invalid requests or server faults.
- **User-Friendly Interface**: Simple and clear commands for the client application.

---

## Setup and Usage

### Prerequisites
- **Java**: Use Java with an IDE like Eclipse, NetBeans, or IntelliJ IDEA.
- **Alpha Vantage API Key**: Generate a free API key [here](https://www.alphavantage.co/support/#api-key).

### Steps
1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd Alpha416


![image](https://github.com/user-attachments/assets/705f652b-7c88-4f3b-a312-33ad9940ea71)
![image](https://github.com/user-attachments/assets/1a699818-8de2-4130-8129-d9c262cabeed)
![image](https://github.com/user-attachments/assets/60f547cf-1a88-43ef-b298-bcb4a66aa487)
![image](https://github.com/user-attachments/assets/ec1c4b3b-037d-4a21-9972-9c94229cc87f)
